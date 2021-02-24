package com.enliple.pudding.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.RelativeLayout;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.enliple.pudding.AbsBaseActivity;
import com.enliple.pudding.R;
import com.enliple.pudding.adapter.shoptree.PhotoReviewAdapter;
import com.enliple.pudding.commons.log.Logger;
import com.enliple.pudding.commons.app.ImageLoad;
import com.enliple.pudding.commons.app.NetworkStatusUtils;
import com.enliple.pudding.commons.app.Utils;
import com.enliple.pudding.commons.internal.AppPreferences;
import com.enliple.pudding.commons.network.NetworkApi;
import com.enliple.pudding.commons.network.NetworkBus;
import com.enliple.pudding.commons.network.NetworkBusResponse;
import com.enliple.pudding.commons.network.vo.API46;
import com.enliple.pudding.widget.ReportDialog;
import com.enliple.pudding.widget.SingleButtonDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.RequestBody;

public class PhotoReviewActivity extends AbsBaseActivity {
    private static final String NONE_RECOM = "0";
    private static final String RECOM = "1";
    private static final String NOT_RECOM = "2";
    public static final int RECOM_STATUS_NONE = 0;
    public static final int RECOM_STATUS_SAME_BUTTON_CLICKED = 1;
    public static final int RECOM_STATUS_DIFF_BUTTON_CLICKED = 2;
    private ViewPager pager;
    private PhotoReviewAdapter adapter;
    private RelativeLayout buttonClose;
    private AppCompatTextView page, strNotRecom, strRecom, name, date, option, content;
    private RelativeLayout bot_layer, btnFolder, btnReport, likeLayer, notLikeLayer;
    private AppCompatImageView image, fold_img;
    private int rootWidth, rootHeight, shortRootHeight, iRecommend, iNotRecommend;
    private boolean toggleOpen = false;
    private ArrayList<String> data = new ArrayList<String>();
    private API46.ReviewItem item = new API46.ReviewItem();
    private boolean isLikeClicked = false;
    private String deleteKey = "";
    private boolean isLikeDelete = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_review);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
//        rootWidth = displayMetrics.widthPixels;
//        rootHeight = displayMetrics.heightPixels - Utils.ConvertDpToPx(PhotoReviewActivity.this, 167);
//        shortRootHeight = displayMetrics.heightPixels - Utils.ConvertDpToPx(PhotoReviewActivity.this, 297);
        EventBus.getDefault().register(this);

        Intent intent = getIntent();
        if (intent != null) {
            String result = intent.getStringExtra("PHOTO_REVIEW");
            if (!TextUtils.isEmpty(result)) {
                try {
                    Logger.e("result ::::: " + result);
                    JSONObject object = new JSONObject(result);
                    item.is_id = object.optString("is_id");
                    item.ct_id = object.optString("ct_id");
                    item.is_type = object.optString("is_type");
                    item.is_score = object.optString("is_score");
                    item.is_subject = object.optString("is_subject");
                    item.is_content = object.optString("is_content");
                    item.is_time = object.optString("is_time");
                    item.mb_id = object.optString("mb_id");
                    item.mb_nick = object.optString("mb_nick");
                    item.mb_user_img = object.optString("mb_user_img");
                    item.ct_option = object.optString("ct_option");
                    item.recommend = object.optString("recommend");
                    item.not_recommend = object.optString("not_recommend");
                    item.is_mine = object.optString("is_mine");
                    item.is_recommend = object.optString("is_recommend");

                    try {
                        iRecommend = Integer.valueOf(item.recommend);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        iNotRecommend = Integer.valueOf(item.not_recommend);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    JSONArray array = object.optJSONArray("is_photo");
                    if (array != null && array.length() > 0) {
                        List<String> images = new ArrayList<String>();
                        for (int i = 0; i < array.length(); i++) {
                            images.add((String) array.get(i));
                            data.add((String) array.get(i));
                        }
                        item.is_photo = images;
                    }

                    image = findViewById(R.id.image);
                    name = findViewById(R.id.name);
                    date = findViewById(R.id.date);
                    option = findViewById(R.id.option);
                    content = findViewById(R.id.content);
                    pager = findViewById(R.id.pager);
                    pager.setOffscreenPageLimit(data.size() - 1);
                    bot_layer = findViewById(R.id.bot_layer);
                    btnFolder = findViewById(R.id.btnFold);
                    fold_img = findViewById(R.id.fold_img);
                    btnReport = findViewById(R.id.btnReport);
                    strRecom = findViewById(R.id.strRecom);
                    strNotRecom = findViewById(R.id.strNotRecom);
                    buttonClose = findViewById(R.id.buttonClose);
                    likeLayer = findViewById(R.id.likeLayer);
                    notLikeLayer = findViewById(R.id.notLikeLayer);
                    page = findViewById(R.id.page);

                    name.setText(item.mb_nick);
                    date.setText(item.is_time);
                    option.setText(item.ct_option);
                    content.setText(item.is_content);

//                    Glide.with(PhotoReviewActivity.this)
//                            .load(item.mb_user_img)
//                            .placeholder(R.drawable.profile_icon_bg)
//                            .priority(Priority.HIGH)
//                            .centerCrop()
//                            .diskCacheStrategy(DiskCacheStrategy.ALL)
//                            .into(image);
                    ImageLoad.setImage(PhotoReviewActivity.this, image, item.mb_user_img, R.drawable.profile_icon_bg, ImageLoad.SCALE_CENTER_CROP, DiskCacheStrategy.ALL);

                    String text = 1 + "/" + data.size();
                    page.setText(text);
                    strRecom.setText(getString(R.string.msg_recom, item.recommend));
                    strNotRecom.setText(getString(R.string.msg_not_recom, item.not_recommend));
                    if ("0".equals(item.is_recommend)) {
                        strRecom.setTextColor(0xff192028);
                        strNotRecom.setTextColor(0xff192028);
                    } else if ("1".equals(item.is_recommend)) {
                        strRecom.setTextColor(0xffff6c6c); // 붉은색
                        strNotRecom.setTextColor(0xff192028); // 진회색
                    } else if ("2".equals(item.is_recommend)) {
                        strRecom.setTextColor(0xff192028);
                        strNotRecom.setTextColor(0xffff6c6c);
                    } else {
                        strRecom.setTextColor(0xff192028);
                        strNotRecom.setTextColor(0xff192028);
                    }
                    buttonClose.setOnClickListener(clickListener);
                    btnFolder.setOnClickListener(clickListener);
                    btnReport.setOnClickListener(clickListener);
                    if (!"Y".equals(item.is_mine)) {
                        likeLayer.setOnClickListener(clickListener);
                        notLikeLayer.setOnClickListener(clickListener);
                    }

                    pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                        @Override
                        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                            int pos = position + 1;
                            String text = pos + "/" + data.size();
                            page.setText(text);
                        }

                        @Override
                        public void onPageSelected(int position) {
                            int pos = position + 1;
                            String text = pos + "/" + data.size();
                            page.setText(text);
                        }

                        @Override
                        public void onPageScrollStateChanged(int state) {

                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    finish();
                }
            } else {
                finish();
            }
        } else {
            finish();
        }

        Logger.e("rootHeight onCreate :: " + rootHeight);
        Logger.e("shortRootHeight onCreate :: " + shortRootHeight);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(NetworkBusResponse data) {
        if (data.arg1.equals(NetworkApi.API47.toString())) {
            if ("ok".equals(data.arg2)) {
                if (isLikeClicked) {
                    // like 추가임
                    strRecom.setTextColor(0xffff6c6c);
                    strNotRecom.setTextColor(0xff192028);
                    iRecommend++;
                    strRecom.setText(getString(R.string.msg_recom, "" + iRecommend));
                    strNotRecom.setText(getString(R.string.msg_not_recom, "" + iNotRecommend));
                    item.is_recommend = "1";
                } else {
                    strRecom.setTextColor(0xff192028);
                    strNotRecom.setTextColor(0xffff6c6c);
                    iNotRecommend++;
                    strRecom.setText(getString(R.string.msg_recom, "" + iRecommend));
                    strNotRecom.setText(getString(R.string.msg_not_recom, "" + iNotRecommend));
                    item.is_recommend = "2";
                }
            } else if ("fail".equals(data.arg2) && "400".equals(data.arg3)) {
                Logger.e("fail 400 is_recommend :: " + item.is_recommend + " isLikeClicked :: " + isLikeClicked);
                if (item.is_recommend.equals("1") && isLikeClicked) {
                    Logger.e("like delete");
                    isLikeDelete = true;
                    deleteKey = NetworkApi.API50.toString() + "?is_id=" + item.is_id + "&user=" + AppPreferences.Companion.getUserId(PhotoReviewActivity.this);
                    NetworkBus bus = new NetworkBus(NetworkApi.API50.name(), item.is_id, AppPreferences.Companion.getUserId(PhotoReviewActivity.this));
                    EventBus.getDefault().post(bus);
                } else if (item.is_recommend.equals("2") && !isLikeClicked) {
                    Logger.e("not like delete");
                    isLikeDelete = false;
                    deleteKey = NetworkApi.API50.toString() + "?is_id=" + item.is_id + "&user=" + AppPreferences.Companion.getUserId(PhotoReviewActivity.this);
                    NetworkBus bus = new NetworkBus(NetworkApi.API50.name(), item.is_id, AppPreferences.Companion.getUserId(PhotoReviewActivity.this));
                    EventBus.getDefault().post(bus);
                } else {
                    String str = data.arg4;
                    String message = "이미 평가하였습니다. 변경하시려면 앞선 평가를 다시 눌러서 취소하고 시도해주세요.";
                    if (!TextUtils.isEmpty(data.arg4)) {
                        try {
                            JSONObject object = new JSONObject(str);
                            message = object.optString("message");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    DisplayMetrics displayMetrics = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                    int screenWidth = displayMetrics.widthPixels;
                    int dialogWidth = screenWidth - Utils.ConvertDpToPx(PhotoReviewActivity.this, 40);
                    new SingleButtonDialog(PhotoReviewActivity.this, dialogWidth, message, "확인", new SingleButtonDialog.SingleButtonDialogListener() {
                        @Override
                        public void onConfirm() {

                        }
                    }).show();
                }
            }
        } else if (data.arg1.equals(deleteKey)) {
            if ("ok".equals(data.arg2)) {
                if (isLikeDelete) {
                    if (iRecommend > 0)
                        iRecommend--;
                    strRecom.setText(getString(R.string.msg_recom, "" + iRecommend));
                    strNotRecom.setText(getString(R.string.msg_not_recom, "" + iNotRecommend));
                } else {
                    if (iNotRecommend > 0)
                        iNotRecommend--;
                    strRecom.setText(getString(R.string.msg_recom, "" + iRecommend));
                    strNotRecom.setText(getString(R.string.msg_not_recom, "" + iNotRecommend));
                }
                strRecom.setTextColor(0xff192028);
                strNotRecom.setTextColor(0xff192028);
                item.is_recommend = "0";
            }
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        int pagerHeight = pager.getMeasuredHeight();
        int pagerWidth = pager.getMeasuredWidth();
        rootHeight = pagerHeight;
        rootWidth = pagerWidth;
        shortRootHeight = pagerHeight - Utils.ConvertDpToPx(PhotoReviewActivity.this, 130);
        Logger.e("rootHeight onWindowFocusChanged :: " + rootHeight);
        Logger.e("shortRootHeight onWindowFocusChanged :: " + shortRootHeight);
        adapter = new PhotoReviewAdapter(getSupportFragmentManager(), PhotoReviewActivity.this, data, pagerWidth, rootHeight, shortRootHeight, toggleOpen);
        pager.setAdapter(adapter);
        adapter.setItem(data);

        sendBroadcast(toggleOpen);
    }

    private void setToggleOpen() {
        toggleOpen = !toggleOpen;
        if (toggleOpen) {
            fold_img.setBackgroundResource(R.drawable.fold_arrow_ico);
            RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, Utils.ConvertDpToPx(PhotoReviewActivity.this, 240));
            param.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            bot_layer.setLayoutParams(param);
        } else {
            fold_img.setBackgroundResource(R.drawable.spread_arrow_ico);
            RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, Utils.ConvertDpToPx(PhotoReviewActivity.this, 110));
            param.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            bot_layer.setLayoutParams(param);
        }

//        sendBroadcast(toggleOpen);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (adapter != null) {
                    adapter.setToggle(toggleOpen);
                    sendBroadcast(toggleOpen);
                }
            }
        }, 100);

    }

    private void sendBroadcast(boolean opened) {
        Intent sendIntent = new Intent("is_exp_opened");
        sendIntent.putExtra("isOpened", opened);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                LocalBroadcastManager.getInstance(PhotoReviewActivity.this).sendBroadcast(sendIntent);
            }
        });
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.buttonClose:
                    finish();
                    break;
                case R.id.btnFold:
                    setToggleOpen();
                    break;
                case R.id.btnReport:
                    ReportDialog dialog = new ReportDialog(PhotoReviewActivity.this, item.is_id);
                    dialog.show();
                    break;
                case R.id.likeLayer:
                    boolean isLogin = AppPreferences.Companion.getLoginStatus(PhotoReviewActivity.this);
                    if (!isLogin) {
                        showLoginDialog();
                    } else {
                        isLikeClicked = true;
                        HashMap<String, String> map = new HashMap<>();
                        map.put("is_id", item.is_id);
                        map.put("type", "1");
                        map.put("user", AppPreferences.Companion.getUserId(PhotoReviewActivity.this));

                        RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), (new JSONObject(map)).toString());

                        NetworkBus bus = new NetworkBus(NetworkApi.API47.name(), body);
                        EventBus.getDefault().post(bus);
                    }

                    break;
                case R.id.notLikeLayer:
                    isLogin = AppPreferences.Companion.getLoginStatus(PhotoReviewActivity.this);
                    if (!isLogin) {
                        showLoginDialog();
                    } else {
                        isLikeClicked = false;
                        HashMap<String, String> map = new HashMap<>();
                        map.put("is_id", item.is_id);
                        map.put("type", "2");
                        map.put("user", AppPreferences.Companion.getUserId(PhotoReviewActivity.this));

                        RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), (new JSONObject(map)).toString());

                        NetworkBus bus = new NetworkBus(NetworkApi.API47.name(), body);
                        EventBus.getDefault().post(bus);
                    }
                    break;
            }
        }
    };

    private void showLoginDialog() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int dialogWidth = screenWidth - Utils.ConvertDpToPx(PhotoReviewActivity.this, 40);
        new SingleButtonDialog(PhotoReviewActivity.this, dialogWidth, "추천/비추천은 로그인 후 이용 가능합니다.", "확인", new SingleButtonDialog.SingleButtonDialogListener() {
            @Override
            public void onConfirm() {

            }
        }).show();
    }

    @Override
    public void onNetworkStatusChanged(@NotNull NetworkStatusUtils.NetworkStatus status) {

    }

    @Override
    public void onDozeModeStateChanged(boolean dozeEnable) {

    }
}
