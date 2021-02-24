package com.enliple.pudding.activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.enliple.pudding.AbsBaseActivity;
import com.enliple.pudding.R;
import com.enliple.pudding.commons.app.NetworkStatusUtils;
import com.enliple.pudding.commons.app.ShopTreeKey;
import com.enliple.pudding.commons.internal.AppPreferences;
import com.enliple.pudding.commons.log.Logger;
import com.enliple.pudding.commons.network.NetworkApi;
import com.enliple.pudding.commons.network.NetworkBus;
import com.enliple.pudding.commons.network.NetworkBusResponse;
import com.enliple.pudding.commons.network.NetworkHandler;
import com.enliple.pudding.commons.widget.toast.AppToast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class MobonWebActivity extends AbsBaseActivity {
    public static final String INTENT_EXTRA_KEY_LINK = "LINK";

    private RelativeLayout buttonBack, buttonZzim;
    private AppCompatTextView main_title, goBroadcast;
    private AppCompatImageView imageViewZzim;
    private WebView webView;

    private String siteUrl = "";
    private String idx = "";
    private String name = "";
    private String price = "";
    private String image = "";
    private String storeName = "";
    private String pCode = "";
    private String scCode = "";
    private String zzimStatus = "N";
    private boolean isLoginUser = false;
    private String strType = "1";
    @Override
    public void onCreate(Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mobon_webview_activity);
        EventBus.getDefault().register(this);
        buttonBack = findViewById(R.id.buttonBack);
        buttonZzim = findViewById(R.id.buttonZzim);
        main_title = findViewById(R.id.main_title);
        goBroadcast = findViewById(R.id.goBroadcast);
        imageViewZzim = findViewById(R.id.imageViewZzim);
        webView = findViewById(R.id.webView);

        goBroadcast.setOnClickListener(clickListener);
        buttonBack.setOnClickListener(clickListener);
        buttonZzim.setOnClickListener(clickListener);

        WebSettings webSetting = webView.getSettings();
        webSetting.setJavaScriptEnabled(true);
        webSetting.setJavaScriptCanOpenWindowsAutomatically(true);
        webSetting.setSupportMultipleWindows(true);
        webSetting.setDomStorageEnabled(true);
        webSetting.setUseWideViewPort(true);
        webSetting.setLoadWithOverviewMode(true);
        webSetting.setAllowUniversalAccessFromFileURLs(true);
        webSetting.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        webSetting.setSupportZoom(true);
        //webSetting.setBuiltInZoomControls(true);
        //webSetting.setCacheMode(WebSettings.LOAD_NO_CACHE);

        //CookieManager cookieManager = CookieManager.getInstance();
        //cookieManager.setAcceptCookie(true);
        //cookieManager.setAcceptThirdPartyCookies(webView, true);

        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
//        webView.setWebChromeClient(new MyWebChromeClient());
//        webView.setWebViewClient(new MyWebViewClient());

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {

            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                Logger.e("onReceivedError :: error :: " + error.toString());
            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                Logger.e("onReceivedHttpError :: error :: " + errorResponse.toString());
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                Logger.e("onReceivedSslError :: error :: " + error.toString());
            }
        });

        Intent intent = getIntent();
        if (intent == null) {
            Logger.e("intent null error!!  finish activity");
            finish();
            return;
        }
        strType = intent.getStringExtra("strType");
        siteUrl = intent.getStringExtra(INTENT_EXTRA_KEY_LINK);
        idx = intent.getStringExtra("idx");
        name = intent.getStringExtra("name");
        price = intent.getStringExtra("price");
        image = intent.getStringExtra("image");
        storeName = intent.getStringExtra("storeName");
        pCode = intent.getStringExtra(ShopTreeKey.KEY_PCODE);
        scCode = intent.getStringExtra(ShopTreeKey.KEY_SCCODE);
        zzimStatus = intent.getStringExtra("zzim_status");

        if ( "Y".equals(zzimStatus) ) {
            imageViewZzim.setSelected(true);
            imageViewZzim.setBackgroundResource(R.drawable.like_on_ico);
        } else {
            imageViewZzim.setSelected(false);
            imageViewZzim.setBackgroundResource(R.drawable.like_off_ico);
        }

        if (TextUtils.isEmpty(siteUrl)) {
            Logger.e("siteUrl null error ");
            finish();
            return;
        }

        Logger.e("onCreate url:" + siteUrl);

        if (siteUrl.startsWith("https://") || siteUrl.startsWith("http://")) {
            webView.loadUrl(siteUrl);
        } else {
            siteUrl = "https://" + siteUrl;
            webView.loadUrl(siteUrl);
        }

        main_title.setText(name);
    }

    @Override
    public void onResume() {
        super.onResume();
        isLoginUser = AppPreferences.Companion.getLoginStatus(MobonWebActivity.this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        AppToast.Companion.cancelAllToast();
    }

    @Override
    public void onBackPressed() {
        Logger.e("onBackPressed");
        Intent intent = getIntent();
        intent.putExtra("idx", idx);
        intent.putExtra("LIKE_STATUS", imageViewZzim.isSelected());
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId() ) {
                case R.id.buttonZzim:
                    processZzimStatus(!imageViewZzim.isSelected());
                    break;
                case R.id.buttonBack:
                    Intent intent1 = new Intent();
                    intent1.putExtra("idx", idx);
                    intent1.putExtra("LIKE_STATUS", imageViewZzim.isSelected());
                    setResult(RESULT_CANCELED, intent1);
                    finish();
                    break;
                case R.id.goBroadcast:
                    Intent intent = new Intent();
                    intent.putExtra("idx", idx);
                    intent.putExtra("name", name);
                    intent.putExtra("price", price);
                    intent.putExtra("image", image);
                    intent.putExtra("storeName", storeName);
                    intent.putExtra("strType", strType);
                    intent.putExtra(ShopTreeKey.KEY_PCODE, pCode);
                    intent.putExtra(ShopTreeKey.KEY_SCCODE, scCode);
                    setResult(RESULT_OK, intent);
                    finish();
                    break;
            }
        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(NetworkBusResponse data) {
        String api126 = NetworkHandler.Companion.getInstance(this)
                .getKey(NetworkApi.API126.toString(), idx, "");
        Logger.e("data_arg1 :: "  + data.arg1);
        Logger.e("api126 :: " + api126);
        if(data.arg1.equals(api126)) {
            if("ok".equals(data.arg2)) {
                if ( imageViewZzim.isSelected() )
                    imageViewZzim.setBackgroundResource(R.drawable.like_off_ico);
                else
                    imageViewZzim.setBackgroundResource(R.drawable.like_on_ico);
                imageViewZzim.setSelected(!imageViewZzim.isSelected());
            } else {
                try {
                    JSONObject response = new JSONObject(data.arg4);
                    new AppToast(this).showToastMessage(response.getString("message"),
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_BOTTOM);
                } catch (Exception e) {
                    Logger.p(e);
                }
            }
        }
    }

    private void processZzimStatus(boolean status) {
        if ( isLoginUser ) {
            try {
                JSONObject obj = new JSONObject();
                obj.put("user", AppPreferences.Companion.getUserId(this));
                obj.put("is_wish" , status == true ? "Y" : "N");
                obj.put("type", "1");

                RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), obj.toString());
                NetworkBus bus = new NetworkBus(NetworkApi.API126.name(), idx, body);
                EventBus.getDefault().post(bus);
            } catch (Exception e) {
                Logger.p(e);
            }
        } else {
            goLogin();
        }
    }

    private void goLogin() {
        startActivity(new Intent(MobonWebActivity.this, LoginActivity.class));
    }

    @Override
    public void onNetworkStatusChanged(@NotNull NetworkStatusUtils.NetworkStatus status) {

    }

    @Override
    public void onDozeModeStateChanged(boolean dozeEnable) {

    }
}
