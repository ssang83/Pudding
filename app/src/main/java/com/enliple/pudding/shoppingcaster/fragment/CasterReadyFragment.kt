package com.enliple.pudding.shoppingcaster.fragment

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.*
import android.widget.Chronometer
import android.widget.Toast
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.enliple.pudding.R
import com.enliple.pudding.commons.app.ImageLoad
import com.enliple.pudding.commons.data.CategoryItem
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.*
import com.enliple.pudding.commons.network.vo.API136
import com.enliple.pudding.commons.network.vo.API72
import com.enliple.pudding.commons.widget.toast.AppToast
import com.enliple.pudding.shoppingcaster.activity.ShoppingCastActivity
import com.enliple.pudding.shoppingcaster.activity.VODPostActivity
import com.enliple.pudding.shoppingcaster.config.BaseStreamConfig
import com.google.gson.Gson
import com.ksyun.media.streamer.kit.StreamerConstants
import kotlinx.android.synthetic.main.fragment_caster_ready.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * 방송준비가 완료되고 방송을 시작하기 바로 직전 단계를 보여주는 Fragment
 * @author hkcha
 * @since 2018.08.27
 */
class CasterReadyFragment : androidx.fragment.app.Fragment(), ProgressRequestBody.ProgressCallback {
    companion object {
        const val BUNDLE_EXTRA_KEY_SUBJECT = "subject"
        const val BUNDLE_EXTRA_KEY_CASTING_MODE = "casting_mode"
        const val BUNDLE_EXTRA_VALUE_CASTING_VOD = 1
        const val BUNDLE_EXTRA_VALUE_CASTING_LIVE = 2
        const val BUNDLE_EXTRA_VALUE_CASTING_MULTI_LIVE = 3
        const val BUNDLE_EXTRA_VALUE_CASTING_VOD_UPLOAD = 4

        const val BUNDLE_EXTRA_KEY_FIRST_CATEGORY = "first_category"
        const val BUNDLE_EXTRA_KEY_SECOND_CATEGORY = "second_category"
        const val BUNDLE_EXTRA_KEY_THIRD_CATEGORY = "third_category"
        const val BUNDLE_EXTRA_KEY_COVER_IMAGE_URI = "coverImgUri"
        const val BUNDLE_EXTRA_KEY_STREAM_KEY = "streamKey"

        private const val ACTIVITY_REQUEST_CODE_GALLERY = 0x101
        private const val ACTIVITY_REQUEST_CODE_GALLERY_VIDEO = 0x102

        private const val PERCENT_FORMAT = "%d %s"
        private const val READY_TO_COUNTDOWN_SECONDS = 5
        private const val COUNTDOWN_TICK_INTERVAL = 1000L
        private const val COUNTDOWN_ANIM_DURATION = 400L

        private const val MIN_TIME = 60 // 10초를 의미
    }

    private var castingMode: Int = BUNDLE_EXTRA_VALUE_CASTING_LIVE               // defaults
    private var firstCategory: CategoryItem? = null
    private var secondCategory: CategoryItem? = null
    private var thirdCategory: ArrayList<CategoryItem> = ArrayList()
    private var subject: String? = null
    private var coverImgUri: Uri? = null
    private var mFileName: String = ""
    private var recordingFilePath: String = ""
    private var recordingFileUri: Uri? = null
    private var mHandler = Handler()
    private var mTimer: CountDownTimer? = null
    private lateinit var countDownAnimSet: AnimationSet
    private lateinit var queueAnimSet: AnimationSet
    private var isCastReady = false
    private var errorStr = ""
    private var isUploadActive = false
    private var streamKey = ""
    private var mLiveButtonClicked = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        EventBus.getDefault().register(this)

        initStartAnimationScenario()

        subject = arguments?.getString(BUNDLE_EXTRA_KEY_SUBJECT)
        castingMode = arguments?.getInt(BUNDLE_EXTRA_KEY_CASTING_MODE, BUNDLE_EXTRA_VALUE_CASTING_LIVE)!!
        Logger.i("onCreate() mode: $castingMode")
        firstCategory = arguments?.getParcelable(BUNDLE_EXTRA_KEY_FIRST_CATEGORY)
        secondCategory = arguments?.getParcelable(BUNDLE_EXTRA_KEY_SECOND_CATEGORY)
        streamKey = arguments?.getString(BUNDLE_EXTRA_KEY_STREAM_KEY) ?: ""

        var coverImg = arguments?.getString(BUNDLE_EXTRA_KEY_COVER_IMAGE_URI)
        if (coverImg != null) {
            coverImgUri = Uri.parse(coverImg)
        }

        var receivedThirdCategory: ArrayList<CategoryItem>? = arguments?.getParcelableArrayList(BUNDLE_EXTRA_KEY_THIRD_CATEGORY)
        if (receivedThirdCategory != null) {
            thirdCategory.addAll(receivedThirdCategory)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_caster_ready, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity as? ShoppingCastActivity)?.isCastingReady = true
    }

    override fun onDetach() {
        (activity as? ShoppingCastActivity)?.isCastingReady = false
        super.onDetach()
    }

    override fun onDestroy() {
        super.onDestroy()

        EventBus.getDefault().unregister(this)
        stopTimer()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        when (castingMode) {
            BUNDLE_EXTRA_VALUE_CASTING_MULTI_LIVE,
            BUNDLE_EXTRA_VALUE_CASTING_LIVE -> {
                buttonLive.visibility = View.VISIBLE
                layoutVodButtons.visibility = View.GONE
                buttonRotateCamera.visibility = View.VISIBLE
                buttonUpload.visibility = View.GONE
            }

            BUNDLE_EXTRA_VALUE_CASTING_VOD -> {
                buttonLive.visibility = View.GONE
                layoutVodButtons.visibility = View.VISIBLE
                buttonRotateCamera.visibility = View.GONE
                buttonUpload.visibility = View.GONE
            }
        }

        var thirdCategoryStr = ""
        if (thirdCategory != null) {
            for (i in 0 until thirdCategory?.size) {
                thirdCategoryStr += if (i == thirdCategory.size - 1) {
                    thirdCategory[i].categoryName
                } else {
                    "${thirdCategory[i].categoryName}, "
                }
            }
        }
        Logger.e("thirdCategoryStr :: " + thirdCategoryStr)
        textViewSubject.text = subject
        textViewCategory.text = "${firstCategory?.categoryName} > ${secondCategory?.categoryName} > $thirdCategoryStr"

        ImageLoad.setImage(view.context, imageViewThumbnail, coverImgUri, null, ImageLoad.SCALE_CIRCLE_CROP, DiskCacheStrategy.ALL)

        buttonBack.setOnClickListener(clickListener)
        buttonLive.setOnClickListener(clickListener)
        //buttonGallery.setOnClickListener(clickListener)
        buttonVOD.setOnClickListener(clickListener)
        buttonRotateCamera.setOnClickListener(clickListener)
        buttonRotateVODCamera.setOnClickListener(clickListener)
        buttonUpload.setOnClickListener(clickListener)

        chronometer.onChronometerTickListener = Chronometer.OnChronometerTickListener { chronometer ->
            if (chronometer != null) {
                var time = SystemClock.elapsedRealtime() - chronometer.base
                Logger.e("time :: $time")
                if (!isUploadActive) {
                    if (time > 1000 * MIN_TIME) {
                        isUploadActive = true
                        buttonUpload.setBackgroundResource(R.drawable.vod_complete_on_btn)
                    }
                }
            }
        }
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        if (requestCode == ACTIVITY_REQUEST_CODE_GALLERY_VIDEO && resultCode == Activity.RESULT_OK) {
//            var selectedMediaData = data?.data
//            var filePath = data?.getStringExtra("path")
//            Logger.e("Selected Media Data : $selectedMediaData, filePath : ${filePath}")
//
//            val intent = Intent(view!!.context, VODPostActivity::class.java)
//            intent.putExtra(VODPostActivity.INTENT_EXTRA_KEY_VIDEO_URI, selectedMediaData.toString())
//            intent.putExtra(VODPostActivity.INTENT_EXTRA_KEY_VIDEO_FILE_PATH, filePath)
//            intent.putExtra(VODPostActivity.INTENT_EXTRA_KEY_FROM_GALLERY, true)
//            startActivity(intent)
//        } else {
//            super.onActivityResult(requestCode, resultCode, data)
//        }
//    }

    override fun onProgress(progress: Long, total: Long) {
        var currentProgress = (100 * progress / total).toInt()
        progressBar.post { progressBar.progress = currentProgress }
        textViewProgress.post { textViewProgress.text = String.format(PERCENT_FORMAT, currentProgress, "%") }
        Logger.d("progress : $currentProgress")
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: NetworkBusResponse) {
        var api = NetworkHandler.getInstance(context!!).getKey(NetworkApi.API72.toString(), AppPreferences.getUserId(context!!)!!, "")
        val api136 = NetworkHandler.getInstance(context!!).getKey(NetworkApi.API136.toString(), AppPreferences.getUserId(context!!)!!, "")
        if (data.arg1 == api) {
            handleStartLiveInfo(data)
        } else if (data.arg1 == api136) {
            handleNetworkAPI136(data)
        }
    }

    private fun castReady() {
        var countText = READY_TO_COUNTDOWN_SECONDS

        mHandler?.postDelayed({
            var metrics = DisplayMetrics()
            activity!!.windowManager.defaultDisplay.getMetrics(metrics)

            mTimer = object : CountDownTimer(READY_TO_COUNTDOWN_SECONDS * COUNTDOWN_TICK_INTERVAL, COUNTDOWN_TICK_INTERVAL) {
                override fun onFinish() {
                    Logger.e("onFinish")

                    if (textViewCountDown != null && countDownLayer != null) {
                        textViewCountDown.text = "시작"
                        textViewCountDown.textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30.0f, metrics)
                        countDownLayer.visibility = View.VISIBLE
                        textViewCountDown.startAnimation(queueAnimSet)

                        Handler().postDelayed({
                            if (textViewCountDown != null && countDownLayer != null) {
                                countDownLayer?.visibility = View.GONE // 연결 실패 시 다시 원래 화면으로 되돌아와야하는 경우가 발생하므로
                                textViewCountDown?.setTextSize(TypedValue.COMPLEX_UNIT_SP, 220f) // 카운트 다운 후 통신 실패등의 이유로 다음 화면 진입 못해 다시 카운트다운 진행할 때 text size가 작게 나오는 현상 수정 위해
                                if (isCastReady) {
                                    (activity as ShoppingCastActivity)?.onCastingStarted()
                                } else {
                                    if (TextUtils.isEmpty(errorStr)) {
                                        errorStr = "서버와의 연결에 실패하였습니다. 잠시후 다시 이용해주시기 바랍니다."
                                    }
                                    AppToast(context!!).showToastMessage(errorStr,
                                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                                            AppToast.GRAVITY_BOTTOM)
                                }
                            }
                        }, 100L)
                    }
                }

                override fun onTick(millisUntilFinished: Long) {
                    Logger.e("onTick")

                    if (textViewCountDown != null && countDownLayer != null) {
                        countDownLayer.visibility = View.VISIBLE
                        textViewCountDown.text = countText.toString()
                        textViewCountDown.startAnimation(countDownAnimSet)
                        --countText
                    }
                }
            }

            mTimer?.start()
        }, 1000)
    }

    private fun initStartAnimationScenario() {
        val countAnimList = ArrayList<Animation>().apply {
            add(AlphaAnimation(1.0f, 0.0f).apply {
                startOffset = 0
                duration = COUNTDOWN_ANIM_DURATION
            })
            add(ScaleAnimation(1.0f, 0.0f, 1.0f, 0.0f,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f).apply {
                startOffset = 0
                duration = COUNTDOWN_ANIM_DURATION
            })
        }

        val queueAnimList = ArrayList<Animation>().apply {
            add(AlphaAnimation(1.0f, 0.0f).apply {
                startOffset = 0
                duration = COUNTDOWN_ANIM_DURATION
            })
            add(ScaleAnimation(1.0f, 1.0f, 1.0f, 1.0f,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f).apply {
                startOffset = 0
                duration = COUNTDOWN_ANIM_DURATION
            })
        }

        countDownAnimSet = AnimationSet(true).apply {
            interpolator = AccelerateInterpolator()
            duration = COUNTDOWN_ANIM_DURATION
            fillAfter = true
            repeatCount = 0

            for (animation in countAnimList) {
                addAnimation(animation)
            }
        }

        queueAnimSet = AnimationSet(true).apply {
            interpolator = AccelerateInterpolator()
            duration = 1000L
            fillAfter = true
            repeatCount = 0

            for (animation in queueAnimList) {
                addAnimation(animation)
            }

            setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?) {}
                override fun onAnimationStart(animation: Animation?) {}
                override fun onAnimationEnd(animation: Animation?) {
                    textViewCountDown?.visibility = View.INVISIBLE
                }
            })
        }
    }

    private fun stopTimer() {
        if (mHandler != null && mTimer != null) {
            mTimer!!.cancel()
            mHandler.removeCallbacksAndMessages(null)
        }
    }

    private fun handleStartLiveInfo(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            isCastReady = true
            val str = DBManager.getInstance(context!!).get(data.arg1)
            val response: API72 = Gson().fromJson(str, API72::class.java)
            Logger.e("handleStartLiveInfo : $response")

            (activity as? ShoppingCastActivity)?.castUrl = response.stream_name
            (activity as? ShoppingCastActivity)?.title = response.title
            (activity as? ShoppingCastActivity)?.tag = response.tag
            (activity as? ShoppingCastActivity)?.chatRoomId = response.chat_key
            (activity as? ShoppingCastActivity)?.streamKey = response.stream_key
            (activity as? ShoppingCastActivity)?.productArray = response.relationPrd.data
        } else {
            Logger.e("data.arg3 :: ${data.arg3} and data.arg4 :: ${data.arg4}")
            errorStr = data.arg4
        }
    }

    private fun handleNetworkAPI136(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            isCastReady = true
            val response: API136 = Gson().fromJson(DBManager.getInstance(context!!).get(data.arg1), API136::class.java)
            Logger.e("broadcast schedule info : $response")

            (activity as? ShoppingCastActivity)?.castUrl = response.stream_name
            (activity as? ShoppingCastActivity)?.title = response.title
            (activity as? ShoppingCastActivity)?.tag = response.tag
            (activity as? ShoppingCastActivity)?.chatRoomId = response.chat_key
            (activity as? ShoppingCastActivity)?.streamKey = response.stream_key
            (activity as? ShoppingCastActivity)?.productItems = response.relationPrd.data
        } else {
            Logger.e("data.arg3 :: ${data.arg3} and data.arg4 :: ${data.arg4}")
            errorStr = data.arg4
        }
    }

    private fun videoFromGallery() {
        var pickIntent = if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        } else {
            Intent(Intent.ACTION_PICK, MediaStore.Video.Media.INTERNAL_CONTENT_URI)
        }

        startActivityForResult(pickIntent.apply {
            type = "video/mp4"
            action = Intent.ACTION_PICK
            putExtra("return-data", true)
        }, ACTIVITY_REQUEST_CODE_GALLERY)
    }

    /**
     * 녹화 시간을 카운팅하는 Chronometer 를 시작
     */
    private fun startChronometer() {
        chronometer.base = SystemClock.elapsedRealtime()
        chronometer.start()
//        mIsChronometerStarted = true
    }

    /**
     * 녹화 시간을 카운팅하는 Chronometer 를 종료
     */
    private fun stopChronometer() {
        chronometer?.base = SystemClock.elapsedRealtime()
        chronometer?.stop()
//        mIsChronometerStarted = false
    }

    /**
     * 비디오 파일 업로드 준비 및 준비 완료시 업로드 요청 호출
     */
/*    private fun prepareUpload() {
        layoutProgress.visibility = View.VISIBLE
        progressBar.isIndeterminate = true
        textViewProgress.text = getString(R.string.msg_ready_to_vod_upload)

        var uploadService = RetrofitNetworkProvider
                .createRetrofitMultipartInstance(view!!.context, (activity as? ShoppingCastActivity)?.uploadHost!!, VODService::class.java)

        Handler().postDelayed({ uploadVideo((activity as? ShoppingCastActivity)?.response!!, uploadService) }, 3000L)
    }*/

    /**
     * 미디버스로 VOD 비디오 업로드
     */
//    private fun uploadVideo(response: VODResponse.VODUploadInfoResponse, service: VODService) {
//        progressBar.isIndeterminate = false
//        progressBar.progress = 0
//
//        var requestBody = RetrofitNetworkProvider
//                .createProgressRequestBody(view!!.context, Uri.fromFile(uploadFile),
//                        MediaType.parse("video/*")!!, this@CasterReadyFragment)
//
//        var part = RetrofitNetworkProvider
//                .createMultipartBody("file", "${response.fileName}.mp4", requestBody)
//
//        service.uploadVOD(response.xMBusToken, response.xMBusChannel, response.uploadPath, part)
//                .enqueue(object : Callback<VODResponse.XMBusUploadResponse> {
//                    override fun onFailure(call: Call<VODResponse.XMBusUploadResponse>?, t: Throwable?) {
//                        Logger.e("Upload Response Failure : $t")
//                        progressBar.visibility = View.GONE
//                        textViewProgress.text = getString(R.string.msg_vod_upload_error)
//                    }
//
//                    override fun onResponse(call: Call<VODResponse.XMBusUploadResponse>?, response: Response<VODResponse.XMBusUploadResponse>?) {
//                        Logger.e("Upload Response : $response")
//
//                        val response = response ?: return
//                        val body = response?.body() ?: return
//
//                        Logger.e("Upload Result : success : ${body.isSuccess}, mediaId : ${body.mediaId}")
//
//                        AppToast(view!!.context).showToastMessage(getString(R.string.msg_vod_upload_complete),
//                                AppToast.DURATION_MILLISECONDS_DEFAULT,
//                                AppToast.GRAVITY_BOTTOM)
//                    }
//                })
//    }

    /**
     * VOD 녹화시 파일 저장되는 경로 가져오기
     */
    private fun getVODFilePath(): String {
        var storageDir: File? = null
        try {
            val dir = File(Environment.getExternalStorageDirectory().toString() + "/vod/")
            if (!dir.exists()) {
                dir.mkdir()
            }

            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            mFileName = "$timeStamp.mp4"
            storageDir = File(Environment.getExternalStorageDirectory().absoluteFile.toString() + "/vod/" + mFileName)
        } catch (e: Exception) {
            Logger.p(e)
        }

        Logger.e("getVODFilePath:" + storageDir?.absolutePath)
        return storageDir?.absolutePath ?: ""
    }

    private fun registerBroadcastInfo(info: com.enliple.pudding.shoppingcaster.data.BroadcastInfo) {
        Logger.d("registerBroadcastInfo: ${info.categoryCode}")

        isCastReady = false
        var coverImg = File(Uri.parse(info.image).path)
        var requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), coverImg)
        var fileBody = MultipartBody.Part.createFormData("strThumbnail", coverImg.name, requestFile)
        val baseStreamConfig = BaseStreamConfig()

        var body = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("strTitle", info.subject)
                .addFormDataPart("strCategory", info.categoryCode)
                .addFormDataPart("strTag", info.tag)
                .addFormDataPart("strNoti", info.registration)
                .addFormDataPart("strMulti", info.strMulti)
                .addFormDataPart("strAge", info.strAge)
                .addFormDataPart("strSex", info.strSex)
                .addFormDataPart("strContentSize", if(baseStreamConfig.mOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) "v" else "h")
                .addFormDataPart("strContentRate", getResolution(baseStreamConfig.mTargetResolution))
                .addPart(fileBody)

        if (info.productItems != null && info.productItems.size > 0) {
            for (i in info.productItems.indices) {
                var key = "strItems${i + 1}"
                body.addFormDataPart(key, info.productItems[i])
            }
        }

        EventBus.getDefault().post(NetworkBus(NetworkApi.API72.name, body.build()))
        //(activity as? ShoppingCastActivity)?.startCasting()
        castReady()
    }

    private fun getResolution(resolution: Int) :String{
        return when(resolution) {
            StreamerConstants.VIDEO_RESOLUTION_360P -> "360"
            StreamerConstants.VIDEO_RESOLUTION_480P -> "480"
            StreamerConstants.VIDEO_RESOLUTION_540P -> "540"
            StreamerConstants.VIDEO_RESOLUTION_720P -> "720"
            StreamerConstants.VIDEO_RESOLUTION_1080P -> "1080"
            else -> "720"
        }
    }

    private fun goBroadcastForSchedule() {
        JSONObject().apply {
            put("streamKey", streamKey)
        }.let {
            val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), it.toString())
            NetworkBus(NetworkApi.API136.name, body).let {
                EventBus.getDefault().post(it)
            }
        }

        castReady()
    }

    private val clickListener = View.OnClickListener {
        when (it?.id) {
            R.id.buttonBack -> (activity as? ShoppingCastActivity)?.castingReadyToBack()

            R.id.buttonRotateCamera,
            R.id.buttonRotateVODCamera -> (activity as? ShoppingCastActivity)?.switchCamera()

            R.id.buttonLive -> {
                Handler().postDelayed({
                    mLiveButtonClicked = false
                }, 2000L)
                Logger.e("")
                if ( !mLiveButtonClicked && countDownLayer.visibility != View.VISIBLE ) {
                    mLiveButtonClicked = true
                    Logger.e("clicked !!!")
                    if (streamKey.isEmpty()) {
                        var info = getBroadcastInfo()
                        if (info == null) {
                            (activity as? ShoppingCastActivity)?.castingReadyToBack()
                        } else {
                            registerBroadcastInfo(info)
                        }
                    } else {
                        goBroadcastForSchedule()
                    }
                }

            }

            R.id.buttonVOD -> {
                buttonVOD.isSelected = !buttonVOD.isSelected

                if (buttonVOD.isSelected) {
                    buttonUpload.visibility = View.VISIBLE
                    buttonUpload.setBackgroundResource(R.drawable.vod_complete_off_btn)

                    isUploadActive = false
                    layoutChronometer.visibility = View.VISIBLE
                    layoutHeader.visibility = View.GONE

                    startChronometer()

                    recordingFilePath = getVODFilePath()
                    recordingFileUri = Uri.fromFile(File(recordingFilePath))

                    (activity as? ShoppingCastActivity)?.castUrl = recordingFilePath
                    (activity as? ShoppingCastActivity)?.startRecord()
                } else {
                    stopChronometer()
                    (activity as? ShoppingCastActivity)?.onCastRecordStop()
                }
            }

            R.id.buttonUpload -> {
                if (!isUploadActive) {
                    AppToast(context!!).showToastMessage("1분 이상 녹화해 주세요..",
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_MIDDLE)
                    return@OnClickListener
                }

                isUploadActive = false
                if (buttonVOD.isSelected) {
                    stopChronometer()
                    (activity as? ShoppingCastActivity)?.onCastRecordStop()
                }

                startActivity(Intent(it!!.context, VODPostActivity::class.java).apply {
                    putExtra(VODPostActivity.INTENT_EXTRA_KEY_VIDEO_FILE_PATH, recordingFilePath)
                    putExtra(VODPostActivity.INTENT_EXTRA_KEY_VIDEO_URI, recordingFileUri.toString())
                    putExtra(VODPostActivity.INTENT_EXTRA_KEY_FROM_GALLERY, false)
                })

                // 종료..
                (activity as? ShoppingCastActivity)?.finish()
            }
        }
    }

    private fun getBroadcastInfo(): com.enliple.pudding.shoppingcaster.data.BroadcastInfo? {
        var info = AppPreferences.getBroadcastInfo(context!!)
        Logger.e("getBroadcastInfo :: $info")
        try {
            var infoModel = com.enliple.pudding.shoppingcaster.data.BroadcastInfo()
            var obj = JSONObject(info)
            infoModel.subject = obj.optString(com.enliple.pudding.shoppingcaster.data.BroadcastInfo.KEY_SUBJECT)
            infoModel.registration = obj.optString(com.enliple.pudding.shoppingcaster.data.BroadcastInfo.KEY_REGISTRATION)
            infoModel.tag = obj.optString(com.enliple.pudding.shoppingcaster.data.BroadcastInfo.KEY_TAG)
            infoModel.categoryCode = obj.optString(com.enliple.pudding.shoppingcaster.data.BroadcastInfo.KEY_CATEGORY_CODE)
            infoModel.image = obj.optString(com.enliple.pudding.shoppingcaster.data.BroadcastInfo.KEY_IMAGE)
            var productArray = ArrayList<String>()
            var jsonArray = obj.optJSONArray(com.enliple.pudding.shoppingcaster.data.BroadcastInfo.KEY_PRODUCT_ITEMS)
            if (jsonArray != null && jsonArray.length() > 0) {
                for (i in 0 until jsonArray.length()) {
                    var subObject = jsonArray.get(i)
                    var item = subObject.toString()
                    productArray.add(item)
                }
            }

            var firstCategoryObj = obj.optJSONObject(com.enliple.pudding.shoppingcaster.data.BroadcastInfo.KEY_FIRST_CATEGORY)
            var firstCategoryItem: CategoryItem? = null
            if (firstCategoryObj != null) {
                var categoryId = firstCategoryObj.optString(com.enliple.pudding.shoppingcaster.data.BroadcastInfo.KEY_CATEGORY_ID)
                var categoryName = firstCategoryObj.optString(com.enliple.pudding.shoppingcaster.data.BroadcastInfo.KEY_CATEGORY_NAME)
                var categoryImage = firstCategoryObj.optString(com.enliple.pudding.shoppingcaster.data.BroadcastInfo.KEY_CATEGORY_IMAGE)
                firstCategoryItem = CategoryItem(0L, categoryId, categoryName, "", "", categoryImage)
            }

            var secondCategoryObj = obj.optJSONObject(com.enliple.pudding.shoppingcaster.data.BroadcastInfo.KEY_SECOND_CATEGORY)
            var secondCategoryItem: CategoryItem? = null
            if (secondCategoryObj != null) {
                var categoryId = secondCategoryObj.optString(com.enliple.pudding.shoppingcaster.data.BroadcastInfo.KEY_CATEGORY_ID)
                var categoryName = secondCategoryObj.optString(com.enliple.pudding.shoppingcaster.data.BroadcastInfo.KEY_CATEGORY_NAME)
                var categoryImage = secondCategoryObj.optString(com.enliple.pudding.shoppingcaster.data.BroadcastInfo.KEY_CATEGORY_IMAGE)
                secondCategoryItem = CategoryItem(0L, categoryId, categoryName, "", "", categoryImage)
            }

            var thirdCategoryArray = ArrayList<CategoryItem>()
            var thirdCategoryJsonArray = obj.optJSONArray(com.enliple.pudding.shoppingcaster.data.BroadcastInfo.KEY_THIRD_CATEGORY)
            if (thirdCategoryJsonArray != null && thirdCategoryJsonArray.length() > 0) {
                for (i in 0 until thirdCategoryJsonArray.length()) {
                    var subObject: JSONObject = thirdCategoryJsonArray.getJSONObject(i)
                    var categoryId = subObject.optString(com.enliple.pudding.shoppingcaster.data.BroadcastInfo.KEY_CATEGORY_ID)
                    var categoryName = subObject.optString(com.enliple.pudding.shoppingcaster.data.BroadcastInfo.KEY_CATEGORY_NAME)
                    var categoryImage = subObject.optString(com.enliple.pudding.shoppingcaster.data.BroadcastInfo.KEY_CATEGORY_IMAGE)
                    var categoryItem = CategoryItem(0L, categoryId, categoryName, "", "", categoryImage)
                    thirdCategoryArray.add(categoryItem)
                }
            }
            infoModel.thirdCategory = thirdCategoryArray
            infoModel.secondCategory = secondCategoryItem
            infoModel.firstCategory = firstCategoryItem
            infoModel.productItems = productArray
            infoModel.strMulti = obj.optString(com.enliple.pudding.shoppingcaster.data.BroadcastInfo.KEY_MULTI)
            infoModel.strAge = obj.optString(com.enliple.pudding.shoppingcaster.data.BroadcastInfo.KEY_AGE)
            infoModel.strSex = obj.optString(com.enliple.pudding.shoppingcaster.data.BroadcastInfo.KEY_SEX)
            return infoModel
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}