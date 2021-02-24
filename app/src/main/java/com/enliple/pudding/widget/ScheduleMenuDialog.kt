package com.enliple.pudding.widget

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.enliple.pudding.PuddingApplication
import com.enliple.pudding.R
import com.enliple.pudding.activity.*
import com.enliple.pudding.commons.internal.AppPreferences
import kotlinx.android.synthetic.main.dialog_schedule_menu.*

/**
 */
open class ScheduleMenuDialog : androidx.fragment.app.DialogFragment(), View.OnClickListener {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_schedule_menu, container)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setStyle(androidx.fragment.app.DialogFragment.STYLE_NO_FRAME, R.style.AppTheme)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        fab1.setOnClickListener(this)
        fab2.setOnClickListener(this)
        fab3.setOnClickListener(this)
        close.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.fab1 -> {
                if (AppPreferences.getLoginStatus(v.context)) {
                    context?.startActivity(Intent(context, ScheduleAlarmListActivity::class.java))
                } else {
                    context?.startActivity(Intent(context, LoginActivity::class.java))
                }

                dismiss()
            }

            R.id.fab2 -> {
                if (AppPreferences.getLoginStatus(v.context)) {
                    context?.startActivity(Intent(context, ScheduleListActivity::class.java))
                } else {
                    context?.startActivity(Intent(context, LoginActivity::class.java))
                }

                dismiss()
            }

            R.id.fab3 -> {
                if (AppPreferences.getLoginStatus(v.context)) {
                    context?.startActivity(Intent(context, BroadcastSettingActivity::class.java).apply {
                        putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_TAB, BroadcastSettingActivity.TAB_RESERVE_BROADCAST)
                    })
                } else {
                    context?.startActivity(Intent(context, LoginActivity::class.java))
                }

                dismiss()
            }

            R.id.close -> dismiss()
        }
    }
}