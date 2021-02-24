package com.enliple.pudding.commons.widget

import android.content.Context
import androidx.appcompat.app.AppCompatDialog
import android.view.WindowManager
import com.enliple.pudding.commons.R

/**
 * Back Button 및 기타 화면 터치를 발생시키지 않기 위해 표시되는 Loading Progress Bar
 * @author hkcha
 * @since 2018.08.29
 */
class PendingProgressDialog : AppCompatDialog {
    constructor(context: Context) : super(context, R.style.PendingProgressDialog) {

        var windowParams = window.attributes
        windowParams.width = WindowManager.LayoutParams.MATCH_PARENT
        windowParams.height = WindowManager.LayoutParams.MATCH_PARENT
        window?.attributes = windowParams

        setCancelable(false)
        setCanceledOnTouchOutside(false)

        setContentView(R.layout.dialog_progress)
    }
}