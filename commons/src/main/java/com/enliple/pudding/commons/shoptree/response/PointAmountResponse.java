package com.enliple.pudding.commons.shoptree.response;

import com.enliple.pudding.commons.shoptree.data.PointAmountData;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by Kim Joonsung on 2018-09-20.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PointAmountResponse extends BaseResponse {
    /**
     * 적립 예정 포인트
     */
    @JsonProperty(value = "userSchedulePoint")
    public int userSchedulePoint = 0;

    /**
     * 소멸 예정 금액
     */
    @JsonProperty(value = "userExtinctionPoint")
    public int userExtinctionPoint = 0;

    @JsonProperty(value = "pointList")
    public List<PointAmountData> pointList;
}
