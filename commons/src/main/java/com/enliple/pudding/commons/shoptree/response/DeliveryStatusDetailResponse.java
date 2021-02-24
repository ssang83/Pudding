package com.enliple.pudding.commons.shoptree.response;

import com.enliple.pudding.commons.shoptree.data.DeliveryInfo;
import com.enliple.pudding.commons.shoptree.data.DeliveryStatusDetailData;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Kim Joonsung on 2018-09-20.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeliveryStatusDetailResponse extends BaseResponse {
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

    @JsonProperty(value = "deliveryInfo")
    public DeliveryInfo deliveryInfo;

    @JsonProperty(value = "ordersInfo")
    public DeliveryStatusDetailData ordersInfo;

}
