package com.lennoardsilva.androidmobillschallenge.fragments

import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ktx.toObjects
import com.lennoardsilva.androidmobillschallenge.BillsApp
import com.lennoardsilva.androidmobillschallenge.R
import com.lennoardsilva.androidmobillschallenge.data.model.Expense
import com.lennoardsilva.androidmobillschallenge.data.model.Revenue
import com.lennoardsilva.androidmobillschallenge.sheets.CreateTransactionSheetFragment
import com.lennoardsilva.androidmobillschallenge.utils.show
import kotlinx.android.synthetic.main.base_list_fragment.*

class RevenuesFragment : BaseListFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        baseListFragmentAdd.hide()

        baseListFragmentSwipeLayout.apply {
            setColorSchemeResources(R.color.colorAccent, R.color.colorAccentVariant)
            setOnRefreshListener {
                retrieveRevenues()
            }
        }

        baseListFragmentAdd.setOnClickListener {
            val appCompatActivity = requireActivity() as AppCompatActivity
            val title = getString(R.string.new_revenue)
            CreateTransactionSheetFragment.newInstance(Revenue(), title).show(
                appCompatActivity.supportFragmentManager,
                "CreateTransactionSheetFragment"
            )
        }

        adapter?.setRef(BillsApp.userRevenuesRef)
        adapter?.setOnLoadMoreListener(object : OnLoadMoreListener {
            override fun onLoadMore(currentPosition: Int) {
                adapter?.addItem(null)
                maxResults += ITEMS_PER_PAGE
                retrieveRevenues(currentPosition)
            }
        })
    }

    override fun onResume() {
        super.onResume()

        retrieveRevenues()
    }

    private fun retrieveRevenues(currentPosition: Int = 0) {
        baseListFragmentSwipeLayout.isRefreshing = true
        BillsApp.userRevenuesRef.get().addOnCompleteListener { task ->
            if (!isAdded) return@addOnCompleteListener
            baseListFragmentSwipeLayout.isRefreshing = false

            if (task.isSuccessful) {
                adapter?.clearItems()
                transactions.clear()
                transactions.addAll(task.result?.toObjects<Revenue>()!!)

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
        fun newInstance() = RevenuesFragment()
    }
}