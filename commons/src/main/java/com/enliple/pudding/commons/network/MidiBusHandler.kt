package com.enliple.pudding.commons.network

import android.content.Context
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Callback

/**
 * Created by Kim Joonsung on 2018-12-18.
 * Network MIDIBUS 통신 Retrofit 구현부
 */
class MidiBusHandler {

    companion object {
        @Volatile
        private var instance: MidiBusHandler? = null

        private lateinit var mContext: Context
        private lateinit var mManager: MidiBusManager

        fun getInstance(context: Context, baseUrl: String): MidiBusHandler {
            if (instance == null) {
                instance = MidiBusHandler()
                mManager = MidiBusManager(context, baseUrl)
                mContext = context
            }

            return instance as MidiBusHandler
        }
    }

    fun run(n: NetworkMidiBus) {
        when (n.arg1) {
            NetworkApi.API71.name -> postVODFileUpload(n.arg2, n.arg3, n.arg4, n.part)
        }
    }

    private fun postVODFileUpload(xMBusToken: String, xMBusChannel: String, relativePath: String, file: MultipartBody.Part) {
        mManager.service.postVODFileUpload(xMBusToken, xMBusChannel, relativePath, file)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }
}