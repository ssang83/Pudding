package com.enliple.pudding.commons.shoptree.response;

import com.enliple.pudding.commons.shoptree.data.PurchaseHistoryData;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by Kim Joonsung on 2018-09-18.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PurchaseHistoryResponse extends BaseResponse {
    @JsonProperty(value = "orderCnt")
    public int orderCnt;

    @JsonProperty(value = "orders")
    public List<PurchaseHistoryData> orders;
}
