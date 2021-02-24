package com.enliple.pudding.shoppingcaster.data;

public class LiveProductInfo {
    private String image;
    private String name;
    private String sale;
    private double price;
    private String storeName;
    private String pcode;
    private String scCode;

    public void setImage(String image) {
        this.image = image;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSale(String sale) {
        this.sale = sale;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public void setPcode(String pcode) {
        this.pcode = pcode;
    }

    public void setScCode(String scCode) {
        this.scCode = scCode;
    }

    public String getImage() {
        return this.image;
    }

    public String getName() {
        return this.name;
    }

    public String getSale() {
        return this.sale;
    }

    public double getPrice() {
        return this.price;
    }

    public String getStoreName() {
        return this.storeName;
    }

    public String getPcode() {
        return this.pcode;
    }

    public String getScCode() {
        return this.scCode;
    }
}
