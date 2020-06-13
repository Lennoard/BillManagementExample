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
import com.lennoardsilva.androidmobillschallenge.timeString
import com.lennoardsilva.androidmobillschallenge.toast
import com.lennoardsilva.androidmobillschallenge.utils.*
import kotlinx.android.synthetic.main.sheet_expense.*
import java.util.*

class ExpenseSheetFragment : BaseSheetFragment() {
    private var expense : Expense? = null
    private var editing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let { args ->
            (args.getSerializable(Consts.EXTRA_EXPENSE) as Expense?)?.let {
                expense = it
                editing = true
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.sheet_expense, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        expenseSheetTime.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                pickDate()
            }
        }

        expenseSheetDone.setOnClickListener {
            if (validateInputFields()) {
                if (expense == null) {
                    expense = Expense()
                }

                val date = Utils.parseDate(expenseSheetTime.text.toString())
                expense?.apply {
                    descricao = expenseSheetDescription.text.toString()
                    valor = expenseSheetValue.text.toString().toDouble()
                    pago = expenseSheetPaid.isChecked

                    date?.let {
                        data = Timestamp(date)
                        time = date.time
                    }
                }

                sendExpenseToDatabase()
            }
        }

        expenseSheetCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun sendExpenseToDatabase() = expense?.let {
        expenseSheetProgress.show()
        BillsApp.userExpensesRef.document(it.id).set(it).addOnCompleteListener { task ->
            expenseSheetProgress.hide()
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

                if (expense == null) {
                    expense = Expense()
                }

                expense?.time = dateMillis
                expense?.data = Timestamp(Date(dateMillis))

                // 01/01/2010 01:01
                expenseSheetTime.setText(
                    "${dayOfMonth.timeString()}/${month.timeString()}/$year ${hourOfDay.timeString()}:${minute.timeString()}"
                )
            }, 7, 0, true)
            timePicker.show()
        }, today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH))
        datePicker.show()
    }

    private fun validateInputFields() : Boolean {
        if (!expenseSheetValue.validateDouble { it >= 0 }) {
            expenseSheetValue.error = getString(R.string.invalid_value)
            return false
        }

        if (!expenseSheetTime.validateDatetime()) {
            expenseSheetTime.error = getString(R.string.invalid_value)
            return false
        }

        if (!expenseSheetDescription.validateString { it.isNotEmpty() }) {
            expenseSheetDescription.error = getString(R.string.invalid_value)
            return false
        }

        return true
    }

    companion object {
        fun newInstance(expense: Expense?): ExpenseSheetFragment = ExpenseSheetFragment().apply {
            arguments = Bundle().apply {
                putSerializable(Consts.EXTRA_EXPENSE, expense)
            }
        }
    }
}