package com.enliple.pudding.commons.shoptree.response;

import com.enliple.pudding.commons.shoptree.data.PointHistoryData;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by Kim Joonsung on 2018-09-19.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PointHistoryResponse extends BaseResponse {
    /**
     * 사용 가능 포인트
     */
    @JsonProperty(value = "userPoint")
    public int userPoint = 0;

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

    /**
     * 포인트 히스토리 내역
     */
    @JsonProperty(value = "pointList")
    public List<PointHistoryData> pointList;
}
