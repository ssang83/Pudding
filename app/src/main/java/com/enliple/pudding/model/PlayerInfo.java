package com.enliple.pudding.model;

import com.enliple.pudding.bus.VideoPipBus;

public class PlayerInfo {
    private String streamKey = "";
    private int fromWhere = -1;
    private String casterWhat = "";
    private String chatAccount = "";
    private String chatNickName = "";
    private String chatRoomId = "";
    private int itemPosition = 0;
    private int myVODPosition = -1;
    private String casterId = "";
    private int playerFlag = -1;
    private String videoType = "";
    private String shareKey = "";
    private boolean requestedOrientation = true;
    private String title = "";
    private String productName = "";
    private String type = VideoPipBus.TYPE_MAIN;

    public String getStreamKey() {
        return streamKey;
    }

    public void setStreamKey(String streamKey) {
        this.streamKey = streamKey;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getFromWhere() {
        return fromWhere;
    }

    public void setFromWhere(int fromWhere) {
        this.fromWhere = fromWhere;
    }

    public String getCasterWhat() {
        return casterWhat;
    }

    public void setCasterWhat(String casterWhat) {
        this.casterWhat = casterWhat;
    }

    public String getChatAccount() {
        return chatAccount;
    }

    public void setChatAccount(String chatAccount) {
        this.chatAccount = chatAccount;
    }

    public String getChatNickName() {
        return chatNickName;
    }

    public void setChatNickName(String chatNickName) {
        this.chatNickName = chatNickName;
    }

    public String getChatRoomId() {
        return chatRoomId;
    }

    public void setChatRoomId(String chatRoomId) {
        this.chatRoomId = chatRoomId;
    }

    public int getItemPosition() {
        return itemPosition;
    }

    public void setItemPosition(int itemPosition) {
        this.itemPosition = itemPosition;
    }

    public int getMyVODPosition() {
        return myVODPosition;
    }

    public void setMyVODPosition(int myVODPosition) {
        this.myVODPosition = myVODPosition;
    }

    public String getCasterId() {
        return casterId;
    }

    public void setCasterId(String casterId) {
        this.casterId = casterId;
    }

    public int getPlayerFlag() {
        return playerFlag;
    }

    public void setPlayerFlag(int playerFlag) {
        this.playerFlag = playerFlag;
    }

    public String getVideoType() {
        return videoType;
    }

    public void setVideoType(String videoType) {
        this.videoType = videoType;
    }

    public String getShareKey() {
        return shareKey;
    }

    public void setShareKey(String shareKey) {
        this.shareKey = shareKey;
    }

    public boolean isRequestedOrientation() {
        return requestedOrientation;
    }

    public void setRequestedOrientation(boolean requestedOrientation) {
        this.requestedOrientation = requestedOrientation;
    }
}
