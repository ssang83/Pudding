package com.enliple.pudding.model;

public class DeliveryInfo {
    private String mZipCode;
    private String mNewAddress;
    private String mAddress;
    private String mDetailAddress;
    private String mName;
    private String mPhone;
    private String mMemo;

    public void setZipCode(String val) {
        mZipCode = val;
    }

    public void setNewAddress(String val) {
        mNewAddress = val;
    }

    public void setAddress(String val) {
        mAddress = val;
    }

    public void setDetailAddress(String val) {
        mDetailAddress = val;
    }

    public void setName(String val) {
        mName = val;
    }

    public void setPhone(String val) {
        mPhone = val;
    }

    public void setMemo(String val) {
        mMemo = val;
    }

    public String getZipCode() {
        return mZipCode;
    }

    public String getNewAddress() {
        return mNewAddress;
    }

    public String getAddress() {
        return mAddress;
    }

    public String getDetailAddress() {
        return mDetailAddress;
    }

    public String getName() {
        return mName;
    }

    public String getPhone() {
        return mPhone;
    }

    public String getMemo() {
        return mMemo;
    }
}
