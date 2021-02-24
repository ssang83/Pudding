package com.enliple.pudding.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.enliple.pudding.AbsBaseActivity;
import com.enliple.pudding.R;
import com.enliple.pudding.bus.ZzimStatusBus;
import com.enliple.pudding.commons.app.NetworkStatusUtils;
import com.enliple.pudding.commons.db.DBManager;
import com.enliple.pudding.commons.internal.AppPreferences;
import com.enliple.pudding.commons.log.Logger;
import com.enliple.pudding.commons.network.NetworkApi;
import com.enliple.pudding.commons.network.NetworkBus;
import com.enliple.pudding.commons.network.NetworkBusResponse;
import com.enliple.pudding.commons.network.NetworkHandler;
import com.enliple.pudding.commons.network.vo.API138;
import com.enliple.pudding.commons.network.vo.API73;
import com.enliple.pudding.commons.network.vo.BaseAPI;
import com.enliple.pudding.commons.shoptree.network.ShopTreeAsyncTask;
import com.enliple.pudding.commons.widget.toast.AppToast;
import com.enliple.pudding.shoppingcaster.activity.CoverSelectActivity;
import com.enliple.pudding.widget.LinkProductDialog;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class LinkWebViewActivity extends AbsBaseActivity implements LinkProductDialog.Listener {
    public static final String INTENT_EXTRA_KEY_LINK = "LINK";
    public static final String INTENT_EXTRA_KEY_SHOP_NAME = "shop_name";
    public static final String INTENT_EXTRA_KEY_SC_CODE = "sc_code";
    public static final String INTENT_EXTRA_KEY_FROM_CASTING = "from_casting";

    private static final int REQUEST_CODE_IMAGE_PICK = 0xBC02;

    private WebView webView;
    private AppCompatImageButton buttonGoBack, buttonGoFowrad, buttonLinkCopy, buttonClose;
    private AppCompatTextView title;
    private RelativeLayout layoutButton, layoutTitle, buttonBack, buttonZzim;
    private AppCompatImageView imageViewZzim;
    private ProgressBar progressBar;

    private String siteUrl = "";
    private String loadUrl = "";
    private String scCode = "";
    private String shopName = "";
    private String imageUrl = "";
    private String linkId = "";
    private String strTitle = "";
    private String productTitle = "";
    private String price = "";
    private Uri productImg = null;
    private boolean fromCasting = false;

    private LinkProductDialog productDialog = null;
    private Dialog mDialog = null;

    private boolean mIsBuyMode = false;

    private boolean isLoginUser = false;
    private String idx = "";
    private String is_wish = "N";
    private String type = "1";
    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Logger.d("shouldOverrideUrlLoading:" + url);

            loadUrl = url;
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            Logger.d("onPageFinished:" + url);
        }
    }

    private class MyWebChromeClient extends WebChromeClient {
        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
            Logger.d("onCreateWindow");

            WebView newWebView = new WebView(LinkWebViewActivity.this);
            WebSettings webSettings = newWebView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setSupportMultipleWindows(true);
            webSettings.setJavaScriptCanOpenWindowsAutomatically(true);

            mDialog = new Dialog(LinkWebViewActivity.this);
            mDialog.setContentView(newWebView);
            mDialog.show();
            mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    Logger.d("onCancel");

                    onCancelDialog();
                }
            });

            ((WebView.WebViewTransport) resultMsg.obj).setWebView(newWebView);
            resultMsg.sendToTarget();
            return true;
        }

        @Override
        public void onCloseWindow(WebView window) {
            Logger.d("onCloseWindow");

            if (mDialog != null) {
                mDialog.dismiss();
                mDialog = null;
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.e("onCreate");

        setContentView(R.layout.activity_link_webview);

        EventBus.getDefault().register(this);

        buttonZzim = findViewById(R.id.buttonZzim);
        imageViewZzim = findViewById(R.id.imageViewZzim);
        buttonClose = findViewById(R.id.buttonClose);
        buttonBack = findViewById(R.id.buttonBack);
        buttonLinkCopy = findViewById(R.id.buttonLinkCopy);
        buttonGoBack = findViewById(R.id.buttonGoBack);
        buttonGoFowrad = findViewById(R.id.buttonGoFowrad);
        layoutButton = findViewById(R.id.layoutButton);
        title = findViewById(R.id.title);
        layoutTitle = findViewById(R.id.layoutTitle);
        progressBar = findViewById(R.id.progressBar);

        buttonClose.setOnClickListener(clickListener);
        buttonBack.setOnClickListener(clickListener);
        buttonLinkCopy.setOnClickListener(clickListener);
        buttonGoBack.setOnClickListener(clickListener);
        buttonGoFowrad.setOnClickListener(clickListener);
        buttonZzim.setOnClickListener(clickListener);
        Intent intent = getIntent();
        if (intent == null) {
            Logger.e("intent null error!!  finish activity");
            finish();
            return;
        }

        siteUrl = intent.getStringExtra(INTENT_EXTRA_KEY_LINK);
        if (TextUtils.isEmpty(siteUrl)) {
            Logger.e("siteUrl null error ");
            finish();
            return;
        }

        Logger.e("onCreate url:" + siteUrl);

        scCode = intent.getStringExtra(INTENT_EXTRA_KEY_SC_CODE);
        shopName = intent.getStringExtra(INTENT_EXTRA_KEY_SHOP_NAME);
        idx = intent.getStringExtra("IDX");
        is_wish = intent.getStringExtra("IS_WISH");
        type = intent.getStringExtra("TYPE");
        Logger.e("sssssss idx :: " + idx);
        Logger.e("sssssss is_wish :: " + is_wish);
        Logger.e("sssssss type :: " + type);
        if (TextUtils.isEmpty(shopName)) {
            shopName = "네이버";
        }

        if ( TextUtils.isEmpty(idx) ) {
            buttonZzim.setVisibility(View.GONE);
        } else {
            buttonZzim.setVisibility(View.VISIBLE);
        }

        if( "Y".equals(is_wish) ) {
            imageViewZzim.setSelected(true);
            imageViewZzim.setBackgroundResource(R.drawable.like_on_ico);
        } else {
            imageViewZzim.setSelected(false);
            imageViewZzim.setBackgroundResource(R.drawable.like_off_ico);
        }

        fromCasting = intent.getBooleanExtra(INTENT_EXTRA_KEY_FROM_CASTING, fromCasting);
        strTitle = intent.getStringExtra("TITLE");
        Logger.e("fromCasting :: " + fromCasting);
        Logger.e("strTitle :: " + strTitle);
        if (fromCasting) {
            layoutButton.setVisibility(View.VISIBLE);
            layoutTitle.setVisibility(View.GONE);
        } else {
            layoutButton.setVisibility(View.GONE);
            layoutTitle.setVisibility(View.VISIBLE);

            if (TextUtils.isEmpty(strTitle)) {
                layoutTitle.setVisibility(View.GONE);
            } else {
                title.setText(strTitle);
            }
        }



        mIsBuyMode = intent.getBooleanExtra("BUY_MODE", mIsBuyMode); // 쿠키 구매 팝업에서 넘어노는 경우
        if (mIsBuyMode) {  // 쿠키 구매일 경우 브라우저를 사용하자!!
            mIsBuyMode = false;

            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(siteUrl));
            startActivity(browserIntent);
            finish();
        }

        webView = findViewById(R.id.webView);
        //webView.clearHistory();
        //webView.clearCache(true);

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
        webView.setWebChromeClient(new MyWebChromeClient());
        webView.setWebViewClient(new MyWebViewClient());

        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (siteUrl.startsWith("https://") || siteUrl.startsWith("http://")) {
            webView.loadUrl(siteUrl);
        } else {
            siteUrl = "https://" + siteUrl;
            webView.loadUrl(siteUrl);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        EventBus.getDefault().post("resume_floating_video"); // video pip resume
        isLoginUser = AppPreferences.Companion.getLoginStatus(LinkWebViewActivity.this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        EventBus.getDefault().post("pause_floating_video"); // video pip pause
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }

    private void onCancelDialog() {
        Logger.e("onCancelDialog");
        if (mDialog != null) {
            mDialog.dismiss();
        }

        if (mIsBuyMode) {
            // 푸딩 구매 팝업 창에서 back 한 경우, 다시 구매 팝업이 뜨지 않아 부득이 하게 Activity 를 finish 하고 새로 만든다.
            finish();

            Intent i = new Intent(this, LinkWebViewActivity.class);
            i.putExtra("LINK", siteUrl);
            i.putExtra("TITLE", "젤리 구매");
            i.putExtra("BUY_MODE", true);
            startActivity(i);
        }
    }

    @Override
    public void onNetworkStatusChanged(@NotNull NetworkStatusUtils.NetworkStatus status) {
    }

    @Override
    public void onDozeModeStateChanged(boolean dozeEnable) {
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        Logger.e("onUserLeaveHint");

        if (mIsBuyMode) {
            // 푸딩 구매 화면에서 나간 경우
            onCancelDialog();
        } else {
            EventBus.getDefault().post("FINISH_PIP_MODE");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE_IMAGE_PICK && resultCode == Activity.RESULT_OK) {
            File myDir = new File(getCacheDir() + "/temp_image");
            String name = "cropTemp.jpg";

            File file = new File(myDir, name);
            productImg = Uri.parse(file.getPath());

            if(productImg != null) {
                productDialog.setProductImage(productImg);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onCastingProduct() {
        registerLinkProduct();
    }

    @Override
    public void onProductImage() {
        Intent intnet = new Intent(this, CoverSelectActivity.class);
        intnet.putExtra("ratio_square", true);
        startActivityForResult(intnet, REQUEST_CODE_IMAGE_PICK);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(NetworkBusResponse data) {
        String api138 = NetworkApi.API138.toString() + "?strUrl=" + loadUrl;
        if (data.arg1.equals(NetworkApi.API73.toString())) {
            handlePostLink(data);
        } else if (data.arg1.equals(api138)) {
            handleLinkParsing(data);
        }

        if ( idx != null) {
            String api126 = NetworkHandler.Companion.getInstance(this).getKey(NetworkApi.API126.toString(), idx, "");
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

    }

    private void handleLinkParsing(NetworkBusResponse data) {
        if ("ok".equals(data.arg2)) {
            API138 response = new Gson().fromJson(DBManager.getInstance(this).get(data.arg1), API138.class);

            imageUrl = response.imageurl;
            productTitle = response.title;
            price = String.valueOf(response.price);
            shopName = response.sitename;

            productDialog = new LinkProductDialog(this, shopName, imageUrl, productTitle, price);
            productDialog.setListener(this);
            productDialog.show();
        } else {
            Logger.e("error : " + data.arg3 + data.arg4);
        }
    }

    private void handlePostLink(NetworkBusResponse data) {
        if ("ok".equals(data.arg2)) {
            API73 response = new Gson().fromJson(DBManager.getInstance(this).get(data.arg1), API73.class);

            Intent intent = new Intent();
            intent.putExtra("image", response.imageurl);
            intent.putExtra("url", loadUrl);
            intent.putExtra("storeName", response.sitename);
            intent.putExtra("title", response.title);
            intent.putExtra("linkid", String.valueOf(response.linkid));
            intent.putExtra("money", response.ad_usemoney);
            intent.putExtra("price", response.price);
            setResult(Activity.RESULT_OK, intent);

            finish();
        } else {
            BaseAPI error = new Gson().fromJson(data.arg4, BaseAPI.class);
            Logger.e("error : " + error.toString());

            new AppToast(this).showToastMessage(error.message,
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM);
        }
    }

    private void linkParsing(String link) {
//        EventBus.getDefault().post(new NetworkBus(NetworkApi.API138.name(), link));
        progressBar.setVisibility(View.VISIBLE);
        ShopTreeAsyncTask task = new ShopTreeAsyncTask(this);
        task.getProductLink(link, new ShopTreeAsyncTask.OnDefaultObjectCallbackListener() {
            @Override
            public void onResponse(boolean rt, Object obj) {
                try {
                    progressBar.setVisibility(View.GONE);

                    API138 response = new Gson().fromJson(obj.toString(), API138.class);

                    if(response.result != null) {
                        if (response.result.toLowerCase().equals("success")) {
                            imageUrl = response.imageurl;
                            productTitle = response.title;
                            price = response.price;
                            shopName = response.sitename;

                            productDialog = new LinkProductDialog(LinkWebViewActivity.this, shopName, imageUrl, productTitle, price);
                            productDialog.setListener(LinkWebViewActivity.this);
                            productDialog.show();
                        }
                    } else {
                        JSONObject object = new JSONObject(obj.toString());
                        new AppToast(LinkWebViewActivity.this).showToastMessage(object.getString("message"),
                                AppToast.DURATION_MILLISECONDS_DEFAULT,
                                AppToast.GRAVITY_BOTTOM);
                    }
                } catch (Exception e) {
                    Logger.p(e);
                }
            }
        });
    }

    private void registerLinkProduct() {
        MultipartBody.Builder body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("siteurl", loadUrl)
                .addFormDataPart("sitename", productDialog.getShopName())
                .addFormDataPart("price", productDialog.getPrice())
                .addFormDataPart("imageurl", imageUrl)
                .addFormDataPart("title", productDialog.getProduct());

        if (productImg != null) {
            File thumbImg = new File(productImg.getPath());
            RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), thumbImg);
            MultipartBody.Part filebody = MultipartBody.Part.createFormData("thumbnail", thumbImg.getName(), requestFile);
            if (thumbImg.exists()) {
                body.addPart(filebody);
            }
        }

        EventBus.getDefault().post(new NetworkBus(NetworkApi.API73.name(), body.build()));
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.buttonBack) {
                onBackPressed();
            } else if (v.getId() == R.id.buttonLinkCopy) {
                linkParsing(loadUrl);
            } else if (v.getId() == R.id.buttonGoBack) {
                onBackPressed();
            } else if (v.getId() == R.id.buttonGoFowrad) {
                if (webView != null && webView.canGoForward()) {
                    webView.goForward();
                }
            } else if ( v.getId() == R.id.buttonZzim) {
                processZzimStatus(!imageViewZzim.isSelected());
            }
        }
    };

    @Override
    public void onBackPressed() {
        Logger.e("onBackPressed");

//        if (webView != null && webView.canGoBack()) {
//            webView.goBack();  // 가끔 안나가지는 link 가 있어서 사용 못함
//            return;
//        }

        finish();
    }

    private void processZzimStatus(boolean status) {
        if ( isLoginUser ) {
            try {
                JSONObject obj = new JSONObject();
                obj.put("user", AppPreferences.Companion.getUserId(this));
                obj.put("is_wish" , status == true ? "Y" : "N");
                obj.put("type", type);

                RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), obj.toString());
                NetworkBus bus = new NetworkBus(NetworkApi.API126.name(), idx, body);
                EventBus.getDefault().post(bus);

                EventBus.getDefault().post(new ZzimStatusBus(idx, status == true ? "Y" : "N", ""));
            } catch (Exception e) {
                Logger.p(e);
            }
        } else {
            goLogin();
        }
    }

    private void goLogin() {
        startActivity(new Intent(LinkWebViewActivity.this, LoginActivity.class));
    }
}