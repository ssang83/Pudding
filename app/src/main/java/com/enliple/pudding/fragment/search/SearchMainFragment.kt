package com.enliple.pudding.fragment.search

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.enliple.pudding.AbsBaseFragment
import com.enliple.pudding.R
import com.enliple.pudding.activity.HashTagMoreActivity
import com.enliple.pudding.activity.ShoppingPlayerActivity
import com.enliple.pudding.adapter.search.KeywordListAdapter
import com.enliple.pudding.adapter.search.KeywordVODListAdapter
import com.enliple.pudding.common.VideoDataContainer
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.internal.AppConstants
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.NetworkApi
import com.enliple.pudding.commons.network.NetworkBus
import com.enliple.pudding.commons.network.NetworkBusResponse
import com.enliple.pudding.commons.network.NetworkHandler
import com.enliple.pudding.commons.network.vo.API27
import com.enliple.pudding.commons.network.vo.BaseAPI
import com.enliple.pudding.commons.network.vo.VOD
import com.enliple.pudding.commons.widget.toast.AppToast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_search_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Created by Kim Joonsung on 2018-11-26.
 */
class SearchMainFragment : AbsBaseFragment(), KeywordListAdapter.Listener, KeywordVODListAdapter.Listener {

    companion object {

        private const val RECYCLER_VIEW_ITEM_CACHE_SIZE = 20
    }

    private lateinit var mAdapter: KeywordListAdapter
    private var dataKey = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_search_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerViewKeywords.setHasFixedSize(true)
        recyclerViewKeywords.setItemViewCacheSize(RECYCLER_VIEW_ITEM_CACHE_SIZE)

        mAdapter = KeywordListAdapter()
        mAdapter.setListener(this@SearchMainFragment)
        mAdapter.setVodListener(this@SearchMainFragment)

        recyclerViewKeywords.adapter = mAdapter

        EventBus.getDefault().post(NetworkBus(NetworkApi.API27.name))
    }

    override fun onStart() {
        super.onStart()

        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()

        EventBus.getDefault().unregister(this)
    }

    override fun onDetailItemClicked(item: API27.KeywordVodItem) {
        val intent = Intent(context!!, HashTagMoreActivity::class.java)
        intent.putExtra(HashTagMoreActivity.INTENT_EXTRA_KEY_HASH_TAG, item.tag)
        intent.putExtra(HashTagMoreActivity.INTENT_EXTRA_KEY_HASH_TAG_ID, item.t_idx)
        startActivity(intent)
    }

    override fun onVodItemClicked(item: API27.KeywordVodItem.VodItem, position: Int, tagPosition: Int) {
        Logger.e("URL : ${item.stream}")

        // 페이징 및 dbKey 포지션 문제 때문에 앞으로 아래와 같이 List 객체를 만들어서 영상 데이터 사용
        val json = Gson().toJson(mAdapter.getItems(tagPosition))
        val videoItems : MutableList<VOD.DataBeanX> = Gson().fromJson(json, object : TypeToken<MutableList<VOD.DataBeanX>>(){}.type)
        VideoDataContainer.getInstance().mVideoData = videoItems

        if (!TextUtils.isEmpty(item.stream)) {
            startActivity(Intent(context!!, ShoppingPlayerActivity::class.java).apply {
                putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_PLAYER_FLAG, AppConstants.HASH_TAG_VOD_PLAYER)
                putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_MY_VOD_POSITION, position)
                putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_CASTER_ID, item.userId)
                putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_VIDEO_TYPE, item.videoType)
                data = Uri.parse("vcommerce://shopping?url=${item.stream}")
            })
        } else {
            AppToast(context!!).showToastMessage("방송 정보 메타 데이터가 존재하지 않습니다.",
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: NetworkBusResponse) {
        dataKey = NetworkHandler.getInstance(context!!).getKey(NetworkApi.API27.toString(), "", "")
        if (data.arg1.startsWith(dataKey)) {
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

    private fun handleNetworkResult(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            var str = DBManager.getInstance(context!!).get(data.arg1)
            var response: API27 = Gson().fromJson(str, API27::class.java)

            mAdapter.setItem(response.data)
        } else {
            var errorResult: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
            Logger.e("error : $errorResult")
        }
    }
}