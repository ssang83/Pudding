package com.enliple.pudding.commons.shoptree.response;

import com.enliple.pudding.commons.shoptree.data.CartData;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by Kim Joonsung on 2018-09-18.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductCartResponse extends BaseResponse {
    @JsonProperty(value = "goodsPrice")
    public long totalProductPrice;

    @JsonProperty(value = "feePrice")
    public int totalDeliveryPrice;

    @JsonProperty(value = "totalPrice")
    public long totalOrderPrice;

    @JsonProperty(value = "cartList")
    public List<CartData> cartList;
}
