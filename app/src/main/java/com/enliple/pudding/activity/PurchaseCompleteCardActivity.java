package com.enliple.pudding.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.enliple.pudding.AbsBaseActivity;
import com.enliple.pudding.R;
import com.enliple.pudding.commons.app.NetworkStatusUtils;
import com.enliple.pudding.commons.app.Utils;
import com.enliple.pudding.commons.chat.ChatManager;
import com.enliple.pudding.commons.log.Logger;
import com.enliple.pudding.fragment.main.ShoppingLiveFragment;
import com.igaworks.v2.abxExtensionApi.AbxCommerce;
import com.igaworks.v2.abxExtensionApi.AbxCommon;
import com.igaworks.v2.core.AdBrixRm;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PurchaseCompleteCardActivity extends AbsBaseActivity implements View.OnClickListener {
    private RelativeLayout buttonBack;
    private TextView textViewProductPrice;  // 상품금액
    private TextView textViewOrderNumber;   // 주문번호
    private TextView textViewTotalDeliveryPrice;    // 배송비
    private TextView textViewPoint; // 포인트
    private TextView textViewTotalPayPrice; // 총 결제금액
    private TextView textViewPointToEarn;   // 적립 예정 포인트
    private LinearLayout buttonOrderDetail;   // 주문 상세보기 버튼
    private Button goShopping;
    private RelativeLayout savePointLayer, deliveryLayer, productPriceLayer;
    private double mGoodsPrice, mDeliveryPrice, mTotalPrice, mSavePoint;
    private String mUsedPoint, mOrderNumber, mLOrderNumber, payment;
    private int numOfItems;
    private boolean isOnlyPoint = false;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase_complete_card);

        initViews();
    }

    private void initViews() {
        buttonBack = findViewById(R.id.buttonBack);
        savePointLayer = findViewById(R.id.savePointLayer);
        deliveryLayer = findViewById(R.id.deliveryLayer);
        productPriceLayer = findViewById(R.id.productPriceLayer);

        textViewProductPrice = (TextView) findViewById(R.id.textViewProductPrice);
        textViewOrderNumber = (TextView) findViewById(R.id.textViewOrderNumber);
        textViewTotalDeliveryPrice = (TextView) findViewById(R.id.textViewTotalDeliveryPrice);
        textViewPoint = (TextView) findViewById(R.id.textViewPoint);
        textViewTotalPayPrice = (TextView) findViewById(R.id.textViewTotalPayPrice);
        textViewPointToEarn = (TextView) findViewById(R.id.textViewPointToEarn);
        buttonOrderDetail = (LinearLayout) findViewById(R.id.buttonOrderDetail);
        goShopping = findViewById(R.id.goShopping);

        Intent intent = getIntent();
        if (intent != null) {
            isOnlyPoint = intent.getBooleanExtra(PurchaseActivity.EXTRA_ONLY_POINT, false);
            mGoodsPrice = intent.getDoubleExtra(PurchaseActivity.EXTRA_PRODUCT_PRICE, 0);
            mDeliveryPrice = intent.getDoubleExtra(PurchaseActivity.EXTRA_DELIVERY_PRICE, 0);
            mTotalPrice = intent.getDoubleExtra(PurchaseActivity.EXTRA_TOTAL_PRRICE, 0);
            mSavePoint = intent.getDoubleExtra(PurchaseActivity.EXTRA_SAVE_POINT, 0);
            numOfItems = intent.getIntExtra(PurchaseActivity.EXTRA_NUM_ITEMS, 0);
            payment = intent.getStringExtra(PurchaseActivity.EXTRA_PAYMENT);

            String used_point = intent.getStringExtra(PurchaseActivity.EXTRA_USED_POINT);
            Logger.e("mGoodsPrice :: " + mGoodsPrice);
            Logger.e("mDeliveryPrice :: " + mDeliveryPrice);
            Logger.e("mTotalPrice :: " + mTotalPrice);
            Logger.e("mSavePoint :: " + mSavePoint);
            Logger.e("numOfItems :: " + numOfItems);
            Logger.e("used_point :: " + used_point);
            Logger.e("payment :: " + payment);
            if ( !TextUtils.isEmpty(used_point) ) {
                if ( used_point.contains(",")) used_point = used_point.replaceAll(",", "");
                Logger.e("used_point 1 :: " + used_point);
                if ( used_point.contains("원")) used_point = used_point.replaceAll("원", "");
                Logger.e("used_point 2 :: " + used_point);
                if ( used_point.contains("-")) used_point = used_point.replaceAll("-", "");
                Logger.e("used_point 3 :: " + used_point);
                if ( used_point.contains(".0")) used_point = used_point.replaceAll("\\.0", "");

                Logger.e("used_point 4 :: " + used_point);
                if (  !TextUtils.isEmpty(used_point) ) {
                    if ("0".equals(used_point))
                        mUsedPoint = used_point.replaceAll("\\.0", "");
                    else
                        mUsedPoint = used_point.replaceAll("\\.0", "");
                } else {
                    mUsedPoint = "0";
                }
            } else {
                mUsedPoint = "0";
            }
            double val = 4000.0;
            try {
                double dUsedPoint = Double.parseDouble(mUsedPoint);
                if ( dUsedPoint > 0 )
                    mUsedPoint = "-" + Utils.ToNumFormat(dUsedPoint) + "원";
                else
                    mUsedPoint = Utils.ToNumFormat(dUsedPoint) + "원";
            } catch (Exception e) {
                e.printStackTrace();
            }

            mOrderNumber = intent.getStringExtra(PurchaseActivity.EXTRA_ORDER_NUMBER);
            mLOrderNumber = intent.getStringExtra(PurchaseActivity.EXTRA_L_ORDER_NUMBER);
            String s_goods_price = Utils.ToNumFormat(mGoodsPrice) + "원";
            String s_total_price = Utils.ToNumFormat(mTotalPrice) + "원";
            String s_save_point = Utils.ToNumFormat(mSavePoint) + "원";
            String s_delivery_price = Utils.ToNumFormat(mDeliveryPrice) + "원";

            try {
                AdBrixRm.CommerceProductModel productModel = new AdBrixRm.CommerceProductModel()
                        .setCurrency(AdBrixRm.Currency.KR_KRW)
                        .setPrice(mTotalPrice);

                ArrayList proList = new ArrayList();
                proList.add(productModel);

                AbxCommon.purchase("orderid_1", proList, 0.00, mDeliveryPrice, AdBrixRm.CommercePaymentMethod.CreditCard, new AdBrixRm.CommonProperties.Purchase());

            } catch (Exception e) {
                e.printStackTrace();
            }

            textViewOrderNumber.setText("주문번호 " + mOrderNumber);
            textViewProductPrice.setText(s_goods_price);
            textViewTotalDeliveryPrice.setText(s_delivery_price);
            textViewPoint.setText(mUsedPoint);
            textViewTotalPayPrice.setText(s_total_price);
            textViewPointToEarn.setText(s_save_point);

            buttonBack.setOnClickListener(this);
            buttonOrderDetail.setOnClickListener(this);
            goShopping.setOnClickListener(this);

            if ( isOnlyPoint ) {
                savePointLayer.setVisibility(View.GONE);
                deliveryLayer.setVisibility(View.GONE);
                productPriceLayer.setVisibility(View.GONE);
            } else {
                savePointLayer.setVisibility(View.VISIBLE);
                deliveryLayer.setVisibility(View.VISIBLE);
                productPriceLayer.setVisibility(View.VISIBLE);
            }

            Intent chatIntent = new Intent(ShoppingLiveFragment.SEND_CHAT);
            chatIntent.putExtra("GUBUN", ChatManager.GUBUN_BUY);
            chatIntent.putExtra("NAME", payment);
            sendBroadcast(chatIntent);

//            logPurchasedEvent(numOfItems, Constants.PARAM_CONTENT_TYPE, mOrderNumber, Constants.PARAM_CONTENT_CURRENCY, mTotalPrice);
        } else {
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_OK);
        finish();
    }

    /**
     * This function assumes logger is an instance of AppEventsLogger and has been
     * created using AppEventsLogger.newLogger() call.
     */
//    public void logPurchasedEvent (int numItems, String contentType, String contentId, String currency, double price) {
//        Bundle params = new Bundle();
//        params.putInt(AppEventsConstants.EVENT_PARAM_NUM_ITEMS, numItems);
//        params.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, contentType);
//        params.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, contentId);
//        params.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, currency);
//        AppEventsLogger.newLogger(BaseApplication.getGlobalApplicationContext())
//                .logPurchase(BigDecimal.valueOf(price), Currency.getInstance(currency),params);
//    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonBack:
//                onBackPressed();
                setResult(RESULT_OK);
                finish();
                break;
            case R.id.buttonOrderDetail:
                Intent intent = new Intent(PurchaseCompleteCardActivity.this, PurchaseHistoryDetailActivity.class);
                intent.putExtra(PurchaseHistoryDetailActivity.INTENT_EXTRA_KEY_ORDER_NUMBER, mLOrderNumber);
                startActivity(intent);
                finish();
                break;
            case R.id.goShopping:
                setResult(RESULT_OK);
                finish();
                break;
        }
    }

    @Override
    public void onNetworkStatusChanged(@NotNull NetworkStatusUtils.NetworkStatus status) {

    }

    @Override
    public void onDozeModeStateChanged(boolean dozeEnable) {

    }
}
