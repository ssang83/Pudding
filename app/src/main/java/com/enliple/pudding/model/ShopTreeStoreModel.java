package com.enliple.pudding.model;

/**
 * Created by Administrator on 2018-03-27.
 */

public class ShopTreeStoreModel {
    private String mKey, mName, mSexType, mStoreType, mSiteUrl, mSiteAddr, mSitePhone, mCompanyName, mOwnerName, mEMail, mBusinessCode, mSalesCode;
    private ShopTreeImageModel mImageModel;

    public void setKey(String val) {
        mKey = val;
    }

    public void setName(String val) {
        mName = val;
    }

    public void setCompanyName(String val) {
        mCompanyName = val;
    }

    public void setSexType(String val) {
        mSexType = val;
    }

    public void setStoreType(String val) {
        mStoreType = val;
    }

    public void setSiteUrl(String val) {
        mSiteUrl = val;
    }

    public void setSiteAddr(String val) {
        mSiteAddr = val;
    }

    public void setSitePhone(String val) {
        mSitePhone = val;
    }

    public void setShopTreeImageModel(ShopTreeImageModel val) {
        mImageModel = val;
    }

    public void setOwnerName(String val) {
        mOwnerName = val;
    }

    public void setEMail(String val) {
        mEMail = val;
    }

    public void setBusinessCode(String val) {
        mBusinessCode = val;
    }

    public void setSalesCode(String val) {
        mSalesCode = val;
    }

    public String getKey() {
        return mKey;
    }

    public String getName() {
        return mName;
    }

    public String getCompanyName() {
        return mCompanyName;
    }

    public String getSexType() {
        return mSexType;
    }

    public String getStoreType() {
        return mStoreType;
    }

    public String getSiteUrl() {
        return mSiteUrl;
    }

    public String getSiteAddr() {
        return mSiteAddr;
    }

    public String getSitePhone() {
        return mSitePhone;
    }

    public ShopTreeImageModel getShopTreeImageModel() {
        return mImageModel;
    }

    public String getOwnerName() {
        return mOwnerName;
    }

    public String getEMail() {
        return mEMail;
    }

    public String getBusinessCode() {
        return mBusinessCode;
    }

    public String getSalesCode() {
        return mSalesCode;
    }
}
