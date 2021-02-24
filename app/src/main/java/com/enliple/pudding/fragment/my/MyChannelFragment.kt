package com.enliple.pudding.fragment.my

import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.enliple.pudding.AbsBaseTabFragment
import com.enliple.pudding.R
import com.enliple.pudding.activity.*
import com.enliple.pudding.adapter.my.MyChannelVideoAdapter
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.events.OnSingleClickListener
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.*
import com.enliple.pudding.commons.network.vo.API0
import com.enliple.pudding.commons.network.vo.API56
import com.enliple.pudding.commons.network.vo.BaseAPI
import com.enliple.pudding.commons.widget.recyclerview.WrappedLinearLayoutManager
import com.enliple.pudding.enumeration.MyTab
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_my_channel.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

/**
 * My - My채널 Fragment
 * @author hkcha
 * @since 2018.08.08
 */
class MyChannelFragment : AbsBaseTabFragment(), MyChannelVideoAdapter.Listener {
    private var mIsVisibleToUser = false
    private var count = 0
    companion object {
        private const val ACTIVITY_REQUEST_CODE_CASTING = 0xAB01
        private const val ACTIVITY_REQUEST_CODE_LOGIN = 0xAB02
    }

    private lateinit var mAdapter: MyChannelVideoAdapter

    private var listPage = 1
    private var totalPageCount = 1

    private var isCasting = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var bundle = arguments
        if ( bundle != null ) {
            count = bundle!!.getInt("count")
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_my_channel, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        EventBus.getDefault().register(this)

        recyclerViewVod?.setHasFixedSize(true)
        //recyclerViewVod.setItemViewCacheSize(RECYCLER_VIEW_ITEM_CACHE_COUNT)
        recyclerViewVod?.layoutManager = androidx.recyclerview.widget.GridLayoutManager(view.context, 2)

        recyclerViewVod.post {
            if (recyclerViewVod != null) {
                mAdapter = MyChannelVideoAdapter(recyclerViewVod.width).apply {
                    setListener(this@MyChannelFragment)
                }
                recyclerViewVod?.adapter = mAdapter
            }
        }

        buttonAllVODMore.setOnClickListener(clickListener)
        layoutSettings.setOnClickListener(clickListener)
        layoutShare.setOnClickListener(clickListener)
        layoutDaily.setOnClickListener(clickListener)
        layoutBoardInfo.setOnClickListener(clickListener)
        layoutSaleInfo.setOnClickListener(clickListener)
        layoutRankInfo.setOnClickListener(clickListener)
        layoutCompany.setOnClickListener(clickListener)
        buttonNotice.setOnClickListener(clickListener)
        buttonTerms.setOnClickListener(clickListener)
        buttonPrivacy.setOnClickListener(clickListener)
        buttonCustomerCenter.setOnClickListener(clickListener)
        imageViewEmpty.setOnClickListener(clickListener)

        textViewShareVodCount.text = "$count"
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
        Logger.e("onResume")

        refresh()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    fun setCount(count:Int) {
        if ( textViewShareVodCount != null ) {
            textViewShareVodCount.text = "$count"
        }
    }

    private fun refresh() {
        if (!mIsVisibleToUser) return

        Logger.d("refresh")
        EventBus.getDefault().post(NetworkBus(NetworkApi.API0.name))
    }

    override fun onTabChanged(tabIndex: Int) {
        if (tabIndex == MyTab.CHANNEL.ordinal) {

        }
    }

    override fun onMyLikeStatusChanged(isMyLike: Boolean, itemPosition: Int) {
        Logger.d("LikeStatusChanged : $isMyLike , $itemPosition")
    }

    override fun onItemClicked(item: API0.DataBeanX, position: Int) {
        startActivity(Intent(view!!.context, MyVODActivity::class.java))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == ACTIVITY_REQUEST_CODE_CASTING) {
            // 방송요청을 다시 할 수 있도록 UI Lock 을 Release
            isCasting = false
        } else if (requestCode == ACTIVITY_REQUEST_CODE_LOGIN) {
            isCasting = false
            var userId = AppPreferences.getUserId(view!!.context)
            if (!TextUtils.isEmpty(userId)) {
                startActivityForResult(Intent(view!!.context, BroadcastSettingActivity::class.java)
                        .apply { putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_CHAT_ACCOUNT, userId) }
                        .apply { putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_CHAT_ROOM_ID, "") }
                        .apply { putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_CHAT_NICKNAME, userId) },
                        ACTIVITY_REQUEST_CODE_CASTING)
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: NetworkBusResponse) {
        val api0 = NetworkHandler.getInstance(context!!).getKey(NetworkApi.API0.toString(), AppPreferences.getUserId(context!!)!!, "")
        if (data.arg1.startsWith(api0)) {
            handleNetworkResult(data)
        } else if (data.arg1 == NetworkApi.API56.toString()) {
            handleNetworkAPI56(data)
        }
    }

    private fun handleNetworkAPI56(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            val response: API56 = Gson().fromJson(DBManager.getInstance(context!!).get(data.arg1), API56::class.java)

            if (response.result == "success") {
                textViewCompanyName.text = response.data.name
                textViewCompanyOwner.text = response.data.owner
                textViewCompanyNumber.text = response.data.company_no
                textViewCommunicationNumber.text = response.data.communication_no
                textViewCustomerCenter.text = response.data.tel
                textViewCompanyMail.text = response.data.email
                textViewCompanyAddr.text = response.data.addr
                textViewCompanyHosting.text = response.data.hosting
            }
        } else {
            var errorResult: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
            Logger.e("error : $errorResult")
        }
    }

    private fun handleNetworkResult(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            var str = DBManager.getInstance(context!!).get(data.arg1)
            Logger.e("str :::: " + str)
            var response: API0 = Gson().fromJson(str, API0::class.java)

            totalPageCount = response.nTotalCount
            listPage = response.pageCount
            Logger.e("response:total :: ${response.nTotalCount}")

            if (response.nTotalCount > 0) {
                recyclerViewVod.visibility = View.VISIBLE
                buttonAllVODMore.visibility = View.VISIBLE
                imageViewEmpty.visibility = View.GONE

                val items = ArrayList<API0.DataBeanX>()
                if (response.nTotalCount < 2) {
                    if ( response.data[0].show_YN == "Y") {
                        items.add(response.data[0])
                        if ( mAdapter != null )
                            mAdapter?.setItems(items)
                    } else {
                        recyclerViewVod.visibility = View.GONE
                        buttonAllVODMore.visibility = View.GONE
                        imageViewEmpty.visibility = View.VISIBLE
                    }
                } else {
                    for (i in 0 until response.data.size) {
                        Logger.e("showYN")
                        if ( response.data[i].show_YN == "Y") {
                            items.add(response.data[i])
                        }

                        if ( items.size == 2 )
                            break
                    }

                    if ( items.size > 0 ) {
                        if ( mAdapter != null )
                            mAdapter?.setItems(items)
                    } else {
                        recyclerViewVod.visibility = View.GONE
                        buttonAllVODMore.visibility = View.GONE
                        imageViewEmpty.visibility = View.VISIBLE
                    }
                }
            } else {
                recyclerViewVod.visibility = View.GONE
                buttonAllVODMore.visibility = View.GONE
                imageViewEmpty.visibility = View.VISIBLE
            }
        } else {
            var errorResult: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
            Logger.e("error : $errorResult")

            recyclerViewVod.visibility = View.GONE
            buttonAllVODMore.visibility = View.GONE
            imageViewEmpty.visibility = View.VISIBLE
        }
    }

    /**
     * UI 상 내 비디오 콘텐츠 영역의 표시여부를 설정
     * (내 비디오가 없는 경우를 고려한 Setting)
     */
    private fun setMyVideoVisibility(visible: Boolean) {
        layoutMyVideo.visibility = if (visible) View.VISIBLE else View.GONE
    }

    private val clickListener = object : OnSingleClickListener() {
        override fun onSingleClick(v: View?) {
            when (v?.id) {
                R.id.buttonAllVODMore -> startActivity(Intent(v.context, MyVODActivity::class.java))
                R.id.layoutSettings -> startActivity(Intent(v.context, SettingsActivity::class.java))
                R.id.layoutDaily -> {
                    startActivity(Intent(v.context, StatisticsActivity::class.java).apply {
                        var title = resources.getString(R.string.msg_my_menu_statistics_daily)
                        putExtra("TITLE", title)
                        putExtra("URL", NetworkConst.STATISTICS_DAILY_URL)
                    })
                }

                R.id.layoutBoardInfo -> {
                    startActivity(Intent(v.context, StatisticsActivity::class.java).apply {
                        var title = resources.getString(R.string.msg_my_menu_statistics_board_info)
                        putExtra("TITLE", title)
                        putExtra("URL", NetworkConst.STATISTICS_BOARD_URL)
                    })
                }

                R.id.layoutSaleInfo -> {
                    startActivity(Intent(v.context, StatisticsActivity::class.java).apply {
                        var title = resources.getString(R.string.msg_my_menu_statistics_sale_info)
                        putExtra("TITLE", title)
                        putExtra("URL", NetworkConst.STATISTICS_SALE_URL)
                    })
                }

                R.id.layoutRankInfo -> {
                    startActivity(Intent(v.context, StatisticsActivity::class.java).apply {
                        var title = resources.getString(R.string.msg_my_menu_statistics_rank_info)
                        putExtra("TITLE", title)
                        putExtra("URL", NetworkConst.STATISTICS_RANK_URL)
                    })
                }

                R.id.layoutShare -> startActivity(Intent(v.context, ShareContentActivity::class.java))

                R.id.layoutCompany -> {
                    if (!buttonDromDown.isSelected) {
                        buttonDromDown.isSelected = true
                        layoutCompanyInfo.visibility = View.VISIBLE

                        val bus = NetworkBus(NetworkApi.API56.name)
                        EventBus.getDefault().post(bus)
                    } else {
                        buttonDromDown.isSelected = false
                        layoutCompanyInfo.visibility = View.GONE
                    }
                }

                R.id.buttonNotice -> startActivity(Intent(v.context, NoticeListActivity::class.java))

                R.id.buttonTerms -> {
                    startActivity(Intent(v!!.context, AgreementActivity::class.java).apply {
                        putExtra(AgreementActivity.INTENT_EXTRA_KEY_MODE, AgreementActivity.INTENT_EXTRA_VALUE_TERM)
                    })
                }

                R.id.buttonPrivacy -> {
                    startActivity(Intent(v!!.context, AgreementActivity::class.java).apply {
                        putExtra(AgreementActivity.INTENT_EXTRA_KEY_MODE, AgreementActivity.INTENT_EXTRA_VALUE_PRIVACY_USE)
                    })
                }

                R.id.buttonCustomerCenter -> startActivity(Intent(v.context, CustomerCenterMainActivity::class.java))

                R.id.imageViewEmpty -> {
                    if (!isCasting) {
                        isCasting = true

                        var userId = AppPreferences.getUserId(view!!.context)
                        var isLogin = AppPreferences.getLoginStatus(view!!.context)
                        if (isLogin) {
                            startActivityForResult(Intent(view!!.context, BroadcastSettingActivity::class.java)
                                    .apply { putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_CHAT_ACCOUNT, userId) }
                                    .apply { putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_CHAT_ROOM_ID, "") }
                                    .apply { putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_CHAT_NICKNAME, userId) },
                                    ACTIVITY_REQUEST_CODE_CASTING)
                        } else {
                            startActivityForResult(Intent(view!!.context, LoginActivity::class.java),
                                    ACTIVITY_REQUEST_CODE_LOGIN)
                        }
                    }
                }
            }
        }
    }
}