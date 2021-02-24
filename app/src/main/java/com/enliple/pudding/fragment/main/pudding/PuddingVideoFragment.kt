package com.enliple.pudding.fragment.main.pudding

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.enliple.pudding.R
import com.enliple.pudding.activity.ShoppingPlayerActivity
import com.enliple.pudding.adapter.home.CategoryAdapter
import com.enliple.pudding.adapter.home.HomeVideoAdapter
import com.enliple.pudding.adapter.home.HotAdapter
import com.enliple.pudding.commons.app.CategoryModel
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.NetworkApi
import com.enliple.pudding.commons.network.NetworkBus
import com.enliple.pudding.commons.network.NetworkBusResponse
import com.enliple.pudding.commons.network.NetworkHandler
import com.enliple.pudding.commons.network.vo.API114
import com.enliple.pudding.commons.network.vo.API81
import com.enliple.pudding.commons.network.vo.BaseAPI
import com.enliple.pudding.commons.widget.toast.AppToast
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_pudding_video.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Created by Kim Joonsung on 2019-01-24.
 */
class PuddingVideoFragment : androidx.fragment.app.Fragment(), CategoryAdapter.Listener, HomeVideoAdapter.Listener {
    override fun onCategoryClick(categoryId: String) {

    }

    companion object {
        private const val NUMBER_COLUMNS = 2
        private const val PAGE_DATA_COUNT = 100
    }

    private lateinit var mHomeAdapter: HomeVideoAdapter

    private var homeOrder = ""
    private var mIsVisibleToUser = false
    private var mCategoryList: MutableList<API81.CategoryItem> = mutableListOf()
    private var isReload = false
    private var pageCount = 1
    private lateinit var mLayoutManager: androidx.recyclerview.widget.GridLayoutManager
    private var isOrderClick = false
    private var mCategoryId = ""
    private var isEndOfData = false
    private var dbKey = ""
    private var pagingDbKey = ""
    private var mIsFirst = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_pudding_video, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Logger.d("onViewCreated")

        EventBus.getDefault().register(this)

        // Main List
        recyclerViewMain.setHasFixedSize(true)
        recyclerViewMain.addOnScrollListener(scrollListener)
        mLayoutManager = androidx.recyclerview.widget.GridLayoutManager(context!!, NUMBER_COLUMNS).apply {
            orientation = androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
            spanSizeLookup = object : androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (position == 0 || position == 1) {
                        NUMBER_COLUMNS
                    } else {
                        1
                    }
                }
            }
        }

        recyclerViewMain.layoutManager = mLayoutManager

        mHomeAdapter = HomeVideoAdapter(context!!).apply {
            setListener(this@PuddingVideoFragment)
            setCategoryListener(this@PuddingVideoFragment)
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

        refresh()
    }

    override fun onDestroy() {
        super.onDestroy()

        EventBus.getDefault().unregister(this)
    }

/*    override fun onHotItemClicked(item: API114.HotDataItem, position: Int) {
        Logger.e("URL : ${item.stream}")

        dbKey = NetworkHandler.getInstance(context!!).getHomeKey(3, "1", homeOrder, "")

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
    }*/

    override fun onCategoryClicked(categoryId: String) {
        Logger.e("onCategoryClicked id:$categoryId")

        isReload = false
        pageCount = 1
        mCategoryId = categoryId

        mHomeAdapter.setCategoryClick(true)

        if ("all" == categoryId) {
            EventBus.getDefault().post(NetworkBus(NetworkApi.API114.name, "vod", "1", "", homeOrder))
        } else {
            EventBus.getDefault().post(NetworkBus(NetworkApi.API114.name, "vod", "1", categoryId, homeOrder))
        }
    }

    override fun onItemClicked(item: API114.VideoItem, position: Int) {
        Logger.e("URL : ${item.stream}")

        if ("all" == mCategoryId) mCategoryId = ""
        val page = (position / PAGE_DATA_COUNT) + 1
        pagingDbKey = NetworkHandler.getInstance(context!!).getHomeKey(3, page.toString(), homeOrder, mCategoryId)

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

    override fun onRecentOrderClicked(order: String) {
        isOrderClick = true
        homeOrder = order
        EventBus.getDefault().post(NetworkBus(NetworkApi.API114.name, "vod", "1", mCategoryId, homeOrder))
    }

    override fun onPopularityOrderClicked(order: String) {
        isOrderClick = true
        homeOrder = order
        EventBus.getDefault().post(NetworkBus(NetworkApi.API114.name, "vod", "1", mCategoryId, homeOrder))
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: NetworkBusResponse) {
        if (data.arg1.startsWith("GET/mui/main/vod?page=")) {
            handleNetworkVODResult(data)
        } else if (data.arg1.startsWith("GET/mui/category/select_vod?sc_code=")) {
            initCategory(data)
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

            isReload = false
            progressBar.visibility = View.VISIBLE
            homeOrder = ""
            pageCount = 1

            EventBus.getDefault().post(NetworkBus(NetworkApi.API81.name, "select_vod", ""))
        }
    }

    private fun initCategory(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            EventBus.getDefault().post(NetworkBus(NetworkApi.API114.name, "vod", "1", mCategoryId, homeOrder))
        } else {
            Logger.e("error : ${data.arg3}, ${data.arg4}")
        }
    }

    private fun handleNetworkVODResult(data: NetworkBusResponse) {
        progressBar.visibility = View.GONE
        if ("ok" == data.arg2) {
            val response: API114 = Gson().fromJson(DBManager.getInstance(context!!).get(data.arg1), API114::class.java)

            mHomeAdapter.setTotalItemCount(response.nTotalCount)

            isEndOfData = response.data.size < PAGE_DATA_COUNT
            // get category list ( all category 인 경우만 갱신한다.. )
            if (!isReload) {
                if (!isOrderClick) {
                    val key = NetworkHandler.getInstance(context!!).getKey(NetworkApi.API81.toString(), "select_vod", "")
                    val str = DBManager.getInstance(activity).get(key)
                    Logger.e("setCategoryItem str :: " + str);
                    mCategoryList = CategoryModel.getCategoryList(context!!, "select_vod")
                    mHomeAdapter.setCategoryItems(mCategoryList, str)

                    if (response.nHotTotalCount.toInt() > 0) {
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

    private val scrollListener = object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (mIsVisibleToUser) {
                var lastVisibleItemPosition = mLayoutManager.findLastCompletelyVisibleItemPosition()
                var totalItemCount = mHomeAdapter.itemCount - 1
                if ((lastVisibleItemPosition == totalItemCount) && !isEndOfData) {
                    isReload = true
                    ++pageCount

                    AppToast(context!!).showToastMessage("loading 중...",
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_BOTTOM)

                    EventBus.getDefault().post(NetworkBus(NetworkApi.API114.name, "vod", pageCount.toString(), mCategoryId, homeOrder))
                }
            }
        }
    }
}