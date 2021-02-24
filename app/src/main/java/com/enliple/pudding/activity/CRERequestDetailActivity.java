package com.enliple.pudding.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;

import com.enliple.pudding.AbsBaseActivity;
import com.enliple.pudding.R;
import com.enliple.pudding.commons.app.NetworkStatusUtils;
import com.enliple.pudding.commons.app.PriceFormatter;
import com.enliple.pudding.commons.app.Utils;
import com.enliple.pudding.commons.internal.AppPreferences;
import com.enliple.pudding.commons.log.Logger;
import com.enliple.pudding.commons.shoptree.network.ShopTreeAsyncTask;
import com.enliple.pudding.commons.shoptree.response.BaseResponse;
import com.enliple.pudding.commons.shoptree.response.ComplainCancelForm;
import com.enliple.pudding.commons.widget.toast.AppToast;
import com.enliple.pudding.model.SpinnerSelectModel;
import com.enliple.pudding.widget.SpinnerListDialog;
import com.enliple.pudding.widget.shoptree.CancelRequestDialog;
import com.enliple.pudding.widget.shoptree.ExchangeAndRefundRequestDialog;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kim Joonsung on 2018-09-20.
 */

public class CRERequestDetailActivity extends AbsBaseActivity {
    public final static String INTENT_KEY_REQUEST_TYPE = "CRERequestDetailActivity.INTENT_KEY_REQUEST_TYPE";
    public final static String INTENT_KEY_PRODUCT_NAME = "CRERequestDetailActivity.INTENT_KEY_PRODUCT_NAME";
    public final static String INTENT_KEY_PRODUCT_OPTION = "CRERequestDetailActivity.INTENT_KEY_PRODUCT_OPTION";
    public final static String INTENT_KEY_PRODUCT_PRICE = "CRERequestDetailActivity.INTENT_KEY_PRODUCT_PRICE";
    public final static String INTENT_KEY_PRODUCT_KEY = "CRERequestDetailActivity.INTENT_KEY_PRODUCT_KEY";
    public final static String INTENT_KEY_DELIVERY_COMPANY = "CRERequestDetailActivity.INTENT_KEY_DELIVERY_COMPANY";
    public final static String INTENT_KEY_ST_ORDER_NUMBER = "CRERequestDetailActivity.INTENT_KEY_ST_ORDER_NUMBER";

    public final static String CANCEL_REQUEST = "cancel";
    public final static String REFUND_REQUEST = "refund";
    public final static String EXCHANGE_REQUEST = "exchange";

    public final static String TYPE_CANCEL = "1";
    public final static String TYPE_EXCHANGE = "2";
    public final static String TYPE_REFUND = "3";

    List<SpinnerSelectModel> arr = new ArrayList<>();

    private RelativeLayout buttonBack;
    private AppCompatButton buttonCancel;
    private AppCompatButton buttonRequest;
    private AppCompatTextView textViewTitle;
    private AppCompatTextView textViewProductTitle;
    private AppCompatTextView textViewRequestProduct;
    private AppCompatTextView textViewOption;
    private AppCompatTextView textViewPrice;
    private AppCompatTextView textViewDetailInfo;
    private AppCompatTextView textViewReason;
    private AppCompatTextView textViewWarning;
    private AppCompatTextView textViewRequestMethod;
    private AppCompatTextView textViewRefundPrice;
    private AppCompatTextView textViewCancelPrice, textViewDeliveryFee1, textViewDeliveryFee2;
    private AppCompatTextView textViewMethod, textViewReasonCount;
    private AppCompatTextView spinnerText;
    private View viewForCancel;
    private View viewForRefund, viewForExchange;
    private View viewForMethod;
    private View viewForExchangeWarning;
    private View viewForCancelWarning;
    private View viewForRefundWarning;
    private View viewForCancelPrice;
    private ProgressBar progressBar;
    private AppCompatEditText editTextReasonDetail;


    private String requestType;
    private String requestReason;
    private String productName, productOption, deliveryCompany, productKey, orderNo;
    private double productPrice;
    private String type = TYPE_CANCEL;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cre_request_detail);
        setLayout();
        Logger.e("onCreate");
        buttonBack.setOnClickListener(clickListener);
        buttonCancel.setOnClickListener(clickListener);
        buttonRequest.setOnClickListener(clickListener);
        spinnerText.setOnClickListener(clickListener);
        checkIntentData(getIntent());
    }

    @Override
    public void onNetworkStatusChanged(@NotNull NetworkStatusUtils.NetworkStatus status) {

    }

    @Override
    public void onDozeModeStateChanged(boolean dozeEnable) {

    }

    @Override
    public void onBackPressed() {
        Logger.e("onBackPressed");
        finish();
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Logger.e("clickListener called");
            long viewId = v.getId();
            if (viewId == R.id.buttonBack) {
                Logger.e("cancel clicked");
                onBackPressed();
            } else if ( viewId == R.id.cancel ) {
                Logger.e("cancel clicked");
                onBackPressed();
            } else if (viewId == R.id.buttonRequest) {
                if (buttonRequest.getText().toString().equals(getString(R.string.request_cancel_do))) {     // 취소요청
                    if (TextUtils.isEmpty(requestReason)) {
                        new AppToast(CRERequestDetailActivity.this).showToastMessage(getString(R.string.msg_cancel_reason_no_select),
                                AppToast.DURATION_MILLISECONDS_DEFAULT,
                                AppToast.GRAVITY_BOTTOM);
                    } else {
                        ShopTreeAsyncTask task = new ShopTreeAsyncTask(CRERequestDetailActivity.this);
                        task.requestCancel(getRequestCancelObj(),
                                (result, obj) -> {
                                    Logger.d("response ::: " + obj.toString());
                                    try {
                                        ObjectMapper mapper = new ObjectMapper();
                                        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
                                        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
                                        final BaseResponse responseObj = mapper.readValue(obj.toString(),
                                                BaseResponse.class);

                                        if (responseObj.getResult().equals("SUCCESS")) {
                                            CancelRequestDialog dialog = new CancelRequestDialog(CRERequestDetailActivity.this, () -> {
                                                setResult(Activity.RESULT_OK);
                                                finish();
                                            });

                                            dialog.dialogShow(CRERequestDetailActivity.this);
                                        } else {
                                            new AppToast(CRERequestDetailActivity.this).showToastMessage(responseObj.getResultMessage(),
                                                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                                                    AppToast.GRAVITY_BOTTOM);
                                        }
                                    } catch (Exception e) {
                                    }
                                });
                    }
                } else if (buttonRequest.getText().toString().equals(getString(R.string.msg_cre_exchange_request_do))) {   // 교환요청
                    if (TextUtils.isEmpty(requestReason)) {
                        new AppToast(CRERequestDetailActivity.this).showToastMessage(getString(R.string.msg_exchange_reason_no_select),
                                AppToast.DURATION_MILLISECONDS_DEFAULT,
                                AppToast.GRAVITY_BOTTOM);
                    } else if (TextUtils.isEmpty(editTextReasonDetail.getText().toString())) {
                        new AppToast(CRERequestDetailActivity.this).showToastMessage(getString(R.string.msg_exchange_reason_no_input),
                                AppToast.DURATION_MILLISECONDS_DEFAULT,
                                AppToast.GRAVITY_BOTTOM);
                    } else {
                        ShopTreeAsyncTask task = new ShopTreeAsyncTask(CRERequestDetailActivity.this);
                        task.requestExchange(getRequestObject(EXCHANGE_REQUEST),
                                (result, obj) -> {
                                    Logger.d("" + obj.toString());
                                    try {
                                        ObjectMapper mapper = new ObjectMapper();
                                        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
                                        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
                                        final BaseResponse responseObj = mapper.readValue(obj.toString(),
                                                BaseResponse.class);

                                        if (responseObj.getResult().equals("SUCCESS")) {
                                            ExchangeAndRefundRequestDialog dialog = new ExchangeAndRefundRequestDialog(CRERequestDetailActivity.this, true, () -> {
                                                setResult(Activity.RESULT_OK);
                                                finish();
                                            });

                                            dialog.dialogShow(CRERequestDetailActivity.this);
                                        } else {
                                            new AppToast(CRERequestDetailActivity.this).showToastMessage(responseObj.getResultMessage(),
                                                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                                                    AppToast.GRAVITY_BOTTOM);
                                        }
                                    } catch (Exception e) {
                                    }
                                });
                    }
                } else {    // 환불요청
                    if (TextUtils.isEmpty(requestReason)) {
                        new AppToast(CRERequestDetailActivity.this).showToastMessage(getString(R.string.msg_refund_reason_no_input),
                                AppToast.DURATION_MILLISECONDS_DEFAULT,
                                AppToast.GRAVITY_BOTTOM);
                    } else {
                        ShopTreeAsyncTask task = new ShopTreeAsyncTask(CRERequestDetailActivity.this);
                        task.requestRefund(getRequestObject(REFUND_REQUEST),
                                (result, obj) -> {
                                    Logger.d("" + obj.toString());
                                    try {
                                        ObjectMapper mapper = new ObjectMapper();
                                        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
                                        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
                                        final BaseResponse responseObj = mapper.readValue(obj.toString(),
                                                BaseResponse.class);

                                        if (responseObj.getResult().equals("SUCCESS")) {
                                            ExchangeAndRefundRequestDialog dialog = new ExchangeAndRefundRequestDialog(CRERequestDetailActivity.this, false, () -> {
                                                setResult(Activity.RESULT_OK);
                                                finish();
                                            });

                                            dialog.dialogShow(CRERequestDetailActivity.this);
                                        } else {
                                            new AppToast(CRERequestDetailActivity.this).showToastMessage(responseObj.getResultMessage(),
                                                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                                                    AppToast.GRAVITY_BOTTOM);
                                        }
                                    } catch (Exception e) {
                                        Logger.e(e.toString());
                                    }
                                });
                    }
                }
            } else if ( viewId == R.id.spinnerText) {
                spinnerShow();
            }
        }
    };

    private void checkIntentData(Intent intent) {
        requestType = intent.getStringExtra(INTENT_KEY_REQUEST_TYPE);
        productName = intent.getStringExtra(INTENT_KEY_PRODUCT_NAME);
        productOption = intent.getStringExtra(INTENT_KEY_PRODUCT_OPTION);
        Logger.e("string val ::: " + intent.getStringExtra(INTENT_KEY_PRODUCT_PRICE));
        String sPrice = intent.getStringExtra(INTENT_KEY_PRODUCT_PRICE);
        try {
            productPrice = Double.valueOf(sPrice);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        productPrice = intent.getDoubleExtra(INTENT_KEY_PRODUCT_PRICE, 0);
        deliveryCompany = intent.getStringExtra(INTENT_KEY_DELIVERY_COMPANY);
        productKey = intent.getStringExtra(INTENT_KEY_PRODUCT_KEY);
        orderNo = intent.getStringExtra(INTENT_KEY_ST_ORDER_NUMBER);
        Logger.e("orderNo :: " + orderNo);
        type = TYPE_CANCEL;
        if (requestType.equals(CANCEL_REQUEST)) {
            viewForCancel.setVisibility(View.VISIBLE);
            viewForRefund.setVisibility(View.GONE);
            viewForExchange.setVisibility(View.GONE);
            viewForMethod.setVisibility(View.GONE);
            viewForExchangeWarning.setVisibility(View.GONE);
            viewForRefundWarning.setVisibility(View.GONE);
            viewForCancelWarning.setVisibility(View.VISIBLE);
            viewForCancelPrice.setVisibility(View.VISIBLE);
            editTextReasonDetail.setVisibility(View.VISIBLE);
            editTextReasonDetail.setHint("취소 사유를 상세히 입력해주세요");
            textViewTitle.setText(R.string.cancel_title);
            textViewProductTitle.setText(R.string.msg_cre_cancel_product_info);
            textViewDetailInfo.setText(R.string.msg_cre_cancel_detail_info);
            textViewReason.setText(R.string.msg_cre_cancel_reason);
            textViewWarning.setText(R.string.msg_cre_cancel_warnaing);
            buttonRequest.setText(R.string.request_cancel_do);

            type = TYPE_CANCEL;
        } else if (requestType.equals(EXCHANGE_REQUEST)) {
            viewForCancel.setVisibility(View.GONE);
            viewForRefund.setVisibility(View.GONE);
            viewForExchange.setVisibility(View.VISIBLE);
            viewForMethod.setVisibility(View.GONE);
            viewForExchangeWarning.setVisibility(View.VISIBLE);
            viewForRefundWarning.setVisibility(View.GONE);
            viewForCancelWarning.setVisibility(View.GONE);
            viewForCancelPrice.setVisibility(View.GONE);
            editTextReasonDetail.setVisibility(View.VISIBLE);
            editTextReasonDetail.setHint("교환 사유를 상세히 입력해주세요");
            textViewTitle.setText(R.string.msg_cre_exchange_request);
            textViewProductTitle.setText(R.string.msg_cre_exchange_product_info);
            textViewDetailInfo.setText(R.string.msg_cre_exchange_detail_info);
            textViewReason.setText(R.string.msg_cre_exchange_reason);
            textViewWarning.setText(R.string.msg_cre_exchange_warnaing);
            buttonRequest.setText(R.string.msg_cre_exchange_request_do);

            setDeliveryFee1GuideText("단순변심일 경우, 왕복 배송비 5,000원", "단순변심");
            setDeliveryFee2GuideText("판매자 책임일경우, 왕복 배송비 판매자 부담", "판매자 책임");

            type = TYPE_EXCHANGE;

        } else if (requestType.equals(REFUND_REQUEST)) {
            viewForCancel.setVisibility(View.GONE);
            viewForRefund.setVisibility(View.VISIBLE);
            viewForMethod.setVisibility(View.VISIBLE);
            viewForExchange.setVisibility(View.GONE);
            viewForExchangeWarning.setVisibility(View.GONE);
            viewForRefundWarning.setVisibility(View.VISIBLE);
            viewForCancelWarning.setVisibility(View.GONE);
            viewForCancelPrice.setVisibility(View.VISIBLE);
            editTextReasonDetail.setVisibility(View.VISIBLE);
            editTextReasonDetail.setHint("반품 사유를 상세히 입력해주세요");
            textViewTitle.setText(R.string.msg_cre_refund_request);
            textViewProductTitle.setText(R.string.msg_cre_refund_product_info);
            textViewDetailInfo.setText(R.string.msg_cre_refund_detail_info);
            textViewReason.setText(R.string.msg_cre_refund_reason);
            textViewWarning.setText(R.string.msg_cre_refund_warnaing);
            buttonRequest.setText(R.string.msg_cre_refund_request_do);
            textViewMethod.setText(getString(R.string.msg_cre_refund_request_method));

            type = TYPE_REFUND;
        }
        setRequestData();
    }

    private void spinnerShow() {
        if ( TextUtils.isEmpty(orderNo) )
            return;
        String title = "";
        if ( type == TYPE_CANCEL ) {
            title = "취소 사유 선택";
        } else if ( type == TYPE_REFUND ) {
            title = "환불 사유 선택";
        } else if ( type == TYPE_EXCHANGE ) {
            title = "교환 사유 선택";
        }
        final String fTitle = title;
        if ( arr != null && arr.size() > 0 ) {
            int width = AppPreferences.Companion.getScreenWidth(CRERequestDetailActivity.this) - Utils.ConvertDpToPx(CRERequestDetailActivity.this, 40);

            new SpinnerListDialog(CRERequestDetailActivity.this, width, fTitle, arr, new SpinnerListDialog.Listener() {
                @Override
                public void onConfirm(String category) {
                    Logger.e("clicked category :: " + category);
                    spinnerText.setText(category);
                    requestReason = category;
                }

                @Override
                public void cancel() {

                }
            }).show();
        } else {
            ShopTreeAsyncTask task = new ShopTreeAsyncTask(CRERequestDetailActivity.this);
            task.orderComplainCancelForm(orderNo, type, new ShopTreeAsyncTask.OnDefaultObjectCallbackListener() {
                @Override
                public void onResponse(boolean result, Object obj) {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
                        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
                        final ComplainCancelForm responseObj = mapper.readValue(obj.toString(), ComplainCancelForm.class);
                        String rt = responseObj.result;
                        String price = responseObj.cancel_price;
                        List<String> category = responseObj.category;

                        for ( int i = 0 ; i < category.size() ; i ++ ) {
                            SpinnerSelectModel model = new SpinnerSelectModel();
                            model.setCategory(category.get(i));
                            model.setSelected(false);
                            arr.add(model);
                        }

                        int width = AppPreferences.Companion.getScreenWidth(CRERequestDetailActivity.this) - Utils.ConvertDpToPx(CRERequestDetailActivity.this, 40);

                        new SpinnerListDialog(CRERequestDetailActivity.this, width, fTitle, arr, new SpinnerListDialog.Listener() {
                            @Override
                            public void onConfirm(String category) {
                                Logger.e("clicked category :: " + category);
                                spinnerText.setText(category);
                                requestReason = category;
                            }

                            @Override
                            public void cancel() {

                            }
                        }).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

    }


//    private void setSpinner(String type) {
//        if ( TextUtils.isEmpty(orderNo) )
//            return;
//        ShopTreeAsyncTask task = new ShopTreeAsyncTask(CRERequestDetailActivity.this);
//        task.orderComplainCancelForm(orderNo, type, new ShopTreeAsyncTask.OnDefaultObjectCallbackListener() {
//            @Override
//            public void onResponse(boolean result, Object obj) {
//                try {
//                    ObjectMapper mapper = new ObjectMapper();
//                    mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
//                    mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
//                    final ComplainCancelForm responseObj = mapper.readValue(obj.toString(), ComplainCancelForm.class);
//                    String rt = responseObj.result;
//                    String price = responseObj.cancel_price;
//                    List<String> category = responseObj.category;
//                    Logger.e("category size :: " + category.size());
//                    for ( int i = 0 ; i < category.size() ; i ++ ) {
//                        Logger.e("category :: " + category.get(i));
//                    }
//                    try {
//                        productPrice = Double.valueOf(price);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(CRERequestDetailActivity.this, R.layout.spinner_item, category);
//                    adapter.setDropDownViewResource(R.layout.spinner_dropdown);
//                    spinner.setAdapter(adapter);
//                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//                        @Override
//                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                            requestReason = spinner.getSelectedItem().toString();
//                            Logger.e("requestReason :: " + requestReason);
//                        }
//
//                        @Override
//                        public void onNothingSelected(AdapterView<?> parent) {
//
//                        }
//                    });
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//            }
//        });
//    }

//    /**
//     * 취소요청 사유 리스트 구성
//     */
//    private void setCancelRequestSpinner() {
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item, getCancelReasonList());
//        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
//        spinner.setAdapter(adapter);
//        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                requestReason = spinner.getSelectedItem().toString();
//                Logger.e("requestReason :: " + requestReason);
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });
//    }
//
//    /**
//     * 교환요청 사유 리스트 구성
//     */
//    private void setExchangeRequestSpinner() {
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(CRERequestDetailActivity.this, R.layout.spinner_item, getReasonList());
//        adapter.setDropDownViewResource(R.layout.spinner_dropdown);
//        spinner.setAdapter(adapter);
//        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                requestReason = spinner.getSelectedItem().toString();
//                Logger.e("requestReason :: " + requestReason);
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });
//    }
//
//    /**
//     * 환불요청 사유 리스트 구성
//     */
//    private void setRefundRequestSpinner() {
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(CRERequestDetailActivity.this, R.layout.spinner_item, getReasonList());
//        adapter.setDropDownViewResource(R.layout.spinner_dropdown);
//        spinner.setAdapter(adapter);
//        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                requestReason = spinner.getSelectedItem().toString();
//                Logger.e("requestReason :: " + requestReason);
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });
//    }

    /**
     * 요청 방법 관련 문구 색상 설정
     *
     * @param text
     * @param colorSpannableText
     */
    private void setRequestMethodText(@NonNull String text, @NonNull String colorSpannableText) {
        final SpannableStringBuilder sp = new SpannableStringBuilder(text);
        sp.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.purple_color)),
                12, colorSpannableText.length() + 12, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        sp.setSpan(new StyleSpan(Typeface.NORMAL), 12, colorSpannableText.length() + 12, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        textViewRequestMethod.setText(sp);
    }

    private void setDeliveryFee1GuideText(@NonNull String text, @NonNull String colorSpannableText) {
        final SpannableStringBuilder sp = new SpannableStringBuilder(text);
        sp.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.purple_color)),
                0, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        sp.setSpan(new StyleSpan(Typeface.BOLD), 0, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        textViewDeliveryFee1.setText(sp);
    }

    private void setDeliveryFee2GuideText(@NonNull String text, @NonNull String colorSpannableText) {
        final SpannableStringBuilder sp = new SpannableStringBuilder(text);
        sp.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.purple_color)),
                0, 6, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        sp.setSpan(new StyleSpan(Typeface.BOLD), 0, 6, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        textViewDeliveryFee2.setText(sp);
    }

    /**
     * 취소 사유 리스트
     *
     * @return
     */
    private List<String> getCancelReasonList() {
        List<String> list = new ArrayList<>();
        list.add(getString(R.string.msg_cre_request_cancel_reason1));
        list.add(getString(R.string.msg_cre_request_cancel_reason2));
        list.add(getString(R.string.msg_cre_request_cancel_reason3));
        list.add(getString(R.string.msg_cre_request_cancel_reason4));
        list.add(getString(R.string.msg_cre_request_cancel_reason5));
        list.add(getString(R.string.msg_cre_request_cancel_reason6));
        list.add(getString(R.string.msg_cre_request_cancel_reason7));

        return list;
    }

    /**
     * 교환/환불 사유 리스트
     *
     * @return
     */
    private List<String> getReasonList() {
        List<String> list = new ArrayList<>();
        list.add(getString(R.string.msg_cre_request_exchange_reason1));
        list.add(getString(R.string.msg_cre_request_exchange_reason2));
        list.add(getString(R.string.msg_cre_request_exchange_reason3));
        list.add(getString(R.string.msg_cre_request_exchange_reason4));

        return list;
    }


    /**
     * 환불/교환/취소 상품 데이터를 설정한다.
     */
    private void setRequestData() {
        textViewRequestProduct.setText(productName);
        textViewOption.setText(productOption);
        textViewPrice.setText(String.format(getString(R.string.msg_price_format),
                PriceFormatter.Companion.getInstance().getFormattedValue(productPrice)));
        textViewRefundPrice.setText(String.format(getString(R.string.msg_price_format),
                PriceFormatter.Companion.getInstance().getFormattedValue(productPrice)));
        textViewCancelPrice.setText(String.format(getString(R.string.msg_price_format),
                PriceFormatter.Companion.getInstance().getFormattedValue(productPrice)));
    }

    /**
     * 환불/교환요청 JSON 데이터를 구성한다.
     *
     * @return
     */
    private JSONObject getRequestObject(String type) {
        JSONObject jsonObject = new JSONObject();

        JSONArray items = new JSONArray();
        JSONObject rowObject = new JSONObject();
        try {
            rowObject.put("key", productKey);

            if (type.equals(REFUND_REQUEST)) {
                rowObject.put("message", requestReason);
            } else {
                String requestDetail = editTextReasonDetail.getText().toString();
                StringBuilder sb = new StringBuilder(requestReason);
                sb.append(" ");
                sb.append(requestDetail);

                rowObject.put("message", sb.toString());
            }
        } catch (Exception e) {
        }

        items.put(rowObject);

        try {
            if (type.equals(REFUND_REQUEST)) {
                jsonObject.put("status", "반품 요청");
            } else {
                jsonObject.put("status", "교환 요청");
            }
            jsonObject.put("items", items);
        } catch (Exception e) {
        }

        Logger.e("######### getRequestObject Request Data : " + jsonObject.toString());

        return jsonObject;
    }

    /**
     * 취소요청 JSON 데이터를 구성한다.(결재완료 상태), 건별취소일 경우
     *
     * @return
     */
    private JSONObject getRequestCancelObj() {
        JSONObject jsonObject = new JSONObject();

        JSONArray items = new JSONArray();
        JSONObject rowObject = new JSONObject();
        try {
            rowObject.put("key", productKey);
            rowObject.put("message", requestReason);
        } catch (Exception e) {
        }

        items.put(rowObject);

        try {
            jsonObject.put("status", "환불 요청");
            jsonObject.put("items", items);
        } catch (Exception e) {
        }

        Logger.e("######### getRequestCancelObj Request Data : " + jsonObject.toString());

        return jsonObject;
    }

    private TextWatcher textChangeListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            textViewReasonCount.setText("0/50");

            if(s.toString().length() > 0) {
                StringBuilder sb = new StringBuilder(String.valueOf(s.toString().length()));
                sb.append("/50");
                textViewReasonCount.setText(sb);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {}
    };

    private void setLayout() {
        buttonBack = findViewById(R.id.buttonBack);
        buttonCancel = findViewById(R.id.cancel);
        buttonRequest = findViewById(R.id.buttonRequest);

        textViewTitle = findViewById(R.id.textViewTitle);
        textViewProductTitle = findViewById(R.id.textViewProductTitle);
        textViewRequestProduct = findViewById(R.id.textViewRequestProduct);
        textViewOption = findViewById(R.id.textViewOption);
        textViewPrice = findViewById(R.id.textViewPrice);
        textViewDetailInfo = findViewById(R.id.textViewDetailInfo);
        textViewReason = findViewById(R.id.textViewReason);
        textViewWarning = findViewById(R.id.textViewWarning);
        textViewRequestMethod = findViewById(R.id.textViewRequestMethod);
        textViewRefundPrice = findViewById(R.id.textViewRefundPrice);
        textViewCancelPrice = findViewById(R.id.textViewCancelPrice);
        textViewMethod = findViewById(R.id.textViewMethod);
        textViewReasonCount = findViewById(R.id.textViewReasonCount);
        textViewDeliveryFee1 = findViewById(R.id.textViewDeliveryFee1);
        textViewDeliveryFee2 = findViewById(R.id.textViewDeliveryFee2);

        viewForCancel = findViewById(R.id.layoutForCancel);
        viewForRefund = findViewById(R.id.layoutForRefund);
        viewForMethod = findViewById(R.id.layoutForMethod);
        viewForExchange = findViewById(R.id.layoutForExchange);
        viewForExchangeWarning = findViewById(R.id.layoutForExchangeWarning);
        viewForCancelWarning = findViewById(R.id.layoutForCancelWarning);
        viewForRefundWarning = findViewById(R.id.layoutForRefundWarning);
        viewForCancelPrice = findViewById(R.id.layoutForCancelPrice);

        progressBar = findViewById(R.id.progressBar);
        editTextReasonDetail = findViewById(R.id.editTextReasonDetail);
        editTextReasonDetail.addTextChangedListener(textChangeListener);

        spinnerText = findViewById(R.id.spinnerText);

//        textViewTitle.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                tempSpinnerShow();
//            }
//        });
    }
}
