package com.enliple.pudding.model;

public class DetailOptionItem {
    private String key;
    private String name;
    private double price;
    private int quantity;
    private int selectedQuantity = 0;
    private String optionkey;
    private boolean bSoldout;

    public int getSelectedQuantity() {
        return selectedQuantity;
    }

    public void setSelectedQuantity(int selectedQuantity) {
        this.selectedQuantity = selectedQuantity;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getOptionkey() {
        return optionkey;
    }

    public void setOptionkey(String optionkey) {
        this.optionkey = optionkey;
    }

    public boolean isbSoldout() {
        return bSoldout;
    }

    public void setbSoldout(boolean bSoldout) {
        this.bSoldout = bSoldout;
    }
}
