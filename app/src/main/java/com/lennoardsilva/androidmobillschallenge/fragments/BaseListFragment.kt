package com.lennoardsilva.androidmobillschallenge.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lennoardsilva.androidmobillschallenge.BillsApp
import com.lennoardsilva.androidmobillschallenge.R
import com.lennoardsilva.androidmobillschallenge.adapters.TransactionAdapter
import com.lennoardsilva.androidmobillschallenge.data.model.Transaction
import com.lennoardsilva.androidmobillschallenge.utils.goAway
import com.lennoardsilva.androidmobillschallenge.utils.show
import kotlinx.android.synthetic.main.base_list_fragment.*

interface OnLoadMoreListener {
    fun onLoadMore(currentPosition: Int = -1)
}

abstract class BaseListFragment : Fragment() {
    protected val transactions = mutableListOf<Transaction>()
    protected var adapter: TransactionAdapter? = null
    protected var maxResults = ITEMS_PER_PAGE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        childFragmentManager.clearFragmentResult(TRANSACTION_REQUEST_KEY)
        childFragmentManager.clearFragmentResultListener(TRANSACTION_REQUEST_KEY)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.base_list_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
    }

    abstract fun retrieveData(currentPosition: Int = 0)

    protected fun refresh(currentPosition: Int = 0) {
        if (transactions.isEmpty()) {
            baseListFragmentNothingFound.show()
        } else {
            baseListFragmentNothingFound.goAway()
        }

        if (currentPosition > -1 && currentPosition <= transactions.size -1) {
            baseListFragmentRecyclerView.scrollToPosition(currentPosition)
        }
    }

    private fun setupRecyclerView() {
        baseListFragmentRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(this.context, recyclerViewColumns)

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    if (dy > 0) {
                        baseListFragmentAdd?.shrink()
                    } else {
                        baseListFragmentAdd?.extend()
                    }
                }
            })
        }

        adapter = TransactionAdapter(
            requireActivity(),
            mutableListOf(),
            baseListFragmentRecyclerView,
            BillsApp.userExpensesRef
        )

        baseListFragmentRecyclerView.adapter = adapter
    }

    private val recyclerViewColumns: Int
        get () {
            val isLandscape = resources.getBoolean(R.bool.is_landscape)
            return if (isLandscape) 2 else 1
        }

    companion object {
        const val ITEMS_PER_PAGE = 10
        const val TRANSACTION_REQUEST_KEY = "transaction_request"
    }
}