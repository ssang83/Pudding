package com.enliple.pudding.commons.shoptree.network;

import android.content.Context;
import android.text.TextUtils;

import com.enliple.pudding.commons.internal.AppPreferences;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;

public class ShopTreeBaseRetrofit {
    private static Context mContext;

    protected static Object retrofit(Context context, Class<?> className) {
        mContext = context;
        // TODO : 협의 완료되는대로 값을 정상 HOST로 변경해야함
        String host = ShopTreeUrl.DOMAIN;

        OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
        builder.readTimeout(20, TimeUnit.SECONDS);
        builder.connectTimeout(20, TimeUnit.SECONDS);
        builder.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                String authorization = AppPreferences.Companion.getUserId(context);
                if (TextUtils.isEmpty(authorization)) {
                    authorization = "";
                }
                Request request = chain.request().newBuilder()
                        .addHeader("Authorization", authorization)
                        .addHeader("jwt", AppPreferences.Companion.getJWT(context))
                        .build();
                return chain.proceed(request);
            }
        });

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        builder.addNetworkInterceptor(logging);

        OkHttpClient okHttpClient = builder.build();
        Retrofit retrofit = new Retrofit.Builder().baseUrl(host).client(okHttpClient).build();
        return retrofit.create(className);
    }
}