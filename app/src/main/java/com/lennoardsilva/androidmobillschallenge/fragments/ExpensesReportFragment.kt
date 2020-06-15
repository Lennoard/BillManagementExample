package com.lennoardsilva.androidmobillschallenge.fragments

import android.os.Bundle
import android.view.View
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObjects
import com.lennoardsilva.androidmobillschallenge.BillsApp
import com.lennoardsilva.androidmobillschallenge.R
import com.lennoardsilva.androidmobillschallenge.data.model.Expense
import kotlinx.android.synthetic.main.reports_fragment.*

class ExpensesReportFragment : BaseReportFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        reportCardTotalTransactions.setTitleText(getString(R.string.total_expenses))
    }

    override fun onResume() {
        super.onResume()

        retrieveData()
    }

    override fun retrieveData() {
        BillsApp.userExpensesRef.orderBy("time",
            Query.Direction.ASCENDING
        ).get().addOnCompleteListener { task ->
            if (!isAdded) return@addOnCompleteListener

            if (task.isSuccessful) {
                transactions.clear()
                transactions.addAll(task.result?.toObjects<Expense>()!!)
                populate()
            } else {
                showErrorDialog(task.exception?.message!!)
            }
        }
    }

    companion object {
        fun newInstance() = ExpensesReportFragment()
    }
}