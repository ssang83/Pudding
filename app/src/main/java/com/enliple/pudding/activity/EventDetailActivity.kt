package com.enliple.pudding.activity

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import com.google.android.material.tabs.TabLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.enliple.pudding.AbsBaseActivity
import com.enliple.pudding.R
import com.enliple.pudding.adapter.home.PromotionProductAdapter
import com.enliple.pudding.adapter.home.PromotionVODAdapter
import com.enliple.pudding.common.VideoDataContainer
import com.enliple.pudding.commons.app.ImageLoad
import com.enliple.pudding.commons.app.NetworkStatusUtils
import com.enliple.pudding.commons.app.PriceFormatter
import com.enliple.pudding.commons.app.ShopTreeKey
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.internal.AppConstants
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.NetworkApi
import com.enliple.pudding.commons.network.NetworkBus
import com.enliple.pudding.commons.network.NetworkBusResponse
import com.enliple.pudding.commons.network.NetworkHandler
import com.enliple.pudding.commons.network.vo.*
import com.enliple.pudding.commons.shoptree.network.ShopTreeAsyncTask
import com.enliple.pudding.commons.widget.recyclerview.WrappedGridLayoutManager
import com.enliple.pudding.commons.widget.recyclerview.WrappedLinearLayoutManager
import com.enliple.pudding.commons.widget.toast.AppToast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_event_detail.*
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import java.net.URL

/**
 * Created by Kim Joonsung on 2019-01-09.
 */
class EventDetailActivity : AbsBaseActivity(), PromotionVODAdapter.Listener, PromotionProductAdapter.Listener {

    companion object {
        const val INTENT_KEY_EVENT_ID = "event_id"
        const val INTENT_KEY_EVENT_TYPE = "event_type"

        private const val RECYCLER_VIEW_ITEM_CACHE_SIZE = 20
        private val VIDEO_COLUMNS = 2

        private const val EVENT_TYPE_IMAGE      = "img"
        private const val EVENT_TYPE_PRODUCT    = "prd"
        private const val EVENT_TYPE_VOD        = "vod"
    }

    private var mAdapter:PromotionVODAdapter? = null
    private var mProductAdapter:PromotionProductAdapter? = null

    private var eventId = ""
    private var eventTitle = ""
    private var eventType = ""
    private var dataKey = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_detail)
        EventBus.getDefault().register(this)

        buttonClose.setOnClickListener { onBackPressed() }
    }

    override fun onResume() {
        super.onResume()
        checkIntent(intent)
    }

    override fun onDozeModeStateChanged(dozeEnable: Boolean) {}
    override fun onNetworkStatusChanged(status: NetworkStatusUtils.NetworkStatus) {}

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onBackPressed() {
        super.onBackPressed()

        finish()
    }

    override fun onVodClicked(item: API116.DataBeanX.EvPickItem.TagDataItem, position:Int) {
        Logger.e("URL : ${item.stream}")

        // 페이징 및 dbKey 포지션 문제 때문에 앞으로 아래와 같이 List 객체를 만들어서 영상 데이터 사용
        val json = Gson().toJson(mAdapter?.items)
        val videoItems : MutableList<VOD.DataBeanX> = Gson().fromJson(json, object : TypeToken<MutableList<VOD.DataBeanX>>(){}.type)
        VideoDataContainer.getInstance().mVideoData = videoItems

        if (!TextUtils.isEmpty(item.stream)) {
            startActivity(Intent(this, ShoppingPlayerActivity::class.java).apply {
                /**
                putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_PLAYER_FLAG, AppConstants.EVENT_DETAIL_VOD_PLAYER)
                putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_MY_VOD_POSITION, position)
                putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_CASTER_ID, item.userId)
                putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_VIDEO_TYPE, item.videoType)

                data = Uri.parse("vcommerce://shopping?url=${item.stream}")
                **/
                putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_POSITION, position)
                putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_CASTER_ID, item.userId)

                data = Uri.parse("vcommerce://shopping?url=${item.stream}")
            })
        } else {
            AppToast(this).showToastMessage("방송 정보 메타 데이터가 존재하지 않습니다.",
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM)
        }
    }

    override fun onProductClicked(item: API117.DataItem.EvPickItem.ThemeDataItem) {
        if("1" == item.strType) {
            startActivity(Intent(this, ProductDetailActivity::class.java).apply {
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
            ShopTreeAsyncTask(this).getDRCLink(item.idx, "", "", item.strType,
                    { result, obj ->
                        try {
                            val response: API139 = Gson().fromJson(obj.toString(), API139::class.java)
                            if(response.result == "success") {
                                startActivity(Intent(this, LinkWebViewActivity::class.java).apply {
                                    putExtra(LinkWebViewActivity.INTENT_EXTRA_KEY_LINK, response.url)
                                    putExtra("TITLE", item.title)
                                    putExtra("IDX", item.idx)
                                    putExtra("TYPE", item.strType)
                                    putExtra("IS_WISH", item.is_wish)
                                })
                            } else {
                                AppToast(this).showToastMessage("잘못된 링크 입니다.",
                                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                                        AppToast.GRAVITY_BOTTOM)
                            }
                        } catch (e:Exception) {
                            Logger.p(e)
                        }
                    })
        }
    }

    override fun onLikeClicked(item: API117.DataItem.EvPickItem.ThemeDataItem, status: Boolean) {
        JSONObject().apply {
            put("user", AppPreferences.getUserId(this@EventDetailActivity))
            put("is_wish", if(status) "Y" else "N")
            put("type", item.strType)
        }.let {
            val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), it.toString())
            EventBus.getDefault().post(NetworkBus(NetworkApi.API126.name, item.idx, body))
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data:NetworkBusResponse) {
        val API115 = NetworkHandler.getInstance(this)
                .getKey(NetworkApi.API115.toString(), eventId, "")

        dataKey = NetworkHandler.getInstance(this)
                .getKey(NetworkApi.API116.toString(), eventId, "")

        val API117 = NetworkHandler.getInstance(this)
                .getKey(NetworkApi.API117.toString(), eventId, "")

        if(data.arg1 == API115) {
            handleNetworkAPI115(data)
        } else if(data.arg1 == dataKey) {
            handleNetworkAPI116(data)
        } else if(data.arg1 == API117) {
            handleNetworkAPI117(data)
        } else if(data.arg1.startsWith("POST/products/") && data.arg1.endsWith("wish")) {
            val arr = data.arg1.split("/")
            val idx = arr[2]
            if ( mProductAdapter != null )
                mProductAdapter?.likeSuccess(idx)
            if ( mAdapter != null )
                mAdapter?.changeZzim(idx)
        }
    }

    private fun handleNetworkAPI115(data: NetworkBusResponse) {
        if("ok" == data.arg2) {
            val response:API115 = Gson().fromJson(DBManager.getInstance(this).get(data.arg1), API115::class.java)

            textViewTitle.text = response.data.evTitle

            if(response.data.sub_img1.isNotEmpty()) {
                imageViewEvent.visibility = View.VISIBLE
                imageViewEvent.setRatio(response.data.sub_img1_width.toFloat() / response.data.sub_img1_height.toFloat())
                ImageLoad.setImage(
                        this,
                        imageViewEvent,
                        response.data.sub_img1,
                        null,
                        ImageLoad.SCALE_NONE,
                        DiskCacheStrategy.ALL)
            } else {
                imageViewEvent.visibility = View.GONE
            }

            if(response.data.sub_img2.isNotEmpty()) {
                imageViewEvent1.visibility = View.VISIBLE
                imageViewEvent1.setRatio(response.data.sub_img2_width.toFloat() / response.data.sub_img2_height.toFloat())
                ImageLoad.setImage(
                        this,
                        imageViewEvent1,
                        response.data.sub_img2,
                        null,
                        ImageLoad.SCALE_NONE,
                        DiskCacheStrategy.ALL)
            } else {
                imageViewEvent1.visibility = View.GONE
            }

            if(response.data.sub_img3.isNotEmpty()) {
                imageViewEvent2.visibility = View.VISIBLE
                imageViewEvent2.setRatio(response.data.sub_img3_width.toFloat() / response.data.sub_img3_height.toFloat())
                ImageLoad.setImage(
                        this,
                        imageViewEvent2,
                        response.data.sub_img3,
                        null,
                        ImageLoad.SCALE_NONE,
                        DiskCacheStrategy.ALL)
            } else {
                imageViewEvent2.visibility = View.GONE
            }
        } else {
            val error:BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
            Logger.e("error : $error")
        }
    }

    private fun handleNetworkAPI116(data:NetworkBusResponse) {
        if("ok" == data.arg2) {
            val response: API116 = Gson().fromJson(DBManager.getInstance(this).get(data.arg1), API116::class.java)

            textViewTitle.text = response.data.evTitle

//            runOnUiThread {
//                var url = URL(response.data.mainImg)
//                var bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream())
//                Logger.e("bmWidth :: " + bmp.width + "bmHeight :: " + bmp.height)
//            }
            var imageUrl = response.data.sub_img1
            if ( TextUtils.isEmpty(imageUrl) ) {
                imageUrl = response.data.mainImg
            }

            try {
                Thread {
                    //Do some Network Request
                    var url = URL(imageUrl)
                    var bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream())
                    var viewWidth = AppPreferences.getScreenWidth(this@EventDetailActivity)
                    var imageWidth = bmp.width
                    var imageHeight = bmp.height
                    var viewHeight = (imageHeight * viewWidth) / imageWidth
                    var param = imageViewBanner.layoutParams
                    param.width = viewWidth
                    param.height = viewHeight
                    imageViewBanner.layoutParams = param
                    runOnUiThread {
                        ImageLoad.setImage(
                                this,
                                imageViewBanner,
                                imageUrl,
                                null,
                                ImageLoad.SCALE_NONE,
                                DiskCacheStrategy.ALL)
                    }
                }.start()
            } catch (e:Exception) {
                e.printStackTrace()
            }

            if(response.nTotalCount > 0) {
                mAdapter?.setItems(response.data.evPick[0].tagData)
            }
        } else {
            val error:BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
            Logger.e("error : $error")
        }
    }

    private fun handleNetworkAPI117(data:NetworkBusResponse) {
        if("ok" == data.arg2) {
            val response:API117 = Gson().fromJson(DBManager.getInstance(this).get(data.arg1), API117::class.java)

            textViewTitle.text = response.data.evTitle
            var imageUrl = response.data.sub_img1
            if ( TextUtils.isEmpty(imageUrl) ) {
                imageUrl = response.data.mainImg
            }

            try {
                Thread {
                    //Do some Network Request
                    var url = URL(imageUrl)
                    var bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream())
                    var viewWidth = AppPreferences.getScreenWidth(this@EventDetailActivity)
                    var imageWidth = bmp.width
                    var imageHeight = bmp.height
                    var viewHeight = 0
                    if ( imageWidth > 0 )
                        viewHeight = (imageHeight * viewWidth) / imageWidth
                    var param = imageViewBanner.layoutParams
                    param.width = viewWidth
                    param.height = viewHeight
                    imageViewBanner.layoutParams = param
                    runOnUiThread {
                        ImageLoad.setImage(
                                this,
                                imageViewBanner,
                                imageUrl,
                                null,
                                ImageLoad.SCALE_NONE,
                                DiskCacheStrategy.ALL)
                    }
                }.start()
            } catch (e:Exception) {
                e.printStackTrace()
            }

//            imageViewBanner.setRatio(response.data.sub_img1_width.toFloat() / response.data.sub_img1_height.toFloat())


            if(response.nTotalCount > 0) {
                mProductAdapter?.setItems(response.data.evPick[0].themeData)
            }
        } else {
            val error:BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
            Logger.e("error : $error")
            try {
                var obj = JSONObject(data.arg4)
                var message = obj.optString("message")
                if ( !TextUtils.isEmpty(message) )
                    AppToast(this@EventDetailActivity).showToastMessage(message,
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_BOTTOM)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            finish()
        }
    }

    private fun checkIntent(intent: Intent?) {
        if(intent != null) {
            eventId = intent.getStringExtra(INTENT_KEY_EVENT_ID)
            eventType = intent.getStringExtra(INTENT_KEY_EVENT_TYPE)
        }

        recyclerViewPromotionDetail.setHasFixedSize(true)
        recyclerViewPromotionDetail.setItemViewCacheSize(RECYCLER_VIEW_ITEM_CACHE_SIZE)
        recyclerViewPromotionDetail.isNestedScrollingEnabled = false

        if(eventType == EVENT_TYPE_VOD) {
            recyclerViewPromotionDetail.visibility = View.VISIBLE
            imageViewBanner.visibility = View.VISIBLE
            divider.visibility = View.VISIBLE
            layoutImage.visibility = View.GONE

            recyclerViewPromotionDetail.layoutManager = WrappedLinearLayoutManager(this).apply {
                orientation = androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
            }

            mAdapter = PromotionVODAdapter(this).apply {
                setListener(this@EventDetailActivity)
            }

            recyclerViewPromotionDetail.adapter = mAdapter

            NetworkBus(NetworkApi.API116.name, eventId).let {
                EventBus.getDefault().post(it)
            }
        } else if(eventType == EVENT_TYPE_PRODUCT) {
            recyclerViewPromotionDetail.visibility = View.VISIBLE
            imageViewBanner.visibility = View.VISIBLE
            divider.visibility = View.VISIBLE
            layoutImage.visibility = View.GONE

            recyclerViewPromotionDetail.layoutManager = WrappedLinearLayoutManager(this)
            mProductAdapter = PromotionProductAdapter(this).apply {
                setListener(this@EventDetailActivity)
            }

            recyclerViewPromotionDetail.adapter = mProductAdapter

            NetworkBus(NetworkApi.API117.name, eventId).let {
                EventBus.getDefault().post(it)
            }
        } else {
            recyclerViewPromotionDetail.visibility = View.GONE
            imageViewBanner.visibility = View.GONE
            divider.visibility = View.GONE
            layoutImage.visibility = View.VISIBLE

            NetworkBus(NetworkApi.API115.name, eventId).let {
                EventBus.getDefault().post(it)
            }
        }
    }
}