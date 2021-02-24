package com.enliple.pudding.commons.chat

import android.text.TextUtils
import com.enliple.pudding.commons.BuildConfig
import com.enliple.pudding.commons.chat.dao.ChatUser
import com.enliple.pudding.commons.chat.dao.ChatUserSerializer
import com.enliple.pudding.commons.log.Logger
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.json.JSONException
import org.json.JSONObject
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * 채팅 Manager
 */
class ChatManager {
    companion object {
        const val ERROR_CODE_DUPLICATE_NAME = 0
        const val ERROR_CODE_CONNECT_FAIL = 1

        const val NORMAL_CHAT_CMD = "0001"
        const val COOKIE_ETC_CHAT_CMD = "0006"
        const val INQUIRE_CHAT_CMD = "0007"
        const val HEART_CHAT_CMD = "0008"
        const val BROADCAST_INFO_CMD = "0010"
        const val FORCE_EXIT_CMD = "E001"

        const val CHAT_SEND_INTERVAL = 300L

        const val GUBUN_COOKIE = "COOKIE"
        const val GUBUN_BUY = "BUY"
        const val GUBUN_CART = "CART"
        const val GUBUN_SYSTEM = "SYSTEM"
        const val GUBUN_LIKE = "LIKE"

        /**
         * TimeStamp 데이터를 HH:mm:ss 문자열 형식으로 변환
         * @param currentTimeStamp
         * @return
         */
        fun getDate(currentTimeStamp: Long): String {
            var now = Date(currentTimeStamp)
            var format: DateFormat = SimpleDateFormat("HH:mm:ss")
            return format.format(now)
        }
    }

    interface ChatListener {
        fun onChatDisconnected()
        fun onChatConnectError(errorCode: Int)
        fun onRoomConnected(users: List<String>)
        fun onRoomJoinedUser(user: ChatUser, json: String)
        fun onRoomLeavedUser(user: ChatUser, json: String)
        fun onChatFinished()
        fun onChatReceived(chatMsg: Map<String, String>)
        fun onHeartAdded(user:ChatUser)
    }

    private var cmd: String? = null
    private var color: String? = null
    private var from: String? = null
    private var aim: String? = null
    private var flag: Int = -1                               // Init defaults;
    private var content: String? = null
    private var target: String? = null
    private var userIds: MutableList<String> = ArrayList()   // 방내 접속자 아이디 리스트
    private lateinit var mChatUser: ChatUser                 // 현재 사용자 아이디
    private var mListener: ChatListener? = null

    private var mGatewayClient: SocketClient? = null  // gateway 연결을 위한  socket
    private var mClient: SocketClient? = null
    private var isSendCart:Boolean = false              // 유저당 장바구니 메시지 한번만 전송하기 위한 플래그
    private var isSendPurchase:Boolean = false              // 유저당 구매완료 메시지 한번만 전송하기 위한 플래그

    /**
     * 채팅방 정보를 획득하여 접속하기 위한 GateServer 로 접속을 시도
     */
    fun connect(chatUser: ChatUser, ip: String, port: Int) {
        Logger.e("connectServer.. user:${chatUser.nickName}")

        mChatUser = chatUser

        mGatewayClient = when {
            BuildConfig.IS_DEV -> SocketClient("14.0.92.122", 23014) // 채팅방의 위치를 확인하기 위한 Gate Server
            else -> SocketClient(ip, port)
        }

        mGatewayClient?.on("onConnect", onConnect)
        mGatewayClient?.on("onDisconnect", onDisconnect)
        mGatewayClient?.on("onError", onError)
        mGatewayClient?.connect()
    }

    fun disconnect() {
        Logger.e("disconnect")

        mGatewayClient?.disconnect()
        mGatewayClient = null

        mClient?.disconnect()
        mClient = null
    }

    fun setChatListener(listener: ChatListener) {
        mListener = listener
    }

    private val onError = DataListener {
        Logger.e("onError:${it.message}")
    }

    private val onConnect = DataListener {
        Logger.e("onConnect")

        if (mClient == null) {
            Logger.e("Gate Server Established. querying entry...")
            queryEntry()
        } else {
            connectChatServer()
        }
    }

    private val onDisconnect = DataListener {
        if (mGatewayClient != null) {
            Logger.e("onDisconnect: gateway connect")
            mGatewayClient = null
        }

        // 의도치 않게 끊어지는 경우?? 에도 연결을 해준다.
        mClient?.connect()
    }

    private val onChat = DataListener {
        var msg = it.message.getJSONObject("body")
        Logger.d("onChat:$msg")

        try {
            cmd = msg.optString("cmd", NORMAL_CHAT_CMD)
            color = msg.optString("color", "")
            from = msg.getString("from")
            aim = msg.getString("target")
            content = msg.getString("msg")
            flag = 1

            handleReceiveMessage(from, target, cmd!!, color!!, content, aim, getFrom(), flag)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private val onLeave = DataListener {
        var msg = it.message.getJSONObject("body")
        Logger.d("onLeave:$msg")

        try {
            var userStr = msg.optString("user")
            if (userStr != getFrom()) {
                mListener?.onRoomLeavedUser(Gson().fromJson<ChatUser>(userStr, ChatUser::class.java), userStr)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private val onAdd = DataListener {
        var msg = it.message.getJSONObject("body")
        Logger.d("onAdd:$msg")

        try {
            var userStr = msg.optString("user")
            if (userStr != getFrom()) {
                mListener?.onRoomJoinedUser(Gson().fromJson<ChatUser>(userStr, ChatUser::class.java), userStr)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun getFrom(): String = GsonBuilder().registerTypeAdapter(ChatUser::class.java, ChatUserSerializer())
            .excludeFieldsWithoutExposeAnnotation().create().toJson(mChatUser)

    /**
     * 텍스트 채팅 메세지를 전송
     * @param message       전송 텍스트 메세지
     * @param target        수신 대상(Null 이거나 비어있는 경우 방내 접속 인원 모두 전송)
     */
    fun sendTextChat(cmd: String, color: String, message: String, target: String?, nickName:String) {
        if (TextUtils.isEmpty(message)) {
            return
        }

        var from = ""
        if(nickName.isNotEmpty()) {
            JSONObject(getFrom()).apply {
                put("nk", nickName)
            }.let { from = it.toString() }
        }

        try {
            var msg = JSONObject().apply {
                put("cmd", cmd)
                put("color", color)
                put("content", message)
                put("target", if (TextUtils.isEmpty(target)) "*" else target)
                put("rid", mChatUser.no)
                put("from", if(from.isEmpty()) getFrom() else from)
            }

            Logger.e("[CHAT SEND]\n$msg")

            mClient?.send("chat.chatHandler.send", msg, DataCallBack {
                if (it.toString() == "{}") {
                    Logger.v("callback data is null.")
                    return@DataCallBack
                }

                flag = 0
                handleReceiveMessage(from!!, target!!, cmd!!, color!!, content!!, aim!!, from!!, flag)
            })
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun sendCookieSignal(cmd: String, color: String, cnt: Int, sender: String) {
        var message = "COOKIE|^|$sender|^|$cnt"

        try {
            var msg = JSONObject().apply {
                put("cmd", cmd)
                put("color", color)
                put("content", message)
                put("target", if (TextUtils.isEmpty(target)) "*" else target)
                put("rid", mChatUser.no)
                put("from", getFrom())
            }

            Logger.e("[COOKIE SEND]\n$msg")

            mClient?.send("chat.chatHandler.send", msg, DataCallBack {
                if (it.toString() == "{}") {
                    Logger.v("callback data is null.")
                    return@DataCallBack
                }

                flag = 0
                handleReceiveMessage(from!!, target!!, cmd!!, color!!, content!!, aim!!, from!!, flag)
            })
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun sendCartSignal(cmd: String, color: String, productName: String, sender: String) {
        var message = "CART|^|$sender|^|$productName"

        if(!isSendCart) {
            try {
                var msg = JSONObject().apply {
                    put("cmd", cmd)
                    put("color", color)
                    put("content", message)
                    put("target", if (TextUtils.isEmpty(target)) "*" else target)
                    put("rid", mChatUser.no)
                    put("from", getFrom())
                }

                Logger.e("[CART SEND]\n$msg")
                isSendCart = true

                mClient?.send("chat.chatHandler.send", msg, DataCallBack {
                    if (it.toString() == "{}") {
                        Logger.v("callback data is null.")
                        return@DataCallBack
                    }

                    flag = 0
                    handleReceiveMessage(from!!, target!!, cmd!!, color!!, content!!, aim!!, from!!, flag)
                })
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }

    fun sendBuySignal(cmd: String, color: String, productName: String, sender: String) {
        var message = "BUY|^|$sender|^|$productName"

        if(!isSendPurchase) {
            try {
                var msg = JSONObject().apply {
                    put("cmd", cmd)
                    put("color", color)
                    put("content", message)
                    put("target", if (TextUtils.isEmpty(target)) "*" else target)
                    put("rid", mChatUser.no)
                    put("from", getFrom())
                }

                Logger.e("[BUY SEND]\n$msg")
                isSendPurchase = true

                mClient?.send("chat.chatHandler.send", msg, DataCallBack {
                    if (it.toString() == "{}") {
                        Logger.v("callback data is null.")
                        return@DataCallBack
                    }

                    flag = 0
                    handleReceiveMessage(from!!, target!!, cmd!!, color!!, content!!, aim!!, from!!, flag)
                })
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 좋아요 UI Effect 를 채팅 구성원 전체에게 전송
     */
    fun sendHeartSignal() {
        var cmd = HEART_CHAT_CMD

        try {
            var msg = JSONObject().apply {
                put("cmd", cmd)
                put("content", "")
                put("target", "*")
                put("rid", mChatUser.no)
                put("from", getFrom())
            }

            Logger.e("[HEART SEND]\n$msg")

            mClient?.send("chat.chatHandler.send", msg, DataCallBack {
                if (it.toString() == "{}") {
                    Logger.v("callback data is null.")
                    return@DataCallBack
                }

                flag = 0
                handleReceiveMessage(from!!, target!!, cmd, color!!, content!!, aim!!, from!!, flag)
            })
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    /**
     * GateServer 에서 채팅방 접속을 위한 질의를 전송
     */
    private fun queryEntry() {
        var msg = JSONObject().apply {
            put("uid", Gson().toJson(getFrom()))
        }

        mGatewayClient?.send("gate.gateHandler.queryEntry", msg,
                DataCallBack {
                    Logger.e(it.toString())

                    if (it.has("error")) {
                        mListener?.onChatConnectError(ERROR_CODE_DUPLICATE_NAME)
                        return@DataCallBack
                    }

                    enterRoom(it.getString("host"), it.getInt("port"))
                })
    }

    /**
     * Gate Server 로 부터 받은 채팅방 서버 주소로 접속을 시도
     */
    private fun enterRoom(host: String, port: Int) {
        Logger.e("enterRoom: $host:$port")

        // gate server 연결을 끊는다.
        mGatewayClient?.disconnect()

        mClient = SocketClient(host, port)
        mClient?.on("onChat", onChat)
        mClient?.on("onLeave", onLeave)
        mClient?.on("onAdd", onAdd)
        mClient?.on("onDisconnect", onDisconnect)
        mClient?.on("onConnect", onConnect)
        mClient?.on("onError", onError)
    }

    /**
     * GateServer 를 통해 받은 주소 연결 후에 유저 정보 전달
     */
    private fun connectChatServer() {
        val msg = JSONObject()
        try {
            msg.put("username", getFrom())
            msg.put("rid", mChatUser.no)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        mClient?.send("connector.entryHandler.enter", msg, DataCallBack {
            Logger.e(it.toString())

            if (it.has("error")) {
                mListener?.onChatConnectError(ERROR_CODE_CONNECT_FAIL)
                return@DataCallBack
            }

            try {
                var jsonArray = it.getJSONArray("users")
                //userIds.add(0, "*")
                for (i in 1..jsonArray.length()) {
                    userIds.add(jsonArray.getString(i - 1))
                }

                mListener?.onRoomConnected(userIds)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        })
    }

    /**
     * 수신받은 메세지를 핸들링
     */
    private fun handleReceiveMessage(from: String?, target: String?, cmd: String?, color: String, content: String?,
                                     aim: String?, userName: String?, flag: Int) {
        Logger.i("handleReceiveMessage: from :$from, target:$target\n , cmd:$cmd , content:$content" +
                " aim: $aim, userName:$userName, flag:$flag")

        var targetVar = target
        var userNameVar = userName

        if (flag == 1) {
            targetVar = aim
            userNameVar = from
        }

        targetVar = if (target.equals("*", true)) "all" else targetVar
        var result = "$userNameVar say to $targetVar : $content"
        Logger.d("targetVar : $targetVar, result : $result")

        if (cmd == HEART_CHAT_CMD) {
            mListener?.onHeartAdded(Gson().fromJson<ChatUser>(userNameVar, ChatUser::class.java))
        } else {
            mListener?.onChatReceived(HashMap<String, String>().apply {
                if (cmd != null) {
                    put("Cmd", cmd)
                } else {
                    put("Cmd", NORMAL_CHAT_CMD)
                }
                put("Color", color)
                put("ChatContent", result)
                put("DateContent", System.currentTimeMillis().toString())
            })
        }
    }
}