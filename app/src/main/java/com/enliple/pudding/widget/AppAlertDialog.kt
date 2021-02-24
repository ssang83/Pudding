package com.enliple.pudding.widget

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import com.enliple.pudding.AbsBaseDialog
import com.enliple.pudding.R
import kotlinx.android.synthetic.main.dialog_double_button.*

/**
 * Created by hkcha on 2018-01-24.
 * 애플리케이션 전용 AlertDialog UI
 */
open class AppAlertDialog : AbsBaseDialog {

    private var canDismissOnBackPressed: Boolean = false

    constructor(context: Context?) : super(context, false, null) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(R.layout.dialog_double_button)
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

    /**
     * 팝업에 표시할 메세지를 설정
     */
    open fun setMessage(messageRes: Int) = setMessage(context.getText(messageRes).toString())

    open fun setMessage(message: String) {
        textViewMsg.text = message
    }

    open fun setMessage(message: CharSequence) {
        textViewMsg.text = message
    }

    /**
     * 팝업에 표시할 타이틀 설정
     */
    open fun setTitle(title: String) {
        if(title.isNotEmpty()) {
            titleLayout.visibility = View.VISIBLE
            textViewTitle.text = title
        } else {
            titleLayout.visibility = View.GONE
        }
    }

    /**
     * 뒤로가기 버튼을 눌렀을 때 팝업을 자동으로 닫는 여부를 설정
     * @param canDismiss
     */
    open fun setCanDismissOnBackPress(canDismiss: Boolean) {
        canDismissOnBackPressed = canDismiss
    }

    /**
     * 팝업 좌측 하단에 버튼을 추가
     * @param buttonText
     * @param clickListener
     */
    open fun setLeftButton(buttonTextRes: Int, clickListener: View.OnClickListener) = setLeftButton(context.getText(buttonTextRes).toString(), clickListener)

    open fun setLeftButton(buttonText: String, clickListener: View.OnClickListener) {
        buttonCancel.text = buttonText
        buttonCancel.setOnClickListener(clickListener)
    }

    /**
     * 팝업 우측 하단에 버튼을 추가
     * @param buttonText
     * @param clickListener
     */
    open fun setRightButton(buttonTextRes: Int, clickListener: View.OnClickListener) = setRightButton(context.getText(buttonTextRes).toString(), clickListener)

    open fun setRightButton(buttonText: String, clickListener: View.OnClickListener) {
        buttonConfirm.text = buttonText
        buttonConfirm.setOnClickListener(clickListener)
    }
}