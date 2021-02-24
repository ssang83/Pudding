package com.enliple.pudding.widget

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import androidx.appcompat.app.AppCompatDialog
import com.enliple.pudding.R
import kotlinx.android.synthetic.main.dialog_double_button.*

/**
 * Created by Kim Joonsung on 2019-04-26.
 */
class DoubleButtonDialog : AppCompatDialog {

    private var mContent = ""
    private var mTitle = ""

    private var mListener:Listener? = null

    constructor(context:Context, msg:String, title:String) : super(context) {
        this.mContent = msg
        this.mTitle = title

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val v = LayoutInflater.from(context).inflate(R.layout.dialog_double_button, null, false)
        setContentView(v)

        textViewMsg.text = mContent
        textViewTitle.text = mTitle

        buttonCancel.setOnClickListener(clickListener)
        buttonConfirm.setOnClickListener(clickListener)
    }

    fun setListener(listener: Listener) {
        this.mListener = listener
    }

    private val clickListener = View.OnClickListener {
        when(it?.id) {
            R.id.buttonConfirm -> {
                mListener?.onConfirm()
                dismiss()
            }

            R.id.buttonCancel -> dismiss()
        }
    }

    interface Listener {
        fun onConfirm()
    }
}