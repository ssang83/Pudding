package com.enliple.pudding.commons.shoptree.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by Kim Joonsung on 2018-09-20.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeliveryStatusDetailData {
    @JsonProperty(value = "orderNumber")
    public String orderNumber;

    @JsonProperty(value = "stOrderNumber")
    public String stOrderNumber;

    @JsonProperty(value = "regdate")
    public String regdate;

    @JsonProperty(value = "items")
    public List<DeliveryStatusDetailItems> items;
}
