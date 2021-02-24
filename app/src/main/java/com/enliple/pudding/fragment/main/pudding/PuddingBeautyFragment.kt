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
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import com.enliple.pudding.R
import com.enliple.pudding.activity.LinkWebViewActivity
import com.enliple.pudding.activity.ProductDetailActivity
import com.enliple.pudding.activity.ShoppingPlayerActivity
import com.enliple.pudding.adapter.home.BeautyAdapter
import com.enliple.pudding.common.VideoDataContainer
import com.enliple.pudding.commons.app.ShopTreeKey
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.NetworkApi
import com.enliple.pudding.commons.network.NetworkBus
import com.enliple.pudding.commons.network.NetworkBusResponse
import com.enliple.pudding.commons.network.NetworkHandler
import com.enliple.pudding.commons.network.vo.API114
import com.enliple.pudding.commons.network.vo.API139
import com.enliple.pudding.commons.network.vo.API98
import com.enliple.pudding.commons.network.vo.VOD
import com.enliple.pudding.commons.shoptree.network.ShopTreeAsyncTask
import com.enliple.pudding.commons.widget.recyclerview.WrappedLinearLayoutManager
import com.enliple.pudding.commons.widget.toast.AppToast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_pudding_beauty.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONArray
import org.json.JSONObject

/**
 * Created by Kim Joonsung on 2019-04-17.
 */
class PuddingBeautyFragment : androidx.fragment.app.Fragment() , BeautyAdapter.Listener {

    private lateinit var mAdapter:BeautyAdapter
    private lateinit var mLayoutManager: WrappedLinearLayoutManager

    private var pageCount = 1
    private var isReload = false
    private var isEndOfData = false
    private var dbKey = ""
    private var mIsVisibleToUser = false
    private var mIsFirst = true
    private var beautyOrder = "1"
    private var isOrderClick = false
    private var mCategoryId = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_pudding_beauty, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Logger.e("onViewCreated")
        EventBus.getDefault().register(this)
        registerReceiver()
        topBtn.setOnClickListener { recyclerViewBeauty.scrollToPosition(0) }

        recyclerViewBeauty.setHasFixedSize(false)
        recyclerViewBeauty.addOnScrollListener(scrollListener)

        mLayoutManager = WrappedLinearLayoutManager(context!!).apply {
            orientation = RecyclerView.VERTICAL
        }

        recyclerViewBeauty.layoutManager = mLayoutManager

        mAdapter = BeautyAdapter(context!!).apply {
            setListener(this@PuddingBeautyFragment)
        }

        recyclerViewBeauty.adapter = mAdapter
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        Logger.i("setUserVisibleHint: $isVisibleToUser")

        mIsVisibleToUser = isVisibleToUser

        if(isVisibleToUser) {
            if(mIsFirst) {
                Logger.e("first beauty")
//                mIsFirst = true // 최초 한번만 가져온다.
                NetworkBus(NetworkApi.API81.name, "select_union", "", "beauty").let {
                    EventBus.getDefault().post(it)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Logger.e("onResume")

        if(!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }

        if ( !mIsFirst )
            refresh()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        unregisterReceiver()
    }

    override fun onCategoryClick(categoryId: String) {
        Logger.d("onCategoryClick id:$categoryId")
        isReload = false
        pageCount = 1
        mCategoryId = categoryId

        mAdapter.setCategoryClick(true)

        if("all" == categoryId) {
            EventBus.getDefault().post(NetworkBus(NetworkApi.API114.name, "beauty", "1", "", beautyOrder))
        } else {
            EventBus.getDefault().post(NetworkBus(NetworkApi.API114.name, "beauty", "1", categoryId, beautyOrder))
        }
    }

    override fun onItemClicked(item: API114.VideoItem, position: Int) {
        Logger.e("URL : ${item.stream}")

        // 페이징 및 dbKey 포지션 문제 때문에 앞으로 아래와 같이 List 객체를 만들어서 영상 데이터 사용
        val json = Gson().toJson(mAdapter.mItems)
        val videoItems : MutableList<VOD.DataBeanX> = Gson().fromJson(json, object : TypeToken<MutableList<VOD.DataBeanX>>(){}.type)
        VideoDataContainer.getInstance().mVideoData = videoItems

        if("LIVE" == item.videoType) {
            val task = ShopTreeAsyncTask(context!!)
            task.getLiveInfo(item.id, { result, obj ->
                try {
                    val response = Gson().fromJson(obj.toString(), API98::class.java)
                    if(response.data.size > 0) {
                        if("Y" == response.data[0].isOnAir) {
                            startActivity(Intent(context!!, ShoppingPlayerActivity::class.java).apply {
                                putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_POSITION, position)
                                putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_CASTER_ID, response.data[0].userId)

                                data = Uri.parse("vcommerce://shopping?url=${response.data[0].stream}")
                            })
                        } else {
                            AppToast(context!!).showToastMessage("종료된 라이브 방송입니다.",
                                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                                    AppToast.GRAVITY_BOTTOM)
                        }
                    } else {
                        AppToast(context!!).showToastMessage("종료된 라이브 방송입니다.",
                                AppToast.DURATION_MILLISECONDS_DEFAULT,
                                AppToast.GRAVITY_BOTTOM)
                    }
                } catch (e : java.lang.Exception) {
                    Logger.p(e)
                }
            })
        } else {
            if (!TextUtils.isEmpty(item.stream)) {
                startActivity(Intent(context!!, ShoppingPlayerActivity::class.java).apply {
                    putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_POSITION, position)
                    putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_CASTER_ID, item.userId)

                    data = Uri.parse("vcommerce://shopping?url=${item.stream}")
                })
            } else {
                AppToast(context!!).showToastMessage("방송 정보 메타 데이터가 존재하지 않습니다.",
                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                        AppToast.GRAVITY_BOTTOM)
            }
        }
    }

    override fun onPagerClicked(item: API114.EventItem) {
        if("1" == item.ev_strType) {
            startActivity(Intent(context!!, ProductDetailActivity::class.java).apply {
                putExtra("from_live", false)
                putExtra("from_store", false)
                putExtra("idx", item.ev_it_id)
                putExtra(ShopTreeKey.KEY_STREAM_KEY, "")
                putExtra(ShopTreeKey.KEY_VOD_TYPE, "")
                putExtra(ShopTreeKey.KEY_RECOMMEND_ID, "")
            })
        } else {
            ShopTreeAsyncTask(context!!).getDRCLink(item.ev_it_id, "", "", item.ev_strType,
                    { result, obj ->
                        try {
                            val response: API139 = Gson().fromJson(obj.toString(), API139::class.java)
                            if(response.result == "success") {
                                startActivity(Intent(context!!, LinkWebViewActivity::class.java).apply {
                                    putExtra(LinkWebViewActivity.INTENT_EXTRA_KEY_LINK, response.url)
                                    putExtra("TITLE", item.ev_title)
                                    putExtra("IDX", item.ev_it_id)
                                    putExtra("TYPE", item.ev_strType)
                                    putExtra("IS_WISH", item.ev_is_wish)
                                })
                            } else {
                                AppToast(context!!).showToastMessage("잘못된 링크 입니다.",
                                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                                        AppToast.GRAVITY_BOTTOM)
                            }
                        } catch (e:Exception) {
                            Logger.p(e)
                        }
                    })
        }
    }

    override fun onPopularityOrderClicked(order: String) {
        isOrderClick = true
        beautyOrder = order

        EventBus.getDefault().post(NetworkBus(NetworkApi.API114.name, "beauty", "1", mCategoryId, order))
    }

    override fun onRecentOrderClicked(order: String) {
        isOrderClick = true
        beautyOrder = order

        EventBus.getDefault().post(NetworkBus(NetworkApi.API114.name, "beauty", "1", mCategoryId, order))
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data:NetworkBusResponse) {
        if(data.arg1.startsWith("POST/products/") && data.arg1.endsWith("wish")) {
            var arr = data.arg1.split("/")
            var s_idx = arr[2]
            Logger.e("s_idx")
            if ( mAdapter != null ) {
                mAdapter.changeZzim(s_idx)
            }
        } else {
            if(mIsVisibleToUser && (parentFragment as PuddingHomeTabFragment).userVisibleHint) {
                val api81 = NetworkHandler.getInstance(activity!!).getKey(NetworkApi.API81.toString(), "select_union", "", "beauty")
                if(data.arg1.startsWith("GET/mui/main/beauty?page=")) {
                    handleNetworkResult(data)
                } else if(data.arg1.startsWith(api81)) {
                    try {
                        var categoryStr = DBManager.getInstance(getActivity()).get(data.arg1);
                        val `object` = JSONObject(categoryStr)
                        val array = `object`.optJSONArray("data")
                        val firstData = array.optJSONObject(0)
                        mCategoryId = firstData.optString("categoryId")
                        Logger.e("mCategoryId :: $mCategoryId")
                        if ( mAdapter != null )
                            mAdapter.setFirstCategory(mCategoryId)
                        refresh()
                    } catch (e: Exception ) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    @Subscribe
    fun onMessageEvent(data: String) {
        if (data.startsWith("refresh")) {
            refresh()
        }
    }

    private fun refresh() {
        if(mIsVisibleToUser && (parentFragment as PuddingHomeTabFragment)?.userVisibleHint) {
            Logger.d("refresh")
            isReload = false
            pageCount = 1
            progressBar.visibility = View.VISIBLE
            Logger.e("mCategoryId :: " + mCategoryId)
            EventBus.getDefault().post(NetworkBus(NetworkApi.API114.name, "beauty", "1", mCategoryId, beautyOrder))
        }
    }

    private fun handleNetworkResult(data: NetworkBusResponse) {
        progressBar.visibility = View.GONE
        if("ok" == data.arg2) {
            val response: API114 = Gson().fromJson(DBManager.getInstance(context!!).get(data.arg1), API114::class.java)

            isEndOfData = response.data.size < response.nDataCount

            pageCount = response.pageCount.toInt()
            dbKey = data.arg1

            if(!isReload) {
                mAdapter.setEventItems(response.event)

                if(!isOrderClick) {
                    mAdapter.setItems(response.data, isOrderClick)
                } else {
                    mAdapter.setItems(response.data, isOrderClick)
                    isOrderClick = false
                }
            } else {
                mAdapter.addItems(response.data)

                // 하단으로 scroll 필요
                recyclerViewBeauty?.scrollBy(0, 200)

                AppToast.cancelAllToast()
            }
        } else {
            Logger.e("${data.arg3} ${data.arg4}")
        }
        mIsFirst = false
    }

    private val scrollListener = object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if(mIsVisibleToUser) {
                val lastVisibleItemPosition = mLayoutManager.findLastCompletelyVisibleItemPosition()
                val visibleCount = mLayoutManager.findFirstVisibleItemPosition()
                val totalItemCount = mAdapter.itemCount - 1

//                if(visibleCount > 8) {
//                    topBtn.visibility = View.VISIBLE
//                } else {
//                    topBtn.visibility = View.GONE
//                }

                if ( topBtn.visibility == View.GONE ) {
                    if ( visibleCount > 8 )
                        topBtn.visibility = View.VISIBLE
                    else
                        topBtn.visibility = View.GONE
                }

                if((lastVisibleItemPosition == totalItemCount) && !isEndOfData) {
                    isReload = true
                    ++pageCount

                    AppToast(context!!).showToastMessage("loading 중...",
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_BOTTOM)

                    EventBus.getDefault().post(NetworkBus(NetworkApi.API114.name, "beauty", pageCount.toString(), mCategoryId, beautyOrder))
                }
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            // scroll 중에 swiperefresh 기능을 off 해야 정상적으로 swipe 된다.
            if (newState == androidx.viewpager.widget.ViewPager.SCROLL_STATE_IDLE) {
                (parentFragment as PuddingHomeTabFragment).enableSwipeRefresh(true)
            } else {
                (parentFragment as PuddingHomeTabFragment).enableSwipeRefresh(false)
            }

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
        LocalBroadcastManager.getInstance(activity!!).registerReceiver(receiver, IntentFilter("PAGE_CHANGED"))
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
                    if ( mAdapter.mItems == null )
                        return
                    else {
                        if ( mAdapter.mItems.size <= 0 )
                            return
                    }
                }

                var obj = intent.getStringExtra("change")
                Logger.e("obj ::: " + obj)
                var arr = JSONArray(obj)
                var arrsize = arr.length()
                if ( arrsize > 0 ) {
                    var itemSize = mAdapter.getItemSize()
                    var lenth = arr.length()
                    for ( i in 0 until itemSize ) {
                        var item = mAdapter.mItems.get(i)
                        for ( j in 0 until lenth ) {
                            var modelObj = arr.optJSONObject(j)
                            if ( item.id == modelObj.optString("streamKey") ) {
                                var it = item
                                it.favoriteCount = modelObj.optString("cnt")
                                mAdapter.mItems.set(i, it)
                            }
                        }
                    }
                    mAdapter.notifyDataSetChanged()
                }
            } else if (action == "PAGE_CHANGED") {
                var page = intent.getIntExtra("page", -1)
                var gubun = intent.getStringExtra("gubun")
                if ( gubun == "run") {
                    if ( page == PuddingHomeTabFragment.PAGE_BEAUTY && ::mAdapter.isInitialized ) {
                        mAdapter!!.runTimer()
                    }
                } else if ( gubun == "cancel") {
                    mAdapter!!.cancelTimer()
                } else if ( gubun == "paging") {
                    if (::mAdapter.isInitialized ) {
                        if ( page == PuddingHomeTabFragment.PAGE_BEAUTY ) {
                            mAdapter!!.runTimer()
                        } else {
                            mAdapter!!.cancelTimer()
                        }
                    }
                }
            }
        }
    }
}