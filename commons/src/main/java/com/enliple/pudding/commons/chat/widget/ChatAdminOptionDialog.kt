package com.enliple.pudding.commons.chat.widget

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatDialog
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import com.enliple.pudding.commons.R
import kotlinx.android.synthetic.main.dialog_chat_admin_option.*

/**
 * 채팅창에서 해당 사용자를 누르면 표시되는 팝업
 * (방장의 옵션 권한으로 Streamer 에서만 사용하고 있음)
 */
class ChatAdminOptionDialog : AppCompatDialog {

    private var targetUser:String
    private var callback: OptionCallback? = null

    constructor(context: Context, targetUser:String, callback: OptionCallback?) : super(context, R.style.ChatAdminOptionDialog) {
        this.targetUser = targetUser
        this.callback = callback

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        var windowParams = window.attributes
        windowParams.gravity = Gravity.BOTTOM
        windowParams.width = WindowManager.LayoutParams.MATCH_PARENT
        windowParams.height = context.resources.getDimensionPixelSize(R.dimen.chat_option_dialog_height)
        window.attributes = windowParams

        setContentView(R.layout.dialog_chat_admin_option)

        setCancelable(true)
        setCanceledOnTouchOutside(true)

        buttonBan.setOnClickListener { callback?.onKickUser(targetUser) }
        buttonChatDenied.setOnClickListener { callback?.onDeniedChat(targetUser) }
        buttonDeniedAll.setOnClickListener { callback?.onDeniedAll(targetUser) }
    }

    interface OptionCallback {
        fun onDeniedChat(user:String)
        fun onKickUser(user:String)
        fun onDeniedAll(user:String)
    }
}