package com.enliple.pudding.commons.widget.toast

import android.content.Context
import android.os.Build
import android.os.CountDownTimer
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.enliple.pudding.commons.R
import com.enliple.pudding.commons.log.Logger
import kotlinx.android.synthetic.main.custom_toast_layout.view.*
import java.lang.reflect.Field

/**
 * Created by hkcha on 2018-01-25.
 * Application 전용 Custom Toast
 */
class AppToast(context: Context) : Toast(context), AppToastBadTokenListener {
    companion object {
        private const val TAG = "AppToast"

        private const val COUNT_DOWN_DEFAULT_TICK = 1000L
        const val DURATION_MILLISECONDS_FAST = 2000L
        const val DURATION_MILLISECONDS_DEFAULT = 3000L
        const val DURATION_MILLISECONDS_LONG = 5000L

        const val GRAVITY_TOP = 0
        const val GRAVITY_BOTTOM = 1
        const val GRAVITY_MIDDLE = 2

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
                } catch (e: Exception) { // ignore : not attached.. }
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
            } catch (e: java.lang.Exception) {
                Logger.p(e)
            }
        }
    }

    private val context = context

    /**
     * 메세지 표시형태의 Toast Message 를 출력
     * @param msgResId
     * @param durationMilliseconds
     * @param gravity
     */
    fun showToastMessage(msgResId: Int, durationMilliseconds: Long, gravity: Int) {
        showToastMessage(context.getString(msgResId), durationMilliseconds, gravity)
    }

    /**
     * 메세지 표시형태의 Toast Message 를 출력
     * @param body
     * @param durationMilliseconds
     * @param gravity
     */
    fun showToastMessage(body: String, durationMilliseconds: Long, gravity: Int) {
        var view = LayoutInflater.from(context).inflate(R.layout.custom_toast_layout, null)
        view.textViewMessage.text = body
        showToastAtView(view, durationMilliseconds, gravity)
    }

    /**
     * 사용자 지정 View 를 Toast 로 출력
     * @param view
     * @param durationMilliseconds
     * @param gravity
     */
    fun showToastAtView(view: View, durationMilliseconds: Long, gravity: Int) {
        show(this, view, durationMilliseconds, gravity)
    }

    override fun setView(view: View?) {
        super.setView(view)
        if (view != null) {
            setContext(view, AppSafeToastContext(view.context, this, this))
        }
    }

    override fun onBadTokenCaught(toast: Toast) {
        Logger.e(TAG, "BadTokenCaught.. skip window draw")
    }

    /**
     * 생성된 Custom View 를 Toast 로 출력
     * @param toast
     * @param view
     * @param durationMilliseconds
     * @param gravity
     */
    private fun show(toast: Toast, view: View, durationMilliseconds: Long, gravity: Int) {
        cancelAllToast()

        var yOffset = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40f, context.resources?.displayMetrics).toInt()

        when (gravity) {
            GRAVITY_TOP -> toast.setGravity(Gravity.TOP or Gravity.FILL_HORIZONTAL, 0, yOffset)
            GRAVITY_MIDDLE -> toast.setGravity(Gravity.CENTER or Gravity.FILL_HORIZONTAL, 0, 0)
            else -> toast.setGravity(Gravity.BOTTOM or Gravity.FILL_HORIZONTAL, 0, yOffset)
        }

        toast.duration = LENGTH_SHORT
        toast.view = view
        lastToast = toast

        // 9 버전에서 토스트가 죽는다.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            setContext(toast.view, context)
        } else {
            setContext(toast.view, AppSafeToastContext(context, toast, this))
        }

        timer = object : CountDownTimer(durationMilliseconds, COUNT_DOWN_DEFAULT_TICK) {
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