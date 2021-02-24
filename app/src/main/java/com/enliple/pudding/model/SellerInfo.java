package com.enliple.pudding.model;

import java.io.Serializable;

public class SellerInfo implements Serializable {
    private String mPhone, mEMail, mAddress, mDeliveryInfo, mWarrantyInfo, mRefundInfo, mExchangeInfo, mIcon, mName, mBusinessCode, mSalesCode, mOwnerName;

    public void setPhone(String val) {
        this.mPhone = val;
    }

    public void setEMail(String val) {
        this.mEMail = val;
    }

    public void setAddress(String val) {
        this.mAddress = val;
    }

    public void setDeliveryInfo(String val) {
        this.mDeliveryInfo = val;
    }

    public void setWarrantyInfo(String val) {
        this.mWarrantyInfo = val;
    }

    public void setRefundInfo(String val) {
        this.mRefundInfo = val;
    }

    public void setExchangeInfo(String val) {
        this.mExchangeInfo = val;
    }

    public void setIcon(String val) {
        this.mIcon = val;
    }

    public void setName(String val) {
        this.mName = val;
    }

    public void setBusinessCode(String val) {
        this.mBusinessCode = val;
    }

    public void setSalesCode(String val) {
        this.mSalesCode = val;
    }

    public void setOWnerName(String val) {
        this.mOwnerName = val;
    }

    public String getPhone() {
        return this.mPhone;
    }

    public String getEMail() {
        return this.mEMail;
    }

    public String getAddress() {
        return this.mAddress;
    }

    public String getDeliveryInfo() {
        return this.mDeliveryInfo;
    }

    public String getWarrantyInfo() {
        return this.mWarrantyInfo;
    }

    public String getRefundInfo() {
        return this.mRefundInfo;
    }

    public String getExchangeinfo() {
        return this.mExchangeInfo;
    }

    public String getIcon() {
        return this.mIcon;
    }

    public String getName() {
        return this.mName;
    }

    public String getBusinessCode() {
        return this.mBusinessCode;
    }

    public String getSalesCode() {
        return this.mSalesCode;
    }

    public String getmOwnerName() {
        return this.mOwnerName;
    }
}
