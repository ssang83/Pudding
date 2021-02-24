package com.enliple.pudding.fragment.search

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.enliple.pudding.AbsBaseTabFragment
import com.enliple.pudding.R
import com.enliple.pudding.activity.LinkWebViewActivity
import com.enliple.pudding.activity.ProductDetailActivity
import com.enliple.pudding.adapter.search.SearchProductAdapter
import com.enliple.pudding.bus.SearchResultBus
import com.enliple.pudding.commons.app.PriceFormatter
import com.enliple.pudding.commons.app.ShopTreeKey
import com.enliple.pudding.commons.app.Utils
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.NetworkApi
import com.enliple.pudding.commons.network.NetworkBus
import com.enliple.pudding.commons.network.NetworkBusResponse
import com.enliple.pudding.commons.network.vo.API138
import com.enliple.pudding.commons.network.vo.API139
import com.enliple.pudding.commons.network.vo.API90
import com.enliple.pudding.commons.network.vo.BaseAPI
import com.enliple.pudding.commons.shoptree.network.ShopTreeAsyncTask
import com.enliple.pudding.commons.widget.recyclerview.WrappedLinearLayoutManager
import com.enliple.pudding.commons.widget.toast.AppToast
import com.enliple.pudding.enumeration.SearchResultTab
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_search_product.*
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

/**
 * Created by Kim Joonsung on 2018-12-06.
 */
class SearchProductFragment : AbsBaseTabFragment(), View.OnClickListener, SearchProductAdapter.Listener {

    companion object {
        const val BUNDLE_EXTRA_KEY_KEYWORD = "keyword"

        private const val RECYCLER_VIEW_ITEM_CACHE_SIZE = 20
    }

    private var mAdapter: SearchProductAdapter? = null
    private lateinit var mLayoutManager: WrappedLinearLayoutManager

    private var keyword = ""
    private var order = "1"
    private var isReload = false
    private var isEndOfData = false
    private var pageCount = 1
    private var mIsVisibleToUser = false
    private var API90Key = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_search_product, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        EventBus.getDefault().register(this)

        if (arguments != null) {
            keyword = arguments!!.getString(BUNDLE_EXTRA_KEY_KEYWORD)
        }

        topBtn.setOnClickListener { recyclerVeiwProducts.scrollToPosition(0) }
        textViewPopularity.setOnClickListener(this)
        textViewRecently.setOnClickListener(this)

        recyclerVeiwProducts.addOnScrollListener(scrollListener)
        recyclerVeiwProducts.setItemViewCacheSize(RECYCLER_VIEW_ITEM_CACHE_SIZE)
        recyclerVeiwProducts.setHasFixedSize(false)
        recyclerVeiwProducts.isNestedScrollingEnabled = false

        mLayoutManager = WrappedLinearLayoutManager(context!!).apply {
            orientation = androidx.recyclerview.widget.GridLayoutManager.VERTICAL
        }
        recyclerVeiwProducts.layoutManager = mLayoutManager

        textViewPopularity.isSelected = false
        textViewRecently.isSelected = true

//        if(mIsVisibleToUser) {
//            NetworkBus(NetworkApi.API90.name, keyword, order, pageCount.toString()).let {
//                EventBus.getDefault().post(it)
//            }
//        }

        setOrderFont(order)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onTabChanged(tabIndex: Int) {
        if (tabIndex == SearchResultTab.PRODUCT.ordinal) {

        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        mIsVisibleToUser = isVisibleToUser

        if(mIsVisibleToUser && TextUtils.isEmpty(API90Key)) {
            NetworkBus(NetworkApi.API90.name, keyword, order, pageCount.toString()).let {
                EventBus.getDefault().post(it)
            }
        }
    }

    override fun onItemClicked(item: API90.ProductItem) {
        if(item.strType == 1) {
            startActivity(Intent(context!!, ProductDetailActivity::class.java).apply {
                putExtra("from_live", false)
                putExtra("from_store", false)
                putExtra("idx", item.idx)
                putExtra("name", item.title)
                putExtra("price", PriceFormatter.getInstance()!!.getFormattedValue(item.price))
                putExtra("image", item.image1)
                putExtra("storeName", item.sitename)
                putExtra(ShopTreeKey.KEY_PCODE, item.pcode)
                putExtra(ShopTreeKey.KEY_SCCODE, item.sc_code)
                putExtra(ShopTreeKey.KEY_STREAM_KEY, "")
                putExtra(ShopTreeKey.KEY_VOD_TYPE, "")
                putExtra(ShopTreeKey.KEY_RECOMMEND_ID, "")
            })
        } else {
            val task = ShopTreeAsyncTask(context!!)
            task.getDRCLink(item.idx, "", "", "${item.strType}", {
                result, obj ->
                    try {
                        val response: API139 = Gson().fromJson(obj.toString(), API139::class.java)
                        if(response.result == "success") {
                            startActivity(Intent(context!!, LinkWebViewActivity::class.java).apply {
                                putExtra(LinkWebViewActivity.INTENT_EXTRA_KEY_LINK, response.url)
                                putExtra("TITLE", item.title)
                                putExtra("IDX", item.idx)
                                putExtra("TYPE", item.strType)
                                putExtra("IS_WISH", item.is_wish)
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

    override fun onLikeClicked(item: API90.ProductItem, status: Boolean) {
        JSONObject().apply {
            put("user", AppPreferences.getUserId(context!!))
            put("is_wish", if(status) "Y" else "N")
            put("type", item.strType)
        }.let {
            val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), it.toString())
            EventBus.getDefault().post(NetworkBus(NetworkApi.API126.name, item.idx, body))
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.textViewPopularity -> {
                textViewPopularity.isSelected = true
                textViewRecently.isSelected = false

                order = "0"
                isReload = false
                NetworkBus(NetworkApi.API90.name, keyword, order, "1").let {
                    EventBus.getDefault().post(it)
                }

                setOrderFont(order)
            }

            R.id.textViewRecently -> {
                textViewPopularity.isSelected = false
                textViewRecently.isSelected = true

                order = "1"
                isReload = false
                NetworkBus(NetworkApi.API90.name, keyword, order, "1").let {
                    EventBus.getDefault().post(it)
                }

                setOrderFont(order)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: NetworkBusResponse) {
        if(data.arg1.startsWith(NetworkApi.API90.toString())) {
            if ("ok" == data.arg2) {
                val str = DBManager.getInstance(context!!).get(data.arg1)
                val response: API90 = Gson().fromJson(str, API90::class.java)

                API90Key = NetworkApi.API90.toString()

                isEndOfData = response.data.size == 0 || response.nProductTotalCount.toInt() == 0
                pageCount = response.pageCount.toInt()

                if(response.nProductTotalCount.toInt() > 0) {
                    layoutNoEmpty.visibility = View.VISIBLE
                    textViewTotalCount.text = String.format(getString(R.string.msg_search_result_count),
                            PriceFormatter.getInstance()?.getFormattedValue(response.nProductTotalCount))
                } else {
                    layoutNoEmpty.visibility = View.GONE
                }

                if(mAdapter == null) {
                    mAdapter = SearchProductAdapter(context!!, response.nProductTotalCount.toInt()).apply {
                        setListener(this@SearchProductFragment)
                    }

                    recyclerVeiwProducts.adapter = mAdapter
                }

                if(!isReload) {
                    mAdapter?.setKeyword(keyword)
                    mAdapter?.setItems(response.data)
                } else {
                    mAdapter?.addItems(response.data)
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
        } else if(data.arg1.startsWith("POST/products/") && data.arg1.endsWith("wish")) {
            val arr = data.arg1.split("/")
            val idx = arr[2]
            mAdapter?.likeSuccess(idx)
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

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (mIsVisibleToUser) {
                val lastVisibleItemPosition = mLayoutManager.findLastCompletelyVisibleItemPosition()
                val visibleCount = mLayoutManager.findFirstVisibleItemPosition()
                val totalItemCount = mAdapter?.itemCount?.minus(1)

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

                    NetworkBus(NetworkApi.API90.name, keyword, order, pageCount.toString()).let { bus ->
                        EventBus.getDefault().post(bus)
                    }
                }
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            var state = newState
            if ( state == RecyclerView.SCROLL_STATE_IDLE ) {
                Handler().postDelayed({
                    if(isAdded) {
                        if ( state == RecyclerView.SCROLL_STATE_IDLE )
                            topBtn.visibility = View.GONE
                    }
                }, 1500)
            }
        }
    }
}