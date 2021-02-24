package com.enliple.pudding.shoppingcaster.widget

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatDialog
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import com.enliple.pudding.R
import kotlinx.android.synthetic.main.dialog_video_effect.*

/**
 * 방송간 비디오 효과 설정 팝업
 * @author hkcha
 * @since 2018.08.29
 */
class CastEffectDialog : AppCompatDialog {

    constructor(context: Context, listener: SelectionListener) : super(context, R.style.CasterBottomDialog) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


        var windowParams = window.attributes
        windowParams.gravity = Gravity.BOTTOM
        windowParams.width = WindowManager.LayoutParams.MATCH_PARENT
        windowParams.height = context.resources.getDimensionPixelSize(R.dimen.dialog_video_effect_height)
        window.attributes = windowParams

        setContentView(R.layout.dialog_video_effect)

        buttonBeauty.setOnClickListener { listener?.onBeautyFilterSelected() }
        buttonFormationVideo.setOnClickListener { listener?.onVideoFormationSelected() }
        buttonSticker.setOnClickListener { listener?.onChooseStickerSelected() }
    }

    interface SelectionListener {
        fun onBeautyFilterSelected()

        fun onVideoFormationSelected()

        fun onChooseStickerSelected()
    }
}