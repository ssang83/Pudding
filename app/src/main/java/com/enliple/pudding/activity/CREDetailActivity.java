package com.enliple.pudding.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.enliple.pudding.AbsBaseActivity;
import com.enliple.pudding.R;
import com.enliple.pudding.commons.app.ImageLoad;
import com.enliple.pudding.commons.app.NetworkStatusUtils;
import com.enliple.pudding.commons.app.PriceFormatter;
import com.enliple.pudding.commons.log.Logger;
import com.enliple.pudding.commons.shoptree.network.ShopTreeAsyncTask;
import com.enliple.pudding.commons.shoptree.response.CREDetailResponse;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Kim Joonsung on 2018-09-21.
 */

public class CREDetailActivity extends AbsBaseActivity implements View.OnClickListener {
    public final static String INTENT_KEY_ITEM_KEY = "CREDetailActivity.INTENT_KEY_ITEM_KEY";

    private RelativeLayout buttonBack;
    private AppCompatImageView imageViewThumbnail;
    private AppCompatTextView textViewRequestDate;                  // 취소/환불/교환/ 날짜
    private AppCompatTextView textViewDate;                         // 날짜
    private AppCompatTextView textViewOrderNumber;                  // 주문번호
    private AppCompatTextView textViewStatus;                       // 처리 상태
    private AppCompatImageView imageViewProductType;
    private AppCompatTextView textViewShopName;
    private AppCompatTextView textViewTitle;
    private AppCompatTextView textViewOption;                                // 옵션
    private AppCompatTextView textViewTotalPaymentPrice;                     // 취소상품/환불상품 총 금액
    private AppCompatTextView textViewProductPrice;                          // 상품금액
    private AppCompatTextView textViewDeliveryPrice;                         // 배송비
    private AppCompatTextView textViewAvailablePrice;                        // 적립금
    private AppCompatTextView textViewFlow;
    private View layoutForPriceInformation;
    private View textViewPayCancelGuide;
    private ProgressBar progressBar;

    String itemKey;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cre_detail);
        setLayout();

        buttonBack.setOnClickListener(this);

        Intent intent = getIntent();
        itemKey = intent.getStringExtra(INTENT_KEY_ITEM_KEY);

        loadData();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onNetworkStatusChanged(@NotNull NetworkStatusUtils.NetworkStatus status) {

    }

    @Override
    public void onDozeModeStateChanged(boolean dozeEnable) {

    }

    @Override
    public void onClick(View v) {
        long viewId = v.getId();

        if (viewId == R.id.buttonBack) {
            onBackPressed();
        }
    }

    private void loadData() {
        ShopTreeAsyncTask task = new ShopTreeAsyncTask(this);
        task.getCREDetail(itemKey,
                (result, obj) -> {
                    Logger.d("" + obj.toString());
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
                        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
                        final CREDetailResponse responseObj = mapper.readValue(obj.toString(),
                                CREDetailResponse.class);

                        if (responseObj != null) {
                            ImageLoad.setImage(this, imageViewThumbnail, responseObj.image, null, ImageLoad.SCALE_NONE, DiskCacheStrategy.ALL);

                            String[] array = responseObj.rdate.split(" ");
                            String date = array[0];
                            String time = array[1];
                            String[] aDate = date.split("-");
                            String[] aTime = time.split(":");
                            String fTime = aDate[0] + "년 " + aDate[1] + "월 " + aDate[2] + "일" + "(" + aTime[0] + ":" + aTime[1] + ")";
                            textViewDate.setText(fTime);

                            if(!TextUtils.isEmpty(responseObj.response_date)) {
                                String[] dateArray = responseObj.response_date.split(" ");
                                String responseDate = dateArray[0];
                                String responseTiem = dateArray[1];
                                String mDate = responseDate.replace("-", ".");
                                String[] mTime = responseTiem.split(":");
                                String rDate = mDate + "(" + mTime[0] + ":" + mTime[1] + ")";
                                textViewRequestDate.setText(rDate);
                            } else {
                                String[] dateArray = responseObj.request_date.split(" ");
                                String requestDate = dateArray[0];
                                String requestTime = dateArray[1];
                                String mDate = requestDate.replace("-", ".");
                                String[] mTime = requestTime.split(":");
                                String rDate = mDate + "(" + mTime[0] + ":" + mTime[1] + ")";
                                textViewRequestDate.setText(rDate);
                            }

                            textViewStatus.setText(responseObj.status);
                            textViewFlow.setText(String.format(getString(R.string.msg_request_format), responseObj.flow));
                            textViewShopName.setText("CHANEL");
                            textViewTitle.setText(responseObj.product_name);
                            textViewOrderNumber.setText("12345567678");
                            imageViewProductType.setBackgroundResource(R.drawable.item_shop_ic);
                            textViewOption.setText(responseObj.option);

                            textViewTotalPaymentPrice.setText(String.format(getString(R.string.msg_price_format),
                                    PriceFormatter.Companion.getInstance().getFormattedValue(responseObj.product_price)));
                            textViewProductPrice.setText(String.format(getString(R.string.msg_price_format),
                                    PriceFormatter.Companion.getInstance().getFormattedValue(responseObj.product_price)));
                            textViewDeliveryPrice.setText(String.format(getString(R.string.msg_price_format),
                                    PriceFormatter.Companion.getInstance().getFormattedValue(responseObj.delivery_price)));
                            textViewAvailablePrice.setText(String.format(getString(R.string.msg_price_format),
                                    PriceFormatter.Companion.getInstance().getFormattedValue(responseObj.point)));

                            if (responseObj.flow.equals("반품") ||
                                    responseObj.flow.equals("취소") ||
                                    responseObj.flow.equals("환불")) {
                                layoutForPriceInformation.setVisibility(View.VISIBLE);
                                textViewPayCancelGuide.setVisibility(View.VISIBLE);
                            } else {
                                layoutForPriceInformation.setVisibility(View.GONE);
                                textViewPayCancelGuide.setVisibility(View.GONE);
                            }
                        }
                    } catch (Exception e) {
                        progressBar.setVisibility(View.GONE);
                    } finally {
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void setLayout() {
        buttonBack = findViewById(R.id.buttonBack);
        imageViewThumbnail = findViewById(R.id.imageViewThumbnail);
        textViewRequestDate = findViewById(R.id.textViewRequestDate);
        textViewShopName = findViewById(R.id.textViewShopName);
        textViewTitle = findViewById(R.id.textViewTitle);
        textViewTotalPaymentPrice = findViewById(R.id.textViewTotalPaymentPrice);
        textViewProductPrice = findViewById(R.id.textViewProductPrice);
        textViewDeliveryPrice = findViewById(R.id.textViewDeliveryPrice);
        textViewAvailablePrice = findViewById(R.id.textViewAvailablePrice);
        textViewOption = findViewById(R.id.textViewOption);
        textViewPayCancelGuide = findViewById(R.id.textViewPayCancelGuide);
        layoutForPriceInformation = findViewById(R.id.layoutForPriceInformation);
        progressBar = findViewById(R.id.progressBar);
        textViewFlow = findViewById(R.id.textViewFlow);
        textViewDate = findViewById(R.id.textViewDate);
        textViewStatus = findViewById(R.id.textViewStatus);
        textViewOrderNumber = findViewById(R.id.textViewOrderNumber);
        imageViewProductType = findViewById(R.id.imageViewProductType);
    }
}
