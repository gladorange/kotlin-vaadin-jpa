package ru.evilorange.redbutton

import com.vaadin.annotations.Push
import com.vaadin.annotations.Theme
import com.vaadin.server.VaadinRequest
import com.vaadin.shared.communication.PushMode
import com.vaadin.shared.ui.ui.Transport
import com.vaadin.spring.annotation.SpringUI
import com.vaadin.spring.navigator.SpringNavigator
import com.vaadin.ui.UI
import com.vaadin.ui.VerticalLayout
import org.springframework.beans.factory.annotation.Autowired
import ru.evilorange.redbutton.model.Team

@SpringUI
@Push(value = PushMode.AUTOMATIC,transport = Transport.WEBSOCKET_XHR)
@Theme("def")
class MyVaadinUI : UI() {

    @Autowired
    lateinit var navigator: SpringNavigator

    var cTeam:Team? = null

    override fun init(request: VaadinRequest?) {
        val main = VerticalLayout()
        main.setMargin(false)
        navigator.init(this,main)
        content = main
    }

    var UI.getTeam:Team?
        get() = (this as MyVaadinUI).cTeam
        set(t) {
            (this as MyVaadinUI).cTeam = t
        }

}



