package com.enliple.pudding.widget

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.Window
import androidx.appcompat.app.AppCompatDialog
import com.enliple.pudding.R
import kotlinx.android.synthetic.main.dialog_app_confirm.*

/**
 * Created by Kim Joonsung on 2019-04-29.
 */
class AppConfirmDialog : AppCompatDialog {

    private var canDismissOnBackPressed: Boolean = false

    constructor(context: Context?) : super(context, false, null) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(R.layout.dialog_app_confirm)
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
     * 뒤로가기 버튼을 눌렀을 때 팝업을 자동으로 닫는 여부를 설정
     * @param canDismiss
     */
    fun setCanDismissOnBackPress(canDismiss: Boolean) {
        canDismissOnBackPressed = canDismiss
    }

    fun setMessage(msg:String) {
        textViewMsg.text = msg
    }

    fun setTitle(title:String) {
        textViewTitle.text = title
    }

    fun setButton(buttonText: String, clickListener: View.OnClickListener) {
        buttonClose.text = buttonText
        buttonClose.setOnClickListener(clickListener)
    }
}