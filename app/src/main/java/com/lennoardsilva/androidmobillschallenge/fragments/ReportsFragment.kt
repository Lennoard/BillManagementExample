package com.lennoardsilva.androidmobillschallenge.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObjects
import com.lennoardsilva.androidmobillschallenge.*
import com.lennoardsilva.androidmobillschallenge.data.model.Expense
import kotlinx.android.synthetic.main.reports_fragment.*
import java.util.*

class ReportsFragment : Fragment() {
    private val expenses = mutableListOf<Expense>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.reports_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onResume() {
        super.onResume()

        retrieveData()
    }

    private fun retrieveData() {
        BillsApp.userExpensesRef.orderBy("time",
            Query.Direction.ASCENDING
        ).get().addOnCompleteListener { task -> // TODO: 14/06/2020 month
            if (!isAdded) return@addOnCompleteListener

            if (task.isSuccessful) {
                expenses.clear()
                expenses.addAll(task.result?.toObjects<Expense>()!!)
                populate()
            }
        }
    }

    private fun populate() {
        val entries = mutableListOf<Entry>()
        var totalExpenses = 0.0
        var lastMonthTotal = 0.0
        var totalPaid = 0.0
        var totalToBePaid = 0.0
        var mostExpensive = 0.0
        var cheapest = Double.MAX_VALUE
        var oldest = System.currentTimeMillis()
        var mostRecent = 0L

        expenses.filter { it.time >= getCurrentMonthTime() }.forEachIndexed { index, it ->
            if (it.valor > mostExpensive) mostExpensive = it.valor
            if (it.valor < cheapest) cheapest = it.valor
            if (it.time < oldest) oldest = it.time
            if (it.time > mostRecent) mostRecent = it.time

            if (it.pago) {
                totalPaid += it.valor
            } else {
                totalToBePaid += it.valor
            }

            totalExpenses += it.valor
            entries.add(Entry(index.toFloat(), it.valor.toFloat()))
        }

        expenses.filter { it.time >= getLastMonthTime() }.forEach {
            lastMonthTotal += it.valor
        }

        val averageExpense = totalExpenses / expenses.filter {
            it.time >= getCurrentMonthTime()
        }.size
        val percentageChange = totalExpenses percentageChangeFrom lastMonthTotal

        reportCardBalance.setValueText(getPercentageChange(totalExpenses, lastMonthTotal))
        reportCardBalance.setValueTextColor(if (percentageChange > 0) {
            ContextCompat.getColor(requireContext(), R.color.colorSuccess)
        } else {
            ContextCompat.getColor(requireContext(), R.color.colorError)
        })

        reportCardTotalExpenses.setValueText(totalExpenses.formatCurrency(requireContext()))
        reportCardTotalExpenses.setSubText(getString(
            R.string.last_month_format,
            lastMonthTotal.formatCurrency(requireContext())
        ))

        reportCardTotalPaid.setValueText(totalPaid.formatCurrency(requireContext()))
        reportCardTotalToBePaid.setValueText(totalToBePaid.formatCurrency(requireContext()))

        reportCardAverage.setValueText(averageExpense.formatCurrency(requireContext()))
        reportCardAverage.setSubText(getString(
            R.string.smallest_format,
            cheapest.formatCurrency(requireContext())
        ))

        val set = LineDataSet(entries,"").apply {
            setDrawIcons(false)
            setDrawCircles(false)
            setDrawCircleHole(false)
            setDrawValues(false)
            setDrawVerticalHighlightIndicator(false)
            setDrawHorizontalHighlightIndicator(false)
            color = ContextCompat.getColor(requireContext(), R.color.colorPrimary)
            lineWidth = 3F
        }

        reportCardTimeline.setChartDataSet(set)
        reportCardTimeline.setValueText(expenses.filter {
            it.time >= getCurrentMonthTime()
        }.size.toString())
        reportCardTimeline.setSubText(getString(
            R.string.biggest_format,
            mostExpensive.formatCurrency(requireContext())
        ))
    }

    private fun getCurrentMonthTime(): Long {
        return GregorianCalendar().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
        }.time.time
    }

    private fun getLastMonthTime(): Long {
        val thisMonth = GregorianCalendar().get(Calendar.MONTH)
        val lastMonth = if (thisMonth == Calendar.JANUARY) {
            Calendar.DECEMBER
        } else {
            thisMonth - 1
        }

        return GregorianCalendar().apply {
            set(Calendar.MONTH, lastMonth)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
        }.time.time
    }

    private fun getPercentageChange(a: Double, b: Double) : String {
        val change = a percentageChangeFrom b
        return when {
            change >= 500 -> {
                "-500%"
            }
            change > 0 -> {
                "-${change.round()}%"
            }
            change <= 500 -> {
                "+500%"
            }
            else -> {
                "+${change.round()}%"
            }
        }
    }

    companion object {
        fun newInstance() = ReportsFragment()
    }
}