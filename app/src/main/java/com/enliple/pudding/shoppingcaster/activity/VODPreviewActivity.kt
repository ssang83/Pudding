package com.enliple.pudding.shoppingcaster.activity

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.MediaController
import com.enliple.pudding.R
import com.enliple.pudding.commons.log.Logger
import kotlinx.android.synthetic.main.activity_vod_preview.*

/**
 * Created by Kim Joonsung on 2018-11-07.
 */
class VODPreviewActivity : AppCompatActivity() {

    companion object {
        const val INTENT_KEY_VIDEO_URI = "videoUri"
    }

    private var videoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vod_preview)

        val video = intent.getStringExtra(INTENT_KEY_VIDEO_URI)
        Logger.e("############# vieo : $video")

        videoUri = Uri.parse(video)
        videoViewVOD.setVideoURI(videoUri)
        videoViewVOD.setMediaController(MediaController(this@VODPreviewActivity))
        videoViewVOD.start()
    }
}