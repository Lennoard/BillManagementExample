package com.lennoardsilva.androidmobillschallenge.sheets

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.Timestamp
import com.lennoardsilva.androidmobillschallenge.BillsApp
import com.lennoardsilva.androidmobillschallenge.R
import com.lennoardsilva.androidmobillschallenge.data.Consts
import com.lennoardsilva.androidmobillschallenge.data.model.Expense
import com.lennoardsilva.androidmobillschallenge.data.model.Revenue
import com.lennoardsilva.androidmobillschallenge.data.model.Transaction
import com.lennoardsilva.androidmobillschallenge.timeString
import com.lennoardsilva.androidmobillschallenge.toast
import com.lennoardsilva.androidmobillschallenge.utils.*
import kotlinx.android.synthetic.main.sheet_create_transaction.*
import java.util.*

class CreateTransactionSheetFragment : BaseSheetFragment() {
    private var transaction : Transaction? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let { args ->
            (args.getSerializable(Consts.EXTRA_TRANSACTION) as Transaction?)?.let {
                transaction = it
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.sheet_create_transaction, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            createTransactionSheetTitle.text = it.getString(Consts.EXTRA_TITLE)
        }

        when (transaction) {
            is Expense -> createTransactionSheetPaid.setText(R.string.paid)
            is Revenue -> createTransactionSheetPaid.setText(R.string.received)
        }

        createTransactionSheetTime.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                pickDate()
            }
        }

        createTransactionSheetDone.setOnClickListener {
            if (validateInputFields()) {
                val date = Utils.parseDate(createTransactionSheetTime.text.toString())
                transaction?.apply {
                    descricao = createTransactionSheetDescription.text.toString()
                    valor = createTransactionSheetValue.text.toString().toDouble()

                    date?.let {
                        data = Timestamp(date)
                        time = date.time
                    }
                }

                when (transaction) {
                    is Expense -> (transaction as Expense).pago = createTransactionSheetPaid.isChecked
                    is Revenue -> (transaction as Revenue).recebido = createTransactionSheetPaid.isChecked
                }

                sendTransactionToDatabase()
            }
        }

        createTransactionSheetCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun sendTransactionToDatabase() = transaction?.let {
        createTransactionSheetProgress.show()

        val ref = if (transaction is Expense) {
            BillsApp.userExpensesRef
        } else {
            BillsApp.userRevenuesRef
        }

        ref.document(it.id).set(it).addOnCompleteListener { task ->
            createTransactionSheetProgress.hide()
            if (task.isSuccessful) {
                dismiss()
            } else {
                requireContext().toast(R.string.failed_to_save)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun pickDate() {
        val today = GregorianCalendar.getInstance()
        val datePicker = DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
            val timePicker = TimePickerDialog(requireContext(), { _, hourOfDay, minute ->
                val dateMillis = GregorianCalendar(
                    year, month, dayOfMonth,
                    hourOfDay, minute
                ).timeInMillis

                transaction?.time = dateMillis
                transaction?.data = Timestamp(Date(dateMillis))

                // 01/01/2010 01:01
                createTransactionSheetTime.setText(
                    "${dayOfMonth.timeString()}/${month.timeString()}/$year ${hourOfDay.timeString()}:${minute.timeString()}"
                )
            }, 7, 0, true)
            timePicker.show()
        }, today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH))
        datePicker.show()
    }

    private fun validateInputFields() : Boolean {
        if (!createTransactionSheetValue.validateDouble { it >= 0 }) {
            createTransactionSheetValue.error = getString(R.string.invalid_value)
            return false
        }

        if (!createTransactionSheetTime.validateDatetime()) {
            createTransactionSheetTime.error = getString(R.string.invalid_value)
            return false
        }

        if (!createTransactionSheetDescription.validateString { it.isNotEmpty() }) {
            createTransactionSheetDescription.error = getString(R.string.invalid_value)
            return false
        }

        return true
    }

    companion object {
        fun newInstance(transaction: Transaction?, title: String) = CreateTransactionSheetFragment().apply {
            arguments = Bundle().apply {
                putSerializable(Consts.EXTRA_TRANSACTION, transaction)
                putSerializable(Consts.EXTRA_TITLE, title)
            }
        }
    }
}