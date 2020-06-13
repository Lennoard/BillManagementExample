package com.lennoardsilva.androidmobillschallenge.sheets

import android.widget.EditText
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lennoardsilva.androidmobillschallenge.R
import com.lennoardsilva.androidmobillschallenge.utils.Utils
import java.lang.Exception
import java.util.regex.Pattern

open class BaseSheetFragment : BottomSheetDialogFragment() {

    protected fun showErrorDialog(reason: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setIcon(R.drawable.ic_error)
            .setTitle(R.string.error)
            .setMessage(getString(R.string.failure_format, reason))
            .setPositiveButton(android.R.string.ok) { _, _ -> }
            .show()
    }

    protected fun EditText.validateString(predicate: (String) -> Boolean) : Boolean {
        return predicate(text.toString())
    }

    protected fun EditText.validateDouble(predicate: (Double) -> Boolean) : Boolean {
        return runCatching {
            predicate(text.toString().trim().toDouble())
        }.getOrDefault(false)
    }

    protected fun EditText.validateDatetime(pattern: String = "dd/MM/yyyy HH:mm") : Boolean {
        return runCatching {
            Utils.parseDate(text.toString().trim(), pattern) != null
        }.getOrDefault(false)
    }
}