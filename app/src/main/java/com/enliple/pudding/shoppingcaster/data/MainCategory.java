package com.enliple.pudding.shoppingcaster.data;

import java.util.List;

public class MainCategory {
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

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String categoryId;
    public String categoryName;
    public boolean isSelected;

    public List<SubCategory> getSubArray() {
        return subArray;
    }

    public void setSubArray(List<SubCategory> subArray) {
        this.subArray = subArray;
    }

    public List<SubCategory> subArray;
}
