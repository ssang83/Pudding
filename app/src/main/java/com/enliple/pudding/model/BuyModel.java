package com.enliple.pudding.model;

import java.util.ArrayList;

public class BuyModel {
    private ArrayList<BuyItemModel> mModelArray;
    private String mStoreName;
    private double mOrderPrice;
    private double mDeliveryPrice;
    private String mDeliveryFeeType;
    private double mDeliveryFeeRange;
    private double mTotalChapterPrice;
    private String mSc;


    public void setModelArray(ArrayList<BuyItemModel> val) {
        mModelArray = val;
    }

    public void setStoreName(String val) {
        mStoreName = val;
    }

    public void setOrderPrice(double val) {
        mOrderPrice = val;
    }

    public void setDeliveryPrice(double val) {
        mDeliveryPrice = val;
    }

    public void setDeliveryFeeType(String val) {
        mDeliveryFeeType = val;
    }

    public void setDeliveryFeeRange(double val) {
        mDeliveryFeeRange = val;
    }

    public void setTotalChapterPrice(double val) {
        mTotalChapterPrice = val;
    }

    public void setSc(String val) {
        mSc = val;
    }

    public ArrayList<BuyItemModel> getModelArray() {
        return mModelArray;
    }

    public String getStoreName() {
        return mStoreName;
    }

    public double getOrderPrice() {
        return mOrderPrice;
    }

    public double getDeliveryPrice() {
        return mDeliveryPrice;
    }

    public String getDeliveryFeeType() {
        return mDeliveryFeeType;
    }

    public double getDeliveryFeeRange() {
        return mDeliveryFeeRange;
    }

    public double getTotalChapterPrice() {
        return mTotalChapterPrice;
    }

    public String getSc() {
        return mSc;
    }
}
