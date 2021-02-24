package com.enliple.pudding.fragment.main.pudding

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.enliple.pudding.R
import com.enliple.pudding.activity.CasterProfileActivity
import com.enliple.pudding.activity.ShoppingPlayerActivity
import com.enliple.pudding.adapter.home.FollowingAdapter
import com.enliple.pudding.adapter.home.FollowingUserAdapter
import com.enliple.pudding.adapter.home.RecommendUserAdapter
import com.enliple.pudding.common.VideoDataContainer
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.internal.AppConstants
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.*
import com.enliple.pudding.commons.network.vo.API113
import com.enliple.pudding.commons.network.vo.API78
import com.enliple.pudding.commons.network.vo.VOD
import com.enliple.pudding.commons.widget.recyclerview.WrappedGridLayoutManager
import com.enliple.pudding.commons.widget.recyclerview.WrappedLinearLayoutManager
import com.enliple.pudding.commons.widget.toast.AppToast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_pudding_following.*
import kotlinx.android.synthetic.main.fragment_pudding_following.refreshLayout
import kotlinx.android.synthetic.main.fragment_pudding_schedule.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONArray

/**
 * Created by Kim Joonsung on 2019-01-25.
 */
class PuddingFollowingFragment : Fragment(), FollowingAdapter.Listener,
        RecommendUserAdapter.Listener,
        FollowingUserAdapter.Listener,
        SwipeRefreshLayout.OnRefreshListener {

    companion object {
        private const val RECYCLER_VIEW_ITEM_CACHE_SIZE = 20
        private const val RECOMMEND_USER_COLUMN = 3
        private const val PAGE_DATA_COUNT = 100
    }

    private var mAdapter: FollowingAdapter? = null
    private var mEmptyAdapter: RecommendUserAdapter? = null
    private lateinit var mLayoutManager: WrappedLinearLayoutManager

    private var dataKey = ""
    private var keyword = ""
    private var pageCount = 1
    private var isReload = false
    private var isEndOfData = false
    private var mIsVisibleToUser = false
    private var mIsFirst = false
    private var mUserCount = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_pudding_following, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        EventBus.getDefault().register(this)
        registerReceiver()
        topBtn.setOnClickListener { recyclerViewFollow.scrollToPosition(0) }

        recyclerViewFollow.addOnScrollListener(scrollListener)
        recyclerViewFollow.setItemViewCacheSize(RECYCLER_VIEW_ITEM_CACHE_SIZE)
        recyclerViewFollow.setHasFixedSize(true)

        mLayoutManager = WrappedLinearLayoutManager(context!!).apply {
            orientation = RecyclerView.VERTICAL
        }

        recyclerViewFollow.layoutManager = mLayoutManager

        mAdapter = FollowingAdapter(context!!).apply {
            setListener(this@PuddingFollowingFragment)
            setSubListener(this@PuddingFollowingFragment)
            setFooterListener(this@PuddingFollowingFragment)
        }

        recyclerViewFollow.adapter = mAdapter

        refreshLayout.setOnRefreshListener(this)
        refreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
        )
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        Logger.i("setUserVisibleHint: $isVisibleToUser")

        mIsVisibleToUser = isVisibleToUser

        if (isVisibleToUser && context != null) {
            if (!mIsFirst) {
                mIsFirst = true  // 최초 한번만 가져온다.
                refresh()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        unregisterReceiver()
    }

    override fun onRefresh() {
        Logger.e("onRefresh")
        recyclerViewFollow.visibility = View.INVISIBLE
        NetworkBus(NetworkApi.API77.name, AppPreferences.getUserId(context!!), "2", "").let {
            EventBus.getDefault().post(it)
        }

        refreshLayout.isRefreshing = false
    }

    private fun refresh() {
        if (mIsVisibleToUser) { // && (parentFragment as PuddingHomeTabFragment)?.userVisibleHint) {
            Logger.d("refresh")
            recyclerViewFollow.visibility = View.INVISIBLE
            progressBar.visibility = View.VISIBLE

            isReload = false
            Logger.e("pageCount = 1 refresh")
            pageCount = 1

            NetworkBus(NetworkApi.API77.name, AppPreferences.getUserId(context!!), "2", "").let {
                EventBus.getDefault().post(it)
            }
        }
    }

    override fun onItemClicked(item: VOD.DataBeanX, position: Int) {
        Logger.e("URL : ${item.stream}")

        // 페이징 및 dbKey 포지션 문제 때문에 앞으로 아래와 같이 List 객체를 만들어서 영상 데이터 사용
        val json = Gson().toJson(mAdapter?.items)
        val videoItems : MutableList<VOD.DataBeanX> = Gson().fromJson(json, object : TypeToken<MutableList<VOD.DataBeanX>>(){}.type)
        VideoDataContainer.getInstance().mVideoData = videoItems

        if (!TextUtils.isEmpty(item.stream)) {
            startActivity(Intent(context!!, ShoppingPlayerActivity::class.java).apply {
                putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_POSITION, position)
                putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_VIDEO_TYPE, item.videoType)

                data = Uri.parse("vcommerce://shopping?url=${item.stream}")
            })
        } else {
            AppToast(context!!).showToastMessage("방송 정보 메타 데이터가 존재하지 않습니다.",
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM)
        }
    }

    override fun onUserClicked(item: API113.UserItem) {
        startActivity(Intent(context!!, CasterProfileActivity::class.java).apply {
            putExtra(CasterProfileActivity.INTENT_KEY_EXTRA_USER_ID, item.mb_id)
        })
    }

    override fun onFollowingUserClick(item: API78.FollowItem) {
        startActivity(Intent(context!!, CasterProfileActivity::class.java).apply {
            putExtra(CasterProfileActivity.INTENT_KEY_EXTRA_USER_ID, item.strToUserId)
        })
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: NetworkBusResponse) {
        val key = NetworkHandler.getInstance(context!!).getKey(NetworkApi.VOD0.toString(), "follow", pageCount.toString())
        val api77 = NetworkHandler.getInstance(context!!).getKey(NetworkApi.API77.toString(), AppPreferences.getUserId(context!!)!!, "2", "")

        if (data.arg1.startsWith(key)) {
            handleNetworkFollowing(data)
        } else if (data.arg1.startsWith(NetworkApi.API113.toString())) {
            handleNetworkNotFollowing(data)
        } else if (data.arg1.startsWith(NetworkApi.API2.toString())) {
//            Logger.e("load follow list")
//            NetworkBus(NetworkApi.VOD0.name, "follow", pageCount.toString(), keyword).let {
//                EventBus.getDefault().post(it)
//            }
        } else if (data.arg1 == api77) {
            handleNetworkAPI78(data)
        }
    }

    @Subscribe
    fun onMessageEvent(data: String) {
        if (data.startsWith("refresh")) {
            refresh()
        }
    }

    private fun handleNetworkAPI78(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            val response: API78 = Gson().fromJson(DBManager.getInstance(context!!).get(data.arg1), API78::class.java)

            mUserCount = response.nTotalCount
            if (response.nTotalCount > 0) {
//                recyclerViewFollow.visibility = View.VISIBLE
//                layoutEmpty.visibility = View.GONE

                mAdapter?.setFollowingUserItem(response.data)
                Logger.e("pageCount 1 :: + $pageCount")
                NetworkBus(NetworkApi.VOD0.name, "follow", pageCount.toString(), keyword).let {
                    EventBus.getDefault().post(it)
                }
            } else {
                recyclerViewFollow.visibility = View.GONE
                layoutEmpty.visibility = View.VISIBLE

                NetworkBus(NetworkApi.API113.name).let {
                    EventBus.getDefault().post(it)
                }
            }
        }
    }

    private fun handleNetworkFollowing(data: NetworkBusResponse) {
        progressBar.visibility = View.GONE
        if ("ok" == data.arg2) {
            val result: VOD = Gson().fromJson(DBManager.getInstance(context!!).get(data.arg1), VOD::class.java)
            Logger.e("result totalCount : ${result.nTotalCount}")
            isEndOfData = result.data.size < PAGE_DATA_COUNT

            pageCount = result.pageCount
            Logger.e("pageCount = ${pageCount} handleNetworkFollowing")
            dataKey = data.arg1
            Logger.e("pageCount = " + pageCount)
            var itemSize = mAdapter!!.itemCount
            var dataSize = 0
            if ( result != null && result!!.data != null )
                dataSize = result.data.size
            Logger.e("itemSize :: $itemSize and dataSize $dataSize")
            var tSize = itemSize + dataSize
            Logger.e("tSize :: $tSize")
            if (pageCount == 1 && result.nTotalCount <= 10) {
                if ( result.nTotalCount > 0 ) {
                    recyclerViewFollow.visibility = View.VISIBLE
                    layoutEmpty.visibility = View.GONE
                }
                mAdapter?.setIsFooter(true)
            } else if ( pageCount == 1 && result.nTotalCount > 10 ) {
                recyclerViewFollow.visibility = View.VISIBLE
                layoutEmpty.visibility = View.GONE
                mAdapter?.setIsFooter(false)
            } else {
                if ( tSize <= 10 ) {
                    if ( tSize > 0 ) {
                        recyclerViewFollow.visibility = View.VISIBLE
                        layoutEmpty.visibility = View.GONE
                    }
                    mAdapter?.setIsFooter(true)
                } else {
                    recyclerViewFollow.visibility = View.VISIBLE
                    layoutEmpty.visibility = View.GONE
                    mAdapter?.setIsFooter(false)
                }
            }
            if (!isReload) {
                mAdapter?.setItems(result.data)
            } else {
                mAdapter?.addItems(result.data)
            }
            NetworkBus(NetworkApi.API113.name).let {
                EventBus.getDefault().post(it)
            }
        }
    }

    private fun handleNetworkNotFollowing(data: NetworkBusResponse) {
        progressBar.visibility = View.GONE
        if ("ok" == data.arg2) {
            val result: API113 = Gson().fromJson(DBManager.getInstance(context!!).get(data.arg1), API113::class.java)

            if (mUserCount > 0) {
                Logger.e("mUserCount over 0")
                if ( mAdapter != null && mAdapter!!.items != null && mAdapter !!.items.size > 0 ) {
                    recyclerViewFollow.visibility = View.VISIBLE
                    mAdapter?.setFooterItem(result.data)
                } else {
                    recyclerViewFollow.visibility = View.GONE
                    layoutEmpty.visibility = View.VISIBLE

                    recyclerViewRecommendUser.setHasFixedSize(true)
                    var manager = WrappedGridLayoutManager(context!!, RECOMMEND_USER_COLUMN).apply {
                        orientation = RecyclerView.VERTICAL
                        spanSizeLookup = object : androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup() {
                            override fun getSpanSize(position: Int): Int {
                                return if (position == 0) {
                                    RECOMMEND_USER_COLUMN
                                } else {
                                    1
                                }
                            }
                        }
                    }
                    recyclerViewRecommendUser!!.addOnScrollListener(object: RecyclerView.OnScrollListener() {
                        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                            Logger.e("dy :: " + dy)
                        }

                        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                            if(newState == RecyclerView.SCROLL_STATE_IDLE) {
                                if ( recyclerViewRecommendUser.canScrollVertically(1) ) {
                                    Logger.e("can not scroll swipable enable true")
                                    enableSwipeRefresh(true)
                                } else {
                                    Logger.e("can scroll")
                                }
                            } else {
                                Logger.e("swipable enable false")
                                enableSwipeRefresh(false)
                            }
                        }
                    })
                    recyclerViewRecommendUser.layoutManager = manager
                    mEmptyAdapter = RecommendUserAdapter(context!!, true).apply {
                        setListener(this@PuddingFollowingFragment)
                    }

                    recyclerViewRecommendUser.adapter = mEmptyAdapter
                    mEmptyAdapter?.setItems(result.data)
                }
            } else {
                Logger.e("mUserCount 0")
                recyclerViewRecommendUser.setHasFixedSize(true)
                var manager = WrappedGridLayoutManager(context!!, RECOMMEND_USER_COLUMN).apply {
                    orientation = RecyclerView.VERTICAL
                    spanSizeLookup = object : androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup() {
                        override fun getSpanSize(position: Int): Int {
                            return if (position == 0) {
                                RECOMMEND_USER_COLUMN
                            } else {
                                1
                            }
                        }
                    }
                }

                recyclerViewRecommendUser!!.addOnScrollListener(object: RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    }

                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        if(newState == RecyclerView.SCROLL_STATE_IDLE) {
                            if ( recyclerViewRecommendUser.canScrollVertically(1) ) {
                                enableSwipeRefresh(true)
                            }
                        } else {
                            enableSwipeRefresh(false)
                        }
                    }
                })
                recyclerViewRecommendUser.layoutManager = manager

                mEmptyAdapter = RecommendUserAdapter(context!!, true).apply {
                    setListener(this@PuddingFollowingFragment)
                }

                recyclerViewRecommendUser.adapter = mEmptyAdapter
                mEmptyAdapter?.setItems(result.data)
            }
        }
    }

    private fun enableSwipeRefresh(enable:Boolean) {
        refreshLayout.isEnabled = enable
    }

    private val scrollListener = object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (mIsVisibleToUser) {
                val lastVisibleItemPosition = mLayoutManager.findLastCompletelyVisibleItemPosition()
                val visibleCount = mLayoutManager.findFirstVisibleItemPosition()
                val totalItemCount = mAdapter!!.itemCount - 1

//                if (visibleCount > 8) {
//                    topBtn.visibility = View.VISIBLE
//                } else {
//                    topBtn.visibility = View.GONE
//                }

                if ( topBtn.visibility == View.GONE ) {
                    if ( visibleCount > 8 )
                        topBtn.visibility = View.VISIBLE
                }

                if ((lastVisibleItemPosition == totalItemCount) && !isEndOfData && dy > 0) {
                    isReload = true
                    ++pageCount

                    AppToast(context!!).showToastMessage("loading 중...",
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_BOTTOM)
                    Logger.e("pageCount 2 :: + $pageCount")
                    NetworkBus(NetworkApi.VOD0.name, "follow", pageCount.toString(), keyword).let {
                        EventBus.getDefault().post(it)
                    }
                }
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            var state = newState
            if ( state == RecyclerView.SCROLL_STATE_IDLE ) {
                Handler().postDelayed({
                    if ( state == RecyclerView.SCROLL_STATE_IDLE )
                        topBtn.visibility = View.GONE
                }, 1500)
            }
        }
    }

    private fun registerReceiver() {
        LocalBroadcastManager.getInstance(context!!).registerReceiver(receiver, IntentFilter("likeChange"))
    }

    private fun unregisterReceiver() {
        try {
            LocalBroadcastManager.getInstance(context!!).unregisterReceiver(receiver)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if ( action == "likeChange") {
                if ( mAdapter == null )
                    return
                else {
                    if ( mAdapter!!.items == null )
                        return
                    else {
                        if ( mAdapter!!.items!!.size <= 0 )
                            return
                    }
                }

                var obj = intent.getStringExtra("change")
                Logger.e("obj ::: " + obj)
                var arr = JSONArray(obj)
                var arrsize = arr.length()
                if ( arrsize > 0 ) {
                    var itemSize = mAdapter!!.getItemSize()
                    var lenth = arr.length()
                    for ( i in 0 until itemSize ) {
                        var item = mAdapter!!.items!!.get(i)
                        for ( j in 0 until lenth ) {
                            var modelObj = arr.optJSONObject(j)
                            if ( item.id == modelObj.optString("streamKey") ) {
                                var it = item
                                it.favoriteCount = modelObj.optString("cnt")
                                mAdapter!!.items!!.set(i, it)
                            }
                        }
                    }
                    mAdapter!!.notifyDataSetChanged()
                }
            }
        }
    }
}