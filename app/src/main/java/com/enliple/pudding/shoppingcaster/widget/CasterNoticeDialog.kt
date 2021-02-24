package com.enliple.pudding.shoppingcaster.widget

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialog
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.Window
import com.enliple.pudding.R
import com.enliple.pudding.commons.events.OnSingleClickListener
import com.enliple.pudding.commons.log.Logger
import kotlinx.android.synthetic.main.dialog_caster_notice.*

/**
 * Created by Kim Joonsung on 2018-10-31.
 */
class CasterNoticeDialog : AppCompatDialog {
    companion object {
        //private const val TAG = "CasterNoticeDialog"
    }

    private var canDismissOnBackPressed: Boolean = false
    private var mListener: Listener? = null
    //private var isOn: Boolean = false
    private var mNotice = ""

    constructor(context: Context, notice: String) : super(context, true, null) {
        Logger.e("notice:$notice")
        mNotice = notice
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        setContentView(R.layout.dialog_caster_notice)

        editTextNotice.setText(mNotice)

        buttonCancel.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                dismiss()
            }
        })

        buttonConfirm.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                if (editTextNotice?.text!!.isNotEmpty()) {
                    mListener?.onNoticeText(editTextNotice.text.toString())
                }
                //mListener?.onNoticeOnOff(isOn)
            }
        })

//        buttonSwitch.setOnClickListener(object : OnSingleClickListener() {
//            override fun onSingleClick(v: View?) {
//                v!!.isSelected = !v!!.isSelected
//                if (v!!.isSelected) isOn = true else isOn = false
//            }
//        })

        editTextNotice.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                textViewCount.text = "0/30"
                if (s?.toString()?.length ?: 0 > 0 && s?.toString()?.length ?: 0 <= 30) {
                    textViewCount.text = "${s?.length}/30"
                }
            }
        })
    }

    override fun onBackPressed() {
        if (canDismissOnBackPressed) {
            dismiss()
        }
    }

    override fun dismiss() {
        if (this.isShowing) {
            super.dismiss()
        }
    }

    fun setListener(listener: Listener) {
        this.mListener = listener
    }

    interface Listener {
        fun onNoticeText(text: String)
        fun onNoticeOnOff(isOnOff: Boolean)
    }
}