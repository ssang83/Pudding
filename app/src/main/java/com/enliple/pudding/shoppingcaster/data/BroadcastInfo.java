package com.enliple.pudding.shoppingcaster.data;

import com.enliple.pudding.commons.data.CategoryItem;

import java.util.ArrayList;

public class BroadcastInfo {
    public static final String KEY_SUBJECT = "subject";
    public static final String KEY_CATEGORY_CODE = "categoryCode";
    public static final String KEY_PRODUCT_ITEMS = "productItems";
    public static final String KEY_TAG = "tag";
    public static final String KEY_REGISTRATION = "registration";
    public static final String KEY_FIRST_CATEGORY = "first_category";
    public static final String KEY_SECOND_CATEGORY = "second_category";
    public static final String KEY_THIRD_CATEGORY = "third_category";
    public static final String KEY_CATEGORY_RECORD_ID = "category_record_id";
    public static final String KEY_CATEGORY_ID = "category_id";
    public static final String KEY_CATEGORY_NAME = "category_name";
    public static final String KEY_CATEGORY_IMAGE = "category_image";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_TYPE = "type";
    public static final String KEY_MULTI = "isMulti";
    public static final String KEY_AGE = "age";
    public static final String KEY_SEX = "sex";
    public static final String KEY_DATE = "date";

    private String subject;
    private String categoryCode;
    private ArrayList<String> productItems;
    private CategoryItem firstCategory;
    private CategoryItem secondCategory;
    private ArrayList<CategoryItem> thirdCategory;
    private String tag;
    private String registration;
    private String image;
    private String type;
    private String strMulti;
    private String strAge;
    private String strSex;
    private String date;

    public String getStrMulti() {
        return strMulti;
    }

    public void setStrMulti(String strMulti) {
        this.strMulti = strMulti;
    }

    public String getStrAge() {
        return strAge;
    }

    public void setStrAge(String strAge) {
        this.strAge = strAge;
    }

    public String getStrSex() {
        return strSex;
    }

    public void setStrSex(String strSex) {
        this.strSex = strSex;
    }

    public CategoryItem getFirstCategory() {
        return firstCategory;
    }

    public void setFirstCategory(CategoryItem firstCategory) {
        this.firstCategory = firstCategory;
    }

    public CategoryItem getSecondCategory() {
        return secondCategory;
    }

    public void setSecondCategory(CategoryItem secondCategory) {
        this.secondCategory = secondCategory;
    }

    public ArrayList<CategoryItem> getThirdCategory() {
        return thirdCategory;
    }

    public void setThirdCategory(ArrayList<CategoryItem> thirdCategory) {
        this.thirdCategory = thirdCategory;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(String categoryCode) {
        this.categoryCode = categoryCode;
    }

    public ArrayList<String> getProductItems() {
        return productItems;
    }

    public void setProductItems(ArrayList<String> productItems) {
        this.productItems = productItems;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getRegistration() {
        return registration;
    }

    public void setRegistration(String registration) {
        this.registration = registration;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
    }
}
