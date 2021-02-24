package com.enliple.pudding

import android.content.BroadcastReceiver
import android.content.IntentFilter
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.enliple.pudding.commons.internal.AppConstants

/**
 * Created by hkcha on 2018-01-12.
 */
open class AbsBaseFragment : androidx.fragment.app.Fragment() {

    var mLocalBroadcastManager: androidx.localbroadcastmanager.content.LocalBroadcastManager? = null
    var mFilter: IntentFilter = IntentFilter()

    init {
        mFilter.addAction(AppConstants.BROADCAST_ACTION_REFRESH)
        mFilter.addAction(AppConstants.BROADCAST_ACTION_TAB_CHANGE)
    }

    fun registerReceiver(receiver: BroadcastReceiver, additionalFilters: MutableList<String>?) {
        if (activity == null) return

        if (mLocalBroadcastManager == null) {
            mLocalBroadcastManager = androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(activity as androidx.fragment.app.FragmentActivity)
        }

        additionalFilters?.forEach {
            mFilter.addAction(it)
        }

        mLocalBroadcastManager?.registerReceiver(receiver, mFilter)
    }

    fun unRegisterReceiver(receiver: BroadcastReceiver) = mLocalBroadcastManager?.unregisterReceiver(receiver)

    protected fun getFragmentName(): String {
        return javaClass.simpleName
    }
}