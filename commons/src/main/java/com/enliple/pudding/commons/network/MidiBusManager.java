package com.enliple.pudding.commons.network;

import android.content.Context;

import com.enliple.pudding.commons.log.Logger;

import org.greenrobot.eventbus.EventBus;

import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import okio.Buffer;
import okio.BufferedSource;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Kim Joonsung on 2018-12-18.
 */
public class MidiBusManager implements HttpLoggingInterceptor.Logger {

    private static final int TIME_OUT = 5; // 5 sec
    private static final int READ_TIME_OUT = 30; // 30 SDC
    private static MidiBusManager sSingleton;
    private Context mContext;
    private NetworkService mNetworkService;
    private String serverUrl;

    public class RequestCallback implements Callback<ResponseBody> {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            if (call == null) {
                Logger.e("call null error!");
                return;
            }

            String result;
            if (response.isSuccessful()) {
                try {
                    result = getBody(response.body());
                } catch (Exception e) {
                    Logger.p(e);
                    result = null;
                }
            } else {
                try {
                    result = getBody(response.errorBody());
                } catch (Exception e) {
                    Logger.p(e);
                    result = null;
                }
            }

            if (response.isSuccessful()) {
                EventBus.getDefault().post(new NetworkBusResponse("ok", result));
            } else {
                Logger.e("error");
                String code = String.valueOf(response.code());
                EventBus.getDefault().post(new NetworkBusResponse("fail", code, result));
            }
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            Logger.e("onFailure:" + t.getMessage());
            EventBus.getDefault().post(new NetworkBusResponse("fail", "-1", "server connection fail!"));
        }
    }

    public static synchronized MidiBusManager getInstance(Context context, String baseUrl) {
        if (sSingleton == null) {
            sSingleton = new MidiBusManager(context, baseUrl);
        }

        return sSingleton;
    }

    public MidiBusManager(Context context, String baseUrl) {
        mContext = context;
        serverUrl = baseUrl;

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor(this);
        logging.setLevel(HttpLoggingInterceptor.Level.HEADERS);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(TIME_OUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIME_OUT, TimeUnit.SECONDS)
                .addNetworkInterceptor(logging)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        mNetworkService = retrofit.create(NetworkService.class);
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
        Logger.l(message);
    }
}
