package com.enliple.pudding.commons.shoptree.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Kim Joonsung on 2018-09-18.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BaseResponse {
    @JsonProperty(value = "result")
    public String result = "fail";

    @JsonProperty(value = "resultMessage")
    public String resultMessage = "";

    @JsonProperty(value = "resultCode")
    public String resultCode = "";

    public String getResult() {
        return result.toUpperCase();
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public String getResultCode() {
        return resultCode;
    }
}
