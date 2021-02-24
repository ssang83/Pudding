package com.enliple.pudding.commons.app

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Process
import androidx.appcompat.app.AppCompatActivity
import com.enliple.pudding.commons.log.Logger

/**
 * Created by hkcha on 2018-01-24.
 * Device 의 Recent App 창에 표시되지 않게 하기 위해 임시적으로 실행되는 Activity
 */
open class RemoveRecentFinishActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "RemoveRecentFinishActivity"

        fun exitApplication(activity: Activity) {
            var intent = Intent(activity, RemoveRecentFinishActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK or
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
            activity.startActivity(intent)
            activity.overridePendingTransition(0, 0)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            finishAndRemoveTask()
            overridePendingTransition(0, 0)
            Logger.d(TAG, "Process Kill Terminate Application...")
            Process.killProcess(Process.myPid())
        } else {
            finish()
            overridePendingTransition(0, 0)
            Logger.d(TAG, "Process Kill Terminate Application...")
            Process.killProcess(Process.myPid())
        }
    }
}