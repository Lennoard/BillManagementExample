package com.lennoardsilva.androidmobillschallenge.activities

import android.net.ConnectivityManager
import android.net.Network
import android.os.Bundle
import android.os.Handler
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.AuthResult
import com.lennoardsilva.androidmobillschallenge.BillsApp
import com.lennoardsilva.androidmobillschallenge.R
import com.lennoardsilva.androidmobillschallenge.runSafeOnUiThread
import com.lennoardsilva.androidmobillschallenge.sheets.OnUserCreatedListener
import com.lennoardsilva.androidmobillschallenge.sheets.RegisterSheetFragment
import com.lennoardsilva.androidmobillschallenge.toast
import com.lennoardsilva.androidmobillschallenge.utils.afterTextChanged
import com.lennoardsilva.androidmobillschallenge.utils.goAway
import com.lennoardsilva.androidmobillschallenge.utils.show
import com.lennoardsilva.androidmobillschallenge.utils.snackbar
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class LoginActivity : BaseActivity(), OnUserCreatedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loginEmail.afterTextChanged {
            if (it.length < 6) return@afterTextChanged
            loginEmailContainer.error = if (validateEmail(it)) null else {
                getString(R.string.invalid_email_message)
            }
        }

        loginPassword.afterTextChanged {
            if (it.length < 3) return@afterTextChanged

            loginPasswordContainer.error = if (it.length < 6) {
                getString(R.string.invalid_password_message)
            } else null
        }

        loginButton.setOnClickListener {
            val email = loginEmail.text.toString()
            val pass = loginPassword.text.toString()

            if (email.isEmpty() || pass.isEmpty()) {
                loginRegisterCard.snackbar(getString(R.string.check_input_fields))
            } else {
                loginProgress.show()
                BillsApp.auth.signInWithEmailAndPassword(email, pass)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            sendUserToFirebaseAndThen {
                                toast(
                                    getString(
                                        R.string.welcome_toast,
                                        BillsApp.findCurrentAccount().displayName
                                    )
                                )
                            }
                        } else {
                            val message = getLoginFailureMessage(task)
                            showErrorDialog(getString(R.string.sign_in_error, message))
                            loginProgress.goAway()
                        }
                }
            }
        }

        loginRegister.setOnClickListener {
            RegisterSheetFragment.newInstance().show(supportFragmentManager, "Login")
        }
    }

    override suspend fun onUserCreated(email: String, pass: String) {
        runSafeOnUiThread {
            loginEmail.setText(email)
            loginPassword.setText(pass)
            Handler().postDelayed({ loginButton.performClick() }, 350)
        }
    }

    override val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            GlobalScope.launch {
                runSafeOnUiThread {
                    if (networkFlag) {
                        loginRegisterCard.snackbar(getString(R.string.connected))
                    }

                    loginButton.isEnabled = true
                }
            }
        }

        override fun onLost(network: Network?) {
            GlobalScope.launch {
                runSafeOnUiThread {
                    networkFlag = true
                    loginButton.isEnabled = false
                    loginRegisterCard.snackbar(getString(R.string.network_unavailable), Snackbar.LENGTH_LONG)
                }
            }
        }
    }

    private fun sendUserToFirebaseAndThen(block: () -> Unit) {
        val localUser = BillsApp.findCurrentAccount()
        BillsApp.userRef.set(localUser.toMap()).addOnSuccessListener {
            block()
        }.addOnFailureListener {
            showErrorDialog(it.message!!)
            loginProgress.goAway()
        }
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

    private fun getLoginFailureMessage(task: Task<AuthResult>): String {
        return when {
            task.exception == null -> {
                getString(R.string.sign_in_error_unknown)
            }
            task.exception!!.message == null -> {
                getString(R.string.sign_in_error_unknown)
            }
            else -> when {
                task.exception!!.message!!.contains("deleted") -> {
                    getString(R.string.sign_in_error_no_user)
                }

                task.exception!!.message!!.contains("password is invalid") -> {
                    getString(R.string.sign_in_error_password)
                }

                else -> getString(R.string.sign_in_error_unknown)
            }
        }
    }
}

