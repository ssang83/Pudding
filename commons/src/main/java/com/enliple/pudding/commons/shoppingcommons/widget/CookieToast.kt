package com.enliple.pudding.commons.shoppingcommons.widget

import android.content.Context
import android.os.CountDownTimer
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.enliple.pudding.commons.R
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.widget.toast.AppSafeToastContext
import com.enliple.pudding.commons.widget.toast.AppToastBadTokenListener
import kotlinx.android.synthetic.main.toast_cookie_transfer.view.*
import java.lang.reflect.Field

/**
 * 방송간 푸딩가 전송되었을때 사용자들에게 표시되는 Toast Popup
 * @author hkcha
 * @since 2018.08.28
 */
class CookieToast(context: Context, nickName: String, account: String, cookieCount: Int) : Toast(context), AppToastBadTokenListener {
    companion object {
        private const val TAG = "CookieToast"

        private const val COUNT_DOWN_DEFAULT_TICK = 1000L
        const val DURATION_MILLISECONDS_DEFAULT = 3000L

        private var timer: CountDownTimer? = null
        private var lastToast: Toast? = null

        /**
         * Toast 활동을 모두 취소
         */
        fun cancelAllToast() {
            timer?.cancel()
            timer = null

            var context: Context? = lastToast?.view?.context
            if (context != null) {
                var manager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                try {
                    if (lastToast?.view != null && lastToast?.view?.isAttachedToWindow!!) {
                        manager.removeView(lastToast?.view)
                    }
                } catch (e: IllegalArgumentException) { // ignore : not attached.. }
                }
                lastToast?.cancel()
                lastToast = null
            }
        }

        private fun setContext(view: View, context: Context) {
            try {
                var field: Field = View::class.java.getDeclaredField("mContext")
                field.isAccessible = true
                field.set(view, context)
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }
    }

    private val context = context
    private val nickName: String = nickName
    private val account: String = account
    private val cookieCount: Int = cookieCount

    override fun setView(view: View?) {
        super.setView(view)
        if (view != null) {
            setContext(view, AppSafeToastContext(view.context, this, this))
        }
    }

    override fun onBadTokenCaught(toast: Toast) {
        Logger.e(TAG, "BadTokenCaught.. skip window draw")
    }

    fun show(toast: Toast) {
        cancelAllToast()

        var yOffset = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                40f, context.resources?.displayMetrics).toInt()

        toast.setGravity(Gravity.CENTER, 0, yOffset)

        var cookieToastView = LayoutInflater.from(context).inflate(R.layout.toast_cookie_transfer, null, false)

        cookieToastView.textViewSender.text =
                String.format(context.getString(R.string.msg_cookie_transfer_toast_sender_format), nickName, account)

        cookieToastView.cookieCount.text =
                String.format(context.getString(R.string.msg_cookie_transfer_toast_count_format), cookieCount)


        toast.duration = Toast.LENGTH_SHORT
        toast.view = cookieToastView
        lastToast = toast

        setContext(toast.view, AppSafeToastContext(context, toast, this))

        timer = object : CountDownTimer(DURATION_MILLISECONDS_DEFAULT, COUNT_DOWN_DEFAULT_TICK) {
            override fun onFinish() {
                cancelAllToast()
                toast.show()
                lastToast = null
            }

            override fun onTick(millisUntilFinished: Long) {
                toast.show()
            }
        }.start()
    }
}