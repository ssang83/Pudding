package com.enliple.pudding.model;

import java.io.Serializable;

/**
 * Created by Administrator on 2018-03-27.
 */

public class ShipCondition implements Serializable {
    private String mFeeType;
    private double mShipFee, mFeeRange;

    public void setFeeType(String val) {
        mFeeType = val;
    }

    public void setShipFee(double val) {
        mShipFee = val;
    }

    public void setFeeRange(double val) {
        mFeeRange = val;
    }

    public String getFeeType() {
        return mFeeType;
    }

    public double getShipFee() {
        return mShipFee;
    }

    public double getFeeRange() {
        return mFeeRange;
    }
}
