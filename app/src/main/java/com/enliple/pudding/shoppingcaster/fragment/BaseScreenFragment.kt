package com.enliple.pudding.shoppingcaster.fragment

import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.*
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.enliple.pudding.R
import com.enliple.pudding.activity.BroadcastSettingActivity
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.widget.toast.AppToast
import com.enliple.pudding.shoppingcaster.VCommerceStreamer
import com.enliple.pudding.shoppingcaster.activity.ShoppingCastActivity
import com.enliple.pudding.shoppingcaster.config.BaseStreamConfig
import com.ksyun.media.streamer.encoder.VideoEncodeFormat
import com.ksyun.media.streamer.filter.imgtex.ImgTexFilterMgt
import com.ksyun.media.streamer.framework.AVConst
import com.ksyun.media.streamer.kit.KSYStreamer
import com.ksyun.media.streamer.kit.StreamerConstants
import com.ksyun.media.streamer.util.gles.GLRender
import kotlinx.android.synthetic.main.fragment_base_casting.*
import java.util.*

open class BaseScreenFragment : androidx.fragment.app.Fragment() {
    companion object {
        //val TAG = "BaseScreenFragment"
        const val WATERMARK_VISIBLE = false
    }

    open lateinit var mConfig: BaseStreamConfig
    open var mIsLandScape: Boolean = false
    open var mIsFlashOpened: Boolean = false
    open var mStreaming: Boolean = false
    open var mIsChronometerStarted: Boolean = false
    open var mDebugInfo: String = ""
    open var mHWEncoderUnsupported: Boolean = false
    open var mSWEncoderUnsupported: Boolean = false

    open var mStreamer: VCommerceStreamer? = null
    open var mMainHandler: Handler? = null
    open var mTimer: Timer? = null

    open var cameraOrientation: Int = 0

    open var mRecording: Boolean = false

    private var mRecordingUrl: String? = ""
    // SDCARD PATH
    open var mSdCardPath = Environment.getExternalStorageDirectory().path!!

    // 워터마크에 표시할 로고 절대경로
    open var mLogoPath = "assets://watermark.png"

    // OnPause 등 카메라 정지시 표시될 백그라운드 이미지 경로
    open var mBgImagePath = "assets://pause.png"

    open var mReCast: Boolean = false  // 방송 재시작시 사용

    open fun getConfig(bundle: Bundle?): BaseStreamConfig {
        var config = BaseStreamConfig()
        var json = bundle?.getString("initStreamingConfig")

        return if (json != null) BaseStreamConfig().fromJson(bundle!!.getString("initStreamingConfig")) else config
    }

    open fun getLayoutId(): Int = R.layout.fragment_base_casting

    open fun setDisplayPreview() {
        mStreamer?.setDisplayPreview(gl_surface_view)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(getLayoutId(), container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Logger.d("BaseScreenFragment - onViewCreated")

        mMainHandler = Handler()
        mStreamer = VCommerceStreamer(context!!)
        mConfig = getConfig(arguments)

        initUI()

        initStreamingConfig()
        if ( WATERMARK_VISIBLE )
            showWaterMarkLogo()

        if (mConfig.mAutoStart) {
            startCasting(null)
        }

        if (mConfig.mShowDebugInfo) {
            startDebugInfoTimer()
        }
    }

    override fun onResume() {
        super.onResume()

        handleOnResume()
    }

    override fun onPause() {
        super.onPause()

        handleOnPause()
    }

    override fun onDestroy() {
        super.onDestroy()

        mMainHandler?.removeCallbacksAndMessages(null)
        mMainHandler = null

        mTimer?.cancel()
        mTimer = null

        mStreamer?.release()
    }

    open fun initUI() {
        // TODO : 기타 다른 UI 등에 대한 초기화가 필요한 경우 정의
    }

    open fun setStreamUrl(url: String) {
        mStreamer?.url = url
    }

    /**
     * Streaming 환경설정
     * (Configuration 에 URL 만 명시되어 있어도 정책 기본설정에 따라 송출되도록 변경처리 되었음)
     */
    open fun initStreamingConfig() {
        // 스트리밍 송출 경로 설정
        if (!TextUtils.isEmpty(mConfig?.mUrl)) {
            Logger.d("Streaming URL : ${mConfig.mUrl ?: ""}")
            mStreamer?.url = mConfig.mUrl ?: ""
        }

        // Preview 및 송출 해상도 설정
        // 오레오 버전 미만일 경우 mTargetResolution를 720으로 나눈다 , mTargetResolution를 720 -> 1080으로 올리면서 저사양 OR 낮은 OS 에서 문제가 발생할 여지가 있어
        // 사양이 낮거나 OS가 낮을 경우 720으로 그렇지 않을 경우에는 1080으로 적용하기 위해
        var targetResolution = mConfig.mTargetResolution
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            targetResolution = StreamerConstants.VIDEO_RESOLUTION_720P
        }
        mStreamer?.setPreviewResolution(targetResolution)
        mStreamer?.setTargetResolution(targetResolution)

        // Encoding 방식 설정 (HW / SW)
        mStreamer?.setEncodeMethod(mConfig?.mEncodeMethod)
        // HW Encode 가속에 대한 퍼포먼스 설정
        if (mConfig.mEncodeMethod == StreamerConstants.ENCODE_METHOD_HARDWARE) {
            mStreamer?.videoEncodeProfile = VideoEncodeFormat.ENCODE_PROFILE_HIGH_PERFORMANCE
        }

        // FrameRate 설정
        if (mConfig.mFrameRate > 0) {
            mStreamer?.previewFps = mConfig.mFrameRate
            mStreamer?.targetFps = mConfig.mFrameRate
        }

        // Video 송출 Bitrate 설정
        var videoBitrate = mConfig.mVideoKBitrate
        // 720으로 돌리면서 아래 주석 처리함
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
//            videoBitrate /= 2 // 오레오 버전 미만일 경우 bitrate를 2로 나눈다 , mVideoKBitrate를 2M -> 4M 로 올리면서 저사양 OR 낮은 OS 에서 문제가 발생할 여지가 있어 사양이 낮거나 OS가 낮을 경우 2M로 그렇지 않을 경우에는 4M로 적용하기 위해
//        }
        if (videoBitrate > 0) {
            mStreamer?.setVideoKBitrate(videoBitrate * 3 / 4, videoBitrate, videoBitrate / 4)   // 초기, 최고, 최저 평균
        }

        // Audio 송출 Bitrate 설정
        if (mConfig.mAudioKBitrate > 0) {
            mStreamer?.setAudioKBitrate(mConfig.mAudioKBitrate)
        }

        mStreamer?.audioEncodeProfile = AVConst.PROFILE_AAC_LOW

        // 화면 촬영 및 송출 Source 에 대한 초기 방향 설정
        if (mConfig.mOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            mIsLandScape = true
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            mStreamer?.rotateDegrees = 90
        } else if (mConfig.mOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            mIsLandScape = false
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            mStreamer?.rotateDegrees = 0
        }

        // 송출에 필요한 카메라 원본 (Front / Back) 설정
        mStreamer?.cameraFacing = mConfig.mCameraFacing

        // Preview 화면 초기화
        setDisplayPreview()

        // Callback Listener 등록
        mStreamer?.onInfoListener = mOnInfoListener
        mStreamer?.onErrorListener = mOnErrorListener
        mStreamer?.enableRepeatLastFrame = false

        mStreamer?.startCameraPreview()

        // 방송스트리머에서 나오는 스트림 영상이 시청자 스트림에도 동일하게 표시하도록 설정
        //mStreamer?.setFrontCameraMirror(true)
    }

    open fun handleOnResume() {
        Logger.e("handleOnResume")

        mStreamer?.onResume()
        mStreamer?.startCameraPreview()
        mStreamer?.resumeBgVideo()
        mStreamer?.hideBgPicture()
        mStreamer?.setUseDummyAudioCapture(false)

//        startCameraPreviewWithPermCheck()
    }

    open fun handleOnPause() {
        Logger.e("handleOnPause")

        mStreamer?.onPause()
        mStreamer?.stopCameraPreview()
        mStreamer?.pauseBgVideo()
        mStreamer?.showBgPicture("assets://pause.png")
        mStreamer?.setUseDummyAudioCapture(true)
    }

    /**
     * 방송을 시작
     */
    open fun startCasting(streamUrl: String?) {
        if (streamUrl != null) {
            mStreamer?.url = streamUrl
        }

        mStreamer?.startStream()
        mStreaming = true
    }

    /**
     * 방송을 종료
     */
    open fun stopCasting() {
        mStreamer?.stopStream()
        mStreaming = false

        stopChronometer()

        if (activity != null && activity is ShoppingCastActivity) {
            (activity as ShoppingCastActivity).onCastingStopped()
        }
    }

    /**
     * VOD 녹화를 시작
     */
    open fun startRecord(recordUrl: String?) {
        if (recordUrl != null) {
            mStreamer?.url = recordUrl
        }

        if (mRecording) {
            return
        }

        mStreamer?.startRecord(recordUrl)
        mRecordingUrl = recordUrl

        mRecording = true
    }

    /**
     * VOD 녹화를 종료
     */
    open fun stopRecord() {
        mStreamer?.stopRecord()
    }

    /**
     * 워터마크 로고를 표시
     */
    open fun showWaterMarkLogo() {
        if (mIsLandScape) {
            mStreamer?.showWaterMarkLogo(mLogoPath, 0.08f, 0.04f, 0.20f, 0.0f, 1.0f)
            //mStreamer?.showWaterMarkTime(0.03f, 0.01f, 0.35f, Color.WHITE, 1.0f)
        } else {
            mStreamer?.showWaterMarkLogo(mLogoPath, 0.02f, 0.95f, 0.30f, 0.0f, 1.0f)
            //mStreamer?.showWaterMarkTime(0.01f, 0.03f, 0.22f, Color.WHITE, 1.0f)
        }
    }

    /**
     * 워터마크 로그를 숨김
     */
    open fun hideWaterMarkLogo() {
        mStreamer?.hideWaterMarkLogo()
        //mStreamer?.hideWaterMarkTime()
    }

    /**
     * 뷰티 필터 기능을 활성화
     */
    open fun enableBeautyVideoFilter() {
        mStreamer?.imgTexFilterMgt?.setOnErrorListener { _, _ ->
            AppToast(context!!).showToastMessage(R.string.filter_not_supported,
                    AppToast.DURATION_MILLISECONDS_DEFAULT, AppToast.GRAVITY_BOTTOM)
            mStreamer?.imgTexFilterMgt?.setFilter(mStreamer?.glRender, ImgTexFilterMgt.KSY_FILTER_BEAUTY_DISABLE)
        }

        mStreamer?.imgTexFilterMgt?.setFilter(mStreamer?.glRender, ImgTexFilterMgt.KSY_FILTER_BEAUTY_PRO)
    }

    /**
     * 뷰티 필터 기능을 비 활성화
     */
    open fun disableBeautyVideoFilter() {
        mStreamer?.imgTexFilterMgt?.setFilter(mStreamer?.glRender, ImgTexFilterMgt.KSY_FILTER_BEAUTY_DISABLE)
        mStreamer?.imgTexFilterMgt?.setOnErrorListener(null)
    }

    open fun clearFilter() {
        //mStreamer?.imgTexFilterMgt?.setFilter(ImgTexGPUImageFilter(getGLRender(), GPUImageFilter()))
    }

    /**
     * GPUImageFilter 생산에 필요한 GLRender 를 획득
     */
    open fun getGLRender(): GLRender? = mStreamer?.glRender

    /**
     * 디버깅 타이머를 Thread 를 시작
     */
    open fun startDebugInfoTimer() {
        if (mTimer == null) {
            mTimer = Timer()
        }

        mTimer?.schedule(object : TimerTask() {
            override fun run() {
                updateDebugInfo()
                activity?.runOnUiThread {
                    debug_info.text = mDebugInfo
                }
            }
        }, 1000L, 1000L)
    }

    /**
     * 표시될 디버깅 정보를 갱신
     */
    open fun updateDebugInfo() {
        if (mStreamer == null) return

        var encodeMethod = when (mStreamer?.videoEncodeMethod) {
            StreamerConstants.ENCODE_METHOD_HARDWARE -> "HW"
            StreamerConstants.ENCODE_METHOD_SOFTWARE -> "SW"
            else -> "SW1"
        }

        mDebugInfo = String.format(Locale.getDefault(),
                "EncodeMethod=%s PreviewFps=%.2f \n " +
                        "RtmpHostIP()=%s DropppedFrameCount()=%d \n " +
                        "ConnectTime()=%sms DnsParseTime()=%d \n " +
                        "UploadedKb()=%s EncodedFrames()=%d \n " +
                        "CurrentKBitrate=%d Streamer Version()=%s",
                encodeMethod, mStreamer?.previewFps,
                mStreamer?.rtmpHostIP, mStreamer?.droppedFrameCount,
                mStreamer?.connectTime, mStreamer?.dnsParseTime,
                mStreamer?.uploadedKBytes, mStreamer?.encodedFrames,
                mStreamer?.currentUploadKBitrate, KSYStreamer.getVersion())
    }

    /**
     * 방송 시간을 카운팅하는 Chronometer 를 시작
     */
    open fun startChronometer() {
        chronometer.base = SystemClock.elapsedRealtime()
        chronometer.start()
        mIsChronometerStarted = true
    }

    /**
     * 방송 시간을 카운팅하는 Chronometer 를 종료
     */
    open fun stopChronometer() {
        chronometer?.base = SystemClock.elapsedRealtime()
        chronometer?.stop()
        mIsChronometerStarted = false
    }

    /**
     * Handler 로 호출되는 Streaming 정보에 대한 처리
     */
    open fun onStreamerInfo(what: Int, msg1: Int, msg2: Int) {
        Logger.d("OnInfo: $what, msg1:$msg1, msg2:$msg2")
        when (what) {
            StreamerConstants.KSY_STREAMER_CAMERA_INIT_DONE -> {
                // 스트리머 초기화 완료
                Logger.i("STREAMER_CAMERA_INIT_DONE")
            }

            StreamerConstants.KSY_STREAMER_CAMERA_FACEING_CHANGED -> {
                // 카메라 포지션 위치가 변경됨
                Logger.d("STREAMER_CAMERA_FACEING_CHANGED")
            }

            StreamerConstants.KSY_STREAMER_OPEN_STREAM_SUCCESS -> {
                // 방송 스트림이 활성화되었음
                Logger.d("STREAMER_OPEN_STREAM_SUCCESS")
                startChronometer()
                if (activity != null && activity is ShoppingCastActivity) {
//                    (activity as ShoppingCastActivity).onCastingStarted()
                    (activity as ShoppingCastActivity).showCastUI()
                }
            }

            StreamerConstants.KSY_STREAMER_FRAME_SEND_SLOW -> {
                // 프레임 전송속도가 느려지고 있음 (네트워크 속도가 느림)
                Logger.e("STREAMER_FRAME_SEND_SLOW !! $msg1 ms")
                AppToast(context!!).showToastMessage(R.string.network_too_slow,
                        AppToast.DURATION_MILLISECONDS_DEFAULT, AppToast.GRAVITY_BOTTOM)
            }

            StreamerConstants.KSY_STREAMER_EST_BW_RAISE -> {
                // Streaming Bandwidth 값이 증가 됨
                Logger.i("BandWidth raise to ${msg1 / 1000} kbps")
            }

            StreamerConstants.KSY_STREAMER_EST_BW_DROP -> {
                // Streaming Bandwidth 값이 감소 됨
                Logger.i("BandWidth drop to ${msg1 / 1000} kbps")
            }

            StreamerConstants.KSY_STREAMER_OPEN_FILE_SUCCESS -> {
                Logger.d("KSY_STREAMER_OPEN_FILE_SUCCESS")
                startChronometer()
            }

            StreamerConstants.KSY_STREAMER_FILE_RECORD_STOPPED -> {
                Logger.d("KSY_STREAMER_FILE_RECORD_STOPPED")
                mRecording = false
                stopChronometer()
            }
        }
    }

    /**
     * Handler 로 호출되는 Streaming 오류에 대한 처리
     */
    open fun onStreamerError(what: Int, msg1: Int, msg2: Int) {
        Logger.e("Streaming error: what=$what, msg1=$msg1, msg2=$msg2")
        when (what) {
            StreamerConstants.KSY_STREAMER_AUDIO_RECORDER_ERROR_START_FAILED,
            StreamerConstants.KSY_STREAMER_AUDIO_RECORDER_ERROR_UNKNOWN -> {
            }

            StreamerConstants.KSY_STREAMER_CAMERA_ERROR_UNKNOWN,
            StreamerConstants.KSY_STREAMER_CAMERA_ERROR_START_FAILED,
            StreamerConstants.KSY_STREAMER_CAMERA_ERROR_EVICTED,
            StreamerConstants.KSY_STREAMER_CAMERA_ERROR_SERVER_DIED -> {
                mStreamer?.stopCameraPreview()
            }

            StreamerConstants.KSY_STREAMER_VIDEO_ENCODER_ERROR_UNSUPPORTED,
            StreamerConstants.KSY_STREAMER_VIDEO_ENCODER_ERROR_UNKNOWN -> {
                handleEncodeError()

                if (mRecording && mMainHandler != null) {
                    stopRecord()
                    mMainHandler!!.postDelayed({
                        startRecord(mRecordingUrl)
                    }, 100L)
                }
            }

            StreamerConstants.KSY_STREAMER_FILE_PUBLISHER_CLOSE_FAILED,
            StreamerConstants.KSY_STREAMER_FILE_PUBLISHER_ERROR_UNKNOWN,
            StreamerConstants.KSY_STREAMER_FILE_PUBLISHER_OPEN_FAILED,
            StreamerConstants.KSY_STREAMER_FILE_PUBLISHER_FORMAT_NOT_SUPPORTED,
            StreamerConstants.KSY_STREAMER_FILE_PUBLISHER_WRITE_FAILED -> {
                // just stop record without retry.
                stopRecord()
                stopChronometer()
            }

            else -> reCasting(what)
        }
    }

    /**
     * 방송 송출을 재시작
     */
    open fun reCasting(what: Int) {
        Logger.e("reCasting")

        mReCast = true
        mStreamer?.showBgPicture("assets://pause.png")

        startCasting(null)
    }

    open fun handleEncodeError() {
        var encodeMethod = mStreamer?.videoEncodeMethod ?: StreamerConstants.ENCODE_METHOD_HARDWARE
        if (encodeMethod == StreamerConstants.ENCODE_METHOD_HARDWARE) {
            mHWEncoderUnsupported = true
            if (mSWEncoderUnsupported) {
                mStreamer?.setEncodeMethod(StreamerConstants.ENCODE_METHOD_SOFTWARE_COMPAT)
                Logger.e("Got HW encoder error, switch to SOFTWARE_COMPAT mode")
            } else {
                mStreamer?.setEncodeMethod(StreamerConstants.ENCODE_METHOD_SOFTWARE)
                Logger.e("Got HW encoder error, switch to SOFTWARE mode")
            }
        } else if (encodeMethod == StreamerConstants.ENCODE_METHOD_SOFTWARE) {
            mSWEncoderUnsupported = true
            mStreamer?.setEncodeMethod(StreamerConstants.ENCODE_METHOD_SOFTWARE_COMPAT)
            Logger.e("Got SW encoder error, switch to SOFTWARE_COMPAT mode")
        }
    }

    open fun onHandlePermissionRequestResult(requestCode: Int, permissions: Array<out String>, grantResult: IntArray) {
        Logger.c("onHandlePermissionRequestResult")

        when (requestCode) {
            BroadcastSettingActivity.PERMISSION_REQUEST_CAMERA_AUDIO_REC -> {
                if (grantResult.isNotEmpty() && grantResult[0] == PackageManager.PERMISSION_GRANTED) {
                    mStreamer?.startCameraPreview()
                } else {
                    Logger.e("No CAMERA or AudioRecord permission")
                    AppToast(context!!).showToastMessage(R.string.not_permission_granted,
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_MIDDLE)
                }
            }
        }
    }

//    /**
//     * 카메라 Preview 및 시스템 권한 검사
//     */
//    open fun startCameraPreviewWithPermCheck() {
//        Logger.e("startCameraPreviewWithPermCheck")
//
//        val cameraPerm = ActivityCompat.checkSelfPermission(context!!, Manifest.permission.CAMERA)
//        val audioPerm = ActivityCompat.checkSelfPermission(context!!, Manifest.permission.RECORD_AUDIO)
//        if (cameraPerm != PackageManager.PERMISSION_GRANTED || audioPerm != PackageManager.PERMISSION_GRANTED) {
//            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
//                Logger.e("No CAMERA or AudioRecord permission, please check")
//                Toast.makeText(context, "No CAMERA or AudioRecord permission, please check", Toast.LENGTH_LONG).show()
//            } else {
//                val permissions = arrayOf(Manifest.permission.CAMERA,
//                        Manifest.permission.RECORD_AUDIO,
//                        Manifest.permission.READ_EXTERNAL_STORAGE,
//                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                //Manifest.permission.READ_PHONE_STATE)
//                ActivityCompat.requestPermissions(activity as Activity, permissions, PERMISSION_REQUEST_CAMERA_AUDIO_REC)
//            }
//        } else {
//            mStreamer?.startCameraPreview()
//        }
//    }

    /**
     * PIP 시작
     */
    open fun startPip(url: String) {
        mStreamer?.hideBgVideo()
        mStreamer?.showBgVideo(url)
        mStreamer?.setCameraPreviewRect(0.65f, 0f, 0.35f, 0.3f)
        mStreamer?.switchMainScreen()
    }

    /**
     * 카메라 화면을 회전
     */
    open fun switchCamera() {
        mStreamer?.switchCamera()
    }

    /**
     * 카메라 촬영 좌우반전을 수행
     */
    open fun switchCameraOrientation() {
        mStreamer?.toggleCameraSourceMirror()
    }

    /**
     * 플래시 라이트를 토글
     * @return          플래시 켜짐 상태
     */
    open fun toggleFlashLight(): Boolean {
        mIsFlashOpened = if (mIsFlashOpened) {
            mStreamer?.toggleTorch(false)
            false
        } else {
            mStreamer?.toggleTorch(true)
            true
        }

        return mIsFlashOpened
    }

    private val mOnInfoListener = KSYStreamer.OnInfoListener { what, msg1, msg2 ->
        if (!mReCast) {
            onStreamerInfo(what, msg1, msg2)
        }
    }

    private val mOnErrorListener = KSYStreamer.OnErrorListener { what, msg1, msg2 ->
        onStreamerError(what, msg1, msg2)
    }
}