package com.enliple.pudding.shoppingcaster.data;

public class SalingItem {
    private String image;
    private String name;
    private String price;
    private String storeName;
    private boolean isLink = true;
    private int type;
    private String itemIndex;
    private String linkId;
    private String quantity;
    private String rate;
    private String adUsemoney;

    public boolean isFromZzim() {
        return fromZzim;
    }

    public void setFromZzim(boolean fromZzim) {
        this.fromZzim = fromZzim;
    }

    private boolean fromZzim = false;


    public void setAdUsemoney(String adUsemoney) {
        this.adUsemoney = adUsemoney;
    }

    public String getAdUsemoney() {
        return adUsemoney;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getQuantity() {
        return this.quantity;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }

    public String getRate() {
        return this.rate;
    }

    public void setLinkId(String linkId) {
        this.linkId = linkId;
    }

    public String getLinkId() {
        return linkId;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public void setLink(boolean isLink) {
        this.isLink = isLink;
    }

    public void setProductType(int type) {
        this.type = type;
    }

    public void setProductIndex(String index) {
        this.itemIndex = index;
    }

    public String getImage() {
        return this.image;
    }

    public String getName() {
        return this.name;
    }

    public String getPrice() {
        return this.price;
    }

    public String getStoreName() {
        return this.storeName;
    }

    public boolean getIsLink() {
        return this.isLink;
    }

    public int getProductType() {
        return this.type;
    }

    public String getProductIndex() {
        return this.itemIndex;
    }
}
