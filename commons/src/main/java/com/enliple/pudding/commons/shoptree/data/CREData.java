package com.enliple.pudding.commons.shoptree.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Kim Joonsung on 2018-09-21.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class CREData {

    @JsonProperty(value = "ct_id")
    public String ct_id;

    @JsonProperty(value = "ct_status")
    public String ct_status;

    @JsonProperty(value = "store_name")
    public String store_name;

    @JsonProperty(value = "complaints_date")
    public String complaints_date;

    @JsonProperty(value = "complaints_flow")
    public String complaints_flow;

    @JsonProperty(value = "it_main_thumbnail")
    public String it_img1;

    @JsonProperty(value = "it_img_width")
    public String it_img_width;

    @JsonProperty(value = "it_img_height")
    public String it_img_height;

    @JsonProperty(value = "product_name")
    public String product_name;

    @JsonProperty(value = "option")
    public String option;

    /*    *//** 상태 타입 *//*
    @JsonProperty(value = "status")
    public String status;

    *//** 상품 이미지 *//*
    @JsonProperty(value = "image")
    public String image;

    @JsonProperty(value = "image_width")
    public int imgWidth;

    @JsonProperty(value = "image_height")
    public int imgHeight;

    *//** 상점이름 *//*
    @JsonProperty(value = "store_name")
    public String shopName;

    *//** 상품타이틀 *//*
    @JsonProperty(value = "product_name")
    public String title;

    *//** 요청일 *//*
    @JsonProperty(value = "rdate")
    public String date;

    @JsonProperty(value = "itemIdx")
    public String itemIdx;

    @JsonProperty(value = "orderNumber")
    public String orderNumber;*/
}
