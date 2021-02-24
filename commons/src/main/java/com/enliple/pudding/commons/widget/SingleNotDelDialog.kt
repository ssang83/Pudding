package com.enliple.pudding.commons.widget

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatDialog
import android.view.View
import android.view.Window
import com.enliple.pudding.commons.R
import kotlinx.android.synthetic.main.dialog_single_not_del.*

class SingleNotDelDialog : AppCompatDialog, View.OnClickListener {

    private var listener: Listener? = null

    constructor(context: Context, message: String, btnStr: String, listener: Listener) : super(context) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(R.layout.dialog_single_not_del)
        this.listener = listener
        content.text = message
        buttonOk.text = btnStr

        buttonOk.setOnClickListener(this)
    }

    override fun onBackPressed() {

    }

    override fun dismiss() {
        if (this.isShowing) {
            super.dismiss()
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.buttonOk -> {
                listener?.onClick()
                dismiss()
            }
        }
    }

    interface Listener {
        fun onClick()
    }
}