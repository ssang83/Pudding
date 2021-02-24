package com.enliple.pudding.shoppingcaster.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import androidx.core.app.ActivityCompat
import android.util.Log
import android.view.Surface
import android.view.View
import android.widget.Toast
import com.enliple.pudding.R
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.model.PermissionObject
import com.enliple.pudding.shoppingcaster.VCommerceStreamer
import com.enliple.pudding.widget.MainPermissionDialog
import kotlinx.android.synthetic.main.fragment_caster.*
import java.util.ArrayList

/**
 * 비디오 방송 영상을 표시 하고 송출하는 Fragment
 * @author hkcha
 * @since 2018.07.06
 */
open class CasterVideoFragment : BaseScreenFragment() {

    private lateinit var mStickerDeleteBitmap: Bitmap // 스티커 보조 영역 삭제 버튼 (그림 스티커 및 자막 스티커 공통)
    private lateinit var mStickerRotateBitmap: Bitmap // 스티커 보조 영역의 회전 버튼 (그림 스티커와 자막 스티커 공통

    interface ClickTimeListener {
        fun getClickTime()
    }

    var listener: ClickTimeListener? = null

    fun setClickTimeListener(listener: ClickTimeListener?) {
        this.listener = listener
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mStickerDeleteBitmap = BitmapFactory.decodeResource(view!!.context.resources, android.R.drawable.ic_delete)
        mStickerRotateBitmap = BitmapFactory.decodeResource(view!!.context.resources, android.R.drawable.ic_menu_rotate)

        enableBeautyVideoFilter()
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_caster
    }

    override fun handleOnResume() {
        super.handleOnResume()

        mStreamer?.setDisplayPreview(gl_surface_view)
        mStreamer?.rotateDegrees = getDisplayRotation()
    }

    private fun getDisplayRotation(): Int {
        var rotation = activity?.windowManager?.defaultDisplay?.rotation ?: 0

        return when (rotation) {
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> 0
        }
    }

    /**
     * 앱 내부의 이미지 리소스를 이용하여 스티커를 추가
     * @param stickerResourceId
     */
    fun addImageStickerByResource(stickerResourceId: Int) {
        Logger.e("addImageStickerByResource : $stickerResourceId")
        if (view == null) {
            return
        }

//        val info = KSYStickerInfo()
//        info.bitmap = BitmapFactory.decodeResource(view!!.context.resources, stickerResourceId)
//        info.stickerType = KSYStickerInfo.STICKER_TYPE_IMAGE
//
//        stickerViewPanel.addSticker(info, mStickerHelpBoxInfo)
//        stickerViewPanel.updateStickerInfo(info)
    }

    fun removeAllSticker() {
//        stickerViewPanel.removeStickers()
    }

    fun getStream(): VCommerceStreamer? {
        return mStreamer
    }

//    private val stickerStateChangedListener = object : KSYStickerView.OnStickerStateChanged {
//        override fun deleted(p0: MutableList<Int>?, p1: String?) {
//            Logger.e(TAG, "Sticker Deleted")
//        }
//
//        override fun selected(p0: Int, p1: String?) {
//            Logger.e(TAG, "Sticker Selected")
//        }
//    }

    fun setFilter(isRunFilter: Boolean) {
        Logger.e("setFilter called")
        if ( isRunFilter )
            enableBeautyVideoFilter()
        else
            disableBeautyVideoFilter()
    }

}