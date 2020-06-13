package com.lennoardsilva.androidmobillschallenge.data.model

import com.google.firebase.Timestamp
import com.lennoardsilva.androidmobillschallenge.utils.Utils
import java.io.Serializable

typealias Expense = Despesa
typealias Revenue = Receita

open class Transaction(
    var id: String = Utils.randomId(),
    var valor: Double = 0.0, // value /* TODO: BigDecimal /*
    var descricao: String = "", // description
    @Transient var data: Timestamp? = null, // date
    var time: Long = 0,
    var attachmentUrls : MutableList<String> = mutableListOf()
) : Serializable

data class Despesa(
    var pago: Boolean = false // paid
) : Transaction()

data class Receita(
    var recebido: Boolean = false // received
) : Transaction()