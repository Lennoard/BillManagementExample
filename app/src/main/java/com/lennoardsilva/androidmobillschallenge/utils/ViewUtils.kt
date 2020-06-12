package com.lennoardsilva.androidmobillschallenge.utils

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar

typealias SnackbarDuration = BaseTransientBottomBar.Duration

inline fun EditText.afterTextChanged(crossinline afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) { }
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) { }
    })
}

fun View.goAway() { this.visibility = View.GONE }
fun View.hide() { this.visibility = View.INVISIBLE }
fun View.show() { this.visibility = View.VISIBLE }

fun View.snackbar(msg: String, @SnackbarDuration duration: Int = Snackbar.LENGTH_SHORT) {
    Snackbar.make(this, msg, duration).show()
}