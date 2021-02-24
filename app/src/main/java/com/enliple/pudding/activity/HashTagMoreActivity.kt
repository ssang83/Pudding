package com.enliple.pudding.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.enliple.pudding.AbsBaseActivity
import com.enliple.pudding.R
import com.enliple.pudding.adapter.search.HashTagVodListAdapter
import com.enliple.pudding.common.VideoDataContainer
import com.enliple.pudding.commons.app.NetworkStatusUtils
import com.enliple.pudding.commons.app.PriceFormatter
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.NetworkApi
import com.enliple.pudding.commons.network.NetworkBus
import com.enliple.pudding.commons.network.NetworkBusResponse
import com.enliple.pudding.commons.network.NetworkHandler
import com.enliple.pudding.commons.network.vo.API28
import com.enliple.pudding.commons.network.vo.VOD
import com.enliple.pudding.commons.widget.recyclerview.WrappedLinearLayoutManager
import com.enliple.pudding.commons.widget.toast.AppToast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_hashtag_search.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


/**
 * Created by Kim Joonsung on 2018-10-02.
 */
class HashTagMoreActivity : AbsBaseActivity(), View.OnClickListener, HashTagVodListAdapter.Listener {
    companion object {
        private const val RECYCLER_VIEW_ITEM_CACHE_SIZE = 20

        const val INTENT_EXTRA_KEY_HASH_TAG = "hashtag"
        const val INTENT_EXTRA_KEY_HASH_TAG_ID = "tagId"

        private const val PAGE_DATA_COUNT = 100
    }

    private lateinit var mAdapter: HashTagVodListAdapter
    private var tag: String = ""
    private var tagId: String = ""
    private var dataKey = ""
    private var order = ""
    private var pageCount = 1
    private var layoutManager: WrappedLinearLayoutManager? = null
    private var isReload = false
    private var isEndOfData = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hashtag_search)
        EventBus.getDefault().register(this)

        val intent = intent
        if (intent != null) {
            tag = intent.getStringExtra(INTENT_EXTRA_KEY_HASH_TAG)
            tagId = intent.getStringExtra(INTENT_EXTRA_KEY_HASH_TAG_ID)
        }

        textViewPopularity.setOnClickListener(this)
        textViewRecently.setOnClickListener(this)
        buttonBack.setOnClickListener(this)

        recyclerViewTag.setHasFixedSize(false)
        recyclerViewTag.addOnScrollListener(scrollListener)
        recyclerViewTag.setItemViewCacheSize(RECYCLER_VIEW_ITEM_CACHE_SIZE)
        layoutManager = WrappedLinearLayoutManager(this)
        recyclerViewTag.layoutManager = layoutManager

        mAdapter = HashTagVodListAdapter(this).apply {
            setListener(this@HashTagMoreActivity)
        }

        recyclerViewTag.adapter = mAdapter

        textViewPopularity.isSelected = false
        textViewRecently.isSelected = true

        textViewTitle.text = "#${tag}"

        order = "1"
    }

    override fun onResume() {
        super.onResume()
        NetworkBus(NetworkApi.API28.name, tagId, order, "$pageCount").let { EventBus.getDefault().post(it) }
    }

    override fun onDozeModeStateChanged(dozeEnable: Boolean) {}

    override fun onNetworkStatusChanged(status: NetworkStatusUtils.NetworkStatus) {}

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onVodItemClicked(item: API28.VodItem, position: Int) {
        Logger.e("URL : ${item.stream}")

        // 페이징 및 dbKey 포지션 문제 때문에 앞으로 아래와 같이 List 객체를 만들어서 영상 데이터 사용
        val json = Gson().toJson(mAdapter.items)
        val videoItems : MutableList<VOD.DataBeanX> = Gson().fromJson(json, object : TypeToken<MutableList<VOD.DataBeanX>>(){}.type)
        VideoDataContainer.getInstance().mVideoData = videoItems

        if (!TextUtils.isEmpty(item.stream)) {
            startActivity(Intent(this@HashTagMoreActivity, ShoppingPlayerActivity::class.java).apply {
                putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_POSITION, position)
                putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_CASTER_ID, item.userId)
                putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_VIDEO_TYPE, item.videoType)

                data = Uri.parse("vcommerce://shopping?url=${item.stream}")
            })
        } else {
            AppToast(this@HashTagMoreActivity).showToastMessage("방송 정보 메타 데이터가 존재하지 않습니다.",
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.buttonBack -> {
                onBackPressed()
            }
            R.id.buttonSearchBack -> {
                onBackPressed()
            }
            R.id.textViewPopularity -> {
                if ( order == "0" )
                    return
                pageCount = 1
                mAdapter.removeItems()
                Logger.e("after remove size :: ${mAdapter.itemCount}")
                textViewPopularity.isSelected = true
                textViewRecently.isSelected = false

                order = "0"
                var bus = NetworkBus(NetworkApi.API28.name, tagId, order, "$pageCount")
                EventBus.getDefault().post(bus)
            }
            R.id.textViewRecently -> {
                if ( order == "1" )
                    return
                pageCount = 1
                mAdapter.removeItems()
                Logger.e("after remove size :: ${mAdapter.itemCount}")
                textViewPopularity.isSelected = false
                textViewRecently.isSelected = true

                order = "1"
                var bus = NetworkBus(NetworkApi.API28.name, tagId, order, "$pageCount")
                EventBus.getDefault().post(bus)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: NetworkBusResponse) {
        dataKey = NetworkHandler.getInstance(this@HashTagMoreActivity).getKey(NetworkApi.API28.toString(), tagId, order)
        if (data.arg1.startsWith(dataKey)) {
            handleNetworkResult(data)
        }
    }

    private fun handleNetworkResult(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            val str = DBManager.getInstance(this@HashTagMoreActivity).get(data.arg1)
            val response: API28 = Gson().fromJson(str, API28::class.java)
            Logger.e("totalCount :: ${response.nTotalCount}")
            Logger.e("response.data size :: ${response.data.size}")

            isEndOfData = response.data.size < PAGE_DATA_COUNT
            pageCount = response.pageCount

            if(!isReload) {
                textViewTotalCount.text = String.format(getString(R.string.msg_search_result_count), PriceFormatter.getInstance()?.getFormattedValue(response.nTotalCount))
                mAdapter.setItem(response.data)
            } else {
                mAdapter.addItem(response.data)
            }
        } else {
            Logger.e("error ${data.arg3} ${data.arg4}")
        }
    }

    private val scrollListener = object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val lastVisibleItemPosition = layoutManager!!.findLastCompletelyVisibleItemPosition()
            val totalItemCount = mAdapter.itemCount - 1
            if ((lastVisibleItemPosition == totalItemCount) && !isEndOfData) {
                isReload = true
                ++pageCount

                NetworkBus(NetworkApi.API28.name, tagId, order, "$pageCount").let { EventBus.getDefault().post(it) }
            }
        }
    }
}