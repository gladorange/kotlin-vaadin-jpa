package ru.evilorange.redbutton.view

import com.vaadin.navigator.View
import com.vaadin.navigator.ViewChangeListener
import com.vaadin.spring.annotation.SpringView
import com.vaadin.ui.VerticalLayout

@SpringView(name="")
class UserView : VerticalLayout(), View {


    override fun enter(event: ViewChangeListener.ViewChangeEvent?) {

    }
}