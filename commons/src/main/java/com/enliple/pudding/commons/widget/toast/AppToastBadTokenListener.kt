package com.enliple.pudding.commons.widget.toast

import androidx.annotation.NonNull
import android.widget.Toast

/**
 * Created by hkcha on 2018-01-30.
 * Toast 출력간 WindowBadTokenException 이 발생을 확인하는 EventListener
 */
interface AppToastBadTokenListener {
    fun onBadTokenCaught(@NonNull toast: Toast)
}