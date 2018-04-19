package ru.evilorange.redbutton.model

import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class Team(@Id var name:String, var password:String)