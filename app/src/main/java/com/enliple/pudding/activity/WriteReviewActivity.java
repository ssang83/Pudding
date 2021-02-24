package com.enliple.pudding.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.RatingBar;
import android.widget.RelativeLayout;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.enliple.pudding.AbsBaseActivity;
import com.enliple.pudding.R;
import com.enliple.pudding.bus.SoftKeyboardBus;
import com.enliple.pudding.commons.log.Logger;
import com.enliple.pudding.commons.app.ImageLoad;
import com.enliple.pudding.commons.app.NetworkStatusUtils;
import com.enliple.pudding.commons.app.PriceFormatter;
import com.enliple.pudding.commons.app.Utils;
import com.enliple.pudding.commons.db.DBManager;
import com.enliple.pudding.commons.internal.AppPreferences;
import com.enliple.pudding.commons.network.NetworkApi;
import com.enliple.pudding.commons.network.NetworkBus;
import com.enliple.pudding.commons.network.NetworkBusResponse;
import com.enliple.pudding.commons.network.NetworkHandler;
import com.enliple.pudding.commons.network.vo.API21;
import com.enliple.pudding.commons.shoptree.network.ShopTreeAsyncTask;
import com.enliple.pudding.commons.shoptree.response.BaseResponse;
import com.enliple.pudding.commons.widget.toast.AppToast;
import com.enliple.pudding.keyboard.KeyboardHeightProvider;
import com.enliple.pudding.widget.SingleButtonDialog;
import com.enliple.pudding.widget.shoptree.PurchaseConfirmDialog;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.joooonho.SelectableRoundedImageView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class WriteReviewActivity extends AbsBaseActivity {
    private static final int REQUEST_GO_CART = 50222;

    public final static String INTENT_KEY_PRODUCT_NAME = "WriteReviewActivity.INTENT_KEY_PRODUCT_NAME";
    public final static String INTENT_KEY_STORE_NAME = "WriteReviewActivity.INTENT_KEY_STORE_NAME";
    public final static String INTENT_KEY_PRODUCT_OPTION = "WriteReviewActivity.INTENT_KEY_PRODUCT_OPTION";
    public final static String INTENT_KEY_PRODUCT_PRICE = "WriteReviewActivity.INTENT_KEY_PRODUCT_PRICE";
    public final static String INTENT_KEY_PRODUCT_KEY = "WriteReviewActivity.INTENT_KEY_PRODUCT_KEY";
    public final static String INTENT_KEY_DELIVERY_COMPANY = "WriteReviewActivity.INTENT_KEY_DELIVERY_COMPANY";
    public final static String INTENT_KEY_ST_ORDER_NUMBER = "WriteReviewActivity.INTENT_KEY_ST_ORDER_NUMBER";
    public final static String INTENT_KEY_ITEM_KEY = "WriteReviewActivity.INTENT_KEY_ITEM_KEY";
    public final static String INTENT_KEY_ORDER_NUMBER = "WriteReviewActivity.INTENT_KEY_ORDER_NUMBER";
    public final static String INTENT_KEY_PRODUCT_IMG = "WriteReviewActivity.INTENT_KEY_PRODUCT_IMG";
    public final static String INTENT_KEY_PRODUCT_STATUS = "WriteReviewActivity.INTENT_KEY_STATUS";
    public final static String INTENT_KEY_TRACKING_NAME = "WriteReviewActivity.INTENT_KEY_TRACKING_NAME";
    public final static String INTENT_KEY_TRACKING_CODE = "WriteReviewActivity.INTENT_KEY_TRACKING_CODE";
    public final static String INTENT_KEY_TRACKING_URL = "WriteReviewActivity.INTENT_KEY_TRACKING_URL";
    public final static String INTENT_KEY_CT_ID = "WriteReviewActivity.INTENT_KEY_CT_ID";
    public final static String INTENT_KEY_IT_ID = "WriteReviewActivity.INTENT_KEY_IT_ID";
    public final static String INTENT_KEY_DATE = "WriteReviewActivity.INTENT_KEY_DATE";

    private RelativeLayout buttonBack;
    private AppCompatImageButton buttonMessage, buttonCart;
    private AppCompatTextView textViewMessageBadge, textViewCartBadge, orderNo, date, deliveryStatus1, deliveryStatus2,
            name, option, price, ratingNum, purchaseConfirmDate, buttonCancel, buttonPurchaseConfirm, textViewDeliveryCheck, shopName;
    private AppCompatImageView image;
//    private RelativeLayout buttonDeliverySearch;
    private RatingBar rating;
    private AppCompatEditText editReview;
    private int dialogWidth = 0;
    private String productTitle, productOption, deliveryCompany, itemKey, stOrderNo, orderNumber, productImage, productStatus, ct_id, it_id, trackingName, trackingCode, trackingUrl, strDate, storeName;
    private double productPrice;

    private KeyboardHeightProvider mKeyboardHeightProvider;
    private View emptyTouch;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                MultipartBody.Builder builder = new MultipartBody.Builder();
                builder.setType(MultipartBody.FORM);
                Logger.e(":it_id :: " + it_id);
                Logger.e(":score :: " + ratingNum.getText().toString());
                Logger.e(":content :: " + editReview.getText().toString());
                builder.addFormDataPart("userId", AppPreferences.Companion.getUserId(WriteReviewActivity.this));
                builder.addFormDataPart("it_id", it_id);
                builder.addFormDataPart("ct_id", ct_id);
                builder.addFormDataPart("content", editReview.getText().toString());
                builder.addFormDataPart("type", "1");
                builder.addFormDataPart("score", ratingNum.getText().toString());

                RequestBody body = builder.build();
                NetworkBus bus = new NetworkBus(NetworkApi.API44.name(), body);
                EventBus.getDefault().post(bus);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_review);

        mKeyboardHeightProvider = new KeyboardHeightProvider(this);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mKeyboardHeightProvider.start();
            }
        }, 500);


        emptyTouch = findViewById(R.id.emptyTouch);
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
        shopName = findViewById(R.id.shopName);
        option = findViewById(R.id.option);
        price = findViewById(R.id.price);
        ratingNum = findViewById(R.id.ratingNum);
        purchaseConfirmDate = findViewById(R.id.purchaseConfirmDate);
        buttonCancel = findViewById(R.id.buttonCancel);
        buttonPurchaseConfirm = findViewById(R.id.buttonPurchaseConfirm);
        image = findViewById(R.id.image);
//        buttonDeliverySearch = findViewById(R.id.buttonDeliverySearch);
        rating = findViewById(R.id.rating);
        editReview = findViewById(R.id.editReview);
        price = findViewById(R.id.price);
        textViewDeliveryCheck = findViewById(R.id.textViewDeliveryCheck);

        buttonBack.setOnClickListener(clickListener);
        buttonMessage.setOnClickListener(clickListener);
        buttonCart.setOnClickListener(clickListener);
        buttonCancel.setOnClickListener(clickListener);
        buttonPurchaseConfirm.setOnClickListener(clickListener);
        textViewDeliveryCheck.setOnClickListener(clickListener);

        rating.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            if (rating < 1.0f) {
                ratingBar.setRating(1.0f);
                rating = ratingBar.getRating();
            }
            String str = "" + rating;
            str = str.replaceAll(".0", "");
            ratingNum.setText(str);
        });

        emptyTouch.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Logger.e("emptyTouch onTouch event.getAction() :: " + event.getAction());
                if ( event.getAction() == MotionEvent.ACTION_DOWN ) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(editReview.getWindowToken(), 0);
                }
                return false;
            }
        });

        EventBus.getDefault().register(WriteReviewActivity.this);
        NetworkBus bus = new NetworkBus(NetworkApi.API21.name(), AppPreferences.Companion.getUserId(WriteReviewActivity.this));
        EventBus.getDefault().post(bus);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        dialogWidth = screenWidth - Utils.ConvertDpToPx(WriteReviewActivity.this, 40);

        checkIntent(getIntent());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(WriteReviewActivity.this);
        if (mKeyboardHeightProvider != null) {
            mKeyboardHeightProvider.close();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(SoftKeyboardBus bus) {
        Logger.e("SoftKeyboardBus height: " + bus.height);
        if (bus.height > 100) {
            emptyTouch.setVisibility(View.VISIBLE);
        } else {
            emptyTouch.setVisibility(View.GONE);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(NetworkBusResponse data) {
        String key = NetworkHandler.Companion.getInstance(WriteReviewActivity.this)
                .getKey(NetworkApi.API21.toString(), AppPreferences.Companion.getUserId(WriteReviewActivity.this), "");
        String postReviewKey = NetworkHandler.Companion.getInstance(WriteReviewActivity.this).getKey(NetworkApi.API44.toString(), AppPreferences.Companion.getUserId(WriteReviewActivity.this), "");
        Logger.e("postReviewKey :: " + postReviewKey);
        Logger.e("data.arg1 :: " + data.arg1);
        Logger.e("data.arg2 :: " + data.arg2);
        if (data.arg1.equals(key)) {
            String str = DBManager.getInstance(WriteReviewActivity.this).get(data.arg1);
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
                showPurchaseConfirmDialog(getResources().getString(R.string.msg_purchase_confirm_dialog_content));
            } else {
                Logger.e("error :: " + data.arg2);
                showPurchaseConfirmDialog(getResources().getString(R.string.msg_purchase_confirm_dialog_content_1));
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
                case R.id.buttonMessage:
                    startActivity(new Intent(WriteReviewActivity.this, MessageActivity.class));
                    break;
                case R.id.buttonCart:
                    Intent intent = new Intent(WriteReviewActivity.this, ProductCartActivity.class);
                    startActivityForResult(intent, REQUEST_GO_CART);
                    break;
//                case R.id.buttonDeliverySearch:
//                    break;
                case R.id.buttonCancel:
                    break;
                case R.id.buttonPurchaseConfirm:

                    new PurchaseConfirmDialog(WriteReviewActivity.this, dialogWidth, new PurchaseConfirmDialog.PurchaseConfirmDialogListener() {
                        @Override
                        public void onConfirm(Context context) {
                            Logger.e("onConfirm");
                            // TODO:구매확정 API
                            confirmRequest();
                        }
                    }).show();
                    break;

                case R.id.textViewDeliveryCheck:
                    Intent intent1 = new Intent(WriteReviewActivity.this, DeliveryCheckActivity.class);
                    intent1.putExtra(DeliveryCheckActivity.INTENT_EXTRA_KEY_URL, trackingUrl);
                    startActivity(intent1);
                    break;
            }
        }
    };

    private void checkIntent(Intent intent) {
        if (intent != null) {
            productTitle = intent.getStringExtra(INTENT_KEY_PRODUCT_NAME);
            storeName = intent.getStringExtra(INTENT_KEY_STORE_NAME);
            productOption = intent.getStringExtra(INTENT_KEY_PRODUCT_OPTION);
            productPrice = intent.getDoubleExtra(INTENT_KEY_PRODUCT_PRICE, 0);
            deliveryCompany = intent.getStringExtra(INTENT_KEY_DELIVERY_COMPANY);
            itemKey = intent.getStringExtra(INTENT_KEY_ITEM_KEY);
            stOrderNo = intent.getStringExtra(INTENT_KEY_ST_ORDER_NUMBER);
            orderNumber = intent.getStringExtra(INTENT_KEY_ORDER_NUMBER);
            productImage = intent.getStringExtra(INTENT_KEY_PRODUCT_IMG);
            productStatus = intent.getStringExtra(INTENT_KEY_PRODUCT_STATUS);
            ct_id = intent.getStringExtra(INTENT_KEY_CT_ID);
            it_id = intent.getStringExtra(INTENT_KEY_IT_ID);
            strDate = intent.getStringExtra(INTENT_KEY_DATE);
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
//            String time1 = "";
//            try {
//                time1 = "(" + t_arr[1] + ")";
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//            date.setText(time + time1);
            name.setText(productTitle);
            shopName.setText(storeName);
            option.setText(productOption);
            deliveryStatus1.setText(productStatus);
            price.setText(String.format(getString(R.string.msg_price_format),
                    PriceFormatter.Companion.getInstance().getFormattedValue(productPrice)));
            orderNo.setText(orderNumber);
            trackingName = intent.getStringExtra(INTENT_KEY_TRACKING_NAME);
            trackingCode = intent.getStringExtra(INTENT_KEY_TRACKING_CODE);
            trackingUrl = intent.getStringExtra(INTENT_KEY_TRACKING_URL);
            if (!TextUtils.isEmpty(trackingName)) {
                StringBuilder sb = new StringBuilder(trackingName);
                sb.append(" ");
                sb.append(trackingCode);

                deliveryStatus2.setText("(" + sb.toString() + ")");
            }
//            Glide.with(this)
//                    .load(productImage)
//                    .asBitmap()
//                    .priority(Priority.HIGH)
//                    .diskCacheStrategy(DiskCacheStrategy.ALL)
//                    .into(image);
            ImageLoad.setImage(this, image, productImage, null, ImageLoad.SCALE_NONE, DiskCacheStrategy.ALL);
        }
    }

    /**
     * 구매확정 다이얼로그. 구매확정 api 에서 success 일 경우 호출
     */
    private void showPurchaseConfirmDialog(String message) {
        new SingleButtonDialog(WriteReviewActivity.this, dialogWidth,
                message,
                getString(R.string.msg_confirm),
                new SingleButtonDialog.SingleButtonDialogListener() {
                    @Override
                    public void onConfirm() {
                        setResult(Activity.RESULT_OK);
                        finish();
                    }
                }).show();
    }

    /**
     * 구매확정
     */
    public void confirmRequest() {
        ShopTreeAsyncTask task = new ShopTreeAsyncTask(this);
        task.purchaseComplete(itemKey,
                (result, obj) -> {
                    Logger.d("confirmRequest " + obj.toString());
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
                        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
                        final BaseResponse responseObj = mapper.readValue(obj.toString(),
                                BaseResponse.class);

                        if (responseObj != null) {
                            if (responseObj.getResult().equals("SUCCESS")) {
                                handler.sendEmptyMessage(0);
                            } else {
                                new AppToast(this).showToastMessage(responseObj.getResultMessage(),
                                        AppToast.DURATION_MILLISECONDS_DEFAULT, AppToast.GRAVITY_BOTTOM);
                            }
                        }
                    } catch (Exception e) {
                        Logger.e(e.toString());
                    }
                });

    }

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
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onNetworkStatusChanged(@NotNull NetworkStatusUtils.NetworkStatus status) {

    }

    @Override
    public void onDozeModeStateChanged(boolean dozeEnable) {

    }
}
