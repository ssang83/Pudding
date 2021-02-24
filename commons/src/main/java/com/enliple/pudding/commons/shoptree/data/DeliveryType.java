package com.enliple.pudding.commons.shoptree.data;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Kim Joonsung on 2018-09-20.
 */

public class DeliveryType {
    @JsonProperty(value = "count_total")
    public int totalCount;

    @JsonProperty(value = "init_pay")
    public int receivedOrder;

    @JsonProperty(value = "count_pay")
    public int paymentSuccess;

    @JsonProperty(value = "ready_pay")
    public int productPending;

    @JsonProperty(value = "delivery_pay")
    public int productDeliverying;

    @JsonProperty(value = "complete_pay")
    public int productDelivered;
}
