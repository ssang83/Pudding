package com.enliple.pudding.commons.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.text.TextUtils;

import com.couchbase.lite.MutableDocument;
import com.enliple.pudding.commons.db.DBManager;
import com.enliple.pudding.commons.internal.AppPreferences;
import com.enliple.pudding.commons.log.Logger;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import okhttp3.Authenticator;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import okhttp3.Route;
import okhttp3.logging.HttpLoggingInterceptor;
import okio.Buffer;
import okio.BufferedSource;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Network manager.
 */
public class NetworkManager implements HttpLoggingInterceptor.Logger {
    public static final String ROOT = Environment.getExternalStorageDirectory() + "/Pudding/";

    public static final String LOG_FOLDER = ROOT + "log/";

    private static final int TIME_OUT = 20; // 8 sec

    private static NetworkManager sSingleton;

    private Context mContext;

    private NetworkService mNetworkService;

    public class Token {
        public String jwt;
    }

    private class MyInterceptor implements Interceptor {
        @Override
        public okhttp3.Response intercept(Chain chain) throws IOException {
            Request original = chain.request();

            String jwt = AppPreferences.Companion.getJWT(mContext);
            Request.Builder requestBuilder = original.newBuilder()
                    .header("content-type:", "text/html")
                    .header("charset", "UTF-8")
                    .header("jwt", jwt)
                    .method(original.method(), original.body());

            return chain.proceed(requestBuilder.build());
        }
    }

    private class MyAuthenticator implements Authenticator {
        @Override
        public Request authenticate(Route route, okhttp3.Response response) {
            Logger.e("Authenticating code: " + response.code() + " message:" + response.message());

//            String body = getBody(response);
//            if (!TextUtils.isEmpty(body) && body.contains("ERR_TOKEN_EXPIRED")) {
//                //getAccessToken();
//            }
//
//            Token token = getToken();
//
//            String tokenString = token.jwt;
//            if (TextUtils.isEmpty(tokenString)) {
//                tokenString = token.jwt;
//            }

            String jwt = AppPreferences.Companion.getJWT(mContext);

            return response.request().newBuilder()
                    .header("jwt", jwt)
                    .build();
        }
    }

    public class RequestCallback implements Callback<ResponseBody> {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            if (call == null || response == null) {
                Logger.e("call or response null error!");
                return;
            }

            String result;
            String key;
            String urlDecode = "";
            try {
                urlDecode = URLDecoder.decode(call.request().url().toString(), "UTF-8");
                Logger.d("urlDecode:" + urlDecode);
                key = makeKey(call.request().method(), urlDecode);
            } catch (Exception e) {
                key = "";
                Logger.p(e);
            }

            if (response.isSuccessful()) {
                Logger.d("key:" + key);

                try {
//                    result = response.body().string();
                    result = getBody(response.body());
                } catch (Exception e) {
                    Logger.p(e);
                    result = null;
                }
            } else {
                try {
//                    result = response.errorBody().string();
                    result = getBody(response.errorBody());
                } catch (Exception e) {
                    Logger.p(e);
                    result = null;
                }
            }

            if (TextUtils.isEmpty(result) || TextUtils.isEmpty(key)) {
                Logger.e("result or key null");
                return;
            }

            if (response.isSuccessful()) {
                MutableDocument document = new MutableDocument(key);
                document.setString(key, result);
                DBManager.getInstance(mContext).put(document);

                if (key.startsWith(NetworkApi.API8.toString())                // 좋아요
                        || key.startsWith(NetworkApi.API26.toString())        // 스크랩 등록
                        || key.startsWith(NetworkDBKey.INSTANCE.getAPI24Key(mContext)) // 스크랩 해제
                        || key.contains("GET/mui/vod/")     // 비디오 정보
                        || key.contains("/report/cookie")   // 받은 젤리 현황
                        || key.contains(NetworkApi.API142.toString())   // 채팅방 호출
                        || key.contains(NetworkApi.API84.toString())    // 젤리 선물하기
                        || key.startsWith(NetworkApi.API2.toString())   // 팔로우
                ) {
                    EventBus.getDefault().post(new NetworkBusFastResponse(key, "ok"));
                } else {
                    EventBus.getDefault().post(new NetworkBusResponse(key, "ok"));
                }
            } else {
                String code = String.valueOf(response.code());
                EventBus.getDefault().post(new NetworkBusResponse(key, "fail", code, result));

                Logger.e("network error! code:" + code + " result:" + result);
            }
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            String key = makeKey(call.request().method(), call.request().url().toString());
            Logger.e("onFailure:" + t.getMessage() + " key:" + key);
            EventBus.getDefault().post(new NetworkBusResponse(key, "fail", "-1", "server connection fail!"));
        }
    }

    public static synchronized NetworkManager getInstance(Context context) {
        if (sSingleton == null) {
            sSingleton = new NetworkManager(context);
        }
        return sSingleton;
    }

    public NetworkManager(Context context) {
        mContext = context;

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor(this);
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(TIME_OUT, TimeUnit.SECONDS)
                .readTimeout(TIME_OUT, TimeUnit.SECONDS)
                .addNetworkInterceptor(logging)
                .addNetworkInterceptor(new MyInterceptor())
                //.authenticator(new MyAuthenticator())
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(NetworkConst.SERVER_API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        mNetworkService = retrofit.create(NetworkService.class);
    }

    public Token getToken() {
        SharedPreferences prefs = mContext.getSharedPreferences("a", Context.MODE_PRIVATE);
        String accessToken = prefs.getString("accessToken", "");

        Token token = new Token();
        if (!TextUtils.isEmpty(accessToken)) {
            token.jwt = accessToken;
        }

        return token;
    }

    public Callback getCallback() {
        return new RequestCallback();
    }

    public NetworkService getService() {
        return mNetworkService;
    }

    private String getBody(okhttp3.ResponseBody responseBody) {
        String body = "";

        //ResponseBody responseBody = response.body();
        BufferedSource source = responseBody.source();
        try {
            source.request(Long.MAX_VALUE); // Buffer the entire body.
            Buffer buffer = source.buffer();

            Charset charset = Charset.forName("UTF-8");
            MediaType contentType = responseBody.contentType();
            if (contentType != null) {
                charset = contentType.charset(Charset.forName("UTF-8"));
            }

            if (responseBody.contentLength() != 0) {
                body = buffer.clone().readString(charset);
            }
        } catch (Exception e) {
            Logger.p(e);

        }

        return body;
    }

    @Override
    public void log(String message) {
        if (!TextUtils.isEmpty(message)) {
            if (!message.contains("Content-Disposition: form-data")) {
                Logger.l(message);
            } else {
                int max = message.length();
                if (max > 500) max = 500;
                Logger.i(message.substring(0, max));
            }
        }

        //saveLog(message);
    }

    private void saveLog(String message) {
        String tag = "NetworkManager";

        String status = Environment.getExternalStorageState();
        if (status.equalsIgnoreCase(Environment.MEDIA_MOUNTED) == false) {
            Logger.e(tag, "SDCard Status:" + status);
            return;
        }

        File dir = new File(LOG_FOLDER, "network.txt");
        if (dir.exists() == false) {
            dir.mkdirs();
        }

        try {
            //File f = new File(LOG_FOLDER, "network.txt");
            FileOutputStream fos = new FileOutputStream(dir, true);
            fos.write((message + "\n").getBytes());
            fos.close();
        } catch (Exception e) {
            Logger.p(tag, e);
        }
    }

    private String makeKey(String method, String url) {
        if (!TextUtils.isEmpty(url)) {
            return method + "/" + url.replace(NetworkConst.SERVER_API_URL, "");
        }

        return "";
    }
}