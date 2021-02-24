package com.enliple.pudding.commons.shoptree.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Kim Joonsung on 2018-09-20.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PointAmountData {
    @JsonProperty(value = "title")
    public String title;

    @JsonProperty(value = "savedate")
    public String saveDate;

    @JsonProperty(value = "order_number")
    public String orderNo;

    @JsonProperty(value = "expiredate")
    public String expireDate;

    @JsonProperty(value = "regdate")
    public String regDate;

    @JsonProperty(value = "point")
    public int point;
}
