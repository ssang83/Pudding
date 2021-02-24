package com.enliple.pudding.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import com.enliple.pudding.AbsBaseActivity
import com.enliple.pudding.BuildConfig
import com.enliple.pudding.R
import com.enliple.pudding.commons.app.NetworkStatusUtils
import com.enliple.pudding.commons.app.VersionCompare
import kotlinx.android.synthetic.main.activity_version_info.*


/**
 * My-설정-버전정보 Activity
 * @author hkcha
 * @since 2018.09.05
 */
class VersionInfoActivity : AbsBaseActivity() {
    companion object {
        const val INTENT_EXTRA_KEY_LATEST_VERSION = "latestVersion"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_version_info)

        var currentVersion = BuildConfig.VERSION_NAME
        var latestVersion = intent.getStringExtra(INTENT_EXTRA_KEY_LATEST_VERSION)


        buttonCurrentVersion.text = String.format(getString(R.string.msg_settings_current_version_format), currentVersion)
        when (VersionCompare.compareVersions(currentVersion, latestVersion)) {
            1 -> {
                buttonLatestVersion.text = String.format(getString(R.string.msg_settings_latest_version_format), latestVersion)
                buttonLatestVersion.isEnabled = true
            }

            0, -1 -> {
                buttonLatestVersion.text = getString(R.string.msg_settings_currently_latest_version)
                buttonLatestVersion.isEnabled = false
            }

            else -> buttonLatestVersion.visibility = View.GONE
        }

        buttonBack.setOnClickListener { onBackPressed() }
        buttonLatestVersion.setOnClickListener {
            // GooglePlay Store 이동
            try {
                startActivity(Intent(Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=${BuildConfig.APPLICATION_ID}")))
            } catch (e: android.content.ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}")))
            }
        }
    }

    override fun onNetworkStatusChanged(status: NetworkStatusUtils.NetworkStatus) {}

    override fun onDozeModeStateChanged(dozeEnable: Boolean) {}

    override fun onBackPressed() {
        finish()
    }
}