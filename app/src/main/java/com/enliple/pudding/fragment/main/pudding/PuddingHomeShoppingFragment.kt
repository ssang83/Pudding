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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.enliple.pudding.R
import com.enliple.pudding.activity.ShoppingPlayerActivity
import com.enliple.pudding.adapter.home.HomeVideoAdapter
import com.enliple.pudding.adapter.home.HotAdapter
import com.enliple.pudding.adapter.home.HotShoppingAdapter
import com.enliple.pudding.common.VideoDataContainer
import com.enliple.pudding.commons.app.CategoryModel
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.NetworkApi
import com.enliple.pudding.commons.network.NetworkBus
import com.enliple.pudding.commons.network.NetworkBusResponse
import com.enliple.pudding.commons.network.NetworkHandler
import com.enliple.pudding.commons.network.vo.*
import com.enliple.pudding.commons.shoptree.network.ShopTreeAsyncTask
import com.enliple.pudding.commons.widget.toast.AppToast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_pudding_live.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONArray

/**
 * Created by Kim Joonsung on 2019-01-24.
 */
class PuddingHomeShoppingFragment : androidx.fragment.app.Fragment(), HotShoppingAdapter.Listener, HomeVideoAdapter.Listener {

    private lateinit var mHomeAdapter: HomeVideoAdapter

    private lateinit var mLayoutManager: LinearLayoutManager
    private var homeOrder = "1"
    private var mIsVisibleToUser = false
    private var mCategoryList: MutableList<API81.CategoryItem> = mutableListOf()
    private var isReload = false
    private var pageCount = 1
    private var isOrderClick = false
    private var mCategoryId = ""
    private var isEndOfData = false
    private var mIsFirst = false
    private var mPosition = -1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_pudding_live, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Logger.i("onViewCreated")

        EventBus.getDefault().register(this)
        registerReceiver()
        topBtn.setOnClickListener { recyclerViewMain.scrollToPosition(0) }

        recyclerViewMain.setHasFixedSize(true)
        recyclerViewMain.addOnScrollListener(scrollListener)
        mLayoutManager = LinearLayoutManager(context).apply {
            orientation = RecyclerView.VERTICAL
        }

        recyclerViewMain.layoutManager = mLayoutManager

        mHomeAdapter = HomeVideoAdapter(context!!).apply {
            setListener(this@PuddingHomeShoppingFragment)
            setHotListener(this@PuddingHomeShoppingFragment)
        }

        recyclerViewMain.adapter = mHomeAdapter
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        Logger.i("setUserVisibleHint: $isVisibleToUser")

        mIsVisibleToUser = isVisibleToUser

        if (context != null && mIsVisibleToUser) {
            if (!mIsFirst) {
                mIsFirst = true  // 최초 한번만 가져온다.
                refresh()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Logger.d("onResume")

        if(!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }

        refresh()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        unregisterReceiver()
    }

    override fun onCategoryClick(categoryId: String) {
        Logger.e("onCategoryClicked id:$categoryId")
        isReload = false
        pageCount = 1
        mCategoryId = categoryId

        mHomeAdapter.setCategoryClick(true)

        if ("all" == categoryId) {
            EventBus.getDefault().post(NetworkBus(NetworkApi.API114.name, "shopping", "1", "", homeOrder))
        } else {
            EventBus.getDefault().post(NetworkBus(NetworkApi.API114.name, "shopping", "1", categoryId, homeOrder))
        }
    }

    override fun onHotItemClicked(item: API114.HotDataItem, position: Int) {
        Logger.e("URL : ${item.stream}")
        mPosition = position

        // 페이징 및 dbKey 포지션 문제 때문에 앞으로 아래와 같이 List 객체를 만들어서 영상 데이터 사용 예정
        val json = Gson().toJson(mHomeAdapter.hotItems)
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
                                putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_POSITION, mPosition)
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
                } catch (e : Exception) {
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

//    override fun onCategoryClicked(categoryId: String) {
//        Logger.e("onCategoryClicked id:$categoryId")
//        isReload = false
//        pageCount = 1
//        mCategoryId = categoryId
//
//        mHomeAdapter.setCategoryClick(true)
//
//        if ("all" == categoryId) {
//            EventBus.getDefault().post(NetworkBus(NetworkApi.API114.name, "shopping", "1", "", homeOrder))
//        } else {
//            EventBus.getDefault().post(NetworkBus(NetworkApi.API114.name, "shopping", "1", categoryId, homeOrder))
//        }
//    }

    override fun onItemClicked(item: API114.VideoItem, position: Int) {
        Logger.e("URL : ${item.stream}")
        mPosition = position

        // 페이징 및 dbKey 포지션 문제 때문에 앞으로 아래와 같이 List 객체를 만들어서 영상 데이터 사용
        val json = Gson().toJson(mHomeAdapter.items)
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
                                putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_POSITION, mPosition)
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
                } catch (e : Exception) {
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

    override fun onRecentOrderClicked(order: String) {
        isOrderClick = true
        homeOrder = order
        EventBus.getDefault().post(NetworkBus(NetworkApi.API114.name, "shopping", "1", mCategoryId, order))
    }

    override fun onPopularityOrderClicked(order: String) {
        isOrderClick = true
        homeOrder = order
        EventBus.getDefault().post(NetworkBus(NetworkApi.API114.name, "shopping", "1", mCategoryId, order))
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: NetworkBusResponse) {
        if(data.arg1.startsWith("POST/products/") && data.arg1.endsWith("wish")) {
            var arr = data.arg1.split("/")
            var s_idx = arr[2]
            if ( mHomeAdapter != null ) {
                mHomeAdapter.changeZzim(s_idx)
            }
        } else {
            if(mIsVisibleToUser && (parentFragment as PuddingHomeTabFragment).userVisibleHint) {
                if (data.arg1.startsWith("GET/mui/main/shopping?page=")) {
                    handleNetworkLiveResult(data)
                } else if (data.arg1.equals("GET/mui/category/select_union?sc_code=")) {
                    initCategory(data)
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

    // api 갱신은 여기서 하자
    private fun refresh() {
        if (mIsVisibleToUser && (parentFragment as PuddingHomeTabFragment)?.userVisibleHint) {
            Logger.e("refresh")

            progressBar.visibility = View.VISIBLE
//            homeOrder = ""
            isReload = false
            pageCount = 1

            EventBus.getDefault().post(NetworkBus(NetworkApi.API81.name, "select_union", ""))
        }
    }

    private fun initCategory(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            NetworkBus(NetworkApi.API114.name, "shopping", "1", mCategoryId, homeOrder).let {
                EventBus.getDefault().post(it)
            }
        } else {
            Logger.e("error : ${data.arg3}, ${data.arg4}")
        }
    }

    private fun handleNetworkLiveResult(data: NetworkBusResponse) {
        progressBar.visibility = View.GONE
        if ("ok" == data.arg2) {
            val response: API114 = Gson().fromJson(DBManager.getInstance(context!!).get(data.arg1), API114::class.java)

            mHomeAdapter.setTotalItemCount(response.nTotalCount)

            isEndOfData = response.data.size < response.nDataCount

            // get category list ( all category 인 경우만 갱신한다.. )
            if (!isReload) {
                if (!isOrderClick) {
                    val key = NetworkHandler.getInstance(context!!).getKey(NetworkApi.API81.toString(), "select_union", "")
                    Logger.e("key ::: " + key)
                    val str = DBManager.getInstance(activity).get(key)
                    mCategoryList = CategoryModel.getCategoryList(context!!, "select_union")
                    Logger.e("str :: " + str)
                    mHomeAdapter.setCategoryItems(mCategoryList, str)

                    if (response.nHotTotalCount > 0) {
                        mHomeAdapter.setHotItems(response.hotData)
                    }

                    mHomeAdapter.setItems(response.data, isOrderClick)
                } else {
                    mHomeAdapter.setItems(response.data, isOrderClick)
                    isOrderClick = false
                }
            } else {
                mHomeAdapter.addItems(response.data)

                // 하단으로 scroll 필요
                recyclerViewMain?.scrollBy(0, 200)

                AppToast.cancelAllToast()
            }
        } else {
            val error: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
            Logger.e("error : $error")
        }
    }

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (mIsVisibleToUser) {
                val lastVisibleItemPosition = mLayoutManager.findLastCompletelyVisibleItemPosition()
                val visibleCount = mLayoutManager.findFirstVisibleItemPosition()
                val totalItemCount = mHomeAdapter.itemCount - 1

//                if (visibleCount > 8) {
//                    topBtn.visibility = View.VISIBLE
//                } else {
//                    topBtn.visibility = View.GONE
//                }

                if ( topBtn.visibility == View.GONE ) {
                    if ( visibleCount > 8 )
                        topBtn.visibility = View.VISIBLE
                }

                if ((lastVisibleItemPosition == totalItemCount) && !isEndOfData) {
                    isReload = true
                    ++pageCount

                    AppToast(context!!).showToastMessage("loading 중...",
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_BOTTOM)

                    NetworkBus(NetworkApi.API114.name, "shopping", pageCount.toString(), mCategoryId, homeOrder).let {
                        EventBus.getDefault().post(it)
                    }
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
                if ( mHomeAdapter == null )
                    return
                else {
                    if ( mHomeAdapter.getItemSize() <= 0 || mHomeAdapter.getHotItemSize() <= 0 )
                        return
                }

                var obj = intent.getStringExtra("change")
                Logger.e("obj ::: " + obj)
                var arr = JSONArray(obj)
                var arrsize = arr.length()
                if ( arrsize > 0 ) {
                    var itemSize = mHomeAdapter.getItemSize()
                    var hotItemSize = mHomeAdapter.getHotItemSize()
                    var lenth = arr.length()

                    for ( i in 0 until itemSize ) {
                        var item = mHomeAdapter.items.get(i)
                        for ( j in 0 until lenth ) {
                            var modelObj = arr.optJSONObject(j)
                            if ( item.id == modelObj.optString("streamKey") ) {
                                var it = item
                                it.favoriteCount = modelObj.optString("cnt")
                                mHomeAdapter.items.set(i, it)
                            }
                        }
                    }

                    for ( i in 0 until hotItemSize ) {
                        var item = mHomeAdapter.hotItems.get(i)
                        for ( j in 0 until lenth ) {
                            var modelObj = arr.optJSONObject(j)
                            if ( item.id == modelObj.optString("streamKey") ) {
                                var it = item
                                it.favoriteCount = modelObj.optString("cnt")
                                mHomeAdapter.hotItems.set(i, it)
                            }
                        }
                    }

                    mHomeAdapter.notifyDataSetChanged()
                }
            }
        }
    }
}