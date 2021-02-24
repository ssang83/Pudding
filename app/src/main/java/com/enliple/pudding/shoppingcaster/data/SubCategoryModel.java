package com.enliple.pudding.shoppingcaster.data;

public class SubCategoryModel {
    private String subCategory = "";
    private boolean selected = false;

    public void setSubCategory(String subCategory) {
        this.subCategory = subCategory;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getSubCategory() {
        return this.subCategory;
    }

    public boolean getSelected() {
        return this.selected;
    }
}
