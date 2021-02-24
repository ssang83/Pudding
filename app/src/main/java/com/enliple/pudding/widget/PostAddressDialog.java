package com.enliple.pudding.widget;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

import com.enliple.pudding.R;
import com.enliple.pudding.commons.log.Logger;
import com.enliple.pudding.commons.network.NetworkConst;

public class PostAddressDialog extends Dialog implements View.OnClickListener {
    private static final String JAVA_SCRIPT_INTERFACE_NAME = "PostAddrDialog";

    private RelativeLayout buttonClose;
    private WebView webView;

    private ResultListener mListener;
    private DaumPostAddressFindInterface mJavascriptInterface = new DaumPostAddressFindInterface();
    private android.os.Handler mHandler = new Handler();

    public PostAddressDialog(@NonNull Context context) {
        super(context, R.style.FindAddressDialog);

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        WindowManager.LayoutParams params = getWindow().getAttributes();

        params.height = (int) (metrics.heightPixels * 0.8);
        getWindow().setAttributes(params);

        View v = LayoutInflater.from(context).inflate(R.layout.layout_dialog_post_addr, null, false);
        setContentView(v);
        webView = (WebView) v.findViewById(R.id.webView);
        buttonClose = v.findViewById(R.id.buttonClose);
        setCancelable(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WebSettings settings = webView.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setDisplayZoomControls(false);
        settings.setBuiltInZoomControls(true);
        settings.setSupportZoom(true);
        settings.setDatabaseEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setSupportMultipleWindows(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setAllowUniversalAccessFromFileURLs(true);
        settings.setAllowFileAccessFromFileURLs(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        webView.addJavascriptInterface(mJavascriptInterface, JAVA_SCRIPT_INTERFACE_NAME);
        webView.setWebViewClient(mWebViewClient);
        buttonClose.setOnClickListener(this);
        webView.loadUrl(NetworkConst.POST_ADDRESS_FINDER_URL);
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
            return;
        }

        dialogDismiss();
    }

    @Override
    public void onClick(View v) {
        long viewId = v.getId();

        if (viewId == R.id.buttonClose) {
            dialogDismiss();
        }
    }

    @Override
    public void dismiss() {
        webView.clearHistory();
        webView.stopLoading();
        webView.removeJavascriptInterface("Android");
        webView.destroy();

        super.dismiss();
    }

    public void setResultListener(ResultListener listener) {
        mListener = listener;
    }

    public void dialogShow(Activity activity) {
        if (!this.isShowing() && activity != null && !activity.isFinishing()) {
            show();
        }
    }

    public void dialogShow(Context context) {
        if (context != null) {
            show();
        }
    }

    public void dialogDismiss() {
        if (this.isShowing()) {
            dismiss();
        }
    }

    public class DaumPostAddressFindInterface {
        /**
         * 주소검색 결과를 WebView 에서 반환
         *
         * @param postAddress
         * @param newAddress
         * @param oldAddress
         */
        @JavascriptInterface
        public void onPostAddressResult(final String postAddress, final String newAddress, final String oldAddress) {
            Logger.d("POST Address Result :"
                    + "\n 우편번호 : " + postAddress
                    + "\n신주소 : " + newAddress
                    + "\n구주소 : " + oldAddress);

            PostAddressDialog.this.mHandler.post(() -> {
                if (mListener != null) {
                    mListener.onPostAddressResult(postAddress, newAddress, oldAddress);
                }

                dialogDismiss();
            });
        }
    }

    private WebViewClient mWebViewClient = new WebViewClient() {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
//            viewForProgress.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
//            viewForProgress.setVisibility(View.GONE);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    };

    public interface ResultListener {
        void onPostAddressResult(String postAddress, String newAddress, String oldAddress);
    }
}
