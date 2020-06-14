package com.lennoardsilva.androidmobillschallenge.fragments

import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ktx.toObjects
import com.lennoardsilva.androidmobillschallenge.BillsApp
import com.lennoardsilva.androidmobillschallenge.R
import com.lennoardsilva.androidmobillschallenge.data.model.Expense
import com.lennoardsilva.androidmobillschallenge.sheets.CreateTransactionSheetFragment
import com.lennoardsilva.androidmobillschallenge.utils.show
import kotlinx.android.synthetic.main.base_list_fragment.*

class ExpensesFragment : BaseListFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        baseListFragmentAdd.hide()

        baseListFragmentSwipeLayout.apply {
            setColorSchemeResources(R.color.colorAccent, R.color.colorAccentVariant)
            setOnRefreshListener {
                retrieveExpenses()
            }
        }

        baseListFragmentAdd.setOnClickListener {
            val appCompatActivity = requireActivity() as AppCompatActivity
            CreateTransactionSheetFragment.newInstance(null).show(
                appCompatActivity.supportFragmentManager,
                "ExpenseSheetFragment"
            )
        }

        adapter?.setRef(BillsApp.userExpensesRef)
        adapter?.setOnLoadMoreListener(object : OnLoadMoreListener {
            override fun onLoadMore(currentPosition: Int) {
                adapter?.addItem(null)
                maxResults += ITEMS_PER_PAGE
                retrieveExpenses(currentPosition)
            }
        })
    }

    override fun onResume() {
        super.onResume()

        retrieveExpenses()
    }

    private fun retrieveExpenses(currentPosition: Int = 0) {
        baseListFragmentSwipeLayout.isRefreshing = true
        BillsApp.userExpensesRef.get().addOnCompleteListener { task ->
            if (!isAdded) return@addOnCompleteListener
            baseListFragmentSwipeLayout.isRefreshing = false

            if (task.isSuccessful) {
                adapter?.clearItems()
                transactions.clear()
                transactions.addAll(task.result?.toObjects<Expense>()!!)

                if (transactions.isNotEmpty()) {
                    transactions.forEach { adapter?.addItem(it) }
                } else {
                    baseListFragmentNothingFound.show()
                }
                adapter?.setLoaded()
                refresh(currentPosition)
            } else {
                baseListFragmentNothingFound.show()
            }

            Handler().postDelayed({ baseListFragmentAdd.show() }, 100)
        }
    }

    companion object {

        fun newInstance() = ExpensesFragment().apply {
            arguments = Bundle().apply {

            }
        }
    }
}