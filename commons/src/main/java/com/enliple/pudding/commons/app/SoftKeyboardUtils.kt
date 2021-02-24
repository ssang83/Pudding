package com.enliple.pudding.commons.app


import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.ResultReceiver
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.enliple.pudding.commons.log.Logger

/**
 * Created by hkcha on 2018-01-08.
 */
class SoftKeyboardUtils {
    companion object {
        private const val TAG = "SoftKeyboardUtils"

        /**
         * Activity Level 에서 강제로 키보드를 닫을때 사용
         * (일부 Nexus 기종의 Side Effect 에 대한 대응)
         * @param activity
         */
        fun hideKeyboard(activity: Activity?) {
            if (activity == null) return

            var imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            var view = activity.currentFocus ?: View(activity)
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

        /**
         * 특정 View 의 widowToken 을 사용하여 SoftKeyboard 를 숨김
         */
        fun hideKeyboard(view: View?) {
            try {
                var imm: InputMethodManager? = view?.context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm?.hideSoftInputFromWindow(view.windowToken, 0)
            } catch (e: Exception) {
            }
        }

        /**
         * SoftKeyboard 를 사용하려는 View 의 WindowToken을 사용하여 표시
         * @param view
         */
        fun showKeyboard(view: View?) {
            try {
                var imm: InputMethodManager? = view?.context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm?.showSoftInput(view, InputMethodManager.SHOW_FORCED)
            } catch (e: Exception) {
            }
        }
    }

    /**
     *
     */
    open class IMMResult : ResultReceiver(null) {
        private var result = -1

        override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
            result = resultCode
        }

        fun getResult(): Int {
            try {
                var sleep = 0L
                while (result == -1 && sleep < 500) {
                    Thread.sleep(100)
                    sleep += 100
                }
            } catch (e: InterruptedException) {
                Logger.p(e)
            }

            return result
        }
    }
}