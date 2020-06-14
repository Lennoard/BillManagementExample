package com.lennoardsilva.androidmobillschallenge.sheets

import android.graphics.PorterDuff
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.lennoardsilva.androidmobillschallenge.R
import com.lennoardsilva.androidmobillschallenge.toPx
import com.lennoardsilva.androidmobillschallenge.utils.show
import kotlinx.android.synthetic.main.context_bottom_sheet.*
import java.io.Serializable

typealias SheetOption = ContextBottomSheet.Option
class ContextBottomSheet : BaseSheetFragment() {
    data class Option(
        var title: String,
        val tag: String,
        @ColorRes val tintColor: Int = R.color.colorOnBackground,
        @DrawableRes val icon: Int? = null
    ) : Serializable

    interface OnOptionClickListener {
        fun onOptionClick(tag: String)
    }

    var onOptionClickListener : OnOptionClickListener = object : OnOptionClickListener {
        override fun onOptionClick(tag: String) { dismiss() }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.context_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            val title = it.getString(EXTRA_TITLE, "")
            if (title.isNotEmpty()) {
                contextBottomSheetTitle.show()
                contextBottomSheetTitle.text = title
            }

            val options = it.getSerializable(EXTRA_OPTIONS) as ArrayList<*>?
            options?.forEach { option ->
                (option as Option?)?.let { opt ->
                    contextBottomSheetOptionsContainer.addView(generateOptionView(opt))
                }
            }
        }
    }

    private fun generateOptionView(option: Option) : LinearLayout {
        val backgroundDrawable = TypedValue()
        context?.theme?.resolveAttribute(
            R.attr.selectableItemBackground,
            backgroundDrawable,
            true
        )

        val rootLayout = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                48.toPx(context)
            )
            gravity = Gravity.CENTER_VERTICAL
            tag = option.tag
            setPadding(16.toPx(context), 0, 16.toPx(context), 0)
            setBackgroundResource(backgroundDrawable.resourceId)
            setOnClickListener {
                onOptionClickListener.onOptionClick(option.tag)
            }
        }

        option.icon?.let {
            rootLayout.addView(ImageView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    24.toPx(context),
                    24.toPx(context)
                ).apply {
                    setMargins(0, 0, 16.toPx(context), 0)
                }
                setImageResource(it)
                setColorFilter(
                    ContextCompat.getColor(context, option.tintColor),
                    PorterDuff.Mode.SRC_IN
                )
            })
        }

        rootLayout.addView(TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1F
            )
            text = option.title
            setTextColor(ContextCompat.getColor(context, option.tintColor))
        })

        return rootLayout
    }

    companion object {
        private const val EXTRA_TITLE = "title"
        private const val EXTRA_OPTIONS = "options"

        @JvmStatic
        fun newInstance(
            title: String? = "",
            options: ArrayList<Option>
        ): ContextBottomSheet = ContextBottomSheet().apply {
            arguments = Bundle().apply {
                putString(EXTRA_TITLE, title)
                putSerializable(EXTRA_OPTIONS, options)
            }
        }
    }
}