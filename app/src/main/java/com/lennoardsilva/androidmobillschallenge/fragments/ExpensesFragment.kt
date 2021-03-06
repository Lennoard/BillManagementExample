package com.lennoardsilva.androidmobillschallenge.fragments

import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObjects
import com.lennoardsilva.androidmobillschallenge.BillsApp
import com.lennoardsilva.androidmobillschallenge.R
import com.lennoardsilva.androidmobillschallenge.data.model.Expense
import com.lennoardsilva.androidmobillschallenge.sheets.CreateTransactionSheetFragment
import com.lennoardsilva.androidmobillschallenge.utils.show
import kotlinx.android.synthetic.main.base_list_fragment.*

class ExpensesFragment : BaseListTransactionsFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        childFragmentManager.setFragmentResultListener(TRANSACTION_REQUEST_KEY, this) { _, _ ->
            retrieveData()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        baseListFragmentAdd.setOnClickListener {
            val title = getString(R.string.new_expense)
            CreateTransactionSheetFragment.newInstance(Expense(), title).show(
                childFragmentManager,
                "CreateTransactionSheetFragment"
            )
        }

        adapter?.setRef(BillsApp.userExpensesRef)
        adapter?.setOnLoadMoreListener(object : OnLoadMoreListener {
            override fun onLoadMore(currentPosition: Int) {
                adapter?.addItem(null)
                maxResults += ITEMS_PER_PAGE
                retrieveData(currentPosition)
            }
        })
    }

    override fun onResume() {
        super.onResume()

        retrieveData()
    }

    override fun retrieveData(currentPosition: Int) {
        baseListFragmentSwipeLayout.isRefreshing = true
        BillsApp.userExpensesRef.orderBy(
            "time",
            Query.Direction.DESCENDING
        ).limit(maxResults.toLong()).get().addOnCompleteListener { task ->
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
                refresh(currentPosition - 2)
            } else {
                baseListFragmentNothingFound.show()
            }

            Handler().postDelayed({ baseListFragmentAdd.show() }, 100)
        }
    }

    companion object {
        fun newInstance() = ExpensesFragment()
    }
}