package com.enliple.pudding.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.enliple.pudding.AbsBaseActivity;
import com.enliple.pudding.R;
import com.enliple.pudding.adapter.my.PurchaseHistoryDetailAdapter;
import com.enliple.pudding.commons.log.Logger;
import com.enliple.pudding.commons.app.NetworkStatusUtils;
import com.enliple.pudding.commons.app.PriceFormatter;
import com.enliple.pudding.commons.db.DBManager;
import com.enliple.pudding.commons.internal.AppPreferences;
import com.enliple.pudding.commons.network.NetworkApi;
import com.enliple.pudding.commons.network.NetworkBus;
import com.enliple.pudding.commons.network.NetworkBusResponse;
import com.enliple.pudding.commons.network.NetworkHandler;
import com.enliple.pudding.commons.network.vo.API21;
import com.enliple.pudding.commons.shoptree.data.PurchaseHistoryDetailProductData;
import com.enliple.pudding.commons.shoptree.network.ShopTreeAsyncTask;
import com.enliple.pudding.commons.shoptree.response.BaseResponse;
import com.enliple.pudding.commons.shoptree.response.PurchaseHistoryDetailResponse;
import com.enliple.pudding.commons.widget.toast.AppToast;
import com.enliple.pudding.widget.shoptree.CancelRequestDialog;
import com.enliple.pudding.widget.shoptree.DepositCancelDialog;
import com.enliple.pudding.widget.shoptree.ExchangeRequestDialog;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;


/**
 * Created by Kim Joonsung on 2018-09-19.
 */

public class PurchaseHistoryDetailActivity extends AbsBaseActivity implements PurchaseHistoryDetailAdapter.Listener {
    private static final int REQUEST_CANCEL = 0;
    private static final int REQUEST_REFUND = 1;
    private static final int REQUEST_EXCHANGE = 2;
    private static final int REQUEST_REVIEW = 3;

    private static final int REQUEST_GO_CART = 50222;

    public static final String INTENT_EXTRA_KEY_ORDER_NUMBER = "order_number";

    private static final String PAYMENT_SUCCESS = "결제완료";
    private static final String DEPOSIT = "입금 대기";

    private RelativeLayout buttonBack;
    private AppCompatImageButton buttonMessage, buttonCart;
    private AppCompatTextView textViewOrderNumber;
    private AppCompatTextView textViewDate;
    private AppCompatTextView textViewReceiveAddress;
    private AppCompatTextView textViewPhoneNumber;
    private AppCompatTextView textViewRecipient;
    private AppCompatTextView textViewTotalProductPrice;
    private AppCompatTextView textViewTotalDeliveryPrice;
    private AppCompatTextView textViewPoint;
    private AppCompatTextView textViewTotalPayPrice;
    private AppCompatTextView textViewDepositPrice;
    private AppCompatTextView textViewDepositDueDate;
    private AppCompatTextView textViewDepositBank;
    private AppCompatTextView textViewDepositAccount;
    private AppCompatTextView textViewAccountName, textViewMessageBadge, textViewCartBadge;
    private AppCompatTextView buttonAllCancel;
    private RecyclerView recyclerViewProducts;
    private ProgressBar progressBar;
    private LinearLayout viewForAllCancel;
    private View viewForDeposit;
    private String stOrderNumber = "";

    private String orderNumber;
    private String shopTreeOrderNo;
    private String deliveryName;
    private String bankName, account, accountName;
    private int refundPoint = 0;
    private PurchaseHistoryDetailAdapter mAdapter;
    private String regDate = "";
    private View.OnClickListener clickListener = v -> {
        long viewId = v.getId();
        if (viewId == R.id.buttonBack) {
            onBackPressed();
        } else if (viewId == R.id.buttonAllCancel) {
            cancelAllRequest();
        } else if (viewId == R.id.buttonMessage) {
            startActivity(new Intent(this, MessageActivity.class));
        } else if (viewId == R.id.buttonCart) {
            Intent intent = new Intent(this, ProductCartActivity.class);
            startActivityForResult(intent, REQUEST_GO_CART);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase_history_detail);
        setLayout();
        EventBus.getDefault().register(this);

        orderNumber = getIntent().getStringExtra(INTENT_EXTRA_KEY_ORDER_NUMBER);

        recyclerViewProducts.setHasFixedSize(false);
        recyclerViewProducts.setNestedScrollingEnabled(false);
        recyclerViewProducts.setHasFixedSize(true);

        bankName = "국민은행";
        account = "1100225-253-153254";
        accountName = "예금주:(주)인라이플";

        loadData();

        NetworkBus bus = new NetworkBus(NetworkApi.API21.name(), AppPreferences.Companion.getUserId(this));
        EventBus.getDefault().post(bus);
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
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CANCEL || requestCode == REQUEST_REFUND ||
                    requestCode == REQUEST_EXCHANGE || requestCode == REQUEST_REVIEW) {
                loadData();
            } else if (requestCode == REQUEST_GO_CART) {
                int cartCnt = data.getIntExtra("CART_CNT", 0);
//                setCartBadgeCount(cartCnt);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * 환불요청
     *
     * @param data
     */
    @Override
    public void refundRequest(PurchaseHistoryDetailProductData data) {
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
     * 교환요청
     *
     * @param data
     */
    @Override
    public void exchangeRequest(PurchaseHistoryDetailProductData data) {
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
     * 취소요청
     *
     * @param data
     */
    @Override
    public void cancelRequest(PurchaseHistoryDetailProductData data) {
        if (data != null) {
            if (data.status.equals(DEPOSIT)) {
                DepositCancelDialog dialog = new DepositCancelDialog(this, data.price, refundPoint, () -> {
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
    }

    /**
     * 수취확인/리뷰작성
     *
     * @param data
     */
    @Override
    public void productReview(PurchaseHistoryDetailProductData data, String shopName) {
        if (data != null) {
            Intent intent = new Intent(this, WriteReviewActivity.class);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_PRODUCT_NAME, data.title);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_STORE_NAME, shopName);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_PRODUCT_OPTION, data.option);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_PRODUCT_PRICE, data.price);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_PRODUCT_KEY, data.itemKey);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_ITEM_KEY, data.itemKey);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_ORDER_NUMBER, orderNumber);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_PRODUCT_IMG, data.image);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_PRODUCT_STATUS, data.status);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_TRACKING_NAME, data.trackingInfoName);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_TRACKING_CODE, data.trackingInfoCode);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_TRACKING_URL, data.trackingInfoUrl);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_CT_ID, data.ct_id);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_IT_ID, data.it_id);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_DATE, regDate);

            startActivityForResult(intent, REQUEST_REVIEW);
        }
    }

    /**
     * 꼼꼼리뷰 작성ad
     *
     * @param data
     */
    @Override
    public void productDetailReview(PurchaseHistoryDetailProductData data, String shopName) {
        if (data != null) {
            Intent intent = new Intent(this, WriteDetailReviewActivity.class);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_PRODUCT_NAME, data.title);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_STORE_NAME, shopName);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_PRODUCT_OPTION, data.option);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_PRODUCT_PRICE, String.valueOf(data.price));
            intent.putExtra(WriteReviewActivity.INTENT_KEY_ITEM_KEY, data.itemKey);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_ORDER_NUMBER, orderNumber);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_PRODUCT_IMG, data.image);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_PRODUCT_STATUS, data.status);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_CT_ID, data.ct_id);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_IT_ID, data.it_id);
            intent.putExtra(WriteReviewActivity.INTENT_KEY_DATE, regDate);

            if (data.review != null) {
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
            }

            startActivity(intent);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(NetworkBusResponse data) {
        String key = NetworkHandler.Companion.getInstance(this)
                .getKey(NetworkApi.API21.toString(), AppPreferences.Companion.getUserId(this), "");
        if (data.arg1.equals(key)) {
            String str = DBManager.getInstance(this).get(data.arg1);
            API21 response = new Gson().fromJson(str, API21.class);
            int smsCount = 0;
            int cartCount = 0;
            try {
                smsCount = Integer.valueOf(response.newMessage);
                cartCount = Integer.valueOf(response.cartCount);
            } catch (Exception e) {
                e.printStackTrace();
            }
//            setSmsBadgeCount(smsCount);
//            setCartBadgeCount(cartCount);
        }
    }

    /**
     * 전체 주문 취소
     */
    private void cancelAllRequest() {
        ShopTreeAsyncTask task = new ShopTreeAsyncTask(this);
        task.requestAllCancel(stOrderNumber, null,
                (result, obj) -> {
                    Logger.d("" + obj.toString());
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
                        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
                        final BaseResponse responseObj = mapper.readValue(obj.toString(),
                                BaseResponse.class);

                        if (responseObj != null) {
                            if (responseObj.getResult().equals("SUCCESS")) {
//                                new AppToast(this).showToastMessage(
//                                        getString(R.string.msg_my_shopping_order_detail_all_cancel_success),
//                                        AppToast.DURATION_MILLISECONDS_DEFAULT, AppToast.GRAVITY_BOTTOM);
                                Toast.makeText(this, getString(R.string.msg_my_shopping_order_detail_all_cancel_success), Toast.LENGTH_SHORT).show();
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

    /**
     * 취소요청 JSON 데이터를 구성한다.(입금대기 상태)
     *
     * @return
     */
    private JSONObject getRequestCancelObj(PurchaseHistoryDetailProductData data) {
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

/*    private void setSmsBadgeCount(int count) {
        textViewMessageBadge.setText("" + count);
        if (count > 0) {
            textViewMessageBadge.setVisibility(View.VISIBLE);
        } else {
            textViewMessageBadge.setVisibility(View.GONE);
        }
    }*/

    /**
     * 장바구니 Badge 에 설정할 Count 를 지정 (0 개인 경우 숨김처리)
     *
     */
/*    private void setCartBadgeCount(int count) {
        textViewCartBadge.setText("" + count);
        if (count > 0) {
            textViewCartBadge.setVisibility(View.VISIBLE);
        } else {
            textViewCartBadge.setVisibility(View.GONE);
        }
    }*/

    private void loadData() {
        ShopTreeAsyncTask task = new ShopTreeAsyncTask(this);
        task.getOrderDetailList(orderNumber, (result, obj) -> {
            Logger.d("" + obj.toString());
            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
                mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
                final PurchaseHistoryDetailResponse responseObj = mapper.readValue(obj.toString(),
                        PurchaseHistoryDetailResponse.class);

                if (responseObj != null && responseObj.getResult().equals("SUCCESS")) {
                    // 배송정보
                    if (responseObj.deliveryInfo != null) {
                        textViewReceiveAddress.setText(responseObj.deliveryInfo.deliveryAddress);
                        textViewPhoneNumber.setText(responseObj.deliveryInfo.deliveryPhone);
                        textViewRecipient.setText(responseObj.deliveryInfo.deliveryName);
                        textViewTotalDeliveryPrice.setText(String.format(getString(R.string.msg_price_format),
                                PriceFormatter.Companion.getInstance().getFormattedValue(responseObj.deliveryInfo.deliveryPrice)));

                        deliveryName = responseObj.deliveryInfo.deliveryName;
                    }

                    // 결제정보
                    textViewTotalProductPrice.setText(String.format(getString(R.string.msg_price_format),
                            PriceFormatter.Companion.getInstance().getFormattedValue(responseObj.paymentPrice)));
                    textViewPoint.setText(String.format(getString(R.string.msg_price_format),
                            PriceFormatter.Companion.getInstance().getFormattedValue(responseObj.usePoint)));
                    textViewTotalPayPrice.setText(String.format(getString(R.string.msg_price_format),
                            PriceFormatter.Companion.getInstance().getFormattedValue(responseObj.totalPrice)));

                    refundPoint = responseObj.usePoint;
                    shopTreeOrderNo = responseObj.ordersInfo.shopTreeOrderNo;

                    // 입금정보
                    if (responseObj.deposit.equals("Y")) {
                        viewForDeposit.setVisibility(View.VISIBLE);
                        if (responseObj.bankingInfo != null) {
                            textViewDepositPrice.setText(String.format(getString(R.string.msg_price_format),
                                    PriceFormatter.Companion.getInstance().getFormattedValue(responseObj.bankingInfo.price)));
                            textViewDepositBank.setText(bankName);

                            StringBuilder date = new StringBuilder(responseObj.bankingInfo.pdate);
                            date.append("까지");
                            textViewDepositDueDate.setText(date.toString());
                            textViewDepositAccount.setText(account);
                            textViewAccountName.setText(accountName);
                        }
                    } else {
                        viewForDeposit.setVisibility(View.GONE);
                    }

//                     전체취소 버튼 활성화 여부
                    boolean isVisible = false;
                    for (int i = 0; i < responseObj.ordersInfo.items.size(); i++) {
                        List<PurchaseHistoryDetailProductData> productData = responseObj.ordersInfo.items.get(i).productData;
                        for (int j = 0; j < productData.size(); j++) {
                            Logger.e("status :: " + productData.get(j).status);
                            if (PAYMENT_SUCCESS.equals(productData.get(j).status.replaceAll(" ", "")) || "상품준비중".equalsIgnoreCase(productData.get(j).status.replaceAll(" ", ""))) {
                                isVisible = true;
                                break;
                            }
                        }
                    }
                    Logger.e("isVisible :: " + isVisible);
                    if (isVisible) {
                        viewForAllCancel.setVisibility(View.VISIBLE);
                    } else {
                        viewForAllCancel.setVisibility(View.GONE);
                    }

                    // 상품정보
                    if (responseObj.ordersInfo != null) {
                        textViewOrderNumber.setText(orderNumber);

                        String[] array = responseObj.ordersInfo.regdate.split(" ");
                        String time = array[0];
                        String[] aTime = time.split("-");
                        String fTime = aTime[0] + "년 " + aTime[1] + "월 " + aTime[2] + "일";
                        textViewDate.setText(fTime);
                        stOrderNumber = responseObj.ordersInfo.shopTreeOrderNo;
                        regDate = responseObj.ordersInfo.regdate;
                        mAdapter = new PurchaseHistoryDetailAdapter(this);
                        recyclerViewProducts.setAdapter(mAdapter);
                        mAdapter.setListener(this);
                        mAdapter.setItems(responseObj.ordersInfo.items);
                    }
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
        buttonBack.setOnClickListener(clickListener);
        buttonAllCancel = findViewById(R.id.buttonAllCancel);
        buttonAllCancel.setOnClickListener(clickListener);

        textViewOrderNumber = findViewById(R.id.textViewOrderNumber);
        textViewDate = findViewById(R.id.textViewDate);
        textViewReceiveAddress = findViewById(R.id.textViewReceiveAddress);
        textViewPhoneNumber = findViewById(R.id.textViewPhoneNumber);
        textViewRecipient = findViewById(R.id.textViewRecipient);
        textViewTotalProductPrice = findViewById(R.id.textViewTotalProductPrice);
        textViewTotalDeliveryPrice = findViewById(R.id.textViewTotalDeliveryPrice);
        textViewPoint = findViewById(R.id.textViewPoint);
        textViewTotalPayPrice = findViewById(R.id.textViewTotalPayPrice);
        textViewDepositPrice = findViewById(R.id.textViewDepositPrice);
        textViewDepositDueDate = findViewById(R.id.textViewDepositDueDate);
        textViewDepositBank = findViewById(R.id.textViewDepositBank);
        textViewDepositAccount = findViewById(R.id.textViewDepositAccount);
        textViewAccountName = findViewById(R.id.textViewAccountName);

        progressBar = findViewById(R.id.progressBar);
        viewForAllCancel = findViewById(R.id.layoutForAllCancel);
        viewForDeposit = findViewById(R.id.layoutForDeposit);

        recyclerViewProducts = findViewById(R.id.recyclerViewProducts);
    }
}
