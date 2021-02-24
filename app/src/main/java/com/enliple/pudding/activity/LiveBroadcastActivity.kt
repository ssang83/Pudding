package com.enliple.pudding.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.enliple.pudding.AbsBaseActivity
import com.enliple.pudding.BuildConfig
import com.enliple.pudding.R
import com.enliple.pudding.activity.*
import com.enliple.pudding.adapter.my.ChannelAdapter
import com.enliple.pudding.common.VideoDataContainer
import com.enliple.pudding.commons.app.ImageLoad
import com.enliple.pudding.commons.app.NetworkStatusUtils
import com.enliple.pudding.commons.app.Utils
import com.enliple.pudding.commons.data.TempVOD
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.db.VodDBManager
import com.enliple.pudding.commons.internal.AppConstants
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.*
import com.enliple.pudding.commons.network.vo.API0
import com.enliple.pudding.commons.network.vo.API21
import com.enliple.pudding.commons.network.vo.VOD
import com.enliple.pudding.commons.shoptree.network.ShopTreeAsyncTask
import com.enliple.pudding.commons.widget.recyclerview.WrappedLinearLayoutManager
import com.enliple.pudding.commons.widget.toast.AppToast
import com.enliple.pudding.model.PermissionObject
import com.enliple.pudding.widget.MainPermissionDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_pudding_channel.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

/**
 * main live 방송 시작 activity
 */
class LiveBroadcastActivity : AbsBaseActivity(), ChannelAdapter.Listener {
    companion object {
        private const val ACTIVITY_REQUEST_CODE_VOD = 0xAB01
        private const val ACTIVITY_REQUEST_CODE_LIVE = 0xAB02
        private const val ACTIVITY_REQUEST_CODE_RECORDING = 0xAB03

        val PERMISSION_REQUEST_LIVE = 1
        val PERMISSION_REQUEST_VOD = 2

        val INDEX_CAMERA = 0
        val INDEX_RECORD_AUDIO = 1
        val INDEX_READ_EXTERNAL_STORAGE = 2
        val INDEX_WRITE_EXTERNAL_STORAGE = 3
        val TRUE_VALUE = "TRUE"
        val FALSE_VALUE = "FALSE"
        val NONE_VALUE = "NONE"
    }

    private var mAdapter: ChannelAdapter? = null
    private var cTime = 0L
    private var dialog: MainPermissionDialog? = null
    private var rationalArray: java.util.ArrayList<String>? = null
    private var requestPermissionArray: java.util.ArrayList<String>? = null
    private var isTempVOD = false
    private var tempVODCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //inflater.inflate(R.layout.fragment_pudding_channel, container, false)
        setContentView(R.layout.fragment_pudding_channel)

        EventBus.getDefault().register(this)
        mActivityList.add(this)     // 강제 Activity를 종료 시키려면 리스트에 추가한다.

        recyclerViewVod.setHasFixedSize(true)
        recyclerViewVod.layoutManager = WrappedLinearLayoutManager(this).apply {
            orientation = LinearLayoutManager.HORIZONTAL
        }

        if (recyclerViewVod != null) {
            mAdapter = ChannelAdapter(this@LiveBroadcastActivity, false).apply {
                setListener(this@LiveBroadcastActivity)
            }
            recyclerViewVod?.adapter = mAdapter
        }

        var screenWidth = AppPreferences.getScreenWidth(this@LiveBroadcastActivity);
        var btnWidth = (screenWidth - Utils.ConvertDpToPx(this@LiveBroadcastActivity, 46)) / 2
        var btnHeight = (btnWidth * 61) / 157

        var l_param = buttonLive.layoutParams as LinearLayout.LayoutParams
        l_param.width = btnWidth
        l_param.height = btnHeight
        l_param.leftMargin = Utils.ConvertDpToPx(this@LiveBroadcastActivity, 15)
        l_param.rightMargin = Utils.ConvertDpToPx(this@LiveBroadcastActivity, 8)
        buttonLive.layoutParams = l_param

        var r_param = buttonVod.layoutParams as LinearLayout.LayoutParams
        r_param.width = btnWidth
        r_param.height = btnHeight
        r_param.leftMargin = Utils.ConvertDpToPx(this@LiveBroadcastActivity, 8)
        r_param.rightMargin = Utils.ConvertDpToPx(this@LiveBroadcastActivity, 15)

        buttonVod.layoutParams = r_param
        buttonBack.setOnClickListener(clickListener)
        buttonScrap.setOnClickListener(clickListener)
        buttonLatestView.setOnClickListener(clickListener)
        buttonShare.setOnClickListener(clickListener)
        buttonDaily.setOnClickListener(clickListener)
        buttonSaleInfo.setOnClickListener(clickListener)
        buttonRegister.setOnClickListener(clickListener)
//        buttonCalc.setOnClickListener(clickListener)
        buttonAllVODMore.setOnClickListener(clickListener)
        buttonRecording.setOnClickListener(clickListener)
        buttonLive.setOnClickListener(clickListener)
        buttonVod.setOnClickListener(clickListener)
        btnProfile.setOnClickListener(clickListener)
    }

    override fun onResume() {
        super.onResume()
        checkTempVOD()
        refresh()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        mActivityList.remove(this)      // 정상적인 Activity 종료인 경우는 리스트에서 제거
    }

    override fun onDozeModeStateChanged(dozeEnable: Boolean) {}
    override fun onNetworkStatusChanged(status: NetworkStatusUtils.NetworkStatus) {}

    override fun onItemClicked(item: API0.DataBeanX, position: Int) {
        // 페이징 및 dbKey 포지션 문제 때문에 앞으로 아래와 같이 List 객체를 만들어서 영상 데이터 사용 예정
        val json = Gson().toJson(mAdapter?.items)
        val videoItems : MutableList<VOD.DataBeanX> = Gson().fromJson(json, object : TypeToken<MutableList<VOD.DataBeanX>>(){}.type)
        VideoDataContainer.getInstance().mVideoData = videoItems

        if (!TextUtils.isEmpty(item.stream) && item.show_YN == "Y") {
            startActivity(Intent(this, ShoppingPlayerActivity::class.java).apply {
                putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_PLAYER_FLAG, AppConstants.MY_UPLOAD_VOD_PLAYER)
                putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_MY_VOD_POSITION, position)
                putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_CASTER_ID, item.userId)

                data = Uri.parse("vcommerce://shopping?url=${item.stream}")
            })
        } else {
            if (item != null && item.show_YN == "N") {
                AppToast(this).showToastMessage("삭제된 동영상은 재생할 수 없습니다.",
                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                        AppToast.GRAVITY_BOTTOM)
            } else {
                AppToast(this).showToastMessage("방송 정보 메타 데이터가 존재하지 않습니다.",
                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                        AppToast.GRAVITY_BOTTOM)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: NetworkBusResponse) {
        val api0 = NetworkHandler.getInstance(this).getKey(NetworkApi.API0.toString(), AppPreferences.getUserId(this)!!, "")
        val api21 = NetworkHandler.getInstance(this).getKey(NetworkApi.API21.toString(), AppPreferences.getUserId(this)!!, "")

        if (data.arg1.startsWith(api0)) {
            handleNetworkResult(data)
        } else if (data.arg1 == api21) {
            if ("ok" == data.arg2) {
                var response: API21 = Gson().fromJson(DBManager.getInstance(this).get(data.arg1), API21::class.java)

                if (!response.userIMG.isNullOrEmpty()) {
                    ImageLoad.setImage(this, btnProfile, response.userIMG, null, ImageLoad.SCALE_CIRCLE_CROP, DiskCacheStrategy.ALL)
                }
                setShareVodCount(response.userSharedCount.toInt())
            }
        }
    }

    private fun handleNetworkResult(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            val response: API0 = Gson().fromJson(DBManager.getInstance(this).get(data.arg1), API0::class.java)
            Logger.e("response:total :: ${response.nTotalCount}")

            if (response.nTotalCount > 0) {
                myTotalCount.text = "${response.nTotalCount}"
                recyclerViewVod.visibility = View.VISIBLE
                buttonAllVODMore.visibility = View.VISIBLE
                imageViewEmpty.visibility = View.GONE

                val items = ArrayList<API0.DataBeanX>()
                if (response.nTotalCount < 2) {
                    for (i in 0 until response.data.size) {
                        if (response.data[i].show_YN == "Y") {
                            items.add(response.data[i])
                        }
                    }

                    if (items.size > 0) {
                        mAdapter?.setItems(items)
                    } else {
                        recyclerViewVod.visibility = View.GONE
                        buttonAllVODMore.visibility = View.GONE
                        imageViewEmpty.visibility = View.VISIBLE
                    }
                } else {
                    for (i in 0 until response.data.size) {
                        if (response.data[i].show_YN == "Y") {
                            items.add(response.data[i])
                        }

                        if (items.size == 2) {
                            break
                        }
                    }

                    if (items.size > 0) {
                        mAdapter?.setItems(items)
                    } else {
                        recyclerViewVod.visibility = View.GONE
                        buttonAllVODMore.visibility = View.GONE
                        imageViewEmpty.visibility = View.VISIBLE
                    }
                }
            } else if(tempVODCount > 0) {
                myTotalCount.text = "1"
                recyclerViewVod.visibility = View.GONE
                buttonAllVODMore.visibility = View.VISIBLE
                imageViewEmpty.visibility = View.VISIBLE
            } else {
                myTotalCount.text = "0"
                recyclerViewVod.visibility = View.GONE
                buttonAllVODMore.visibility = View.GONE
                imageViewEmpty.visibility = View.VISIBLE
            }
        } else {
            //var errorResult: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
            Logger.e("error : ${data.arg1}, ${data.arg2}, ${data.arg3}, ${data.arg4}")

            recyclerViewVod.visibility = View.GONE
            buttonAllVODMore.visibility = View.GONE
            imageViewEmpty.visibility = View.VISIBLE
        }
    }

    private fun refresh() {
        EventBus.getDefault().post(NetworkBus(NetworkApi.API0.name))
        EventBus.getDefault().post(NetworkBus(NetworkApi.API21.name, AppPreferences.getUserId(this)))

        var task = ShopTreeAsyncTask(this)
        task.getProductCount { result, obj ->
            val data = obj as JSONObject
            if (data != null) {
                val subObject = data.optJSONObject("deliveryType")
                val scrap_cnt = subObject.optString("scrap_cnt")
                val vod_cnt = subObject.optString("vod_cnt")
                Logger.e("scrap_cnt $scrap_cnt")
                Logger.e("vod_cnt $vod_cnt")

                setScrapCount(scrap_cnt.toInt())
                setRecenViewCount(vod_cnt.toInt())
            }
        }
    }

    private fun setShareVodCount(count: Int) {
        textViewShareBadge.text = count.toString()
        textViewShareBadge.visibility = if (count > 0) View.VISIBLE else View.GONE
    }

    private fun setScrapCount(count: Int) {
        textViewScrapBadge.text = count.toString()
        textViewScrapBadge.visibility = if (count > 0) View.VISIBLE else View.GONE
    }

    private fun setRecenViewCount(count: Int) {
        textViewRecentViewBadge.text = count.toString()
        textViewRecentViewBadge.visibility = if (count > 0) View.VISIBLE else View.GONE
    }

    private fun checkTempVOD() {
        val items: MutableList<TempVOD> = ArrayList()
        VodDBManager.getInstance(this).loadAllByOrder().forEach {
            val image = it.getDictionary(VodDBManager.KEY).getBlob("thumbnailImage")
            items.add(TempVOD(
                    it.getDictionary(VodDBManager.KEY).getString("id"),
                    it.getDictionary(VodDBManager.KEY).getString("videoUrl"),
                    it.getDictionary(VodDBManager.KEY).getString("broadCastInfo"),
                    image.content,
                    it.getDictionary(VodDBManager.KEY).getString("reg_date")))
        }

        Logger.e("######### TempVOD : ${items.size}")
        if (items.size > 0) {
            isTempVOD = true
            tempVODCount = items.size
        }
    }

    private val clickListener = View.OnClickListener {
        when (it?.id) {
            R.id.buttonRegister -> {
                var browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://shop-tree.com/"))
                startActivity(browserIntent)
            }
//            R.id.buttonCalc -> {
//                var browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://puddinglive.com/bbs/login.php?url=https://puddinglive.com/shop/mypage/my_adjustment.php"))
//                startActivity(browserIntent)
//            }
            R.id.buttonBack -> finish()
            R.id.buttonScrap -> startActivity(Intent(it.context, ScrapVODActivity::class.java))
            R.id.buttonLatestView -> startActivity(Intent(it.context, LatestViewVODActivity::class.java))
            R.id.buttonShare -> startActivity(Intent(it.context, ShareVODActivity::class.java))
            R.id.buttonDaily -> {
                startActivity(Intent(it.context, StatisticsActivity::class.java).apply {
                    var title = resources.getString(R.string.msg_my_menu_statistics_daily)
                    putExtra("TITLE", title)
                    putExtra("URL", NetworkConst.STATISTICS_DAILY_URL)
                })
            }

            R.id.buttonSaleInfo -> {
                startActivity(Intent(it.context, StatisticsActivity::class.java).apply {
                    var title = resources.getString(R.string.msg_my_menu_statistics_sale_info)
                    putExtra("TITLE", title)
                    putExtra("URL", NetworkConst.STATISTICS_SALE_URL)
                })
            }

            R.id.buttonAllVODMore -> startActivity(Intent(it.context, MyVODActivity::class.java))
            R.id.buttonRecording -> {
                startActivity(Intent(this, BroadcastSettingActivity::class.java)
                        .apply { putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_CHAT_ACCOUNT, AppPreferences.getUserId(this@LiveBroadcastActivity)) }
                        .apply { putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_CHAT_ROOM_ID, "") }
                        .apply { putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_TAB, BroadcastSettingActivity.TAB_VOD) }
                        .apply { putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_CHAT_NICKNAME, AppPreferences.getUserId(this@LiveBroadcastActivity)) })
            }

            R.id.buttonLive -> {
                if ( isPermissionChecked() ) {
                    startActivity(Intent(this, BroadcastSettingActivity::class.java)
                            .apply { putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_CHAT_ACCOUNT, AppPreferences.getUserId(this@LiveBroadcastActivity)) }
                            .apply { putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_CHAT_ROOM_ID, "") }
                            .apply { putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_TAB, BroadcastSettingActivity.TAB_LIVE) }
                            .apply { putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_CHAT_NICKNAME, AppPreferences.getUserId(this@LiveBroadcastActivity)) })
                } else {
                    showPermissionDialog(true)
                }
            }

            R.id.buttonVod -> {
                if ( isPermissionChecked() ) {
                    startActivity(Intent(this, BroadcastSettingActivity::class.java)
                            .apply { putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_CHAT_ACCOUNT, AppPreferences.getUserId(this@LiveBroadcastActivity)) }
                            .apply { putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_CHAT_ROOM_ID, "") }
                            .apply { putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_TAB, BroadcastSettingActivity.TAB_VOD_UPLOAD) }
                            .apply { putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_CHAT_NICKNAME, AppPreferences.getUserId(this@LiveBroadcastActivity)) })
                } else {
                    showPermissionDialog(false)
                }
            }

            R.id.btnProfile -> {
                startActivity(Intent(this, CasterProfileActivity::class.java).apply {
                    putExtra(CasterProfileActivity.INTENT_KEY_EXTRA_USER_ID, AppPreferences.getUserId(this@LiveBroadcastActivity))
                })
            }
        }
    }
    private fun isPermissionChecked() : Boolean {
        val cameraPerm = ActivityCompat.checkSelfPermission(this@LiveBroadcastActivity, Manifest.permission.CAMERA)
        val audioPerm = ActivityCompat.checkSelfPermission(this@LiveBroadcastActivity, Manifest.permission.RECORD_AUDIO)
        val readPerm = ActivityCompat.checkSelfPermission(this@LiveBroadcastActivity, Manifest.permission.READ_EXTERNAL_STORAGE)
        val writePerm = ActivityCompat.checkSelfPermission(this@LiveBroadcastActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        return !(cameraPerm != PackageManager.PERMISSION_GRANTED
                || audioPerm != PackageManager.PERMISSION_GRANTED
                || readPerm != PackageManager.PERMISSION_GRANTED
                || writePerm != PackageManager.PERMISSION_GRANTED)
    }

    private fun showPermissionDialog(isLive: Boolean) {
        val cameraPerm = ActivityCompat.checkSelfPermission(this@LiveBroadcastActivity, Manifest.permission.CAMERA)
        val audioPerm = ActivityCompat.checkSelfPermission(this@LiveBroadcastActivity, Manifest.permission.RECORD_AUDIO)
        val readPerm = ActivityCompat.checkSelfPermission(this@LiveBroadcastActivity, Manifest.permission.READ_EXTERNAL_STORAGE)
        val writePerm = ActivityCompat.checkSelfPermission(this@LiveBroadcastActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (cameraPerm != PackageManager.PERMISSION_GRANTED
                || audioPerm != PackageManager.PERMISSION_GRANTED
                || readPerm != PackageManager.PERMISSION_GRANTED
                || writePerm != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                Toast.makeText(this@LiveBroadcastActivity, "No CAMERA or AudioRecord permission, please check", Toast.LENGTH_LONG).show()
            } else {
                val `object` = PermissionObject()
                val array = ArrayList<PermissionObject.Objects>()
                var s_object: PermissionObject.Objects
                requestPermissionArray = ArrayList<String>()

                if (cameraPerm != PackageManager.PERMISSION_GRANTED) {
                    s_object = PermissionObject.Objects()
                    s_object.image = R.drawable.authority_camera_ico
                    s_object.title = "카메라 (필수)"
                    s_object.content = "푸딩에서 라이브 방송을 하기 위한 필수 기능 입니다."
                    array.add(s_object)
                    requestPermissionArray!!.add(Manifest.permission.CAMERA)
                }

                if (audioPerm != PackageManager.PERMISSION_GRANTED) {
                    s_object = PermissionObject.Objects()
                    s_object.image = R.drawable.authority_mic_ico
                    s_object.title = "마이크 (필수)"
                    s_object.content = "푸딩에서 라이브 방송을 하기 위한 필수 기능 입니다."
                    array.add(s_object)
                    requestPermissionArray!!.add(Manifest.permission.RECORD_AUDIO)
                }

                if (readPerm != PackageManager.PERMISSION_GRANTED) {
                    s_object = PermissionObject.Objects()
                    s_object.image = R.drawable.authority_pic_ico
                    s_object.title = "사진/미디어/파일 (필수)"
                    s_object.content = "푸딩에서 라이브 방송을 하기 위한 필수 기능 입니다."
                    array.add(s_object)
                    requestPermissionArray!!.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                    requestPermissionArray!!.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }

                `object`.title = "푸딩 앱을 즐기기 위해\n다음의 앱 권한을 허용해주세요."
                `object`.objectArray = array

                dialog = MainPermissionDialog(`object`)
                dialog!!.setListener(object : MainPermissionDialog.DialogClickListener {
                    override fun onDialogClick() {
                        cTime = System.currentTimeMillis()
                        rationalArray = java.util.ArrayList()
                        var onlyCameraDenied = NONE_VALUE
                        var onlyRecordAudioDenied = NONE_VALUE
                        var onlyReadExternalStorageDenied = NONE_VALUE
                        var onlyWriteExternalStorageDenied = NONE_VALUE

                        if (cameraPerm != PackageManager.PERMISSION_GRANTED) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(this@LiveBroadcastActivity, Manifest.permission.CAMERA)) {
                                // 사용자가 다시 보지 않기에 체크하지 않고 권한 설정을 거절한 이력이 있을 경우
                                onlyCameraDenied = TRUE_VALUE
                            } else {
                                // 사용자가 다시 보지 않기에 체크하고 권한 설정을 거절한 이력이 있을 경우
                                onlyCameraDenied = FALSE_VALUE
                            }
                        }
                        rationalArray!!.add(INDEX_CAMERA, onlyCameraDenied)

                        if (audioPerm != PackageManager.PERMISSION_GRANTED) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(this@LiveBroadcastActivity, Manifest.permission.RECORD_AUDIO)) {
                                // 사용자가 다시 보지 않기에 체크하지 않고 권한 설정을 거절한 이력이 있을 경우
                                onlyRecordAudioDenied = TRUE_VALUE
                            } else {
                                // 사용자가 다시 보지 않기에 체크하고 권한 설정을 거절한 이력이 있을 경우
                                onlyRecordAudioDenied = FALSE_VALUE
                            }
                        }
                        rationalArray!!.add(INDEX_RECORD_AUDIO, onlyRecordAudioDenied)

                        if (readPerm != PackageManager.PERMISSION_GRANTED) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(this@LiveBroadcastActivity, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                                // 사용자가 다시 보지 않기에 체크하지 않고 권한 설정을 거절한 이력이 있을 경우
                                onlyReadExternalStorageDenied = TRUE_VALUE
                            } else {
                                // 사용자가 다시 보지 않기에 체크하고 권한 설정을 거절한 이력이 있을 경우
                                onlyReadExternalStorageDenied = FALSE_VALUE
                            }

                        }
                        rationalArray!!.add(INDEX_READ_EXTERNAL_STORAGE, onlyReadExternalStorageDenied)

                        if (writePerm != PackageManager.PERMISSION_GRANTED) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(this@LiveBroadcastActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                // 사용자가 다시 보지 않기에 체크하지 않고 권한 설정을 거절한 이력이 있을 경우
                                onlyWriteExternalStorageDenied = TRUE_VALUE
                            } else {
                                // 사용자가 다시 보지 않기에 체크하고 권한 설정을 거절한 이력이 있을 경우
                                onlyWriteExternalStorageDenied = FALSE_VALUE
                            }
                        }
                        rationalArray!!.add(INDEX_WRITE_EXTERNAL_STORAGE, onlyWriteExternalStorageDenied)
                        for (i in rationalArray!!.indices) {
                            Logger.e("rationalArray value :: " + rationalArray!!.get(i))
                        }
                        if (requestPermissionArray != null) {
                            val permissionArray = requestPermissionArray!!.toTypedArray()
                            for (j in permissionArray.indices)
                                Logger.e("permissionArray value :: " + permissionArray[j])
                            if ( isLive )
                                ActivityCompat.requestPermissions(this@LiveBroadcastActivity, permissionArray, PERMISSION_REQUEST_LIVE)
                            else
                                ActivityCompat.requestPermissions(this@LiveBroadcastActivity, permissionArray, PERMISSION_REQUEST_VOD)
                        }
                    }

                    override fun onDismissed() {

                    }
                })
                dialog!!.show(supportFragmentManager, "")
            }
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_REQUEST_LIVE || requestCode == PERMISSION_REQUEST_VOD) {
            Handler().postDelayed({
                Logger.e("onRequestPermissionsResult  PERMISSION_REQUEST_CAMERA_AUDIO_REC called ")
                val now = System.currentTimeMillis()
                val gap = now - cTime
                var result_t_count = 0
                var isAllGranted = true
                for (i in grantResults.indices) {
                    if (grantResults[i] == 0) {
                        result_t_count++
                    } else {
                        isAllGranted = false
                    }
                }
                Logger.e("isAllGranted :: $isAllGranted")
                if (isAllGranted) {
                    // 모든 권한 허용됨
                    if (dialog != null)
                        dialog!!.dismiss()
                    if (requestCode == PERMISSION_REQUEST_LIVE ) {
                        if (AppPreferences.getLoginStatus(this)) {
                            startActivity(Intent(this, BroadcastSettingActivity::class.java)
                                    .apply { putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_CHAT_ACCOUNT, AppPreferences.getUserId(this@LiveBroadcastActivity)) }
                                    .apply { putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_CHAT_ROOM_ID, "") }
                                    .apply { putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_TAB, BroadcastSettingActivity.TAB_LIVE) }
                                    .apply { putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_CHAT_NICKNAME, AppPreferences.getUserId(this@LiveBroadcastActivity)) })
                        } else {
                            startActivityForResult(Intent(this, LoginActivity::class.java), ACTIVITY_REQUEST_CODE_LIVE)
                        }
                    } else {
                        if (AppPreferences.getLoginStatus(this)) {
                            startActivity(Intent(this, BroadcastSettingActivity::class.java)
                                    .apply { putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_CHAT_ACCOUNT, AppPreferences.getUserId(this@LiveBroadcastActivity)) }
                                    .apply { putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_CHAT_ROOM_ID, "") }
                                    .apply { putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_TAB, BroadcastSettingActivity.TAB_VOD_UPLOAD) }
                                    .apply { putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_CHAT_NICKNAME, AppPreferences.getUserId(this@LiveBroadcastActivity)) })
                        } else {
                            startActivityForResult(Intent(this, LoginActivity::class.java), ACTIVITY_REQUEST_CODE_VOD)
                        }
                    }
                } else {
                    if (rationalArray != null) {
                        val prevCamera = rationalArray!!.get(INDEX_CAMERA)
                        val prevRecordAudio = rationalArray!!.get(INDEX_RECORD_AUDIO)
                        val prevReadExternalStorage = rationalArray!!.get(INDEX_READ_EXTERNAL_STORAGE)
                        val prevWriteExternalStorage = rationalArray!!.get(INDEX_WRITE_EXTERNAL_STORAGE)
                        if (TRUE_VALUE == prevCamera && TRUE_VALUE == prevRecordAudio && TRUE_VALUE == prevReadExternalStorage && TRUE_VALUE == prevWriteExternalStorage) {
                            // 모두 한번씩 봤었던 권한들인데 전부 설정을 안한 경우 사용자가 승인 하지 않은 것으로 봄
                            if (dialog != null)
                                dialog!!.dismiss()
                        } else {
                            var false_cnt = 0
                            var true_cnt = 0
                            for (i in rationalArray!!.indices) {
                                if (TRUE_VALUE == rationalArray!!.get(i)) {
                                    true_cnt++
                                } else if (FALSE_VALUE == rationalArray!!.get(i)) {
                                    false_cnt++
                                }
                            }

                            // 설정 이전에 권한이 true false가 섞여 있을 경우
                            // 즉, 사용자가 첫 권한 설정이 아니면서 처음에 다시보지 않기를 한 항목이 있을 경우
                            if (false_cnt > 0 && true_cnt > 0) {
                                if (true_cnt == result_t_count) {
                                    // 설정으로 넘김, 사용자가 이전에 설정 안한 갯수(다시보지않기 제외) 권한설정 갯수가 같으면 다시보지 않기 제외하고는 모두 권한 설정했다는 뜻이므로 다시보지않기 했던 권한 설정할 수 있도록
                                    goSetting()
                                } else {
                                    // dialog 닫고 화면 닫고, 사용자가 이전 설정 안한것들 중 현재 다시 설정을 안한 값이 있다는 뜻이므로
                                    if (dialog != null)
                                        dialog!!.dismiss()
                                }
                            } else if (false_cnt > 0 && true_cnt == 0) {
                                if (gap < 1000) {
                                    // 설정으로 넘김, 첫 설정이 모두 false 인데 1초이내 결과가 모두 넘어온 경우 즉, 사용자가 이전에 모두 다시보지 않기 눌렀을 경우
                                    goSetting()
                                } else {
                                    // dialog 닫고 화면 닫고, 첫설정이 모두 false인데 1초 이후 들어온 경우이므로 첫 설정 다이얼로그 떴음, 그런데 결과가 모두 true가 아니므로 사용자가 권한 사용 안하겠다는거.
                                    finish()
                                }
                            } else if (false_cnt == 0 && true_cnt > 0) {
                                if (dialog != null)
                                    dialog!!.dismiss()
                                // dialog 닫고 화면 닫고, 사용자가 이전에 모두 권한 사용 안함 눌렀는데 결과가 모두 true가 아닌 경우 즉. 사용자가 권한 사용 안하겠다는 의미
                            } else {
                                goSetting()
                                // 설정으로 넘김
                            }
                        }
                    }
                }
            }, 100)
        }
    }
    private fun goSetting() {
        val i = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID))
        startActivity(i)
    }
}