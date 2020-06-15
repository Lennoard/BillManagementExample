package com.lennoardsilva.androidmobillschallenge.sheets

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lennoardsilva.androidmobillschallenge.BillsApp
import com.lennoardsilva.androidmobillschallenge.R
import com.lennoardsilva.androidmobillschallenge.toast
import com.lennoardsilva.androidmobillschallenge.utils.afterTextChanged
import com.lennoardsilva.androidmobillschallenge.utils.goAway
import com.lennoardsilva.androidmobillschallenge.utils.show
import kotlinx.android.synthetic.main.sheet_register.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Exception

interface OnUserCreatedListener {
    suspend fun onUserCreated(email: String, pass: String)
}

class RegisterSheetFragment : BottomSheetDialogFragment() {
    private var onUserCreatedListener: OnUserCreatedListener? = null

    companion object {
        fun newInstance(): RegisterSheetFragment = RegisterSheetFragment()
    }

    override fun onAttach(context: Context) {
        try {
            onUserCreatedListener = context as OnUserCreatedListener
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("RegisterSheet", "Activity should implement OnUserCreatedListener")
        }

        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.sheet_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        registerEmail.afterTextChanged {
            if (it.length < 6) return@afterTextChanged
            registerEmailContainer.error = if (validateEmail(it)) null else {
                getString(R.string.invalid_email_message)
            }
        }

        registerPassword.afterTextChanged {
            if (it.length < 3) return@afterTextChanged

            registerPasswordContainer.error = if (it.length < 6) {
                getString(R.string.invalid_password_message)
            } else null
        }

        registerCancel.setOnClickListener {
            dismiss()
        }

        registerButton.setOnClickListener {
            val email = registerEmail.text.toString()
            val pass = registerPassword.text.toString()

            if (email.isEmpty() || pass.isEmpty()) {
                requireContext().toast(getString(R.string.check_input_fields))
            } else {
                registerProgress.show()
                BillsApp.auth.createUserWithEmailAndPassword(email, pass)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            dismiss()
                            requireContext().toast(R.string.registration_complete)
                            GlobalScope.launch {
                                onUserCreatedListener?.onUserCreated(email, pass)
                            }
                        } else {
                            val message = task.exception?.message
                                ?: getString(R.string.sign_in_error_unknown)

                            showErrorDialog(getString(R.string.failure_format, message))
                            registerProgress.goAway()
                        }
                    }
            }
        }

    }

    private fun showErrorDialog(reason: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setIcon(R.drawable.ic_error)
            .setTitle(R.string.error)
            .setMessage(getString(R.string.failure_format, reason))
            .setPositiveButton(android.R.string.ok) { _, _ -> }
            .show()
    }

    private fun validateEmail(email: String) : Boolean {
        if (email.contains("@")) {
            // abc@gmailbutalso@outlook.com
            if (email.count { it == '@' } > 1) return false

            // pass
            return email.indexOfLast {
                it == '.'
            } > email.indexOf("@")
        }
        return false
    }
}