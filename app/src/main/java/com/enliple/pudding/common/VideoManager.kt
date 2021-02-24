package com.enliple.pudding.common

import android.content.Context
import android.net.Uri
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.shoppingplayer.StreamingType
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.rtmp.RtmpDataSourceFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import java.io.File

class VideoManager {
    companion object {
        private const val USER_AGENT = "Pudding VideoPlayer for Android"

        private const val CACHE_SIZE: Long = 200 * 1024 * 1024 // 200M

        private var mSimpleCache: SimpleCache? = null

        fun getExoPlayer(context: Context, track: DefaultTrackSelector): SimpleExoPlayer {
            var player = ExoPlayerFactory.newSimpleInstance(context, DefaultRenderersFactory(context), track, DefaultLoadControl())
            player?.seekTo(0)
            return player
        }

        fun getMediaSource(context: Context, url: String): MediaSource {
            //var dataSourceFactory = getCacheDataSource(context)
            var dataSourceFactory = DefaultDataSourceFactory(context, USER_AGENT, DefaultBandwidthMeter())

            val uri = Uri.parse(url)
            val videoType = getVideoType(url)
            Logger.d("getMediaSource: $videoType")
            when (videoType) {
                StreamingType.FILE -> {
                    return ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
                }

                StreamingType.RTMP,
                StreamingType.RTSP -> {
                    var videoUrl = uri.toString()
                    var playPath = videoUrl!!.split("/")
                    // ExoPlayer RTMP Extension 내 탑재된 Native Library 인 LibRTMP 관련 Parameter 설정 (재생버퍼 및 연결 타임아웃)
                    videoUrl += if ((videoUrl!!.indexOf(":v") != -1) || (videoUrl!!.indexOf(":h") != -1)) {
                        " app=${playPath[playPath.size - 3]} playpath=/${playPath[playPath.size - 2]}/${playPath[playPath.size - 1]} buffer=3000 timeout=10 live=1"
                    } else {
                        // 일반적인 주소형태로 들어온 Case
                        "buffer=10000 timeout=10"
                    }
                    return ExtractorMediaSource.Factory(RtmpDataSourceFactory()).createMediaSource(Uri.parse(videoUrl))
                }

                StreamingType.MPEG_DASH -> return DashMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
                else -> return HlsMediaSource.Factory(dataSourceFactory)
                        .setAllowChunklessPreparation(true) // prepare 속도가 올라간다?
                        .createMediaSource(uri)
            }
        }

        /**
         * cache data source 반환
         */
        private fun getCacheDataSource(context: Context): DataSource.Factory? {
            if (context == null) {
                Logger.e("context null")
                return null
            }

            if (mSimpleCache == null) {
                val cacheFolder = File(context.cacheDir, "media")
                mSimpleCache = SimpleCache(cacheFolder, LeastRecentlyUsedCacheEvictor(CACHE_SIZE))
            }

            //return CacheDataSourceFactory(mSimpleCache, DefaultHttpDataSourceFactory(USER_AGENT))
            return CacheDataSourceFactory(mSimpleCache, DefaultDataSourceFactory(context, USER_AGENT))
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