package com.enliple.pudding.model;

public class ProductModel {
    private String mPCode;
    private int mQuantity;
    private double mPrice;
    private double mOptionPrice;
    private String mKey;

    public void setPCode(String val) {
        mPCode = val;
    }

    public void setQuantity(int val) {
        mQuantity = val;
    }

    public void setPrice(double val) {
        mPrice = val;
    }

    public void setOptionPrice(double val) {
        mOptionPrice = val;
    }

    public void setKey(String val) {
        mKey = val;
    }

    public String getPCode() {
        return mPCode;
    }

    public int getQuantity() {
        return mQuantity;
    }

    public double getPrice() {
        return mPrice;
    }

    public double getOptionPrice() {
        return mOptionPrice;
    }

    public String getKey() {
        return mKey;
    }
}
