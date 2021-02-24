package com.enliple.pudding.fcm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.text.TextUtils
import androidx.core.app.NotificationCompat
import com.enliple.pudding.R
import com.enliple.pudding.activity.MainActivity
import com.enliple.pudding.activity.MessageActivity
import com.enliple.pudding.commons.app.Utils
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.NetworkApi
import com.enliple.pudding.commons.network.NetworkBus
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import okhttp3.MultipartBody
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import java.util.concurrent.atomic.AtomicInteger


/**
 * Created by Kim Joonsung on 2018-12-07.
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {
    companion object {
        const val NOTIFICATION_CHANNEL_ID = "Pudding_Channel_ID"

        // NOTIFICATIOM ID 값 추출
        private val c = AtomicInteger(0)
        fun getNotificationID() = c.incrementAndGet()
    }
    // compile error :: 'onNewToken' overrides nothing 발생으로 token: String? -> token: String 으로 변경
    override fun onNewToken(token: String) {
        Logger.e("onNewToken : $token")
        if ( token != null ) {
            AppPreferences.setPushToken(this, token!!)
            registerPushToken()
        }
    }
    // compile error :: 'onNewToken' overrides nothing 발생으로 message: RemoteMessage? -> message: RemoteMessage 으로 변경
    override fun onMessageReceived(message: RemoteMessage) {
        if (message != null) {
            Logger.e("onMessageReceived body: ${message?.notification?.body}")
            Logger.e("onMessageReceived tag: ${message?.notification?.tag}")
            Logger.e("onMessageReceived title: ${message?.notification?.title}")
            Logger.e("onMessageReceived data: ${message?.data}")
            //Logger.e("onMessageReceived body: ${message?.notification?.}")
            sendNotification(message)
        }
    }

    fun getParsingData(tag: String, jsonStr: String?): String? = if (TextUtils.isEmpty(jsonStr)) {
        null
    } else JSONObject(jsonStr).getString(tag)

    private fun sendNotification(message: RemoteMessage?) {
        if (message == null) {
            Logger.e("message null !!")
            return
        }

        var type = message.data["type"]
        Logger.e("sendNotification type: $type")

        if ("message" == type) {
            EventBus.getDefault().post("onMessageReceived")

            var id = message.data["send_id"]
            var name = message.data["send_name"]
            var body = message.data["body"]
            Logger.e("id:$id name:$name body:$body")

            var context = this@MyFirebaseMessagingService
            val intent = Intent(context, MessageActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            var pi = PendingIntent.getActivity(context, System.currentTimeMillis().toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT)

            var builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                    .setAutoCancel(true)
                    .setContentTitle("보낸사람: $name($id)")
                    .setContentText(message?.notification?.body)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setColor(Color.parseColor("#9f56f2"))
                    .setContentText(body)
                    .setContentIntent(pi)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)

            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val appName = context.applicationInfo.loadLabel(packageManager).toString()
                val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, appName, NotificationManager.IMPORTANCE_HIGH).apply {
                    setShowBadge(true)
                }
                nm.createNotificationChannel(channel)
            }

            nm.notify(getNotificationID(), builder.build())
        } else if("live" == type) {             // 방송 시작 관련
            // {img_url_click=, body=[ 테스터2 ] 님이 라이브 방송을 시작 했습니다., type=live, send_name=, title=방송 시작 알림, img_url=, send_id=}
            var id = message.data["send_id"]
            var title = message.data["title"]
            var name = message.data["send_name"]
            var body = message.data["body"]
            var streamKey = message.data["img_url_click"]
            Logger.e("id:$id name:$name body:$body streamKey:$streamKey")

            var context = this@MyFirebaseMessagingService
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra("noti_type", type)
            intent.putExtra("streamKey", streamKey)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            var pi = PendingIntent.getActivity(context, System.currentTimeMillis().toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT)

            var builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                    .setAutoCancel(true)
                    .setContentTitle(title)
                    .setContentText(message?.notification?.body)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setColor(Color.parseColor("#9f56f2"))
                    .setContentText(body)
                    .setContentIntent(pi)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)

            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val appName = context.applicationInfo.loadLabel(packageManager).toString()
                val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, appName, NotificationManager.IMPORTANCE_HIGH).apply {
                    setShowBadge(true)
                }
                nm.createNotificationChannel(channel)
            }

            nm.notify(getNotificationID(), builder.build())
        } else if("system" == type) {           // 전체 혹은 시스템 관련
            var title = message.data["title"]
            var id = message.data["send_id"]
            var name = message.data["send_name"]
            var body = message.data["body"]
            Logger.e("id:$id name:$name body:$body")

            var context = this@MyFirebaseMessagingService
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra("noti_type", type)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            var pi = PendingIntent.getActivity(context, System.currentTimeMillis().toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT)

            var builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                    .setAutoCancel(true)
                    .setContentTitle(title)
                    .setContentText(message?.notification?.body)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setColor(Color.parseColor("#9f56f2"))
                    .setContentText(body)
                    .setContentIntent(pi)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)

            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val appName = context.applicationInfo.loadLabel(packageManager).toString()
                val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, appName, NotificationManager.IMPORTANCE_HIGH).apply {
                    setShowBadge(true)
                }
                nm.createNotificationChannel(channel)
            }

            nm.notify(getNotificationID(), builder.build())
        } else if("noti_follow" == type) {      // 알림 - 팔로우 리스트 이동
            var title = message.data["title"]
            var id = message.data["send_id"]
            var name = message.data["send_name"]
            var body = message.data["body"]
            Logger.e("id:$id name:$name body:$body")

            var context = this@MyFirebaseMessagingService
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra("noti_type", type)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            var pi = PendingIntent.getActivity(context, System.currentTimeMillis().toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT)

            var builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                    .setAutoCancel(true)
                    .setContentTitle(title)
                    .setContentText(message?.notification?.body)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setColor(Color.parseColor("#9f56f2"))
                    .setContentText(body)
                    .setContentIntent(pi)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)

            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val appName = context.applicationInfo.loadLabel(packageManager).toString()
                val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, appName, NotificationManager.IMPORTANCE_HIGH).apply {
                    setShowBadge(true)
                }
                nm.createNotificationChannel(channel)
            }

            nm.notify(getNotificationID(), builder.build())
        } else if("noti_order" == type) {       // 알림 - My쇼핑 이동
            var title = message.data["title"]
            var id = message.data["send_id"]
            var name = message.data["send_name"]
            var body = message.data["body"]
            Logger.e("id:$id name:$name body:$body")

            var context = this@MyFirebaseMessagingService
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra("noti_type", type)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            var pi = PendingIntent.getActivity(context, System.currentTimeMillis().toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT)

            var builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                    .setAutoCancel(true)
                    .setContentTitle(title)
                    .setContentText(message?.notification?.body)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setColor(Color.parseColor("#9f56f2"))
                    .setContentText(body)
                    .setContentIntent(pi)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)

            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val appName = context.applicationInfo.loadLabel(packageManager).toString()
                val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, appName, NotificationManager.IMPORTANCE_HIGH).apply {
                    setShowBadge(true)
                }
                nm.createNotificationChannel(channel)
            }

            nm.notify(getNotificationID(), builder.build())
        } else if("noti_format" == type) {      // 알림 - 편성표 이동
//            var id = message.data["send_id"]
            var name = message.data["send_name"]
            var title = message.data["title"]
            var body = message.data["body"]
            Logger.e("title:$title name:$name body:$body")

            var context = this@MyFirebaseMessagingService
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra("noti_type", type)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            var pi = PendingIntent.getActivity(context, System.currentTimeMillis().toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT)

            var builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                    .setAutoCancel(true)
                    .setContentTitle(title)
                    .setContentText(message?.notification?.body)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setColor(Color.parseColor("#9f56f2"))
                    .setContentText(body)
                    .setContentIntent(pi)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)

            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val appName = context.applicationInfo.loadLabel(packageManager).toString()
                val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, appName, NotificationManager.IMPORTANCE_HIGH).apply {
                    setShowBadge(true)
                }
                nm.createNotificationChannel(channel)
            }

            nm.notify(getNotificationID(), builder.build())
        } else if("noti_sanction" == type) {    // 알림 - My쇼핑 -> 설정 -> 제재내역 이동
            var title = message.data["title"]
            var id = message.data["send_id"]
            var name = message.data["send_name"]
            var body = message.data["body"]
            Logger.e("id:$id name:$name body:$body")

            var context = this@MyFirebaseMessagingService   
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra("noti_type", type)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            var pi = PendingIntent.getActivity(context, System.currentTimeMillis().toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT)

            var builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                    .setAutoCancel(true)
                    .setContentTitle(title)
                    .setContentText(message?.notification?.body)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setColor(Color.parseColor("#9f56f2"))
                    .setContentText(body)
                    .setContentIntent(pi)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)

            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val appName = context.applicationInfo.loadLabel(packageManager).toString()
                val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, appName, NotificationManager.IMPORTANCE_HIGH).apply {
                    setShowBadge(true)
                }
                nm.createNotificationChannel(channel)
            }

            nm.notify(getNotificationID(), builder.build())
        }
    }

    private fun registerPushToken() {
        var token = AppPreferences.getPushToken(this@MyFirebaseMessagingService)
        Logger.i("registerPushToken getPushToken:$token")

        if (!TextUtils.isEmpty(token)) {
            var uuid = Utils.getUniqueID(this@MyFirebaseMessagingService)
            Logger.i("registerPushToken getUniqueID:$uuid")

            var body = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("token", token)
                    .addFormDataPart("deviceId", uuid)
                    .addFormDataPart("deviceType", "android")
                    .addFormDataPart("user", AppPreferences.getUserId(this@MyFirebaseMessagingService)!!)
                    .build()

            EventBus.getDefault().post(NetworkBus(NetworkApi.API85.name, body))
        } else {
            Logger.e("registerPushToken token null error")
        }
    }
}