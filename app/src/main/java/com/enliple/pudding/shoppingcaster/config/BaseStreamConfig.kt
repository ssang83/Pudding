package com.enliple.pudding.shoppingcaster.config

import android.content.pm.ActivityInfo
import com.google.gson.GsonBuilder
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.ksyun.media.streamer.kit.StreamerConstants

/**
 * 기본 스트리밍 환경정보
 * (URL 을 제외한 나머지 기본설정은 정책에 따름)
 */
class BaseStreamConfig {
    @Expose
    @SerializedName("mUrl")
    var mUrl: String? = null

    @Expose
    @SerializedName("mCameraFacing")
    var mCameraFacing: Int = 1 // 0 : Back , 1: Front

    @Expose
    @SerializedName("mFrameRate")
    var mFrameRate: Float = 30f //15f // 30.0f 발열이 심하다

    @Expose
    @SerializedName("mVideoKBitrate")
    var mVideoKBitrate: Int = 2790

    @Expose
    @SerializedName("mAudioKBitrate")
    var mAudioKBitrate: Int = 128 //128 / 2

    @Expose
    @SerializedName("mTargetResolution")
    var mTargetResolution: Int = StreamerConstants.VIDEO_RESOLUTION_720P

    @Expose
    @SerializedName("mOrientation")
    var mOrientation: Int = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

    @Expose
    @SerializedName("mEncodeMethod")
    var mEncodeMethod: Int = StreamerConstants.ENCODE_METHOD_HARDWARE // ENCODE_METHOD_SOFTWARE 로도 테스트가 필요하다.

    @Expose
    @SerializedName("mAutoStart")
    var mAutoStart: Boolean = false

    @Expose
    @SerializedName("mShowDebugInfo")
    var mShowDebugInfo: Boolean = false


    fun fromJson(json: String): BaseStreamConfig {
        return GsonBuilder().create().fromJson(json, this::class.java)
    }

    fun toJson(): String {
        return GsonBuilder().create().toJson(this)
    }
}