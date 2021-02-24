package com.enliple.pudding.commons.shoptree.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ComplainCancelForm {
    @JsonProperty(value = "result")
    public String result = "fail";

    @JsonProperty(value = "cancel_price")
    public String cancel_price = "";

    @JsonProperty(value = "category")
    public List<String> category = null;

    public String getResult() {
        return result.toUpperCase();
    }
}
