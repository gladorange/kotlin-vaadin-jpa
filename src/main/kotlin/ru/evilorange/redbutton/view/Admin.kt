package ru.evilorange.redbutton.view

import com.vaadin.navigator.View
import com.vaadin.navigator.ViewChangeListener
import com.vaadin.server.VaadinService
import com.vaadin.shared.ui.ContentMode
import com.vaadin.spring.annotation.SpringView
import com.vaadin.ui.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.vaadin.dialogs.ConfirmDialog
import ru.evilorange.redbutton.Broadcaster
import ru.evilorange.redbutton.ButtonService
import ru.evilorange.redbutton.model.Team
import ru.evilorange.redbutton.model.TeamDao
import java.util.*
import javax.servlet.http.Cookie


@SpringView(name="adm")
class AdminView : VerticalLayout(), View, Broadcaster.BroadcastListener {
    @Autowired
    lateinit var teamDao: TeamDao
    @Autowired
    lateinit var buttonService: ButtonService
    val login = TextField("Логин")
    val newTeamLogin = TextField("Название новой команды")
    val newTeamPassword = TextField("Пароль новой команды")
    val password = PasswordField("Пароль")
    val loginLayout = loginLayout()

    val resetButton = Button("Следующий вопрос")
    val answers = Label("Ответы", ContentMode.HTML)
    val teams = Grid<Team>(Team::class.java)
    val addTeam = Button("Новая команда")
    val deleteTeam = Button("Удалить команду")
    val saveButton = Button ("Сохранить")
    val deleteAll = Button("Удалить всех")
    val mainLayout = mainLayout()
    val passwordField = TextField()

    @Value("\${evilorange.admin.login}")
    lateinit var adminLogin:String
    @Value("\${evilorange.admin.password}")
    lateinit var adminPassword:String

    var lastUpdateTime = 0L
    private fun loginLayout(): Component {
        val loginBtn = Button("Войти")

        loginBtn.addClickListener({
            try {
                if (adminPassword != password.value && adminLogin!=login.value) {
                    Notification.show("Неправильный пароль", Notification.Type.WARNING_MESSAGE)
                } else {
                    val cookie = Cookie(adminLogin, adminPassword)
                    cookie.maxAge = 60*24*30
                    cookie.path = "/"
                    VaadinService.getCurrentResponse().addCookie(cookie)
                    removeAllComponents()
                    addComponent(mainLayout)
                }
            } catch (e: NoSuchElementException) {
                Notification.show("Неправильное имя комнады", Notification.Type.WARNING_MESSAGE)
            }
        })
        return VerticalLayout(login,password,loginBtn)
    }

    private fun mainLayout(): Component {
        return VerticalLayout(answers, resetButton,teams,
                HorizontalLayout(newTeamLogin,newTeamPassword,addTeam),
                HorizontalLayout(saveButton,deleteTeam,
                deleteAll)

        )
    }

    init {
        deleteAll.addClickListener {
            ConfirmDialog.show(UI.getCurrent(),"Вы действительно хотите удалить все команды?", {
                if (it.isConfirmed) {
                    teamDao.deleteAll()
                    refresh()
                }
            })
        }

        deleteTeam.addClickListener {
            if (teams.selectedItems.isEmpty()) {
                Notification.show("Выберите элемент для удаления!", Notification.Type.WARNING_MESSAGE )
                return@addClickListener
            }

            teamDao.delete(teams.selectedItems.iterator().next())
            refresh()
        }

        saveButton.addClickListener {
            if (teams.selectedItems.isEmpty()) {
                Notification.show("Выберите элемент для сохранения!", Notification.Type.WARNING_MESSAGE )
                return@addClickListener
            }

            teamDao.save(teams.selectedItems.iterator().next())

            refresh()
        }

        addTeam.addClickListener {
            val newTeam = Team(newTeamLogin.value,newTeamPassword.value)
            try {
                teamDao.save(newTeam)
            } catch(e:Exception) {
                Notification.show("Команда с таким названием уже есть!", Notification.Type.WARNING_MESSAGE )
            }
            refresh()
        }

        teams.editor.addSaveListener {
            if (it.bean != null) {
                teamDao.save(it.bean)
                refresh()
            }
        }

        resetButton.addClickListener { buttonService.reset() }

        teams.editor.saveCaption = "Сохранить"
        teams.editor.cancelCaption = "Отменить"
        teams.editor.isEnabled = true

        Broadcaster.register(this)
    }

    override fun receiveBroadcast(message: ButtonService.ButtonNotification) {
        ui.access {
            if (message.time > lastUpdateTime) {
                lastUpdateTime = message.time
                answers.value = message.teams.map { "${it.key.name} - ${it.value / 1000.0} с." }
                        .joinToString("<br/>")
            }

            if (message.reset) {
                answers.value = ""
            }
        }
    }

    override fun detach() {
        Broadcaster.unregister(this)
        super.detach()
    }

    override fun enter(event: ViewChangeListener.ViewChangeEvent?) {
        refresh()
        removeAllComponents()
        val c = VaadinService.getCurrentRequest().cookies.filter { it.name == adminLogin }.any { it.value == adminPassword}

        if (!c) {
            addComponents(loginLayout)
        } else {
            addComponent(mainLayout)
        }


        answers.value = buttonService.getSortedTeamsMap().map { "${it.key.name} - ${it.value / 1000.0} с." }
                .joinToString("<br/>")
    }

    fun refresh() {
        teams.setItems(teamDao.findAll())
        teams.getColumn("name").caption = "Команда"
        teams.getColumn("password").setEditorComponent(passwordField).caption = "Пароль"
        teams.setColumnOrder("name","password")

    }

    }