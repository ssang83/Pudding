package com.enliple.pudding.fragment.main.pudding

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.enliple.pudding.R
import com.enliple.pudding.activity.*
import com.enliple.pudding.adapter.my.MyChannelVideoAdapter
import com.enliple.pudding.common.VideoDataContainer
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.internal.AppConstants
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.*
import com.enliple.pudding.commons.network.vo.API0
import com.enliple.pudding.commons.network.vo.API21
import com.enliple.pudding.commons.network.vo.VOD
import com.enliple.pudding.commons.shoptree.network.ShopTreeAsyncTask
import com.enliple.pudding.commons.widget.toast.AppToast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_pudding_channel.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

/**
 * Created by Kim Joonsung on 2019-03-07.
 */
class PuddingChannelFragment : androidx.fragment.app.Fragment(), MyChannelVideoAdapter.Listener {

    companion object {
        private const val ACTIVITY_REQUEST_CODE_VOD = 0xAB01
        private const val ACTIVITY_REQUEST_CODE_LIVE = 0xAB02
        private const val ACTIVITY_REQUEST_CODE_RECORDING = 0xAB03
    }

    private var mAdapter: MyChannelVideoAdapter? = null

    private var mIsVisibleToUser = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_pudding_channel, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        EventBus.getDefault().register(this)

        recyclerViewVod.setHasFixedSize(true)
        recyclerViewVod.layoutManager = androidx.recyclerview.widget.GridLayoutManager(view.context, 2)
        recyclerViewVod.post {
            if (recyclerViewVod != null) {
                mAdapter = MyChannelVideoAdapter(recyclerViewVod.width).apply {
                    setListener(this@PuddingChannelFragment)
                }
                recyclerViewVod?.adapter = mAdapter
            }
        }

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
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        Logger.i("setUserVisibleHint: $isVisibleToUser")

        mIsVisibleToUser = isVisibleToUser

        if (isVisibleToUser) {
            refresh()
        }
    }

    override fun onResume() {
        super.onResume()
        if (mIsVisibleToUser) {
            refresh()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onItemClicked(item: API0.DataBeanX, position: Int) {
//        startActivity(Intent(view!!.context, MyVODActivity::class.java))
        if (!TextUtils.isEmpty(item.stream) && item.show_YN == "Y") {
            // 페이징 및 dbKey 포지션 문제 때문에 앞으로 아래와 같이 List 객체를 만들어서 영상 데이터 사용
            val json = Gson().toJson(mAdapter?.items)
            val videoItems : MutableList<VOD.DataBeanX> = Gson().fromJson(json, object : TypeToken<MutableList<VOD.DataBeanX>>(){}.type)
            VideoDataContainer.getInstance().mVideoData = videoItems

            startActivity(Intent(context!!, ShoppingPlayerActivity::class.java).apply {
                putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_PLAYER_FLAG, AppConstants.MY_UPLOAD_VOD_PLAYER)
                putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_MY_VOD_POSITION, position)
                putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_CASTER_ID, item.userId)
                putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_VIDEO_TYPE, item.videoType)

                data = Uri.parse("vcommerce://shopping?url=${item.stream}")
            })
        } else {
            if (item != null && item.show_YN == "N") {
                AppToast(context!!).showToastMessage("삭제된 동영상은 재생할 수 없습니다.",
                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                        AppToast.GRAVITY_BOTTOM)
            } else {
                AppToast(context!!).showToastMessage("방송 정보 메타 데이터가 존재하지 않습니다.",
                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                        AppToast.GRAVITY_BOTTOM)
            }
        }
    }

    override fun onMyLikeStatusChanged(isMyLike: Boolean, itemPosition: Int) {
        Logger.d("LikeStatusChanged : $isMyLike , $itemPosition")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == ACTIVITY_REQUEST_CODE_LIVE && resultCode == Activity.RESULT_OK) {
            startActivity(Intent(context!!, BroadcastSettingActivity::class.java)
                    .apply { putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_CHAT_ACCOUNT, AppPreferences.getUserId(context!!)) }
                    .apply { putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_CHAT_ROOM_ID, "") }
                    .apply { putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_TAB, BroadcastSettingActivity.TAB_LIVE) }
                    .apply { putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_CHAT_NICKNAME, AppPreferences.getUserId(context!!)) })
        } else if (requestCode == ACTIVITY_REQUEST_CODE_RECORDING && resultCode == Activity.RESULT_OK) {
            startActivity(Intent(context!!, BroadcastSettingActivity::class.java)
                    .apply { putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_CHAT_ACCOUNT, AppPreferences.getUserId(context!!)) }
                    .apply { putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_CHAT_ROOM_ID, "") }
                    .apply { putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_TAB, BroadcastSettingActivity.TAB_VOD) }
                    .apply { putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_CHAT_NICKNAME, AppPreferences.getUserId(context!!)) })
        } else if (requestCode == ACTIVITY_REQUEST_CODE_VOD && resultCode == Activity.RESULT_OK) {
            startActivity(Intent(context!!, BroadcastSettingActivity::class.java)
                    .apply { putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_CHAT_ACCOUNT, AppPreferences.getUserId(context!!)) }
                    .apply { putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_CHAT_ROOM_ID, "") }
                    .apply { putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_TAB, BroadcastSettingActivity.TAB_VOD) }
                    .apply { putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_CHAT_NICKNAME, AppPreferences.getUserId(context!!)) })
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: NetworkBusResponse) {
        val api0 = NetworkHandler.getInstance(context!!).getKey(NetworkApi.API0.toString(), AppPreferences.getUserId(context!!)!!, "")
        val api21 = NetworkHandler.getInstance(context!!).getKey(NetworkApi.API21.toString(), AppPreferences.getUserId(context!!)!!, "")

        if (data.arg1 == api0) {
            handleNetworkResult(data)
        } else if (data.arg1 == api21) {
            if ("ok" == data.arg2) {
                var response: API21 = Gson().fromJson(DBManager.getInstance(context!!).get(data.arg1), API21::class.java)

                setShareVodCount(response.userShareCount.toInt())
            }
        }
    }

    private fun handleNetworkResult(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            val response: API0 = Gson().fromJson(DBManager.getInstance(context!!).get(data.arg1), API0::class.java)
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
            } else {
                myTotalCount.text = "${response.nTotalCount}"
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
        EventBus.getDefault().post(NetworkBus(NetworkApi.API21.name, AppPreferences.getUserId(context!!)))

        var task = ShopTreeAsyncTask(activity)
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

    private val clickListener = View.OnClickListener {
        when (it?.id) {
            R.id.buttonRegister -> {
                var browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://shop-tree.com/"))
                startActivity(browserIntent)
            }
//            R.id.buttonCalc -> {
//                var browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://puddinglive.com"))
//                startActivity(browserIntent)
//            }
            R.id.buttonScrap -> startActivity(Intent(it.context, ScrapVODActivity::class.java))
            R.id.buttonLatestView -> startActivity(Intent(it.context, LatestViewVODActivity::class.java))
            R.id.buttonShare -> startActivity(Intent(it.context, ShareContentActivity::class.java))
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
                if (AppPreferences.getLoginStatus(context!!)) {
                    startActivity(Intent(context!!, BroadcastSettingActivity::class.java)
                            .apply { putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_CHAT_ACCOUNT, AppPreferences.getUserId(context!!)) }
                            .apply { putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_CHAT_ROOM_ID, "") }
                            .apply { putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_TAB, BroadcastSettingActivity.TAB_VOD) }
                            .apply { putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_CHAT_NICKNAME, AppPreferences.getUserId(context!!)) })
                } else {
                    startActivityForResult(Intent(context!!, LoginActivity::class.java), ACTIVITY_REQUEST_CODE_RECORDING)
                }
            }

            R.id.buttonLive -> {
                if (AppPreferences.getLoginStatus(context!!)) {
                    startActivity(Intent(context!!, BroadcastSettingActivity::class.java)
                            .apply { putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_CHAT_ACCOUNT, AppPreferences.getUserId(context!!)) }
                            .apply { putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_CHAT_ROOM_ID, "") }
                            .apply { putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_TAB, BroadcastSettingActivity.TAB_LIVE) }
                            .apply { putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_CHAT_NICKNAME, AppPreferences.getUserId(context!!)) })
                } else {
                    startActivityForResult(Intent(context!!, LoginActivity::class.java), ACTIVITY_REQUEST_CODE_LIVE)
                }
            }

            R.id.buttonVod -> {
                if (AppPreferences.getLoginStatus(context!!)) {
                    startActivity(Intent(context!!, BroadcastSettingActivity::class.java)
                            .apply { putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_CHAT_ACCOUNT, AppPreferences.getUserId(context!!)) }
                            .apply { putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_CHAT_ROOM_ID, "") }
                            .apply { putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_TAB, BroadcastSettingActivity.TAB_VOD_UPLOAD) }
                            .apply { putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_CHAT_NICKNAME, AppPreferences.getUserId(context!!)) })
                } else {
                    startActivityForResult(Intent(context!!, LoginActivity::class.java), ACTIVITY_REQUEST_CODE_VOD)
                }
            }
        }
    }
}