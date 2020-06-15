package com.lennoardsilva.androidmobillschallenge.views

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.android.material.card.MaterialCardView
import com.lennoardsilva.androidmobillschallenge.R
import com.lennoardsilva.androidmobillschallenge.formatCurrency
import com.lennoardsilva.androidmobillschallenge.utils.goAway
import com.lennoardsilva.androidmobillschallenge.utils.hide
import com.lennoardsilva.androidmobillschallenge.utils.show

class ReportCard(context: Context, attrs: AttributeSet): MaterialCardView(context, attrs) {

    private val titleTextView: TextView by lazy { findViewById<TextView>(R.id.reportCardTitle) }
    private val valueTextView: TextView by lazy { findViewById<TextView>(R.id.reportCardValue) }
    private val subTextView: TextView by lazy { findViewById<TextView>(R.id.reportCardSubText) }
    private val chart: LineChart by lazy { findViewById<LineChart>(R.id.reportCardChart) }
    private val card: MaterialCardView by lazy { findViewById<MaterialCardView>(R.id.reportCard) }

    init {
        inflate(context, R.layout.report_card, this)
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.ReportCard)

        titleTextView.text = attributes.getString(R.styleable.ReportCard_title)

        attributes.getString(R.styleable.ReportCard_value)?.let {
            valueTextView.text = it
            valueTextView.show()
        }

        attributes.getString(R.styleable.ReportCard_subText)?.let {
            subTextView.text = it
            subTextView.show()
        }

        attributes.recycle()

        with (chart) {
            description.isEnabled = false
            isDragEnabled = false
            setTouchEnabled(false)
            setScaleEnabled(false)
            setDrawMarkers(false)
            axisRight.isEnabled = false
            axisLeft.isEnabled = false
            legend.isEnabled = false
        }

        with (chart.xAxis) {
            setDrawGridLines(false)
            setDrawAxisLine(false)
            setDrawLabels(false)
        }
    }

    override fun setOnClickListener(l: OnClickListener?) {
        card.setOnClickListener(l)
    }

    fun setTitleText(title: String) {
        titleTextView.text = title
    }

    fun setValueText(value: String) {
        valueTextView.text = value
        valueTextView.show()
    }

    fun setValueTextColor(@ColorInt color: Int) {
        valueTextView.setTextColor(color)
    }

    fun setSubText(text: String) {
        subTextView.text = text
        subTextView.show()
    }

    fun setChartData(data: LineData) {
        chart.data = data
        chart.show()
    }

    fun setChartDataSet(dataSet: LineDataSet) {
        setChartData(LineData(dataSet))
    }
}
