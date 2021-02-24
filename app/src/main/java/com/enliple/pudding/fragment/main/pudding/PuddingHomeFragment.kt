package com.enliple.pudding.fragment.main.pudding

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.enliple.pudding.R
import com.enliple.pudding.activity.MainActivity
import com.enliple.pudding.activity.ShoppingPlayerActivity
import com.enliple.pudding.adapter.home.HomeAdapter
import com.enliple.pudding.adapter.home.HotAdapter
import com.enliple.pudding.adapter.home.MDPickAdapter
import com.enliple.pudding.bus.ViewCountBus
import com.enliple.pudding.common.VideoDataContainer
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.NetworkApi
import com.enliple.pudding.commons.network.NetworkBus
import com.enliple.pudding.commons.network.NetworkBusResponse
import com.enliple.pudding.commons.network.vo.API114
import com.enliple.pudding.commons.network.vo.API98
import com.enliple.pudding.commons.network.vo.BaseAPI
import com.enliple.pudding.commons.network.vo.VOD
import com.enliple.pudding.commons.shoptree.network.ShopTreeAsyncTask
import com.enliple.pudding.commons.widget.toast.AppToast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_pudding_home.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONArray
import java.lang.ref.WeakReference

/**
 * Main Home Tab
 */
class PuddingHomeFragment : Fragment(), HotAdapter.Listener, HomeAdapter.Listener, MDPickAdapter.Listener {
    companion object {
        private const val MESSAGE_REFRESH_DATA = 1000

        private const val RECYCLER_VIEW_ITEM_CACHE_SIZE = 20
        private const val REQUEST_GO_PLAYER_HOT = 21321
        private const val REQUEST_GO_PLAYER_ITEM = 21322
        private const val REQUEST_GO_PLAYER_MDPICK = 21323
        //private const val NUMBER_COLUMNS = 2
    }

    private lateinit var mHomeAdapter: HomeAdapter
    //private var mBannerHolder: HomeAdapter.PuddingHomeBannerHolder? = null
    private lateinit var mLayoutManager: LinearLayoutManager

    private var mIsVisibleToUser = false
    private var mIsCreated = true

    // Handler 메모리 릭 발생하니깐 항상 아래 처럼 만들자..
    private var mHandler: MyHandler? = null
    private var mVideoType = ""
    private var visibleItem = -1
    private class MyHandler(fragment: PuddingHomeFragment) : Handler() {
        private val mReference: WeakReference<PuddingHomeFragment> = WeakReference(fragment)

        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MESSAGE_REFRESH_DATA -> {
                    Logger.e("handleMessage")
                    mReference.get()?.refresh()
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_pudding_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Logger.i("onViewCreated")

        EventBus.getDefault().register(this)
        registerReceiver()
        topBtn.setOnClickListener { recyclerViewMain.scrollToPosition(0) }

        recyclerViewMain.addOnScrollListener(scrollListener)
        recyclerViewMain.setItemViewCacheSize(RECYCLER_VIEW_ITEM_CACHE_SIZE)

        mLayoutManager = LinearLayoutManager(context!!).apply {
            orientation = RecyclerView.VERTICAL
        }
        recyclerViewMain.layoutManager = mLayoutManager

        // Fatal Exception: java.lang.IllegalArgumentException
        //No view found for id 0x7f0a007e (com.enliple.pudding:id/bestViewPager) issue 발생으로 변경
        // fragmentmanager -> childFragmentManager -> activity.fragmentManager
        mHomeAdapter = HomeAdapter(context!!, activity!!.supportFragmentManager).apply {
            setListener(this@PuddingHomeFragment)
            setHotListener(this@PuddingHomeFragment)
            setMdPickListener(this@PuddingHomeFragment)
            setParentFragment(parentFragment as PuddingHomeTabFragment)
        }
        mHomeAdapter.setActivity(activity!!)
        recyclerViewMain.adapter = mHomeAdapter

        mIsCreated = true
//        mHandler = MyHandler(this)
//        mHandler?.sendEmptyMessageDelayed(MESSAGE_REFRESH_DATA, 500)
//        mHandler?.sendEmptyMessage(MESSAGE_REFRESH_DATA)
        refresh()
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        Logger.i("setUserVisibleHint: $isVisibleToUser")

        mIsVisibleToUser = isVisibleToUser

        if (context != null && mIsVisibleToUser) {
            //mHandler?.sendEmptyMessage(MESSAGE_REFRESH_DATA)
            if (!mIsCreated) {
                EventBus.getDefault().post("home_video_play")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Logger.d("onResume")

        if(!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }

        // 처음 시작 시에는 onResume 에서 refresh 하지 않는다.
        if (mIsCreated) {
            mIsCreated = false
        } else {
//            mHandler?.sendEmptyMessageDelayed(MESSAGE_REFRESH_DATA, 100)  // 아이폰이랑 동일하게 영상에서 백키로 리스트로 돌아왔을 경우 refresh 하지 않는다.
        }
    }

    override fun onPause() {
        super.onPause()
        Logger.d("onPause")
    }

    override fun onStop() {
        super.onStop()
        Logger.d("onStop")

//        if (mBannerHolder != null) {
//            mHomeAdapter.stopBanner(mBannerHolder!!)
//        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        unregisterReceiver()
    }

    override fun onHotItemClicked(item: API114.BestDataItem, position: Int) {
        Logger.e("URL : ${item.stream}")
        mVideoType = item.videoType

        // 페이징 및 dbKey 포지션 문제 때문에 앞으로 아래와 같이 List 객체를 만들어서 영상 데이터 사용
        val json = Gson().toJson(mHomeAdapter.bestItems)
        val videoItems: MutableList<VOD.DataBeanX> = Gson().fromJson(json, object : TypeToken<MutableList<VOD.DataBeanX>>() {}.type)
        VideoDataContainer.getInstance().mVideoData = videoItems

//        val json1 = Gson().toJson(VideoDataContainer.getInstance().mVideoData)
//        val videoItems1: MutableList<API114.HotDataItem> = Gson().fromJson(json, object : TypeToken<MutableList<API114.HotDataItem>>() {}.type)


        if(item.videoType == "LIVE") {
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
                } catch (e : Exception) {
                    Logger.p(e)
                }
            })
        } else {
            if (!TextUtils.isEmpty(item.stream)) {
                startActivityForResult(Intent(context!!, ShoppingPlayerActivity::class.java).apply {
                    putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_POSITION, position)
                    putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_CASTER_ID, item.userId)

                    data = Uri.parse("vcommerce://shopping?url=${item.stream}")
                }, REQUEST_GO_PLAYER_HOT)
            } else {
                AppToast(context!!).showToastMessage("방송 정보 메타 데이터가 존재하지 않습니다.",
                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                        AppToast.GRAVITY_BOTTOM)
            }
        }
    }

    override fun onItemClicked(item: API114.VideoItem, position: Int) {
        Logger.e("URL : ${item.stream} position:$position")
        mVideoType = item.videoType

        // 페이징 및 dbKey 포지션 문제 때문에 앞으로 아래와 같이 List 객체를 만들어서 영상 데이터 사용
        val json = Gson().toJson(mHomeAdapter.items)
        val videoItems: MutableList<VOD.DataBeanX> = Gson().fromJson(json, object : TypeToken<MutableList<VOD.DataBeanX>>() {}.type)
        VideoDataContainer.getInstance().mVideoData = videoItems

        if (!TextUtils.isEmpty(item.stream)) {
            startActivityForResult(Intent(context!!, ShoppingPlayerActivity::class.java).apply {
                putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_POSITION, position)
                putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_CASTER_ID, item.userId)

                data = Uri.parse("vcommerce://shopping?url=${item.stream}")
            }, REQUEST_GO_PLAYER_ITEM)
        } else {
            AppToast(context!!).showToastMessage("방송 정보 메타 데이터가 존재하지 않습니다.",
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM)
        }
    }

    override fun onMDPickClicked(item: API114.MdPickItem.EvPickBean, position: Int) {
        Logger.e("URL : ${item.stream}")

        // 페이징 및 dbKey 포지션 문제 때문에 앞으로 아래와 같이 List 객체를 만들어서 영상 데이터 사용
        val json = Gson().toJson(mHomeAdapter.mdPickItems)
        val videoItems: MutableList<VOD.DataBeanX> = Gson().fromJson(json, object : TypeToken<MutableList<VOD.DataBeanX>>() {}.type)
        VideoDataContainer.getInstance().mVideoData = videoItems

        if (!TextUtils.isEmpty(item.stream)) {
            startActivityForResult(Intent(context!!, ShoppingPlayerActivity::class.java).apply {
                putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_POSITION, position - 1)
                putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_CASTER_ID, item.userId)

                data = Uri.parse("vcommerce://shopping?url=${item.stream}")
            }, REQUEST_GO_PLAYER_MDPICK)
        } else {
            AppToast(context!!).showToastMessage("방송 정보 메타 데이터가 존재하지 않습니다.",
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM)
        }
    }

//    override fun onBannerHolder(holder: HomeAdapter.PuddingHomeBannerHolder) {
//        mBannerHolder = holder
//    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data:ViewCountBus) {
        mHomeAdapter.setViewCount(data.streamKey, data.count)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: NetworkBusResponse) {
        if(data.arg1.startsWith("POST/products/") && data.arg1.endsWith("wish")) {
            var arr = data.arg1.split("/")
            var s_idx = arr[2]
            Logger.e("s_idx")
            if ( mHomeAdapter != null ) {
                mHomeAdapter.changeZzim(s_idx)
            }
        } else {
            if(mIsVisibleToUser && (parentFragment as PuddingHomeTabFragment).userVisibleHint) {
                if (data.arg1.startsWith("GET/mui/main/top?page=")) {
                    handleNetworkHomeResult(data)
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
            mHandler?.removeMessages(MESSAGE_REFRESH_DATA)
            EventBus.getDefault().post(NetworkBus(NetworkApi.API114.name, "top", "1"))
        }
    }

    private fun handleNetworkHomeResult(data: NetworkBusResponse) {
        Logger.e("handleNetworkHomeResult")
        progressBar.visibility = View.GONE
        if (data.arg2 == "ok") {
            val response: API114 = Gson().fromJson(DBManager.getInstance(context!!).get(data.arg1), API114::class.java)
            // Banner Item
            if (response.banner.size > 0) {
                //mHomeAdapter.setBannerItem(response.banner)
            }

            // best Item
            if (response.nBestTotalCount > 0) {
                mHomeAdapter.setBestItems(response.bestData)
            }

            // Hot Item
            if (response.nHotTotalCount > 0) {
                mHomeAdapter.setHotItems(response.hotData)
                VideoDataContainer.getInstance().mHomeHotData = response.hotData
            }

            // MDPick Item
            if (response.mdPick != null) {
                mHomeAdapter.setMdPickInfo(true, response.mdPick.evTitle, response.mdPick.mainImg)
                mHomeAdapter.setMdPickItems(response.mdPick.evPick)
            } else {
                mHomeAdapter.setMdPickInfo(false, "", "")
            }

            if (response.nTotalCount > 0) {
                mHomeAdapter.setItems(response.data)
            }
        } else {
            val error = Gson().fromJson(data.arg4, BaseAPI::class.java)
            Logger.e("error : $error")
        }

        recyclerViewMain?.visibility = View.VISIBLE

        // home Data 를 가져오면 MainActivity Splash 화면 제거
//        mHandler?.post({ EventBus.getDefault().post("FINISH_MAIN_LOADING") })
        EventBus.getDefault().post("FINISH_MAIN_LOADING")
    }

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (mIsVisibleToUser) {

                val visibleCount = mLayoutManager.findFirstVisibleItemPosition()
//                if (visibleCount > 8) {
//                    topBtn.visibility = View.VISIBLE
//                } else {
//                    topBtn.visibility = View.GONE
//                }

                if ( topBtn.visibility == View.GONE ) {
                    if ( visibleCount > 8 )
                        topBtn.visibility = View.VISIBLE
                }
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            var state = newState
            if ( state == RecyclerView.SCROLL_STATE_IDLE ) {
                var visiblePosition = mLayoutManager.findFirstCompletelyVisibleItemPosition() // 완전히 보이는 것이 없으면 -1 return

                if ( visiblePosition >= 0 ) {
                    if ( visibleItem != visiblePosition ) {
                        visibleItem = visiblePosition
                        Logger.e("visibleItem :: " + visibleItem)
                        if ( mHomeAdapter != null )
                            mHomeAdapter.playVideo(visibleItem)
                    }
                }
                Handler().postDelayed({
                    if ( state == RecyclerView.SCROLL_STATE_IDLE )
                        topBtn.visibility = View.GONE
                }, 1500)
            }
        }
    }

    private fun registerReceiver() {
        LocalBroadcastManager.getInstance(context!!).registerReceiver(receiver, IntentFilter("likeChange"))
        LocalBroadcastManager.getInstance(context!!).registerReceiver(receiver, IntentFilter("closeFloatingView"))
        LocalBroadcastManager.getInstance(activity!!).registerReceiver(receiver, IntentFilter("PAGE_CHANGED"))
    }

    private fun unregisterReceiver() {
        try {
            LocalBroadcastManager.getInstance(context!!).unregisterReceiver(receiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            Logger.e("receiver action :: " + action)
            if ( action == "likeChange") {
                var obj = intent.getStringExtra("change")
                Logger.e("obj ::: " + obj)
                var arr = JSONArray(obj)
                var arrsize = arr.length()
                if ( arrsize > 0 ) {
                    val bestSize = mHomeAdapter.bestItems.size
                    val itemSize = mHomeAdapter.items.size
                    val hotSize = VideoDataContainer.getInstance().mHomeHotData.size

                    for ( i in 0 until hotSize ) {
                        var item = VideoDataContainer.getInstance().mHomeHotData[i]
                        Logger.d("item. title ::: " + item.title )
                        Logger.d("item. favorite count :: " + item.favoriteCount)
                        var hotStreamKey = item.id
                        for ( j in 0 until arrsize ) {
                            Logger.e("Hot data exist")
                            var modelObj = arr.optJSONObject(j)
                            if ( hotStreamKey == modelObj.optString("streamKey") ) {
                                item.favoriteCount = modelObj.optString("cnt")
                                VideoDataContainer.getInstance().mHomeHotData.set(i, item)
                            }
                        }
                    }

/*                    var dbKey = "GET/mui/main/top?page=1&category=&age=all&sex=all&user=${AppPreferences.getUserId(context!!)}&order="
                    val json = DBManager.getInstance(context).get(dbKey)
                    if (!TextUtils.isEmpty(json)) {
                        var homeList = Gson().fromJson(json, API114::class.java)
                        if ( homeList != null ) {
                            var hotData = homeList!!.hotData.size
                            for ( i in 0 until hotData ) {
                                var item = homeList!!.hotData[i]
                                Logger.d("item. title ::: " + item.title )
                                Logger.d("item. favorite count :: " + item.favoriteCount)
                                var hotStreamKey = item.id
                                for ( j in 0 until arrsize ) {
                                    Logger.e("Hot data exist")
                                    var modelObj = arr.optJSONObject(j)
                                    if ( hotStreamKey == modelObj.optString("streamKey") ) {
                                        item.favoriteCount = modelObj.optString("cnt")
                                        homeList!!.hotData.set(i, item)
                                    }
                                }
                            }

                            var temp = Gson().toJson(homeList)
                            val document = MutableDocument(dbKey)
                            document.setString(dbKey, temp)
                            DBManager.getInstance(context).put(document)
                            Logger.e("****************************")
                        }
                    }*/

                    for ( i in 0 until bestSize ) {
                        var b_item = mHomeAdapter.bestItems.get(i)
                        for ( j in 0 until arr.length() ) {
                            var modelObj = arr.optJSONObject(j)
                            if ( b_item.id == modelObj.optString("streamKey") ) {
                                var item = b_item
                                item.favoriteCount = modelObj.optString("cnt")
                                mHomeAdapter.bestItems.set(i, item)
                                Logger.e("best exist data set")
                            }
                        }
                    }

                    for ( i in 0 until itemSize ) {
                        var item = mHomeAdapter.items.get(i)
                        for ( j in 0 until arr.length() ) {
                            var modelObj = arr.optJSONObject(j)
                            Logger.d("item.id :: " + item.id)
                            Logger.d("stream  :: " + modelObj.optString("streamKey"))
                            if ( item.id == modelObj.optString("streamKey") ) {
                                var it = item
                                it.favoriteCount = modelObj.optString("cnt")
                                mHomeAdapter.items.set(i, it)
                                Logger.e("item exist data set")
                            }
                        }
                    }
                    mHomeAdapter.notifyDataSetChanged()
                }
            } else if ( action == "closeFloatingView") {
                if ( mHomeAdapter != null && mLayoutManager != null && mLayoutManager.findFirstCompletelyVisibleItemPosition() >= 2 ) {
                    var visiblePosition = mLayoutManager.findFirstCompletelyVisibleItemPosition()
                    visibleItem = visiblePosition
                    mHomeAdapter.playVideo(visibleItem)
                }

//                    if ( mLayoutManager.findFirstCompletelyVisibleItemPosition())
//                    mHomeAdapter.playVideo(visibleItem)
            } else if (action == "PAGE_CHANGED") {
                var page = intent.getIntExtra("page", -1)
                var gubun = intent.getStringExtra("gubun")
                if ( gubun == "run") {
                    if ( page == PuddingHomeTabFragment.PAGE_HOME && ::mHomeAdapter.isInitialized ) {
                        mHomeAdapter!!.runTimer()
                    }
                } else if ( gubun == "cancel") {
                    mHomeAdapter!!.cancelTimer()
                } else if ( gubun == "paging") {
                    if (::mHomeAdapter.isInitialized ) {
                        if ( page == PuddingHomeTabFragment.PAGE_HOME ) {
                            mHomeAdapter!!.runTimer()
                        } else {
                            mHomeAdapter!!.cancelTimer()
                        }
                    }
                }
            }
        }
    }
//    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
//        super.onActivityResult(requestCode, resultCode, intent)
//        Logger.e("onActivityResult called")
//        if ( resultCode == Activity.RESULT_OK ) {
//            if ( requestCode == REQUEST_GO_PLAYER_HOT ) {
//                val json = Gson().toJson(VideoDataContainer.getInstance().mVideoData)
//                var size = VideoDataContainer.getInstance().mVideoData.size
//                for ( i in 0 until size ) {
//                    Logger.e("dt :: " + VideoDataContainer.getInstance().mVideoData.get(i).title)
//                    Logger.e("dt :: " + VideoDataContainer.getInstance().mVideoData.get(i).favoriteCount)
//                }
//                val videoItems: MutableList<API114.HotDataItem> = Gson().fromJson(json, object : TypeToken<MutableList<API114.HotDataItem>>() {}.type)
//                if ( mHomeAdapter != null ) {
//                    mHomeAdapter.setHotItems(videoItems)
//                    mHomeAdapter.notifyDataSetChanged()
//                }
//            } else if ( requestCode == REQUEST_GO_PLAYER_ITEM) {
//                val json = Gson().toJson(VideoDataContainer.getInstance().mVideoData)
//                val videoItems: MutableList<API114.VideoItem> = Gson().fromJson(json, object : TypeToken<MutableList<API114.VideoItem>>() {}.type)
//                if ( mHomeAdapter != null ) {
//                    mHomeAdapter.setItems(videoItems)
//                }
//            }
//        }
//    }
}