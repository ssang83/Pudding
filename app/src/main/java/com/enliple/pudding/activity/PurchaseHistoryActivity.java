package com.enliple.pudding.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.recyclerview.widget.RecyclerView;

import com.enliple.pudding.AbsBaseActivity;
import com.enliple.pudding.R;
import com.enliple.pudding.adapter.my.PurchaseHistoryAdapter;
import com.enliple.pudding.commons.app.NetworkStatusUtils;
import com.enliple.pudding.commons.log.Logger;
import com.enliple.pudding.commons.shoptree.data.PurchaseHistoryItems;
import com.enliple.pudding.commons.shoptree.network.ShopTreeAsyncTask;
import com.enliple.pudding.commons.shoptree.response.BaseResponse;
import com.enliple.pudding.commons.shoptree.response.PurchaseHistoryResponse;
import com.enliple.pudding.commons.widget.toast.AppToast;
import com.enliple.pudding.widget.shoptree.CancelRequestDialog;
import com.enliple.pudding.widget.shoptree.DepositCancelDialog;
import com.enliple.pudding.widget.shoptree.ExchangeRequestDialog;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Kim Joonsung on 2018-09-18.
 */

public class PurchaseHistoryActivity extends AbsBaseActivity implements PurchaseHistoryAdapter.Listener {

    private static final int REQUEST_CANCEL = 0;
    private static final int REQUEST_REFUND = 1;
    private static final int REQUEST_EXCHANGE = 2;
    private static final int REQUEST_REVIEW = 3;

    private static final String DEPOSIT = "입금 대기";

    private static final int RECYCLER_VIEW_ITEM_CACHE_SIZE = 20;
    private static final int REQUEST_DETAIL = 0;
    private static final int REQUEST_GO_CART = 50222;

    private RecyclerView recyclerViewPurchaseHistory;
    private ProgressBar progressBar;
    private View viewForList;
    private RelativeLayout buttonBack;
    private View layoutEmpty;

    private PurchaseHistoryAdapter mAdapter;

    private View.OnClickListener clickListener = v -> {
        long viewId = v.getId();
        if (viewId == R.id.buttonBack) {
            onBackPressed();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase_history);
        setLayout();

        recyclerViewPurchaseHistory.setItemViewCacheSize(RECYCLER_VIEW_ITEM_CACHE_SIZE);
        recyclerViewPurchaseHistory.setHasFixedSize(false);

        loadData();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void onNetworkStatusChanged(@NotNull NetworkStatusUtils.NetworkStatus status) {

    }

    @Override
    public void onDozeModeStateChanged(boolean dozeEnable) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onItemClicked(PurchaseHistoryItems data) {
        Logger.e("################## Order Number : " + data.orderNumber);
        String orderNo = data.orderNumber;
        Intent intent = new Intent(PurchaseHistoryActivity.this, PurchaseHistoryDetailActivity.class);
        intent.putExtra(PurchaseHistoryDetailActivity.INTENT_EXTRA_KEY_ORDER_NUMBER, orderNo);
        startActivityForResult(intent, REQUEST_DETAIL);
    }

    @Override
    public void exchangeRequest(PurchaseHistoryItems data) {
        if (data != null) {
            ExchangeRequestDialog dialog = new ExchangeRequestDialog(this, context -> {
                Intent intent = new Intent(context, CRERequestDetailActivity.class);
                intent.putExtra(CRERequestDetailActivity.INTENT_KEY_REQUEST_TYPE, "exchange");
                intent.putExtra(CRERequestDetailActivity.INTENT_KEY_PRODUCT_NAME, data.title);
                intent.putExtra(CRERequestDetailActivity.INTENT_KEY_PRODUCT_OPTION, data.option);
                intent.putExtra(CRERequestDetailActivity.INTENT_KEY_PRODUCT_PRICE, "");
                intent.putExtra(CRERequestDetailActivity.INTENT_KEY_PRODUCT_KEY, data.itemKey);
                intent.putExtra(CRERequestDetailActivity.INTENT_KEY_ST_ORDER_NUMBER, data.stOrderNumber);
                intent.putExtra(CRERequestDetailActivity.INTENT_KEY_DELIVERY_COMPANY, data.trackingInfoName);

                startActivityForResult(intent, REQUEST_EXCHANGE);
            });

            dialog.dialogShow(this);
        }
    }

    @Override
    public void refundRequest(PurchaseHistoryItems data) {
        if (data != null) {
            Intent intent = new Intent(this, CRERequestDetailActivity.class);
            intent.putExtra(CRERequestDetailActivity.INTENT_KEY_REQUEST_TYPE, "refund");
            intent.putExtra(CRERequestDetailActivity.INTENT_KEY_PRODUCT_NAME, data.title);
            intent.putExtra(CRERequestDetailActivity.INTENT_KEY_PRODUCT_OPTION, data.option);
            intent.putExtra(CRERequestDetailActivity.INTENT_KEY_PRODUCT_PRICE, String.valueOf(data.price));
            intent.putExtra(CRERequestDetailActivity.INTENT_KEY_ST_ORDER_NUMBER, data.stOrderNumber);
            intent.putExtra(CRERequestDetailActivity.INTENT_KEY_PRODUCT_KEY, data.itemKey);
            intent.putExtra(CRERequestDetailActivity.INTENT_KEY_DELIVERY_COMPANY, data.trackingInfoName);

            startActivityForResult(intent, REQUEST_REFUND);
        }
    }

    @Override
    public void cancelRequest(PurchaseHistoryItems data) {
        if (data != null) {
            if (data.status.equals(DEPOSIT)) {

                DepositCancelDialog dialog = new DepositCancelDialog(this, data.price, data.usePoint, () -> {
                    ShopTreeAsyncTask task = new ShopTreeAsyncTask(this);
                    task.requestCancel(getRequestCancelObj(data),
                            (result, obj) -> {
                                Logger.d("" + obj.toString());
                                try {
                                    ObjectMapper mapper = new ObjectMapper();
                                    mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
                                    mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
                                    final BaseResponse responseObj = mapper.readValue(obj.toString(),
                                            BaseResponse.class);

                                    if (responseObj != null) {
                                        if (responseObj.getResult().equals("SUCCESS") && responseObj.getResultCode().equals("200")) {
                                            CancelRequestDialog cancelRequestDialog = new CancelRequestDialog(this, () -> {
                                                setResult(Activity.RESULT_OK);
                                                finish();
                                            });

                                            cancelRequestDialog.dialogShow(this);
                                        } else {
                                            new AppToast(this).showToastMessage(responseObj.getResultMessage(),
                                                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                                                    AppToast.GRAVITY_BOTTOM);
                                        }
                                    }
                                } catch (Exception e) {
                                }
                            });
                });

                dialog.dialogShow(this);
            } else {
                Intent intent = new Intent(this, CRERequestDetailActivity.class);
                Logger.e("data.price :::::: " + data.price);
                intent.putExtra(CRERequestDetailActivity.INTENT_KEY_REQUEST_TYPE, "cancel");
                intent.putExtra(CRERequestDetailActivity.INTENT_KEY_PRODUCT_NAME, data.title);
                intent.putExtra(CRERequestDetailActivity.INTENT_KEY_PRODUCT_OPTION, data.option);
                intent.putExtra(CRERequestDetailActivity.INTENT_KEY_PRODUCT_PRICE, String.valueOf(data.price));
                intent.putExtra(CRERequestDetailActivity.INTENT_KEY_PRODUCT_KEY, data.itemKey);
                intent.putExtra(CRERequestDetailActivity.INTENT_KEY_ST_ORDER_NUMBER, data.stOrderNumber);
                intent.putExtra(CRERequestDetailActivity.INTENT_KEY_DELIVERY_COMPANY, data.trackingInfoName);

                startActivityForResult(intent, REQUEST_CANCEL);
            }
        }
    }

    @Override
    public void productReview(PurchaseHistoryItems data, String date) {
        if (data != null) {
            Intent intent = new Intent(this, WriteReviewActivity.class);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_PRODUCT_NAME, data.title);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_PRODUCT_NAME, data.storeNname);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_PRODUCT_OPTION, data.option);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_PRODUCT_PRICE, data.price);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_PRODUCT_KEY, data.itemKey);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_ITEM_KEY, data.itemKey);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_ORDER_NUMBER, data.orderNumber);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_PRODUCT_IMG, data.image);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_PRODUCT_STATUS, data.status);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_TRACKING_NAME, data.trackingInfoName);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_TRACKING_CODE, data.trackingInfoCode);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_TRACKING_URL, data.trackingInfoUrl);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_CT_ID, data.ct_id);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_IT_ID, data.it_id);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_DATE, data.ctTime);

            startActivityForResult(intent, REQUEST_REVIEW);
        }
    }

    @Override
    public void productDetailReview(PurchaseHistoryItems data, String date) {
        if (data != null) {
            Intent intent = new Intent(this, WriteDetailReviewActivity.class);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_PRODUCT_NAME, data.title);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_STORE_NAME, data.storeNname);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_PRODUCT_OPTION, data.option);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_PRODUCT_PRICE, data.price);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_ITEM_KEY, data.itemKey);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_ORDER_NUMBER, data.orderNumber);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_PRODUCT_IMG, data.image);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_PRODUCT_STATUS, data.status);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_CT_ID, data.ct_id);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_IT_ID, data.it_id);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_DATE, data.ctTime);

            if (data.review != null) {
                Logger.e("data.review not null");
                JSONObject object = new JSONObject();
                try {
                    object.put("is_id", data.review.is_id);
                    object.put("is_type", data.review.is_type);
                    object.put("is_score", data.review.is_score);
                    object.put("is_subject", data.review.is_subject);
                    object.put("is_content", data.review.is_content);
                    JSONArray array = new JSONArray();
                    if (!data.review.is_photo.isEmpty()) {
                        for (int i = 0; i < data.review.is_photo.size(); i++) {
                            array.put(data.review.is_photo.get(i));
                        }
                    }
                    object.put("is_photo", array);

                    String result = object.toString();
                    Logger.e("result :: " + result);
                    intent.putExtra(WriteDetailReviewActivity.WRITE_DETAIL_REVIEW_OBJECT, result);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Logger.e("data.review null");
            }
            startActivity(intent);
        }
    }

    /**
     * 취소요청 JSON 데이터를 구성한다.(입금대기 상태)
     *
     * @return
     */
    private JSONObject getRequestCancelObj(PurchaseHistoryItems data) {
        JSONObject jsonObject = new JSONObject();

        JSONArray items = new JSONArray();
        JSONObject rowObject = new JSONObject();
        try {
            rowObject.put("key", data.itemKey);
            rowObject.put("message", "");
        } catch (Exception e) {
            Logger.e(e.toString());
        }

        items.put(rowObject);

        try {
            jsonObject.put("status", "취소 요청");
            jsonObject.put("items", items);
        } catch (Exception e) {
            Logger.e(e.toString());
        }

        Logger.e("######### getRequestCancelObj Request Data : " + jsonObject.toString());

        return jsonObject;
    }

    private void setLayout() {
        recyclerViewPurchaseHistory = findViewById(R.id.recyclerViewPurchaseHistory);
        progressBar = findViewById(R.id.progressBar);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        viewForList = findViewById(R.id.layoutForList);
        buttonBack = findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(clickListener);
    }


    private void loadData() {
        ShopTreeAsyncTask task = new ShopTreeAsyncTask(this);
        task.getPurchaseHistoryList((result, obj) -> {
            Logger.d("" + obj.toString());
            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
                mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
                final PurchaseHistoryResponse responseObj = mapper.readValue(obj.toString(),
                        PurchaseHistoryResponse.class);

                if (responseObj != null && responseObj.getResult().equals("SUCCESS")) {
                    if (responseObj.orders != null && responseObj.orders.size() > 0) {
                        viewForList.setVisibility(View.VISIBLE);
                        layoutEmpty.setVisibility(View.GONE);

                        mAdapter = new PurchaseHistoryAdapter(this);
                        recyclerViewPurchaseHistory.setAdapter(mAdapter);
                        mAdapter.setListener(this);
                        mAdapter.setItems(responseObj.orders);
                    } else {
                        viewForList.setVisibility(View.GONE);
                        layoutEmpty.setVisibility(View.VISIBLE);
                    }
                } else {
                    viewForList.setVisibility(View.GONE);
                    layoutEmpty.setVisibility(View.VISIBLE);
                }

            } catch (Exception e) {
                Logger.e(e.toString());
                progressBar.setVisibility(View.GONE);
            } finally {
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}
