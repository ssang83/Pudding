package com.enliple.pudding.commons.shoptree.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by Kim Joonsung on 2018-09-18.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PurchaseHistoryItems {
    @JsonProperty(value = "title")
    public String title;

    @JsonProperty(value = "img")
    public String image;

    @JsonProperty(value = "status")
    public String status;

    @JsonProperty(value = "option")
    public String option = "";

    @JsonProperty(value = "itemKey")
    public String itemKey = "";

    @JsonProperty(value = "price")
    public double price = 0;

    @JsonProperty(value = "deliveryPrice")
    public int deliveryPrice = 0;

    @JsonProperty(value = "trackingInfo_name")
    public String trackingInfoName = "";

    @JsonProperty(value = "trackingInfo_code")
    public String trackingInfoCode = "";

    @JsonProperty(value = "trackingInfo_url")
    public String trackingInfoUrl = "";

    @JsonProperty(value = "it_id")
    public String it_id;

    @JsonProperty(value = "ct_id")
    public String ct_id;

    @JsonProperty(value = "store")
    public String storeNname = "";

    @JsonProperty(value = "orderNumber")
    public String orderNumber = "";

    @JsonProperty(value = "stOrderNumber")
    public String stOrderNumber = "";

    @JsonProperty(value = "ct_time")
    public String ctTime = "";

    @JsonProperty(value = "review")
    public ReviewInfo review;

    @JsonProperty(value = "usePoint")
    public int usePoint = 0;
}
