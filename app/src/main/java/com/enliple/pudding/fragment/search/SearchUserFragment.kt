package com.enliple.pudding.fragment.search

import android.content.Intent
import android.os.Bundle
import androidx.core.widget.NestedScrollView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.enliple.pudding.AbsBaseTabFragment
import com.enliple.pudding.R
import com.enliple.pudding.activity.CasterProfileActivity
import com.enliple.pudding.adapter.search.SearchUserAdapter
import com.enliple.pudding.adapter.search.SearchUserEmptyAdapter
import com.enliple.pudding.bus.SearchResultBus
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.NetworkApi
import com.enliple.pudding.commons.network.NetworkBus
import com.enliple.pudding.commons.network.NetworkBusFastResponse
import com.enliple.pudding.commons.network.NetworkBusResponse
import com.enliple.pudding.commons.network.vo.API31
import com.enliple.pudding.commons.network.vo.API78
import com.enliple.pudding.commons.network.vo.BaseAPI
import com.enliple.pudding.commons.widget.recyclerview.WrappedGridLayoutManager
import com.enliple.pudding.commons.widget.recyclerview.WrappedLinearLayoutManager
import com.enliple.pudding.commons.widget.toast.AppToast
import com.enliple.pudding.enumeration.SearchResultTab
import com.enliple.pudding.fragment.main.CasterVODFragment
import com.enliple.pudding.widget.FollowCancelDialog
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_search_user.*
import kotlinx.android.synthetic.main.fragment_search_user.topBtn
import kotlinx.android.synthetic.main.fragment_search_vod.*
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

/**
 * Created by Kim Joonsung on 2018-11-26.
 */
class SearchUserFragment : AbsBaseTabFragment(), SearchUserAdapter.Listener, SearchUserEmptyAdapter.Listener {

    companion object {
        const val BUNDLE_EXTRA_KEY_KEYWORD = "keyword"
        private const val RECYCLER_VIEW_ITEM_CACHE_SIZE = 20

        private const val PAGE_DATA_COUNT = 90
        private const val NUMS_OF_COLUMN = 3
    }

    private var mAdapter: SearchUserAdapter? = null
    private var mEmptyAdapter:SearchUserEmptyAdapter? = null
    private var mLayoutManager:WrappedLinearLayoutManager? = null

    private var keyword = ""
    private var isReload = false
    private var isEndOfData = false
    private var pageCount = 1
    private var mIsVisibleToUser = false
    private var API31Key = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_search_user, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        EventBus.getDefault().register(this)

        if (arguments != null) {
            keyword = arguments!!.getString(SearchVODFragment.BUNDLE_EXTRA_KEY_KEYWORD)
        }

        Logger.e("user keyword : $keyword")

        topBtn.setOnClickListener { recyclerVeiwUsers.scrollToPosition(0) }

        recyclerVeiwUsers.setHasFixedSize(false)
        recyclerVeiwUsers.setItemViewCacheSize(RECYCLER_VIEW_ITEM_CACHE_SIZE)

    }

    override fun onResume() {
        super.onResume()
        Logger.e("SearchUserFragment onResume")

        if(mIsVisibleToUser) {
            EventBus.getDefault().post(NetworkBus(NetworkApi.API31.name, keyword, "1"))
        }
    }

    override fun onStop() {
        super.onStop()

    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onTabChanged(tabIndex: Int) {
        if (tabIndex == SearchResultTab.USER.ordinal) {

        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        mIsVisibleToUser = isVisibleToUser

        if(mIsVisibleToUser && TextUtils.isEmpty(API31Key)) {
            NetworkBus(NetworkApi.API31.name, keyword, pageCount.toString()).let {
                EventBus.getDefault().post(it)
            }
        }
    }

    override fun onItemClicked(item: API31.UserItem) {
        startActivity(Intent(context!!, CasterProfileActivity::class.java).apply {
            putExtra(CasterProfileActivity.INTENT_KEY_EXTRA_USER_ID, item.mb_id)
        })
    }

    override fun onUserClicked(item: API31.UserItem) {
        startActivity(Intent(context!!, CasterProfileActivity::class.java).apply {
            putExtra(CasterProfileActivity.INTENT_KEY_EXTRA_USER_ID, item.mb_id)
        })
    }

    override fun onFollowClicked(status: String, item: API31.UserItem) {
        if("N" == status) {
            val casterId = item.mb_id
            val userId = AppPreferences.getUserId(context!!)!!

            val followItem = API78.FollowItem().apply {
                strToUserId = casterId
                strThumbnail = item.mb_user_img
            }

            FollowCancelDialog(context!!, followItem, "", object:FollowCancelDialog.Listener {
                override fun onDismiss() {
                    val map = HashMap<String, String>()
                    map.put("strUserId", userId)
                    map.put("strToUserId", casterId)
                    map.put("isFollow", "N")
                    val body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), (JSONObject(map)).toString())
                    Logger.e("body :: $body")
                    val bus = NetworkBus(NetworkApi.API2.name, body)
                    EventBus.getDefault().post(bus)
                }
            }).show()
        } else {
            val casterId = item.mb_id
            val userId = AppPreferences.getUserId(context!!)
            val map = HashMap<String, String>()
            map.put("strUserId", userId!!)
            map.put("strToUserId", casterId)
            map.put("isFollow", "Y")
            val body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), (JSONObject(map)).toString())
            Logger.e("body :: $body")
            val bus = NetworkBus(NetworkApi.API2.name, body)
            EventBus.getDefault().post(bus)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: NetworkBusFastResponse) {
        if ( data.arg1.startsWith(NetworkApi.API2.toString()) ) {
            if (data.arg2 == "ok") {
                val bus = NetworkBus(NetworkApi.API31.name, keyword)
                EventBus.getDefault().post(bus)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: NetworkBusResponse) {
        if (data.arg1.startsWith(NetworkApi.API31.toString())) {
            handleNetworkResult(data)
        }
    }

    private fun handleNetworkResult(data: NetworkBusResponse) {
        Logger.e("SearchUserFragment handleNetworkResult")
        if ("ok" == data.arg2) {
            val str = DBManager.getInstance(context!!).get(data.arg1)
            val response: API31 = Gson().fromJson(str, API31::class.java)

            API31Key = NetworkApi.API31.toString()

            isEndOfData = response.nTotalCount < PAGE_DATA_COUNT
            if ( TextUtils.isEmpty(response.pageCount) )
                pageCount = 1
            else
                pageCount = response.pageCount.toInt()

            if(response.nUserTotalCount.toInt() == 0) {
                recyclerVeiwUsers.layoutManager = WrappedGridLayoutManager(context!!, NUMS_OF_COLUMN).apply {
                    orientation = androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
                    spanSizeLookup = object : androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup() {
                        override fun getSpanSize(position: Int): Int =
                                when (position) {
                                    0 -> NUMS_OF_COLUMN
                                    else -> 1
                                }
                    }
                }

                mEmptyAdapter = SearchUserEmptyAdapter(context!!).apply {
                    setListener(this@SearchUserFragment)
                    setKeyword(keyword)
                }

                recyclerVeiwUsers.adapter = mEmptyAdapter
                mEmptyAdapter!!.setItems(response.data)
            } else {
                if(!isReload) {
                    mLayoutManager = WrappedLinearLayoutManager(context!!)
                    mLayoutManager!!.orientation = androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
                    recyclerVeiwUsers.layoutManager = mLayoutManager
                    recyclerVeiwUsers.addOnScrollListener(scrollListener)

                    mAdapter = SearchUserAdapter().apply {
                        setListener(this@SearchUserFragment)
                        setKeyword(keyword)
                    }

                    recyclerVeiwUsers.adapter = mAdapter
                    mAdapter!!.setItems(response.data)
                } else {
                    mAdapter!!.addItems(response.data)
                }
            }

            EventBus.getDefault().post(SearchResultBus(0, response.nVodTotalCount.toInt()))
            EventBus.getDefault().post(SearchResultBus(1, response.nProductTotalCount.toInt()))
            EventBus.getDefault().post(SearchResultBus(2, response.nFormationTotalCount.toInt()))
            EventBus.getDefault().post(SearchResultBus(3, response.nTagTotalCount.toInt()))
            EventBus.getDefault().post(SearchResultBus(4, response.nUserTotalCount.toInt()))
        } else {
            Logger.e("SearchUserFragment handleNetworkResult else")
            val errorResult: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
            Logger.e("error : $errorResult")
        }
    }

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (mIsVisibleToUser) {
                val lastVisibleItemPosition = mLayoutManager?.findLastCompletelyVisibleItemPosition()
                val visibleCount = mLayoutManager?.findFirstVisibleItemPosition()
                val totalItemCount = mAdapter?.itemCount?.minus(1)

                if (visibleCount!! > 20) {
                    topBtn.visibility = View.VISIBLE
                } else {
                    topBtn.visibility = View.GONE
                }

                if ((lastVisibleItemPosition == totalItemCount) && !isEndOfData) {
                    isReload = true
                    ++pageCount

                    AppToast(context!!).showToastMessage("loading 중...",
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_BOTTOM)

                    EventBus.getDefault().post(NetworkBus(NetworkApi.API31.name, keyword, pageCount++.toString()))
                }
            }
        }
    }
}