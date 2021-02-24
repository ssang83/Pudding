package com.enliple.pudding.commons.shoptree.response;

import com.enliple.pudding.commons.shoptree.data.CREData;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by Kim Joonsung on 2018-09-21.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class CREResponse extends BaseResponse {
    /**
     * 취소/환불/교환 내역
     */
    @JsonProperty(value = "orders")
    public List<CREData> creData;
}