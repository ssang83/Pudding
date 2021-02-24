package com.enliple.pudding

import android.content.*
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.appcompat.app.AppCompatDialog
import com.enliple.pudding.commons.internal.AppConstants
import com.enliple.pudding.commons.log.Logger

/**
 * Created by hkcha on 2018-01-24.
 * App 전용 Custom Dialog 기본 구현
 */
open class AbsBaseDialog(context: Context?, cancelable: Boolean, cancelListener: DialogInterface.OnCancelListener?)
    : AppCompatDialog(context, cancelable, cancelListener) {

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        registerBR()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        unRegisterBR()
    }

    private fun registerBR() {
        var filter = IntentFilter()
        filter.addAction(AppConstants.BROADCAST_ACTION_TERMINATE)
        androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(context).registerReceiver(broadcastReceiver, filter)
    }

    private fun unRegisterBR() {
        androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(context).unregisterReceiver(broadcastReceiver)
    }

    fun getDialogName(): String {
        return javaClass.simpleName
    }

    private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) return

            val action = intent.action
            if (action != null) {
                if (action == AppConstants.BROADCAST_ACTION_TERMINATE) {
                    var terminateType = intent.getIntExtra(AppConstants.BROADCAST_EXTRA_KEY_TERMINATE_TYPE, AppConstants.BROADCAST_EXTRA_VALUE_TERMINATE_ALL)
                    if (terminateType == AppConstants.BROADCAST_EXTRA_VALUE_TERMINATE_ALL
                            || terminateType == AppConstants.BROADCAST_EXTRA_VALUE_TERMINATE_DIALOG_ONLY) {
                        Logger.d("Terminate Dialog(Dismiss)")
                        dismiss()
                    }
                }
            }
        }
    }

}