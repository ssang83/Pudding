package com.enliple.pudding.widget

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.provider.Settings
import android.view.LayoutInflater
import android.view.Window
import androidx.appcompat.app.AppCompatDialog
import com.enliple.pudding.R
import com.enliple.pudding.activity.LinkWebViewActivity
import com.enliple.pudding.activity.LoginActivity
import com.enliple.pudding.activity.ProductDetailActivity
import com.enliple.pudding.adapter.ProductListAdapter
import com.enliple.pudding.bus.VideoPipBus
import com.enliple.pudding.bus.ZzimStatusBus
import com.enliple.pudding.commons.app.ShopTreeKey
import com.enliple.pudding.commons.data.DialogModel
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.NetworkApi
import com.enliple.pudding.commons.network.NetworkBus
import com.enliple.pudding.commons.network.NetworkBusResponse
import com.enliple.pudding.commons.network.vo.API139
import com.enliple.pudding.commons.shoptree.network.ShopTreeAsyncTask
import com.enliple.pudding.commons.widget.toast.AppToast
import com.google.gson.Gson
import kotlinx.android.synthetic.main.dialog_player_products.*
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

/**
 * 시청간 상품 버튼을 눌렀을때 상품정보 리스트를 표시시키는 팝업
 * @author hkcha
 * @since 2018.08.29
 */
class PlayerProductDialog : AppCompatDialog, ProductListAdapter.Listener {
    companion object {
        private const val RECYCLER_VIEW_ITEM_CACHE_COUNT = 20
    }

    private lateinit var mAdapter: ProductListAdapter
    private var mLinkTitle = ""
    private var mVideoPipBus: VideoPipBus? = null
    private var productId = ""
    private var mItems:List<DialogModel> = mutableListOf()
    private lateinit var mSelectItem: DialogModel
    private lateinit var mSelectUrl: String
    private var mIsCast = false     // 방송자인지 아닌지 구별하는 변수

    constructor(context: Context, items: List<DialogModel>, bus: VideoPipBus?, isCast:Boolean) : super(context, R.style.PlayerProductListDialog) {
        if (items == null) {
            return
        }

        mVideoPipBus = bus
        mItems = items
        mIsCast = isCast

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        var count = items.size
        Logger.d("productCount $count")

        setCancelable(true)
        setCanceledOnTouchOutside(true)

        val view = LayoutInflater.from(context).inflate(R.layout.dialog_player_products, null, false)
        setContentView(view)
        view.post {
            recyclerViewProducts.setHasFixedSize(true)
            recyclerViewProducts.setItemViewCacheSize(RECYCLER_VIEW_ITEM_CACHE_COUNT)

            mAdapter = ProductListAdapter().apply {
                setListener(this@PlayerProductDialog)
            }
            recyclerViewProducts.adapter = mAdapter
            mAdapter.setItems(items)
        }

        productCount?.text = String.format(view!!.context.getString(R.string.msg_product_count), count)

        EventBus.getDefault().register(this)

        mainLayer.setOnClickListener { dismiss() }
        buttonClose.setOnClickListener { dismiss() }
    }

    override fun dismiss() {
        super.dismiss()
        EventBus.getDefault().unregister(this)
    }

    override fun onProductItemClicked(position: Int, item: DialogModel) {
        Logger.e("onProductItemClicked() $position")
        mSelectItem = item
        if(!mIsCast) {
            if(item.type == "1") {
                if (mVideoPipBus != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                    if( ! AppPreferences.getPipPermission(context)) {
//                        AppPreferences.setPipPermission(context, true)
//                        EventBus.getDefault().post("startActivityForResult_OVERLAY")
//                    } else {
////                        dismiss()
//                        startProductDetailActivity()
//                        if (Settings.canDrawOverlays(context))
//                            sendVideoPipBus()
//                    }
                    startProductDetailActivity()
                    if (Settings.canDrawOverlays(context))
                        sendVideoPipBus()
                } else {
//                    dismiss()
                    startProductDetailActivity()
                    sendVideoPipBus()
                }
            } else {
                mLinkTitle = item.name!!

                val task = ShopTreeAsyncTask(context)
                task.getDRCLink(item.idx, item.streamKey, "", item?.type) { _, obj ->
                    try {
                        val response: API139 = Gson().fromJson(obj.toString(), API139::class.java)
                        if(response.result == "success") {

                            mSelectUrl = response.url

                            if (mVideoPipBus != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                                if( ! AppPreferences.getPipPermission(context)) {
//                                    AppPreferences.setPipPermission(context, true)
//                                    EventBus.getDefault().post("startActivityForResult_OVERLAY")
//                                } else {
////                                    dismiss()
//                                    startLinkWebViewActivity()
//                                    if (Settings.canDrawOverlays(context))
//                                        sendVideoPipBus()
//                                }
                                startLinkWebViewActivity()
                                if (Settings.canDrawOverlays(context))
                                    sendVideoPipBus()
                            } else {
//                                dismiss()
                                startLinkWebViewActivity()
                                sendVideoPipBus()
                            }
                        } else {
                            val errorJson = JSONObject(obj.toString())
                            AppToast(context).showToastMessage(errorJson["message"].toString(),
                                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                                    AppToast.GRAVITY_BOTTOM)
                        }
                    } catch (e:Exception) {
                        Logger.p(e)
                    }
                }
            }
        }

//        dismiss()

        //EventBus.getDefault().post("onProductItemClicked")
    }

    private fun startProductDetailActivity() = context.startActivity(Intent(context, ProductDetailActivity::class.java).apply {
        putExtra(ShopTreeKey.KEY_IDX, mSelectItem.idx)
        putExtra(ShopTreeKey.KEY_PCODE, mSelectItem.pcode)
        putExtra(ShopTreeKey.KEY_SCCODE, mSelectItem.scCode)
        putExtra(ShopTreeKey.KEY_STREAM_KEY, mSelectItem.streamKey)
        putExtra(ShopTreeKey.KEY_VOD_TYPE, mSelectItem.vodType)
        putExtra(ShopTreeKey.KEY_RECOMMEND_ID, mSelectItem.recommendId)
    })

    private fun startLinkWebViewActivity() = context.startActivity(Intent(context, LinkWebViewActivity::class.java).apply {
        putExtra("LINK", mSelectUrl)
        putExtra("IDX", mSelectItem?.idx)
        putExtra("TYPE", mSelectItem?.type)
        putExtra("IS_WISH", mSelectItem?.is_wish)
        putExtra("TITLE", mLinkTitle)
        putExtra("ITEM_LINK", true)
    })

    override fun onProductCartClicked(position: Int, item: DialogModel) {
        Logger.e("onProductCartClicked(): $position")

        dismiss()

        //EventBus.getDefault().post("onProductCartClicked")
        sendVideoPipBus()
    }

    override fun onProductZzimClicked(position: Int, item: DialogModel) {
        Logger.e("onProductZzimClicked(): $position")
        productId = item.idx

        if(AppPreferences.getLoginStatus(context!!)) {
            JSONObject().apply {
                put("user", AppPreferences.getUserId(context!!)!!)
                put("is_wish", if("Y" == item.is_wish) "N" else "Y")
                put("type", item.type)
            }.let {
                val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), it.toString())
                EventBus.getDefault().post(NetworkBus(NetworkApi.API126.name, item.idx, body))
            }
        } else {
            context.startActivity(Intent(context!!, LoginActivity::class.java))
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(bus: ZzimStatusBus) {
        val idx = bus.productId
        val status = bus.status
        productId = idx
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: NetworkBusResponse) {
        Logger.e("Data.arg1 ::: " + data.arg1)
        if(data.arg1 == "POST/products/${productId}/wish") {
            if("ok" == data.arg2) {
                sendZzimStatusBus(productId)
                mAdapter.updateZzim(productId)
            }
        }
    }

    private fun sendZzimStatusBus(productId:String) {
        var is_wish =""
        var wish_cnt = ""

        try {
            for(i in 0 until mItems.size) {
                val data = mItems[i]
                if(mItems[i].idx == productId) {
                    var count = data.wish_cnt.toInt()
                    if("Y" == data.is_wish) {
                        if(count > 0) {
                            --count
                        }
                        is_wish = "N"
                        wish_cnt = count.toString()
                    } else {
                        ++count
                        is_wish = "Y"
                        wish_cnt = count.toString()
                    }
                }
            }

            EventBus.getDefault().post(ZzimStatusBus(productId, is_wish, wish_cnt))
        } catch (e : Exception) {
            Logger.p(e)
        }

    }

    private fun sendVideoPipBus() {
        if (mVideoPipBus != null) {
            Logger.e("sendVideoPipBus: ${mVideoPipBus?.url}")

            EventBus.getDefault().post(mVideoPipBus)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(bus: String) {
        if(bus == "canDrawOverlays==true") {

            if(mSelectItem.type == "1")
                startProductDetailActivity()
            else
                startLinkWebViewActivity()

            dismiss()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(context))
                sendVideoPipBus()
        }
    }
}