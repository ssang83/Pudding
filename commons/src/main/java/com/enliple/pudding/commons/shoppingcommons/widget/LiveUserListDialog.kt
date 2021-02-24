package com.enliple.pudding.commons.shoppingcommons.widget

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import androidx.appcompat.app.AppCompatDialog
import android.widget.Toast
import com.enliple.pudding.commons.R
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.NetworkApi
import com.enliple.pudding.commons.network.NetworkBus
import com.enliple.pudding.commons.network.NetworkBusResponse
import com.enliple.pudding.commons.network.NetworkHandler
import com.enliple.pudding.commons.network.vo.API99
import com.enliple.pudding.commons.network.vo.BaseAPI
import com.enliple.pudding.commons.shoppingcommons.adapter.LiveUserListAdapter
import com.google.gson.Gson
import kotlinx.android.synthetic.main.dialog_user_list.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import java.lang.reflect.InvocationTargetException
import java.util.HashMap
import kotlin.collections.ArrayList

/**
 * 방송 채팅 참여자 리스트 팝업
 */
class LiveUserListDialog : AppCompatDialog {
    companion object {
        private const val TAG = "LiveUserListDialog"
        private const val RECYCLER_VIEW_ITEM_CACHE_COUNT = 20
    }

    private val users: MutableList<String> = ArrayList()
    private var mAdapter: LiveUserListAdapter? = null
    private var mRoomID = ""

    private val textWaterListener = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            val text = s?.toString()
            mAdapter?.getFilter(text!!)
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    constructor(context: Context) : super(context, R.style.UserListDialogStyle) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        var windowParams = window.attributes
        windowParams.gravity = Gravity.RIGHT
        windowParams.width = context.resources.getDimensionPixelSize(R.dimen.chat_user_list_layout_width)
        windowParams.height = WindowManager.LayoutParams.MATCH_PARENT
        window.attributes = windowParams

        setCancelable(true)
        setCanceledOnTouchOutside(true)

        setContentView(LayoutInflater.from(context).inflate(R.layout.dialog_user_list, null, false))

        EventBus.getDefault().register(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        editSearch.addTextChangedListener(textWaterListener)
        buttonRefresh.setOnClickListener { NetworkBus(NetworkApi.API99.name, mRoomID).let{ EventBus.getDefault().post(it) } }

        mAdapter = com.enliple.pudding.commons.shoppingcommons.adapter.LiveUserListAdapter().apply {
            setItems(users)
        }

        recyclerViewUserList.setItemViewCacheSize(RECYCLER_VIEW_ITEM_CACHE_COUNT)
        recyclerViewUserList.setHasFixedSize(true)
        recyclerViewUserList.adapter = mAdapter
    }

    override fun dismiss() {
        super.dismiss()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: NetworkBusResponse) {
        var API99 = NetworkHandler.getInstance(context!!).getKey(NetworkApi.API99.toString(), "", "")

        if (data.arg1.startsWith(API99)) {
            handleNetworkFavorResult(data)
        }
    }

    fun setItems(items: HashMap<String, String>) {
        if (items == null || items.size == 0) {
            return
        }

        var collection = items.values
        var list = ArrayList<String>(collection)
        setItems(list)
    }

    fun setItems(items: List<String>) {
        this.users.clear()
        // 방송을 개시하는 방장은 제외하고 화면에 출력하기 위해 필터링
        if (items != null) {
            for (i in 0 until items.size) {
                Logger.e(TAG, "${items[i]}")
                try {
                    if (items[i] == "*" || JSONObject(items[i])?.getString("an") == "1") {
                        continue
                    } else {
                        this.users.add(items[i])
                    }
                } catch (e: InvocationTargetException) {
                }
            }
        }

//        mAdapter?.setItems(this.users)
    }

    fun setChatKey(chatKey: String) {
        mRoomID = chatKey
        NetworkBus(NetworkApi.API99.name, mRoomID).let{ EventBus.getDefault().post(it) }
    }

    private fun handleNetworkFavorResult(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            var str = DBManager.getInstance(context!!).get(data.arg1)
            var response: API99 = Gson().fromJson(str, API99::class.java)

            if (response.cnt > 0) {
                mAdapter?.setItems(response.users)
            }

        } else {
            var errorResult: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
            Logger.e("error : $errorResult")
        }
    }
}