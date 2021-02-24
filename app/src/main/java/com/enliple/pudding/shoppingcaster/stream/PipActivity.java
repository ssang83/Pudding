package com.enliple.pudding.shoppingcaster.stream;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.RectF;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.enliple.pudding.R;
import com.enliple.pudding.commons.log.Logger;
import com.enliple.pudding.shoppingcaster.VCommerceStreamer;
import com.enliple.pudding.shoppingcaster.activity.VODSelectActivity;
import com.enliple.pudding.shoppingcaster.fragment.BaseScreenFragment;
import com.enliple.pudding.shoppingcaster.screen.FloatingView;
import com.ksyun.media.player.IMediaPlayer;
import com.ksyun.media.streamer.capture.camera.CameraTouchHelper;
import com.ksyun.media.streamer.filter.imgtex.ImgTexFilter;
import com.ksyun.media.streamer.filter.imgtex.ImgTexFilterBase;
import com.ksyun.media.streamer.filter.imgtex.ImgTexFilterMgt;
import com.ksyun.media.streamer.kit.OnAudioRawDataListener;
import com.ksyun.media.streamer.kit.OnPreviewFrameListener;
import com.ksyun.media.streamer.kit.StreamerConstants;
import com.ksyun.media.streamer.logstats.StatsLogReport;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class PipActivity extends Activity implements ActivityCompat.OnRequestPermissionsResultCallback {
    private static final String TAG = "PipActivity";

    private GLSurfaceView mCameraPreviewView;
    //private TextureView mCameraPreviewView;
    CameraTouchHelper mCameraTouchHelper;
    private CameraHintView mCameraHintView;
    private Chronometer mChronometer;
    private View mDeleteView;
    private View mSwitchCameraView;
    private View mFlashView;
    private TextView mShootingText;
    private CheckBox mWaterMarkCheckBox;
    private CheckBox mBeautyCheckBox;
    private CheckBox mReverbCheckBox;
    private CheckBox mAudioPreviewCheckBox;
    private CheckBox mBgmCheckBox;
    private CheckBox mMuteCheckBox;
    private CheckBox mAudioOnlyCheckBox;
    private CheckBox mPipCheckBox;
    private CheckBox mFrontMirrorCheckBox;
    private TextView mUrlTextView;
    private TextView mDebugInfoTextView;

    private ButtonObserver mObserverButton;
    private CheckBoxObserver mCheckBoxObserver;

    //private VCommerceStreamer mStreamer;
    private KSYPipStreamer mStreamer;
    private Handler mMainHandler;
    private Timer mTimer;

    private boolean mAutoStart;
    private boolean mIsLandscape;
    private boolean mPrintDebugInfo = false;
    private boolean mRecording = false;
    private boolean mPipMode = false;
    private boolean isFlashOpened = false;
    private String mUrl;
    private String mDebugInfo = "";
    private String mBgmPath = "/sdcard/test.mp3";
    private String mBgPicPath = "/sdcard/test.png";
    private String mPipVideoPath = "/sdcard/1.mp4";
    private String mLogoPath = "/sdcard/test.png";

    private boolean mHWEncoderUnsupported;
    private boolean mSWEncoderUnsupported;

    private final static int PERMISSION_REQUEST_CAMERA_AUDIOREC = 1;
    private static final String START_STRING = "시작";
    private static final String STOP_STRING = "중지";

    public final static String URL = "url";
    public final static String FRAME_RATE = "framerate";
    public final static String VIDEO_BITRATE = "video_bitrate";
    public final static String AUDIO_BITRATE = "audio_bitrate";
    public final static String VIDEO_RESOLUTION = "video_resolution";
    public final static String LANDSCAPE = "landscape";
    public final static String ENCDODE_METHOD = "encode_method";
    public final static String START_ATUO = "start_auto";
    public static final String SHOW_DEBUGINFO = "show_debuginfo";

    private WindowManager.LayoutParams mWmParams;  //layout的布局
    private WindowManager mWindowManager;
    private volatile boolean mPreviewWindowShow = false;
    private FloatingView mFloatingView;
    private ImageView mFloatingClose;
    //private TextureView mCameraView;
    //private GLSurfaceView mCameraView;

//    public static void startActivity(Context context, int fromType,
//                                     String rtmpUrl, int frameRate,
//                                     int videoBitrate, int audioBitrate,
//                                     int videoResolution, boolean isLandscape,
//                                     int encodeMethod, boolean startAuto,
//                                     boolean showDebugInfo) {
//        Intent intent = new Intent(context, PipActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent.putExtra("type", fromType);
//        intent.putExtra(URL, rtmpUrl);
//        intent.putExtra(FRAME_RATE, frameRate);
//        intent.putExtra(VIDEO_BITRATE, videoBitrate);
//        intent.putExtra(AUDIO_BITRATE, audioBitrate);
//        intent.putExtra(VIDEO_RESOLUTION, videoResolution);
//        intent.putExtra(LANDSCAPE, isLandscape);
//        intent.putExtra(ENCDODE_METHOD, encodeMethod);
//        intent.putExtra(START_ATUO, startAuto);
//        intent.putExtra(SHOW_DEBUGINFO, showDebugInfo);
//        context.startActivity(intent);
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_pip);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mCameraHintView = (CameraHintView) findViewById(R.id.camera_hint);
        mCameraPreviewView = (GLSurfaceView) findViewById(R.id.camera_preview);
        //mCameraPreviewView = (TextureView) findViewById(R.id.camera_preview);
        mUrlTextView = (TextView) findViewById(R.id.url);
        mChronometer = (Chronometer) findViewById(R.id.chronometer);
        mDebugInfoTextView = (TextView) findViewById(R.id.debuginfo);

        mObserverButton = new ButtonObserver();
        mShootingText = (TextView) findViewById(R.id.click_to_shoot);
        mShootingText.setOnClickListener(mObserverButton);
        mDeleteView = findViewById(R.id.backoff);
        mDeleteView.setOnClickListener(mObserverButton);
        mSwitchCameraView = findViewById(R.id.switch_cam);
        mSwitchCameraView.setOnClickListener(mObserverButton);
        mFlashView = findViewById(R.id.flash);
        mFlashView.setOnClickListener(mObserverButton);

        mCheckBoxObserver = new CheckBoxObserver();
        mBeautyCheckBox = (CheckBox) findViewById(R.id.click_to_switch_beauty);
        mBeautyCheckBox.setOnCheckedChangeListener(mCheckBoxObserver);
        mReverbCheckBox = (CheckBox) findViewById(R.id.click_to_select_audio_filter);
        mReverbCheckBox.setOnCheckedChangeListener(mCheckBoxObserver);
        mBgmCheckBox = (CheckBox) findViewById(R.id.bgm);
        mBgmCheckBox.setOnCheckedChangeListener(mCheckBoxObserver);
        mAudioPreviewCheckBox = (CheckBox) findViewById(R.id.ear_mirror);
        mAudioPreviewCheckBox.setOnCheckedChangeListener(mCheckBoxObserver);
        mMuteCheckBox = (CheckBox) findViewById(R.id.mute);
        mMuteCheckBox.setOnCheckedChangeListener(mCheckBoxObserver);
        mWaterMarkCheckBox = (CheckBox) findViewById(R.id.watermark);
        mWaterMarkCheckBox.setOnCheckedChangeListener(mCheckBoxObserver);
        mFrontMirrorCheckBox = (CheckBox) findViewById(R.id.front_camera_mirror);
        mFrontMirrorCheckBox.setOnCheckedChangeListener(mCheckBoxObserver);
        mAudioOnlyCheckBox = (CheckBox) findViewById(R.id.audio_only);
        mAudioOnlyCheckBox.setOnCheckedChangeListener(mCheckBoxObserver);
        mPipCheckBox = (CheckBox) findViewById(R.id.pip);
        mPipCheckBox.setOnCheckedChangeListener(mCheckBoxObserver);

        findViewById(R.id.select).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivityForResult();
                startActivityForResult(new Intent(PipActivity.this, VODSelectActivity.class), 200);
            }
        });

        mMainHandler = new Handler();
        //mStreamer = new VCommerceStreamer(this);
        mStreamer = new KSYPipStreamer(this);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String url = bundle.getString(URL);
            if (!TextUtils.isEmpty(url)) {
                mUrl = url;
                mUrlTextView.setText(mUrl);
                mStreamer.setUrl(url);
            }

            mStreamer.setPreviewFps(15);
            mStreamer.setTargetFps(15);

            int videoBitrate = 800;
            mStreamer.setVideoKBitrate(videoBitrate * 3 / 4, videoBitrate, videoBitrate / 4);

            mStreamer.setAudioKBitrate(48);

            mStreamer.setPreviewResolution(StreamerConstants.VIDEO_RESOLUTION_720P);
            mStreamer.setTargetResolution(StreamerConstants.VIDEO_RESOLUTION_720P); // VIDEO_RESOLUTION_720P

            mStreamer.setEncodeMethod(StreamerConstants.ENCODE_METHOD_SOFTWARE);

            mIsLandscape = false;
            if (mIsLandscape) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
            mStreamer.setRotateDegrees(mIsLandscape ? 90 : 0);

            mAutoStart = false;//bundle.getBoolean(START_AUTO, false);
            mPrintDebugInfo = false;//bundle.getBoolean(SHOW_DEBUG_INFO, false);
        }

        mStreamer.setDisplayPreview(mCameraPreviewView);
        mStreamer.setEnableStreamStatModule(true);
        mStreamer.enableDebugLog(false);

        mStreamer.setFrontCameraMirror(true);
        mStreamer.setMuteAudio(false);
        mStreamer.setEnableAudioPreview(false);

        //mStreamer.setOnInfoListener(mOnInfoListener);
        //mStreamer.setOnErrorListener(mOnErrorListener);
        //mStreamer.setOnLogEventListener(mOnLogEventListener);
        //mStreamer.getImgTexFilterMgt().setFilter(mStreamer.getGLRender(), ImgTexFilterMgt.KSY_FILTER_BEAUTY_DENOISE);
        //mStreamer.setEnableImgBufBeauty(true);
        mStreamer.getImgTexFilterMgt().setOnErrorListener(new ImgTexFilterBase.OnErrorListener() {
            @Override
            public void onError(ImgTexFilterBase filter, int errno) {
                Toast.makeText(PipActivity.this, "필터 적용 실패", Toast.LENGTH_SHORT).show();
                mStreamer.getImgTexFilterMgt().setFilter(mStreamer.getGLRender(), ImgTexFilterMgt.KSY_FILTER_BEAUTY_DISABLE);
            }
        });

        // touch focus and zoom support
        mCameraTouchHelper = new CameraTouchHelper();
        mCameraTouchHelper.setCameraCapture(mStreamer.getCameraCapture());
        mCameraPreviewView.setOnTouchListener(mCameraTouchHelper);
        // set CameraHintView to show focus rect and zoom ratio
        mCameraTouchHelper.setCameraHintView(mCameraHintView);
        //mCameraHintView.hideAll();

        // add jhs
        mCameraTouchHelper.addTouchListener(mSubScreenTouchListener);

        //mStreamer.showBgVideo(mPipVideoPath);
    }

    @Override
    public void onResume() {
        super.onResume();
        startCameraPreviewWithPermCheck();
        mStreamer.onResume();
        if (mPipMode) {
            //mStreamer.getMediaPlayerCapture().getMediaPlayer().start();
            mStreamer.getPictureCapture().start(this, mBgPicPath);
        }
        if (mWaterMarkCheckBox.isChecked()) {
            showWaterMark();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mStreamer.onPause();
        mStreamer.stopCameraPreview();
        if (mPipMode) {
            //mStreamer.getMediaPlayerCapture().getMediaPlayer().pause();
            mStreamer.getPictureCapture().stop();
        }
        hideWaterMark();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMainHandler != null) {
            mMainHandler.removeCallbacksAndMessages(null);
            mMainHandler = null;
        }
        if (mTimer != null) {
            mTimer.cancel();
        }
        mStreamer.release();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 200 && resultCode == Activity.RESULT_OK) {
            String filePath = data.getStringExtra("path");
            Logger.e("onActivityResult:" + filePath);
            mPipVideoPath = filePath;

            mStreamer.hideBgVideo();
            mStreamer.showBgVideo(mPipVideoPath);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                onBackoffClick();
                break;
            default:
                break;
        }
        return true;
    }

    private void startStream() {
        mStreamer.startStream();

        mShootingText.setText(STOP_STRING);
        //mShootingText.postInvalidate();
        mRecording = true;
    }

    private void stopStream() {
        mStreamer.stopStream();
        mChronometer.setBase(SystemClock.elapsedRealtime());
        mChronometer.stop();
        mShootingText.setText(START_STRING);
        //mShootingText.postInvalidate();
        mRecording = false;
    }

    private void beginInfoUploadTimer() {
        if (mPrintDebugInfo && mTimer == null) {
            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    updateDebugInfo();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mDebugInfoTextView.setText(mDebugInfo);
                        }
                    });
                }
            }, 100, 1000);
        }
    }

    //update debug info
    private void updateDebugInfo() {
        if (mStreamer == null) return;
        mDebugInfo = String.format(Locale.getDefault(),
                "RtmpHostIP()=%s DroppedFrameCount()=%d \n " +
                        "ConnectTime()=%d DnsParseTime()=%d \n " +
                        "UploadedKB()=%d EncodedFrames()=%d \n" +
                        "CurrentKBitrate=%d Version()=%s",
                mStreamer.getRtmpHostIP(), mStreamer.getDroppedFrameCount(),
                mStreamer.getConnectTime(), mStreamer.getDnsParseTime(),
                mStreamer.getUploadedKBytes(), mStreamer.getEncodedFrames(),
                mStreamer.getCurrentUploadKBitrate(), mStreamer.getVersion());
    }

    //show watermark in specific location
    private void showWaterMark() {
        if ( BaseScreenFragment.WATERMARK_VISIBLE)
            return;
        if (!mIsLandscape) {
            mStreamer.showWaterMarkLogo(mLogoPath, 0.08f, 0.04f, 0.20f, 0, 0.8f);
            //mStreamer.showWaterMarkTime(0.03f, 0.01f, 0.35f, Color.WHITE, 1.0f);
        } else {
            mStreamer.showWaterMarkLogo(mLogoPath, 0.05f, 0.09f, 0, 0.20f, 0.8f);
            //mStreamer.showWaterMarkTime(0.01f, 0.03f, 0.22f, Color.WHITE, 1.0f);
        }
    }

    private void hideWaterMark() {
        mStreamer.hideWaterMarkLogo();
        //mStreamer.hideWaterMarkTime();
    }

    private void startPip() {
        if (mPipMode) {
            return;
        }
        mPipMode = true;
//        mStreamer.getMediaPlayerCapture().getMediaPlayer()
//                .setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
//                    @Override
//                    public void onCompletion(IMediaPlayer iMediaPlayer) {
//                        Log.d(TAG, "End of the currently playing video");
//                        mStreamer.showBgVideo(mPipVideoPath);
//                    }
//                });
//        mStreamer.getMediaPlayerCapture().getMediaPlayer()
//                .setOnErrorListener(new IMediaPlayer.OnErrorListener() {
//                    @Override
//                    public boolean onError(IMediaPlayer iMediaPlayer, int what, int extra) {
//                        Log.e(TAG, "MediaPlayer error, what=" + what + " extra=" + extra);
//                        return false;
//                    }
//                });

        mStreamer.showBgPicture(this, mBgPicPath);
        mStreamer.showBgVideo(mPipVideoPath);
        //mStreamer.getMediaPlayerCapture().getMediaPlayer().setVolume(0.4f, 0.4f);
        mStreamer.setCameraPreviewRect(0.65f, 0.f, 0.35f, 0.3f);
        mStreamer.switchMainScreen();
        //mStreamer.setVideoPreviewRect(0.65f, 0.f, 0.35f, 0.3f);

        //addFloatView();
        // disable touch focus
        mCameraTouchHelper.setEnableTouchFocus(false);
    }

    private void stopPip() {
        if (!mPipMode) {
            return;
        }
        mStreamer.hideBgPicture();
        mStreamer.hideBgVideo();
        //mStreamer.setCameraPreviewRect(0.f, 0.f, 1.f, 1.f);
        mStreamer.setFullCameraPreview();
        mPipMode = false;

        // enable touch focus
        mCameraTouchHelper.setEnableTouchFocus(true);

        //removeSurfaceWindow();
    }

    // Example to handle camera related operation
    private void setCameraAntiBanding50Hz() {
        Camera.Parameters parameters = mStreamer.getCameraCapture().getCameraParameters();
        if (parameters != null) {
            parameters.setAntibanding(Camera.Parameters.ANTIBANDING_50HZ);
            mStreamer.getCameraCapture().setCameraParameters(parameters);
        }
    }

    private VCommerceStreamer.OnInfoListener mOnInfoListener = new VCommerceStreamer.OnInfoListener() {
        @Override
        public void onInfo(int what, int msg1, int msg2) {
            switch (what) {
                case StreamerConstants.KSY_STREAMER_CAMERA_INIT_DONE:
                    Log.d(TAG, "KSY_STREAMER_CAMERA_INIT_DONE");
                    setCameraAntiBanding50Hz();
                    if (mAutoStart) {
                        startStream();
                    }
                    break;
                case StreamerConstants.KSY_STREAMER_OPEN_STREAM_SUCCESS:
                    Log.d(TAG, "KSY_STREAMER_OPEN_STREAM_SUCCESS");
                    mChronometer.setBase(SystemClock.elapsedRealtime());
                    mChronometer.start();
                    beginInfoUploadTimer();
                    break;
                case StreamerConstants.KSY_STREAMER_FRAME_SEND_SLOW:
                    Log.d(TAG, "KSY_STREAMER_FRAME_SEND_SLOW " + msg1 + "ms");
                    Toast.makeText(PipActivity.this, "Network not good!",
                            Toast.LENGTH_SHORT).show();
                    break;
                case StreamerConstants.KSY_STREAMER_EST_BW_RAISE:
                    Log.d(TAG, "BW raise to " + msg1 / 1000 + "kbps");
                    break;
                case StreamerConstants.KSY_STREAMER_EST_BW_DROP:
                    Log.d(TAG, "BW drop to " + msg1 / 1000 + "kpbs");
                    break;
                default:
                    Log.d(TAG, "OnInfo: " + what + " msg1: " + msg1 + " msg2: " + msg2);
                    break;
            }
        }
    };

    private void handleEncodeError() {
        int encodeMethod = mStreamer.getVideoEncodeMethod();
        if (encodeMethod == StreamerConstants.ENCODE_METHOD_HARDWARE) {
            mHWEncoderUnsupported = true;
            if (mSWEncoderUnsupported) {
                mStreamer.setEncodeMethod(
                        StreamerConstants.ENCODE_METHOD_SOFTWARE_COMPAT);
                Log.e(TAG, "Got HW encoder error, switch to SOFTWARE_COMPAT mode");
            } else {
                mStreamer.setEncodeMethod(StreamerConstants.ENCODE_METHOD_SOFTWARE);
                Log.e(TAG, "Got HW encoder error, switch to SOFTWARE mode");
            }
        } else if (encodeMethod == StreamerConstants.ENCODE_METHOD_SOFTWARE) {
            mSWEncoderUnsupported = true;
            if (mHWEncoderUnsupported) {
                mStreamer.setEncodeMethod(
                        StreamerConstants.ENCODE_METHOD_SOFTWARE_COMPAT);
                Log.e(TAG, "Got SW encoder error, switch to SOFTWARE_COMPAT mode");
            } else {
                mStreamer.setEncodeMethod(StreamerConstants.ENCODE_METHOD_HARDWARE);
                Log.e(TAG, "Got SW encoder error, switch to HARDWARE mode");
            }
        }
    }

    private VCommerceStreamer.OnErrorListener mOnErrorListener = new VCommerceStreamer.OnErrorListener() {
        @Override
        public void onError(int what, int msg1, int msg2) {
            switch (what) {
                case StreamerConstants.KSY_STREAMER_ERROR_DNS_PARSE_FAILED:
                    Log.d(TAG, "KSY_STREAMER_ERROR_DNS_PARSE_FAILED");
                    break;
                case StreamerConstants.KSY_STREAMER_ERROR_CONNECT_FAILED:
                    Log.d(TAG, "KSY_STREAMER_ERROR_CONNECT_FAILED");
                    break;
                case StreamerConstants.KSY_STREAMER_ERROR_PUBLISH_FAILED:
                    Log.d(TAG, "KSY_STREAMER_ERROR_PUBLISH_FAILED");
                    break;
                case StreamerConstants.KSY_STREAMER_ERROR_CONNECT_BREAKED:
                    Log.d(TAG, "KSY_STREAMER_ERROR_CONNECT_BREAKED");
                    break;
                case StreamerConstants.KSY_STREAMER_ERROR_AV_ASYNC:
                    Log.d(TAG, "KSY_STREAMER_ERROR_AV_ASYNC " + msg1 + "ms");
                    break;
                case StreamerConstants.KSY_STREAMER_VIDEO_ENCODER_ERROR_UNSUPPORTED:
                    Log.d(TAG, "KSY_STREAMER_VIDEO_ENCODER_ERROR_UNSUPPORTED");
                    break;
                case StreamerConstants.KSY_STREAMER_VIDEO_ENCODER_ERROR_UNKNOWN:
                    Log.d(TAG, "KSY_STREAMER_VIDEO_ENCODER_ERROR_UNKNOWN");
                    break;
                case StreamerConstants.KSY_STREAMER_AUDIO_ENCODER_ERROR_UNSUPPORTED:
                    Log.d(TAG, "KSY_STREAMER_AUDIO_ENCODER_ERROR_UNSUPPORTED");
                    break;
                case StreamerConstants.KSY_STREAMER_AUDIO_ENCODER_ERROR_UNKNOWN:
                    Log.d(TAG, "KSY_STREAMER_AUDIO_ENCODER_ERROR_UNKNOWN");
                    break;
                case StreamerConstants.KSY_STREAMER_AUDIO_RECORDER_ERROR_START_FAILED:
                    Log.d(TAG, "KSY_STREAMER_AUDIO_RECORDER_ERROR_START_FAILED");
                    break;
                case StreamerConstants.KSY_STREAMER_AUDIO_RECORDER_ERROR_UNKNOWN:
                    Log.d(TAG, "KSY_STREAMER_AUDIO_RECORDER_ERROR_UNKNOWN");
                    break;
                case StreamerConstants.KSY_STREAMER_CAMERA_ERROR_UNKNOWN:
                    Log.d(TAG, "KSY_STREAMER_CAMERA_ERROR_UNKNOWN");
                    break;
                case StreamerConstants.KSY_STREAMER_CAMERA_ERROR_START_FAILED:
                    Log.d(TAG, "KSY_STREAMER_CAMERA_ERROR_START_FAILED");
                    break;
                case StreamerConstants.KSY_STREAMER_CAMERA_ERROR_SERVER_DIED:
                    Log.d(TAG, "KSY_STREAMER_CAMERA_ERROR_SERVER_DIED");
                    break;
                default:
                    Log.d(TAG, "what=" + what + " msg1=" + msg1 + " msg2=" + msg2);
                    break;
            }
            switch (what) {
                case StreamerConstants.KSY_STREAMER_CAMERA_ERROR_UNKNOWN:
                case StreamerConstants.KSY_STREAMER_CAMERA_ERROR_START_FAILED:
                case StreamerConstants.KSY_STREAMER_AUDIO_RECORDER_ERROR_START_FAILED:
                case StreamerConstants.KSY_STREAMER_AUDIO_RECORDER_ERROR_UNKNOWN:
                    break;
                case StreamerConstants.KSY_STREAMER_CAMERA_ERROR_SERVER_DIED:
                    mStreamer.stopCameraPreview();
                    mMainHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startCameraPreviewWithPermCheck();
                        }
                    }, 5000);
                    break;
                case StreamerConstants.KSY_STREAMER_VIDEO_ENCODER_ERROR_UNSUPPORTED:
                case StreamerConstants.KSY_STREAMER_VIDEO_ENCODER_ERROR_UNKNOWN:
                    handleEncodeError();
                default:
                    stopStream();
                    mMainHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startStream();
                        }
                    }, 3000);
                    break;
            }
        }
    };

    private StatsLogReport.OnLogEventListener mOnLogEventListener =
            new StatsLogReport.OnLogEventListener() {
                @Override
                public void onLogEvent(StringBuilder singleLogContent) {
                    Log.i(TAG, "***onLogEvent : " + singleLogContent.toString());
                }
            };

    private OnAudioRawDataListener mOnAudioRawDataListener = new OnAudioRawDataListener() {
        @Override
        public short[] OnAudioRawData(short[] data, int count, int sampleRate, int channels) {
            Log.d(TAG, "OnAudioRawData data.length=" + data.length + " count=" + count);
            return data;
        }
    };

    private OnPreviewFrameListener mOnPreviewFrameListener = new OnPreviewFrameListener() {
        @Override
        public void onPreviewFrame(byte[] data, int width, int height, boolean isRecording) {
            Log.d(TAG, "onPreviewFrame data.length=" + data.length + " " +
                    width + "x" + height + " isRecording=" + isRecording);
        }
    };

    private void onSwitchCamera() {
        mStreamer.switchCamera();
        mCameraHintView.hideAll();
    }

    private void onFlashClick() {
        if (isFlashOpened) {
            mStreamer.toggleTorch(false);
            isFlashOpened = false;
        } else {
            mStreamer.toggleTorch(true);
            isFlashOpened = true;
        }
    }

    private void onBackoffClick() {
        new AlertDialog.Builder(PipActivity.this).setCancelable(true)
                .setTitle("종료 하시겠습니까?")
                .setNegativeButton("아니요", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                    }
                })
                .setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        mChronometer.stop();
                        mRecording = false;
                        PipActivity.this.finish();
                    }
                }).show();
    }

    private void onShootClick() {
        if (mRecording) {
            stopStream();
        } else {
            startStream();
        }
    }

//    public static final int KSY_FILTER_BEAUTY_SOFT = 16;
//    public static final int KSY_FILTER_BEAUTY_SKINWHITEN = 17;
//    public static final int KSY_FILTER_BEAUTY_ILLUSION = 18;
//    public static final int KSY_FILTER_BEAUTY_DENOISE = 19;
//    public static final int KSY_FILTER_BEAUTY_SMOOTH = 20;
//    public static final int KSY_FILTER_BEAUTY_SOFT_EXT = 21;
//    public static final int KSY_FILTER_BEAUTY_SOFT_SHARPEN = 22;
//    public static final int KSY_FILTER_BEAUTY_PRO = 23;
//    public static final int KSY_FILTER_BEAUTY_PRO1 = 24;
//    public static final int KSY_FILTER_BEAUTY_PRO2 = 25;
//    public static final int KSY_FILTER_BEAUTY_PRO3 = 26;
//    public static final int KSY_FILTER_BEAUTY_PRO4 = 27;

    private void showChooseFilter() {
        AlertDialog alertDialog;
        alertDialog = new AlertDialog.Builder(this)
                .setTitle("카메라 필터 설정")
                .setSingleChoiceItems(
                        new String[]{"SOFT", "SKIN_WHITEN", "ILLUSION", "DENOISE", "SMOOTH", "SOFT_EXT", "SOFT_SHARPEN", "PRO", "PRO1", "PRO2", "PRO3", "PRO4",
                                "DEMOFILTER", "GROUP_FILTER"}, -1,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which < 12) {
                                    mStreamer.getImgTexFilterMgt().setFilter(mStreamer.getGLRender(), which + 16);
                                    mStreamer.getImgTexFilterMgt().setFilter(mStreamer.getGLRender(), ImgTexFilterMgt.KSY_FILTER_BEAUTY_DENOISE);
                                } else if (which == 12) {
                                    mStreamer.getImgTexFilterMgt().setFilter(new DemoFilter(mStreamer.getGLRender()));
                                } else if (which == 13) {
                                    List<ImgTexFilter> groupFilter = new LinkedList<>();
                                    groupFilter.add(new DemoFilter2(mStreamer.getGLRender()));
                                    groupFilter.add(new DemoFilter3(mStreamer.getGLRender()));
                                    groupFilter.add(new DemoFilter4(mStreamer.getGLRender()));
                                    mStreamer.getImgTexFilterMgt().setFilter(groupFilter);
                                }
                                dialog.dismiss();
                            }
                        })
                .create();
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    boolean[] mChooseFilter = {false, false};

    private void onBeautyChecked(boolean isChecked) {
        if (isChecked) {
            if (mStreamer.getVideoEncodeMethod() == StreamerConstants.ENCODE_METHOD_SOFTWARE_COMPAT) {
                mStreamer.getImgTexFilterMgt().setFilter(mStreamer.getGLRender(), ImgTexFilterMgt.KSY_FILTER_BEAUTY_DENOISE);
                mStreamer.setEnableImgBufBeauty(true);
            } else {
                showChooseFilter();
            }
        } else {
            mStreamer.getImgTexFilterMgt().setFilter(mStreamer.getGLRender(), ImgTexFilterMgt.KSY_FILTER_BEAUTY_DISABLE);
            mStreamer.setEnableImgBufBeauty(false);
        }
    }

    private void onAudioFilterChecked(boolean isChecked) {
        //showChooseAudioFilter();
    }

    private void onBgmChecked(boolean isChecked) {
        if (isChecked) {
            // use KSYMediaPlayer instead of KSYBgmPlayer
            mStreamer.getAudioPlayerCapture().getMediaPlayer()
                    .setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(IMediaPlayer iMediaPlayer) {
                            Log.d(TAG, "End of the currently playing music");
                        }
                    });
            mStreamer.getAudioPlayerCapture().getMediaPlayer()
                    .setOnErrorListener(new IMediaPlayer.OnErrorListener() {
                        @Override
                        public boolean onError(IMediaPlayer iMediaPlayer, int what, int extra) {
                            Log.e(TAG, "OnErrorListener, Error:" + what + ", extra:" + extra);
                            return false;
                        }
                    });
            mStreamer.getAudioPlayerCapture().getMediaPlayer().setVolume(1.0f, 1.0f);
            mStreamer.setEnableAudioMix(true);
            mStreamer.startBgm(mBgmPath, true);
        } else {
            mStreamer.stopBgm();
        }
    }

    private void onAudioPreviewChecked(boolean isChecked) {
        mStreamer.setEnableAudioPreview(isChecked);
    }

    private void onMuteChecked(boolean isChecked) {
        mStreamer.setMuteAudio(isChecked);
    }

    private void onWaterMarkChecked(boolean isChecked) {
        if (isChecked)
            showWaterMark();
        else
            hideWaterMark();
    }

    private void onFrontMirrorChecked(boolean isChecked) {
        mStreamer.setFrontCameraMirror(isChecked);
    }

    private void onAudioOnlyChecked(boolean isChecked) {
        mStreamer.setAudioOnly(isChecked);
    }

    private void onPipChecked(boolean isChecked) {
        if (isChecked) {
            startPip();
        } else {
            stopPip();
        }
    }

    private void addFloatView() {
        initSurfaceWindow();
        addSurfaceWindow();

        //int rotation = getDisplayRotation();
        //Log.d(TAG, "InitialRotate: " + rotation);
        //KSYGlobalStreamer.getInstance().setRotateDegrees(rotation);
    }

    private int getDisplayRotation() {
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        switch (rotation) {
            case Surface.ROTATION_0:
                return 0;
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 270;
        }
        return 0;
    }

    private int align(int val, int align) {
        return (val + align - 1) / align * align;
    }

    @SuppressLint("RtlHardcoded")
    private void initSurfaceWindow() {
        if (mWindowManager == null) {
            mWmParams = new WindowManager.LayoutParams();
            mWindowManager = (WindowManager) getApplication().getSystemService(Application.WINDOW_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mWmParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                mWmParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
            } else {
                mWmParams.type = WindowManager.LayoutParams.TYPE_TOAST;
            }

            mWmParams.format = PixelFormat.RGBA_8888;

            mWmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            mWmParams.flags |= WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
            mWmParams.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
            mWmParams.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
            mWmParams.gravity = Gravity.RIGHT | Gravity.BOTTOM;

            mWmParams.x = 0;
            mWmParams.y = 0;

            Point screenPoint = new Point();
            mWindowManager.getDefaultDisplay().getSize(screenPoint);
            int screenWidth = screenPoint.x;
            int screenHeight = screenPoint.y;
            int width;
            int height;

            boolean isLandscape = (getDisplayRotation() % 180) != 0;
            if ((isLandscape && screenWidth < screenHeight) || (!isLandscape) && screenWidth > screenHeight) {
                screenWidth = mWindowManager.getDefaultDisplay().getHeight();
                screenHeight = mWindowManager.getDefaultDisplay().getWidth();
            }

            if (screenWidth < screenHeight) {
                width = align(screenWidth / 3, 8);
                height = align(screenHeight / 3, 8);
            } else {
                width = align(screenWidth / 3, 8);
                height = align(screenHeight / 3, 8);
            }
            mWmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
            mWmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

            LayoutInflater inflater = LayoutInflater.from(getApplication());

            mFloatingView = (FloatingView) inflater.inflate(R.layout.camera_preview, null);

            mFloatingClose = (ImageView) mFloatingView.findViewById(R.id.close_camera);
            mFloatingClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackoffClick();
                }
            });

            // texture or glsurface 중 선택
//            TextureView mCameraView = new TextureView(this);
            GLSurfaceView mCameraView = new GLSurfaceView(this);
            mStreamer.setDisplayPreview(mCameraView);

            RelativeLayout.LayoutParams previewLayoutParams = new RelativeLayout.LayoutParams(width, height);
            mFloatingView.addView(mCameraView, previewLayoutParams);
            mFloatingView.setParams(mWmParams);

            //RelativeLayout.LayoutParams closeParams = new RelativeLayout.LayoutParams(50, 50);
            //closeParams.setMargins(10, 10, 0, 0);
            //mFloatingView.addView(mFloatingClose, closeParams);
        }
    }

    private void addSurfaceWindow() {
        if (mWindowManager != null) {
            mWindowManager.addView(mFloatingView, mWmParams);
        }
        mPreviewWindowShow = true;
    }

    private void removeSurfaceWindow() {
        if (mWindowManager != null) {
            mWindowManager.removeViewImmediate(mFloatingView);
        }
        mPreviewWindowShow = false;
    }

    private class ButtonObserver implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            int id = view.getId();
            if (id == R.id.switch_cam) {
                onSwitchCamera();
            } else if (id == R.id.backoff) {
                onBackoffClick();
            } else if (id == R.id.flash) {
                //onFlashClick();
                mStreamer.switchMainScreen();
            } else if (id == R.id.click_to_shoot) {
                onShootClick();
            }
        }
    }

    private class CheckBoxObserver implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            int id = buttonView.getId();
            if (id == R.id.click_to_switch_beauty) {
                onBeautyChecked(isChecked);
            } else if (id == R.id.click_to_select_audio_filter) {
                onAudioFilterChecked(isChecked);
            } else if (id == R.id.bgm) {
                onBgmChecked(isChecked);
            } else if (id == R.id.mute) {
                onMuteChecked(isChecked);
            } else if (id == R.id.watermark) {
                onWaterMarkChecked(isChecked);
            } else if (id == R.id.front_camera_mirror) {
                onFrontMirrorChecked(isChecked);
            } else if (id == R.id.audio_only) {
                onAudioOnlyChecked(isChecked);
            } else if (id == R.id.pip) {
                onPipChecked(isChecked);
            } else if (id == R.id.ear_mirror) {
                onAudioPreviewChecked(isChecked);
            }
        }
    }

    private void startCameraPreviewWithPermCheck() {
        int cameraPerm = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int audioPerm = ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        if (cameraPerm != PackageManager.PERMISSION_GRANTED ||
                audioPerm != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                Log.e(TAG, "No CAMERA or AudioRecord permission, please check");
                Toast.makeText(this, "No CAMERA or AudioRecord permission, please check",
                        Toast.LENGTH_LONG).show();
            } else {
                String[] permissions = {Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.READ_EXTERNAL_STORAGE};
                ActivityCompat.requestPermissions(this, permissions,
                        PERMISSION_REQUEST_CAMERA_AUDIOREC);
            }
        } else {
            mStreamer.startCameraPreview();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CAMERA_AUDIOREC: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mStreamer.startCameraPreview();
                } else {
                    Log.e(TAG, "No CAMERA or AudioRecord permission");
                    Toast.makeText(this, "No CAMERA or AudioRecord permission",
                            Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }


    /***********************************
     * for sub move&switch
     ********************************/
    private float mSubTouchStartX;
    private float mSubTouchStartY;
    private float mLastRawX;
    private float mLastRawY;
    private float mLastX;
    private float mLastY;
    private float mSubMaxX = 0;
    private float mSubMaxY = 0;
    private boolean mIsSubMoved = false;
    private int SUB_TOUCH_MOVE_MARGIN = 30;

    private CameraTouchHelper.OnTouchListener mSubScreenTouchListener = new CameraTouchHelper.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            mLastRawX = event.getRawX();
            mLastRawY = event.getRawY();
            int width = view.getWidth();
            int height = view.getHeight();
            RectF subRect = mStreamer.getSubScreenRect();
            int left = (int) (subRect.left * width);
            int right = (int) (subRect.right * width);
            int top = (int) (subRect.top * height);
            int bottom = (int) (subRect.bottom * height);
            int subWidth = right - left;
            int subHeight = bottom - top;

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (isSubScreenArea(event.getX(), event.getY(), left, right, top, bottom)) {
                        mSubTouchStartX = event.getX() - left;
                        mSubTouchStartY = event.getY() - top;
                        mLastX = event.getX();
                        mLastY = event.getY();
                    }
                    break;

                case MotionEvent.ACTION_MOVE:
                    int moveX = (int) Math.abs(event.getX() - mLastX);
                    int moveY = (int) Math.abs(event.getY() - mLastY);
                    if (mSubTouchStartX > 0f && mSubTouchStartY > 0f && ((moveX > SUB_TOUCH_MOVE_MARGIN) || (moveY > SUB_TOUCH_MOVE_MARGIN))) {
                        mIsSubMoved = true;
                        updateSubPosition(width, height, subWidth, subHeight);
                    }
                    break;

                case MotionEvent.ACTION_UP:
                    if (!mIsSubMoved && isSubScreenArea(event.getX(), event.getY(), left, right, top, bottom)) {
                        mStreamer.switchMainScreen();
                    }

                    mIsSubMoved = false;
                    mSubTouchStartX = 0f;
                    mSubTouchStartY = 0f;
                    mLastX = 0f;
                    mLastY = 0f;
                    break;
            }

            return true;
        }
    };

    private boolean isSubScreenArea(float x, float y, int left, int right, int top, int bottom) {
        if (x > left && x < right && y > top && y < bottom) {
            return true;
        }
        return false;
    }

    private static final int BG_GAP = 20;
    private void updateSubPosition(int screenWidth, int screenHeight, int subWidth, int subHeight) {
        mSubMaxX = screenWidth - subWidth - BG_GAP;
        mSubMaxY = screenHeight - subHeight - BG_GAP;

        float newX = (mLastRawX - mSubTouchStartX);
        float newY = (mLastRawY - mSubTouchStartY);

        if (newX < BG_GAP) {
            newX = BG_GAP;
        }
        if (newY < BG_GAP) {
            newY = BG_GAP;
        }
        if (newX > mSubMaxX) {
            newX = mSubMaxX;
        }
        if (newY > mSubMaxY) {
            newY = mSubMaxY;
        }

        RectF subRect = mStreamer.getSubScreenRect();
        float width = subRect.width();
        float height = subRect.height();

        float left = newX / screenWidth;
        float top = newY / screenHeight;

        mStreamer.setSubScreenRect(left, top, width, height); //ImgTexMixer.SCALING_MODE_CENTER_CROP = 2
    }
}
