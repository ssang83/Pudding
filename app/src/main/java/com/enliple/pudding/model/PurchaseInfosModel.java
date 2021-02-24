package com.enliple.pudding.model;

import java.util.ArrayList;

public class PurchaseInfosModel {
    private ArrayList<ProductModel> mProductArray;
    private DeliveryInfo mDeliveryInfo;

    public void setProductArray(ArrayList<ProductModel> val) {
        mProductArray = val;
    }

    public void setDeliveryInfo(DeliveryInfo val) {
        mDeliveryInfo = val;
    }

    public ArrayList<ProductModel> getProductArray() {
        return mProductArray;
    }

    public DeliveryInfo getDeliveryInfo() {
        return mDeliveryInfo;
    }

}
