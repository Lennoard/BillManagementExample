package com.lennoardsilva.androidmobillschallenge

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.util.TypedValue
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat

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

fun Context?.isDarkMode() : Boolean {
    if (this == null) return false
    return when (resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
        Configuration.UI_MODE_NIGHT_YES -> true
        else -> false
    }
}

fun Int.toPx(context: Context?): Int {
    if (context == null) return this
    return runCatching {
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            context.resources.displayMetrics
        ).toInt()
    }.getOrDefault(this)
}

fun Double.formatCurrency(context: Context) : String {
    val formatter = NumberFormat.getCurrencyInstance()
    return formatter.format(this)
}

infix fun Double.percentageChangeFrom(other: Double): Double {
    if (this == 0.0) return 100.0
    return ((other - this) / this) * 100
}

fun Double.round(decimals: Int = 2): Double {
    var bd = BigDecimal(this)
    bd = bd.setScale(decimals, RoundingMode.HALF_UP)
    return bd.toDouble()
}

@ColorInt
fun Context.getColorFromAttr(
    @AttrRes attrColor: Int,
    typedValue: TypedValue = TypedValue(),
    resolveRefs: Boolean = true
): Int {
    theme.resolveAttribute(attrColor, typedValue, resolveRefs)
    return typedValue.data
}

fun Int.timeString() : String {
    if (this > 9) return this.toString()
    return "0$this"
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