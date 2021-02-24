package com.enliple.pudding.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentTransaction;

import com.enliple.pudding.AbsBaseActivity;
import com.enliple.pudding.BuildConfig;
import com.enliple.pudding.R;
import com.enliple.pudding.commons.app.NetworkStatusUtils;
import com.enliple.pudding.commons.app.ShopTreeKey;
import com.enliple.pudding.commons.app.Utils;
import com.enliple.pudding.commons.log.Logger;
import com.enliple.pudding.keyboard.KeyboardHeightProvider;
import com.enliple.pudding.model.PermissionObject;
import com.enliple.pudding.shoppingcaster.activity.CoverSelectActivity;
import com.enliple.pudding.shoppingcaster.fragment.CasterInfoFragment;
import com.enliple.pudding.widget.MainPermissionDialog;
import com.enliple.pudding.widget.PermissionDialog;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class BroadcastSettingActivity extends AbsBaseActivity implements PermissionDialog.ClickListener {
    public static final int PERMISSION_REQUEST_CAMERA_AUDIO_REC = 1;

    public static final int INDEX_CAMERA = 0;
    public static final int INDEX_RECORD_AUDIO = 1;
    public static final int INDEX_READ_EXTERNAL_STORAGE = 2;
    public static final int INDEX_WRITE_EXTERNAL_STORAGE = 3;
    public static final String TRUE_VALUE = "TRUE";
    public static final String FALSE_VALUE = "FALSE";
    public static final String NONE_VALUE = "NONE";

    public static final String TAB_VOD = "tab_vod";
    public static final String TAB_LIVE = "tab_live";
    public static final String TAB_VOD_UPLOAD = "tab_vod_upload";
    public static final String TAB_MULTI_LIVE = "tab_multi_live";
    public static final String TAB_RESERVE_BROADCAST = "tab_reserve_broadcast";
    public static final String TAB_PRODUCT = "tab_product";
    public static final String SCHEDULE_LIST = "schedule_list";

    public static final String INTENT_EXTRA_KEY_CHAT_ROOM_ID = "chat_room_id";
    public static final String INTENT_EXTRA_KEY_CHAT_ACCOUNT = "chat_account";
    public static final String INTENT_EXTRA_KEY_CHAT_NICKNAME = "chat_nickname";
    public static final String INTENT_EXTRA_KEY_TAB = "shopping_cast_tab";
    public static final String INTENT_EXTRA_KEY_SCHEDULE_POSITION = "schedule_position";
    public static final String INTENT_EXTRA_KEY_SCHEDULE_STREAM_KEY = "stream_key";

    public static final String INFO_SUBJECT = "SUBJECT";
    public static final String INFO_REGI = "REGI";
    public static final String INFO_COVER_IMAGE = "COVER_IMAGE";
    public static final String INFO_GUBUN = "GUBUN";
    public static final String INFO_FIRST_CATEGORY = "FIRST_CATEGORY";
    public static final String INFO_SECOND_CATEGORY = "SECOND_CATEGORY";
    public static final String INFO_THIRD_CATEGORY = "THIRD_CATEGORY";
    private static final int ACTIVITY_REQUEST_CODE_IMAGE_PICK = 0xBC01;

    private Uri coverImage;
    private String chatAccount;
    private String chatNickName;
    private String chatRoomId;
    private String castGubun;

    private String from_store;
    private String idx;
    private String name;
    private String image;
    private String price;
    private String storeName;
    private String pCode;
    private String scCode;

    private CasterInfoFragment infoFragment;

    private AppCompatTextView topTitleStr;
    private RelativeLayout buttonClose;
    private FrameLayout layoutUiContainer;

    private long cTime = 0L;
    private MainPermissionDialog dialog = null;
    private ArrayList<String> rationalArray = null;
    private ArrayList<String> requestPermissionArray = null;
    private KeyboardHeightProvider mKeyboardHeightProvider;

    // 편성표 예약 수정 방송정보 설정 관련 변수
    private int schedulePos = -1;
    private String schedule_stream_key = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        setContentView(R.layout.activity_broadcast_setting);

        startCameraPreviewWithPermCheck();

        AbsBaseActivity.Companion.getMActivityList().add(this);        // 강제종료 시키기 위해서 리스트에 추가

        topTitleStr = findViewById(R.id.topTitleStr);
        buttonClose = findViewById(R.id.buttonClose);
        layoutUiContainer = findViewById(R.id.layoutUiContainer);

        buttonClose.setOnClickListener(clickListener);

        mKeyboardHeightProvider = new KeyboardHeightProvider(this);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mKeyboardHeightProvider.start();
            }
        }, 500);

        checkIntent();

        castingInfo();

        if (TAB_RESERVE_BROADCAST.equals(castGubun)) {
            topTitleStr.setText(getResources().getString(R.string.msg_reserve_broadcast_title));

            PermissionDialog dialog = new PermissionDialog(this);
            dialog.setListener(this);
            dialog.show();
        } else if (TAB_LIVE.equals(castGubun) || TAB_PRODUCT.equals(castGubun)) {
            topTitleStr.setText("LIVE");
        } else if (TAB_VOD.equals(castGubun)) {
            topTitleStr.setText("VIDEO 촬영");
        } else if (TAB_VOD_UPLOAD.equals(castGubun)) {
            topTitleStr.setText("앨범 업로드");
        } else if (SCHEDULE_LIST.equals(castGubun)) {
            topTitleStr.setText("방송예약 수정");
        } else {
            topTitleStr.setText("방송정보등록");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Logger.e("ssskkim onResume");
        int cameraPerm = ActivityCompat.checkSelfPermission(BroadcastSettingActivity.this, Manifest.permission.CAMERA);
        int audioPerm = ActivityCompat.checkSelfPermission(BroadcastSettingActivity.this, Manifest.permission.RECORD_AUDIO);
        int readPerm = ActivityCompat.checkSelfPermission(BroadcastSettingActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int writePerm = ActivityCompat.checkSelfPermission(BroadcastSettingActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (cameraPerm == PackageManager.PERMISSION_GRANTED
                && audioPerm == PackageManager.PERMISSION_GRANTED
                && readPerm == PackageManager.PERMISSION_GRANTED
                && writePerm == PackageManager.PERMISSION_GRANTED) {
            if (dialog != null) {
                dialog.dismiss();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mKeyboardHeightProvider != null) {
            mKeyboardHeightProvider.close();
        }

        AbsBaseActivity.Companion.getMActivityList().remove(this);   // 정상적인 종료인 경우는 리스트에서 제거
    }

    public void onBackPressed() {
        if (infoFragment != null) {
            infoFragment.handleBackPressed();
        }
    }

    public void finishWithDialog() {
        Intent intent = new Intent();
        intent.putExtra("IS_DIALOG_SHOW", true);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private void checkIntent() {
        Intent intent = getIntent();
        chatAccount = intent.getStringExtra(INTENT_EXTRA_KEY_CHAT_ACCOUNT);
        chatNickName = intent.getStringExtra(INTENT_EXTRA_KEY_CHAT_NICKNAME);
        chatRoomId = intent.getStringExtra(INTENT_EXTRA_KEY_CHAT_ROOM_ID);
        castGubun = intent.getStringExtra(INTENT_EXTRA_KEY_TAB);

        from_store = intent.getStringExtra("from_store");
        idx = intent.getStringExtra("idx");
        name = intent.getStringExtra("name");
        image = intent.getStringExtra("image");
        price = intent.getStringExtra("price");
        storeName = intent.getStringExtra("storeName");
        pCode = intent.getStringExtra(ShopTreeKey.KEY_PCODE);
        scCode = intent.getStringExtra(ShopTreeKey.KEY_SCCODE);
        schedulePos = intent.getIntExtra(INTENT_EXTRA_KEY_SCHEDULE_POSITION, schedulePos);
        schedule_stream_key = intent.getStringExtra(INTENT_EXTRA_KEY_SCHEDULE_STREAM_KEY);

        Logger.e("checkIntent :: chatAccount :: " + chatAccount);
        Logger.e("checkIntent :: chatNickName :: " + chatNickName);
        Logger.e("checkIntent :: chatRoomId :: " + chatRoomId);
        Logger.e("checkIntent :: castGubun :: " + castGubun);
        Logger.e("checkIntent :: from_store :: " + from_store);
        Logger.e("checkIntent :: idx :: " + idx);
        Logger.e("checkIntent :: name :: " + name);
        Logger.e("checkIntent :: image :: " + image);
        Logger.e("checkIntent :: price :: " + price);
        Logger.e("checkIntent :: storeName :: " + storeName);
        Logger.e("checkIntent :: pCode :: " + pCode);
        Logger.e("checkIntent :: scCode :: " + scCode);
        Logger.e("checkIntent :: schedulePos :: " + schedulePos);
        Logger.e("checkIntent :: schedule_stream_key :: " + schedule_stream_key);
    }

    private void castingInfo() {
        infoFragment = new CasterInfoFragment();
        Bundle bundle = new Bundle();
        bundle.putString(INTENT_EXTRA_KEY_CHAT_ACCOUNT, chatAccount);
        bundle.putString(INTENT_EXTRA_KEY_CHAT_NICKNAME, chatNickName);
        bundle.putString(INTENT_EXTRA_KEY_CHAT_ROOM_ID, chatRoomId);
        bundle.putString(INTENT_EXTRA_KEY_TAB, castGubun);

        bundle.putString("from_store", from_store);
        bundle.putString("idx", idx);
        bundle.putString("name", name);
        bundle.putString("image", image);
        bundle.putString("price", price);
        bundle.putString("storeName", storeName);
        bundle.putString(ShopTreeKey.KEY_PCODE, pCode);
        bundle.putString(ShopTreeKey.KEY_SCCODE, scCode);
        bundle.putInt(INTENT_EXTRA_KEY_SCHEDULE_POSITION, schedulePos);
        bundle.putString(INTENT_EXTRA_KEY_SCHEDULE_STREAM_KEY, schedule_stream_key);

        infoFragment.setArguments(bundle);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(layoutUiContainer.getId(), infoFragment, infoFragment.getClass().getName());
        transaction.commitAllowingStateLoss();
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.buttonClose:
                    if(infoFragment != null) {
                        infoFragment.handleBackPressed();
                    } else {
                        finish();
                    }
                    break;

                case R.id.imageViewSelected:
                    startActivityForResult(new Intent(BroadcastSettingActivity.this, CoverSelectActivity.class), ACTIVITY_REQUEST_CODE_IMAGE_PICK);
                    break;
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Logger.e("ssskkim onRequestPermissionsResult");
        if (requestCode == PERMISSION_REQUEST_CAMERA_AUDIO_REC) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Logger.e("onRequestPermissionsResult  PERMISSION_REQUEST_CAMERA_AUDIO_REC called ");
                    long now = System.currentTimeMillis();
                    long gap = now - cTime;
                    int result_t_count = 0;
                    boolean isAllGranted = true;
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] == 0) {
                            result_t_count++;
                        } else {
                            isAllGranted = false;
                        }
                    }
                    Logger.e("isAllGranted :: " + isAllGranted);
                    if (isAllGranted) {
                        // 모든 권한 허용됨
                        if (dialog != null)
                            dialog.dismiss();
                    } else {
                        if (rationalArray != null) {
                            String prevCamera = rationalArray.get(INDEX_CAMERA);
                            String prevRecordAudio = rationalArray.get(INDEX_RECORD_AUDIO);
                            String prevReadExternalStorage = rationalArray.get(INDEX_READ_EXTERNAL_STORAGE);
                            String prevWriteExternalStorage = rationalArray.get(INDEX_WRITE_EXTERNAL_STORAGE);
                            if (TRUE_VALUE.equals(prevCamera) && TRUE_VALUE.equals(prevRecordAudio) && TRUE_VALUE.equals(prevReadExternalStorage) && TRUE_VALUE.equals(prevWriteExternalStorage)) {
                                // 모두 한번씩 봤었던 권한들인데 전부 설정을 안한 경우 사용자가 승인 하지 않은 것으로 봄
                                finish();
                            } else {
                                int false_cnt = 0;
                                int true_cnt = 0;
                                for (int i = 0; i < rationalArray.size(); i++) {
                                    if (TRUE_VALUE.equals(rationalArray.get(i))) {
                                        true_cnt++;
                                    } else if (FALSE_VALUE.equals(rationalArray.get(i))) {
                                        false_cnt++;
                                    }
                                }

                                // 설정 이전에 권한이 true false가 섞여 있을 경우
                                // 즉, 사용자가 첫 권한 설정이 아니면서 처음에 다시보지 않기를 한 항목이 있을 경우
                                if (false_cnt > 0 && true_cnt > 0) {
                                    if (true_cnt == result_t_count) {
                                        // 설정으로 넘김, 사용자가 이전에 설정 안한 갯수(다시보지않기 제외) 권한설정 갯수가 같으면 다시보지 않기 제외하고는 모두 권한 설정했다는 뜻이므로 다시보지않기 했던 권한 설정할 수 있도록
                                        goSetting();
                                    } else {
                                        // dialog 닫고 화면 닫고, 사용자가 이전 설정 안한것들 중 현재 다시 설정을 안한 값이 있다는 뜻이므로
                                        finish();
                                    }
                                } else if (false_cnt > 0 && true_cnt == 0) {
                                    if (gap < 1000) {
                                        // 설정으로 넘김, 첫 설정이 모두 false 인데 1초이내 결과가 모두 넘어온 경우 즉, 사용자가 이전에 모두 다시보지 않기 눌렀을 경우
                                        goSetting();
                                    } else {
                                        // dialog 닫고 화면 닫고, 첫설정이 모두 false인데 1초 이후 들어온 경우이므로 첫 설정 다이얼로그 떴음, 그런데 결과가 모두 true가 아니므로 사용자가 권한 사용 안하겠다는거.
                                        finish();
                                    }
                                } else if (false_cnt == 0 && true_cnt > 0) {
                                    finish();
                                    // dialog 닫고 화면 닫고, 사용자가 이전에 모두 권한 사용 안함 눌렀는데 결과가 모두 true가 아닌 경우 즉. 사용자가 권한 사용 안하겠다는 의미
                                } else {
                                    goSetting();
                                    // 설정으로 넘김
                                }
                            }
                        }
                    }
                }
            }, 100);
        }
    }

    private void goSetting() {
        Intent i = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID));
        startActivity(i);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTIVITY_REQUEST_CODE_IMAGE_PICK) {
            if (resultCode == Activity.RESULT_OK) {
                coverImage = data.getData();
                if (coverImage != null) {
                    String fileName = getFileName(coverImage);
                    DisplayMetrics displayMetrics = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                    int screenWidth = displayMetrics.widthPixels - Utils.ConvertDpToPx(BroadcastSettingActivity.this, 30);
                    coverImage = saveResizedImage(BroadcastSettingActivity.this, data.getData(), fileName, screenWidth);

                    infoFragment.setCoverImage(coverImage);
                }
            }
        }
    }

    private Uri saveResizedImage(Context context, Uri uri, String name, int resize) {
        if (uri == null || name == null)
            return null;
        Bitmap.CompressFormat fileType = null;
        String lName = name.toLowerCase();
        if (lName.endsWith("png")) {
            fileType = Bitmap.CompressFormat.PNG;
        } else if (lName.endsWith("jpg")) {
            fileType = Bitmap.CompressFormat.JPEG;
        }
        if (fileType == null)
            return null;
        Uri path = null;
        Bitmap bitmap = resize(context, uri, resize);
        if (bitmap != null) {
            String storage = "${context.cacheDir}/temp_image/";
            File file = new File(storage);
            if (!file.exists())
                file.mkdir();
            String fileName = name;
            File tempFile = new File(storage, fileName);

            try {
                tempFile.createNewFile();
                FileOutputStream out = new FileOutputStream(tempFile);
                bitmap.compress(fileType, 90, out);
                out.flush();
                out.close();
                path = Uri.fromFile(tempFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return path;
    }

    private Bitmap resize(Context context, Uri uri, int resize) {
        Bitmap resizeBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();
        try {
            BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, options);

            int width = options.outWidth;
            int height = options.outHeight;
            int samplesize = 1;

            while (true) {
                if (width / 2 < resize)
                    break;
                width /= 2;
                height /= 2;
                samplesize *= 2;
            }

            options.inSampleSize = samplesize;
            Bitmap bitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, options);
            try {
                ExifInterface exif = new ExifInterface(uri.getPath());
                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
                Matrix matrix = new Matrix();
                if (orientation == 6) {
                    matrix.postRotate(90f);
                } else if (orientation == 3) {
                    matrix.postRotate(180f);
                } else if (orientation == 8) {
                    matrix.postRotate(270f);
                }
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            resizeBitmap = bitmap;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return resizeBitmap;
    }

    private String getFileName(Uri uri) {
        if (uri == null)
            return null;
        String path = uri.toString();
        String[] paths = path.split("/");
        return paths[paths.length - 1];
    }

    private void startCameraPreviewWithPermCheck() {
        int cameraPerm = ActivityCompat.checkSelfPermission(BroadcastSettingActivity.this, Manifest.permission.CAMERA);
        int audioPerm = ActivityCompat.checkSelfPermission(BroadcastSettingActivity.this, Manifest.permission.RECORD_AUDIO);
        int readPerm = ActivityCompat.checkSelfPermission(BroadcastSettingActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int writePerm = ActivityCompat.checkSelfPermission(BroadcastSettingActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        Logger.e("cameraPerm :: $cameraPerm");
        Logger.e("audioPerm :: $audioPerm");
        Logger.e("readPerm :: $readPerm");
        Logger.e("writePerm :: $writePerm");
        if (cameraPerm != PackageManager.PERMISSION_GRANTED
                || audioPerm != PackageManager.PERMISSION_GRANTED
                || readPerm != PackageManager.PERMISSION_GRANTED
                || writePerm != PackageManager.PERMISSION_GRANTED) { // 권한 설정이 완료되지 않았을 경우
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                Toast.makeText(BroadcastSettingActivity.this, "No CAMERA or AudioRecord permission, please check", Toast.LENGTH_LONG).show();
            } else {
                PermissionObject object = new PermissionObject();
                ArrayList<PermissionObject.Objects> array = new ArrayList<PermissionObject.Objects>();
                PermissionObject.Objects s_object;
                requestPermissionArray = new ArrayList<>();

                if (cameraPerm != PackageManager.PERMISSION_GRANTED) {
                    s_object = new PermissionObject.Objects();
                    s_object.setImage(R.drawable.authority_camera_ico);
                    s_object.setTitle("카메라 (필수)");
                    s_object.setContent("푸딩에서 라이브 방송을 하기 위한 필수 기능 입니다.");
                    array.add(s_object);
                    requestPermissionArray.add(Manifest.permission.CAMERA);
                }

                if (audioPerm != PackageManager.PERMISSION_GRANTED) {
                    s_object = new PermissionObject.Objects();
                    s_object.setImage(R.drawable.authority_mic_ico);
                    s_object.setTitle("마이크 (필수)");
                    s_object.setContent("푸딩에서 라이브 방송을 하기 위한 필수 기능 입니다.");
                    array.add(s_object);
                    requestPermissionArray.add(Manifest.permission.RECORD_AUDIO);
                }

                if (readPerm != PackageManager.PERMISSION_GRANTED) {
                    s_object = new PermissionObject.Objects();
                    s_object.setImage(R.drawable.authority_pic_ico);
                    s_object.setTitle("사진/미디어/파일 (필수)");
                    s_object.setContent("푸딩에서 라이브 방송을 하기 위한 필수 기능 입니다.");
                    array.add(s_object);
                    requestPermissionArray.add(Manifest.permission.READ_EXTERNAL_STORAGE);
                    requestPermissionArray.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }

                object.setTitle("푸딩 앱을 즐기기 위해\n다음의 앱 권한을 허용해주세요.");
                object.setObjectArray(array);

                dialog = new MainPermissionDialog(object);
                dialog.setListener(new MainPermissionDialog.DialogClickListener() {
                    @Override
                    public void onDialogClick() {
                        cTime = System.currentTimeMillis();
                        rationalArray = new ArrayList();
                        String onlyCameraDenied = NONE_VALUE;
                        String onlyRecordAudioDenied = NONE_VALUE;
                        String onlyReadExternalStorageDenied = NONE_VALUE;
                        String onlyWriteExternalStorageDenied = NONE_VALUE;

                        if (cameraPerm != PackageManager.PERMISSION_GRANTED) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(BroadcastSettingActivity.this, Manifest.permission.CAMERA)) {
                                // 사용자가 다시 보지 않기에 체크하지 않고 권한 설정을 거절한 이력이 있을 경우
                                onlyCameraDenied = TRUE_VALUE;
                            } else {
                                // 사용자가 다시 보지 않기에 체크하고 권한 설정을 거절한 이력이 있을 경우
                                onlyCameraDenied = FALSE_VALUE;
                            }
                        }
                        rationalArray.add(INDEX_CAMERA, onlyCameraDenied);

                        if (audioPerm != PackageManager.PERMISSION_GRANTED) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(BroadcastSettingActivity.this, Manifest.permission.RECORD_AUDIO)) {
                                // 사용자가 다시 보지 않기에 체크하지 않고 권한 설정을 거절한 이력이 있을 경우
                                onlyRecordAudioDenied = TRUE_VALUE;
                            } else {
                                // 사용자가 다시 보지 않기에 체크하고 권한 설정을 거절한 이력이 있을 경우
                                onlyRecordAudioDenied = FALSE_VALUE;
                            }
                        }
                        rationalArray.add(INDEX_RECORD_AUDIO, onlyRecordAudioDenied);

                        if (readPerm != PackageManager.PERMISSION_GRANTED) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(BroadcastSettingActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                                // 사용자가 다시 보지 않기에 체크하지 않고 권한 설정을 거절한 이력이 있을 경우
                                onlyReadExternalStorageDenied = TRUE_VALUE;
                            } else {
                                // 사용자가 다시 보지 않기에 체크하고 권한 설정을 거절한 이력이 있을 경우
                                onlyReadExternalStorageDenied = FALSE_VALUE;
                            }

                        }
                        rationalArray.add(INDEX_READ_EXTERNAL_STORAGE, onlyReadExternalStorageDenied);

                        if (writePerm != PackageManager.PERMISSION_GRANTED) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(BroadcastSettingActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                // 사용자가 다시 보지 않기에 체크하지 않고 권한 설정을 거절한 이력이 있을 경우
                                onlyWriteExternalStorageDenied = TRUE_VALUE;
                            } else {
                                // 사용자가 다시 보지 않기에 체크하고 권한 설정을 거절한 이력이 있을 경우
                                onlyWriteExternalStorageDenied = FALSE_VALUE;
                            }
                        }
                        rationalArray.add(INDEX_WRITE_EXTERNAL_STORAGE, onlyWriteExternalStorageDenied);
                        for (int i = 0; i < rationalArray.size(); i++) {
                            Logger.e("rationalArray value :: " + rationalArray.get(i));
                        }
                        if (requestPermissionArray != null) {
                            String[] permissionArray = requestPermissionArray.toArray(new String[requestPermissionArray.size()]);
                            for (int j = 0; j < permissionArray.length; j++)
                                Logger.e("permissionArray value :: " + permissionArray[j]);
                            ActivityCompat.requestPermissions(BroadcastSettingActivity.this, permissionArray, PERMISSION_REQUEST_CAMERA_AUDIO_REC);
                        }
                    }

                    @Override
                    public void onDismissed() {

                    }
                });
                dialog.show(getSupportFragmentManager(), "");
            }
        }
    }

    public ArrayList<String> getRationale() {
        return rationalArray;
    }

    public void dismissDialog() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    @Override
    public void onNetworkStatusChanged(@NotNull NetworkStatusUtils.NetworkStatus status) {

    }

    @Override
    public void onDozeModeStateChanged(boolean dozeEnable) {

    }

    @Override
    public void onOk() {
        // permission dialog ok
        Logger.d("onOk");
    }

    @Override
    public void onCancel() {
        // permission dialog cancel
        Logger.e("onCancel");

        finish();
    }
}