package com.enliple.pudding.commons.shoptree.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by Kim Joonsung on 2018-09-18.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class PurchaseHistoryData {
    @JsonProperty(value = "regdate")
    public String date;

//    @JsonProperty(value = "orderNumber")
//    public String orderNo;

    @JsonProperty(value = "list")
    public List<PurchaseHistoryItems> data;
}
