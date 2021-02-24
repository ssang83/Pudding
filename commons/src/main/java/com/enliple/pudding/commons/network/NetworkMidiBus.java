package com.enliple.pudding.commons.network;

import okhttp3.MultipartBody;

/**
 * Created by Kim Joonsung on 2018-12-18.
 */
public class NetworkMidiBus {
    public String arg1;
    public String arg2;
    public String arg3;
    public String arg4;
    public String arg5;
    public MultipartBody.Part part;

    public NetworkMidiBus(String arg1, String arg2, String arg3, String arg4, String arg5, MultipartBody.Part part) {
        this.arg1 = arg1;
        this.arg2 = arg2;
        this.arg3 = arg3;
        this.arg4 = arg4;
        this.arg5 = arg5;
        this.part = part;
    }
}
