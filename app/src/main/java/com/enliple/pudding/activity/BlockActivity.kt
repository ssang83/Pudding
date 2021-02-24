package com.enliple.pudding.activity

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import android.widget.RelativeLayout
import com.enliple.pudding.AbsBaseActivity
import com.enliple.pudding.R
import com.enliple.pudding.adapter.my.BlockAdapter
import com.enliple.pudding.commons.app.NetworkStatusUtils
import com.enliple.pudding.commons.app.Utils
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.NetworkApi
import com.enliple.pudding.commons.network.NetworkBus
import com.enliple.pudding.commons.network.NetworkBusResponse
import com.enliple.pudding.commons.network.vo.API58
import com.enliple.pudding.commons.network.vo.BaseAPI
import com.enliple.pudding.commons.widget.toast.AppToast
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_block_list.*
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject


/**
 * Created by Kim Joonsung on 2018-10-29.
 */
class BlockActivity : AbsBaseActivity(), BlockAdapter.Listener {

    companion object {
        private const val TAG = "BlockActivity"

        private const val RECYCLER_VIEW_ITEM_CACHE_SIZE = 20
    }

    private lateinit var mAdapter: BlockAdapter
    private var selectedItem: API58.BlockItem? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_block_list)
        EventBus.getDefault().register(this)

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenHeight = displayMetrics.heightPixels

        buttonClose.setOnClickListener(clickListener)
        cancelBlock.setOnClickListener(clickListener)

        recyclerVeiwBlock.setHasFixedSize(false)
        recyclerVeiwBlock.setItemViewCacheSize(RECYCLER_VIEW_ITEM_CACHE_SIZE)

        mAdapter = BlockAdapter(screenHeight, Utils.GetStatusBarHeight(this@BlockActivity))
        recyclerVeiwBlock.adapter = mAdapter
        mAdapter.setListener(this@BlockActivity)

        popupLayer.setOnTouchListener(touchListener)

        val bus = NetworkBus(NetworkApi.API58.name)
        EventBus.getDefault().post(bus)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onDozeModeStateChanged(dozeEnable: Boolean) {}
    override fun onNetworkStatusChanged(status: NetworkStatusUtils.NetworkStatus) {}

    override fun onItemClicked(data: API58.BlockItem) {

    }

    override fun getPopupStartY(y: Int, item: API58.BlockItem) {
        if (popupLayer.visibility == View.GONE) {
            selectedItem = item
            val params = RelativeLayout.LayoutParams(Utils.ConvertDpToPx(this@BlockActivity, 140), Utils.ConvertDpToPx(this@BlockActivity, 40))
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE)
            params.setMargins(0, y, Utils.ConvertDpToPx(this@BlockActivity, 15), 0)
            popup.layoutParams = params
            popupLayer.visibility = View.VISIBLE
        } else {
            popupLayer.visibility = View.GONE
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: NetworkBusResponse) {
        if (data.arg1.startsWith(NetworkApi.API58.toString())) {
            loadData(data)
        } else if (data.arg1.startsWith(NetworkApi.API57.toString())) {
            if ("ok" == data.arg2) {
                popupLayer.visibility = View.GONE

                val bus = NetworkBus(NetworkApi.API58.name)
                EventBus.getDefault().post(bus)
/*                val dialog = SingleNotDelDialog(this@BlockActivity, resources.getString(R.string.msg_block_canceled), resources.getString(R.string.msg_cre_confirm), object: SingleNotDelDialog.Listener {
                    override fun onClick() {

                    }
                })
                dialog.show()*/
            } else {
                val error: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
                Logger.e("error : $error")

                AppToast(this).showToastMessage(error.message,
                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                        AppToast.GRAVITY_BOTTOM)
            }
        }
    }

    private val touchListener = object : View.OnTouchListener {
        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            if (v!!.id == R.id.popupLayer) {
                if (event!!.action == MotionEvent.ACTION_UP) {
                    if (popupLayer.visibility == View.VISIBLE) {
                        popupLayer.visibility = View.GONE
                    }
                }
            }
            return true
        }
    }

    private fun loadData(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            val response: API58 = Gson().fromJson(DBManager.getInstance(this@BlockActivity).get(data.arg1), API58::class.java)

            if (response.nTotalCount > 0) {
                recyclerVeiwBlock.visibility = View.VISIBLE
                layoutEmpty.visibility = View.GONE

                mAdapter.setItems(response.data)
            } else {
                recyclerVeiwBlock.visibility = View.GONE
                layoutEmpty.visibility = View.VISIBLE
            }
        } else {
            val error: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
            Logger.e("error : $error")
        }
    }

    private val clickListener = View.OnClickListener {
        when (it?.id) {
            R.id.buttonClose -> onBackPressed()

            R.id.cancelBlock -> {
                val requestBody = JSONObject().apply {
                    put("user", AppPreferences.getUserId(this@BlockActivity))
                    put("block_user", selectedItem?.block_user)
                    put("status", "N")
                }

                val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), requestBody.toString())
                val bus = NetworkBus(NetworkApi.API57.name, body)
                EventBus.getDefault().post(bus)
            }
        }
    }
}