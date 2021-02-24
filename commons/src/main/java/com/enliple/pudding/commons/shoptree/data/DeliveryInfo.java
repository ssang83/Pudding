package com.enliple.pudding.commons.shoptree.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Kim Joonsung on 2018-09-19.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeliveryInfo {
    @JsonProperty(value = "deliveryAddress")
    public String deliveryAddress;

    @JsonProperty(value = "deliveryName")
    public String deliveryName;

    @JsonProperty(value = "deliveryPhone")
    public String deliveryPhone;

    @JsonProperty(value = "deliveryPrice")
    public String deliveryPrice;

    @JsonProperty(value = "deliveryZipcode")
    public String deliveryZipcode;
}
