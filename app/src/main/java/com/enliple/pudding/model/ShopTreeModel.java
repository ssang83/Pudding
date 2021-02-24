package com.enliple.pudding.model;

import java.util.ArrayList;

/**
 * Created by Administrator on 2018-03-27.
 */

public class ShopTreeModel {
    private String mKey;
    private String mName;
    private String mRegDate;
    private String mNew;
    private String mBest;
    private ShopTreeStoreModel mStoreModel;
    private String mModel;
    private double mPrice;
    private double mOriginPrice;
    private boolean mSoldOut;
    private String mCategory;
    private int mQuantity;
    private ArrayList<ShopTreeOption> mOptionArray;
    private ArrayList<ShopTreeOptionItem> mOptionItemArray;
    private double mMargin;
    private ShipCondition mShipCondition;
    private String mDescription;
    private String mDeliveryInfo;
    private String mWarrantyInfo;
    private String mRefundInfo;
    private String mExchangeInfo;
    private String mBundled;
    private ArrayList<ShopTreeImageModel> mImageArray;
    private ShopTreeImageModel mMainImage;
    private double mSavePoint;
    private double mSaveRate;

    public void setKey(String val) {
        mKey = val;
    }

    public void setName(String val) {
        mName = val;
    }

    public void setRegDate(String val) {
        mRegDate = val;
    }

    public void setNew(String val) {
        mNew = val;
    }

    public void setBest(String val) {
        mBest = val;
    }

    public void setStoreModel(ShopTreeStoreModel val) {
        mStoreModel = val;
    }

    public void setModel(String val) {
        mModel = val;
    }

    public void setPrice(double val) {
        mPrice = val;
    }

    public void setOriginPrice(double val) {
        mOriginPrice = val;
    }

    public void setSoldOut(boolean val) {
        mSoldOut = val;
    }

    public void setCategory(String val) {
        mCategory = val;
    }

    public void setQuantity(int val) {
        mQuantity = val;
    }

    public void setOptionArray(ArrayList<ShopTreeOption> val) {
        mOptionArray = val;
    }

    public void setOptionItemArray(ArrayList<ShopTreeOptionItem> val) {
        mOptionItemArray = val;
    }

    public void setMargin(double val) {
        mMargin = val;
    }

    public void setShipCondition(ShipCondition val) {
        mShipCondition = val;
    }

    public void setDescription(String val) {
        mDescription = val;
    }

    public void setDeliveryInfo(String val) {
        mDeliveryInfo = val;
    }

    public void setWarrantyInfo(String val) {
        mWarrantyInfo = val;
    }

    public void setRefundInfo(String val) {
        mRefundInfo = val;
    }

    public void setExchangeInfo(String val) {
        mExchangeInfo = val;
    }

    public void setBundled(String val) {
        mBundled = val;
    }

    public void setImageArray(ArrayList<ShopTreeImageModel> val) {
        mImageArray = val;
    }

    public void setMainImage(ShopTreeImageModel val) {
        mMainImage = val;
    }

    public void setSavePoint(double val) {
        mSavePoint = val;
    }

    public void setSaveRate(double val) {
        mSaveRate = val;
    }

    public String getKey() {
        return mKey;
    }

    public String getName() {
        return mName;
    }

    public String getRegDate() {
        return mRegDate;
    }

    public String getNew() {
        return mNew;
    }

    public String getBest() {
        return mBest;
    }

    public ShopTreeStoreModel getStoreModel() {
        return mStoreModel;
    }

    public String getModel() {
        return mModel;
    }

    public double getPrice() {
        return mPrice;
    }

    public double getOriginPrice() {
        return mOriginPrice;
    }

    public boolean getSoldOut() {
        return mSoldOut;
    }

    public String getCategory() {
        return mCategory;
    }

    public int getQuantity() {
        return mQuantity;
    }

    public ArrayList<ShopTreeOption> getOptionArray() {
        return mOptionArray;
    }

    public ArrayList<ShopTreeOptionItem> getOptionItemArray() {
        return mOptionItemArray;
    }

    public double getMargin() {
        return mMargin;
    }

    public ShipCondition getShipCondition() {
        return mShipCondition;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getDeliveryInfo() {
        return mDeliveryInfo;
    }

    public String getWarrantyInfo() {
        return mWarrantyInfo;
    }

    public String getRefundInfo() {
        return mRefundInfo;
    }

    public String getExchangeInfo() {
        return mExchangeInfo;
    }

    public String getBundled() {
        return mBundled;
    }

    public ArrayList<ShopTreeImageModel> getImageArray() {
        return mImageArray;
    }

    public ShopTreeImageModel getMainImage() {
        return mMainImage;
    }

    public double getSavePoint() {
        return mSavePoint;
    }

    public double getSaveRate() {
        return mSaveRate;
    }

//    private String mKey;
//    private String mName;
//    private ShopTreeStoreModel mStoreModel;
//    private String mModel;
//    private double mPrice;
//    private boolean mSoldOut;
//    private String mCategory;
//    private int mQuantity;
//    private ArrayList<ProductOption> mOptionArray;
//    private ArrayList<ProductOptionItem> mOptionItemArray;
//    private double mMargin;
//    private ShipCondition mShipCondition;
//    private String mDescription;
//    private String mDeliveryInfo;
//    private String mWarrantyInfo;
//    private String mRefundInfo;
//    private String mExchangeInfo;
//    private ArrayList<ShopTreeImageModel> mImageArray;
//    private ShopTreeImageModel mMainImage;
//
//    public void setKey(String val) { mKey = val; }
//    public void setName(String val) { mName = val; }
//    public void setStoreModel(ShopTreeStoreModel val) { mStoreModel = val; }
//    public void setModel(String val) { mModel = val; }
//    public void setPrice(double val) { mPrice = val; }
//    public void setSoldOut(boolean val) { mSoldOut = val; }
//    public void setCategory(String val) { mCategory = val; }
//    public void setQuantity(int val) { mQuantity = val; }
//    public void setOptionArray(ArrayList<ProductOption> val) { mOptionArray = val; }
//    public void setOptionItemArray(ArrayList<ProductOptionItem> val) { mOptionItemArray = val; }
//    public void setMargin(double val) { mMargin = val; }
//    public void setShipCondition(ShipCondition val) { mShipCondition = val; }
//    public void setDescription(String val) { mDescription = val; }
//    public void setDeliveryInfo(String val) { mDeliveryInfo = val; }
//    public void setWarrantyInfo(String val) { mWarrantyInfo = val; }
//    public void setRefundInfo(String val) { mRefundInfo = val; }
//    public void setExchangeInfo(String val) { mExchangeInfo = val; }
//    public void setImageArray(ArrayList<ShopTreeImageModel> val) { mImageArray = val; }
//    public void setMainImage(ShopTreeImageModel val) { mMainImage = val; }
//
//    public String getKey() { return mKey; }
//    public String getName() { return mName; }
//    public ShopTreeStoreModel getStoreModel() { return mStoreModel; }
//    public String getModel() { return mModel; }
//    public double getPrice() { return mPrice; }
//    public boolean getSoldOut() { return mSoldOut; }
//    public String getCategory() { return mCategory; }
//    public int getQuantity() { return mQuantity; }
//    public ArrayList<ProductOption> getOptionArray() { return mOptionArray; }
//    public ArrayList<ProductOptionItem> getOptionItemArray() { return mOptionItemArray; }
//    public double getMargin() { return mMargin; }
//    public ShipCondition getShipCondition() { return mShipCondition; }
//    public String getDescription() { return mDescription; }
//    public String getDeliveryInfo() { return mDeliveryInfo; }
//    public String getWarrantyInfo() { return mWarrantyInfo; }
//    public String getRefundInfo() { return mRefundInfo; }
//    public String getExchangeInfo() { return mExchangeInfo; }
//    public ArrayList<ShopTreeImageModel> getImageArray() { return mImageArray; }
//    public ShopTreeImageModel getMainImage() { return mMainImage; }
}
