package com.enliple.pudding.widget

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.Window
import androidx.appcompat.app.AppCompatDialog
import com.enliple.pudding.R
import com.enliple.pudding.commons.events.OnSingleClickListener
import com.enliple.pudding.commons.widget.toast.AppToast
import kotlinx.android.synthetic.main.dialog_schedule_cancel.*

/**
 * Created by Kim Joonsung on 2019-05-28.
 */
class ScheduleCancelDialog : AppCompatDialog {

    private var mListener:Listener ? = null
    private var mContext:Context

    constructor(context: Context, listener: Listener) : super(context) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(R.layout.dialog_schedule_cancel)

        this.mListener = listener
        this.mContext = context

        buttonConfirm.isEnabled = false

        editTextContent.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if(s?.length == 0) {
                    buttonConfirm.isEnabled = false
                } else {
                    buttonConfirm.isEnabled = true
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        buttonCancel.setOnClickListener { dismiss() }
        buttonConfirm.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                if(editTextContent.text.toString().isEmpty())  {
                    AppToast(mContext).showToastMessage("취소 사유를 입력해주세요.",
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_BOTTOM)

                    return
                } else {
                    mListener?.onConfirm(editTextContent.text.toString())
                }
            }
        })
    }

    interface Listener {
        fun onConfirm(content:String)
    }
}