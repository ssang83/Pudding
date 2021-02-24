package com.enliple.pudding.shoppingcaster.widget

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatDialog
import android.view.Window
import com.enliple.pudding.R

/**
 * 방송간 상품 표시 설정 팝업
 * @author hkcha
 * @since 2018.07.10
 */
class CasterProductSettingDialog : AppCompatDialog {
    constructor(context: Context?) : super(context, true, null)

    private var listener: Listener? = null
    private var selectedProductPosition = 0

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(R.layout.dialog_caster_product_settings)

        setCancelable(true)
        setCanceledOnTouchOutside(true)
    }

    fun setListener(listener: Listener) {
        this.listener = listener
    }


    interface Listener {
        // 방송 상품이 선택되었음
        fun onCastProductSelected()
    }
}