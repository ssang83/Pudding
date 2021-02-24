package com.enliple.pudding.widget

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatDialog
import android.view.View
import android.view.Window
import com.enliple.pudding.R
import com.enliple.pudding.commons.events.OnSingleClickListener
import kotlinx.android.synthetic.main.dialog_permission.*

/**
 */
class PermissionDialog : AppCompatDialog {
    private var mListener: ClickListener? = null

    interface ClickListener {
        fun onOk()
        fun onCancel()
    }

    constructor(context: Context) : super(context, true, null) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        setContentView(R.layout.dialog_permission)

        content.text = "방송 예약은 시청자와의 소중한 약속입니다.\n" +
                "반드시 예약 시간을 준수해주세요.\n" +
                "\n" +
                "방송 예약시간 10분 전부터는 예약시간 변경이 불가합니다.\n" +
                "방송 예약시간으로부터 30분 이내로 방송을 시작하지 않을 경우,\n" +
                "방송 취소 처리되며 패널티가 부과됩니다.\n" +
                "\n" +
                "월 3회 이상 예약 시간에 방송을 하지 않을 경우,\n" +
                "3개월간 방송 예약이 불가합니다."

        setCancelable(false)
        setCanceledOnTouchOutside(false)

        buttonCancel.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                mListener?.onCancel()
                dismiss()
            }
        })

        buttonOk.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                mListener?.onOk()
                dismiss()
            }
        })
    }

    override fun onBackPressed() {
        dismiss()
    }

    override fun dismiss() {
        if (this.isShowing) {
            super.dismiss()
        }
    }

    fun setListener(listener: ClickListener) {
        mListener = listener
    }
}