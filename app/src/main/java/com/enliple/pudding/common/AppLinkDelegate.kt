package com.enliple.pudding.common

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.enliple.pudding.activity.EventDetailActivity
import com.enliple.pudding.commons.internal.AppConstants
import com.enliple.pudding.commons.log.Logger

/**
 * Created by Kim Joonsung on 2019-02-13.
 */
class AppLinkDelegate {

    companion object {
        private var mInstance: AppLinkDelegate? = null
        private lateinit var mContext:Context

        fun getInstance(context: Context) : AppLinkDelegate {
            if(mInstance == null) {
                mInstance = AppLinkDelegate()
                mContext = context
            }

            return mInstance as AppLinkDelegate
        }
    }

    fun process(uriScheme:String, uriLocation:String) {
        try {
            Logger.e("Proccess App Link - SCHEME : $uriScheme\n URL : $uriLocation")

            if(uriScheme == null) {
                return
            }

            var linkUri = Uri.parse(uriLocation)
            var host = linkUri.host

            if(host == null) return

            when(host) {
                AppConstants.APP_LINK_HOST_EVENT -> {
                    val evId = linkUri.getQueryParameter(AppConstants.APP_LINK_PARAM_EV_ID)
                    val evType = linkUri.getQueryParameter(AppConstants.APP_LINK_PARAM_EV_TYPE)

                    Logger.e("AppLink Target : Event -> EventId : $evId, EventType : $evType")
                    launchEventDetail(evId!!, evType!!)
                }
            }
        } catch (e:Exception) {
           Logger.p(e)
        }
    }

    /**
     * 이벤트 상세 화면으로 이동
     */
    private fun launchEventDetail(evId:String, evType:String) {
        mContext.startActivity(Intent(mContext, EventDetailActivity::class.java).apply {
            putExtra(EventDetailActivity.INTENT_KEY_EVENT_ID, evId)
            putExtra(EventDetailActivity.INTENT_KEY_EVENT_TYPE, evType)
        })
    }
}