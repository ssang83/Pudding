package com.enliple.pudding.shoppingcaster.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.enliple.pudding.AbsBaseActivity
import com.enliple.pudding.R
import com.enliple.pudding.activity.BroadcastSettingActivity
import com.enliple.pudding.commons.app.NetworkStatusUtils
import com.enliple.pudding.commons.data.CategoryItem
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.NetworkApi
import com.enliple.pudding.commons.network.NetworkBus
import com.enliple.pudding.commons.network.NetworkBusFastResponse
import com.enliple.pudding.commons.network.NetworkBusResponse
import com.enliple.pudding.commons.network.vo.API136
import com.enliple.pudding.commons.network.vo.API142
import com.enliple.pudding.commons.network.vo.API72
import com.enliple.pudding.commons.widget.toast.AppToast
import com.enliple.pudding.keyboard.KeyboardHeightProvider
import com.enliple.pudding.shoppingcaster.VCommerceStreamer
import com.enliple.pudding.shoppingcaster.config.BaseStreamConfig
import com.enliple.pudding.shoppingcaster.fragment.CasterReadyFragment
import com.enliple.pudding.shoppingcaster.fragment.CasterUIFragment
import com.enliple.pudding.shoppingcaster.fragment.CasterVideoFragment
import com.enliple.pudding.shoppingcaster.widget.CasterProductDialog
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_caster.*
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

/**
 * 쇼핑 케스트 방송 Activity
 * @author hkcha
 * @since 2018.6.5
 */
class ShoppingCastActivity : AbsBaseActivity(), CasterVideoFragment.ClickTimeListener {
    companion object {
        private const val ACTIVITY_REQUEST_CODE_GALLERY_VIDEO = 1000
    }

    private var readyFragment: CasterReadyFragment? = null
    private var casterFragment: CasterVideoFragment? = null
    private var uiFragment: CasterUIFragment? = null

    //private var alertDialog: AlertDialog? = null

    private var cTime: Long = 0
    var coverImage: Uri? = null
    var notifyString: String? = ""
    var title: String? = null
    var tag: String? = null
    var castUrl: String? = null
    var chatRoomId: String? = ""
    var castGubun: String? = ""
    var streamKey: String = ""
    var productArray: List<API72.RelationPrdBean.ProductItem> = mutableListOf()
    var productItems: List<API136.Products.ProductItem> = mutableListOf()
    var subject: String? = ""
    var regi: String? = ""
    var gubun: Int? = 0
    var firstCategory: CategoryItem? = null
    var secondCategory: CategoryItem? = null
    var thirdCategory: ArrayList<CategoryItem>? = null
    var scheduleStreamKey = ""
    var chatIP = ""
    var chatPort = 0

    private var mHandler: Handler = Handler()

    private lateinit var chatAccount: String
    private lateinit var chatNickName: String

    var isCastingReady = false
    var screenHeight = 0

    private var mKeyboardHeightProvider: KeyboardHeightProvider? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)

        mActivityList.add(this)     // 강제종료 시키기 위해서 리스트에 추가

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        setContentView(R.layout.activity_caster)

        checkIntent()

        Logger.e("gubun:$gubun")

        if (gubun == CasterReadyFragment.BUNDLE_EXTRA_VALUE_CASTING_VOD_UPLOAD) {
            // 동영상 업로드 처리
            startActivityForResult(Intent(this, VODSelectActivity::class.java), ACTIVITY_REQUEST_CODE_GALLERY_VIDEO)
        } else {
            castingInfo()

            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            screenHeight = displayMetrics.heightPixels

            // 소프트 키보드가 변경 될 경우 Bus 로 data 가 온다.
            mKeyboardHeightProvider = KeyboardHeightProvider(this)
            Handler().postDelayed({ mKeyboardHeightProvider?.start() }, 1000)

            if (scheduleStreamKey.isEmpty()) {
                castingReady(subject!!, regi!!, coverImage!!, gubun!!, firstCategory!!, secondCategory!!, thirdCategory!!)
            } else {
                castingScheduleReady()
            }
        }
    }

    override fun onDozeModeStateChanged(dozeEnable: Boolean) {}
    override fun onNetworkStatusChanged(status: NetworkStatusUtils.NetworkStatus) {}

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)

        mActivityList.remove(this)      // 정상종료인 경우 리스트에서 제거
        mKeyboardHeightProvider?.close()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == ACTIVITY_REQUEST_CODE_GALLERY_VIDEO) {
            if (resultCode == Activity.RESULT_OK) {
                var media = data?.data
                var filePath = data?.getStringExtra("path")
                Logger.e("Selected Media Data : $media, filePath : $filePath")

                val intent = Intent(this, VODPostActivity::class.java)
                intent.putExtra(VODPostActivity.INTENT_EXTRA_KEY_VIDEO_URI, media.toString())
                intent.putExtra(VODPostActivity.INTENT_EXTRA_KEY_VIDEO_FILE_PATH, filePath)
                intent.putExtra(VODPostActivity.INTENT_EXTRA_KEY_FROM_GALLERY, true)
                startActivity(intent)

                finish()
            } else {
                Logger.e("pick video fail !!")
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onBackPressed() {
        when {
            isCasting() -> {
                uiFragment?.onBackPressed()
            }

            isCastingReady -> {
                castingReadyToBack()
            }

            else -> {
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
    }

    override fun getClickTime() {
        Logger.e("getClickTime called")
        cTime = System.currentTimeMillis()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: NetworkBusFastResponse) {
        if (data.arg1 == NetworkApi.API142.toString()) {
            if ("ok" == data.arg2) {
                val response: API142 = Gson().fromJson(DBManager.getInstance(this).get(data.arg1), API142::class.java)

                chatIP = response.ip
                chatPort = response.port
                chatRoomId = response.roomid

//                showCastUI()
                onCastStart()
            }
        }
    }

    private fun isCasting(): Boolean = casterFragment?.mStreaming ?: false

    /**
     * 방송 시작을 수행
     */
    fun onCastStart() {
        if (casterFragment == null) {
            Logger.e("casterFragment null url :: $castUrl")
        }
        casterFragment?.startCasting(castUrl)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
    }

    /**
     * 방송 종료 버튼 눌렀을 때 호출 된다.
     */
    fun onCastStop(body: RequestBody?) {
        casterFragment?.stopCasting()

        if (body != null) {
            EventBus.getDefault().post(NetworkBus(NetworkApi.API72.name, body)) // 전송 실패하면 땡이다.
        }
    }

    /**
     * 방송 pause
     */
    fun onCastPause() {
        casterFragment?.handleOnPause()
    }

    /**
     * 방송 resume
     */
    fun onCastResume() {
        casterFragment?.handleOnResume()
    }

    /**
     * 카메라 전/후 전환
     */
    fun switchCamera() {
        casterFragment?.switchCamera()
    }

    fun setBeautyFilter(setFilter: Boolean) {
        casterFragment?.setFilter(setFilter)
    }

    fun getStream(): VCommerceStreamer? {
        return casterFragment?.getStream()
    }

    fun startPip(url: String) {
        casterFragment?.startPip(url)
    }

    /**
     * 오류로 인한 종료처리
     */
    fun errorFinish() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    /**
     * 방송이 시작되었음
     */
    fun onCastingStarted() {
        Logger.d("Video casting is started.")

        //showCastUI()
        JSONObject().apply {
            put("streamKey", streamKey)
            put("user", AppPreferences.getUserId(this@ShoppingCastActivity)!!)
        }.let {
            val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), it.toString())
            NetworkBus(NetworkApi.API142.name, body).let { EventBus.getDefault().post(it) }
        }
    }

    /**
     * 방송을 종료하였음
     */
    fun onCastingStopped() {
        Logger.d("Video casting is stopped.")
        removeCastUI()

        AppToast(this).showToastMessage("방송이 종료 되었습니다.\n종료 된 방송은 내부 검토를 거쳐 리스트에 자동으로 노출 됩니다.",
                AppToast.DURATION_MILLISECONDS_DEFAULT,
                AppToast.GRAVITY_BOTTOM)

        mHandler.postDelayed({
            setResult(Activity.RESULT_OK)
            finish()
        }, 1000L)
    }

    /**
     * VOD 레코딩 시작
     */
    fun startRecord() {
        casterFragment?.startRecord(castUrl)
    }

    /**
     * VOD 레코딩 정지
     */
    fun onCastRecordStop() {
        casterFragment?.stopRecord()
    }

    /**
     * 안내 팝업을 노출
     */
//    fun showAlertDialog(message: String, listener: DialogInterface.OnClickListener?) {
//        dismissAlertDialog()
//
//        val builder = AlertDialog.Builder(this)
//
//        if (listener != null) {
//            builder.setPositiveButton(android.R.string.ok, listener)
//        } else {
//            builder.setPositiveButton(android.R.string.ok) { _, _ -> dismissAlertDialog() }
//        }
//
//        builder.setMessage(message)
//        alertDialog = builder.create()
//        alertDialog?.show()
//    }

    fun switchCameraOrientation() {
        casterFragment?.switchCameraOrientation()
    }

//    fun getCastingGLRender(): GLRender? = casterFragment?.getGLRender()
//
//    fun clearCastingVideoFilter() {
//        casterFragment?.clearFilter()
//    }

    fun addImageStickerByResource(stickerResourceId: Int) {
        Logger.e("addImageStickerByResource : $stickerResourceId")
        casterFragment?.addImageStickerByResource(stickerResourceId)
    }

    fun removeImageStickers() {
        Logger.e("removeImageStickers")
        casterFragment?.removeAllSticker()
    }

    /**
     * 호출된 AppLink 체크
     */
    private fun checkIntent() {
        Logger.d("checkIntent - ${intent.data}")
        chatAccount = intent.getStringExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_CHAT_ACCOUNT)
                ?: ""
        chatNickName = intent.getStringExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_CHAT_NICKNAME)
                ?: ""
        chatRoomId = intent.getStringExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_CHAT_ROOM_ID)
                ?: ""
        castGubun = intent.getStringExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_TAB) ?: ""
        subject = intent.getStringExtra(BroadcastSettingActivity.INFO_SUBJECT) ?: ""
        regi = intent.getStringExtra(BroadcastSettingActivity.INFO_REGI) ?: ""
        gubun = intent.getIntExtra(BroadcastSettingActivity.INFO_GUBUN, 0)
        firstCategory = intent.getParcelableExtra(BroadcastSettingActivity.INFO_FIRST_CATEGORY)
                ?: null
        secondCategory = intent.getParcelableExtra(BroadcastSettingActivity.INFO_SECOND_CATEGORY)
                ?: null
        thirdCategory = intent.getParcelableArrayListExtra(BroadcastSettingActivity.INFO_THIRD_CATEGORY)
                ?: null
        var cover = intent.getStringExtra(BroadcastSettingActivity.INFO_COVER_IMAGE) ?: ""
        if (!cover.isEmpty()) {
            coverImage = Uri.parse(cover)
        }

        scheduleStreamKey = intent.getStringExtra("streamKey") ?: ""
    }

    /**
     * 방송 정보 입력 UI를 초기화
     */
    private fun castingInfo() {
        casterFragment = CasterVideoFragment().apply {
            arguments = Bundle().apply {
                putString("initStreamingConfig", BaseStreamConfig().apply { mUrl = castUrl }.toJson())
            }
            setClickTimeListener(this@ShoppingCastActivity)
            setFilter(true)
        }
        var transaction = supportFragmentManager.beginTransaction()
        transaction.add(layoutVideoContainer.id, casterFragment!!, casterFragment!!::class.java.name)
        transaction.commitAllowingStateLoss()
    }

    /**
     * 방송 대기 UI 를 초기화
     */
    private fun castingReady(title: String, notice: String, coverImage: Uri, castingMode: Int, category: CategoryItem, secondCategory: CategoryItem, items: ArrayList<CategoryItem>) {
        Logger.e("castingReady:$title, $notice, $coverImage, $castingMode, ${category.categoryName}")

        this.title = title
        this.notifyString = notice
        this.coverImage = coverImage

        if (readyFragment == null) {
            readyFragment = CasterReadyFragment()
        }

        readyFragment?.arguments = Bundle().apply {
            putString(CasterReadyFragment.BUNDLE_EXTRA_KEY_COVER_IMAGE_URI, coverImage.toString())
            putString(CasterReadyFragment.BUNDLE_EXTRA_KEY_SUBJECT, title)
            putInt(CasterReadyFragment.BUNDLE_EXTRA_KEY_CASTING_MODE, castingMode)
            putParcelable(CasterReadyFragment.BUNDLE_EXTRA_KEY_FIRST_CATEGORY, category)
            putParcelable(CasterReadyFragment.BUNDLE_EXTRA_KEY_SECOND_CATEGORY, secondCategory)
            putParcelableArrayList(CasterReadyFragment.BUNDLE_EXTRA_KEY_THIRD_CATEGORY, items)
        }

        var transaction = supportFragmentManager.beginTransaction()
        transaction.add(layoutUiContainer.id, readyFragment!!, readyFragment!!::class.java.name)
        transaction.attach(readyFragment!!)
        transaction.show(readyFragment!!)
        transaction.commitAllowingStateLoss()
    }

    private fun castingScheduleReady() {
        if (readyFragment == null) {
            readyFragment = CasterReadyFragment()
        }

        readyFragment?.arguments = Bundle().apply {
            putString(CasterReadyFragment.BUNDLE_EXTRA_KEY_STREAM_KEY, scheduleStreamKey)
            putInt(CasterReadyFragment.BUNDLE_EXTRA_KEY_CASTING_MODE, gubun!!)
        }

        var transaction = supportFragmentManager.beginTransaction()
        transaction.add(layoutUiContainer.id, readyFragment!!, readyFragment!!::class.java.name)
        transaction.attach(readyFragment!!)
        transaction.show(readyFragment!!)
        transaction.commitAllowingStateLoss()
    }

    /**
     * 방송 대기 UI 에서 정보입력 UI 로 이동
     */
    fun castingReadyToBack() {
        finish()
    }

    /**
     * 방송중 상품 팝업을 표시
     * @param castId
     */
    fun showProductDialog() {
        var dialog = CasterProductDialog()
        var bundle = Bundle()
        bundle.putInt("screenHeight", screenHeight)
        bundle.putString("casterId", AppPreferences.getUserId(this@ShoppingCastActivity))
        bundle.putString("streamKey", streamKey)

        dialog.arguments = bundle
        dialog.show(supportFragmentManager, CasterProductDialog::class.java.name)
    }

    /**
     * 방송중 표시되어야 하는 UI 를 화면에 추가
     */
    fun showCastUI() {
        uiFragment = supportFragmentManager.findFragmentByTag(CasterUIFragment::class.java.name) as? CasterUIFragment
        if (uiFragment == null) {
            uiFragment = CasterUIFragment()
        }

        uiFragment?.arguments = Bundle().apply {
            putString(CasterUIFragment.BUNDLE_EXTRA_KEY_ROOM_ID, chatRoomId)
            putString(CasterUIFragment.BUNDLE_EXTRA_KEY_ACCOUNT, chatAccount)
            putString(CasterUIFragment.BUNDLE_EXTRA_KEY_NICKNAME, chatNickName)
            putString(CasterUIFragment.BUNDLE_EXTRA_KEY_CHAT_IP, chatIP)
            putInt(CasterUIFragment.BUNDLE_EXTRA_KEY_CHAT_PORT, chatPort)
            putBoolean(CasterUIFragment.BUNDLE_EXTRA_KEY_CAST_SCHEDULE, if (scheduleStreamKey.isNotEmpty()) true else false)
        }

        var transaction = supportFragmentManager.beginTransaction()
        transaction.replace(layoutUiContainer.id, uiFragment!!, uiFragment!!::class.java.name)
        transaction.commitAllowingStateLoss()
    }

    /**
     * 방송중 표시되는 UI 를 화면에서 제거
     */
    private fun removeCastUI() {
        if (uiFragment != null) {
            var transaction = supportFragmentManager.beginTransaction()
            transaction.detach(uiFragment!!)
            transaction.remove(uiFragment!!)
            transaction.commitAllowingStateLoss()
        }
    }

    /**
     * 안내 팝업을 닫기
     */
//    private fun dismissAlertDialog() {
//        alertDialog?.dismiss()
//        alertDialog = null
//    }

//    private fun goSetting() {
//        val i = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID))
//        startActivity(i)
//    }
}