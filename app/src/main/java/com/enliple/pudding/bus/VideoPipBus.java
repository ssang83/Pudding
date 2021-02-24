package com.enliple.pudding.bus;

import com.enliple.pudding.model.PlayerInfo;

import java.io.Serializable;

public class VideoPipBus implements Serializable {
    public static final String TYPE_MAIN = "TYPE_MAIN";
    public static final String TYPE_DETAIL = "TYPE_DETAIL";
    public static final String TYPE_SWIPE = "TYPE_SWIPE";
    public String strContentSize;
    public String url;
    public Long position;
    public float volume;

    public int fromWhere = -1;
    public String casterWhat = "";
    public String chatAccount = "";
    public String chatNickName = "";
    public String chatRoomId = "";
    public int itemPosition = 0;
    public int myVODPosition = -1;
    public String casterId = "";
    public int playerFlag = -1;
    public String videoType = "";
    public String shareKey = "";
    public boolean requestedOrientation = true;
    public String title = "";
    public String productName = "";
    public String type = TYPE_MAIN;
    public String streamKey = "";

    public VideoPipBus(String url, Long position, String strContentSize, float volume, PlayerInfo info) {
        this.url = url;
        this.position = position;
        this.strContentSize = strContentSize;
        this.volume = volume;
        if ( info != null ) {
            this.fromWhere = info.getFromWhere();
            this.casterWhat = info.getCasterWhat();
            this.chatAccount = info.getChatAccount();
            this.chatNickName = info.getChatNickName();
            this.chatRoomId = info.getChatRoomId();
            this.itemPosition = info.getItemPosition();
            this.myVODPosition = info.getMyVODPosition();
            this.casterId = info.getCasterId();
            this.playerFlag = info.getPlayerFlag();
            this.videoType = info.getVideoType();
            this.shareKey = info.getShareKey();
            this.requestedOrientation = info.isRequestedOrientation();
            this.title = info.getTitle();
            this.productName = info.getProductName();
            this.type = info.getType();
            this.streamKey = info.getStreamKey();
        }
    }
}