package com.enliple.pudding.shoppingcaster.data;

public class LiveProductMainCategory {
    private long id;
    private String categoryId;
    private String categoryName;
    private String categoryImage;

    public void setId(long id) {
        this.id = id;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public void setCategoryImage(String categoryImage) {
        this.categoryImage = categoryImage;
    }

    public long getId() {
        return this.id;
    }

    public String getCategoryId() {
        return this.categoryId;
    }

    public String getCategoryName() {
        return this.categoryName;
    }

    public String getCategoryImage() {
        return this.categoryImage;
    }
}
