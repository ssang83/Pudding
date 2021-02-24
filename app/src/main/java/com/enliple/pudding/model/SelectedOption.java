package com.enliple.pudding.model;

import java.io.Serializable;

/**
 * Created by Administrator on 2018-04-05.
 */

public class SelectedOption implements Serializable {
    private String mKey;
    private String mOptionName;
    private int mQuantity;
    private int mRemainedQuantity;
    private double mOptionPrice;
    private double mProductPrice;
    private double mTotalPrice;
    private String mIdx;

    public void setIdx(String val) {
        mIdx = val;
    }

    public void setKey(String val) {
        mKey = val;
    }

    public void setQuantity(int val) {
        mQuantity = val;
    }

    public void setOptionName(String val) {
        mOptionName = val;
    }

    public void setRemainedQuantity(int val) {
        mRemainedQuantity = val;
    }

    public void setOptionPrice(double val) {
        mOptionPrice = val;
    }

    public void setProductPrice(double val) {
        mProductPrice = val;
    }

    public void setTotalPrice(double val) {
        mTotalPrice = val;
    }

    public String getIdx() {
        return mIdx;
    }

    public String getKey() {
        return mKey;
    }

    public int getQuantity() {
        return mQuantity;
    }

    public String getOptionName() {
        return mOptionName;
    }

    public int getRemainedQuantity() {
        return mRemainedQuantity;
    }

    public double getOptionPrice() {
        return mOptionPrice;
    }

    public double getProductPrice() {
        return mProductPrice;
    }

    public double getTotalPrice() {
        return mTotalPrice;
    }
}
