package com.enliple.pudding.activity;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Browser;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.enliple.pudding.AbsBaseActivity;
import com.enliple.pudding.R;
import com.enliple.pudding.commons.log.Logger;
import com.enliple.pudding.commons.app.NetworkStatusUtils;
import com.enliple.pudding.commons.shoptree.data.PaymentScheme;

import org.jetbrains.annotations.NotNull;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class PaymentActivity extends AbsBaseActivity implements View.OnClickListener {
    public final static int REQUEST_GO_COMPLETE = 9711;
    public final static String INTENT_KEY_PAYMENT_TYPE = "PaymentActivity.INTENT_KEY_PAYMENT_TYPE";
    public final static String INTENT_KEY_PAYMENT_CARD = "PaymentActivity.INTENT_KEY_PAYMENT_CARD";
    public final static String INTENT_KEY_PAYMENT_BANK_TRANSFER = "PaymentActivity.INTENT_KEY_PAYMENT_BANK_TRANSFER";
    public final static String INTENT_KEY_PAYMENT_NO_BANKBOOK = "PaymentActivity.INTENT_KEY_PAYMENT_NO_BANKBOOK";
    public final static String INTENT_KEY_PAYMENT_VIRTUAL_ACCOUNT = "PaymentActivity.INTENT_KEY_PAYMENT_VIRTUAL_ACCOUNT";
    public final static String INTENT_KEY_PAYMENT_MOBILE = "PaymentActivity.INTENT_KEY_PAYMENT_MOBILE";
    public final static String PAYMENT_TYPE_C = "C"; //카드
    public final static String PAYMENT_TYPE_M = "M"; //계좌이체
    public final static String PAYMENT_TYPE_B = "B"; //무통장

    // 결제 관련 Parameter
    public final static String INTENT_KEY_PAYMENT_MID = "PaymentActivity.INTENT_KEY_PAYMENT_MID";
    public final static String INTENT_KEY_PAYMENT_AMT = "PaymentActivity.INTENT_KEY_PAYMENT_AMT";
    public final static String INTENT_KEY_PAYMENT_OID = "PaymentActivity.INTENT_KEY_PAYMENT_OID";
    public final static String INTENT_KEY_PAYMENT_UNAME = "PaymentActivity.INTENT_KEY_PAYMENT_UNAME";
    public final static String INTENT_KEY_PAYMENT_GOODS = "PaymentActivity.INTENT_KEY_PAYMENT_GOODS";
    public final static String INTENT_KEY_PAYMENT_NOTI_URL = "PaymentActivity.INTENT_KEY_PAYMENT_NOTI_URL";
    public final static String INTENT_KEY_PAYMENT_RETURN_URL = "PaymentActivity.INTENT_KEY_PAYMENT_RETURN_URL";
    public final static String INTENT_KEY_PAYMENT_NEXT_URL = "PaymentActivity.INTENT_KEY_PAYMENT_NEXT_URL";

    public final static String PAYMENT_CARD_URL = "https://mobile.inicis.com/smart/wcard/";      // 신용카드
    public final static String PAYMENT_BANK_TRANSFER_URL = "https://mobile.inicis.com/smart/bank/";       // 계좌이체
    public final static String PAYMENT_VIRTUAL_ACCOUNT_URL = "https://mobile.inicis.com/smart/vbank/";      // 가상계좌
    public final static String PAYMENT_MOBILE = "https://mobile.inicis.com/smart/mobile/";   // 휴대폰결제

    private static final String JAVA_SCRIPT_INTERFACE_NAME = "PayResultInterface";

    private RelativeLayout buttonBack;
    private WebView mainWebView;

    String paymentType, payment;
    String pMid, pAmt, pOid, pUname, pGoods, pNextUrl, pNotiUrl, pReturnUrl, lOid;
    private PayInterface mInterface = new PayInterface();
    private double mGoodsPrice, mDeliveryPrice, mTotalPrice, mSavePoint;
    private String mUsedPoint;
    private int numOfItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        buttonBack = findViewById(R.id.buttonBack);
        mainWebView = (WebView) findViewById(R.id.mainWebView);


        WebSettings settings = mainWebView.getSettings();
        settings.setJavaScriptEnabled(true);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);
            cookieManager.setAcceptThirdPartyCookies(mainWebView, true);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            settings.setSafeBrowsingEnabled(false);
        }

//        mainWebView.setWebViewClient(new InicisWebViewClient(this, mainWebView));
        MyWebViewClient client = new MyWebViewClient();
        mainWebView.setWebViewClient(client);

        mainWebView.addJavascriptInterface(mInterface, JAVA_SCRIPT_INTERFACE_NAME);
        buttonBack.setOnClickListener(this);

        Intent intent = getIntent();
        loadData(intent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        loadData(intent);
    }

    @Override
    public void onNetworkStatusChanged(@NotNull NetworkStatusUtils.NetworkStatus status) {

    }

    @Override
    public void onDozeModeStateChanged(boolean dozeEnable) {

    }

    private class MyWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Intent intent = null;
            try {
                Logger.e("shouldOverrideUrlLoading url :: " + url);
                if (url != null && url.startsWith("intent")) {
                    try {
                        intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                        Intent existPackage = getPackageManager().getLaunchIntentForPackage(intent.getPackage());
                        if (existPackage != null) {
                            startActivity(intent);
                        } else {
                            Intent marketIntent = new Intent(Intent.ACTION_VIEW);
                            marketIntent.setData(Uri.parse("market://details?id=" + intent.getPackage()));
                            startActivity(marketIntent);
                        }
                        return true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (url != null && url.startsWith("market://")) {
                    try {
                        intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                        if (intent != null) {
                            startActivity(intent);
                        }
                        return true;
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                } else if (url != null && url.startsWith("ispmobile://")) {
                    boolean isatallFlag = isPackageInstalled(PaymentActivity.this, "kvp.jjy.MispAndroid320");
                    if (isatallFlag) {
                        intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        intent.addCategory(Intent.CATEGORY_BROWSABLE);
                        intent.putExtra(Browser.EXTRA_APPLICATION_ID, getPackageName());
                        try {
                            startActivity(intent);
                        } catch (ActivityNotFoundException ex) {
                        }
                    } else {
                        Intent marketIntent = new Intent(Intent.ACTION_VIEW);
                        marketIntent.setData(Uri.parse("market://details?id=kvp.jjy.MispAndroid320"));
                        startActivity(marketIntent);
                    }
                } else if (url != null && url.startsWith("kftc-bankpay://")) {
                    Logger.e("tftc-bankpay:// started");
                    boolean isatallFlag = isPackageInstalled(PaymentActivity.this, "com.kftc.bankpay.android");
                    if (isatallFlag) {
                        Logger.e("isatallFlag true");
                        intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        intent.addCategory(Intent.CATEGORY_BROWSABLE);
                        intent.putExtra(Browser.EXTRA_APPLICATION_ID, getPackageName());
                        try {
                            startActivity(intent);
                        } catch (ActivityNotFoundException ex) {
                            ex.printStackTrace();
                        }
                    } else {
                        Logger.e("isatallFlag false");
                        Intent marketIntent = new Intent(Intent.ACTION_VIEW);
                        marketIntent.setData(Uri.parse("market://details?id=com.kftc.bankpay.android"));
                        startActivity(marketIntent);
                    }
                }
            } catch (ActivityNotFoundException e) {
                Logger.e("activity not found");
                e.printStackTrace();
                if (intent == null) return false;

                if (handleNotFoundPaymentScheme(view, intent.getScheme())) return true;

                String packageName = intent.getPackage();
                Logger.d("shouldOverrideUrlLoading packageName :::: " + packageName);
                if (packageName != null) {
                    Logger.e("packageName not null");
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
                    return true;
                }

                return false;
            }
            return false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void onClick(View v) {
        long viewId = v.getId();
        if (viewId == R.id.buttonBack) {
            onBackPressed();
        }
    }

    private void loadData(Intent intent) {
        if (intent != null) {

            if (PurchaseActivity.mActivity != null)
                PurchaseActivity.mActivity.finish();
            // TODO :: 이전에 열려있던 창을 닫으려면 이 부분이 수정되어야 함
//            if ( ProductCartActivity.mActivity != null )
//                ProductCartActivity.mActivity.finish();
//
//            if ( ShopTreeProductActivity.mActivity != null )
//                ShopTreeProductActivity.mActivity.finish();

            paymentType = intent.getStringExtra(INTENT_KEY_PAYMENT_TYPE);
            pMid = intent.getStringExtra(INTENT_KEY_PAYMENT_MID);
            pAmt = intent.getStringExtra(INTENT_KEY_PAYMENT_AMT);
            pOid = intent.getStringExtra(INTENT_KEY_PAYMENT_OID);
            pUname = intent.getStringExtra(INTENT_KEY_PAYMENT_UNAME);
            pGoods = intent.getStringExtra(INTENT_KEY_PAYMENT_GOODS);
            pNextUrl = intent.getStringExtra(INTENT_KEY_PAYMENT_NEXT_URL);
            pReturnUrl = intent.getStringExtra(INTENT_KEY_PAYMENT_RETURN_URL);
            pNotiUrl = intent.getStringExtra(INTENT_KEY_PAYMENT_NOTI_URL);
            lOid = intent.getStringExtra(PurchaseActivity.EXTRA_L_ORDER_NUMBER);
            if (paymentType.equals(INTENT_KEY_PAYMENT_CARD)) {
                mainWebView.postUrl(PAYMENT_CARD_URL, makeParam(paymentType));
            } else if (paymentType.equals(INTENT_KEY_PAYMENT_BANK_TRANSFER)) {
                mainWebView.postUrl(PAYMENT_BANK_TRANSFER_URL, makeParam(paymentType));
            } else if (paymentType.equals(INTENT_KEY_PAYMENT_VIRTUAL_ACCOUNT)) {
                mainWebView.postUrl(PAYMENT_VIRTUAL_ACCOUNT_URL, makeParam(paymentType));
            } else if (paymentType.equals(INTENT_KEY_PAYMENT_MOBILE)) {
                Logger.e("load mobile");
                mainWebView.postUrl(PAYMENT_MOBILE, makeParam(paymentType));
            }

            mGoodsPrice = intent.getDoubleExtra(PurchaseActivity.EXTRA_PRODUCT_PRICE, 0);
            mDeliveryPrice = intent.getDoubleExtra(PurchaseActivity.EXTRA_DELIVERY_PRICE, 0);
            mTotalPrice = intent.getDoubleExtra(PurchaseActivity.EXTRA_TOTAL_PRRICE, 0);
            mSavePoint = intent.getDoubleExtra(PurchaseActivity.EXTRA_SAVE_POINT, 0);
            mUsedPoint = intent.getStringExtra(PurchaseActivity.EXTRA_USED_POINT);
            numOfItems = intent.getIntExtra(PurchaseActivity.EXTRA_NUM_ITEMS, 0);
            payment = intent.getStringExtra(PurchaseActivity.EXTRA_PAYMENT);
            Logger.d("PaymentActivity paymentType :: " + paymentType);
            Logger.d("PaymentActivity pMid :: " + pMid);
            Logger.d("PaymentActivity pAmt :: " + pAmt);
            Logger.d("PaymentActivity pOid :: " + pOid);
            Logger.d("PaymentActivity pUname :: " + pUname);
            Logger.d("PaymentActivity pGoods :: " + pGoods);
            Logger.d("PaymentActivity pNextUrl :: " + pNextUrl);
            Logger.d("PaymentActivity pReturnUrl :: " + pReturnUrl);
            Logger.d("PaymentActivity pNotiUrl :: " + pNotiUrl);
            Logger.d("PaymentActivity mGoodsPrice :: " + mGoodsPrice);
            Logger.d("PaymentActivity mDeliveryPrice :: " + mDeliveryPrice);
            Logger.d("PaymentActivity mTotalPrice :: " + mTotalPrice);
            Logger.d("PaymentActivity mSavePoint :: " + mSavePoint);
            Logger.d("PaymentActivity mUsedPoint :: " + mUsedPoint);
            Logger.d("PaymentActivity numOfItems :: " + numOfItems);
            Logger.d("PaymentActivity payment :: " + payment);
        }
    }

    private byte[] makeParam(String paymentType) {
        String param;

        if (paymentType.equals(INTENT_KEY_PAYMENT_CARD)) {
            Logger.d("카드 선택");
            try {
                param = "P_MID=" + URLEncoder.encode(pMid, "EUC-KR") +
                        "&P_AMT=" + URLEncoder.encode(pAmt, "EUC-KR") +
                        "&P_OID=" + URLEncoder.encode(pOid, "EUC-KR") +
                        "&P_UNAME=" + URLEncoder.encode(pUname, "EUC-KR") +
                        "&P_GOODS=" + URLEncoder.encode(pGoods, "EUC-KR") +
                        "&P_RESERVED=" + URLEncoder.encode("twotrs_isp=Y&block_isp=Y&twotrs_isp_noti=N&apprun_check=Y", "EUC-KR") +
                        "&P_NEXT_URL=" + URLEncoder.encode(pNextUrl, "EUC-KR");

                return param.getBytes();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return null;
            }
        } else if (paymentType.equals(INTENT_KEY_PAYMENT_BANK_TRANSFER)) {
            Logger.d("계좌이체 선택");
            try {
                param = "P_MID=" + URLEncoder.encode(pMid, "EUC-KR") +
                        "&P_AMT=" + URLEncoder.encode(pAmt, "EUC-KR") +
                        "&P_OID=" + URLEncoder.encode(pOid, "EUC-KR") +
                        "&P_UNAME=" + URLEncoder.encode(pUname, "EUC-KR") +
                        "&P_GOODS=" + URLEncoder.encode(pGoods, "EUC-KR") +
                        "&P_RESERVED=" + URLEncoder.encode("twotrs_bank=Y&apprun_check=Y", "EUC-KR") +
                        "&P_NEXT_URL=" + URLEncoder.encode(pNextUrl, "EUC-KR") +
                        "&P_NOTI_URL=" + URLEncoder.encode(pNotiUrl, "EUC-KR") +
                        "&P_RETURN_URL=" + URLEncoder.encode(pReturnUrl, "EUC-KR");

                Logger.e("param :: " + param);
                return param.getBytes();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return null;
            }
        } else if (paymentType.equals(INTENT_KEY_PAYMENT_VIRTUAL_ACCOUNT)) {
            Logger.d("가상계좌 선택");
            //                param = "P_MID=" + URLEncoder.encode(pMid, "EUC-KR") +
//                        "&P_AMT=" + URLEncoder.encode(pAmt, "EUC-KR") +
//                        "&P_OID=" + URLEncoder.encode(pOid, "EUC-KR") +
//                        "&P_UNAME=" + URLEncoder.encode(pUname, "EUC-KR") +
//                        "&P_GOODS=" + URLEncoder.encode(pGoods, "EUC-KR") +
//                        "&P_NOTI_URL=" + URLEncoder.encode(pNotiUrl, "EUC-KR") +
//                        "&P_NEXT_URL=" + URLEncoder.encode(pNextUrl, "EUC-KR");
            param = "P_MID=" + pMid +
                    "&P_AMT=" + pAmt +
                    "&P_OID=" + pOid +
                    "&P_UNAME=" + pUname +
                    "&P_GOODS=" + pGoods +
                    "&P_NOTI_URL=" + pNotiUrl +
                    "&P_NEXT_URL=" + pNextUrl;
            return param.getBytes();
        } else if (paymentType.equals(INTENT_KEY_PAYMENT_MOBILE)) {
            Logger.d("휴대폰결제 선택");
            try {
                param = "P_MID=" + URLEncoder.encode(pMid, "UTF-8") +
                        "&P_OID=" + URLEncoder.encode(pOid, "UTF-8") +
                        "&P_AMT=" + URLEncoder.encode(pAmt, "UTF-8") +
                        "&P_UNAME=" + URLEncoder.encode(pUname, "UTF-8") +
                        "&P_GOODS=" + URLEncoder.encode(pGoods, "UTF-8") +
                        "&P_HPP_METHOD=" + URLEncoder.encode("2", "UTF-8") +  // 1일 경우 컨텐츠 2일 경우 실물
                        "&P_NEXT_URL=" + URLEncoder.encode(pNextUrl, "UTF-8");


                Logger.e("param.toString :: " + param.toString());

                return param.getBytes();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    protected boolean handleNotFoundPaymentScheme(WebView view, String scheme) {
        Logger.e("handleNotFoundPaymentScheme called :: scheme :: " + scheme);
        //PG사에서 호출하는 url에 package정보가 없어 ActivityNotFoundException이 난 후 market 실행이 안되는 경우
        if (PaymentScheme.ISP.equalsIgnoreCase(scheme)) {
            Logger.d("isp package");
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + PaymentScheme.PACKAGE_ISP)));
            return true;
        } else if (PaymentScheme.BANKPAY.equalsIgnoreCase(scheme)) {
            Logger.d("bank pay package");
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + PaymentScheme.PACKAGE_BANKPAY)));
            return true;
        } else {
            Logger.d("handleNotFoundPaymentScheme else");
        }

        return false;
    }

    public static boolean isPackageInstalled(Context ctx, String pkgName) {
        try {
            ctx.getPackageManager().getPackageInfo(pkgName, PackageManager.GET_ACTIVITIES);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private class PayInterface {
        @JavascriptInterface
        public void payResult(String result, String msg) {
            Logger.d("payResult result :: " + result);
            if ("true".equals(result)) {
                Logger.e("payResult payment :: " + payment );
                Intent intent = new Intent(PaymentActivity.this, PurchaseCompleteCardActivity.class);
                intent.putExtra(PurchaseActivity.EXTRA_ORDER_NUMBER, pOid);
                intent.putExtra(PurchaseActivity.EXTRA_L_ORDER_NUMBER, lOid);
                intent.putExtra(PurchaseActivity.EXTRA_PRODUCT_PRICE, mGoodsPrice);
                intent.putExtra(PurchaseActivity.EXTRA_DELIVERY_PRICE, mDeliveryPrice);
                intent.putExtra(PurchaseActivity.EXTRA_USED_POINT, mUsedPoint);
                intent.putExtra(PurchaseActivity.EXTRA_TOTAL_PRRICE, mTotalPrice);
                intent.putExtra(PurchaseActivity.EXTRA_SAVE_POINT, mSavePoint);
                intent.putExtra(PurchaseActivity.EXTRA_NUM_ITEMS, numOfItems);
                intent.putExtra(PurchaseActivity.EXTRA_PAYMENT, payment);
                startActivityForResult(intent, REQUEST_GO_COMPLETE);
                finish();
//                CommonRequest.getInstance().sendOrderNumber(pOid,
//                        (Object response) -> {
//                            try {
//                                JSONObject object = new JSONObject((String) response);
//                                Logger.d("sendOrderNumber result :::: " + object.toString());
//                                String rt =  object.optString("Result");
//                                Logger.d("rt :: " + rt);
//                                if ( "success".equals(rt) ) {
//                                    Intent intent = new Intent(PaymentActivity.this, PurchaseCompleteCardActivity.class);
//                                    intent.putExtra(PurchaseActivity.EXTRA_ORDER_NUMBER, pOid);
//                                    intent.putExtra(PurchaseActivity.EXTRA_PRODUCT_PRICE, mGoodsPrice);
//                                    intent.putExtra(PurchaseActivity.EXTRA_DELIVERY_PRICE, mDeliveryPrice);
//                                    intent.putExtra(PurchaseActivity.EXTRA_USED_POINT, mUsedPoint);
//                                    intent.putExtra(PurchaseActivity.EXTRA_TOTAL_PRRICE, mTotalPrice);
//                                    intent.putExtra(PurchaseActivity.EXTRA_SAVE_POINT, mSavePoint);
//                                    startActivity(intent);
//                                    finish();
//                                }
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                        },
//                        (VolleyError error) -> {
//                        }
//                );
            } else {
                try {
                    String message = URLDecoder.decode(msg, "utf-8");
                    Toast.makeText(PaymentActivity.this, message, Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                finish();
            }
//            Toast.makeText(PaymentActivity.this, "result is :: " + result  + " , msg :: " + msg, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ( requestCode == REQUEST_GO_COMPLETE && resultCode == RESULT_OK ) {
            setResult(RESULT_OK);
            finish();
        }
    }
}
