package com.enliple.pudding.model;

public class BuyItemModel {
    private String mProductKey;
    private String mOptionKey;
    private String mOptionName;
    private int mOptionQuantity;
    private String mImage;
    private String mName;
    private double mTotalPrice;


    public void setProductKey(String val) {
        mProductKey = val;
    }

    public void setOptionKey(String val) {
        mOptionKey = val;
    }

    public void setOptionName(String val) {
        mOptionName = val;
    }

    public void setOptionQuantity(int val) {
        mOptionQuantity = val;
    }

    public void setImage(String val) {
        mImage = val;
    }

    public void setName(String val) {
        mName = val;
    }

    public void setTotalPrice(double val) {
        mTotalPrice = val;
    }

    public String getProductKey() {
        return mProductKey;
    }

    public String getOptionKey() {
        return mOptionKey;
    }

    public String getOptionName() {
        return mOptionName;
    }

    public int getOptionQuantity() {
        return mOptionQuantity;
    }

    public String getImage() {
        return mImage;
    }

    public String getName() {
        return mName;
    }

    public double getTotalPrice() {
        return mTotalPrice;
    }
}
