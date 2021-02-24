package com.enliple.pudding.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.enliple.pudding.AbsBaseActivity;
import com.enliple.pudding.R;
import com.enliple.pudding.adapter.ReviewDetailAdapter;
import com.enliple.pudding.bus.SoftKeyboardBus;
import com.enliple.pudding.commons.app.ImageLoad;
import com.enliple.pudding.commons.app.NetworkStatusUtils;
import com.enliple.pudding.commons.app.PriceFormatter;
import com.enliple.pudding.commons.app.SoftKeyboardUtils;
import com.enliple.pudding.commons.app.Utils;
import com.enliple.pudding.commons.db.DBManager;
import com.enliple.pudding.commons.events.OnSingleClickListener;
import com.enliple.pudding.commons.internal.AppPreferences;
import com.enliple.pudding.commons.log.Logger;
import com.enliple.pudding.commons.network.NetworkApi;
import com.enliple.pudding.commons.network.NetworkBus;
import com.enliple.pudding.commons.network.NetworkBusResponse;
import com.enliple.pudding.commons.network.NetworkHandler;
import com.enliple.pudding.commons.network.vo.API21;
import com.enliple.pudding.commons.network.vo.BaseAPI;
import com.enliple.pudding.commons.shoptree.network.ShopTreeAsyncTask;
import com.enliple.pudding.commons.widget.toast.AppToast;
import com.enliple.pudding.keyboard.KeyboardHeightProvider;
import com.enliple.pudding.model.ReviewDetailImageModel;
import com.enliple.pudding.widget.AppAlertDialog;
import com.google.gson.Gson;
import com.joooonho.SelectableRoundedImageView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class WriteDetailReviewActivity extends AbsBaseActivity {

    public static final String WRITE_DETAIL_REVIEW_OBJECT = "review_object";
    public static final String WRITE_DETAIL_REVIEW_PHOTO = "review_photo";
    public static final String WRITE_DETAIL_REVIEW_CONTENT = "review_content";
    public static final String WRITE_DETAIL_REVIEW_SCORE = "review_score";
    public static final String INTENT_KEY_IS_ID = "is_id";
    private static final int ACTIVITY_REQUEST_CODE_IMAGE_PICK = 0xBC01;
    private static final int REQUEST_GO_CART = 50222;
    private static final int NUMBER_COLUMNS = 3;

    private RelativeLayout buttonBack;
    private AppCompatImageButton buttonMessage, buttonCart;
    private AppCompatTextView textViewMessageBadge, textViewCartBadge, orderNo, date, deliveryStatus1, deliveryStatus2,
            name, option, price, ratingNum, buttonRegistration, strContent, buttonDelete, buttonCancel, shopName;
    private AppCompatImageView image;
    private RatingBar rating;
    private AppCompatEditText editReview;
    private RecyclerView recycler;
    private ScrollView scroll;
    private ReviewDetailAdapter reviewDetailAdapter;
    private GridLayoutManager gridLayoutManager;
    private int gridviewItemSize = 0;

    private String productTitle, productOption, deliveryCompany, itemKey, orderNumber, productImage, productStatus, ct_id, it_id, reviewResult, is_id, is_type, is_score, is_subject, is_content, storeName;
    private String reviewContent, reviewScore, reviewPhoto;
    private double productPrice;
    //    private ArrayList<Uri> images;
    private ArrayList<ReviewDetailImageModel> modelArray;
    private KeyboardHeightProvider mKeyboardHeightProvider;

    private String initContentValue;
    private String initScoreValue;
    private String initPhotoValue;
    private String strDate;
    private boolean isScoreChanged = false;
    private boolean isContentChanged = false;
    private boolean isPhotoChanged = false;
    private AppAlertDialog dialog;
    private View emptyTouch;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_detail_review);

        mKeyboardHeightProvider = new KeyboardHeightProvider(this);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mKeyboardHeightProvider.start();
            }
        }, 500);

//        setKeyboardVisibilityListener(this);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        Logger.e("screenWidth :: " + screenWidth);
        gridviewItemSize = (int) ((screenWidth - Utils.ConvertDpToPx(WriteDetailReviewActivity.this, 20)) / 3);
        Logger.e("gridviewItemSize :: " + gridviewItemSize);
        Logger.e("etc  :: " + Utils.ConvertDpToPx(WriteDetailReviewActivity.this, 81));
        int editHeight = screenWidth - Utils.ConvertDpToPx(WriteDetailReviewActivity.this, 81) - gridviewItemSize;
        Logger.e("editHeight :: " + editHeight);
        shopName = findViewById(R.id.shopName);
        buttonCancel = findViewById(R.id.buttonCancel);
        scroll = findViewById(R.id.scroll);
        buttonBack = findViewById(R.id.buttonBack);
        buttonMessage = findViewById(R.id.buttonMessage);
        buttonCart = findViewById(R.id.buttonCart);
        textViewMessageBadge = findViewById(R.id.textViewMessageBadge);
        textViewCartBadge = findViewById(R.id.textViewCartBadge);
        orderNo = findViewById(R.id.orderNo);
        date = findViewById(R.id.date);
        deliveryStatus1 = findViewById(R.id.deliveryStatus1);
        deliveryStatus2 = findViewById(R.id.deliveryStatus2);
        name = findViewById(R.id.name);
        option = findViewById(R.id.option);
        price = findViewById(R.id.price);
        ratingNum = findViewById(R.id.ratingNum);
        image = findViewById(R.id.image);
        rating = findViewById(R.id.rating);
        emptyTouch = findViewById(R.id.emptyTouch);
        emptyTouch.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    SoftKeyboardUtils.Companion.hideKeyboard(editReview);
                }

                return false;
            }
        });

        editReview = findViewById(R.id.editReview);
        strContent = findViewById(R.id.strContent);
        strContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                editReview.callOnClick();
                editReview.setVisibility(View.VISIBLE);
                if (!TextUtils.isEmpty(strContent.getText().toString())) {
                    editReview.setText(strContent.getText().toString());
                    editReview.setSelection(editReview.length());
                }

                editReview.requestFocus();
                editReview.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                        inputMethodManager.showSoftInput(editReview, 0);
                    }
                }, 100);
            }
        });
        price = findViewById(R.id.price);
        recycler = findViewById(R.id.recycler);
        buttonRegistration = findViewById(R.id.buttonRegistration);
        buttonDelete = findViewById(R.id.buttonDelete);
        buttonDelete.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                dialog = new AppAlertDialog(WriteDetailReviewActivity.this);
                dialog.setTitle("리뷰 상세");
                dialog.setMessage("리뷰를 삭제하시겠습니까?");
                dialog.setLeftButton(getString(R.string.msg_my_follow_cancel), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.setRightButton(getString(R.string.msg_my_qna_del), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        NetworkBus bus = new NetworkBus(NetworkApi.API66.name(), is_id);
                        EventBus.getDefault().post(bus);
                    }
                });
                dialog.show();
            }
        });

        ViewGroup.LayoutParams param = strContent.getLayoutParams();
        param.height = editHeight;
        strContent.setLayoutParams(param);
        editReview.setLayoutParams(param);

        // 입력 글자수 500자가 넘어갈 경우 toast 로 사용자에게 noti 하기 위해
        editReview.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                strContent.setText(editReview.getText().toString());
                if (s.length() >= 500) {
                    Toast.makeText(WriteDetailReviewActivity.this, "꼼꼼 리뷰는 500자 이내로 작성이 가능합니다.", Toast.LENGTH_SHORT).show();
                }
                if ( editReview.getText().toString().equals(initContentValue) ) {
                    isContentChanged = false;
                } else {
                    isContentChanged = true;
                }
                changeButtonStatus();
            }
        });

        buttonBack.setOnClickListener(clickListener);
        buttonMessage.setOnClickListener(clickListener);
        buttonCart.setOnClickListener(clickListener);
        buttonRegistration.setOnClickListener(clickListener);
        buttonCancel.setOnClickListener(clickListener);
        gridLayoutManager = new GridLayoutManager(WriteDetailReviewActivity.this, NUMBER_COLUMNS);
        recycler.setLayoutManager(gridLayoutManager);
        reviewDetailAdapter = new ReviewDetailAdapter(WriteDetailReviewActivity.this, gridviewItemSize);
        recycler.setAdapter(reviewDetailAdapter);

        buttonRegistration.setEnabled(false);
        buttonRegistration.setBackgroundResource(R.drawable.disable_registration_bg);
        buttonRegistration.setTextColor(0xFFbcc6d2);
        reviewDetailAdapter.setOnItemClickListener(new ReviewDetailAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, ReviewDetailImageModel data) {
                if (data.isHeader()) {
                    Intent intent = new Intent(WriteDetailReviewActivity.this, CoverMultiSelectActivity.class);
                    Logger.e("count :: " + reviewDetailAdapter.getItemCount());
                    if (reviewDetailAdapter.getItemCount() < 7) {
                        // 추가로 이미지 등록할 수 있는 갯수를 계산하기 위해 현재 등록되어 있는 이미지 갯수를 넘긴다.
                        intent.putExtra("selected_image_count", reviewDetailAdapter.getItemCount() - 1);
                        startActivityForResult(intent,
                                ACTIVITY_REQUEST_CODE_IMAGE_PICK);
                    } else {
                        Toast.makeText(WriteDetailReviewActivity.this, "이미지는 최대 6개까지 등록하실 수 있습니다. 다른 이미지를 등록하시려면 등록되어 있는 이미지를 삭제 후 진행해주시기 바랍니다", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    reviewDetailAdapter.removeItem(position);
                    JSONArray array = new JSONArray();
                    for ( int i = 0 ; i < reviewDetailAdapter.getItemCount() -1 ; i ++ ) {
                        array.put(reviewDetailAdapter.getItems().get(i).getImagePath());
                    }
                    if ( initPhotoValue != null ) {
                        if ( initPhotoValue.equals(array.toString()) ) {
                            isPhotoChanged = false;
                        } else {
                            isPhotoChanged = true;
                        }
                    } else {
                        if ( array.length() > 0 ) {
                            isPhotoChanged  = true;
                        } else {
                            isPhotoChanged = false;
                        }
                    }
                    changeButtonStatus();
                }
            }
        });

        rating.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            String str = "" + rating;
            str = str.replaceAll(".0", "");
            ratingNum.setText(str);

            if ( initScoreValue != null ) {
                if ( initScoreValue.equals(str) ) {
                    isScoreChanged = false;
                } else {
                    isScoreChanged = true;
                }
            }
            changeButtonStatus();
        });

        EventBus.getDefault().register(WriteDetailReviewActivity.this);
        NetworkBus bus = new NetworkBus(NetworkApi.API21.name(), AppPreferences.Companion.getUserId(WriteDetailReviewActivity.this));
        EventBus.getDefault().post(bus);

        checkIntent(getIntent());

        scroll.smoothScrollTo(0, 0);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(WriteDetailReviewActivity.this);
        if (mKeyboardHeightProvider != null) {
            mKeyboardHeightProvider.close();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(SoftKeyboardBus bus) {
        Logger.e("SoftKeyboardBus height: " + bus.height);

        int[] location = new int[2];

        editReview.getLocationOnScreen(location);
        int y = location[1];
        Logger.e("y :: " + y);
        if (bus.height > 100) {
            int editHeight = Utils.ConvertDpToPx(WriteDetailReviewActivity.this, 150);
            int calcedScrollValue = calcScroll(y + editHeight, bus.height);
            Logger.e("calcScrollValue :: " + calcedScrollValue);
            editReview.setVisibility(View.VISIBLE);
            strContent.setVisibility(View.GONE);
            emptyTouch.setVisibility(View.VISIBLE);
            scroll.smoothScrollTo(0, calcedScrollValue + scroll.getScrollY()); // 키보드 올라올 때 edittext box 바로 아래 키보드가 위치하도록 스크롤 시킴
        } else {
            editReview.setVisibility(View.GONE);
            strContent.setVisibility(View.VISIBLE);
            emptyTouch.setVisibility(View.GONE);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(NetworkBusResponse data) {
        String key = NetworkHandler.Companion.getInstance(WriteDetailReviewActivity.this)
                .getKey(NetworkApi.API21.toString(), AppPreferences.Companion.getUserId(WriteDetailReviewActivity.this), "");
        String postReviewKey = NetworkHandler.Companion.getInstance(WriteDetailReviewActivity.this).getKey(NetworkApi.API44.toString(), AppPreferences.Companion.getUserId(WriteDetailReviewActivity.this), "");
        String API66 = NetworkHandler.Companion.getInstance(WriteDetailReviewActivity.this)
                .getKey(NetworkApi.API66.toString(), is_id, "");

        if (data.arg1.equals(key)) {
            String str = DBManager.getInstance(WriteDetailReviewActivity.this).get(data.arg1);
            API21 response = new Gson().fromJson(str, API21.class);
            int smsCount = 0;
            try {
                smsCount = Integer.valueOf(response.userSms);
            } catch (Exception e) {
                e.printStackTrace();
            }
            setSmsBadgeCount(smsCount);
        } else if (data.arg1.equals(postReviewKey)) {
            if ("ok" == data.arg2) {
                new AppToast(this).showToastMessage("꼼꼼리뷰 작성이 완료 되었습니다.",
                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                        AppToast.GRAVITY_BOTTOM);

                setResult(Activity.RESULT_OK);
                finish();
            } else {
                Logger.e("error :: " + data.arg2);
                Toast.makeText(WriteDetailReviewActivity.this, "test message", Toast.LENGTH_SHORT).show();
            }
        } else if (data.arg1.equals(API66)) {
            if ("ok".equals(data.arg2)) {
                new AppToast(this).showToastMessage("정상적으로 리뷰가 삭제되었습니다.",
                        AppToast.DURATION_MILLISECONDS_DEFAULT, AppToast.GRAVITY_BOTTOM);

                setResult(Activity.RESULT_OK);
                finish();
            } else {
                BaseAPI errorResult = new Gson().fromJson(data.arg4, BaseAPI.class);
                Logger.e("error : $errorResult");

                new AppToast(this).showToastMessage(errorResult.message,
                        AppToast.DURATION_MILLISECONDS_DEFAULT, AppToast.GRAVITY_BOTTOM);
            }
        }
    }

    private void checkIntent(Intent intent) {
        productTitle = intent.getStringExtra(WriteReviewActivity.INTENT_KEY_PRODUCT_NAME);
        storeName = intent.getStringExtra(WriteReviewActivity.INTENT_KEY_STORE_NAME);
        productOption = intent.getStringExtra(WriteReviewActivity.INTENT_KEY_PRODUCT_OPTION);
        productPrice = intent.getDoubleExtra(WriteReviewActivity.INTENT_KEY_PRODUCT_PRICE, 0);
        deliveryCompany = intent.getStringExtra(WriteReviewActivity.INTENT_KEY_DELIVERY_COMPANY);
        itemKey = intent.getStringExtra(WriteReviewActivity.INTENT_KEY_ITEM_KEY);
        orderNumber = intent.getStringExtra(WriteReviewActivity.INTENT_KEY_ORDER_NUMBER);
        productImage = intent.getStringExtra(WriteReviewActivity.INTENT_KEY_PRODUCT_IMG);
        productStatus = intent.getStringExtra(WriteReviewActivity.INTENT_KEY_PRODUCT_STATUS);
        ct_id = intent.getStringExtra(WriteReviewActivity.INTENT_KEY_CT_ID);
        it_id = intent.getStringExtra(WriteReviewActivity.INTENT_KEY_IT_ID);
        reviewResult = intent.getStringExtra(WriteDetailReviewActivity.WRITE_DETAIL_REVIEW_OBJECT);
        is_id = intent.getStringExtra(INTENT_KEY_IS_ID);
        reviewContent = intent.getStringExtra(WRITE_DETAIL_REVIEW_CONTENT);
        reviewScore = intent.getStringExtra(WRITE_DETAIL_REVIEW_SCORE);
        reviewPhoto = intent.getStringExtra(WRITE_DETAIL_REVIEW_PHOTO);

        strDate = intent.getStringExtra(WriteReviewActivity.INTENT_KEY_DATE);
        Logger.e("strDate :: " + strDate);

        String[] t_arr = strDate.split(" ");
        String time = t_arr[0];
        String[] t_arr1 = time.split("-");
        try {
            String year = t_arr1[0];
            String month = t_arr1[1];
            String day = t_arr1[2];
            String formattedTime = year + "년 " + month + "월 " + day + "일";
            date.setText(formattedTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        String time1 = "";
//        try {
//            time1 = "(" + t_arr[1] + ")";
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

//        date.setText(time + time1);
        shopName.setText(storeName);
        orderNo.setText("" +orderNumber);
        initContentValue = reviewContent;
        initScoreValue = reviewScore;
        initPhotoValue = reviewPhoto;

        Logger.e("reviewResult :: " + reviewResult);
        Logger.e("reviewContent :: " + reviewContent);
        Logger.e("reviewScore :: " + reviewScore);
        Logger.e("reviewPhoto :: " + reviewPhoto);
        int score = 1;
        try {
            score = Integer.valueOf(reviewScore);
        } catch (Exception e) {
            e.printStackTrace();
        }
        strContent.setText(reviewContent);
        rating.setRating(score);
        ratingNum.setText("" + score);
        name.setText(productTitle);
        option.setText(productOption);
        deliveryStatus1.setText(productStatus);
        price.setText(String.format(getString(R.string.msg_price_format),
                PriceFormatter.Companion.getInstance().getFormattedValue(productPrice)));

        ImageLoad.setImage(this, image, productImage, null, ImageLoad.SCALE_NONE, DiskCacheStrategy.ALL);

        if (!TextUtils.isEmpty(reviewPhoto)) {
            modelArray = new ArrayList<ReviewDetailImageModel>();
            try {
                JSONArray array = new JSONArray(reviewPhoto);
                if (array != null && array.length() > 0) {
                    for (int i = 0; i < array.length(); i++) {
                        if (!TextUtils.isEmpty(array.get(i).toString())) {
                            Uri uri = Uri.parse(array.get(i).toString());
                            ReviewDetailImageModel model = new ReviewDetailImageModel();
                            Logger.e("uri :: " + uri.toString());
                            model.setHeader(false);
                            model.setImagePath(uri);
                            model.setFromServer(true);
                            modelArray.add(model);
                        }
                    }
                    if (modelArray != null && modelArray.size() > 0) {
                        if (reviewDetailAdapter != null)
                            reviewDetailAdapter.addItems(modelArray);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (!TextUtils.isEmpty(reviewResult)) {
            try {
                JSONObject object = new JSONObject(reviewResult);
                if (object != null) {
                    is_id = object.optString("is_id");
                    is_type = object.optString("is_type");
                    is_score = object.optString("is_score");
                    is_subject = object.optString("is_subject");
                    is_content = object.optString("is_content");
                    initContentValue = is_content;
                    initScoreValue = is_score;
                    if (!TextUtils.isEmpty(is_score)) {
                        try {
                            float rate = Float.parseFloat(is_score);
                            rating.setRating(rate);
                            ratingNum.setText("" + rate);
                            rating.setClickable(false);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    if (!TextUtils.isEmpty(is_content)) {
                        editReview.setText(is_content);
                        strContent.setText(is_content);
                    }

                    modelArray = new ArrayList<ReviewDetailImageModel>();
                    JSONArray array = object.optJSONArray("is_photo");
                    initPhotoValue = array.toString();
                    if (array != null && array.length() > 0) {
                        for (int i = 0; i < array.length(); i++) {
                            if (!TextUtils.isEmpty(array.get(i).toString())) {
                                Uri uri = Uri.parse(array.get(i).toString());
                                ReviewDetailImageModel model = new ReviewDetailImageModel();

                                model.setHeader(false);
                                model.setImagePath(uri);
                                model.setFromServer(true);
                                modelArray.add(model);
                            }
                        }
                        if (modelArray != null && modelArray.size() > 0) {
                            if (reviewDetailAdapter != null)
                                reviewDetailAdapter.addItems(modelArray);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.buttonBack:
                    finish();
                    break;
                case R.id.buttonCancel:
                    finish();
                    break;
                case R.id.buttonMessage:
                    startActivity(new Intent(WriteDetailReviewActivity.this, MessageActivity.class));
                    break;
                case R.id.buttonCart:
                    Intent intent = new Intent(WriteDetailReviewActivity.this, ProductCartActivity.class);
                    startActivityForResult(intent, REQUEST_GO_CART);
                    break;
                case R.id.buttonRegistration:
                    String reviewStr = strContent.getText().toString();
                    if (reviewStr.length() <= 0) {
                        Toast.makeText(WriteDetailReviewActivity.this, "내용을 작성해주세요", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    MultipartBody.Builder builder = new MultipartBody.Builder();
                    builder.setType(MultipartBody.FORM);
                    builder.addFormDataPart("userId", AppPreferences.Companion.getUserId(WriteDetailReviewActivity.this));
                    builder.addFormDataPart("it_id", it_id);
                    builder.addFormDataPart("ct_id", ct_id);
                    builder.addFormDataPart("content", strContent.getText().toString());
                    builder.addFormDataPart("type", "2");
                    builder.addFormDataPart("score", ratingNum.getText().toString());

                    ArrayList<ReviewDetailImageModel> tempModels = reviewDetailAdapter.getItems();
                    String strPhoto = "";
                    if (tempModels != null && tempModels.size() > 0) {
                        StringBuilder sb = new StringBuilder();
                        int position = 0;
                        for (int i = 0; i < tempModels.size(); i++) {
                            if (tempModels.get(i).isFromServer()) {
                                sb.append(tempModels.get(i).getImagePath());
                                sb.append("^|^");
                            } else {
                                String image = tempModels.get(i).getImagePath().getPath();
                                File file = new File(image);
                                try {
                                    String encodeFileName = URLEncoder.encode(file.getName(), "UTF-8");
                                    if (file != null) {
                                        builder.addFormDataPart("thumb[" + position + "]", encodeFileName, RequestBody.create(MediaType.parse("multipart/form-data"), file));
                                        position++;
                                    }
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }

                            }
                        }
                        strPhoto = sb.toString();
                        if (!TextUtils.isEmpty(strPhoto))
                            strPhoto = strPhoto.substring(0, strPhoto.length() - 3);
                    }
                    builder.addFormDataPart("photo", strPhoto);
                    RequestBody body = builder.build();
                    NetworkBus bus = new NetworkBus(NetworkApi.API44.name(), body);
                    EventBus.getDefault().post(bus);
                    break;
            }
        }
    };

    private void setSmsBadgeCount(int count) {
        textViewMessageBadge.setText("" + count);
        if (count > 0) {
//            textViewMessageBadge.setVisibility(View.VISIBLE);
        } else {
            textViewMessageBadge.setVisibility(View.GONE);
        }
    }

    /**
     * 장바구니 Badge 에 설정할 Count 를 지정 (0 개인 경우 숨김처리)
     *
     * @param count
     */
    private void setCartBadgeCount(int count) {
        textViewCartBadge.setText("" + count);
        if (count > 0) {
//            textViewCartBadge.setVisibility(View.VISIBLE);
        } else {
            textViewCartBadge.setVisibility(View.GONE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_GO_CART && resultCode == RESULT_OK) {
            int cartCnt = data.getIntExtra("CART_CNT", 0);
            setCartBadgeCount(cartCnt);
        } else if (requestCode == ACTIVITY_REQUEST_CODE_IMAGE_PICK && resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            ArrayList<Uri> uris = bundle.getParcelableArrayList("URI_ARRAY");
            for (int i = 0; i < uris.size(); i++) {
                Logger.e("addedImage ;: " + uris.get(i).toString());
            }
            if (reviewDetailAdapter != null && uris != null && uris.size() > 0) {
                ArrayList<ReviewDetailImageModel> array = new ArrayList<ReviewDetailImageModel>();
                for (int i = 0; i < uris.size(); i++) {
                    ReviewDetailImageModel model = new ReviewDetailImageModel();
                    model.setHeader(false);
                    model.setImagePath(uris.get(i));
                    model.setFromServer(false);
                    array.add(model);
                }
                reviewDetailAdapter.addItems(array);
            }

            JSONArray array = new JSONArray();
            for ( int i = 0 ; i < reviewDetailAdapter.getItemCount() - 1 ; i ++ ) {
                array.put(reviewDetailAdapter.getItems().get(i).getImagePath());
            }
            if ( initPhotoValue != null ) {
                if ( initPhotoValue.equals(array.toString()) ) {
                    isPhotoChanged = false;
                } else {
                    isPhotoChanged = true;
                }
            } else {
                if ( array.length() > 0 ) {
                    isPhotoChanged  = true;
                } else {
                    isPhotoChanged = false;
                }
            }
            changeButtonStatus();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private int calcScroll(int editBottom, int keyboardHeight) {
        int screenHeight = AppPreferences.Companion.getScreenHeight(WriteDetailReviewActivity.this);
        int keyboardTopPosition = screenHeight - keyboardHeight;
        if ( editBottom > keyboardTopPosition ) {
            return editBottom - keyboardTopPosition;
        } else
            return 0;
    }

    private void changeButtonStatus() {
        Logger.e("isScoreChanged :: " + isScoreChanged + " isContentChanged :: " + isContentChanged + "isPhotoChanged :: " + isPhotoChanged );
        if ( isScoreChanged || isContentChanged || isPhotoChanged ) {
            Logger.e("버튼 변경되어야 함");
            buttonCancel.setVisibility(View.VISIBLE);
            buttonDelete.setVisibility(View.GONE);
            buttonRegistration.setEnabled(true);
            buttonRegistration.setBackgroundResource(R.drawable.bg_follow);
            buttonRegistration.setTextColor(0xFFffffff);
        } else {
            Logger.e("버튼 초기화 함");
            buttonCancel.setVisibility(View.GONE);
            buttonDelete.setVisibility(View.VISIBLE);
            buttonRegistration.setEnabled(false);
            buttonRegistration.setBackgroundResource(R.drawable.disable_registration_bg);
            buttonRegistration.setTextColor(0xFFbcc6d2);
        }
    }

    @Override
    public void onNetworkStatusChanged(@NotNull NetworkStatusUtils.NetworkStatus status) {

    }

    @Override
    public void onDozeModeStateChanged(boolean dozeEnable) {

    }
}
