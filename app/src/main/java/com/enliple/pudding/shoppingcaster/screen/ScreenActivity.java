package com.enliple.pudding.shoppingcaster.screen;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.appcompat.widget.AppCompatSpinner;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.enliple.pudding.commons.log.Logger;
import com.enliple.pudding.R;
import com.enliple.pudding.shoppingcaster.config.BaseStreamConfig;
import com.enliple.pudding.shoppingcaster.fragment.BaseScreenFragment;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.source.LoopingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.ksyun.media.player.IMediaPlayer;
import com.ksyun.media.player.KSYMediaPlayer;
import com.ksyun.media.streamer.filter.imgtex.ImgTexFilterBase;
import com.ksyun.media.streamer.filter.imgtex.ImgTexFilterMgt;
import com.ksyun.media.streamer.kit.StreamerConstants;
import com.ksyun.media.streamer.logstats.StatsLogReport;

import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 */

public class ScreenActivity extends Activity implements ActivityCompat.OnRequestPermissionsResultCallback, View.OnClickListener {
    private static final String TAG = "Logger";

    private final static int PERMISSION_REQUEST_RECORD_AUDIO = 2;
    private final static int PERMISSION_REQUEST_CAMERA = 1;

    private int mCameraPreviewerPermFlag = 0x0;
    private final static int CAMERA_PREVIEW_PERMISSION_CAMERA = 0x01;
    private final static int CAMERA_PREVIEW_PERMISSION_OVERLAY = 0x02;
    private final static int PREVIEW_PERMISSION_ALL_GRANTED = 0x03;
    private final static int OVERLAY_PERMISSION_RESULT_CODE = 10;

    private String mLogoPath = "file:///sdcard/test.jpg";
    private String mRecordUrl = "/sdcard/test.mp4";
    private static final String START_RECORDING = "START_RECORDING";
    private static final String STOP_RECORDING = "STOP_RECORDING";

    //params from DemoActivity
    public final static String URL = "url";
    public final static String FRAME_RATE = "frame_rate";
    public final static String VIDEO_BITRATE = "video_bitrate";
    public final static String AUDIO_BITRATE = "audio_bitrate";
    public final static String VIDEO_RESOLUTION = "video_resolution";
    public final static String ORIENTATION = "orientation";
    public final static String STEREO_STREAM = "stereo_stream";
    public final static String START_AUTO = "start_auto";
    public static final String SHOW_DEBUG_INFO = "show_debug_info";
    public final static String ENCODE_TYPE = "encode_type";
    public final static String ENCODE_METHOD = "encode_method";
    public final static String ENCODE_SCENE = "encode_scene";
    public final static String ENCODE_PROFILE = "encode_profile";

    //button
    private TextView mUrlTextView;
    private TextView mDebugInfoTextView;
    private Chronometer mChronometer;
    private CheckBox mMuteCheckBox;
    private CheckBox mVideoCheckBox;
    private CheckBox mWaterMarkCheckBox;
    private CheckBox mReverbCheckBox;
    private CheckBox mCameraPreviewWindowCheckBox;
    private CheckBox mBeautyCameraPreviewCheckBox;
    private View mDeleteView;
    private TextView mShootingText;
    private TextView mRecordingText;

    //private View mBeautyChooseView;
    private AppCompatSpinner mBeautySpinner;
    private LinearLayout mBeautyGrindLayout;
    private TextView mGrindText;
    private AppCompatSeekBar mGrindSeekBar;
    private LinearLayout mBeautyWhitenLayout;
    private TextView mWhitenText;
    private AppCompatSeekBar mWhitenSeekBar;
    private LinearLayout mBeautyRuddyLayout;
    private TextView mRuddyText;
    private AppCompatSeekBar mRuddySeekBar;

    private CheckBoxObserver mCheckBoxObserver;

    private String mUrl;
    private String mDebugInfo = "";

    private KSYScreenStreamer mScreenStreamer;
    private KSYCameraPreview mCameraPreview;
    private Timer mTimer;
    private Handler mMainHandler;

    //status
    private boolean mRecording = false;
    private boolean mPreviewWindowShow = false;
    private boolean mIsFileRecording = false;
    private boolean mHWEncoderUnsupported;
    private boolean mSWEncoderUnsupported;

    //user params
    private boolean mIsLandscape;
    private boolean mAutoStart;
    private boolean mPrintDebugInfo = false;

    private int mLastRotation;
    private OrientationEventListener mOrientationEventListener;
    private int mPresetOrientation;

    //preview window just demo
    private FloatingView mFloatView;
    private WindowManager.LayoutParams mWmParams;
    private WindowManager mWindowManager;

    //camera preview  params
    private TextureView mCameraTextureView;
    private LinearLayout.LayoutParams mPreviewLayout;
    private ImageView mSwitchCameraView;

    private ImageView mSwitchCameraRotate;
    private ImageView mCloseCamera;

    private int mPreviewFps;
    private int mPreviewResolution;

    private BaseStreamConfig mConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.screen_activity);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // hide bottom nav bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }

        mUrlTextView = (TextView) findViewById(R.id.url);
        mChronometer = (Chronometer) findViewById(R.id.chronometer);
        mDebugInfoTextView = (TextView) findViewById(R.id.debuginfo);

        mShootingText = (TextView) findViewById(R.id.click_to_shoot);
        mShootingText.setOnClickListener(this);
        mRecordingText = (TextView) findViewById(R.id.click_to_record);
        mRecordingText.setOnClickListener(this);
        mDeleteView = findViewById(R.id.backoff);
        mDeleteView.setOnClickListener(this);

        mCheckBoxObserver = new CheckBoxObserver();
        mMuteCheckBox = (CheckBox) findViewById(R.id.mute);
        mMuteCheckBox.setOnCheckedChangeListener(mCheckBoxObserver);

        mVideoCheckBox = (CheckBox) findViewById(R.id.video);
        mVideoCheckBox.setOnCheckedChangeListener(mCheckBoxObserver);

        mWaterMarkCheckBox = (CheckBox) findViewById(R.id.watermark);
        mWaterMarkCheckBox.setOnCheckedChangeListener(mCheckBoxObserver);
        mReverbCheckBox = (CheckBox) findViewById(R.id.click_to_select_audio_filter);
        mReverbCheckBox.setOnCheckedChangeListener(mCheckBoxObserver);
        mCameraPreviewWindowCheckBox = (CheckBox) findViewById(R.id.screenCameraWindow);
        mCameraPreviewWindowCheckBox.setOnCheckedChangeListener(mCheckBoxObserver);
        mBeautyCameraPreviewCheckBox = (CheckBox) findViewById(R.id.click_to_switch_beauty);
        mBeautyCameraPreviewCheckBox.setOnCheckedChangeListener(mCheckBoxObserver);

        //mBeautyChooseView = findViewById(R.id.beauty_choose);
        mBeautySpinner = (AppCompatSpinner) findViewById(R.id.beauty_spin);
        mBeautyGrindLayout = (LinearLayout) findViewById(R.id.beauty_grind);
        mGrindText = (TextView) findViewById(R.id.grind_text);
        mGrindSeekBar = (AppCompatSeekBar) findViewById(R.id.grind_seek_bar);
        mBeautyWhitenLayout = (LinearLayout) findViewById(R.id.beauty_whiten);
        mWhitenText = (TextView) findViewById(R.id.whiten_text);
        mWhitenSeekBar = (AppCompatSeekBar) findViewById(R.id.whiten_seek_bar);
        mBeautyRuddyLayout = (LinearLayout) findViewById(R.id.beauty_ruddy);
        mRuddyText = (TextView) findViewById(R.id.ruddy_text);
        mRuddySeekBar = (AppCompatSeekBar) findViewById(R.id.ruddy_seek_bar);

        if (mBeautyCameraPreviewCheckBox.isChecked()) {
            mBeautyCameraPreviewCheckBox.setEnabled(true);
        } else {
            mBeautyCameraPreviewCheckBox.setEnabled(false);
        }

        mMainHandler = new Handler();
        mScreenStreamer = new KSYScreenStreamer(this);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String url = bundle.getString(URL);
            if (!TextUtils.isEmpty(url)) {
                mUrl = url;
                mUrlTextView.setText(mUrl);
                mScreenStreamer.setUrl(url);
            }

            mScreenStreamer.setTargetFps(30.0f);

            int videoBitrate = 2290;
            mScreenStreamer.setVideoKBitrate(videoBitrate * 3 / 4, videoBitrate, videoBitrate / 4);

            mScreenStreamer.setAudioKBitrate(128);

            mScreenStreamer.setTargetResolution(StreamerConstants.VIDEO_RESOLUTION_1080P); // VIDEO_RESOLUTION_720P

            mScreenStreamer.setEncodeMethod(StreamerConstants.ENCODE_METHOD_HARDWARE);

            int orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            mPresetOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            if (orientation == ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
            } else if (orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                mIsLandscape = true;
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                mScreenStreamer.setIsLandscape(mIsLandscape);
            } else {
                mIsLandscape = false;
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                mScreenStreamer.setIsLandscape(mIsLandscape);
            }
            mLastRotation = getDisplayRotation();

            mOrientationEventListener = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL) {
                @Override
                public void onOrientationChanged(int orientation) {
                    int rotation = getDisplayRotation();
                    if (rotation != mLastRotation) {
                        Logger.d(TAG, "Rotation changed " + mLastRotation + "->" + rotation);
                        mIsLandscape = (rotation % 180) != 0;
                        if (mPresetOrientation == ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR) {
                            mScreenStreamer.setIsLandscape(mIsLandscape);
                            if (mWaterMarkCheckBox.isChecked()) {
                                hideWaterMark();
                                if ( BaseScreenFragment.WATERMARK_VISIBLE )
                                    showWaterMark();
                            }
                        }

                        onSwitchRotate();
                        mLastRotation = rotation;
                    }
                }
            };

            mAutoStart = false;//bundle.getBoolean(START_AUTO, false);
            mPrintDebugInfo = false;//bundle.getBoolean(SHOW_DEBUG_INFO, false);
        }

        //default false may change duraing the streaming
        mScreenStreamer.setEnableAutoRestart(true, 3000);
        mScreenStreamer.setMuteAudio(mMuteCheckBox.isChecked());
        mScreenStreamer.setOnInfoListener(mOnInfoListener);
        mScreenStreamer.setOnErrorListener(mOnErrorListener);
        mScreenStreamer.setOnLogEventListener(mOnLogEventListener);

        onShootClick();
    }

    private void initBeautyUI() {
//        String[] items = new String[]{"DISABLE", "BEAUTY_SOFT", "SKIN_WHITEN", "BEAUTY_ILLUSION",
//                "BEAUTY_DENOISE", "BEAUTY_SMOOTH", "BEAUTY_PRO", "DEMO_FILTER", "GROUP_FILTER"};
//        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
//                android.R.layout.simple_spinner_item, items);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        mBeautySpinner.setAdapter(adapter);
//        mBeautySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                TextView textView = ((TextView) parent.getChildAt(0));
//                if (textView != null) {
//                    textView.setTextColor(getResources().getColor(R.color.font_color_35));
//                }
//                if (position == 0) {
//                    mCameraPreview.getImgTexFilterMgt().setFilter((ImgFilterBase) null);
//                } else if (position <= 5) {
//                    mCameraPreview.getImgTexFilterMgt().setFilter(
//                            mCameraPreview.getGLRender(), position + 15);
//                } else if (position == 6) {
//                    mCameraPreview.getImgTexFilterMgt().setFilter(mCameraPreview.getGLRender(),
//                            ImgTexFilterMgt.KSY_FILTER_BEAUTY_PRO);
//                } else if (position == 7) {
//                    mCameraPreview.getImgTexFilterMgt().setFilter(
//                            new DemoFilter(mCameraPreview.getGLRender()));
//                } else if (position == 8) {
//                    List<ImgTexFilter> groupFilter = new LinkedList<>();
//                    groupFilter.add(new DemoFilter2(mCameraPreview.getGLRender()));
//                    groupFilter.add(new DemoFilter3(mCameraPreview.getGLRender()));
//                    groupFilter.add(new DemoFilter4(mCameraPreview.getGLRender()));
//                    mCameraPreview.getImgTexFilterMgt().setFilter(groupFilter);
//                } else if (position == 9) {
//                    ImgBeautyToneCurveFilter acvFilter = new ImgBeautyToneCurveFilter(mCameraPreview.getGLRender());
//                    acvFilter.setFromCurveFileInputStream(
//                            ScreenActivity.this.getResources().openRawResource(R.raw.tone_cuver_sample));
//
//                    mCameraPreview.getImgTexFilterMgt().setFilter(acvFilter);
//                } else if (position == 10) {
//                    ImgBeautyToneCurveFilter acvFilter = new ImgBeautyToneCurveFilter(mCameraPreview.getGLRender());
//                    acvFilter.setFromCurveFileInputStream(
//                            ScreenActivity.this.getResources().openRawResource(R.raw.fugu));
//
//                    mCameraPreview.getImgTexFilterMgt().setFilter(acvFilter);
//                } else if (position == 11) {
//                    ImgBeautyToneCurveFilter acvFilter = new ImgBeautyToneCurveFilter(mCameraPreview.getGLRender());
//                    acvFilter.setFromCurveFileInputStream(
//                            ScreenActivity.this.getResources().openRawResource(R.raw.jiaopian));
//
//                    mCameraPreview.getImgTexFilterMgt().setFilter(acvFilter);
//                }
//
//                List<ImgFilterBase> filters = mCameraPreview.getImgTexFilterMgt().getFilter();
//                if (filters != null && !filters.isEmpty()) {
//                    final ImgFilterBase filter = filters.get(0);
//                    mBeautyGrindLayout.setVisibility(filter.isGrindRatioSupported() ?
//                            View.VISIBLE : View.GONE);
//                    mBeautyWhitenLayout.setVisibility(filter.isWhitenRatioSupported() ?
//                            View.VISIBLE : View.GONE);
//                    mBeautyRuddyLayout.setVisibility(filter.isRuddyRatioSupported() ?
//                            View.VISIBLE : View.GONE);
//                    SeekBar.OnSeekBarChangeListener seekBarChangeListener =
//                            new SeekBar.OnSeekBarChangeListener() {
//                                @Override
//                                public void onProgressChanged(SeekBar seekBar, int progress,
//                                                              boolean fromUser) {
//                                    if (!fromUser) {
//                                        return;
//                                    }
//                                    float val = progress / 100.f;
//                                    if (seekBar == mGrindSeekBar) {
//                                        filter.setGrindRatio(val);
//                                    } else if (seekBar == mWhitenSeekBar) {
//                                        filter.setWhitenRatio(val);
//                                    } else if (seekBar == mRuddySeekBar) {
//                                        if (filter instanceof ImgBeautyProFilter) {
//                                            val = progress / 50.f - 1.0f;
//                                        }
//                                        filter.setRuddyRatio(val);
//                                    }
//                                }
//
//                                @Override
//                                public void onStartTrackingTouch(SeekBar seekBar) {
//                                }
//
//                                @Override
//                                public void onStopTrackingTouch(SeekBar seekBar) {
//                                }
//                            };
//                    mGrindSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);
//                    mWhitenSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);
//                    mRuddySeekBar.setOnSeekBarChangeListener(seekBarChangeListener);
//                    mGrindSeekBar.setProgress((int) (filter.getGrindRatio() * 100));
//                    mWhitenSeekBar.setProgress((int) (filter.getWhitenRatio() * 100));
//                    int ruddyVal = (int) (filter.getRuddyRatio() * 100);
//                    if (filter instanceof ImgBeautyProFilter) {
//                        ruddyVal = (int) (filter.getRuddyRatio() * 50 + 50);
//                    }
//                    mRuddySeekBar.setProgress(ruddyVal);
//                } else {
//                    mBeautyGrindLayout.setVisibility(View.GONE);
//                    mBeautyWhitenLayout.setVisibility(View.GONE);
//                    mBeautyRuddyLayout.setVisibility(View.GONE);
//                }
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//                // do nothing
//            }
//        });
//        mBeautySpinner.setPopupBackgroundResource(R.color.transparent1);
//        mBeautySpinner.setSelection(4);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mOrientationEventListener != null && mOrientationEventListener.canDetectOrientation()) {
            mOrientationEventListener.enable();
        }
        requestPermission();
    }

    @Override
    public void onPause() {
        super.onPause();
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
        mScreenStreamer.setOnLogEventListener(null);

        if (mOrientationEventListener != null) {
            mOrientationEventListener.disable();
        }

        if (mPreviewWindowShow) {
            removeSurfaceWindow();
        }

        if (mCameraPreview != null) {
            mCameraPreview.release();
        }

        releasePlay();
        mScreenStreamer.release();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                onBackoffClick();
                return true;

            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
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

    /**
     *
     */
    private void startStream() {
        mScreenStreamer.startStream();
        mShootingText.setText("중지");
        mShootingText.postInvalidate();
        mRecording = true;
        if (mWaterMarkCheckBox.isChecked() && BaseScreenFragment.WATERMARK_VISIBLE) {
            showWaterMark();
        }
    }

    //start recording to a local file
    private void startRecord() {
        mScreenStreamer.startRecord(mRecordUrl);
        mRecordingText.setText(STOP_RECORDING);
        mRecordingText.postInvalidate();
        mIsFileRecording = true;

        if (mWaterMarkCheckBox.isChecked() && BaseScreenFragment.WATERMARK_VISIBLE) {
            showWaterMark();
        }
    }

    private void stopRecord() {
        mScreenStreamer.stopRecord();
        mRecordingText.setText(START_RECORDING);
        mRecordingText.postInvalidate();
        mIsFileRecording = false;
        stopChronometer();

        if (mWaterMarkCheckBox.isChecked()) {
            hideWaterMark();
        }
    }

    private void stopChronometer() {
        if (mRecording || mIsFileRecording) {
            return;
        }
        mChronometer.setBase(SystemClock.elapsedRealtime());
        mChronometer.stop();
    }

    /**
     */
    private void stopStream() {
        mScreenStreamer.stopStream();
        mShootingText.setText("시작");
        mShootingText.postInvalidate();
        mRecording = false;
        if (mWaterMarkCheckBox.isChecked()) {
            hideWaterMark();
        }
        stopChronometer();
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
        if (mScreenStreamer == null) return;
        mDebugInfo = String.format(Locale.getDefault(),
                "RtmpHostIP()=%s DroppedFrameCount()=%d \n " +
                        "ConnectTime()=%d DnsParseTime()=%d \n " +
                        "UploadedKB()=%d EncodedFrames()=%d \n" +
                        "CurrentKBitrate=%d \n" +
                        "libscreenrecordVersion=%s \n" +
                        "KSYStreamerVersion=%s \n",
                mScreenStreamer.getRtmpHostIP(), mScreenStreamer.getDroppedFrameCount(),
                mScreenStreamer.getConnectTime(), mScreenStreamer.getDnsParseTime(),
                mScreenStreamer.getUploadedKBytes(), mScreenStreamer.getEncodedFrames(),
                mScreenStreamer.getCurrentUploadKBitrate(), mScreenStreamer.getLibScreenStreamerVersion(),
                mScreenStreamer.getKSYStreamerSDKVersion());
    }

    /**
     * show watermark in specific location
     * do not effect on camera preview window
     */
    private void showWaterMark() {
        if (!mIsLandscape) {
            mScreenStreamer.showWaterMarkLogo(mLogoPath, 0.08f, 0.04f, 0.20f, 0, 0.8f);
            mScreenStreamer.showWaterMarkTime(0.03f, 0.01f, 0.35f, Color.WHITE, 1.0f);
        } else {
            mScreenStreamer.showWaterMarkLogo(mLogoPath, 0.05f, 0.09f, 0, 0.20f, 0.8f);
            mScreenStreamer.showWaterMarkTime(0.01f, 0.03f, 0.22f, Color.WHITE, 1.0f);
        }
    }

    /**
     * hide watermark
     * do not effect on camera preview window
     */
    private void hideWaterMark() {
        mScreenStreamer.hideWaterMarkLogo();
        mScreenStreamer.hideWaterMarkTime();
    }

    // Example to handle camera related operation
    private void setCameraAntiBanding50Hz() {
        Camera.Parameters parameters = mCameraPreview.getCameraCapture().getCameraParameters();
        if (parameters != null) {
            parameters.setAntibanding(Camera.Parameters.ANTIBANDING_50HZ);
            mCameraPreview.getCameraCapture().setCameraParameters(parameters);
        }
    }

    /**
     * 推流状态变更消息回调
     * 通过IKSYScreenStreamer.setOnInfoListener接口设置
     */
    private KSYScreenStreamer.OnInfoListener mOnInfoListener =
            new KSYScreenStreamer.OnInfoListener() {
                @Override
                public void onInfo(int what, int msg1, int msg2) {
                    switch (what) {
                        case StreamerConstants.KSY_STREAMER_OPEN_STREAM_SUCCESS:
                            Logger.d(TAG, "KSY_STREAMER_OPEN_STREAM_SUCCESS");
                            mChronometer.setBase(SystemClock.elapsedRealtime());
                            mChronometer.start();
                            beginInfoUploadTimer();
                            if (mCameraPreviewWindowCheckBox.isChecked() && !mPreviewWindowShow) {
                                startCameraPreview();
                            }
                            break;
                        case StreamerConstants.KSY_STREAMER_FRAME_SEND_SLOW:
                            Logger.d(TAG, "KSY_STREAMER_FRAME_SEND_SLOW " + msg1 + "ms");
                            //Toast.makeText(this, "Network not good!", Toast.LENGTH_SHORT).show();
                            break;
                        case StreamerConstants.KSY_STREAMER_EST_BW_RAISE:
                            Logger.d(TAG, "BW raise to " + msg1 / 1000 + "kbps");
                            break;
                        case StreamerConstants.KSY_STREAMER_EST_BW_DROP:
                            Logger.d(TAG, "BW drop to " + msg1 / 1000 + "kpbs");
                            break;
                        default:
                            Logger.d(TAG, "OnInfo: " + what + " msg1: " + msg1 + " msg2: " + msg2);
                            break;
                    }
                }
            };

    private void handleEncodeError() {
        int encodeMethod = mScreenStreamer.getVideoEncodeMethod();
        if (encodeMethod == StreamerConstants.ENCODE_METHOD_HARDWARE) {
            mHWEncoderUnsupported = true;
            if (mSWEncoderUnsupported) {
                mScreenStreamer.setEncodeMethod(
                        StreamerConstants.ENCODE_METHOD_SOFTWARE_COMPAT);
                Logger.e(TAG, "Got HW encoder error, switch to SOFTWARE_COMPAT mode");
            } else {
                mScreenStreamer.setEncodeMethod(StreamerConstants.ENCODE_METHOD_SOFTWARE);
                Logger.e(TAG, "Got HW encoder error, switch to SOFTWARE mode");
            }
        } else if (encodeMethod == StreamerConstants.ENCODE_METHOD_SOFTWARE) {
            mSWEncoderUnsupported = true;
            if (mHWEncoderUnsupported) {
                Logger.e(TAG, "Got SW encoder error, can not streamer");
            } else {
                mScreenStreamer.setEncodeMethod(StreamerConstants.ENCODE_METHOD_HARDWARE);
                Logger.e(TAG, "Got SW encoder error, switch to HARDWARE mode");
            }
        }
    }

    /**
     */
    private KSYScreenStreamer.OnErrorListener mOnErrorListener =
            new KSYScreenStreamer.OnErrorListener() {
                @Override
                public void onError(int what, int msg1, int msg2) {
                    switch (what) {
                        case StreamerConstants.KSY_STREAMER_ERROR_DNS_PARSE_FAILED:
                            Logger.d(TAG, "KSY_STREAMER_ERROR_DNS_PARSE_FAILED");
                            break;
                        case StreamerConstants.KSY_STREAMER_ERROR_CONNECT_FAILED:
                            Logger.d(TAG, "KSY_STREAMER_ERROR_CONNECT_FAILED");
                            break;
                        case StreamerConstants.KSY_STREAMER_ERROR_PUBLISH_FAILED:
                            Logger.d(TAG, "KSY_STREAMER_ERROR_PUBLISH_FAILED");
                            break;
                        case StreamerConstants.KSY_STREAMER_ERROR_CONNECT_BREAKED:
                            Logger.d(TAG, "KSY_STREAMER_ERROR_CONNECT_BREAKED");
                            break;
                        case StreamerConstants.KSY_STREAMER_ERROR_AV_ASYNC:
                            Logger.d(TAG, "KSY_STREAMER_ERROR_AV_ASYNC " + msg1 + "ms");
                            break;
                        case StreamerConstants.KSY_STREAMER_VIDEO_ENCODER_ERROR_UNSUPPORTED:
                            Logger.d(TAG, "KSY_STREAMER_VIDEO_ENCODER_ERROR_UNSUPPORTED");
                            break;
                        case StreamerConstants.KSY_STREAMER_VIDEO_ENCODER_ERROR_UNKNOWN:
                            Logger.d(TAG, "KSY_STREAMER_VIDEO_ENCODER_ERROR_UNKNOWN");
                            break;
                        case StreamerConstants.KSY_STREAMER_AUDIO_ENCODER_ERROR_UNSUPPORTED:
                            Logger.d(TAG, "KSY_STREAMER_AUDIO_ENCODER_ERROR_UNSUPPORTED");
                            break;
                        case StreamerConstants.KSY_STREAMER_AUDIO_ENCODER_ERROR_UNKNOWN:
                            Logger.d(TAG, "KSY_STREAMER_AUDIO_ENCODER_ERROR_UNKNOWN");
                            break;
                        case StreamerConstants.KSY_STREAMER_AUDIO_RECORDER_ERROR_START_FAILED:
                            Logger.d(TAG, "KSY_STREAMER_AUDIO_RECORDER_ERROR_START_FAILED");
                            break;
                        case StreamerConstants.KSY_STREAMER_AUDIO_RECORDER_ERROR_UNKNOWN:
                            Logger.d(TAG, "KSY_STREAMER_AUDIO_RECORDER_ERROR_UNKNOWN");
                            break;
                        case KSYScreenStreamer.KSY_STREAMER_SCREEN_RECORD_UNSUPPORTED:
                            Logger.d(TAG, "KSY_STREAMER_SCREEN_RECORD_UNSUPPORTED");
                            Toast.makeText(ScreenActivity.this, "you android system is below 21, " +
                                            "can not use screenRecord",
                                    Toast.LENGTH_LONG).show();
                            break;
                        case KSYScreenStreamer.KSY_STREAMER_SCREEN_RECORD_PERMISSION_DENIED:
                            Logger.d(TAG, "KSY_STREAMER_SCREEN_RECORD_PERMISSION_DENIED");
                            Toast.makeText(ScreenActivity.this, "No ScreenRecord permission, please check",
                                    Toast.LENGTH_LONG).show();
                            break;
                        default:
                            Logger.d(TAG, "what=" + what + " msg1=" + msg1 + " msg2=" + msg2);
                            break;
                    }
                    switch (what) {
                        case StreamerConstants.KSY_STREAMER_AUDIO_RECORDER_ERROR_START_FAILED:
                        case StreamerConstants.KSY_STREAMER_AUDIO_RECORDER_ERROR_UNKNOWN:
                            break;
                        case KSYScreenStreamer.KSY_STREAMER_SCREEN_RECORD_UNSUPPORTED:
                            mChronometer.stop();
                            mShootingText.setText("시작");
                            mShootingText.postInvalidate();
                            mRecording = false;
                            break;
                        case StreamerConstants.KSY_STREAMER_FILE_PUBLISHER_CLOSE_FAILED:
                        case StreamerConstants.KSY_STREAMER_FILE_PUBLISHER_ERROR_UNKNOWN:
                        case StreamerConstants.KSY_STREAMER_FILE_PUBLISHER_OPEN_FAILED:
                        case StreamerConstants.KSY_STREAMER_FILE_PUBLISHER_WRITE_FAILED:
                            break;
                        case StreamerConstants.KSY_STREAMER_VIDEO_ENCODER_ERROR_UNSUPPORTED:
                        case StreamerConstants.KSY_STREAMER_VIDEO_ENCODER_ERROR_UNKNOWN: {
                            handleEncodeError();
                            stopStream();
                            mMainHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    startStream();
                                }
                            }, 3000);
                        }
                        break;
                        default:
                            if (mScreenStreamer.getEnableAutoRestart()) {
                                mShootingText.setText("시작");
                                mShootingText.postInvalidate();
                                mRecording = false;
                                stopChronometer();
                            } else {
                                stopStream();
                                mMainHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        startStream();
                                    }
                                }, 3000);
                            }
                            break;
                    }
                }
            };

    /**
     */
    private KSYCameraPreview.OnInfoListener mOnPreviewInfoListener =
            new KSYCameraPreview.OnInfoListener() {
                @Override
                public void onInfo(int what, int msg1, int msg2) {
                    switch (what) {
                        case StreamerConstants.KSY_STREAMER_CAMERA_INIT_DONE:
                            Logger.d(TAG, "KSY_STREAMER_CAMERA_INIT_DONE");
                            setCameraAntiBanding50Hz();
                            break;
                        default:
                            Logger.d(TAG, "OnPreviewInfo: " + what + " msg1: " + msg1 + " msg2: " + msg2);
                            break;
                    }
                }
            };

    /**
     */
    private KSYCameraPreview.OnErrorListener mOnPreviewErrorListener =
            new KSYCameraPreview.OnErrorListener() {
                @Override
                public void onError(int what, int msg1, int msg2) {
                    switch (what) {
                        case StreamerConstants.KSY_STREAMER_CAMERA_ERROR_START_FAILED:
                            Logger.d(TAG, "KSY_STREAMER_CAMERA_ERROR_START_FAILED");
                            break;
                        case StreamerConstants.KSY_STREAMER_CAMERA_ERROR_SERVER_DIED:
                            Logger.d(TAG, "KSY_STREAMER_CAMERA_ERROR_SERVER_DIED");
                            break;
                        case StreamerConstants.KSY_STREAMER_CAMERA_ERROR_EVICTED:
                            Logger.d(TAG, "KSY_STREAMER_CAMERA_ERROR_EVICTED");
                            break;
                        case StreamerConstants.KSY_STREAMER_CAMERA_ERROR_UNKNOWN:
                            Logger.d(TAG, "KSY_STREAMER_CAMERA_ERROR_UNKNOWN");
                            break;
                        default:
                            Logger.d(TAG, "OnPreviewError: " + what + " msg1: " + msg1 + " msg2: " + msg2);
                            break;
                    }

                    if (what == StreamerConstants.KSY_STREAMER_CAMERA_ERROR_SERVER_DIED || what == StreamerConstants.KSY_STREAMER_CAMERA_ERROR_EVICTED) {
                        stopCameraPreview();

                        mMainHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                //startCameraPreviewWithPermissionCheck();
                            }
                        }, 5000);
                    }
                }
            };


    private StatsLogReport.OnLogEventListener mOnLogEventListener = new StatsLogReport.OnLogEventListener() {
        @Override
        public void onLogEvent(StringBuilder singleLogContent) {
            Logger.i(TAG, "***onLogEvent : " + singleLogContent.toString());
        }
    };

    /**
     */
    private void onSwitchCamera() {
        if (mPreviewWindowShow) {
            Logger.e("onSwitchCamera");
            mCameraPreview.switchCamera();
        }
    }

    private void onOpenCamera() {
        if (mCameraTextureView.getVisibility() == View.VISIBLE) {
            mCameraPreview.stopCameraPreview();
            mPreviewLayout = (LinearLayout.LayoutParams) mCameraTextureView.getLayoutParams();
            mCameraTextureView.setVisibility(View.GONE);
            mFloatView.removeView(mCameraTextureView);
        } else {
            mCameraTextureView.setVisibility(View.VISIBLE);
            mFloatView.addView(mCameraTextureView, mPreviewLayout);
            mCameraPreview.startCameraPreview();
        }
    }

    private void onBackoffClick() {
        new AlertDialog.Builder(ScreenActivity.this).setCancelable(true)
                .setTitle("종료 하시겠습니까?")
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        return;
                    }
                })
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        mChronometer.stop();
                        mRecording = false;
                        mIsFileRecording = false;
                        ScreenActivity.this.finish();
                    }
                }).show();
    }

    /**
     */
    private void onShootClick() {
        if (mRecording) {
            stopStream();
        } else {
            startStream();
        }
    }

    private void onRecordClick() {
        if (mIsFileRecording) {
            stopRecord();
        } else {
            startRecord();
        }
    }

    /**
     */
    private boolean[] mChooseFilter = {false, false};

    private void showChooseAudioFilter() {
//        AlertDialog alertDialog;
//        alertDialog = new AlertDialog.Builder(this)
//                .setTitle("请选择音频滤镜")
//                .setMultiChoiceItems(
//                        new String[]{"REVERB", "DEMO",}, mChooseFilter,
//                        new DialogInterface.OnMultiChoiceClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
//                                if (isChecked) {
//                                    mChooseFilter[which] = true;
//                                }
//                            }
//                        }
//                ).setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        //添加多种音频滤镜示例代码
//                        if (mChooseFilter[0] && mChooseFilter[1]) {
//                            List<AudioFilterBase> filters = new LinkedList<>();
//                            AudioReverbFilter reverbFilter = new AudioReverbFilter();
//                            DemoAudioFilter demofilter = new DemoAudioFilter();
//                            filters.add(reverbFilter);
//                            filters.add(demofilter);
//                            mScreenStreamer.getAudioFilterMgt().setFilter(filters);
//                        }
//                        //添加一种音频滤镜示例代码(reverb 美声)
//                        else if (mChooseFilter[0]) {
//                            AudioReverbFilter reverbFilter = new AudioReverbFilter();
//                            mScreenStreamer.getAudioFilterMgt().setFilter(reverbFilter);
//                        } else if (mChooseFilter[1]) {
//                            DemoAudioFilter demofilter = new DemoAudioFilter();
//                            mScreenStreamer.getAudioFilterMgt().setFilter(demofilter);
//                        }
//                        //关闭音频滤镜示例代码
//                        else {
//                            mScreenStreamer.getAudioFilterMgt().setFilter((AudioFilterBase) null);
//                        }
//                        dialog.dismiss();
//                    }
//                })
//                .create();
//        alertDialog.setCancelable(false);
//        alertDialog.show();
    }

    /**
     */
    private void onAudioFilterChecked(boolean isChecked) {
        showChooseAudioFilter();
    }

    /**
     *
     */
    private void onMuteChecked(boolean isChecked) {
        mScreenStreamer.setMuteAudio(isChecked);
    }

    private boolean mIsInit = false;
    private ExoPlayer player;

    private void onVideoChecked(boolean isChecked) {
        Logger.e("onVideoCheck");
        PlayerView playerView = findViewById(R.id.playerView);

        if (isChecked) {
            if (!mIsInit) {
                playerView.setVisibility(View.VISIBLE);

                player = VideoManagerExt.Companion.getExoPlayer(this);
                playerView.setPlayer(player);

                Uri path = Uri.parse("/sdcard/1.mp4");
                MediaSource mediaSource = VideoManagerExt.Companion.getMediaSource(this, path);
                mediaSource = new LoopingMediaSource(mediaSource); // 반복 재생
                player.prepare(mediaSource);

                playerView.setControllerShowTimeoutMs(0);
                mIsInit = true;
            }

            player.setPlayWhenReady(true);
            player.getPlaybackState();
        } else {
            playerView.setVisibility(View.INVISIBLE);

            player.setPlayWhenReady(false);
            player.getPlaybackState();
        }
    }

    /**
     */
    private void onWaterMarkChecked(boolean isChecked) {
        if (isChecked) {
            if (mRecording  && BaseScreenFragment.WATERMARK_VISIBLE) {
                showWaterMark();
            }
        } else {
            hideWaterMark();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.preview_switch_cam) {
            onSwitchCamera();
        } else if (id == R.id.preview_switch_rotate) {
            onSwitchRotate();
        } else if (id == R.id.backoff) {
            onBackoffClick();
        } else if (id == R.id.click_to_shoot) {
            onShootClick();
        } else if (id == R.id.click_to_record) {
            onRecordClick();
        } else if (id == R.id.close_camera) {
            onOpenCamera();
        }
    }

    private class CheckBoxObserver implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            int id = buttonView.getId();
            if (id == R.id.click_to_select_audio_filter) {
                onAudioFilterChecked(isChecked);
            } else if (id == R.id.mute) {
                onMuteChecked(isChecked);
            } else if (id == R.id.watermark) {
                onWaterMarkChecked(isChecked);
            } else if (id == R.id.screenCameraWindow) {
                onPreviewWindowChecked(isChecked);
            } else if (id == R.id.click_to_switch_beauty) {
                onBeautyPreviewChecked(isChecked);
            } else if (id == R.id.video) {
                onVideoChecked(isChecked);
            }
        }
    }

    /**
     */
    private void requestPermission() {
        //audio
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                Logger.e(TAG, "No RECORD_AUDIO permission, please check");
                Toast.makeText(this, "No RECORD_AUDIO permission, please check", Toast.LENGTH_LONG).show();
            } else {
                String[] permissions = {Manifest.permission.RECORD_AUDIO};
                ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_RECORD_AUDIO);
            }
        } else {
            if (mAutoStart) {
                startStream();
            }
        }

        //camera
        if (mPreviewWindowShow) {
            int cameraPerm = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
            if (cameraPerm != PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    Logger.e(TAG, "No CAMERA permission, please check");
                    Toast.makeText(this, "No CAMERA permission, please check", Toast.LENGTH_LONG).show();
                } else {
                    String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE};
                    ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CAMERA);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_RECORD_AUDIO: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    Logger.e(TAG, "No RECORD_AUDIO permission");
                    Toast.makeText(this, "No RECORD_AUDIO permission", Toast.LENGTH_LONG).show();
                }
                break;

            }
            case PERMISSION_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mCameraPreviewerPermFlag |= CAMERA_PREVIEW_PERMISSION_CAMERA;
                    if (mCameraPreviewWindowCheckBox.isChecked() && !mPreviewWindowShow) {
                        if (mCameraPreviewerPermFlag == PREVIEW_PERMISSION_ALL_GRANTED) {
                            startCameraPreview();
                        }
                    }
                } else {
                    if (mPreviewWindowShow) {
                        stopCameraPreview();
                    }

                    Logger.e(TAG, "No CAMERA permission");
                    Toast.makeText(this, "No CAMERA permission", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    /**
     */
    private void onSwitchRotate() {
        if (mPreviewWindowShow) {
            int rotation = getDisplayRotation();
            Logger.i(TAG, "onSwitchRotate:" + rotation);
            mCameraPreview.setRotateDegrees(rotation);

            boolean isLastLandscape = (mLastRotation % 180) != 0;
            boolean isLandscape = (rotation % 180) != 0;
            if (isLastLandscape != isLandscape) {
                int width = mCameraTextureView.getHeight();
                int height = mCameraTextureView.getWidth();
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, height);
                layoutParams.gravity = Gravity.BOTTOM | Gravity.TOP | Gravity.LEFT | Gravity.RIGHT;
                mFloatView.updateViewLayout(mCameraTextureView, layoutParams);

                updateViewPosition();
            }
        }
    }

    /**
     * @param isChecked
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private void onPreviewWindowChecked(boolean isChecked) {
        if (isChecked) {
            //CameraPreviewParamsAlertDialog alertDialog = new CameraPreviewParamsAlertDialog(this);
            //alertDialog.setCancelable(false);
            //alertDialog.show();

            //StreamerConstants.VIDEO_RESOLUTION_720P
            mPreviewResolution = StreamerConstants.VIDEO_RESOLUTION_480P;
            startCameraPreviewWithPermissionCheck();
            mBeautyCameraPreviewCheckBox.setEnabled(true);
        } else {
            if (mPreviewWindowShow) {
                stopCameraPreview();
            }
            mBeautyCameraPreviewCheckBox.setEnabled(false);
        }
    }

    /**
     * 摄像头悬浮窗口美颜切换示例
     *
     * @param isChecked
     */
    private void onBeautyPreviewChecked(boolean isChecked) {
        //mBeautyChooseView.setVisibility((isChecked && mPreviewWindowShow) ? View.VISIBLE : View.INVISIBLE);
    }

    private void startCameraPreviewWithPermissionCheck() {
        Logger.e("startCameraPreviewWithPermissionCheck ScreenActivity");
        if (mCameraPreviewWindowCheckBox.isChecked() && !mPreviewWindowShow) {
            int cameraPerm = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
            boolean canDrawOverlay = true;
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                canDrawOverlay = Settings.canDrawOverlays(this);
            }

            if (cameraPerm != PackageManager.PERMISSION_GRANTED || !canDrawOverlay) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    Logger.e(TAG, "No CAMERA or overLay permission, please check");
                    Toast.makeText(this, "No CAMERA or overLay permission, please check", Toast.LENGTH_LONG).show();
                } else {
                    if (!canDrawOverlay) {
                        Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                        startActivityForResult(i, OVERLAY_PERMISSION_RESULT_CODE);
                    } else {
                        mCameraPreviewerPermFlag |= CAMERA_PREVIEW_PERMISSION_OVERLAY;
                    }

                    if (cameraPerm != PackageManager.PERMISSION_GRANTED) {
                        String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE};
                        ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CAMERA);
                    } else {
                        mCameraPreviewerPermFlag |= CAMERA_PREVIEW_PERMISSION_CAMERA;
                    }
                }
            } else {
                mCameraPreviewerPermFlag = 0x03;
                startCameraPreview();
            }
        }
    }

    /**
     * 申请overlay权限窗口返回
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == OVERLAY_PERMISSION_RESULT_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    Toast.makeText(ScreenActivity.this, "SYSTEM_ALERT_WINDOW not granted", Toast.LENGTH_SHORT).show();
                } else {
                    mCameraPreviewerPermFlag |= CAMERA_PREVIEW_PERMISSION_OVERLAY;
                    if (mCameraPreviewerPermFlag == PREVIEW_PERMISSION_ALL_GRANTED) {
                        startCameraPreview();
                    }
                }
            }
        }
    }

    /**
     */
    private void startCameraPreview() {
        Logger.e("startCameraPreview");

        initSurfaceWindow();

        if (mCameraPreview == null) {
            mCameraPreview = new KSYCameraPreview(this, mScreenStreamer.getGLRender().getEGL10Context());
            mCameraPreview.setDisplayPreview(mCameraTextureView);
            mCameraPreview.setOnErrorListener(mOnPreviewErrorListener);
            mCameraPreview.setOnInfoListener(mOnPreviewInfoListener);

            // set beauty filter
            initBeautyUI();

            mCameraPreview.getImgTexFilterMgt().setOnErrorListener(new ImgTexFilterBase.OnErrorListener() {
                @Override
                public void onError(ImgTexFilterBase filter, int errno) {
                    Toast.makeText(ScreenActivity.this, "필터 적용 실패!!", Toast.LENGTH_SHORT).show();
                    mCameraPreview.getImgTexFilterMgt().setFilter(mCameraPreview.getGLRender(), ImgTexFilterMgt.KSY_FILTER_BEAUTY_DISABLE);
                }
            });
        }

        addSurfaceWindow();

        if (mPreviewFps != 0) {
            mCameraPreview.setPreviewFps(mPreviewFps);
        }

        if (mPreviewResolution >= StreamerConstants.VIDEO_RESOLUTION_360P || mPreviewResolution <= StreamerConstants.VIDEO_RESOLUTION_720P) {
            mCameraPreview.setPreviewResolution(mPreviewResolution);
        }

        mCameraPreview.setRotateDegrees(mLastRotation);
        mCameraPreview.startCameraPreview();
    }

    /**
     */
    private void stopCameraPreview() {
        if (mCameraPreview != null) {
            mCameraPreview.stopCameraPreview();
        }

        removeSurfaceWindow();
    }

    /**
     */
    private void initSurfaceWindow() {
        if (mWindowManager == null) {
            Logger.e("initSurfaceWindow");

            mWmParams = new WindowManager.LayoutParams();
            mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

            Logger.i(TAG, "mWindowManager--->" + mWindowManager);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mWmParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                mWmParams.type = WindowManager.LayoutParams.TYPE_TOAST;
            }
            mWmParams.format = PixelFormat.RGBA_8888;
            mWmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            mWmParams.flags |= WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
            mWmParams.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
            mWmParams.gravity = Gravity.RIGHT | Gravity.TOP;
            mWmParams.x = 0;
            mWmParams.y = 0;

            int screenWidth = mWindowManager.getDefaultDisplay().getWidth();
            int screenHeight = mWindowManager.getDefaultDisplay().getHeight();
            int width;
            int height;

            boolean isLandscape = (getDisplayRotation() % 180) != 0;
            if ((isLandscape && screenWidth < screenHeight) || (!isLandscape) && screenWidth > screenHeight) {
                screenWidth = mWindowManager.getDefaultDisplay().getHeight();
                screenHeight = mWindowManager.getDefaultDisplay().getWidth();
            }

            if (screenWidth < screenHeight) {
                width = align(screenWidth / 3, 8);
                height = align(screenHeight / 4, 8);
            } else {
                width = align(screenWidth / 4, 8);
                height = align(screenHeight / 3, 8);
            }
            mWmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
            mWmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

            LayoutInflater inflater = LayoutInflater.from(getApplication());

            mFloatView = (FloatingView) inflater.inflate(R.layout.camera_preview, null);
            mSwitchCameraView = (ImageView) mFloatView.findViewById(R.id.preview_switch_cam);
            mSwitchCameraView.setOnClickListener(this);
            mSwitchCameraRotate = (ImageView) mFloatView.findViewById(R.id.preview_switch_rotate);
            mSwitchCameraRotate.setOnClickListener(this);
            mCloseCamera = (ImageView) mFloatView.findViewById(R.id.close_camera);
            mCloseCamera.setOnClickListener(this);

            mCameraTextureView = new TextureView(this);
            LinearLayout.LayoutParams previewLayoutParams = new LinearLayout.LayoutParams(width, height);
            previewLayoutParams.gravity = Gravity.BOTTOM | Gravity.TOP | Gravity.LEFT | Gravity.RIGHT;

            mFloatView.addView(mCameraTextureView, previewLayoutParams);
            mFloatView.setParams(mWmParams);
        }
    }

    private void addSurfaceWindow() {
        if (mWindowManager != null) {
            mWindowManager.addView(mFloatView, mWmParams);
        }
        mPreviewWindowShow = true;
    }

    private void removeSurfaceWindow() {
        if (mWindowManager != null) {
            mWindowManager.removeViewImmediate(mFloatView);
        }
        mPreviewWindowShow = false;
    }

    private void updateViewPosition() {
        if (mWmParams != null && mWindowManager != null) {
            mWmParams.gravity = Gravity.RIGHT | Gravity.TOP;

            mWmParams.x = 0;
            mWmParams.y = 0;
            mWindowManager.updateViewLayout(mFloatView, mWmParams);
        }
    }

    private int align(int val, int align) {
        return (val + align - 1) / align * align;
    }

    private KSYMediaPlayer mTestMediaPlayer;
    private AudioInputBase mAudioInputBase;

    private void createMediaPlayer() {
        if (mTestMediaPlayer == null) {
            mTestMediaPlayer = new KSYMediaPlayer.Builder(this).build();
        }
        if (mAudioInputBase == null) {
            mAudioInputBase = new AudioInputBase();
            mScreenStreamer.connectAudioInput(mAudioInputBase);
        }
    }

    private String mBgmPath = "/sdcard/test.mp3";

    private void startPlay() {
        mTestMediaPlayer.reset();
        mTestMediaPlayer.setOnAudioPCMAvailableListener(mOnAudioPCMListener);
        mTestMediaPlayer.setPlayerMute(0);
        mTestMediaPlayer.setLooping(false);
        mTestMediaPlayer.shouldAutoPlay(true);

        try {
            mTestMediaPlayer.setDataSource(mBgmPath);
            mTestMediaPlayer.prepareAsync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopPlay() {
        if (mTestMediaPlayer != null) {
            mTestMediaPlayer.setOnAudioPCMAvailableListener(null);
            mTestMediaPlayer.stop();
        }
    }

    private void releasePlay() {
        mScreenStreamer.disconnectAudioInput(mAudioInputBase, true);
        stopPlay();

        if (mTestMediaPlayer != null) {
            mTestMediaPlayer.release();
            mTestMediaPlayer = null;
        }
    }

    private KSYMediaPlayer.OnAudioPCMListener mOnAudioPCMListener =
            new KSYMediaPlayer.OnAudioPCMListener() {
                @Override
                public void onAudioPCMAvailable(IMediaPlayer iMediaPlayer, ByteBuffer byteBuffer,
                                                long timestamp, int channels, int samplerate,
                                                int samplefmt) {
                    mAudioInputBase.onAudioPCMAvailable(byteBuffer, timestamp, channels, samplerate, samplefmt);
                }
            };
}
