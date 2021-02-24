package com.enliple.pudding.model;

public class ShopTreeOptionItem {
    private String key = "";
    private String name = "";
    private String optionkey = "";
    private double price = 0;
    private int quantity = 0;
    private String bSoldoiut = "";

    public void setKey(String key) {
        this.key = key;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOptionKey(String optionkey) {
        this.optionkey = optionkey;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setSoldOut(String bSoldout) {
        this.bSoldoiut = bSoldout;
    }

    public String getKey() {
        return this.key;
    }

    public String getName() {
        return this.name;
    }

    public String getOptionKey() {
        return this.optionkey;
    }

    public double getPrice() {
        return this.price;
    }

    public int getQuantity() {
        return this.quantity;
    }

    public String getSoldOut() {
        return this.bSoldoiut;
    }
}
