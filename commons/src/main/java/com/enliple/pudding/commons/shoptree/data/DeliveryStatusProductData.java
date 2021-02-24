package com.enliple.pudding.commons.shoptree.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Kim Joonsung on 2018-09-20.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeliveryStatusProductData {
    @JsonProperty(value = "itemKey")
    public String itemKey;

    @JsonProperty(value = "img")
    public String image = "";

    @JsonProperty(value = "title")
    public String title;

    @JsonProperty(value = "option")
    public String option;

    @JsonProperty(value = "status")
    public String status = "";

    @JsonProperty(value = "it_id")
    public int it_id = 0;

    @JsonProperty(value = "ct_id")
    public int ct_id = 0;

    @JsonProperty(value = "price")
    public double price = 0;

    @JsonProperty(value = "trackingInfo_name")
    public String trackingInfo_name = "";

    @JsonProperty(value = "trackingInfo_code")
    public String trackingInfo_code = "";

    @JsonProperty(value = "trackingInfo_url")
    public String trackingInfo_url = "";
}
