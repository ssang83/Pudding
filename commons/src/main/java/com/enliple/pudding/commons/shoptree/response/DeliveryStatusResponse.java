package com.enliple.pudding.commons.shoptree.response;

import com.enliple.pudding.commons.shoptree.data.DeliveryStatusData;
import com.enliple.pudding.commons.shoptree.data.DeliveryType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by Kim Joonsung on 2018-09-20.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeliveryStatusResponse extends BaseResponse {
    @JsonProperty(value = "deliveryType")
    public DeliveryType deliveryType;

    @JsonProperty(value = "orders")
    public List<DeliveryStatusData> orders;
}
