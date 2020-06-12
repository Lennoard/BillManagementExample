package com.lennoardsilva.androidmobillschallenge

import android.app.Activity
import android.content.Context
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun Context?.toast(messageRes: Int, length: Int = Toast.LENGTH_SHORT) {
    if (this == null) return
    toast(getString(messageRes), length)
}

fun Context?.toast(message: String?, length: Int = Toast.LENGTH_SHORT) {
    if (message == null || this == null) return
    val ctx = this

    GlobalScope.launch(Dispatchers.Main) {
        Toast.makeText(ctx, message, length).show()
    }
}

suspend inline fun Activity?.runSafeOnUiThread(crossinline uiBlock: () -> Unit) {
    this?.let {
        if (!it.isFinishing && !it.isDestroyed) {
            withContext(Dispatchers.Main) {
                runCatching(uiBlock)
            }
        }
    }
}