package com.enliple.pudding.model;

import java.util.ArrayList;

public class SecondCategoryItem {
    private String categoryId;
    private String categoryName;
    private boolean selected = false;
    private ArrayList<ThirdCategoryItem> thirdCategory;

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public ArrayList<ThirdCategoryItem> getThirdCategory() {
        return thirdCategory;
    }

    public void setThirdCategory(ArrayList<ThirdCategoryItem> thirdCategory) {
        this.thirdCategory = thirdCategory;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}