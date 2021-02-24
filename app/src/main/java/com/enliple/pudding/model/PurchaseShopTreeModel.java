package com.enliple.pudding.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Administrator on 2018-04-05.
 */

public class PurchaseShopTreeModel implements Serializable {
    private String mProductName;
    private String mStoreName;
    private ShopTreeImageModel mImage;

    private ShipCondition mShipCondition;
    private String mKey;
    private String mOptionName;
    private String mOptionKey;
    private double mPrice;
    private double mOptionPrice;
    private int mQuantity;
    private int mOptionQuantity;
    private int mRemainedQuantity;
    private ArrayList<SelectedOption> mOptionArray;
    private String mD_Name;
    private String mD_Tel;
    private String mD_Phone;
    private String mD_Postal;
    private String mD_Address;
    private String mB_User;
    private String mB_Name;
    private String mB_Phone;
    private double mTotalPrice;
    private int mTotalQuantity;
    private String mIdx;

    public void setIdx(String val) {
        mIdx = val;
    }

    public void setImage(ShopTreeImageModel val) {
        mImage = val;
    }

    public void setProductName(String val) {
        mProductName = val;
    }

    public void setStoreName(String val) {
        mStoreName = val;
    }

    public void setShipCondition(ShipCondition val) {
        mShipCondition = val;
    }

    public void setKey(String val) {
        mKey = val;
    }

    public void setOptionName(String val) {
        mOptionName = val;
    }

    public void setOptionKey(String val) {
        mOptionKey = val;
    }

    public void setPrice(double val) {
        mPrice = val;
    }

    public void setOptionPrice(double val) {
        mOptionPrice = val;
    }

    public void setQuantity(int val) {
        mQuantity = val;
    }

    public void setOptionQuantity(int val) {
        mOptionQuantity = val;
    }

    public void setRemainedQuantity(int val) {
        mRemainedQuantity = val;
    }

    public void setOptionArray(ArrayList<SelectedOption> val) {
        mOptionArray = val;
    }

    public void setDName(String val) {
        mD_Name = val;
    }

    public void setDTel(String val) {
        mD_Tel = val;
    }

    public void setDPhone(String val) {
        mD_Phone = val;
    }

    public void setDPostal(String val) {
        mD_Postal = val;
    }

    public void setDAddress(String val) {
        mD_Address = val;
    }

    public void setBUser(String val) {
        mB_User = val;
    }

    public void setBName(String val) {
        mB_Name = val;
    }

    public void setBPhone(String val) {
        mB_Phone = val;
    }

    public void setTotalPrice(double val) {
        mTotalPrice = val;
    }

    public void setTotalQuantity(int val) {
        mTotalQuantity = val;
    }

    public String getIdx() {
        return mIdx;
    }

    public ShopTreeImageModel getImage() {
        return mImage;
    }

    public String getProductName() {
        return mProductName;
    }

    public String getStoreName() {
        return mStoreName;
    }

    public ShipCondition getShipCondition() {
        return mShipCondition;
    }

    public String getKey() {
        return mKey;
    }

    public String getOptionName() {
        return mOptionName;
    }

    public String getOptionKey() {
        return mOptionKey;
    }

    public double getPrice() {
        return mPrice;
    }

    public double getOptionPrice() {
        return mOptionPrice;
    }

    public int getQuantity() {
        return mQuantity;
    }

    public int getOptionQuantity() {
        return mOptionQuantity;
    }

    public int getRemainedQuantity() {
        return mRemainedQuantity;
    }

    public ArrayList<SelectedOption> getOptionArray() {
        return mOptionArray;
    }

    public String getDName() {
        return mD_Name;
    }

    public String getDTel() {
        return mD_Tel;
    }

    public String getDPhone() {
        return mD_Phone;
    }

    public String getDPostal() {
        return mD_Postal;
    }

    public String getDAddress() {
        return mD_Address;
    }

    public String getBUser() {
        return mB_User;
    }

    public String getBName() {
        return mB_Name;
    }

    public String getBPhone() {
        return mB_Phone;
    }

    public double getTotalPrice() {
        return mTotalPrice;
    }

    public int getTotalQuantity() {
        return mTotalQuantity;
    }
}
