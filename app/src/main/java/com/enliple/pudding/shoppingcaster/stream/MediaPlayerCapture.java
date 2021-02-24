package com.enliple.pudding.shoppingcaster.stream;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.view.Surface;

import com.enliple.pudding.commons.log.Logger;
import com.ksyun.media.player.IMediaPlayer;
import com.ksyun.media.player.KSYMediaPlayer;
import com.ksyun.media.streamer.framework.AudioBufFormat;
import com.ksyun.media.streamer.framework.AudioBufFrame;
import com.ksyun.media.streamer.framework.ImgTexFormat;
import com.ksyun.media.streamer.framework.ImgTexFrame;
import com.ksyun.media.streamer.framework.SrcPin;
import com.ksyun.media.streamer.util.gles.GLRender;
import com.ksyun.media.streamer.util.gles.GlUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Media player capture.
 */

public class MediaPlayerCapture implements SurfaceTexture.OnFrameAvailableListener {
    public SrcPin<AudioBufFrame> audioBufSrcPin;
    public SrcPin<ImgTexFrame> imgTexSrcPin;

    private Context mContext;
    private GLRender mGLRender;
    private KSYMediaPlayer mMediaPlayer;

    private AudioBufFormat mAudioBufFormat;
    private ImgTexFormat mImgTexFormat;
    private ByteBuffer mAudioOutBuffer;

    private int mTextureId;
    private SurfaceTexture mSurfaceTexture;
    private Surface mSurface;

    private volatile boolean mStopped = true;

    public MediaPlayerCapture(Context context, GLRender glRender) {
        mContext = context;
        mGLRender = glRender;
        audioBufSrcPin = new SrcPin<>();
        imgTexSrcPin = new SrcPin<>();

        mGLRender.addListener(mGLRenderListener);
        mGLRender.addListener(mGLReleaseListener);
    }

    public KSYMediaPlayer getMediaPlayer() {
        if (mMediaPlayer == null) {
            mMediaPlayer = new KSYMediaPlayer.Builder(mContext).build();
        }
        return mMediaPlayer;
    }

    public void start(String url) {
        mAudioBufFormat = null;

        getMediaPlayer();

        mMediaPlayer.reset();
        mMediaPlayer.shouldAutoPlay(false);
        mMediaPlayer.setOnAudioPCMAvailableListener(mOnAudioPCMListener);
        mMediaPlayer.setOnPreparedListener(mOnPreparedListener);
        try {
            mMediaPlayer.setDataSource(url);
            mMediaPlayer.prepareAsync();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mStopped = false;
    }

    public void stop() {
        if (mMediaPlayer != null) {
            mStopped = true;
            if (mSurface != null) {
                mMediaPlayer.setSurface(null);
            }
            if (mSurfaceTexture != null) {
                mSurfaceTexture.setOnFrameAvailableListener(null);
            }
            mMediaPlayer.setOnAudioPCMAvailableListener(null);
            mMediaPlayer.stop();
            mGLRender.queueEvent(new Runnable() {
                @Override
                public void run() {
                    imgTexSrcPin.onFrameAvailable(new ImgTexFrame(mImgTexFormat, ImgTexFrame.NO_TEXTURE, null, 0));
                }
            });
        }
    }

    public void release() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        mGLRender.removeListener(mGLRenderListener);
    }

    private IMediaPlayer.OnPreparedListener mOnPreparedListener = new IMediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(IMediaPlayer iMediaPlayer) {
            if (mSurface != null) {
                mMediaPlayer.setSurface(mSurface);
            }

            // trig onFormatChanged event
            int w = mMediaPlayer.getVideoWidth();
            int h = mMediaPlayer.getVideoHeight();
            Logger.e("video prepared, " + w + "x" + h);
            if (mSurfaceTexture != null) {
                mSurfaceTexture.setDefaultBufferSize(w, h);
                mSurfaceTexture.setOnFrameAvailableListener(MediaPlayerCapture.this);
            }
            mImgTexFormat = new ImgTexFormat(ImgTexFormat.COLOR_EXTERNAL_OES, w, h);
            imgTexSrcPin.onFormatChanged(mImgTexFormat);
            mAudioBufFormat = null;
            mMediaPlayer.start();

            if (w < h) { // 세로 영상
                mMediaPlayer.setVideoScalingMode(KSYMediaPlayer.VIDEO_SCALING_MODE_NOSCALE_TO_FIT);
            } else {
                mMediaPlayer.setVideoScalingMode(KSYMediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
            }
//            public static final int VIDEO_SCALING_MODE_NOSCALE_TO_FIT = 0;
//            public static final int VIDEO_SCALING_MODE_SCALE_TO_FIT = 1;
//            public static final int VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING = 2;
        }
    };

    private KSYMediaPlayer.OnAudioPCMListener mOnAudioPCMListener = new KSYMediaPlayer.OnAudioPCMListener() {
        @Override
        public void onAudioPCMAvailable(IMediaPlayer iMediaPlayer, ByteBuffer byteBuffer, long timestamp, int channels, int samplerate, int samplefmt) {
            Logger.i("onAudioPCMAvailable");

            if (byteBuffer == null) {
                Logger.e("onAudioPCMAvailable buffer null !!");
                return;
            }

            if (mAudioBufFormat == null) {
                mAudioBufFormat = new AudioBufFormat(samplefmt, samplerate, channels);
                audioBufSrcPin.onFormatChanged(mAudioBufFormat);
            }

            ByteBuffer pcmBuffer = byteBuffer;
            if (!byteBuffer.isDirect()) {
                int len = byteBuffer.limit();
                if (mAudioOutBuffer == null || mAudioOutBuffer.capacity() < len) {
                    mAudioOutBuffer = ByteBuffer.allocateDirect(len);
                    mAudioOutBuffer.order(ByteOrder.nativeOrder());
                }
                mAudioOutBuffer.clear();
                mAudioOutBuffer.put(byteBuffer);
                mAudioOutBuffer.flip();
                pcmBuffer = mAudioOutBuffer;
            }
            audioBufSrcPin.onFrameAvailable(new AudioBufFrame(mAudioBufFormat, pcmBuffer, timestamp));
        }
    };

    private GLRender.OnReadyListener mGLRenderListener = new GLRender.OnReadyListener() {
        @Override
        public void onReady() {
            mTextureId = GlUtil.createOESTextureObject();
            if (mSurfaceTexture != null) {
                mSurfaceTexture.release();
            }
            if (mSurface != null) {
                mSurface.release();
            }
            mSurfaceTexture = new SurfaceTexture(mTextureId);
            mSurfaceTexture.setOnFrameAvailableListener(MediaPlayerCapture.this);
            mSurface = new Surface(mSurfaceTexture);
            if (mMediaPlayer != null) {
                mMediaPlayer.setSurface(mSurface);
                if (mMediaPlayer.isPlaying()) {
                    int w = mMediaPlayer.getVideoWidth();
                    int h = mMediaPlayer.getVideoHeight();
                    Logger.e("onReady " + w + "x" + h);
                    mSurfaceTexture.setDefaultBufferSize(w, h);
                }
            }
        }
    };

    private GLRender.OnReleasedListener mGLReleaseListener = new GLRender.OnReleasedListener() {
        @Override
        public void onReleased() {
            if (mMediaPlayer != null) {
                mMediaPlayer.setSurface(null);
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
    };

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
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
                    e.printStackTrace();
                    Logger.e("Draw player frame failed, ignore");
                }
            }
        });
    }
}