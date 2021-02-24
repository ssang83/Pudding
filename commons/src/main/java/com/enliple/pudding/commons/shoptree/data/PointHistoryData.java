package com.enliple.pudding.commons.shoptree.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Kim Joonsung on 2018-09-19.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PointHistoryData {
    /**
     * 이벤트 발생일
     */
    @JsonProperty(value = "regdate")
    public String regDate = "";

    /**
     * 이벤트소멸일
     */
    @JsonProperty(value = "expiredate")
    public String expireDate = "";

    @JsonProperty(value = "order_number")
    public String orderNumber = "";

    /**
     * 포인트 종류
     */
    @JsonProperty(value = "flag_ment")
    public String flagMent = "";

    @JsonProperty(value = "color_type")
    public String colorType = "";

    @JsonProperty(value = "title")
    public String title = "";

    @JsonProperty(value = "point")
    public int point = 0;
}
