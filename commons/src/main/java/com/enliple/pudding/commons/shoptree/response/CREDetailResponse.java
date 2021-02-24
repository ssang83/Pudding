package com.enliple.pudding.commons.shoptree.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Kim Joonsung on 2018-09-21.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class CREDetailResponse {

    @JsonProperty(value = "option")
    public String option = "";

    @JsonProperty(value = "product_name")
    public String product_name = "";

    @JsonProperty(value = "status")
    public String status = "";

    @JsonProperty(value = "rdate")
    public String rdate = "";

    @JsonProperty(value = "quantity")
    public int quantity = 0;

    @JsonProperty(value = "product_price")
    public int product_price = 0;

    @JsonProperty(value = "delivery_price")
    public int delivery_price = 0;

    @JsonProperty(value = "point")
    public int point = 0;

    @JsonProperty(value = "image")
    public String image = "";

    @JsonProperty(value = "image_width")
    public int image_width = 0;

    @JsonProperty(value = "image_height")
    public int image_height = 0;

    @JsonProperty(value = "request_date")
    public String request_date = "";

    @JsonProperty(value = "response_date")
    public String response_date = "";

    @JsonProperty(value = "flow")
    public String flow = "";

    @JsonProperty(value = "result")
    public String result = "";

    @JsonProperty(value = "resultCode")
    public int resultCode = 0;

/*    @JsonProperty(value = "image")
    public String imgUrl = "";

    @JsonProperty(value = "image_width")
    public int imgWidth = 0;

    @JsonProperty(value = "image_height")
    public int imgHeight = 0;

    @JsonProperty(value = "store_name")
    public String shopName = "";

    @JsonProperty(value = "product_name")
    public String title = "";

    @JsonProperty(value = "status")
    public String status = "";

    @JsonProperty(value = "quantity")
    public int productCnt;

    @JsonProperty(value = "rdate")
    public String date = "";

    @JsonProperty(value = "payment_price")
    public int total_payment_cancel = 0;

    @JsonProperty(value = "product_price")
    public int product_price = 0;

    @JsonProperty(value = "delivery_price")
    public int delivery_price = 0;

    @JsonProperty(value = "point")
    public int point = 0;

    @JsonProperty(value = "option")
    public String option = "";

    @JsonProperty(value = "type")
    public String type = "";*/
}