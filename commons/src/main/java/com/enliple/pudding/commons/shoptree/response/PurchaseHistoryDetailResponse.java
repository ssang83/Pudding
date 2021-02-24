package com.enliple.pudding.commons.shoptree.response;

import com.enliple.pudding.commons.shoptree.data.BankingInfo;
import com.enliple.pudding.commons.shoptree.data.DeliveryInfo;
import com.enliple.pudding.commons.shoptree.data.PurchaseHistoryDetailData;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Kim Joonsung on 2018-09-19.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PurchaseHistoryDetailResponse extends BaseResponse {
    @JsonProperty(value = "itemCnt")
    public String itemCnt;

    @JsonProperty(value = "recipient")
    public String recipient;

    @JsonProperty(value = "contract")
    public String contract;

    @JsonProperty(value = "totalPrice")
    public int totalPrice = 0;

    @JsonProperty(value = "paymentPrice")
    public int paymentPrice = 0;

    @JsonProperty(value = "usePoint")
    public int usePoint = 0;

    @JsonProperty(value = "deposit")
    public String deposit = "";

    @JsonProperty(value = "deliveryInfo")
    public DeliveryInfo deliveryInfo;

    @JsonProperty(value = "bankingInfo")
    public BankingInfo bankingInfo;

    @JsonProperty(value = "ordersInfo")
    public PurchaseHistoryDetailData ordersInfo;
}
