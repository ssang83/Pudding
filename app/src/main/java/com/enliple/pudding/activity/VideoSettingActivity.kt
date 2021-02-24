package com.enliple.pudding.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.enliple.pudding.R
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.widget.ShoppingVideoCheckDialog
import kotlinx.android.synthetic.main.activity_video_setting.*

/**
 * Created by Kim Joonsung on 2019-02-21.
 */
class VideoSettingActivity : AppCompatActivity(), ShoppingVideoCheckDialog.ClickListener {
    private var mDataList = arrayOf("저화질(240P)", "일반화질(480P)", "고화질(720P)")

    private var mIsMobile = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_video_setting)

        textMobile.text = AppPreferences.getMobileVideoResolution(this)
        textWiFi.text = AppPreferences.getWiFiVideoResolution(this)

        buttonBack.setOnClickListener {
            finish()
        }

        layoutLTE.setOnClickListener {
            mIsMobile = true
            showDialog()
        }

        layoutWiFi.setOnClickListener {
            mIsMobile = false
            showDialog()
        }
    }

    private fun showDialog() {
        ShoppingVideoCheckDialog(this, ShoppingVideoCheckDialog.MODE_RESOLUTION).apply {
            setListener(this@VideoSettingActivity)
            setData(mDataList.toMutableList(), 0)
            show()
        }
    }

    override fun onSelected(mode: Int, position: Int, data: String) {
        if (mIsMobile) {
            textMobile.text = data
            AppPreferences.setMobileVideoResolution(this, data)
        } else {
            textWiFi.text = data
            AppPreferences.setWiFiVideoResolution(this, data)
        }
    }
}