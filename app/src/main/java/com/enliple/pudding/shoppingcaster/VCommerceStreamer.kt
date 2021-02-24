package com.enliple.pudding.shoppingcaster

import android.content.Context
import android.graphics.RectF
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.shoppingcaster.stream.BackgroundCapture
import com.enliple.pudding.shoppingcaster.stream.MediaPlayerCaptureExt
import com.enliple.pudding.shoppingcaster.stream.PictureCapture
import com.ksyun.media.streamer.filter.imgtex.ImgTexMixer
import com.ksyun.media.streamer.kit.KSYStreamer

/**
 */
class VCommerceStreamer(context: Context) : KSYStreamer(context) {
    companion object {
        private const val IDX_AUDIO_PIP = 2
        private const val MAIN_SCREEN_CAMERA = 1
        private const val MAIN_SCREEN_VIDEO = 2
    }

    private var mMainScreen = MAIN_SCREEN_CAMERA

    private lateinit var mMediaPlayerCapture: MediaPlayerCaptureExt
    private lateinit var mPictureCapture: PictureCapture // pip background
    private lateinit var mBGPictureCapture: BackgroundCapture // 영상 대체 이미지

    private var isCameraMirror: Boolean = false

    private var mSubViewWidth = .0f
    private var mSubViewHeight = .0f
    private var mSubViewX = .0f
    private var mSubViewY = .0f

    private var mIdxVideo: Int = 0
    private var mIdxPipBgImage: Int = 0
    private var mIdxBgImage: Int = 0

    override fun initModules() {
        // override mixer idx
        mIdxWmTime = 0

        mIdxVideo = 1
        mIdxPipBgImage = 2
        mIdxCamera = 3

        mIdxWmLogo = 6
        mIdxBgImage = 7

        // super init
        super.initModules()

        mBGPictureCapture = BackgroundCapture(glRender)
        mPictureCapture = PictureCapture(glRender)
        mMediaPlayerCapture = MediaPlayerCaptureExt(mContext, glRender)

        // background image
        mBGPictureCapture.imgTexSrcPin.connect(mImgTexPreviewMixer.getSinkPin(mIdxBgImage))

        // pip connection
        mPictureCapture.imgTexSrcPin.connect(mImgTexPreviewMixer.getSinkPin(mIdxPipBgImage))
        mMediaPlayerCapture.imgTexSrcPin.connect(mImgTexPreviewMixer.getSinkPin(mIdxVideo))
        mImgTexPreviewMixer.setScalingMode(mIdxPipBgImage, ImgTexMixer.SCALING_MODE_FULL_FILL)
        mImgTexPreviewMixer.setScalingMode(mIdxVideo, ImgTexMixer.SCALING_MODE_BEST_FIT)
        mImgTexPreviewMixer.setScalingMode(mIdxCamera, ImgTexMixer.SCALING_MODE_CENTER_CROP)
        mImgTexPreviewMixer.setMainSinkPinIndex(mIdxCamera)

        // background image
        mBGPictureCapture.imgTexSrcPin.connect(mImgTexMixer.getSinkPin(mIdxBgImage))

        // pip
        mPictureCapture.imgTexSrcPin.connect(mImgTexMixer.getSinkPin(mIdxPipBgImage))
        mMediaPlayerCapture.imgTexSrcPin.connect(mImgTexMixer.getSinkPin(mIdxVideo))
        mMediaPlayerCapture.audioBufSrcPin.connect(audioMixer.getSinkPin(IDX_AUDIO_PIP))
        mImgTexMixer.setScalingMode(mIdxPipBgImage, ImgTexMixer.SCALING_MODE_FULL_FILL)
        mImgTexMixer.setScalingMode(mIdxVideo, ImgTexMixer.SCALING_MODE_BEST_FIT)
        mImgTexMixer.setScalingMode(mIdxCamera, ImgTexMixer.SCALING_MODE_CENTER_CROP)
        mImgTexMixer.setMainSinkPinIndex(mIdxCamera)
    }

    override fun setMuteAudio(isMute: Boolean) {
        super.setMuteAudio(isMute)

        mMediaPlayerCapture?.mute(isMute)
    }

    override fun setEnableAudioPreview(b: Boolean) {
        super.setEnableAudioPreview(b)

        if (!isAudioMuted) {
            //mMediaPlayerCapture?.mediaPlayer?.setPlayerMute(if (b) 1 else 0)
        } else {
            //mMediaPlayerCapture?.mediaPlayer?.setPlayerMute(1)
        }
    }

    override fun release() {
        super.release()

        mMediaPlayerCapture?.release()
    }

    // pip 테두리 이미지 그리기
    private fun showPipBackground() {
        if (mContext != null) {
            mPictureCapture?.start(mContext, "assets://bg.png")
        }
    }

    fun showBgPicture(uri: String) {
        if (mContext != null) {
            mBGPictureCapture.imgTexSrcPin.connect(mImgTexPreviewMixer.getSinkPin(mIdxCamera))
            mBGPictureCapture.imgTexSrcPin.connect(mImgTexMixer.getSinkPin(mIdxCamera))
            mBGPictureCapture.start(mContext, uri)
        }
    }

    fun hideBgPicture() {
        mBGPictureCapture?.stop()
    }

    fun showBgVideo(url: String) {
        mMediaPlayerCapture?.start(url)
        if (isAudioPreviewing) {
            //mMediaPlayerCapture?.mediaPlayer?.setPlayerMute(1)
        }
    }

    fun hideBgVideo() {
        mMediaPlayerCapture?.stop()
    }

    fun pauseBgVideo() {
        mMediaPlayerCapture?.pause()
    }

    fun resumeBgVideo() {
        mMediaPlayerCapture?.resume()
    }

    /**
     * 카메라 소스 미러링(좌우반전) 설정
     */
    open fun toggleCameraSourceMirror() {
        isCameraMirror = !isCameraMirror

        updateCameraSourceMirror()
    }

    /**
     * 미러 셋팅이 변경된 이후 카메라 원본이 변경(Front / Rear) 되는 것을 대응하기 위해 Override 된 Method
     */
    override fun switchCamera() {
        super.switchCamera()

        updateCameraSourceMirror()
    }

    /**
     * 카메라 소스 미러링 상태를 업데이트
     */
    private fun updateCameraSourceMirror() {
        this.imgTexPreviewMixer.setMirror(this.mIdxCamera, isCameraMirror)
        this.mImgTexMixer.setMirror(this.mIdxCamera, isCameraMirror)
        this.mVideoEncoderMgt.setImgBufMirror(isCameraMirror)
    }

    private fun drawPreviewRectBg(x: Float, y: Float, w: Float, h: Float) {
        mImgTexPreviewMixer.setRenderRect(mIdxPipBgImage, x - 0.007f, y - 0.004f, w + 0.014f, h + 0.008f, 1.0f)
        mImgTexMixer.setRenderRect(mIdxPipBgImage, x - 0.007f, y - 0.004f, w + 0.014f, h + 0.008f, 1.0f)
        //mVideoEncoderMgt.getImgBufMixer().setRenderRect(mIdxPipBgImage, x, y, w + 0.01f, h + 0.01f, 1.0f);
        //mWaterMarkCapture.showLogo(mContext, var1, var4, var5);
    }

    fun setCameraPreviewRect(x: Float, y: Float, w: Float, h: Float) {
        drawPreviewRectBg(x, y, w, h)

        mImgTexPreviewMixer.setRenderRect(mIdxCamera, x, y, w, h, 1.0f)
        mImgTexMixer.setRenderRect(mIdxCamera, x, y, w, h, 1.0f)

        mSubViewWidth = w
        mSubViewHeight = h
        mSubViewX = x
        mSubViewY = y
    }

    private fun setVideoPreviewRect(x: Float, y: Float, w: Float, h: Float) {
        drawPreviewRectBg(x, y, w, h)

        mImgTexPreviewMixer.setRenderRect(mIdxVideo, x, y, w, h, 1.0f)
        mImgTexMixer.setRenderRect(mIdxVideo, x, y, w, h, 1.0f)

        mSubViewWidth = w
        mSubViewHeight = h
        mSubViewX = x
        mSubViewY = y
    }

    private fun setFullCameraPreview() {
        mImgTexPreviewMixer.setRenderRect(mIdxCamera, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f)
        mImgTexMixer.setRenderRect(mIdxCamera, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f)
    }

    private fun setFullVideoPreview() {
        mImgTexPreviewMixer.setRenderRect(mIdxVideo, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f)
        mImgTexMixer.setRenderRect(mIdxVideo, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f)
    }

    private fun getMainScreenText(): String {
        return if (mMainScreen == MAIN_SCREEN_CAMERA) {
            "MAIN_SCREEN_CAMERA"
        } else {
            "MAIN_SCREEN_VIDEO"
        }
    }

    fun switchMainScreen() {
        mPictureCapture.imgTexSrcPin.disconnect(false)
        mMediaPlayerCapture.imgTexSrcPin.disconnect(false)
        mMediaPlayerCapture.audioBufSrcPin.connect(audioMixer.getSinkPin(IDX_AUDIO_PIP))

        if (mMainScreen == MAIN_SCREEN_CAMERA) {
            mMainScreen = MAIN_SCREEN_VIDEO
            mIdxPipBgImage = 4
            mIdxVideo = 5

            mPictureCapture.imgTexSrcPin.connect(mImgTexPreviewMixer.getSinkPin(mIdxPipBgImage))
            mMediaPlayerCapture.imgTexSrcPin.connect(mImgTexPreviewMixer.getSinkPin(mIdxVideo))
            mImgTexPreviewMixer.setMainSinkPinIndex(mIdxCamera)

            mPictureCapture.imgTexSrcPin.connect(mImgTexMixer.getSinkPin(mIdxPipBgImage))
            mMediaPlayerCapture.imgTexSrcPin.connect(mImgTexMixer.getSinkPin(mIdxVideo))
            mImgTexMixer.setMainSinkPinIndex(mIdxCamera)
        } else {
            mMainScreen = MAIN_SCREEN_CAMERA
            mIdxVideo = 1
            mIdxPipBgImage = 2

            mPictureCapture.imgTexSrcPin.connect(mImgTexPreviewMixer.getSinkPin(mIdxPipBgImage))
            mMediaPlayerCapture.imgTexSrcPin.connect(mImgTexPreviewMixer.getSinkPin(mIdxVideo))
            mImgTexPreviewMixer.setMainSinkPinIndex(mIdxCamera)

            mPictureCapture.imgTexSrcPin.connect(mImgTexMixer.getSinkPin(mIdxPipBgImage))
            mMediaPlayerCapture.imgTexSrcPin.connect(mImgTexMixer.getSinkPin(mIdxVideo))
            mImgTexMixer.setMainSinkPinIndex(mIdxCamera)
        }

        showPipBackground()

        Logger.e("switchMainScreen:" + getMainScreenText())

        if (mMainScreen == MAIN_SCREEN_VIDEO) {
            setFullCameraPreview()
            setVideoPreviewRect(mSubViewX, mSubViewY, mSubViewWidth, mSubViewHeight)
        } else {
            setFullVideoPreview()
            setCameraPreviewRect(mSubViewX, mSubViewY, mSubViewWidth, mSubViewHeight)
        }
    }

    fun getSubScreenRect(): RectF {
        return if (mMainScreen == MAIN_SCREEN_VIDEO) {
            mImgTexMixer.getRenderRect(mIdxVideo)
        } else {
            mImgTexMixer.getRenderRect(mIdxCamera)
        }
    }

    fun setSubScreenRect(x: Float, y: Float, w: Float, h: Float) {
        Logger.d("setSubScreenRect x:$x y:$y w:$w h:$h")

        if (mMainScreen == MAIN_SCREEN_VIDEO) {
            setVideoPreviewRect(x, y, w, h)
        } else {
            setCameraPreviewRect(x, y, w, h)
        }
    }
}