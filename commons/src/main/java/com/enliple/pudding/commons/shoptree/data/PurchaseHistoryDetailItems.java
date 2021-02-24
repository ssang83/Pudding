package com.enliple.pudding.commons.shoptree.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by Kim Joonsung on 2018-09-19.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PurchaseHistoryDetailItems {
    @JsonProperty(value = "store_name")
    public String shopName;

    @JsonProperty(value = "item")
    public List<PurchaseHistoryDetailProductData> productData;
}
