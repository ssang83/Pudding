package com.enliple.pudding.commons.internal

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.enliple.pudding.commons.log.Logger

/**
 * Created by hkcha on 2018-01-24.
 * App Internal Broadcasting
 */
class AppBroadcastSender {
    companion object {
        /**
         * 실행중인 모든 화면을 정상 종료
         * @param context
         * @param terminateType          AppConstants BROADCAST_EXTRA
         */
        fun broadcastTerminate(context: Context, terminateType: Int, withMainFinishing: Boolean) {
            var sendIntent = Intent(AppConstants.BROADCAST_ACTION_TERMINATE).apply {
                putExtra(AppConstants.BROADCAST_EXTRA_KEY_TERMINATE_TYPE, terminateType)
            }

            if (terminateType != AppConstants.BROADCAST_EXTRA_VALUE_TERMINATE_DIALOG_ONLY) {
                sendIntent.putExtra(AppConstants.BROADCAST_EXTRA_KEY_WITH_MAIN_FINISHING, withMainFinishing)
            }

            Handler(Looper.getMainLooper()).post { androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(context).sendBroadcast(sendIntent) }
        }

        /**
         * 앱내 갱신 Signal 을 전송
         * @param context
         * @param refreshCode       갱신되는 부분의 식별 코드(#Link AppConstants)
         */
        fun notifyRefresh(context: Context, refreshCode: Int) {
            var sendIntent = Intent(AppConstants.BROADCAST_ACTION_REFRESH).apply {
                putExtra(AppConstants.BROADCAST_EXTRA_KEY_REFRESH_TYPE, refreshCode)
            }

            Handler(Looper.getMainLooper()).post { androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(context).sendBroadcast(sendIntent) }
        }

        /**
         * Tab이 변경될 때 해당 position을 전송
         */
        fun notifyTabChange(context: Context, position: Int) {
            var sendIntent = Intent(AppConstants.BROADCAST_ACTION_TAB_CHANGE).apply {
                putExtra(AppConstants.BROADCAST_EXTRA_KEY_TAB_POSITION, position)
            }

            Handler(Looper.getMainLooper()).post { androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(context).sendBroadcast(sendIntent) }
        }

        /**
         * 탭이 변경 될때 player release 시킨다.
         */
        fun broadCastVideoRelease(context: Context, position: Int) {
            var sendIntent = Intent(AppConstants.BROADCAST_ACTION_MAIN_LIST_VIDEO_RELEASE).apply {
                putExtra(AppConstants.BROADCAST_EXTRA_KEY_TAB_POSITION, position)
            }
            Handler(Looper.getMainLooper()).post { androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(context).sendBroadcast(sendIntent) }
        }

        /**
         * MainList Item Position 이 변경되었음
         */
        fun notifyMainListPositionChanged(context: Context, position: Int) {
            var sendIntent = Intent(AppConstants.BROADCAST_ACTION_MAIN_LIST_POSITION_CHANGED).apply {
                putExtra(AppConstants.BROADCAST_EXTRA_KEY_LIST_POSITION, position)
            }

            Handler(Looper.getMainLooper()).post { androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(context).sendBroadcast(sendIntent) }
        }

        /**
         * 메인 리스트 비디오의 현재 재생을 중단
         */
        fun notifyMainListVideoPause(context: Context, position: Int) {
            Logger.e(javaClass.simpleName, "notifyMainListVideoPause : $position")
            var sendIntent = Intent(AppConstants.BROADCAST_ACTION_MAIN_LIST_VIDEO_PAUSE).apply {
                putExtra(AppConstants.BROADCAST_EXTRA_KEY_LIST_POSITION, position)
            }

            Handler(Looper.getMainLooper()).post { androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(context).sendBroadcast(sendIntent) }
        }

        /**
         * 메인 리스트 비디오의 현재 중단된 재생을 재개
         */
        fun notifyMainListVideoResume(context: Context, position: Int) {
            Logger.e(javaClass.simpleName, "notifyMainListVideoResume : $position")
            var sendIntent = Intent(AppConstants.BROADCAST_ACTION_MAIN_LIST_VIDEO_RESUME).apply {
                putExtra(AppConstants.BROADCAST_EXTRA_KEY_LIST_POSITION, position)
            }

            Handler(Looper.getMainLooper()).post { androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(context).sendBroadcast(sendIntent) }
        }
    }
}