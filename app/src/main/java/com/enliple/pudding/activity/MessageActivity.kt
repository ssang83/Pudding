package com.enliple.pudding.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.widget.RelativeLayout
import android.widget.Toast
import com.enliple.pudding.AbsBaseActivity
import com.enliple.pudding.R
import com.enliple.pudding.adapter.my.MessageAdapter
import com.enliple.pudding.commons.app.NetworkStatusUtils
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.NetworkApi
import com.enliple.pudding.commons.network.NetworkBus
import com.enliple.pudding.commons.network.NetworkBusResponse
import com.enliple.pudding.commons.network.vo.API54
import com.enliple.pudding.commons.network.vo.BaseAPI
import com.enliple.pudding.widget.MessageBlockDialog
import com.enliple.pudding.widget.MessageDeleteDialog
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_message.*
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

/**
 * Created by Kim Joonsung on 2018-10-23.
 */
class MessageActivity : AbsBaseActivity(), MessageAdapter.Listener {

    companion object {
        private const val RECYCLER_VIEW_ITEM_CACHE_SIZE = 20
    }

    private lateinit var moreData: API54.MessageItem
    private lateinit var mAdapter: MessageAdapter
    private var titleHeight: Int = 0
    private var dialog: MessageBlockDialog? = null
    private var delDialog: MessageDeleteDialog? = null
    private var itemPosition = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_message)

        EventBus.getDefault().register(this)

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenHeight = displayMetrics.heightPixels

        titleHeight = convertDpToPx(this@MessageActivity, 56)

        buttonClose.setOnClickListener(clickListener)

        recyclerViewMessage.setHasFixedSize(false)
        recyclerViewMessage.setItemViewCacheSize(RECYCLER_VIEW_ITEM_CACHE_SIZE)

        mAdapter = MessageAdapter(titleHeight, screenHeight)
        mAdapter.setListener(this@MessageActivity)
        recyclerViewMessage.adapter = mAdapter

        moreLayer.setOnTouchListener(touchListener)
        noReceiveAlarm.setOnClickListener(clickListener)
        deleteMessage.setOnClickListener(clickListener)
        banMessage.setOnClickListener(clickListener)
    }

    override fun onResume() {
        super.onResume()

        EventBus.getDefault().post(NetworkBus(NetworkApi.API54.name))
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onDozeModeStateChanged(dozeEnable: Boolean) {}

    override fun onNetworkStatusChanged(status: NetworkStatusUtils.NetworkStatus) {}

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onItemClicked(data: API54.MessageItem) {
        startActivity(Intent(this@MessageActivity, MyChatActivity::class.java).apply {
            putExtra(MyChatActivity.INTENT_EXTRA_KEY_CASTER_NICKNAME, data.send_mb_nick)
            putExtra(MyChatActivity.INTENT_EXTRA_KEY_CASTER_PROFILE, data.send_mb_img)
            putExtra(MyChatActivity.INTENT_EXTRA_KEY_CASTER_FOLLOW, if (data.isFollow == "1") true else false)
            putExtra(MyChatActivity.INTENT_EXTRA_KEY_CASTER_ID, data.send_mb_id)
            putExtra(MyChatActivity.INTENT_EXTRA_KEY_CASTER_LEAVE, data.is_send_mb_leave)
        })
    }

    override fun onMoreClicked(position: Int, data: API54.MessageItem, y: Int) {
        this.moreData = data
        this.itemPosition = position
        if (moreLayer.visibility == View.GONE) {
            val params = RelativeLayout.LayoutParams(convertDpToPx(this@MessageActivity, 126), convertDpToPx(this@MessageActivity, 150))
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE)
            params.setMargins(0, y, convertDpToPx(this@MessageActivity, 15), 0)
            layoutMore.layoutParams = params
            moreLayer.visibility = View.VISIBLE
            Logger.e("moreData.push :: " + moreData.push)
            if (moreData.push == "1") { // moreData.push == "1" 현재 해당 사용자의 알람이 설정되어 있다는 의미 이므로 알람 헤제 텍스트가 세팅 되어 있어야함
                noReceiveAlarm.text = "메시지 알림 해제"
            } else {
                noReceiveAlarm.text = "메시지 알림 설정"
            }
        } else {
            moreLayer.visibility = View.GONE
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: NetworkBusResponse) {
        if (data.arg1.startsWith(NetworkApi.API54.toString())) {
            loadData(data)
        } else if (data.arg1.startsWith(NetworkApi.API57.toString())) {
            if ("ok" == data.arg2) {
                dialog!!.dismiss()

                val bus = NetworkBus(NetworkApi.API54.name)
                EventBus.getDefault().post(bus)
            } else {
                val error: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
                Logger.e("error : $error")
            }
        } else if (data.arg1.startsWith(NetworkApi.API104.toString())) {
            if ("ok" == data.arg2) {
//                moreLayer.visibility = View.GONE
                delDialog!!.dismiss()
                mAdapter.deleteMessage(itemPosition)
            } else {
                val error: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
                Logger.e("error : $error")
            }
        } else if (data.arg1.startsWith(NetworkApi.API108.toString())) {
            if ("ok" == data.arg2) {
                moreLayer.visibility = View.GONE
                var message = ""
                if ( moreData.push == "1" ) {
                    message = "메시지 알림이 해제되었습니다."
                } else {
                    message = "메시지 알림이 허용되었습니다."
                }
                Toast.makeText(this@MessageActivity, message, Toast.LENGTH_SHORT).show()
                val bus = NetworkBus(NetworkApi.API54.name)
                EventBus.getDefault().post(bus)
            } else {
                val error: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
                Logger.e("error : $error")
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: String) {
        Logger.e("onMessageEvent:$data")
        if ("onMessageReceived" == data) {
            EventBus.getDefault().post(NetworkBus(NetworkApi.API54.name))
        }
    }

    private fun loadData(data: NetworkBusResponse) {
        if (data.arg2 == "ok") {
            val str = DBManager.getInstance(this).get(data.arg1)
            val response: API54 = Gson().fromJson(str, API54::class.java)

            setEmptyView(response.nTotalCount)
            mAdapter.setItem(response.data)
        } else {
            val error: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
            Logger.e("error : $error")
        }
    }

    private fun setEmptyView(count: Int) {
        if (count > 0) {
            textViewEmpty.visibility = View.GONE
            recyclerViewMessage.visibility = View.VISIBLE
        } else {
            textViewEmpty.visibility = View.VISIBLE
            recyclerViewMessage.visibility = View.GONE
        }
    }

    private val touchListener = object : View.OnTouchListener {
        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            if (v!!.id == R.id.moreLayer) {
                if (event!!.action == MotionEvent.ACTION_UP) {
                    if (moreLayer.visibility == View.VISIBLE) {
                        moreLayer.visibility = View.GONE
                    }
                }
            }
            return true
        }
    }

    private val clickListener = View.OnClickListener {
        when (it?.id) {
            R.id.buttonClose -> onBackPressed()
            R.id.banMessage -> {
                moreLayer.visibility = View.GONE
                dialog = MessageBlockDialog(this@MessageActivity, moreData, object : MessageBlockDialog.Listener {
                    override fun onDismiss() {
                        Logger.e("onDismiss")
                    }

                    override fun onBlock() {
                        Logger.e("onBlock")

                        val requestBody = JSONObject().apply {
                            put("user", AppPreferences.getUserId(this@MessageActivity))
                            put("block_user", moreData.send_mb_id)
                            put("status", "Y")
                        }

                        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), requestBody.toString())
                        val bus = NetworkBus(NetworkApi.API57.name, body)
                        EventBus.getDefault().post(bus)
                    }
                })
                dialog!!.show()
            }
            R.id.deleteMessage -> {
                moreLayer.visibility = View.GONE
                delDialog = MessageDeleteDialog(this@MessageActivity, moreData, object : MessageDeleteDialog.Listener {
                    override fun onDismiss() {
                        Logger.e("onDismiss")
                    }

                    override fun onDelete() {
                        Logger.e("onDelete")
                        val obj = JSONObject().apply {
                            put("user", AppPreferences.getUserId(this@MessageActivity)!!)
                            put("partner", moreData.send_mb_id)
                        }

                        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), obj.toString())
                        val bus = NetworkBus(NetworkApi.API104.name, body)
                        EventBus.getDefault().post(bus)
                    }
                })
                delDialog!!.show()



//                val obj = JSONObject().apply {
//                    put("user", AppPreferences.getUserId(this@MessageActivity)!!)
//                    put("partner", moreData.send_mb_id)
//                }
//
//                val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), obj.toString())
//                val bus = NetworkBus(NetworkApi.API104.name, body)
//                EventBus.getDefault().post(bus)
            }
            R.id.noReceiveAlarm -> {
                val obj = JSONObject().apply {
                    put("user", AppPreferences.getUserId(this@MessageActivity)!!)
                    put("partner", moreData.send_mb_id)
                    put("status", if (moreData.push == "1") "N" else "Y")
                }

                val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), obj.toString())
                val bus = NetworkBus(NetworkApi.API108.name, body)
                EventBus.getDefault().post(bus)
            }
        }
    }

    private fun convertDpToPx(context: Context, dp: Int): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), context.getResources().getDisplayMetrics()).toInt()
    }
}