package com.enliple.pudding

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.enliple.pudding.bus.ActivityResultBus
import com.enliple.pudding.commons.db.VideoDBManager
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.NetworkBus
import com.enliple.pudding.commons.network.NetworkBusResponse
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

/**
 * jhs
 */
open class VideoBaseFragment : androidx.fragment.app.Fragment(), androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener {
    var isRefresh: Boolean = false
    var mIsVisibleToUser: Boolean = false

    override fun onRefresh() {
        Logger.e("onRefresh")

        isRefresh = true

//        EventBus.getDefault().post(NetworkBus(NetworkApi.VOD0.name, "top", "1"))
//        EventBus.getDefault().post(NetworkBus(NetworkApi.VOD0.name, "follow", "1"))
//        EventBus.getDefault().post(NetworkBus(NetworkApi.VOD0.name, "live", "1"))
//        EventBus.getDefault().post(NetworkBus(NetworkApi.VOD0.name, "vod", "1"))
        EventBus.getDefault().post(NetworkBus("refresh_all_video"))

        Logger.e("delete all video db")
        VideoDBManager.getInstance(context).deleteAll()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        EventBus.getDefault().register(this)
    }

    override fun onDestroy() {
        super.onDestroy()

        EventBus.getDefault().unregister(this)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)

        Logger.d("setUserVisibleHint:$isVisibleToUser")
        mIsVisibleToUser = isVisibleToUser
    }

    @Subscribe()
    fun onMessageEvent(data: NetworkBusResponse) {
        handleNetworkResult(data)

        if (isRefresh) {
            isRefresh = false
        }
    }

    @Subscribe()
    fun handleSomethingElse(bus: ActivityResultBus) {
        moveVideo(bus.requestCode, bus.resultCode)
    }

    open fun playVideo() {}

    open fun stopVideo() {}

    open fun pauseVideo() {}

    open fun handleNetworkResult(data: NetworkBusResponse) {}

    open fun moveVideo(tab: Int, position: Int) {}
}