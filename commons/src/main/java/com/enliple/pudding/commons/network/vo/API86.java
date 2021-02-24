package com.enliple.pudding.commons.network.vo;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Kim Joonsung on 2018-12-10.
 */
public class API86 {

    /**
     * X-Mbus-Token : 467F4A4A221649D98E57588DB4BF93BF35449C85D246EB85A4B7FF06876AFCDADF717F271A0F0EA96D56A597DA01C30020AE00BF875C7317E761E8C97883DC90
     * X-Mbus-Channel : ch_1648a4c9
     * FileName : VOD_userId_20180907113051
     * UploadHost : https://api.midibus.kinxcdn.com/
     * UploadPath : v1/upload/262/v
     */

    @SerializedName("X-Mbus-Token")
    public String XMbusToken;
    @SerializedName("X-Mbus-Channel")
    public String XMbusChannel;
    public String FileName;
    public String UploadHost;
    public String UploadPath;
}
