package com.enliple.pudding.shoppingcaster.screen

import android.content.Context
import android.net.Uri
import com.enliple.pudding.commons.log.Logger
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import java.io.File

class VideoManagerExt {

    companion object {
        enum class StreamingType {
            FILE,
            HLS,
            RTMP,
            RTSP,
            MPEG_DASH,
            UNKNOWN                     // Error CASE
        }

        private const val USER_AGENT = "Enliple VCommerce VideoPlayer for Android"

        private const val cacheSize: Long = 100 * 1024 * 1024 // 100M

        private var simpleCache: SimpleCache? = null

        fun getExoPlayer(context: Context): SimpleExoPlayer {
            val rendererFactory = DefaultRenderersFactory(context)
            val bandwidthMeter = DefaultBandwidthMeter()
            val trackSelectionFactory = AdaptiveTrackSelection.Factory(bandwidthMeter)
            val trackSelector = DefaultTrackSelector(trackSelectionFactory)
            val loadControl = DefaultLoadControl()

            var player = ExoPlayerFactory.newSimpleInstance(context, rendererFactory, DefaultTrackSelector(), loadControl)
//            if (videoType == StreamingType.MPEG_DASH) {
//                player = ExoPlayerFactory.newSimpleInstance(context, rendererFactory, trackSelector, loadControl)
//            }

            player?.seekTo(0)
            return player
        }

        fun getMediaSource(context: Context, uri: Uri): MediaSource {
            var cacheDataSourceFactory = getCacheDataSource(context)
            return ExtractorMediaSource.Factory(cacheDataSourceFactory).createMediaSource(uri)
        }

        /**
         * cache data source 반환
         */
        private fun getCacheDataSource(context: Context): DataSource.Factory? {
            if (context == null) {
                Logger.e("context null")
                return null
            }

            if (simpleCache == null) {
                val cacheFolder = File(context.cacheDir, "mediaaa")
                simpleCache = SimpleCache(cacheFolder, LeastRecentlyUsedCacheEvictor(cacheSize))
            }

            //return CacheDataSourceFactory(simpleCache, DefaultHttpDataSourceFactory(USER_AGENT))
            return CacheDataSourceFactory(simpleCache, DefaultDataSourceFactory(context, USER_AGENT))
        }

        /**
         * 요청된 Video Source URL 을 통하여 스트리밍 재생 타입을 반환
         */
        fun getVideoType(location: String): StreamingType {
            if (location.endsWith(".m3u8", true)) {
                return StreamingType.HLS
            } else if (location.startsWith("rtmp://", true)) {
                return StreamingType.RTMP
            } else if (location.startsWith("rtsp://", true)) {
                return StreamingType.RTSP
            } else if (location.endsWith(".mpd", true)
                    || location.endsWith(".mp", true)) {
                return StreamingType.MPEG_DASH
            } else if (location.endsWith(".avi", true)
                    || location.endsWith(".mp4", true)
                    || location.endsWith(".3gp", true)
                    || location.endsWith(".ts", true)
                    || location.endsWith(".m4a", true)
                    || location.endsWith(".mkv", true)
                    || location.endsWith(".webm", true)) {
                return StreamingType.FILE
            } else {
                return StreamingType.UNKNOWN
            }
        }
    }
}