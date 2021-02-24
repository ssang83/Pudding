package com.enliple.pudding.fragment.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.enliple.pudding.R
import com.enliple.pudding.activity.ShoppingPlayerActivity
import com.enliple.pudding.adapter.home.CasterVODAdapter
import com.enliple.pudding.common.VideoDataContainer
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.NetworkApi
import com.enliple.pudding.commons.network.NetworkBus
import com.enliple.pudding.commons.network.NetworkBusResponse
import com.enliple.pudding.commons.network.NetworkHandler
import com.enliple.pudding.commons.network.vo.API32
import com.enliple.pudding.commons.network.vo.BaseAPI
import com.enliple.pudding.commons.network.vo.VOD
import com.enliple.pudding.commons.widget.recyclerview.WrappedLinearLayoutManager
import com.enliple.pudding.commons.widget.toast.AppToast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_caster_video.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Created by Kim Joonsung on 2018-10-10.
 */
class CasterVODFragment : androidx.fragment.app.Fragment(), CasterVODAdapter.Listener {

    companion object {
        const val BUNDLE_EXTRA_KEY_USER_ID = "CasterVODFragment_userId"

        private const val RECYCLER_VIEW_ITEM_CACHE_SIZE = 20
        private const val PAGE_DATA_COUNT = 100
    }

    private lateinit var mAdapter: CasterVODAdapter
    private lateinit var mLayoutManager: WrappedLinearLayoutManager
    private var userId = ""
    private var pageCount = 1
    private var dataKey = ""
    private var isOrderClick = false
    private var isReload = false
    private var isEndOfData = true
    private var mOrder = "1"
    private var page = 1
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_caster_video, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (arguments != null) {
            userId = arguments!!.getString(BUNDLE_EXTRA_KEY_USER_ID)
            Logger.e("CasterVODFragment userId :: $userId")
        }

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }

        mOrder = "1"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerViewVideo.setHasFixedSize(false)
        recyclerViewVideo.setItemViewCacheSize(RECYCLER_VIEW_ITEM_CACHE_SIZE)
        recyclerViewVideo.addOnScrollListener(scrollListener)

        mLayoutManager = WrappedLinearLayoutManager(view.context)
        recyclerViewVideo.layoutManager = mLayoutManager

        mAdapter = CasterVODAdapter(context!!).apply {
            setListener(this@CasterVODFragment)
        }
        recyclerViewVideo.adapter = mAdapter
    }

    override fun onResume() {
        super.onResume()

        refresh()
    }

    override fun onDestroy() {
        super.onDestroy()

        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }

    override fun onItemClicked(item: API32.DataBeanX, position: Int) {
        Logger.e("URL : ${item.stream}, userId : ${item.userId}")

        // 페이징 및 dbKey 포지션 문제 때문에 앞으로 아래와 같이 List 객체를 만들어서 영상 데이터 사용
        val json = Gson().toJson(mAdapter.mItems)
        val videoItems : MutableList<VOD.DataBeanX> = Gson().fromJson(json, object : TypeToken<MutableList<VOD.DataBeanX>>(){}.type)
        VideoDataContainer.getInstance().mVideoData = videoItems

        if (!TextUtils.isEmpty(item.stream)) {
            startActivity(Intent(context!!, ShoppingPlayerActivity::class.java).apply {
                putExtra(ShoppingPlayerActivity.FROM_WHERE, ShoppingPlayerActivity.FROM_RIGHT)
                putExtra(ShoppingPlayerActivity.CASTER_WHAT, ShoppingPlayerActivity.CASTER_VOD)
                putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_POSITION, position)
                putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_CASTER_ID, item.userId)
                putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_VIDEO_TYPE, item.videoType)
                flags = Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK

                data = Uri.parse("vcommerce://shopping?url=${item.stream}")
            })
        } else {
            AppToast(context!!).showToastMessage("방송 정보 메타 데이터가 존재하지 않습니다.",
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM)
        }
    }

    override fun onRecentlyOrderClicked(order: String) {
        isOrderClick = true

        isReload = false
        mOrder = order
        page = 1
        EventBus.getDefault().post(NetworkBus(NetworkApi.API32.name, userId, "ALL", mOrder, "$page"))
    }

    override fun onPopularityOrderClicked(order: String) {
        isOrderClick = true

        isReload = false
        mOrder = order
        page = 1
        EventBus.getDefault().post(NetworkBus(NetworkApi.API32.name, userId, "ALL", mOrder, "$page"))
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: NetworkBusResponse) {
        val key = NetworkHandler.getInstance(context!!).getKey(NetworkApi.API32.toString(), userId, "ALL")
        if (data.arg1.startsWith(key)) {
            handleNetworkResult(data)
        }
    }

    fun clearData() {
        if(mAdapter != null) {
            mAdapter.clearData()
        }
    }

    fun clearTotalCount() {
        if(mAdapter != null) {
            mAdapter.setTotalCount(0)
        }
    }

    private fun refresh() {
        if (!TextUtils.isEmpty(userId)) {
            EventBus.getDefault().post(NetworkBus(NetworkApi.API32.name, userId, "ALL", mOrder, "$page"))
        }
    }

    private fun handleNetworkResult(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            var str = DBManager.getInstance(context!!).get(data.arg1)
            var result: API32 = Gson().fromJson(str, API32::class.java)

            dataKey = data.arg1

            pageCount = result.pageCount
            isEndOfData = result.data.size == 0
            if (result.data.size > 0) {
                emptyLayer.visibility = View.GONE
                recyclerViewVideo.visibility = View.VISIBLE

                mAdapter.setTotalCount(result.nTotalCount)

                if (!isReload) {
                    mAdapter.setItem(result.data, isOrderClick)
                } else {
                    mAdapter.addItem(result.data)
                }
            } else {
                if(isReload) {
                    if ( mAdapter?.itemCount!! <= 0 ) {
                        emptyLayer.visibility = View.VISIBLE
                        recyclerViewVideo.visibility = View.GONE
                    }
                } else {
                    emptyLayer.visibility = View.VISIBLE
                    recyclerViewVideo.visibility = View.GONE
                }
            }
        } else {
            var errorResult: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
            Logger.e("error : $errorResult")
        }
    }

    private val scrollListener = object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val lastVisibleItemPosition = mLayoutManager.findLastCompletelyVisibleItemPosition()
            val totalItemCount = mAdapter?.itemCount?.minus(1)
            Logger.e("lastVisibleItemPosition :: " + lastVisibleItemPosition)
            Logger.e("totalItemCount :: " + totalItemCount)
            Logger.e("isEndOfData :: " + isEndOfData)
            if ((lastVisibleItemPosition == totalItemCount) && !isEndOfData && dy > 0) {
                isReload = true
                ++pageCount
                page ++

                AppToast(context!!).showToastMessage("loading 중...",
                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                        AppToast.GRAVITY_BOTTOM)

                EventBus.getDefault().post(NetworkBus(NetworkApi.API32.name, userId, "ALL", mOrder, "$page"))
            }
        }
    }
}