package com.enliple.pudding.fragment.main

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.enliple.pudding.R
import com.enliple.pudding.activity.LinkWebViewActivity
import com.enliple.pudding.activity.ProductDetailActivity
import com.enliple.pudding.adapter.home.CasterProductAdapter
import com.enliple.pudding.adapter.home.CasterProductAdapter.CasterLiveHeaderHolder
import com.enliple.pudding.commons.app.ShopTreeKey
import com.enliple.pudding.commons.app.Utils
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.NetworkApi
import com.enliple.pudding.commons.network.NetworkBus
import com.enliple.pudding.commons.network.NetworkBusResponse
import com.enliple.pudding.commons.network.NetworkHandler
import com.enliple.pudding.commons.network.vo.API138
import com.enliple.pudding.commons.network.vo.API139
import com.enliple.pudding.commons.network.vo.API33
import com.enliple.pudding.commons.network.vo.BaseAPI
import com.enliple.pudding.commons.shoptree.network.ShopTreeAsyncTask
import com.enliple.pudding.commons.widget.recyclerview.WrappedLinearLayoutManager
import com.enliple.pudding.commons.widget.toast.AppToast
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_caster_product.*
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.json.JSONObject

/**
 * Created by Kim Joonsung on 2018-10-10.
 */
class CasterProductFragment : androidx.fragment.app.Fragment(), CasterProductAdapter.Listener {

    companion object {
        private const val RECYCLER_VIEW_ITEM_CACHE_SIZE = 20
        private const val PAGE_DATA_COUNT = 100

        const val BUNDLE_EXTRA_KEY_USER_ID = "CasterProductFragment_userId"
    }

    private lateinit var mAdapter: CasterProductAdapter
    private lateinit var mLayoutManager : WrappedLinearLayoutManager
    private var userId = ""
    private var totalPageCount = 1
    private var isOrderClick = false
    private var isReload = false
    private var mOrder = "1"
    private var isEndOfData = true
    private var page = 1
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_caster_product, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (arguments != null) {
            userId = arguments!!.getString(BUNDLE_EXTRA_KEY_USER_ID)
            Logger.e("CasterProductFragment userId :: $userId")
        }

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }

        recyclerViewLastLive.setHasFixedSize(false)
        recyclerViewLastLive.setItemViewCacheSize(RECYCLER_VIEW_ITEM_CACHE_SIZE)
        mLayoutManager = WrappedLinearLayoutManager(context!!)
        recyclerViewLastLive.layoutManager = mLayoutManager

        mAdapter = CasterProductAdapter(context!!).apply {
            setListener(this@CasterProductFragment)
        }

        recyclerViewLastLive.adapter = mAdapter

        if (!TextUtils.isEmpty(userId)) {
            EventBus.getDefault().post(NetworkBus(NetworkApi.API33.name, userId, "ITEM", mOrder, "$page"))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }

    override fun onItemClicked(item: API33.ProductItem) {
        if(item.strType == "1") {
            startActivity(Intent(context!!, ProductDetailActivity::class.java).apply {
                putExtra(ShopTreeKey.KEY_IDX, item.idx)
                putExtra(ShopTreeKey.KEY_PCODE, item.pcode)
                putExtra(ShopTreeKey.KEY_SCCODE, item.sc_code)
                putExtra(ShopTreeKey.KEY_STREAM_KEY, "")
                putExtra(ShopTreeKey.KEY_VOD_TYPE, "")
            })
        } else {
            val task = ShopTreeAsyncTask(context!!)
            task.getDRCLink(item.idx, "", "", item.strType, {
                result, obj ->
                    try {
                        val response: API139 = Gson().fromJson(obj.toString(), API139::class.java)
                        if(response.result == "success") {
                            startActivity(Intent(context!!, LinkWebViewActivity::class.java).apply {
                                putExtra(LinkWebViewActivity.INTENT_EXTRA_KEY_LINK, response.url)
                                putExtra("TITLE", item.title)
                                putExtra("IDX", item.idx)
                                putExtra("TYPE", item?.strType)
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

    override fun onLikeClicked(item: API33.ProductItem, status: Boolean) {
        JSONObject().apply {
            put("user", AppPreferences.getUserId(context!!))
            put("is_wish", if(status) "Y" else "N")
            put("type", item.strType);
        }.let {
            val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), it.toString())
            EventBus.getDefault().post(NetworkBus(NetworkApi.API126.name, item.idx, body))
        }
    }

    override fun onPopularityOrderClicked(holder: CasterLiveHeaderHolder, order: String) {
        mOrder = order
        isOrderClick = true
        isReload = false
        page = 1
        EventBus.getDefault().post(NetworkBus(NetworkApi.API33.name, userId, "ITEM", order, "$page"))
    }

    override fun onRecentlyOrderClicked(holder: CasterLiveHeaderHolder, order: String) {
        mOrder = order
        isOrderClick = true
        isReload = false
        page = 1
        EventBus.getDefault().post(NetworkBus(NetworkApi.API33.name, userId, "ITEM", order, "$page"))
    }

    @Subscribe()
    fun onMessageEvent(data: NetworkBusResponse) {
        val key = NetworkHandler.getInstance(context!!).getKey(NetworkApi.API33.toString(), userId, "ITEM")
        if (data.arg1.startsWith(key)) {
            handleNetworkResult(data)
        } else if(data.arg1.startsWith("POST/products/") && data.arg1.endsWith("wish")) {
            val arr = data.arg1.split("/")
            val idx = arr[2]
            mAdapter.likeSuccess(idx)
        }
    }

    private fun handleNetworkResult(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            var str = DBManager.getInstance(context!!).get(data.arg1)
            var result: API33 = Gson().fromJson(str, API33::class.java)

            totalPageCount = result.pageCount
            isEndOfData = result.data.size == 0

            if (result.data.size > 0) {
                recyclerViewLastLive.visibility = View.VISIBLE
                emptyLayer.visibility = View.GONE

                mAdapter.setTotalCount(result.nTotalCount.toInt())

                if (!isReload) {
                    mAdapter.setItem(result.data, isOrderClick)
                } else {
                    mAdapter.addItem(result.data)
                }
            } else {
                if(isReload) {
                    if ( mAdapter.itemCount <= 0 ) {
                        recyclerViewLastLive.visibility = View.GONE
                        emptyLayer.visibility = View.VISIBLE
                    }
                } else {
                    recyclerViewLastLive.visibility = View.GONE
                    emptyLayer.visibility = View.VISIBLE
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
            val totalItemCount = mAdapter.itemCount - 1
            if ((lastVisibleItemPosition == totalItemCount) && !isEndOfData) {
                isReload = true
                ++totalPageCount
                page ++

                AppToast(context!!).showToastMessage("loading 중...",
                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                        AppToast.GRAVITY_BOTTOM)

                EventBus.getDefault().post(NetworkBus(NetworkApi.API33.name, userId, "ITEM", mOrder, "$page"))
            }
        }
    }
}