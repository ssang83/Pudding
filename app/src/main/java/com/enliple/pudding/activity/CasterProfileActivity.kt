package com.enliple.pudding.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.TextUtils
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.enliple.pudding.AbsBaseActivity
import com.enliple.pudding.R
import com.enliple.pudding.adapter.home.CasterTabPagerAdapter
import com.enliple.pudding.commons.app.ImageLoad
import com.enliple.pudding.commons.app.NetworkStatusUtils
import com.enliple.pudding.commons.app.StringUtils
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.events.OnSingleClickListener
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.*
import com.enliple.pudding.commons.network.vo.API21
import com.enliple.pudding.commons.network.vo.API54
import com.enliple.pudding.commons.network.vo.API78
import com.enliple.pudding.commons.network.vo.BaseAPI
import com.enliple.pudding.commons.ui_compat.PixelUtil
import com.enliple.pudding.commons.widget.tab_layout.WrappedTabLayoutStripUtil
import com.enliple.pudding.commons.widget.toast.AppToast
import com.enliple.pudding.widget.FollowCancelDialog
import com.enliple.pudding.widget.MessageBlockDialog
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_caster_profile.*
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

/**
 * Created by Kim Joonsung on 2018-11-26.
 */
class CasterProfileActivity : AbsBaseActivity() {

    companion object {
        const val INTENT_KEY_EXTRA_USER_ID = "user_id"

        private const val REQUEST_LOGIN_FOR_MORE    = 0x0001
        private const val REQUEST_LOGIN_FOR_FOLLOW  = 0x0002
        private const val REQUEST_LOGIN_FOR_MESSAGE = 0x0003
    }

    private var pagerAdapter: CasterTabPagerAdapter? = null
    private var userId = ""
    private var nickName = ""
    private var profileImg = ""
    private var isFollow = false
    private var followCnt = 0
    private var casterId = ""
    private var isUserBlock = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_caster_profile)
        EventBus.getDefault().register(this)

        viewPagerContainer.setSwipeEnable(false)

        layoutMore.setOnClickListener(clickListener)
        buttonMore.setOnClickListener(clickListener)
        buttonFollow.setOnClickListener(clickListener)
        buttonMessage.setOnClickListener(clickListener)

        textViewReport.setOnClickListener(clickListener)
        textViewBroadcastBlock.setOnClickListener(clickListener)
        textViewAlarmSet.setOnClickListener(clickListener)

        layoutFollower.setOnClickListener(clickListener)
        layoutFollowing.setOnClickListener(clickListener)

        frame.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                return true
            }
        })

        if (intent != null) {
            userId = intent.getStringExtra(INTENT_KEY_EXTRA_USER_ID)
        }

        if(userId == AppPreferences.getUserId(this)) {
            buttonFollow.visibility = View.GONE
            buttonMessage.visibility = View.GONE
            buttonMore.visibility = View.GONE
        } else {
            buttonFollow.visibility = View.VISIBLE
            buttonMessage.visibility = View.VISIBLE
            buttonMore.visibility = View.VISIBLE
        }

        initTab(userId)

//        var bus = NetworkBus(NetworkApi.API21.name, userId)
//        EventBus.getDefault().post(bus)
    }

    override fun onResume() {
        super.onResume()
        var bus = NetworkBus(NetworkApi.API21.name, userId)
        EventBus.getDefault().post(bus)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        layoutMore.visibility = View.GONE
        EventBus.getDefault().unregister(this)
    }

    override fun onDozeModeStateChanged(dozeEnable: Boolean) {}

    override fun onNetworkStatusChanged(status: NetworkStatusUtils.NetworkStatus) {}

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode == Activity.RESULT_OK) {
            if(requestCode == REQUEST_LOGIN_FOR_MORE) {
                if (layoutMore.visibility == View.VISIBLE) {
                    layoutMore.visibility = View.GONE
                } else {
                    layoutMore.visibility = View.VISIBLE
                    layoutMore.bringToFront()
                }
            } else if(requestCode == REQUEST_LOGIN_FOR_MESSAGE) {
                startActivity(Intent(this, MyChatActivity::class.java).apply {
                    putExtra(MyChatActivity.INTENT_EXTRA_KEY_CASTER_NICKNAME, nickName)
                    putExtra(MyChatActivity.INTENT_EXTRA_KEY_CASTER_PROFILE, profileImg)
                    putExtra(MyChatActivity.INTENT_EXTRA_KEY_CASTER_ID, casterId)
                    putExtra(MyChatActivity.INTENT_EXTRA_KEY_CASTER_FOLLOW, isFollow)
                })
            } else if(requestCode == REQUEST_LOGIN_FOR_FOLLOW) {
                JSONObject().apply {
                    put("strUserId", AppPreferences.getUserId(this@CasterProfileActivity)!!)
                    put("strToUserId", casterId)
                    put("isFollow", if(isFollow) "N" else "Y")
                }.let {
                    val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), it.toString())
                    EventBus.getDefault().post(NetworkBus(NetworkApi.API2.name, body))
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: NetworkBusResponse) {
        var key = NetworkHandler.getInstance(this@CasterProfileActivity).getKey(NetworkApi.API21.toString(), userId, "")
        val API64 = NetworkHandler.getInstance(this@CasterProfileActivity).getKey(NetworkApi.API64.toString(), AppPreferences.getUserId(this)!!, "")
        Logger.e("data.arg1 :: " + data.arg1)
        when (data.arg1) {
            key -> handleNetworkResultUserInfo(data)
            API64 -> handleNetworkAPI64(data)
            NetworkApi.API57.toString() -> handleNetworkAPI57(data)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: NetworkBusFastResponse) {
        if(data.arg1 == NetworkApi.API2.toString()) {
            if ("ok" == data.arg2) {
                toggleFollow()
                if (isFollow) {
                    followCnt++
                    setFollowerCount(followCnt)
                    imageViewFollow.setBackgroundResource(R.drawable.following_ico)
                    AppToast(this@CasterProfileActivity).showToastMessage("${casterId}님을 팔로우 하였습니다.",
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_BOTTOM)
                } else {
                    followCnt--
                    setFollowerCount(followCnt)
                    imageViewFollow.setBackgroundResource(R.drawable.follow_ico)
                }
            }
        }
    }

    private fun initTab(userId: String) {
        if (pagerAdapter == null) {
            tabLayoutMy.addTab(tabLayoutMy.newTab().setText(getString(R.string.msg_caster_tab_video)))
            tabLayoutMy.addTab(tabLayoutMy.newTab().setText(getString(R.string.msg_caster_tab_product)))
        }

        pagerAdapter = CasterTabPagerAdapter(viewPagerContainer, supportFragmentManager, userId)
        tabLayoutMy.addOnTabSelectedListener(tabSelectedListener)

        // TabStrip Size 축소
        WrappedTabLayoutStripUtil.wrapTabIndicatorToTitle(tabLayoutMy,
                PixelUtil.dpToPx(this@CasterProfileActivity, 60),
                PixelUtil.dpToPx(this@CasterProfileActivity, 60))

        tabLayoutMy.getTabAt(0)?.select()

        layoutMore.visibility = View.GONE
        setupTabLayoutFonts()
    }

    /**
     * 사용자 정보 설정
     */
    private fun handleNetworkResultUserInfo(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            var str = DBManager.getInstance(this@CasterProfileActivity).get(data.arg1)
            var response: API21 = Gson().fromJson(str, API21::class.java)

            initData(response)
        } else {
            var errorResult: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
            Logger.e("error : $errorResult")
        }
    }

    private fun handleNetworkAPI64(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            Logger.e("clk handleNetworkAPI64 OK")
            layoutMore.visibility = View.GONE
            AppToast(this).showToastMessage("영상 알람이 설정되었습니다.",
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM)
        } else {
            Logger.e("clk handleNetworkAPI64 NOT OK")
            var errorResult: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
            Logger.e("error : $errorResult")

            AppToast(this).showToastMessage(errorResult.message,
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM)
        }
    }

    private fun handleNetworkAPI57(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            layoutMore.visibility = View.GONE

            if(isUserBlock == "N") {
                isUserBlock = "Y"

                AppToast(this).showToastMessage("차단하였습니다.",
                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                        AppToast.GRAVITY_BOTTOM)

                textViewBroadcastBlock.text = getString(R.string.msg_my_message_blcok_cancel)

            } else {
                isUserBlock = "N"

                AppToast(this).showToastMessage("차단해제 하였습니다.",
                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                        AppToast.GRAVITY_BOTTOM)

                textViewBroadcastBlock.text = getString(R.string.msg_broadcast_block)
            }
        } else {
            var errorResult: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
            Logger.e("error : $errorResult")

            AppToast(this).showToastMessage(errorResult.message,
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM)
        }
    }

    /**
     * 사용자 프로필을 설정한다.
     */
    private fun initData(response: API21) {
        if(TextUtils.isEmpty(response.userIMG)) {
            imageViewProfile.setBackgroundResource(R.drawable.profile_default_img)
        } else {
            ImageLoad.setImage(
                    this@CasterProfileActivity,
                    imageViewProfile,
                    response.userIMG,
                    null,
                    ImageLoad.SCALE_CIRCLE_CROP,
                    DiskCacheStrategy.ALL)
        }

        if(TextUtils.isEmpty(response.userIMG_bg)) {
            imageViewBG.setBackgroundResource(R.drawable.profile_default_bg_img)
        } else {
            ImageLoad.setImage(
                    this@CasterProfileActivity,
                    imageViewBG,
                    response.userIMG,
                    null,
                    ImageLoad.SCALE_NONE,
                    DiskCacheStrategy.ALL)
        }

        setNickName(response.userNickname)
//        setVideoCount(response.vodTotalCount.toInt())
        followCnt = response.userFollower.toInt()
        setFollowerCount(followCnt)
        setFollowingCount(response.userFollowing.toInt())
        setLikeCount(response.userFavorCount.toInt())
        setIntroduceSelf(response.userProfile)

        if (response.followingCheck) {
            imageViewFollow.setBackgroundResource(R.drawable.following_ico)
        } else {
            imageViewFollow.setBackgroundResource(R.drawable.follow_ico)
        }

        isFollow = response.followingCheck
        casterId = response.userId
        Logger.e("casterId :: " + casterId)
        nickName = response.userNickname
        profileImg = response.userIMG

        isUserBlock = response.isUserBlock
        textViewBroadcastBlock.text = if(isUserBlock == "Y") getString(R.string.msg_my_message_blcok_cancel) else getString(R.string.msg_broadcast_block)
    }

    /**
     * 동영상 갯수 텍스트를 설정
     * @param count
     */
//    private fun setVideoCount(count: Int) {
//        textViewVideoCount.text = StringUtils.getSnsStyleCount(count)
//    }

    /**
     * 팔로워 갯수 텍스트를 설정
     * @param count
     */
    private fun setFollowerCount(count: Int) {
        textViewFollowerCount.text = StringUtils.getSnsStyleCountZeroBase(count)
    }

    /**
     * 팔로잉 갯수 텍스트를 설정
     * @param count
     */
    private fun setFollowingCount(count: Int) {
        textViewFollowingCount.text = StringUtils.getSnsStyleCountZeroBase(count)
    }

    /**
     * 좋아요 갯수 텍스트를 설정
     * @param count
     */
    private fun setLikeCount(count: Int) {
        textViewLikeCount.text = StringUtils.getSnsStyleCountZeroBase(count)
    }

    /**
     * 자기소개 글 텍스트 설정
     */
    private fun setIntroduceSelf(profile : String) {
        if(profile.isNotEmpty()) {
            layoutIntroduce.visibility = View.VISIBLE
            textViewIntroduce.text = profile
        } else {
            layoutIntroduce.visibility = View.GONE
        }
    }

    /**
     * 프로필 UI 상에 닉네임 텍스트를 설정
     * @param nickName
     */
    private fun setNickName(nickName: String?) {
        if (!TextUtils.isEmpty(nickName)) {
            textViewNickName.text = nickName
            textViewNickName.visibility = View.VISIBLE
        } else {
            textViewNickName.visibility = View.INVISIBLE
        }
    }

    private fun setupTabLayoutFonts() {
        val tabPosition = tabLayoutMy.selectedTabPosition
        val vg = tabLayoutMy.getChildAt(0) as ViewGroup
        val tabCnt = vg.childCount
        for (i in 0 until tabCnt) {
            val vgTab = vg.getChildAt(i) as ViewGroup
            val tabChildCnt = vgTab.childCount
            for (j in 0 until tabChildCnt) {
                val tabViewChild = vgTab.getChildAt(j)
                if (tabViewChild is AppCompatTextView) {
                    if (i == tabPosition) {
                        tabViewChild.typeface = Typeface.createFromAsset(assets, "fonts/notosanskr_bold.otf")
                    } else {
                        tabViewChild.typeface = Typeface.createFromAsset(assets, "fonts/notosanskr_regular.otf")
                    }
                }
            }
        }
    }


    private val tabSelectedListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabReselected(tab: TabLayout.Tab?) {}

        override fun onTabUnselected(tab: TabLayout.Tab?) {}

        override fun onTabSelected(tab: TabLayout.Tab?) {
            setupTabLayoutFonts()
            if (tab != null) {
                viewPagerContainer.currentItem = tab?.position
            }
        }
    }

    private val clickListener = object : OnSingleClickListener() {
        override fun onSingleClick(v: View?) {
            Logger.e("clk onSingleClick")
            when (v?.id) {
                R.id.buttonClose -> {
                    onBackPressed()
                }

                R.id.layoutMore -> {
                    Logger.e("clk layoutMore clicked")
                    layoutMore.visibility = View.GONE
                }

                R.id.buttonMore -> {
                    if(AppPreferences.getLoginStatus(this@CasterProfileActivity)) {
                        if (layoutMore.visibility == View.VISIBLE) {
                            layoutMore.visibility = View.GONE
                        } else {
                            layoutMore.visibility = View.VISIBLE
                            layoutMore.bringToFront()
                        }
                    } else {
                        startActivityForResult(Intent(this@CasterProfileActivity, LoginActivity::class.java), REQUEST_LOGIN_FOR_MORE)
                    }
                }

                R.id.buttonFollow -> {
                    if(AppPreferences.getLoginStatus(this@CasterProfileActivity)) {
                        var userId = AppPreferences.getUserId(this@CasterProfileActivity)
                        if ( userId != casterId ) { // 자기 자신은 팔로우 할 수 없도록 함.
                            var followTemp = "N"
                            if (isFollow)
                                followTemp = "N"
                            else
                                followTemp = "Y"

                            Logger.e("userId :: ${userId}")
                            Logger.e("casterId :: ${casterId}")
                            if (TextUtils.isEmpty(userId))
                                userId = ""

                            if(followTemp == "N") {
                                val item = API78.FollowItem().apply {
                                    strToUserId = casterId
                                    strThumbnail = profileImg
                                }

                                FollowCancelDialog(this@CasterProfileActivity, item, "", object : FollowCancelDialog.Listener {
                                    override fun onDismiss() {
                                        var map = HashMap<String, String>()
                                        map.put("strUserId", userId!!)
                                        map.put("strToUserId", casterId)
                                        map.put("isFollow", "N")
                                        var body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), (JSONObject(map)).toString())
                                        Logger.e("body :: $body")
                                        var bus = NetworkBus(NetworkApi.API2.name, body)
                                        EventBus.getDefault().post(bus)
                                    }
                                }).show()
                            } else {
                                var map = HashMap<String, String>()
                                map.put("strUserId", userId!!)
                                map.put("strToUserId", casterId)
                                map.put("isFollow", "Y")
                                var body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), (JSONObject(map)).toString())
                                Logger.e("body :: $body")
                                var bus = NetworkBus(NetworkApi.API2.name, body)
                                EventBus.getDefault().post(bus)
                            }
                        } else {
                            AppToast(this@CasterProfileActivity).showToastMessage("자기 자신은 팔로우 할 수 없습니다.",
                                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                                    AppToast.GRAVITY_BOTTOM)
                        }
                    } else {
                        startActivityForResult(Intent(this@CasterProfileActivity, LoginActivity::class.java), REQUEST_LOGIN_FOR_FOLLOW)
                    }
                }

                R.id.buttonMessage -> {
                    var userId = AppPreferences.getUserId(this@CasterProfileActivity)
                    if ( userId != casterId ) { // 자기 자신은 팔로우 할 수 없도록 함.
                        if(AppPreferences.getLoginStatus(this@CasterProfileActivity)) {
                            startActivity(Intent(this@CasterProfileActivity, MyChatActivity::class.java).apply {
                                putExtra(MyChatActivity.INTENT_EXTRA_KEY_CASTER_NICKNAME, nickName)
                                putExtra(MyChatActivity.INTENT_EXTRA_KEY_CASTER_PROFILE, profileImg)
                                putExtra(MyChatActivity.INTENT_EXTRA_KEY_CASTER_ID, casterId)
                                putExtra(MyChatActivity.INTENT_EXTRA_KEY_CASTER_FOLLOW, isFollow)
                            })
                        } else {
                            startActivityForResult(Intent(this@CasterProfileActivity, LoginActivity::class.java), REQUEST_LOGIN_FOR_MESSAGE)
                        }
                    }
                }

                R.id.textViewReport -> {
                    layoutMore.visibility = View.GONE
                    startActivity(Intent(v!!.context, BroadcastReportActivity::class.java).apply {
                        putExtra(BroadcastReportActivity.INTENT_EXTRA_KEY_REPORT_TYPE, true)
                        putExtra(BroadcastReportActivity.INTENT_EXTRA_KEY_USER_ID, userId)
                    })
                }

                R.id.textViewBroadcastBlock -> {
                    layoutMore.visibility = View.GONE

                    if(isUserBlock == "N") {
                        val item = API54.MessageItem().apply {
                            send_mb_id = casterId
                            send_mb_img = profileImg
                            send_mb_nick = nickName
                        }

                        val dialog = MessageBlockDialog(this@CasterProfileActivity, item, object : MessageBlockDialog.Listener {
                            override fun onDismiss() {
                                Logger.e("onDismiss")
                            }

                            override fun onBlock() {
                                Logger.e("onBlock")
                                val requestBody = JSONObject().apply {
                                    put("user", AppPreferences.getUserId(this@CasterProfileActivity))
                                    put("block_user", userId)
                                    put("status", "Y")
                                }

                                val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), requestBody.toString())
                                val bus = NetworkBus(NetworkApi.API57.name, body)
                                EventBus.getDefault().post(bus)
                            }
                        })

                        dialog!!.show()

                    } else {
                        val requestBody = JSONObject().apply {
                            put("user", AppPreferences.getUserId(this@CasterProfileActivity))
                            put("block_user", userId)
                            put("status", "N")
                        }

                        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), requestBody.toString())
                        val bus = NetworkBus(NetworkApi.API57.name, body)
                        EventBus.getDefault().post(bus)
                    }
                }

                R.id.textViewAlarmSet -> {
                    Logger.e("clk textViewAlarmSet CLICKED")
                    layoutMore.visibility = View.GONE
                    val obj = JSONObject().apply {
                        put("type", "live")
                        put("val", "Y")
                        put("user", AppPreferences.getUserId(this@CasterProfileActivity)!!)
                        put("follow_user", userId)
                    }

                    val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), obj.toString())
                    val bus = NetworkBus(NetworkApi.API64.name, body)
                    EventBus.getDefault().post(bus)
                }

                R.id.layoutFollower -> {
                    startActivity(Intent(v!!.context, FollowerActivity::class.java).apply {
                        putExtra(FollowerActivity.INTENT_EXTRA_KEY_IS_FOLLOWER, true)
                        putExtra(FollowerActivity.INTENT_EXTRA_KEY_USERID, userId)
                    })
                }

                R.id.layoutFollowing -> {
                    startActivity(Intent(v!!.context, FollowerActivity::class.java).apply {
                        putExtra(FollowerActivity.INTENT_EXTRA_KEY_IS_FOLLOWER, false)
                        putExtra(FollowerActivity.INTENT_EXTRA_KEY_USERID, userId)
                    })
                }
            }
        }
    }

    private fun toggleFollow() {
        isFollow = !isFollow
    }
}