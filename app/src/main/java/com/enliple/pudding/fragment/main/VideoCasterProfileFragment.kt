package com.enliple.pudding.fragment.main

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.enliple.pudding.R
import com.enliple.pudding.activity.BroadcastReportActivity
import com.enliple.pudding.activity.FollowerActivity
import com.enliple.pudding.activity.LoginActivity
import com.enliple.pudding.activity.MyChatActivity
import com.enliple.pudding.adapter.home.CasterTabPagerAdapter
import com.enliple.pudding.commons.app.ImageLoad
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
import com.enliple.pudding.shoppingplayer.CasterTab
import com.enliple.pudding.widget.FollowCancelDialog
import com.enliple.pudding.widget.MessageBlockDialog
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_video_caster_profile.*
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

/**
 * 영상 시청간 방송자 프로필을 표시하는 Fragment
 * @author hkcha
 * @since 2018.08.30
 */
class VideoCasterProfileFragment : androidx.fragment.app.Fragment() {

    companion object {
        private const val REQUEST_LOGIN_FOR_MORE = 0x0001
        private const val REQUEST_LOGIN_FOR_FOLLOW = 0x0002
        private const val REQUEST_LOGIN_FOR_MESSAGE = 0x0003
    }

    private var pagerAdapter: CasterTabPagerAdapter? = null
    private var mUserId = ""
    private var nickName = ""
    private var profileImg = ""
    private var isFollow = false
    private var casterId = ""
    private var followCnt = 0
    private var isFollowRefresh = false
    private var isOpen = false
    private var isUserBlock = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_video_caster_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Logger.e("onViewCreated")

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
        buttonBack.setOnClickListener(clickListener)
    }

    override fun onDestroy() {
        super.onDestroy()

        layoutMore?.visibility = View.GONE

        EventBus.getDefault().unregister(this)
    }

    fun loadData(userId: String) {
        Logger.c("loadData $userId")

        if (AppPreferences.getUserId(context!!)!! == userId) {
            buttonFollow.visibility = View.GONE
            buttonMessage.visibility = View.GONE
            buttonMore.visibility = View.GONE
        } else {
            buttonFollow.visibility = View.VISIBLE
            buttonMessage.visibility = View.VISIBLE
            buttonMore.visibility = View.VISIBLE
        }

        isOpen = true
        EventBus.getDefault().post(NetworkBus(NetworkApi.API21.name, userId))

        initTab(userId)

        mUserId = userId
    }

    fun init() {
        Logger.c("init")

        layoutMore?.visibility = View.GONE

        textViewNickName?.text = ""
        textViewFollowerCount?.text = ""
        textViewFollowingCount?.text = ""
        textViewLikeCount?.text = ""
        textViewIntroduce?.text = ""
        imageViewProfile?.setImageResource(0)
        imageViewBG?.setImageResource(0)

        val fragment = pagerAdapter!!.instantiateItem(viewPagerContainer, CasterTab.VIDEO.ordinal) as Fragment
        if (fragment != null) {
            (fragment as CasterVODFragment).clearTotalCount()
            fragment.clearData()
        }

        isOpen = false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_LOGIN_FOR_MORE) {
                if (layoutMore.visibility == View.VISIBLE) {
                    layoutMore.visibility = View.GONE
                } else {
                    layoutMore.visibility = View.VISIBLE
                    layoutMore.bringToFront()
                }
            } else if (requestCode == REQUEST_LOGIN_FOR_MESSAGE) {
                startActivity(Intent(context!!, MyChatActivity::class.java).apply {
                    putExtra(MyChatActivity.INTENT_EXTRA_KEY_CASTER_NICKNAME, nickName)
                    putExtra(MyChatActivity.INTENT_EXTRA_KEY_CASTER_PROFILE, profileImg)
                    putExtra(MyChatActivity.INTENT_EXTRA_KEY_CASTER_ID, casterId)
                    putExtra(MyChatActivity.INTENT_EXTRA_KEY_CASTER_FOLLOW, isFollow)
                })
            } else if (requestCode == REQUEST_LOGIN_FOR_FOLLOW) {
                JSONObject().apply {
                    put("strUserId", AppPreferences.getUserId(context!!)!!)
                    put("strToUserId", casterId)
                    put("isFollow", if (isFollow) "N" else "Y")
                }.let {
                    val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), it.toString())
                    EventBus.getDefault().post(NetworkBus(NetworkApi.API2.name, body))
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: NetworkBusFastResponse) {
        if (data.arg1 == NetworkApi.API2.toString()) {
            Logger.e("data.arg2 :: ${data.arg2}")
            if ("ok" == data.arg2) {
                toggleFollow()
                if (isFollow) {
                    followCnt++
                    setFollowerCount(followCnt)
                    imageViewFollow.setBackgroundResource(R.drawable.following_ico)
                    AppToast(context!!).showToastMessage("${casterId}님을 팔로우 하였습니다.",
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: NetworkBusResponse) {
        var key = NetworkHandler.getInstance(context!!).getKey(NetworkApi.API21.toString(), mUserId, "")
        val api64 = NetworkHandler.getInstance(context!!).getKey(NetworkApi.API64.toString(), AppPreferences.getUserId(context!!)!!, "")
        if (data.arg1 == key) {
            if (isOpen) {
                if (isFollowRefresh) {
                    var str = DBManager.getInstance(context!!).get(data.arg1)
                    var response: API21 = Gson().fromJson(str, API21::class.java)
                    initData(response)
                } else {
                    handleNetworkResultUserInfo(data)
                }
                isFollowRefresh = false
            }
        } else if (data.arg1 == NetworkApi.API57.toString()) {
            if ("ok" == data.arg2) {
                layoutMore.visibility = View.GONE

                if(isUserBlock == "N") {
                    isUserBlock = "Y"

                    AppToast(view!!.context).showToastMessage("차단하였습니다.",
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_BOTTOM)

                    textViewBroadcastBlock.text = getString(R.string.msg_my_message_blcok_cancel)

                } else {
                    isUserBlock = "N"

                    AppToast(view!!.context).showToastMessage("차단해제 하였습니다.",
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_BOTTOM)

                    textViewBroadcastBlock.text = getString(R.string.msg_broadcast_block)
                }
            } else {
                var errorResult: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
                Logger.e("error : $errorResult")

                AppToast(view!!.context).showToastMessage(errorResult.message,
                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                        AppToast.GRAVITY_BOTTOM)
            }
        } else if (data.arg1 == api64) {
            if ("ok" == data.arg2) {
                layoutMore.visibility = View.GONE
                AppToast(view!!.context).showToastMessage("영상 알람이 설정되었습니다.",
                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                        AppToast.GRAVITY_BOTTOM)
            } else {
                var errorResult: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
                Logger.e("error : $errorResult")

                AppToast(view!!.context).showToastMessage(errorResult.message,
                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                        AppToast.GRAVITY_BOTTOM)
            }
        } else if (data.arg1 == "refresh_follow") {
            isFollowRefresh = true
            EventBus.getDefault().post(NetworkBus(NetworkApi.API54.name))
        }
    }

    private fun initTab(userId: String) {
        if (pagerAdapter == null) {
            tabLayoutMy.addTab(tabLayoutMy.newTab().setText(getString(R.string.msg_caster_tab_video)))
            tabLayoutMy.addTab(tabLayoutMy.newTab().setText(getString(R.string.msg_caster_tab_product)))
        }

        pagerAdapter = CasterTabPagerAdapter(viewPagerContainer, childFragmentManager, userId)
        tabLayoutMy.addOnTabSelectedListener(tabSelectedListener)

        // TabStrip Size 축소
        WrappedTabLayoutStripUtil.wrapTabIndicatorToTitle(tabLayoutMy,
                PixelUtil.dpToPx(view!!.context, 60),
                PixelUtil.dpToPx(view!!.context, 60))

        tabLayoutMy.getTabAt(0)?.select()

        val fragment = pagerAdapter!!.instantiateItem(viewPagerContainer, CasterTab.VIDEO.ordinal) as Fragment
        if (fragment != null) {
            (fragment as CasterVODFragment).clearTotalCount()
            fragment.clearData()
        }

        setupTabLayoutFonts()
    }

    /**
     * 사용자 정보 설정
     */
    private fun handleNetworkResultUserInfo(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            var str = DBManager.getInstance(context!!).get(data.arg1)
            var response: API21 = Gson().fromJson(str, API21::class.java)
            initData(response)
        } else {
            var errorResult: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
            Logger.e("error : $errorResult")
        }
    }

    private fun initData(response: API21) {
        if (TextUtils.isEmpty(response.userIMG)) {
            imageViewProfile.setBackgroundResource(R.drawable.profile_default_img)
        } else {
            ImageLoad.setImage(view!!.context, imageViewProfile, response.userIMG, null, ImageLoad.SCALE_CIRCLE_CROP, DiskCacheStrategy.ALL)
        }

        if (TextUtils.isEmpty(response.userIMG_bg)) {
            imageViewBG.setBackgroundResource(R.drawable.profile_default_bg_img)
        } else {
            ImageLoad.setImage(view!!.context, imageViewBG, response.userIMG, null, ImageLoad.SCALE_NONE, DiskCacheStrategy.ALL)
        }

        setNickName(response.userNickname)

        followCnt = 0
        if (!response?.userFollower.isNullOrEmpty()) {
            followCnt = response?.userFollower.toInt()
        }
        setFollowerCount(followCnt)

        var followingCnt = 0
        if (!response?.userFollowing.isNullOrEmpty()) {
            followingCnt = response?.userFollowing.toInt()
        }
        setFollowingCount(followingCnt)
        setLikeCount(response.userFavorCount.toInt())

        isFollow = response.followingCheck
        casterId = response.userId
        if (response.followingCheck) {
            imageViewFollow.setBackgroundResource(R.drawable.following_ico)
        } else {
            imageViewFollow.setBackgroundResource(R.drawable.follow_ico)
        }

        nickName = response.userNickname
        profileImg = response.userIMG

        if (response.userProfile.isNotEmpty()) {
            layoutIntroduce.visibility = View.VISIBLE
            textViewIntroduce.text = response.userProfile
        } else {
            layoutIntroduce.visibility = View.GONE
        }

        isUserBlock = response.isUserBlock
        textViewBroadcastBlock.text = if(isUserBlock == "Y") getString(R.string.msg_my_message_blcok_cancel) else getString(R.string.msg_broadcast_block)
    }

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

    /**
     * 좋아요 갯수 텍스트를 설정
     * @param count
     */
    private fun setLikeCount(count: Int) {
        textViewLikeCount.text = StringUtils.getSnsStyleCountZeroBase(count)
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
                        tabViewChild.typeface = Typeface.createFromAsset(activity!!.assets, "fonts/notosanskr_bold.otf")
                    } else {
                        tabViewChild.typeface = Typeface.createFromAsset(activity!!.assets, "fonts/notosanskr_regular.otf")
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
            when (v?.id) {
                R.id.buttonBack,
                R.id.buttonClose -> {
                    val intent = Intent("close_drawer")
                    intent.putExtra("isRightClose", true)
                    androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(context!!).sendBroadcast(intent)
                }

                R.id.buttonFollow -> {
                    if (AppPreferences.getLoginStatus(context!!)) {
                        var followTemp = "N"
                        if (isFollow)
                            followTemp = "N"
                        else
                            followTemp = "Y"
                        var userId = AppPreferences.getUserId(context!!)
                        if (TextUtils.isEmpty(userId)) {
                            userId = ""
                        }

                        if (followTemp == "N") {
                            val item = API78.FollowItem().apply {
                                strToUserId = casterId
                                strThumbnail = profileImg
                            }

                            FollowCancelDialog(context!!, item, "", object : FollowCancelDialog.Listener {
                                override fun onDismiss() {
                                    var map = HashMap<String, String>()
                                    map.put("strUserId", userId!!)
                                    map.put("strToUserId", casterId)
                                    map.put("isFollow", "N")
                                    var body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), (JSONObject(map)).toString())
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

                            EventBus.getDefault().post(NetworkBus(NetworkApi.API2.name, body))
                        }
                    } else {
                        startActivityForResult(Intent(context!!, LoginActivity::class.java), REQUEST_LOGIN_FOR_FOLLOW)
                    }
                }

                R.id.buttonMessage -> {
                    if (AppPreferences.getLoginStatus(context!!)) {
                        startActivity(Intent(context!!, MyChatActivity::class.java).apply {
                            putExtra(MyChatActivity.INTENT_EXTRA_KEY_CASTER_NICKNAME, nickName)
                            putExtra(MyChatActivity.INTENT_EXTRA_KEY_CASTER_PROFILE, profileImg)
                            putExtra(MyChatActivity.INTENT_EXTRA_KEY_CASTER_ID, casterId)
                            putExtra(MyChatActivity.INTENT_EXTRA_KEY_CASTER_FOLLOW, isFollow)
                        })
                    } else {
                        startActivityForResult(Intent(context!!, LoginActivity::class.java), REQUEST_LOGIN_FOR_MESSAGE)
                    }
                }

                R.id.layoutMore -> layoutMore?.visibility = View.GONE

                R.id.buttonMore -> {
                    if (!AppPreferences.getLoginStatus(context!!)) {
                        startActivityForResult(Intent(context!!, LoginActivity::class.java), REQUEST_LOGIN_FOR_MORE)
                    } else {
                        if (layoutMore?.isVisible!!) {
                            layoutMore?.visibility = View.GONE
                        } else {
                            layoutMore?.visibility = View.VISIBLE
                        }
                    }
                }

                R.id.textViewReport -> {
                    layoutMore?.visibility = View.GONE

                    startActivity(Intent(view!!.context, BroadcastReportActivity::class.java).apply {
                        putExtra(BroadcastReportActivity.INTENT_EXTRA_KEY_REPORT_TYPE, true)
                        putExtra(BroadcastReportActivity.INTENT_EXTRA_KEY_USER_ID, mUserId)
                    })
                }

                R.id.textViewBroadcastBlock -> {
                    layoutMore?.visibility = View.GONE

                    if(isUserBlock == "N") {
                        val item = API54.MessageItem().apply {
                            send_mb_id = casterId
                            send_mb_img = profileImg
                            send_mb_nick = nickName
                        }

                        val dialog = MessageBlockDialog(context!!, item, object : MessageBlockDialog.Listener {
                            override fun onDismiss() {
                                Logger.e("onDismiss")
                            }

                            override fun onBlock() {
                                Logger.e("onBlock")
                                val requestBody = JSONObject().apply {
                                    put("user", AppPreferences.getUserId(context!!))
                                    put("block_user", casterId)
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
                            put("user", AppPreferences.getUserId(context!!))
                            put("block_user", casterId)
                            put("status", "N")
                        }

                        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), requestBody.toString())
                        val bus = NetworkBus(NetworkApi.API57.name, body)
                        EventBus.getDefault().post(bus)
                    }
                }

                R.id.textViewAlarmSet -> {
                    layoutMore?.visibility = View.GONE

//                    val obj = JSONObject().apply {
//                        put("type", "follow")
//                        put("val", "Y")
//                        put("mUserId", AppPreferences.getUserId(context!!)!!)
//                        put("follow_user", casterId)
                    val obj = JSONObject().apply {
                        put("type", "live")
                        put("val", "Y")
                        put("user", AppPreferences.getUserId(context!!)!!)
                        put("follow_user", casterId)
                    }

                    val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), obj.toString())
                    val bus = NetworkBus(NetworkApi.API64.name, body)
                    EventBus.getDefault().post(bus)
                }

                R.id.layoutFollower -> {
                    startActivity(Intent(context!!, FollowerActivity::class.java).apply {
                        putExtra(FollowerActivity.INTENT_EXTRA_KEY_IS_FOLLOWER, true)
                        putExtra(FollowerActivity.INTENT_EXTRA_KEY_USERID, mUserId)
                    })
                }

                R.id.layoutFollowing -> {
                    startActivity(Intent(context!!, FollowerActivity::class.java).apply {
                        putExtra(FollowerActivity.INTENT_EXTRA_KEY_IS_FOLLOWER, false)
                        putExtra(FollowerActivity.INTENT_EXTRA_KEY_USERID, mUserId)
                    })
                }
            }
        }
    }

    private fun toggleFollow() {
        isFollow = !isFollow
    }
}