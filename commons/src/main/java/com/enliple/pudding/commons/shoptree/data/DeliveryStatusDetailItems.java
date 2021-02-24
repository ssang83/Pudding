package com.enliple.pudding.commons.shoptree.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by Kim Joonsung on 2018-09-20.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeliveryStatusDetailItems {
    @JsonProperty(value = "store_name")
    public String shopName;

    @JsonProperty(value = "item")
    public List<DeliveryStatusDetailProductData> productData;
}
