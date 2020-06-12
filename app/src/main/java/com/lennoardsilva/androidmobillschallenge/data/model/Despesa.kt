package com.lennoardsilva.androidmobillschallenge.data.model

import com.google.firebase.Timestamp
import java.io.Serializable

data class Despesa(
    var valor: Double = 0.0, // value
    var descricao: String = "", // description
    var data: Timestamp? = null, // date
    var time: Long = 0,
    var pago: Boolean = false // paid
) : Serializable