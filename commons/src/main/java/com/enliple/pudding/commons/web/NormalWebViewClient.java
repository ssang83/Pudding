package com.enliple.pudding.commons.web;

import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.provider.Browser;
import androidx.appcompat.app.AlertDialog;
import android.view.KeyEvent;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.enliple.pudding.commons.log.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * 기본 WebViewClient
 *
 * @author hkcha
 * @since 2018.08.10
 */
public class NormalWebViewClient extends WebViewClient {

    private static final String TAG = NormalWebViewClient.class.getSimpleName();
    private ClientCallback callback;
    private WebView webView;
    private String currentUrlLocation;

    public enum UIMode {
        MODE_ISOLATE,
        MODE_LIST
    }

    private UIMode webViewMode;

    public NormalWebViewClient(WebView webView, UIMode webViewMode, ClientCallback callback) {
        this.webView = webView;
        this.callback = callback;
        this.webViewMode = webViewMode;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);

        if (!url.startsWith("source://")) {
            currentUrlLocation = url;
        }

        if (callback != null) {
            callback.onPageStarted(webView);
        }
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);

        if (url.startsWith("source:")) {
            if (currentUrlLocation != null) {
                webView.loadUrl(currentUrlLocation);
            }
        }

        if (callback != null) {
            callback.onPageFinished(webView);
        }
    }

    /**
     * 해당 화면의 HTML 소스코드 부분을 WebView 상에서 확인
     */
    public void viewSource() {
        if (webView != null) {
            webView.loadUrl("javascript:this.document.location.href = 'source://' + encodeURI(document.documentElement.outerHTML);");
        }
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (url.startsWith("http://") || url.startsWith("HTTP://")
                || url.startsWith("https://") || url.startsWith("HTTPS://")) {

            if (webViewMode == UIMode.MODE_LIST) {
                // Scenario : List MODE 에서는 현재 WebView 상에서 URL 이동을 하지 않는다.
                Uri uri = Uri.parse(url);
                Intent i = new Intent(Intent.ACTION_VIEW, uri);
                webView.getContext().startActivity(i);

                return true;
            }

            return false;
        } else if (url.startsWith("source:")) {                // 수신된 HTML Source 코드 확인
            try {
                String html = URLDecoder.decode(url, "UTF-8");

                // Received Source
                Logger.d(TAG, "HTML Source code (" + URLDecoder.decode(url, "UTF-8") + ") : \n" + html);

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                Logger.e(TAG, "Cannot output source code (UnSupported Encoding) : " + url);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                Logger.e(TAG, "Cannot output source code (IllegalArgument) : " + url);
            }

            return true;

        } else if (url.startsWith("vnd.youtube:")) {            // YouTube Content
            int n = url.indexOf("?");

            if (n > 0) {
                webView.getContext().startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse(String.format("http://www.youtube.com/v/%s",
                                url.substring("vnd.youtube:".length(), n)))));
            }

            return true;
        } else {
            boolean override = false;

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.putExtra(Browser.EXTRA_APPLICATION_ID, webView.getContext().getPackageName());

            if (url.startsWith("sms:")) {                    // SMS
                Intent i = new Intent(Intent.ACTION_SENDTO, Uri.parse(url));
                if (webView.getContext() != null) {
                    webView.getContext().startActivity(i);
                }

                return true;
            } else if (url.startsWith("tel:")) {                // Call
                // 전화걸기 (TBD)

                return true;
            } else if (url.startsWith("mailto:")) {            // E-mail
                Intent i = new Intent(Intent.ACTION_SENDTO, Uri.parse(url));
                if (webView.getContext() != null) {
                    webView.getContext().startActivity(i);
                }

                return true;
            } else if (url.startsWith("market:")) {            // PlayStore
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                intent.addCategory(Intent.CATEGORY_BROWSABLE);

                if (webView.getContext() != null) {
                    webView.getContext().startActivity(i);
                }

                return true;
            } else if (url.startsWith("kakaolink:")) {            // KakaoTalk
                try {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    intent.addCategory(Intent.CATEGORY_BROWSABLE);
                    if (webView.getContext() != null) {
                        webView.getContext().startActivity(i);
                    }

                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    Logger.e(TAG, "KakaoLink calling failed");
                }
            } else {                                            // Etc (Unknown scheme)
                try {
                    if (webView.getContext() != null) {
                        webView.getContext().startActivity(intent);
                        override = true;
                    }
                } catch (ActivityNotFoundException e) {
                    // 연결가능한 Application 이 없을 경우에 해당
                    e.printStackTrace();
                    return override;
                }
            }
        }

        return super.shouldOverrideUrlLoading(view, url);
    }

    @Override
    public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
        return super.shouldOverrideKeyEvent(view, event);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @SuppressWarnings("deprecation")
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        return super.shouldInterceptRequest(view, url);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view,
                                                      WebResourceRequest request) {
        return super.shouldInterceptRequest(view, request);
    }

    @Override
    public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
        // SSL 에서 보안인증 관련 에러가 발생해도 계속 진행처리
//        handler.proceed();

        StringBuilder sb = new StringBuilder();

        switch (error.getPrimaryError()) {
            case SslError.SSL_EXPIRED:
                sb.append("이 사이트의 보안 인증서는 신뢰할 수 없습니다.\n");
                break;

            case SslError.SSL_IDMISMATCH:
                sb.append("이 사이트의 보안 인증서는 신뢰할 수 없습니다.\n");
                break;

            case SslError.SSL_NOTYETVALID:
                sb.append("이 사이트의 보안 인증서는 신뢰할 수 없습니다.\n");
                break;

            case SslError.SSL_UNTRUSTED:
                sb.append("이 사이트의 보안 인증서는 신뢰할 수 없습니다.\n");
                break;

            default:
                sb.append("보안 인증서에 오류가 있습니다.\n");
                break;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(webView.getContext());
        builder.setTitle("SSL 보안경고")
                .setMessage(sb.toString())
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    handler.proceed();
                    dialog.dismiss();
                })
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                    handler.cancel();
                    dialog.dismiss();
                })
                .create().show();
    }

    @Override
    public void onScaleChanged(WebView view, float oldScale, float newScale) {
        super.onScaleChanged(view, oldScale, newScale);
    }

    public interface ClientCallback {
        /**
         * Page Loading 시작 됨
         */
        void onPageStarted(WebView webView);

        /**
         * Page Loading 종료 됨
         */
        void onPageFinished(WebView webView);
    }
}

