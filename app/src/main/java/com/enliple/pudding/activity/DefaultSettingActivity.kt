package com.enliple.pudding.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.enliple.pudding.R
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.network.NetworkHandler
import kotlinx.android.synthetic.main.activity_app_setting.*

/**
 * Created by Kim Joonsung on 2019-02-21.
 */
class DefaultSettingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_app_setting)

        buttonActive.isSelected = AppPreferences.getUseMobileAlert(this)

        buttonBack.setOnClickListener {
            finish()
        }

        buttonActive.setOnClickListener {
            it.isSelected = !it.isSelected

            AppPreferences.setUseMobileAlert(this, it.isSelected)
        }
    }
}