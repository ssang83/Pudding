package com.enliple.pudding.commons.data;

import android.os.Parcel;
import android.os.Parcelable;

public class DialogModel implements Parcelable {


    public String getIdx() {
        return idx;
    }

    public void setIdx(String idx) {
        this.idx = idx;
    }

    public String getScCode() {
        return scCode;
    }

    public void setScCode(String scCode) {
        this.scCode = scCode;
    }

    public String getThumbNail() {
        return thumbNail;
    }

    public void setThumbNail(String thumbNail) {
        this.thumbNail = thumbNail;
    }

    private String scCode;

    public String getPcode() {
        return pcode;
    }

    public void setPcode(String pcode) {
        this.pcode = pcode;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLinkUrl() {
        return linkUrl;
    }

    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCustPrice() {
        return custPrice;
    }

    public void setCustPrice(String custPrice) {
        this.custPrice = custPrice;
    }

    public String getSellPrice() {
        return sellPrice;
    }

    public void setSellPrice(String sellPrice) {
        this.sellPrice = sellPrice;
    }

    public String getStreamKey() {
        return streamKey;
    }

    public void setStreamKey(String streamKey) {
        this.streamKey = streamKey;
    }

    public String getVodType() {
        return vodType;
    }

    public void setVodType(String vodType) {
        this.vodType = vodType;
    }

    public void setRecommendId(String recommendId) { this.recommendId = recommendId; }

    public String getRecommendId() { return recommendId;}

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setWish_cnt(String wish_cnt) {
        this.wish_cnt = wish_cnt;
    }

    public void setIs_wish(String is_wish) {
        this.is_wish = is_wish;
    }

    public void setIs_cart(String is_cart) {
        this.is_cart = is_cart;
    }

    public String getWish_cnt() {
        return wish_cnt;
    }

    public String getIs_wish() {
        return is_wish;
    }

    public String getIs_cart() {
        return is_cart;
    }

    private String idx;
    private String pcode;
    private String thumbNail;
    private String type;
    private String linkUrl;
    private String name;
    private String custPrice;
    private String sellPrice;
    private String streamKey;
    private String vodType;
    private String recommendId = "";
    private String storeName;
    private String wish_cnt;
    private String is_wish;
    private String is_cart;

    @Override
    public void writeToParcel(Parcel parcel, int i)
    {
        parcel.writeString(idx);
        parcel.writeString(pcode);
        parcel.writeString(thumbNail);
        parcel.writeString(type);
        parcel.writeString(linkUrl);
        parcel.writeString(name);
        parcel.writeString(custPrice);
        parcel.writeString(sellPrice);
        parcel.writeString(streamKey);
        parcel.writeString(vodType);
        parcel.writeString(recommendId);
        parcel.writeString(storeName);
        parcel.writeString(wish_cnt);
        parcel.writeString(is_wish);
        parcel.writeString(is_cart);
    }

    public DialogModel(Parcel in)
    {
        if (in != null)
        {
            idx = in.readString();
            pcode = in.readString();
            thumbNail = in.readString();
            type = in.readString();
            linkUrl = in.readString();
            name = in.readString();
            custPrice = in.readString();
            sellPrice = in.readString();
            streamKey = in.readString();
            vodType = in.readString();
            recommendId = in.readString();
            storeName = in.readString();
            wish_cnt = in.readString();
            is_wish = in.readString();
            is_cart = in.readString();
        }
    }

    @Override
    public int describeContents()
    {
        return hashCode();
    }

    public DialogModel()
    {

    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator()
    {
        public DialogModel createFromParcel(Parcel in)
        {
            return new DialogModel(in);
        }

        public DialogModel[] newArray(int size)
        {
            return new DialogModel[size];
        }
    };
}
