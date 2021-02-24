package com.enliple.pudding.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.enliple.pudding.R
import com.enliple.pudding.activity.ProductDetailActivity
import com.enliple.pudding.activity.ShoppingPlayerActivity
import com.enliple.pudding.adapter.home.PromotionProductAdapter
import com.enliple.pudding.adapter.home.PromotionVODAdapter
import com.enliple.pudding.common.VideoDataContainer
import com.enliple.pudding.commons.app.PriceFormatter
import com.enliple.pudding.commons.app.ShopTreeKey
import com.enliple.pudding.commons.internal.AppConstants
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.network.NetworkApi
import com.enliple.pudding.commons.network.NetworkBus
import com.enliple.pudding.commons.network.NetworkBusResponse
import com.enliple.pudding.commons.network.vo.API116
import com.enliple.pudding.commons.network.vo.API117
import com.enliple.pudding.commons.network.vo.VOD
import com.enliple.pudding.commons.widget.toast.AppToast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_tab_event.*
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.json.JSONObject

/**
 * Created by Kim Joonsung on 2019-05-23.
 */
class EventTabFragment : Fragment() , PromotionVODAdapter.Listener, PromotionProductAdapter.Listener {

    companion object {
        private const val EVENT_TYPE_PRODUCT    = "prd"
        private const val EVENT_TYPE_VOD        = "vod"
    }

    private var mAdapter: PromotionVODAdapter? = null
    private var mProductAdapter: PromotionProductAdapter? = null

    private var mPosition = -1
    private var mDbKey = ""
    private var mEventType = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mPosition = arguments?.getInt("position", mPosition)!!
        mDbKey = arguments?.getString("dbKey")!!
        mEventType = arguments?.getString("eventType")!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_tab_event, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        EventBus.getDefault().register(this)

        eventList.setHasFixedSize(true)
        if(mEventType == EVENT_TYPE_VOD) {
            val response:API116 = Gson().fromJson(mDbKey, API116::class.java)

            mAdapter = PromotionVODAdapter(context!!).apply {
                setListener(this@EventTabFragment)
            }

            eventList.adapter = mAdapter

            mAdapter?.setItems(response.data.evPick[mPosition].tagData)
        } else if(mEventType == EVENT_TYPE_PRODUCT) {
            val response:API117 = Gson().fromJson(mDbKey, API117::class.java)

            mProductAdapter = PromotionProductAdapter(context!!).apply {
                setListener(this@EventTabFragment)
            }

            mProductAdapter?.setItems(response.data.evPick[mPosition].themeData)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe
    fun onMessageEvent(data:NetworkBusResponse) {
        if(data.arg1.startsWith("POST/products/") && data.arg1.endsWith("wish")) {
            val arr = data.arg1.split("/")
            val idx = arr[2]
            mProductAdapter?.likeSuccess(idx)
        }
    }

    override fun onVodClicked(item: API116.DataBeanX.EvPickItem.TagDataItem, position: Int) {
        // 페이징 및 dbKey 포지션 문제 때문에 앞으로 아래와 같이 List 객체를 만들어서 영상 데이터 사용
        val json = Gson().toJson(mAdapter?.items)
        val videoItems : MutableList<VOD.DataBeanX> = Gson().fromJson(json, object : TypeToken<MutableList<VOD.DataBeanX>>(){}.type)
        VideoDataContainer.getInstance().mVideoData = videoItems

        if (!TextUtils.isEmpty(item.stream)) {
            startActivity(Intent(context!!, ShoppingPlayerActivity::class.java).apply {
                putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_PLAYER_FLAG, AppConstants.EVENT_DETAIL_VOD_PLAYER)
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

    override fun onProductClicked(item: API117.DataItem.EvPickItem.ThemeDataItem) {
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
    }

    override fun onLikeClicked(item: API117.DataItem.EvPickItem.ThemeDataItem, status: Boolean) {
        JSONObject().apply {
            put("user", AppPreferences.getUserId(context!!))
            put("is_wish", if(status) "Y" else "N")
            put("type", item.strType)
        }.let {
            val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), it.toString())
            EventBus.getDefault().post(NetworkBus(NetworkApi.API126.name, item.idx, body))
        }
    }
}