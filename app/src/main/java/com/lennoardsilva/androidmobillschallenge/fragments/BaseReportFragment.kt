package com.lennoardsilva.androidmobillschallenge.fragments

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lennoardsilva.androidmobillschallenge.*
import com.lennoardsilva.androidmobillschallenge.data.model.Expense
import com.lennoardsilva.androidmobillschallenge.data.model.Revenue
import com.lennoardsilva.androidmobillschallenge.data.model.Transaction
import com.lennoardsilva.androidmobillschallenge.utils.Utils
import com.lennoardsilva.androidmobillschallenge.utils.show
import kotlinx.android.synthetic.main.reports_fragment.*
import java.util.*
import kotlin.math.absoluteValue

abstract class BaseReportFragment : Fragment() {
    protected val transactions = mutableListOf<Transaction>()
    private var currentMonth = GregorianCalendar().get(Calendar.MONTH)
    private var currentYear = GregorianCalendar().get(Calendar.YEAR)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.reports_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        reportSwipe.apply {
            setColorSchemeResources(R.color.colorAccent, R.color.colorAccentVariant)
            setOnRefreshListener {
                retrieveData()
            }
            // Prevent pulling it
            isEnabled = false
        }
    }

    override fun onResume() {
        super.onResume()

        reportSwipe.isRefreshing = true
        retrieveData()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.reports_action, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (!isAdded) return false

        return when (item.itemId) {
            R.id.reportActionPreviousMonth -> {
                currentMonth = if (currentMonth == Calendar.JANUARY) {
                    Calendar.DECEMBER
                } else {
                    currentMonth - 1
                }

                currentYear = if (currentMonth == Calendar.DECEMBER) {
                    currentYear - 1
                } else {
                    currentYear
                }

                retrieveData()
                true
            }

            R.id.reportActionNextMonth -> {
                currentMonth = if (currentMonth == Calendar.DECEMBER) {
                    Calendar.JANUARY
                } else {
                    currentMonth + 1
                }

                currentYear = if (currentMonth == Calendar.JANUARY) {
                    currentYear + 1
                } else {
                    currentYear
                }
                retrieveData()
                true
            }
            else -> false
        }
    }

    abstract fun retrieveData()

    protected fun populate() {
        val entries = mutableListOf<Entry>()
        var totalExpenses = 0.0
        var lastMonthTotal = 0.0
        var totalPaid = 0.0
        var totalToBePaid = 0.0
        var mostExpensive = 0.0
        var cheapest = Double.MAX_VALUE
        var oldest = System.currentTimeMillis()
        var mostRecent = 0L

        transactions.filter {
            it.time.between(getCurrentMonthTime(), getNextMonthTime())
        }.forEachIndexed { index, it ->
            if (it.valor > mostExpensive) mostExpensive = it.valor
            if (it.valor < cheapest) cheapest = it.valor
            if (it.time < oldest) oldest = it.time
            if (it.time > mostRecent) mostRecent = it.time

            when (it) {
                is Expense -> {
                    if (it.pago) {
                        totalPaid += it.valor
                    } else {
                        totalToBePaid += it.valor
                    }
                }

                is Revenue -> {
                    if (it.recebido) {
                        totalPaid += it.valor
                    } else {
                        totalToBePaid += it.valor
                    }
                }
            }

            totalExpenses += it.valor
            entries.add(Entry(index.toFloat(), it.valor.toFloat()))
        }

        if (totalExpenses == 0.0) {
            cheapest = 0.0
        }

        transactions.filter {
            it.time.between(getLastMonthTime(), getCurrentMonthTime())
        }.forEach {
            lastMonthTotal += it.valor
        }

        transactions.filter {
            it.time >= getCurrentMonthTime()
        }.size.let {
            val averageExpense = if (it > 0) {
                totalExpenses / it
            } else 0.0
            reportCardAverage.setValueText(averageExpense.formatCurrency())
        }

        val percentageChange = totalExpenses percentageChangeFrom lastMonthTotal

        reportCardBalance.setValueText(getPercentageChange(totalExpenses, lastMonthTotal))
        reportCardBalance.setValueTextColor(if (percentageChange > 0) {
            ContextCompat.getColor(requireContext(), R.color.colorSuccess)
        } else {
            ContextCompat.getColor(requireContext(), R.color.colorError)
        })
        reportCardBalance.setSubText(getString(R.string.compared_last_month))

        reportCardTotalTransactions.setValueText(totalExpenses.formatCurrency())
        reportCardTotalTransactions.setSubText(getString(
            R.string.last_month_format,
            lastMonthTotal.formatCurrency()
        ))

        reportCardTotalPaid.setValueText(totalPaid.formatCurrency())
        reportCardTotalToBePaid.setValueText(totalToBePaid.formatCurrency())

        reportCardAverage.setSubText(getString(
            R.string.smallest_format,
            cheapest.formatCurrency()
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
        reportCardTimeline.setValueText(transactions.filter {
            it.time.between(getCurrentMonthTime(), getNextMonthTime())
        }.size.toString())
        reportCardTimeline.setSubText(getString(
            R.string.biggest_format,
            mostExpensive.formatCurrency()
        ))

        reportLayout.show()
        reportSwipe.isRefreshing = false
        updateSubTitle()
    }

    fun showErrorDialog(message: String?, cancelable: Boolean = false, onOkClick: (() -> Unit)? = null) {
        MaterialAlertDialogBuilder(requireContext())
            .setIcon(R.drawable.ic_error)
            .setTitle(R.string.error)
            .setMessage(message ?: getString(R.string.error))
            .setPositiveButton(android.R.string.ok) { _, _ ->
                onOkClick?.let { it() }
            }
            .setCancelable(cancelable)
            .show()
    }

    private fun getCurrentMonthTime(): Long {
        return GregorianCalendar().apply {
            set(Calendar.YEAR, currentYear)
            set(Calendar.MONTH, currentMonth)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
        }.timeInMillis
    }

    private fun getLastMonthTime(): Long {
        val lastMonth = if (currentMonth == Calendar.JANUARY) {
            Calendar.DECEMBER
        } else {
            currentMonth - 1
        }

        val year = if (lastMonth == Calendar.DECEMBER) {
            currentYear - 1
        } else {
            currentYear
        }

        return GregorianCalendar().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, lastMonth)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
        }.timeInMillis
    }

    private fun getNextMonthTime(): Long {
        val nextMonth = if (currentMonth == Calendar.DECEMBER) {
            Calendar.JANUARY
        } else {
            currentMonth + 1
        }

        val year = if (nextMonth == Calendar.JANUARY) {
            currentYear + 1
        } else {
            currentYear
        }

        return GregorianCalendar().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, nextMonth)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
        }.timeInMillis
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
                "+${change.round().absoluteValue}%"
            }
            else -> {
                "+500%"
            }
        }
    }

    private fun updateSubTitle() {
        val calendar = GregorianCalendar().apply {
            set(Calendar.YEAR, currentYear)
            set(Calendar.MONTH, currentMonth)
        }
        Utils.dateMillisToString(calendar.timeInMillis, "MMMM")?.let {
            (requireActivity() as AppCompatActivity).supportActionBar?.subtitle = it.capitalize()
        }
    }
}