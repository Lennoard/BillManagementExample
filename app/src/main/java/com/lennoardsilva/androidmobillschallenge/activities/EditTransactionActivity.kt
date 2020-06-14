package com.lennoardsilva.androidmobillschallenge.activities

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.MenuItem
import com.google.firebase.Timestamp
import com.lennoardsilva.androidmobillschallenge.BillsApp
import com.lennoardsilva.androidmobillschallenge.R
import com.lennoardsilva.androidmobillschallenge.data.model.Expense
import com.lennoardsilva.androidmobillschallenge.data.model.Revenue
import com.lennoardsilva.androidmobillschallenge.data.model.Transaction
import com.lennoardsilva.androidmobillschallenge.timeString
import com.lennoardsilva.androidmobillschallenge.toast
import com.lennoardsilva.androidmobillschallenge.utils.*
import kotlinx.android.synthetic.main.activity_edit_transaction.*
import java.util.*

class EditTransactionActivity : BaseActivity() {
    private var transaction: Transaction? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_transaction)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        transaction = intent.getSerializableExtra(EXTRA_TRANSACTION) as Transaction?
        if (transaction == null) {
            toast(R.string.error)
            finish()
        } else {
            editTransactionDescription.setText(transaction!!.descricao)
            editTransactionValue.setText(transaction!!.valor.toString())
            editTransactionTime.setText(Utils.dateMillisToString(transaction!!.time))
            editTransactionTime.setOnFocusChangeListener { _, hasFocus -> 
                if (hasFocus) {
                    pickDate()
                }
            }

            when (transaction) {
                is Expense -> {
                    editTransactionPaid.setText(R.string.paid)
                    editTransactionPaid.isChecked = (transaction as Expense).pago
                }

                is Revenue -> {
                    editTransactionPaid.setText(R.string.received)
                    editTransactionPaid.isChecked = (transaction as Revenue).recebido
                }
            }

            editTransactionDone.setOnClickListener { 
                if (validateInputFields()) {
                    saveTransaction()
                } else {
                    editTransactionDone.snackbar(getString(R.string.check_input_fields))
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun saveTransaction() {
        editTransactionProgress.show()

        val ref = if (transaction is Expense) {
            BillsApp.userExpensesRef
        } else {
            BillsApp.userRevenuesRef
        }
        val date = Utils.parseDate(editTransactionTime.text.toString())
        transaction?.apply {
            descricao = editTransactionDescription.text.toString()
            valor = editTransactionValue.text.toString().toDouble()

            date?.let {
                data = Timestamp(date)
                time = date.time
            }
        }

        when (transaction) {
            is Expense -> (transaction as Expense).pago = editTransactionPaid.isChecked
            is Revenue -> (transaction as Revenue).recebido = editTransactionPaid.isChecked
        }

        ref.document(transaction!!.id).set(transaction!!).addOnCompleteListener { task ->
            editTransactionProgress.hide()
            if (task.isSuccessful) {
                toast(R.string.done)
                finish()
            } else {
                showErrorDialog(getString(R.string.failure_format, task.exception?.message))
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun pickDate() {
        val today = GregorianCalendar.getInstance()
        val datePicker = DatePickerDialog(this, { _, year, month, dayOfMonth ->
            val timePicker = TimePickerDialog(this, { _, hourOfDay, minute ->
                val dateMillis = GregorianCalendar(
                    year, month, dayOfMonth,
                    hourOfDay, minute
                ).timeInMillis
                

                transaction?.time = dateMillis
                transaction?.data = Timestamp(Date(dateMillis))

                // 01/01/2010 01:01
                editTransactionTime.setText(
                    "${dayOfMonth.timeString()}/${month.timeString()}/$year ${hourOfDay.timeString()}:${minute.timeString()}"
                )
            }, 7, 0, true)
            timePicker.show()
        }, today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH))
        datePicker.show()
    }

    private fun validateInputFields() : Boolean {
        if (!editTransactionValue.validateDouble { it >= 0 }) {
            editTransactionValue.error = getString(R.string.invalid_value)
            return false
        }

        if (!editTransactionTime.validateDatetime()) {
            editTransactionTime.error = getString(R.string.invalid_value)
            return false
        }

        if (!editTransactionDescription.validateString { it.isNotEmpty() }) {
            editTransactionDescription.error = getString(R.string.invalid_value)
            return false
        }

        return true
    }
    
    companion object {
        const val EXTRA_TRANSACTION = "transaction"
    }
}
