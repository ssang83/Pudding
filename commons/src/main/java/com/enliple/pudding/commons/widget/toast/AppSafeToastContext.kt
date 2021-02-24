package com.enliple.pudding.commons.widget.toast

import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import android.view.Display
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.annotation.Nullable

/**
 * Created by hkcha on 2018-01-30.
 * WindowBadTokenException Crash 대응을 위한 ToastContext by kotlin
 * (https://github.com/drakeet/ToastCompat/blob/master/library/src/main/java/me/drakeet/support/toast/AppSafeToastContext.java)
 */
class AppSafeToastContext(@NonNull base: Context, @NonNull toast: Toast, @Nullable tokenListener: AppToastBadTokenListener?) : ContextWrapper(base) {

    companion object {
        const val TAG = "AppSafeToastContext"
    }

    private var toast = toast
    private var badTokenListener = tokenListener

    override fun getApplicationContext(): Context {
        return ApplicationContextWrapper(baseContext, toast, badTokenListener)
    }

    class ApplicationContextWrapper(base: Context, toast: Toast, badTokenListener: AppToastBadTokenListener?) : ContextWrapper(base) {
        private var toast = toast
        private var badTokenListener = badTokenListener

        override fun getSystemService(name: String?): Any {
            if (Context.WINDOW_SERVICE == name) {
                return WindowManagerWrapper(baseContext.getSystemService(name) as WindowManager, toast, badTokenListener)
            }
            return super.getSystemService(name)
        }
    }

    class WindowManagerWrapper(base: WindowManager, toast: Toast, badTokenListener: AppToastBadTokenListener?) : WindowManager {

        private var base: WindowManager = base
        private var badTokenListener: AppToastBadTokenListener? = badTokenListener
        private var toast: Toast = toast

        override fun getDefaultDisplay(): Display {
            return base.defaultDisplay
        }

        override fun addView(view: View?, params: ViewGroup.LayoutParams?) {
            try {
                base.addView(view, params)
            } catch (e: WindowManager.BadTokenException) {
                if (badTokenListener != null) badTokenListener?.onBadTokenCaught(toast)
            } catch (t: Throwable) {
                Log.e(TAG, "[addView]", t)
            }
        }

        override fun updateViewLayout(view: View?, params: ViewGroup.LayoutParams?) {
            base.updateViewLayout(view, params)
        }

        override fun removeView(view: View?) {
            base.removeView(view)
        }

        override fun removeViewImmediate(view: View?) {
            base.removeViewImmediate(view)
        }
    }
}