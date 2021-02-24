package com.enliple.pudding.shoppingcaster.widget

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatDialog
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import com.enliple.pudding.R
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.widget.recyclerview.WrappedGridLayoutManager
import com.enliple.pudding.shoppingcaster.adapter.StickerSelectAdapter
import kotlinx.android.synthetic.main.dialog_sticker_select.*

/**
 * 화면 내 스티커 효과를 수행하기 위해 표시되는 Dialog
 * @author hkcha
 * @since 2018.08.29
 */
class CastStickerDialog : AppCompatDialog, StickerSelectAdapter.Listener {
    companion object {
        private const val TAG = "CastStickerDialog"
        private const val RECYCLER_VIEW_ITEM_CACHE_COUNT = 20
    }

    private var mListener: Listener? = null

    constructor(context: Context) : super(context, R.style.CasterBottomDialog) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        var windowParams = window.attributes
        windowParams.gravity = Gravity.BOTTOM
        windowParams.width = WindowManager.LayoutParams.MATCH_PARENT
        windowParams.height = context.resources.getDimensionPixelSize(R.dimen.dialog_video_sticker_height)
        window.attributes = windowParams

        var view = LayoutInflater.from(context).inflate(R.layout.dialog_sticker_select, null, false)
        setContentView(view)

        view.post {
            (recyclerViewStickers.layoutManager as WrappedGridLayoutManager)?.spanCount = 3
            (recyclerViewStickers.layoutManager as WrappedGridLayoutManager)?.orientation = WrappedGridLayoutManager.VERTICAL
            recyclerViewStickers.setItemViewCacheSize(RECYCLER_VIEW_ITEM_CACHE_COUNT)
            recyclerViewStickers.setHasFixedSize(false)
            recyclerViewStickers.adapter = StickerSelectAdapter().apply {
                setListener(this@CastStickerDialog)
            }
        }
    }

    override fun onStickerItemClicked(stickerResourceId: Int) {
        Logger.e(TAG, "onStickerItemClicked : $stickerResourceId")
        dismiss()
        mListener?.onStickerResourceClicked(stickerResourceId)
    }

    fun setListener(listener: Listener) {
        mListener = listener
    }

    interface Listener {
        fun onStickerResourceClicked(stickerResourceId: Int)
    }
}