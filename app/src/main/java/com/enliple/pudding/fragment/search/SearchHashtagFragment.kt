package com.enliple.pudding.fragment.search

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.enliple.pudding.AbsBaseTabFragment
import com.enliple.pudding.R
import com.enliple.pudding.activity.HashTagMoreActivity
import com.enliple.pudding.adapter.search.SearchHashTagAdapter
import com.enliple.pudding.bus.SearchResultBus
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.NetworkApi
import com.enliple.pudding.commons.network.NetworkBus
import com.enliple.pudding.commons.network.NetworkBusResponse
import com.enliple.pudding.commons.network.vo.API30
import com.enliple.pudding.commons.network.vo.BaseAPI
import com.enliple.pudding.commons.widget.recyclerview.WrappedLinearLayoutManager
import com.enliple.pudding.enumeration.SearchResultTab
import com.google.gson.Gson
import com.greenfrvr.hashtagview.HashtagView
import kotlinx.android.synthetic.main.fragment_search_hastag.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Created by Kim Joonsung on 2018-11-26.
 */
class SearchHashtagFragment : AbsBaseTabFragment(), SearchHashTagAdapter.Listener {

    companion object {
        const val BUNDLE_EXTRA_KEY_KEYWORD = "keyword"
        private const val RECYCLER_VIEW_ITEM_CACHE_SIZE = 20

        private const val PAGE_DATA_COUNT = 100
    }

    private lateinit var mAdapter: SearchHashTagAdapter
    private lateinit var mLayoutManager:WrappedLinearLayoutManager

    private var keyword = ""
    private var isReload = false
    private var isEndOfData = false
    private var pageCount = 1
    private var mIsVisibleToUser = false
    private var API30Key = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_search_hastag, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        EventBus.getDefault().register(this)

        if (arguments != null) {
            keyword = arguments!!.getString(SearchVODFragment.BUNDLE_EXTRA_KEY_KEYWORD)
        }

        Logger.e("hashtag keyword : $keyword")

        hashTagView.addOnTagClickListener(hastagClickListener)

        recyclerVeiwHashTag.setHasFixedSize(false)
        recyclerVeiwHashTag.setItemViewCacheSize(RECYCLER_VIEW_ITEM_CACHE_SIZE)
        recyclerVeiwHashTag.addOnScrollListener(scrollListener)

        mLayoutManager = WrappedLinearLayoutManager(context!!)
        mLayoutManager.orientation = androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
        recyclerVeiwHashTag.layoutManager = mLayoutManager

        mAdapter = SearchHashTagAdapter().apply {
            setListener(this@SearchHashtagFragment)
            setKeyword(keyword)
        }

        recyclerVeiwHashTag.adapter = mAdapter

        hashTagView.setTypeface(Typeface.createFromAsset(activity!!.assets, "fonts/notosanskr_medium.otf"))
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onTabChanged(tabIndex: Int) {
        if (tabIndex == SearchResultTab.HASHTAG.ordinal) {

        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        mIsVisibleToUser = isVisibleToUser

        if(mIsVisibleToUser && TextUtils.isEmpty(API30Key)) {
            val bus = NetworkBus(NetworkApi.API30.name, keyword, pageCount.toString())
            EventBus.getDefault().post(bus)
        }
    }

    override fun onHashtagClick(item: API30.HashtagItem) {
        startActivity(Intent(context!!, HashTagMoreActivity::class.java).apply {
            putExtra(HashTagMoreActivity.INTENT_EXTRA_KEY_HASH_TAG, item.tag_name)
            putExtra(HashTagMoreActivity.INTENT_EXTRA_KEY_HASH_TAG_ID, item.t_idx.toString())
        })
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: NetworkBusResponse) {
        if(data.arg1.startsWith(NetworkApi.API30.toString())) {
            handleNetworkResult(data)
        }
    }

    private fun handleNetworkResult(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            val str = DBManager.getInstance(context!!).get(data.arg1)
            val response: API30 = Gson().fromJson(str, API30::class.java)

            API30Key = NetworkApi.API30.toString()

            isEndOfData = response.nTotalCount < PAGE_DATA_COUNT
            pageCount = response.pageCount.toInt()

            if(response.nTagTotalCount.toInt() == 0) {
                layoutEmpty.visibility = View.VISIBLE
                recyclerVeiwHashTag.visibility = View.GONE

                textViewEmpty.text = "\'${keyword}\'"

                hashTagView.setData(response.data, object : HashtagView.DataTransform<API30.HashtagItem> {
                    override fun prepare(item: API30.HashtagItem?): CharSequence {
                        val spannableString = SpannableString("#${item?.tag_name}")
                        return spannableString
                    }
                })
            } else {
                layoutEmpty.visibility = View.GONE
                recyclerVeiwHashTag.visibility = View.VISIBLE

                if(!isReload) {
                    mAdapter.setItems(response.data)
                } else {
                    mAdapter.addItems(response.data)
                }
            }

            EventBus.getDefault().post(SearchResultBus(0, response.nVodTotalCount.toInt()))
            EventBus.getDefault().post(SearchResultBus(1, response.nProductTotalCount.toInt()))
            EventBus.getDefault().post(SearchResultBus(2, response.nFormationTotalCount.toInt()))
            EventBus.getDefault().post(SearchResultBus(3, response.nTagTotalCount.toInt()))
            EventBus.getDefault().post(SearchResultBus(4, response.nUserTotalCount.toInt()))
        } else {
            val errorResult: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
            Logger.e("error : $errorResult")
        }
    }

    private fun checkPositionAndLoadMoreData() {
        val lastVisibleItemPosition = mLayoutManager.findLastCompletelyVisibleItemPosition()
        val totalItemCount = mAdapter.itemCount - 1
        if ((lastVisibleItemPosition == totalItemCount) && !isEndOfData) {
            isReload = true
            EventBus.getDefault().post(NetworkBus(NetworkApi.API30.name, keyword, pageCount++.toString()))
        }
    }

    private val scrollListener = object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: androidx.recyclerview.widget.RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
        }

        override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if(mIsVisibleToUser) {
                checkPositionAndLoadMoreData()
            }
        }
    }

    private val hastagClickListener = object : HashtagView.TagsClickListener {
        override fun onItemClicked(item: Any?) {
            val obj = item as API30.HashtagItem
            Logger.d("######### hashTag : ${obj.tag_name}")

            startActivity(Intent(context!!, HashTagMoreActivity::class.java).apply {
                putExtra(HashTagMoreActivity.INTENT_EXTRA_KEY_HASH_TAG, obj.tag_name)
                putExtra(HashTagMoreActivity.INTENT_EXTRA_KEY_HASH_TAG_ID, obj.t_idx)
            })
        }
    }
}