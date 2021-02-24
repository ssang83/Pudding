package com.enliple.pudding.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.enliple.pudding.AbsBaseActivity;
import com.enliple.pudding.R;
import com.enliple.pudding.adapter.my.DeliveryStatusDetailAdapter;
import com.enliple.pudding.commons.app.Utils;
import com.enliple.pudding.commons.log.Logger;
import com.enliple.pudding.commons.app.NetworkStatusUtils;
import com.enliple.pudding.commons.shoptree.data.DeliveryStatusDetailProductData;
import com.enliple.pudding.commons.shoptree.data.DeliveryStatusDetailReview;
import com.enliple.pudding.commons.shoptree.network.ShopTreeAsyncTask;
import com.enliple.pudding.commons.shoptree.response.BaseResponse;
import com.enliple.pudding.commons.shoptree.response.DeliveryStatusDetailResponse;
import com.enliple.pudding.commons.widget.toast.AppToast;
import com.enliple.pudding.widget.shoptree.DeliveryBasicInfoDialog;
import com.enliple.pudding.widget.shoptree.ExchangeRequestDialog;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


/**
 * Created by Kim Joonsung on 2018-09-20.
 */

public class DeliveryStatusDetailActivity extends AbsBaseActivity implements View.OnClickListener, DeliveryStatusDetailAdapter.Listener {

    private static final int REQUEST_CANCEL = 0;
    private static final int REQUEST_REFUND = 1;
    private static final int REQUEST_EXCHANGE = 2;
    private static final int REQUEST_REVIEW = 3;

    public static final String INTENT_EXTRA_KEY_STATUS_TYPE = "status";
    public static final String INTENT_EXTRA_KEY_ORDER_NUMBER = "order_no";
    public static final String INTENT_EXTRA_KEY_SHOPTREE_ORDER_NUMBER = "order_no_stylessum";
    public static final String INTENT_EXTRA_KEY_REG_DATE = "regDate";

    private static final String PAYMENT_SUCCESS = "결제완료";

    private RelativeLayout buttonBack;
    private AppCompatTextView textViewOrderNumber;
    private AppCompatTextView textViewDate;
    private AppCompatTextView textViewReceiveAddress;
    private AppCompatTextView textViewPhoneNumber;
    private AppCompatTextView textViewRecipient;
    private AppCompatTextView pPrice, dPrice, pointPrice, tPrice;
    private ProgressBar progressBar;
    private View viewForAllCancel;
    private View empty;
    private RecyclerView recyclerViewProducts;
    private AppCompatTextView buttonAllCancel;

    private DeliveryStatusDetailAdapter mAdapter;
    private String statusType, orderNumber, stOrderNumber, regDate;
    private String deliveryName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_detail);
        setLayout();

        statusType = getIntent().getStringExtra(INTENT_EXTRA_KEY_STATUS_TYPE);
        orderNumber = getIntent().getStringExtra(INTENT_EXTRA_KEY_ORDER_NUMBER);
        stOrderNumber = getIntent().getStringExtra(INTENT_EXTRA_KEY_SHOPTREE_ORDER_NUMBER);
        regDate = getIntent().getStringExtra(INTENT_EXTRA_KEY_REG_DATE);
        LinearLayoutManager layoutManager = new LinearLayoutManager(DeliveryStatusDetailActivity.this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);

        recyclerViewProducts.setNestedScrollingEnabled(false);
        recyclerViewProducts.setHasFixedSize(true);
        recyclerViewProducts.setLayoutManager(layoutManager);

        buttonBack.setOnClickListener(this);
        buttonAllCancel.setOnClickListener(this);

        loadData();
    }

    @Override
    public void onDozeModeStateChanged(boolean dozeEnable) {

    }

    @Override
    public void onNetworkStatusChanged(@NotNull NetworkStatusUtils.NetworkStatus status) {

    }

    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_OK);
        finish();
    }

    @Override
    public void onClick(View v) {
        long viewId = v.getId();

        if (viewId == R.id.buttonBack) {
            onBackPressed();
        } else if (viewId == R.id.buttonAllCancel) {
            cancelAllRequest();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Logger.e("requestCode :: " + requestCode);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CANCEL || requestCode == REQUEST_REFUND
                    || requestCode == REQUEST_EXCHANGE || requestCode == REQUEST_REVIEW) {
                loadData();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * 배송 정보
     *
     * @param data
     */
    @Override
    public void showBasicInformation(DeliveryStatusDetailProductData data) {
        if (data != null) {
            DeliveryBasicInfoDialog deliveryBasicInfoDialog = new DeliveryBasicInfoDialog(this,
                    deliveryName, data.trackingInfoName, data.trackingInfoCode, data.trackingInfoUrl);
            deliveryBasicInfoDialog.show();
        }
    }

    /**
     * 환불 요청
     *
     * @param data
     */
    @Override
    public void refundRequest(DeliveryStatusDetailProductData data) {
        if (data != null) {
            Intent intent = new Intent(this, CRERequestDetailActivity.class);
            intent.putExtra(CRERequestDetailActivity.INTENT_KEY_REQUEST_TYPE, "refund");
            intent.putExtra(CRERequestDetailActivity.INTENT_KEY_PRODUCT_NAME, data.title);
            intent.putExtra(CRERequestDetailActivity.INTENT_KEY_PRODUCT_OPTION, data.option);
            intent.putExtra(CRERequestDetailActivity.INTENT_KEY_PRODUCT_PRICE, String.valueOf(data.price));
            intent.putExtra(CRERequestDetailActivity.INTENT_KEY_PRODUCT_KEY, data.itemKey);
            intent.putExtra(CRERequestDetailActivity.INTENT_KEY_ST_ORDER_NUMBER, stOrderNumber);
            intent.putExtra(CRERequestDetailActivity.INTENT_KEY_DELIVERY_COMPANY, data.trackingInfoName);

            startActivityForResult(intent, REQUEST_REFUND);
        }
    }

    /**
     * 교환 요청
     *
     * @param data
     */
    @Override
    public void exchangeRequest(DeliveryStatusDetailProductData data) {
        if (data != null) {
            ExchangeRequestDialog dialog = new ExchangeRequestDialog(this, context -> {
                Intent intent = new Intent(context, CRERequestDetailActivity.class);
                intent.putExtra(CRERequestDetailActivity.INTENT_KEY_REQUEST_TYPE, "exchange");
                intent.putExtra(CRERequestDetailActivity.INTENT_KEY_PRODUCT_NAME, data.title);
                intent.putExtra(CRERequestDetailActivity.INTENT_KEY_PRODUCT_OPTION, data.option);
                intent.putExtra(CRERequestDetailActivity.INTENT_KEY_PRODUCT_PRICE, "");
                intent.putExtra(CRERequestDetailActivity.INTENT_KEY_PRODUCT_KEY, data.itemKey);
                intent.putExtra(CRERequestDetailActivity.INTENT_KEY_ST_ORDER_NUMBER, stOrderNumber);
                intent.putExtra(CRERequestDetailActivity.INTENT_KEY_DELIVERY_COMPANY, data.trackingInfoName);

                startActivityForResult(intent, REQUEST_EXCHANGE);
            });

            dialog.dialogShow(this);
        }
    }

    /**
     * 주문 취소
     *
     * @param data
     */
    @Override
    public void cancelRequest(DeliveryStatusDetailProductData data) {
        if (data != null) {
            Intent intent = new Intent(this, CRERequestDetailActivity.class);
            intent.putExtra(CRERequestDetailActivity.INTENT_KEY_REQUEST_TYPE, "cancel");
            intent.putExtra(CRERequestDetailActivity.INTENT_KEY_PRODUCT_NAME, data.title);
            intent.putExtra(CRERequestDetailActivity.INTENT_KEY_PRODUCT_OPTION, data.option);
            intent.putExtra(CRERequestDetailActivity.INTENT_KEY_PRODUCT_PRICE, String.valueOf(data.price));
            intent.putExtra(CRERequestDetailActivity.INTENT_KEY_PRODUCT_KEY, data.itemKey);
            intent.putExtra(CRERequestDetailActivity.INTENT_KEY_ST_ORDER_NUMBER, stOrderNumber);
            intent.putExtra(CRERequestDetailActivity.INTENT_KEY_DELIVERY_COMPANY, data.trackingInfoName);

            startActivityForResult(intent, REQUEST_CANCEL);
        }
    }

    /**
     * 구매 확정
     *
     * @param data
     */
    @Override
    public void confirmRequest(DeliveryStatusDetailProductData data, String shopName) {
        String productTitle = data.title;
        String productOption = data.option;
        String productPrice = data.price + "";
        String deliveryCompany = data.trackingInfoName;
        String productKey = data.itemKey;
        String productImage = data.image;
        String productStatus = data.status;
        String ct_id = data.ct_id;
        String it_id = data.it_id;
        DeliveryStatusDetailReview reviewObject = data.review;
        if (DeliveryStatusDetailAdapter.PRODUCT_DELIVERED.equals(data.status.replaceAll(" ", ""))) {
            Intent intent = new Intent(DeliveryStatusDetailActivity.this, WriteReviewActivity.class);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_PRODUCT_NAME, productTitle);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_STORE_NAME, shopName);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_PRODUCT_OPTION, productOption);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_PRODUCT_PRICE, productPrice);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_DELIVERY_COMPANY, deliveryCompany);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_ITEM_KEY, productKey);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_ST_ORDER_NUMBER, stOrderNumber);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_ORDER_NUMBER, orderNumber);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_PRODUCT_IMG, productImage);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_PRODUCT_STATUS, productStatus);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_CT_ID, ct_id);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_IT_ID, it_id);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_DATE, regDate);
            startActivityForResult(intent, REQUEST_REVIEW);
        } else {
            Intent intent = new Intent(DeliveryStatusDetailActivity.this, WriteDetailReviewActivity.class);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_PRODUCT_NAME, productTitle);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_STORE_NAME, shopName);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_PRODUCT_OPTION, productOption);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_PRODUCT_PRICE, productPrice);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_DELIVERY_COMPANY, deliveryCompany);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_ITEM_KEY, productKey);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_ST_ORDER_NUMBER, stOrderNumber);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_ORDER_NUMBER, orderNumber);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_PRODUCT_IMG, productImage);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_PRODUCT_STATUS, productStatus);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_CT_ID, ct_id);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_IT_ID, it_id);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_DATE, regDate);
            if (reviewObject != null) {
                JSONObject object = new JSONObject();
                try {
                    object.put("is_id", reviewObject.is_id);
                    object.put("is_type", reviewObject.is_type);
                    object.put("is_score", reviewObject.is_score);
                    object.put("is_subject", reviewObject.is_subject);
                    object.put("is_content", reviewObject.is_content);
                    JSONArray array = new JSONArray();
                    if (!reviewObject.is_photo.isEmpty()) {
                        for (int i = 0; i < reviewObject.is_photo.size(); i++) {
                            array.put(reviewObject.is_photo.get(i));
                        }
                    }
                    object.put("is_photo", array);

                    String result = object.toString();
                    Logger.e("result :: " + result);
                    intent.putExtra(WriteDetailReviewActivity.WRITE_DETAIL_REVIEW_OBJECT, result);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                startActivityForResult(intent, REQUEST_REVIEW);
            }

        }
    }

    /**
     * 전체 주문 취소
     */
    private void cancelAllRequest() {
        ShopTreeAsyncTask task = new ShopTreeAsyncTask(this);
        task.requestAllCancel(stOrderNumber, null,
                (result, obj) -> {
                    Logger.d("cancel all request :: " + obj.toString());
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
                        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
                        final BaseResponse responseObj = mapper.readValue(obj.toString(),
                                BaseResponse.class);

                        if (responseObj != null) {
                            Logger.e("response :: " + responseObj.getResult());
                            if (responseObj.getResult().equals("SUCCESS")) {
                                Toast.makeText(this, getString(R.string.msg_my_shopping_order_detail_all_cancel_success), Toast.LENGTH_SHORT).show();
//                                new AppToast(this).showToastMessage(
//                                        getString(R.string.msg_my_shopping_order_detail_all_cancel_success),
//                                        AppToast.DURATION_MILLISECONDS_DEFAULT, AppToast.GRAVITY_BOTTOM);

                                setResult(Activity.RESULT_OK);
                                finish();
                            } else {
                                Logger.e("false message :: " + responseObj.getResultMessage());
                                Toast.makeText(this, responseObj.getResultMessage(), Toast.LENGTH_SHORT).show();
//                                new AppToast(this).showToastMessage(responseObj.getResultMessage(),
//                                        AppToast.DURATION_MILLISECONDS_DEFAULT, AppToast.GRAVITY_BOTTOM);
                            }
                        }
                    } catch (Exception e) {
                        Logger.e(e.toString());
                    }
                });
    }

    private void loadData() {
        ShopTreeAsyncTask task = new ShopTreeAsyncTask(this);
        task.getOrderDetailList(orderNumber,
                (result, obj) -> {
                    Logger.d("order_detail" + obj.toString());
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
                        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
                        final DeliveryStatusDetailResponse responseObj = mapper.readValue(obj.toString(),
                                DeliveryStatusDetailResponse.class);

                        if (responseObj != null && responseObj.getResult().equals("SUCCESS")) {
                            // 배송정보
                            if (responseObj.deliveryInfo != null) {
                                textViewReceiveAddress.setText(responseObj.deliveryInfo.deliveryAddress);
                                textViewPhoneNumber.setText(responseObj.deliveryInfo.deliveryPhone);
                                textViewRecipient.setText(responseObj.deliveryInfo.deliveryName);
                                deliveryName = responseObj.deliveryInfo.deliveryName;
                                try {
                                    double deliveryPrice = Double.valueOf(responseObj.deliveryInfo.deliveryPrice);
                                    String f_price = Utils.ToNumFormat(deliveryPrice);
                                    dPrice.setText(f_price + "원");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            // 전체취소 버튼 활성화 여부
                            boolean isVisible = false;
                            for (int i = 0; i < responseObj.ordersInfo.items.size(); i++) {
                                List<DeliveryStatusDetailProductData> productData = responseObj.ordersInfo.items.get(i).productData;
                                for (int j = 0; j < productData.size(); j++) {
                                    Logger.e("productData.get(j).status ;: " + productData.get(j).status);
                                    if (PAYMENT_SUCCESS.equals(productData.get(j).status.replaceAll(" ", "")) || "상품준비중".equalsIgnoreCase(productData.get(j).status.replaceAll(" ", ""))) {
                                        isVisible = true;
                                        break;
                                    }
                                }
                            }

                            if (isVisible) {
                                viewForAllCancel.setVisibility(View.VISIBLE);
//                                empty.setVisibility(View.GONE);
                            } else {
                                viewForAllCancel.setVisibility(View.GONE);
//                                empty.setVisibility(View.VISIBLE);

                            }

                            // 상품정보
                            if (responseObj.ordersInfo != null) {
//                                textViewOrderNumber.setText(String.format(getString(R.string.msg_my_shopping_order_number_format), orderNumber));
                                textViewOrderNumber.setText("" + orderNumber);
                                String reg = regDate;
//                                SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm");
//                                SimpleDateFormat newFormat = new SimpleDateFormat("yyyy년 mm월 dd일");
                                try {
                                    String[] split = reg.split(" ");
                                    String date = split[0];
                                    String[] dateArr = date.split("-");
                                    String year = dateArr[0];
                                    String month = dateArr[1];
                                    String day = dateArr[2];
//                                    Date original_date = originalFormat.parse(reg);
//                                    String new_date = newFormat.format(original_date);
                                    String new_date = year + "년 " + month + "월 " + day + "일";
                                    textViewDate.setText(new_date);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }


                                mAdapter = new DeliveryStatusDetailAdapter(this);
                                recyclerViewProducts.setAdapter(mAdapter);
                                mAdapter.setListener(this);
                                mAdapter.setItems(responseObj.ordersInfo.items);
                            }

                            try {
                                double deliveryPrice = Double.valueOf(responseObj.deliveryInfo.deliveryPrice);
                                String f_price = Utils.ToNumFormat(deliveryPrice);
                                dPrice.setText(f_price + "원");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            try {
                                double usePoint = Double.valueOf(responseObj.usePoint);
                                String u_price = Utils.ToNumFormat(usePoint);
                                pointPrice.setText(u_price + "원");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            try {
                                double totalPrice = Double.valueOf(responseObj.totalPrice);
                                String t_price = Utils.ToNumFormat(totalPrice);
                                tPrice.setText(t_price + "원");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            try {
                                double paymentPrice = Double.valueOf(responseObj.paymentPrice);
                                String payment_price = Utils.ToNumFormat(paymentPrice);
                                pPrice.setText(payment_price + "원");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            stOrderNumber = responseObj.ordersInfo.stOrderNumber;
                            Logger.e("stOrderNumber :: " + stOrderNumber);
                        }
                    } catch (Exception e) {
                        Logger.e(e.toString());
                        progressBar.setVisibility(View.GONE);
                    } finally {
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void setLayout() {
        buttonBack = findViewById(R.id.buttonBack);
        textViewOrderNumber = findViewById(R.id.textViewOrderNumber);
        textViewDate = findViewById(R.id.textViewDate);
        textViewReceiveAddress = findViewById(R.id.textViewReceiveAddress);
        textViewPhoneNumber = findViewById(R.id.textViewPhoneNumber);
        textViewRecipient = findViewById(R.id.textViewRecipient);
        progressBar = findViewById(R.id.progressBar);
        viewForAllCancel = findViewById(R.id.layoutForAllCancel);
        empty = findViewById(R.id.empty);
        recyclerViewProducts = findViewById(R.id.recyclerViewProducts);
        buttonAllCancel = findViewById(R.id.buttonAllCancel);

        pPrice = findViewById(R.id.pPrice);
        dPrice = findViewById(R.id.dPrice);
        pointPrice = findViewById(R.id.pointPrice);
        tPrice = findViewById(R.id.tPrice);
    }
}
