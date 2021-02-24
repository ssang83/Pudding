package com.enliple.pudding.commons.shoptree.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Kim Joonsung on 2018-09-19.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BankingInfo {
    @JsonProperty(value = "pdate")
    public String pdate;

    @JsonProperty(value = "price")
    public int price = 0;
}
