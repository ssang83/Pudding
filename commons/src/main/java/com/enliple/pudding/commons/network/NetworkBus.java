package com.enliple.pudding.commons.network;

import com.enliple.pudding.commons.data.ApiParams;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class NetworkBus {
    public String arg1;
    public String arg2;
    public String arg3;
    public String arg4;
    public String arg5;
    public String arg6;
    public String arg7;
    public String arg8;
    public RequestBody body;
    public ApiParams.FavorParams favorParams;
    public ApiParams.HitsParams hitsParams;
    public ApiParams.PasswdParams passwdParams;
    public ApiParams.TitleModifyParams titleModifyParams;
    public ApiParams.NoticeModifyParams noticeModifyParams;
    public ApiParams.ReplyInputParams replyInputParams;
    public ApiParams.ReplyModifyParams modifyParams;
    public ApiParams.UserModifyParams userModifyParams;
    public ApiParams.ScrapParams scrapParams;
    public ApiParams.OneByOneQAParams oneByOneParams;
    public MultipartBody.Part part;

    public NetworkBus(String arg1, RequestBody body) {
        this.arg1 = arg1;
        this.body = body;
    }

    public NetworkBus(String arg1, String arg2, RequestBody body) {
        this.arg1 = arg1;
        this.arg2 = arg2;
        this.body = body;
    }

    public NetworkBus(String arg1, String arg2, String arg3, RequestBody body) {
        this.arg1 = arg1;
        this.arg2 = arg2;
        this.arg3 = arg3;
        this.body = body;
    }

    public NetworkBus(String arg1, String arg2, ApiParams.HitsParams params) {
        this.arg1 = arg1;
        this.arg2 = arg2;
        this.hitsParams = params;
    }

    public NetworkBus(String arg1, String arg2, ApiParams.FavorParams params) {
        this.arg1 = arg1;
        this.arg2 = arg2;
        this.favorParams = params;
    }

    public NetworkBus(String arg1, String arg2, ApiParams.PasswdParams params) {
        this.arg1 = arg1;
        this.arg2 = arg2;
        this.passwdParams = params;
    }

//    public NetworkBus(String arg1, ApiParams.PasswdParams params) {
//        this.arg1 = arg1;
//        this.passwdParams = params;
//    }

    public NetworkBus(String arg1, ApiParams.TitleModifyParams params) {
        this.arg1 = arg1;
        this.titleModifyParams = params;
    }

    public NetworkBus(String arg1, ApiParams.NoticeModifyParams params) {
        this.arg1 = arg1;
        this.noticeModifyParams = params;
    }

    public NetworkBus(String arg1, ApiParams.ReplyInputParams params) {
        this.arg1 = arg1;
        this.replyInputParams = params;
    }

    public NetworkBus(String arg1, ApiParams.ReplyModifyParams params) {
        this.arg1 = arg1;
        this.modifyParams = params;
    }

    public NetworkBus(String arg1, ApiParams.UserModifyParams params) {
        this.arg1 = arg1;
        this.userModifyParams = params;
    }

    public NetworkBus(String arg1, ApiParams.ScrapParams params) {
        this.arg1 = arg1;
        this.scrapParams = params;
    }

    public NetworkBus(String arg1, ApiParams.OneByOneQAParams params) {
        this.arg1 = arg1;
        this.oneByOneParams = params;
    }

    public NetworkBus(String data) {
        this(data, "");
    }

    public NetworkBus(String arg1, String arg2) {
        this(arg1, arg2, "");
    }

    public NetworkBus(String arg1, String arg2, String arg3) {
        this(arg1, arg2, arg3, "");
    }

    public NetworkBus(String arg1, String arg2, String arg3, String arg4) {
        this(arg1, arg2, arg3, arg4, "");
    }

    public NetworkBus(String arg1, String arg2, String arg3, String arg4, String arg5) {
        this(arg1, arg2, arg3, arg4, arg5, "");
    }

    public NetworkBus(String arg1, String arg2, String arg3, String arg4, String arg5, String arg6) {
        this.arg1 = arg1;
        this.arg2 = arg2;
        this.arg3 = arg3;
        this.arg4 = arg4;
        this.arg5 = arg5;
        this.arg6 = arg6;
    }

    public NetworkBus(String arg1, String arg2, String arg3, String arg4, String arg5, String arg6, String arg7) {
        this.arg1 = arg1;
        this.arg2 = arg2;
        this.arg3 = arg3;
        this.arg4 = arg4;
        this.arg5 = arg5;
        this.arg6 = arg6;
        this.arg7 = arg7;
    }

    public NetworkBus(String arg1, String arg2, String arg3, String arg4, String arg5, String arg6, String arg7, String arg8) {
        this.arg1 = arg1;
        this.arg2 = arg2;
        this.arg3 = arg3;
        this.arg4 = arg4;
        this.arg5 = arg5;
        this.arg6 = arg6;
        this.arg7 = arg7;
        this.arg8 = arg8;
    }
}