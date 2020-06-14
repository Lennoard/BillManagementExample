package com.lennoardsilva.androidmobillschallenge.data.model

import java.io.Serializable

data class Attachment(
    var downloadURL: String = "",
    var storagePath: String = "",
    var name: String = ""
) : Serializable