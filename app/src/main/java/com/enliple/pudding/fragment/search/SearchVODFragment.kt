package com.enliple.pudding.fragment.search

import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.enliple.pudding.AbsBaseTabFragment
import com.enliple.pudding.R
import com.enliple.pudding.activity.ShoppingPlayerActivity
import com.enliple.pudding.adapter.search.SearchVODAdapter
import com.enliple.pudding.bus.SearchResultBus
import com.enliple.pudding.bus.ViewCountBus
import com.enliple.pudding.common.VideoDataContainer
import com.enliple.pudding.commons.app.PriceFormatter
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.NetworkApi
import com.enliple.pudding.commons.network.NetworkBus
import com.enliple.pudding.commons.network.NetworkBusResponse
import com.enliple.pudding.commons.network.vo.API29
import com.enliple.pudding.commons.network.vo.BaseAPI
import com.enliple.pudding.commons.network.vo.VOD
import com.enliple.pudding.commons.widget.recyclerview.WrappedLinearLayoutManager
import com.enliple.pudding.commons.widget.toast.AppToast
import com.enliple.pudding.enumeration.SearchResultTab
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_search_vod.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Created by Kim Joonsung on 2018-11-26.
 */
class SearchVODFragment : AbsBaseTabFragment(), View.OnClickListener, SearchVODAdapter.Listener {

    companion object {
        const val BUNDLE_EXTRA_KEY_KEYWORD = "keyword"
        private const val RECYCLER_VIEW_ITEM_CACHE_SIZE = 20

        private const val PAGE_DATA_COUNT = 100
    }

    private var mAdapter: SearchVODAdapter? = null
    private lateinit var mLayoutManager: WrappedLinearLayoutManager

    private var keyword = ""
    private var dataKey = ""
    private var order = ""
    private var isReload = false
    private var isEndOfData = false
    private var pageCount = 1
    private var mIsVisibleToUser = false
    private var isAPICall = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_search_vod, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        EventBus.getDefault().register(this)

        if (arguments != null) {
            keyword = arguments!!.getString(BUNDLE_EXTRA_KEY_KEYWORD)
        }

        Logger.e("vod keyword : $keyword")

        topBtn.setOnClickListener { recyclerViewVOD.scrollToPosition(0) }

        textViewPopularity.setOnClickListener(this)
        textViewRecently.setOnClickListener(this)

        recyclerViewVOD.addOnScrollListener(scrollListener)
        recyclerViewVOD.setHasFixedSize(false)
        recyclerViewVOD.setItemViewCacheSize(RECYCLER_VIEW_ITEM_CACHE_SIZE)
        recyclerViewVOD.isNestedScrollingEnabled = false

        mLayoutManager = WrappedLinearLayoutManager(context!!)
        recyclerViewVOD.layoutManager = mLayoutManager

        textViewPopularity.isSelected = false
        textViewRecently.isSelected = true

        order = "1"

        if(mIsVisibleToUser && !isAPICall) {
            NetworkBus(NetworkApi.API29.name, keyword, order, pageCount.toString()).let { EventBus.getDefault().post(it) }
        }

        setOrderFont(order)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onTabChanged(tabIndex: Int) {
        if (tabIndex == SearchResultTab.VOD.ordinal) {

        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        mIsVisibleToUser = isVisibleToUser
    }

    override fun onVodItemClicked(item: API29.VodItem, position: Int) {
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

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.textViewPopularity -> {
                textViewPopularity.isSelected = true
                textViewRecently.isSelected = false

                isReload = false
                order = "0"
                NetworkBus(NetworkApi.API29.name, keyword, order, "1").let { EventBus.getDefault().post(it) }

                setOrderFont(order)
            }
            R.id.textViewRecently -> {
                textViewPopularity.isSelected = false
                textViewRecently.isSelected = true

                isReload = false
                order = "1"
                NetworkBus(NetworkApi.API29.name, keyword, order, "1").let { EventBus.getDefault().post(it) }

                setOrderFont(order)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data:ViewCountBus) {
        mAdapter?.setViewCount(data.streamKey, data.count)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: NetworkBusResponse) {
        if (data.arg1.startsWith(NetworkApi.API29.toString())) {
            handleNetworkResult(data)
        } else if(data.arg1.startsWith("POST/products/") && data.arg1.endsWith("wish")) {
            var arr = data.arg1.split("/")
            var s_idx = arr[2]
            Logger.e("s_idx")
            if ( mAdapter != null ) {
                mAdapter?.changeZzim(s_idx)
            }
        }
    }

    private fun setOrderFont(order:String) {
        if(order == "1") {
            textViewPopularity.typeface = Typeface.createFromAsset(activity!!.assets, "fonts/notosanskr_medium.otf")
            textViewRecently.typeface = Typeface.createFromAsset(activity!!.assets, "fonts/notosanskr_bold.otf")
        } else {
            textViewPopularity.typeface = Typeface.createFromAsset(activity!!.assets, "fonts/notosanskr_bold.otf")
            textViewRecently.typeface = Typeface.createFromAsset(activity!!.assets, "fonts/notosanskr_medium.otf")
        }
    }

    private fun handleNetworkResult(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            val str = DBManager.getInstance(context!!).get(data.arg1)
            val response: API29 = Gson().fromJson(str, API29::class.java)

            isEndOfData = response.nTotalCount < PAGE_DATA_COUNT

            if(response.nVodTotalCount.toInt() > 0) {
                layoutNotEmpty.visibility = View.VISIBLE
                textViewTotalCount.text = String.format(getString(R.string.msg_search_result_count),
                        PriceFormatter.getInstance()?.getFormattedValue(response.nVodTotalCount))
            } else {
                layoutNotEmpty.visibility = View.GONE
            }

            if(mAdapter == null) {
                mAdapter = SearchVODAdapter(context!!, response.nVodTotalCount.toInt()).apply {
                    setListener(this@SearchVODFragment)
                }

                recyclerViewVOD.adapter = mAdapter
            }

            if(!isReload) {
                mAdapter?.setKeyword(keyword)
                mAdapter?.setItem(response.data)
            } else {
                mAdapter?.addItem(response.data)
            }

            EventBus.getDefault().post(SearchResultBus(0, response.nVodTotalCount.toInt()))
            EventBus.getDefault().post(SearchResultBus(1, response.nProductTotalCount.toInt()))
            EventBus.getDefault().post(SearchResultBus(2, response.nFormationTotalCount.toInt()))
            EventBus.getDefault().post(SearchResultBus(3, response.nTagTotalCount.toInt()))
            EventBus.getDefault().post(SearchResultBus(4, response.nUserTotalCount.toInt()))

            isAPICall = true
        } else {
            val errorResult: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
            Logger.e("error : $errorResult")
        }
    }

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (mIsVisibleToUser) {
                val lastVisibleItemPosition = mLayoutManager.findLastCompletelyVisibleItemPosition()
                val visibleCount = mLayoutManager.findFirstVisibleItemPosition()
                val totalItemCount = mAdapter?.itemCount?.minus(1)

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

                    EventBus.getDefault().post(NetworkBus(NetworkApi.API29.name, keyword, order, pageCount.toString()))
                }
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            var state = newState
            if ( state == RecyclerView.SCROLL_STATE_IDLE ) {
                Handler().postDelayed({
                    if ( state == RecyclerView.SCROLL_STATE_IDLE )
                        if(isAdded) {
                            topBtn.visibility = View.GONE
                        }
                }, 1500)
            }
        }
    }
}
