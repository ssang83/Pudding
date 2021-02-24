package com.enliple.pudding.model;

import java.util.ArrayList;

public class ThreeCategoryItem {
    private String categoryId;
    private String categoryName;
    private String categoryImage;
    private String categoryImageOn;
    private String categoryImageOff;
    private boolean selected = false;

    public String getCategoryImageOn() {
        return categoryImageOn;
    }

    public void setCategoryImageOn(String categoryImageOn) {
        this.categoryImageOn = categoryImageOn;
    }

    public String getCategoryImageOff() {
        return categoryImageOff;
    }

    public void setCategoryImageOff(String categoryImageOff) {
        this.categoryImageOff = categoryImageOff;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    private ArrayList<SecondCategoryItem> secondCategory;

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

    public String getCategoryImage() {
        return categoryImage;
    }

    public void setCategoryImage(String categoryImage) {
        this.categoryImage = categoryImage;
    }

    public ArrayList<SecondCategoryItem> getSecondCategory() {
        return secondCategory;
    }

    public void setSecondCategory(ArrayList<SecondCategoryItem> secondCategory) {
        this.secondCategory = secondCategory;
    }
}
