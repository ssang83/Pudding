package com.enliple.pudding.commons.shoptree.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by Kim Joonsung on 2018-09-18.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CartData {
    @JsonProperty(value = "sc_code")
    public String scCode;

    @JsonProperty(value = "shop_name")
    public String shopName = "";

    @JsonProperty(value = "goodsChapterPrice")
    public long goodPrice;

    @JsonProperty(value = "totalChapterPrice")
    public long totalPrice;

    @JsonProperty(value = "feeChapterPrice")
    public int deliveryPrice;

    @JsonProperty(value = "docs")
    public List<CartProductData> productData;

    private boolean isSelected = true;

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
