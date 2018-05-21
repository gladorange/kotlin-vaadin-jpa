package ru.evilorange.redbutton.view

import com.vaadin.navigator.View
import com.vaadin.navigator.ViewChangeListener
import com.vaadin.shared.ui.ContentMode
import com.vaadin.spring.annotation.SpringView
import com.vaadin.ui.*
import org.springframework.beans.factory.annotation.Autowired
import ru.evilorange.redbutton.Broadcaster
import ru.evilorange.redbutton.ButtonService
import ru.evilorange.redbutton.model.Team
import ru.evilorange.redbutton.model.TeamDao
import java.util.*

@SpringView(name="")
class UserView : VerticalLayout(), View, Broadcaster.BroadcastListener {
    @Autowired
    lateinit var teamDao:TeamDao
    @Autowired
    lateinit var buttService: ButtonService

    val teamLabel = Label()
    val stateLabel = Label(null, ContentMode.HTML)
    val bigRedButton = Button()
    val login = TextField("Команда")
    val password = PasswordField("Пароль")

    val loginLayout = loginLayout()
    val mainLayout = mainLayout()

    lateinit var currentTeam: Team
    var lastUpdateTime = 0L


    init {
        Broadcaster.register(this)
        setMargin(false)
    }

    override fun detach() {
        Broadcaster.unregister(this)
        super.detach()
    }

    private fun loginLayout(): Component {
        val loginBtn = Button("Войти")

        loginBtn.addClickListener({
            try {
                val team = teamDao.findById(login.value).get()
                if (team.password != password.value) {
                    Notification.show("Неправильный пароль", Notification.Type.WARNING_MESSAGE)
                } else {
                    removeAllComponents()
                    addComponent(mainLayout)
                    currentTeam = team
                    teamLabel.value = currentTeam.name + ", нажмите кнопку, чтобы ответить на вопрос!"
                }
            } catch (e: NoSuchElementException) {
                Notification.show("Неправильное имя команды", Notification.Type.WARNING_MESSAGE)
            }
        })

        login.styleName = "login-element"
        password.styleName = "login-element"
        loginBtn.styleName = "login-element"
        return VerticalLayout(login,password,loginBtn)
    }

    private fun mainLayout(): Component {
        teamLabel.addStyleName("team-label")
        stateLabel.addStyleName("state-label")
        bigRedButton.addStyleName("big-red-button")
        bigRedButton.addClickListener {
            bigRedButton.addStyleName("pressed")
            buttService.pressButton(currentTeam)
            bigRedButton.isEnabled = false
            teamLabel.value = ""
        }

        val verticalLayout = VerticalLayout(teamLabel, bigRedButton, stateLabel)
        verticalLayout.setMargin(false)
        return verticalLayout
    }

fun getStyle(team:Team):String {
    return if (team == currentTeam) {
        "font-weight:bold"
    } else{
        ""
    }
}

    override fun receiveBroadcast(message: ButtonService.ButtonNotification) {
        ui.access {
            if (message.time > lastUpdateTime) {
                lastUpdateTime = message.time

                stateLabel.value = "Результаты:<br/>" + message.teams.map { "<span style=${getStyle(it.key)}>${it.key
                        .name} " +
                        "- ${it
                        .value/1000.0}</span>" +
                        " с." }
                        .joinToString("<br/>")
            }

            if (message.reset) {
                bigRedButton.removeStyleName("pressed")
                stateLabel.value = ""
                bigRedButton.isEnabled = true
                teamLabel.value = currentTeam?.name + ", нажмите кнопку, чтобы ответить на вопрос!"
            }
        }
    }

    override fun enter(event: ViewChangeListener.ViewChangeEvent?) {
        removeAllComponents()
        addComponent(loginLayout)
    }
}