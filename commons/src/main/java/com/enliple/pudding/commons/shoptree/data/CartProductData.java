package com.enliple.pudding.commons.shoptree.data;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Kim Joonsung on 2018-09-18.
 */

public class CartProductData {
    @JsonProperty(value = "seq")
    public String seq;

    @JsonProperty(value = "ct_id")
    public String ct_id;

    @JsonProperty(value = "pcode")
    public String pCode;

    @JsonProperty(value = "strStreamKey")
    public String strStreamKey;

    @JsonProperty(value = "strVodType")
    public String strVodType;

    @JsonProperty(value = "strRecommendMbId")
    public String strRecommendMbId;

    @JsonProperty(value = "optionname")
    public String optionName = "";

    @JsonProperty(value = "optionprice")
    public int optionPrice;

    @JsonProperty(value = "price")
    public int price = 0;

    @JsonProperty(value = "deliveryprice")
    public int deliveryprice = 0;

    @JsonProperty(value = "optionkey")
    public String optionKey;

    @JsonProperty(value = "displayprice")
    public String displayprice;

    @JsonProperty(value = "title")
    public String title;

    @JsonProperty(value = "image")
    public String image;

    @JsonProperty(value = "cnt")
    public int productCount;

    @JsonProperty(value = "optionqty")
    public String quantity;

    private boolean isSelected = true;

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
