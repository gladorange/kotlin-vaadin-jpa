package ru.evilorange.redbutton

import com.vaadin.server.VaadinRequest
import com.vaadin.spring.annotation.SpringUI
import com.vaadin.spring.navigator.SpringNavigator
import com.vaadin.ui.UI
import com.vaadin.ui.VerticalLayout
import org.springframework.beans.factory.annotation.Autowired
import ru.evilorange.redbutton.model.Team

@SpringUI
class MyVaadinUI : UI() {

    @Autowired
    lateinit var navigator: SpringNavigator

    var cTeam:Team? = null

    override fun init(request: VaadinRequest?) {
        val main = VerticalLayout()
        navigator.init(this,main)
    }

    var UI.getTeam:Team?
        get() = (this as MyVaadinUI).cTeam
        set(t) {
            (this as MyVaadinUI).cTeam = t
        }

}



