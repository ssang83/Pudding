package com.enliple.pudding.shoppingcaster.stream;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.view.Surface;

import com.enliple.pudding.common.VideoManager;
import com.enliple.pudding.commons.log.Logger;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.LoopingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.video.VideoListener;
import com.ksyun.media.streamer.framework.AudioBufFrame;
import com.ksyun.media.streamer.framework.ImgTexFormat;
import com.ksyun.media.streamer.framework.ImgTexFrame;
import com.ksyun.media.streamer.framework.SrcPin;
import com.ksyun.media.streamer.util.gles.GLRender;
import com.ksyun.media.streamer.util.gles.GlUtil;

/**
 * Media player capture. width ExoPlayer2
 */

public class MediaPlayerCaptureExt implements SurfaceTexture.OnFrameAvailableListener {
    public SrcPin<AudioBufFrame> audioBufSrcPin;
    public SrcPin<ImgTexFrame> imgTexSrcPin;

    private Context mContext;
    private GLRender mGLRender;
    //private KSYMediaPlayer mMediaPlayer;
    private SimpleExoPlayer mMediaPlayer;

    //    private AudioBufFormat mAudioBufFormat;
    private ImgTexFormat mImgTexFormat;
//    private ByteBuffer mAudioOutBuffer;

    private int mTextureId;
    private SurfaceTexture mSurfaceTexture;
    private Surface mSurface;

    private volatile boolean mStopped = true;

    public MediaPlayerCaptureExt(Context context, GLRender glRender) {
        mContext = context;
        mGLRender = glRender;
        audioBufSrcPin = new SrcPin<>();
        imgTexSrcPin = new SrcPin<>();

        mGLRender.addListener(new GLRender.OnReleasedListener() {
            @Override
            public void onReleased() {
                Logger.e("onReleased");

                if (mMediaPlayer != null) {
                    mMediaPlayer.setVideoSurface(null);
                }
                if (mSurfaceTexture != null) {
                    mSurfaceTexture.release();
                    mSurfaceTexture = null;
                }
                if (mSurface != null) {
                    mSurface.release();
                    mSurface = null;
                }
            }
        });

        mGLRender.addListener(new GLRender.OnReadyListener() {
            @Override
            public void onReady() {
                Logger.e("onReady");

                mTextureId = GlUtil.createOESTextureObject();
                if (mSurfaceTexture != null) {
                    mSurfaceTexture.release();
                }
                if (mSurface != null) {
                    mSurface.release();
                }
                mSurfaceTexture = new SurfaceTexture(mTextureId);
                mSurfaceTexture.setOnFrameAvailableListener(MediaPlayerCaptureExt.this);
                mSurface = new Surface(mSurfaceTexture);
            }
        });
    }

    public ExoPlayer getMediaPlayer() {
        if (mMediaPlayer == null) {
            DefaultTrackSelector mTrackSelector = new DefaultTrackSelector(new AdaptiveTrackSelection.Factory());
            mMediaPlayer = VideoManager.Companion.getExoPlayer(mContext, mTrackSelector);
        }
        return mMediaPlayer;
    }

    public void pause() {
        if (mMediaPlayer != null) {
            mMediaPlayer.setPlayWhenReady(false);
            mMediaPlayer.getPlaybackState();
        }
    }

    public void resume() {
        if (mMediaPlayer != null) {
            mMediaPlayer.setPlayWhenReady(true);
            mMediaPlayer.getPlaybackState();
        }
    }

    public void start(String url) {
        Logger.e("start:" + url);

        getMediaPlayer();

        MediaSource mediaSource = VideoManager.Companion.getMediaSource(mContext, url);
        mediaSource = new LoopingMediaSource(mediaSource); // 반복 재생

        mMediaPlayer.prepare(mediaSource);
        mMediaPlayer.addVideoListener(new VideoListener() {
            @Override
            public void onVideoSizeChanged(int w, int h, int degrees, float ratio) {
                Logger.d("onVideoSizeChanged: " + w + "x" + h);

                if (mSurfaceTexture != null) {
                    mSurfaceTexture.setDefaultBufferSize(w, h);
                    mSurfaceTexture.setOnFrameAvailableListener(MediaPlayerCaptureExt.this);
                }

                mImgTexFormat = new ImgTexFormat(ImgTexFormat.COLOR_EXTERNAL_OES, w, h);
                imgTexSrcPin.onFormatChanged(mImgTexFormat);

                if (w < h) { // 세로 영상
                    mMediaPlayer.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
                } else {
                    mMediaPlayer.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT);
                }
            }

            @Override
            public void onRenderedFirstFrame() {
                Logger.d("onRenderedFirstFrame");

                updateFrame();
            }
        });

        mMediaPlayer.addListener(new Player.EventListener() {
            @Override
            public void onPlayerError(ExoPlaybackException error) {
                Logger.e("onPlayerError:" + error);
            }

            @Override
            public void onLoadingChanged(boolean isLoading) {
            }
        });

        if (mSurface != null) {
            mMediaPlayer.setVideoSurface(mSurface);
        }

        mMediaPlayer.setPlayWhenReady(true);
        mStopped = false;
    }

    public void stop() {
        if (mMediaPlayer != null) {
            mStopped = true;
            if (mSurface != null) {
                mMediaPlayer.setVideoSurface(null);
            }
            if (mSurfaceTexture != null) {
                mSurfaceTexture.setOnFrameAvailableListener(null);
            }
            mMediaPlayer.stop();
            mGLRender.queueEvent(new Runnable() {
                @Override
                public void run() {
                    imgTexSrcPin.onFrameAvailable(new ImgTexFrame(mImgTexFormat, ImgTexFrame.NO_TEXTURE, null, 0));
                }
            });
        }
    }

    public void mute(boolean isMute) {
        if (isMute) {
            mMediaPlayer.setVolume(0f);
        } else {
            mMediaPlayer.setVolume(1.0f);
        }
    }

    public void release() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        updateFrame();
    }

    private void updateFrame() {
        final long pts = System.nanoTime() / 1000 / 1000;
        mGLRender.queueEvent(new Runnable() {
            @Override
            public void run() {
                mSurfaceTexture.updateTexImage();
                if (mStopped) {
                    return;
                }
                float[] texMatrix = new float[16];
                mSurfaceTexture.getTransformMatrix(texMatrix);
                ImgTexFrame frame = new ImgTexFrame(mImgTexFormat, mTextureId, texMatrix, pts);
                try {
                    imgTexSrcPin.onFrameAvailable(frame);
                } catch (Exception e) {
                    Logger.e("Draw player frame failed, ignore");
                }
            }
        });
    }
}