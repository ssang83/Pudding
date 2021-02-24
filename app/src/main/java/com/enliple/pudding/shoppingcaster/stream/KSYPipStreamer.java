package com.enliple.pudding.shoppingcaster.stream;

import android.content.Context;
import android.graphics.RectF;

import com.enliple.pudding.commons.log.Logger;
import com.ksyun.media.streamer.filter.imgtex.ImgTexMixer;
import com.ksyun.media.streamer.kit.KSYStreamer;

/**
 * All in one streamer class.
 */
public class KSYPipStreamer extends KSYStreamer {
    //private static final int IDX_BG_VIDEO = 1;
    private static final int IDX_AUDIO_PIP = 2;

    private MediaPlayerCaptureExt mMediaPlayerCapture;
    private PictureCapture mPictureCapture;

    private static final int MAIN_SCREEN_CAMERA = 1;
    private static final int MAIN_SCREEN_VIDEO = 2;

    private int mMainScreen = MAIN_SCREEN_CAMERA;

    private float mSubViewWidth = .0f;
    private float mSubViewHeight = .0f;
    private float mSubViewX = .0f;
    private float mSubViewY = .0f;

    private int mIdxVideo;
    private int mIdxBgImage;

    public KSYPipStreamer(Context context) {
        super(context);
    }

    @Override
    protected void initModules() {
        // override mixer idx
        mIdxVideo = 1;
        mIdxBgImage = 2;
        mIdxCamera = 3;

        mIdxWmLogo = 6;
        mIdxWmTime = 7;

        // super init
        super.initModules();

        // create pip modules
        mPictureCapture = new PictureCapture(getGLRender());
        mMediaPlayerCapture = new MediaPlayerCaptureExt(mContext, getGLRender());

        // pip connection
        mPictureCapture.imgTexSrcPin.connect(mImgTexPreviewMixer.getSinkPin(mIdxBgImage));
        mMediaPlayerCapture.imgTexSrcPin.connect(mImgTexPreviewMixer.getSinkPin(mIdxVideo));
        mImgTexPreviewMixer.setScalingMode(mIdxBgImage, ImgTexMixer.SCALING_MODE_FULL_FILL);
        mImgTexPreviewMixer.setScalingMode(mIdxVideo, ImgTexMixer.SCALING_MODE_BEST_FIT);
        mImgTexPreviewMixer.setScalingMode(mIdxCamera, ImgTexMixer.SCALING_MODE_CENTER_CROP);
        mImgTexPreviewMixer.setMainSinkPinIndex(mIdxCamera);

        mPictureCapture.imgTexSrcPin.connect(mImgTexMixer.getSinkPin(mIdxBgImage));
        mMediaPlayerCapture.imgTexSrcPin.connect(mImgTexMixer.getSinkPin(mIdxVideo));
        mMediaPlayerCapture.audioBufSrcPin.connect(getAudioMixer().getSinkPin(IDX_AUDIO_PIP));
        mImgTexMixer.setScalingMode(mIdxBgImage, ImgTexMixer.SCALING_MODE_FULL_FILL);
        mImgTexMixer.setScalingMode(mIdxVideo, ImgTexMixer.SCALING_MODE_BEST_FIT);
        mImgTexMixer.setScalingMode(mIdxCamera, ImgTexMixer.SCALING_MODE_CENTER_CROP);
        mImgTexMixer.setMainSinkPinIndex(mIdxCamera);
    }

    @Override
    public void setMuteAudio(boolean b) {
        super.setMuteAudio(b);

        if (!isAudioPreviewing()) {
            //mMediaPlayerCapture.getMediaPlayer().setPlayerMute(b ? 1 : 0);
        }
    }

    @Override
    public void setEnableAudioPreview(boolean b) {
        super.setEnableAudioPreview(b);

        if (!isAudioMuted()) {
            //mMediaPlayerCapture.getMediaPlayer().setPlayerMute(b ? 1 : 0);
        } else {
            //mMediaPlayerCapture.getMediaPlayer().setPlayerMute(1);
        }
    }

    @Override
    public void release() {
        super.release();
        mMediaPlayerCapture.release();
    }

    /**
     * Get {@link PictureCapture} module instance.
     *
     * @return PictureCapture instance.
     */
    public PictureCapture getPictureCapture() {
        return mPictureCapture;
    }

    /**
     * Get {@link MediaPlayerCapture} module instance.
     *
     * @return MediaPlayerCapture instance.
     */
    //public MediaPlayerCapture getMediaPlayerCapture() {
//        return mMediaPlayerCapture;
//    }
    public void showBgPicture(Context context, String uri) {
        mPictureCapture.start(context, uri);
    }

    public void hideBgPicture() {
        mPictureCapture.stop();
    }

    public void showBgVideo(String url) {
        mMediaPlayerCapture.start(url);
        if (isAudioPreviewing()) {
            //mMediaPlayerCapture.getMediaPlayer().setPlayerMute(1);
        }
    }

    public void hideBgVideo() {
        mMediaPlayerCapture.stop();
    }

    private void drawPreviewRectBg(float x, float y, float w, float h) {
        mImgTexPreviewMixer.setRenderRect(mIdxBgImage, x - 0.01f, y - 0.005f, w + 0.02f, h + 0.01f, 1.0f);
        mImgTexMixer.setRenderRect(mIdxBgImage, x - 0.01f, y - 0.005f, w + 0.02f, h + 0.01f, 1.0f);
        //mVideoEncoderMgt.getImgBufMixer().setRenderRect(mIdxBgImage, x, y, w + 0.01f, h + 0.01f, 1.0f);
        //mWaterMarkCapture.showLogo(mContext, var1, var4, var5);
    }

    public void setCameraPreviewRect(float x, float y, float w, float h) {
        drawPreviewRectBg(x, y, w, h);

        mImgTexPreviewMixer.setRenderRect(mIdxCamera, x, y, w, h, 1.0f);
        mImgTexMixer.setRenderRect(mIdxCamera, x, y, w, h, 1.0f);

        mSubViewWidth = w;
        mSubViewHeight = h;
        mSubViewX = x;
        mSubViewY = y;
    }

    public void setVideoPreviewRect(float x, float y, float w, float h) {
        drawPreviewRectBg(x, y, w, h);

        mImgTexPreviewMixer.setRenderRect(mIdxVideo, x, y, w, h, 1.0f);
        mImgTexMixer.setRenderRect(mIdxVideo, x, y, w, h, 1.0f);

        mSubViewWidth = w;
        mSubViewHeight = h;
        mSubViewX = x;
        mSubViewY = y;
    }

    public void setFullCameraPreview() {
        mImgTexPreviewMixer.setRenderRect(mIdxCamera, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
        mImgTexMixer.setRenderRect(mIdxCamera, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
    }

    public void setFullVideoPreview() {
        mImgTexPreviewMixer.setRenderRect(mIdxVideo, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
        mImgTexMixer.setRenderRect(mIdxVideo, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
    }

    private String getMainScreenText() {
        if (mMainScreen == MAIN_SCREEN_CAMERA) {
            return "MAIN_SCREEN_CAMERA";
        } else {
            return "MAIN_SCREEN_VIDEO";
        }
    }

    public void switchMainScreen() {
        mPictureCapture.imgTexSrcPin.disconnect(false);
        mMediaPlayerCapture.imgTexSrcPin.disconnect(false);
        mMediaPlayerCapture.audioBufSrcPin.connect(getAudioMixer().getSinkPin(IDX_AUDIO_PIP));

        if (mMainScreen == MAIN_SCREEN_CAMERA) {
            mMainScreen = MAIN_SCREEN_VIDEO;
            mIdxBgImage = 4;
            mIdxVideo = 5;

            mPictureCapture.imgTexSrcPin.connect(mImgTexPreviewMixer.getSinkPin(mIdxBgImage));
            mMediaPlayerCapture.imgTexSrcPin.connect(mImgTexPreviewMixer.getSinkPin(mIdxVideo));
            mImgTexPreviewMixer.setMainSinkPinIndex(mIdxCamera);

            mPictureCapture.imgTexSrcPin.connect(mImgTexMixer.getSinkPin(mIdxBgImage));
            mMediaPlayerCapture.imgTexSrcPin.connect(mImgTexMixer.getSinkPin(mIdxVideo));
            mImgTexMixer.setMainSinkPinIndex(mIdxCamera);
        } else {
            mMainScreen = MAIN_SCREEN_CAMERA;
            mIdxVideo = 1;
            mIdxBgImage = 2;

            mPictureCapture.imgTexSrcPin.connect(mImgTexPreviewMixer.getSinkPin(mIdxBgImage));
            mMediaPlayerCapture.imgTexSrcPin.connect(mImgTexPreviewMixer.getSinkPin(mIdxVideo));
            mImgTexPreviewMixer.setMainSinkPinIndex(mIdxCamera);

            mPictureCapture.imgTexSrcPin.connect(mImgTexMixer.getSinkPin(mIdxBgImage));
            mMediaPlayerCapture.imgTexSrcPin.connect(mImgTexMixer.getSinkPin(mIdxVideo));
            mImgTexMixer.setMainSinkPinIndex(mIdxCamera);
        }

        showBgPicture(mContext, "/sdcard/test.png");

        Logger.e("switchMainScreen:" + getMainScreenText());

        if (mMainScreen == MAIN_SCREEN_VIDEO) {
            setFullCameraPreview();
            setVideoPreviewRect(mSubViewX, mSubViewY, mSubViewWidth, mSubViewHeight);
        } else {
            setFullVideoPreview();
            setCameraPreviewRect(mSubViewX, mSubViewY, mSubViewWidth, mSubViewHeight);
        }
    }

    public RectF getSubScreenRect() {
        if (mMainScreen == MAIN_SCREEN_VIDEO) {
            return mImgTexMixer.getRenderRect(mIdxVideo);
        } else {
            return mImgTexMixer.getRenderRect(mIdxCamera);
        }
    }

    public void setSubScreenRect(float x, float y, float w, float h) {
        Logger.e("setSubScreenRect x:" + x + " y:" + y + " w:" + w + " h:" + h);

        if (mMainScreen == MAIN_SCREEN_VIDEO) {
            setVideoPreviewRect(x, y, w, h);
        } else {
            setCameraPreviewRect(x, y, w, h);
        }
    }
}