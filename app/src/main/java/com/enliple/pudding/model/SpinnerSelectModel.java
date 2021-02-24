package com.enliple.pudding.model;

public class SpinnerSelectModel {
    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    private boolean isSelected = false;
    private String category;
}
