package com.lennoardsilva.androidmobillschallenge.utils

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.lennoardsilva.androidmobillschallenge.R

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

fun ImageView.load(url: String) {
    Glide.with(this.context)
        .load(url)
        .centerCrop()
        .placeholder(R.drawable.placeholder_image)
        .error(R.drawable.broken_image)
        .into(this)
}

fun EditText.validateString(predicate: (String) -> Boolean) : Boolean {
    return predicate(text.toString())
}

fun EditText.validateDouble(predicate: (Double) -> Boolean) : Boolean {
    return runCatching {
        predicate(text.toString().trim().toDouble())
    }.getOrDefault(false)
}

fun EditText.validateDatetime(pattern: String = "dd/MM/yyyy HH:mm") : Boolean {
    return runCatching {
        Utils.parseDate(text.toString().trim(), pattern) != null
    }.getOrDefault(false)
}