package com.enliple.pudding.commons.network.vo;

import java.util.List;

/**
 * Created by Kim Joonsung on 2019-01-04.
 */
public class API107 {

    /**
     * result : success
     * nTotalCount : 10
     * data : [{"sa_content":"신고를 너무 많이해서 더 이상 신고 처리 되지않습니다.  // 제재 내용","sa_reg_date":"2018-12-18 14:04:25 // 제재 날짜"}]
     */

    public String result;
    public int nTotalCount;
    public List<SancItem> data;

    public static class SancItem {
        /**
         * sa_content : 신고를 너무 많이해서 더 이상 신고 처리 되지않습니다.  // 제재 내용
         * sa_reg_date : 2018-12-18 14:04:25 // 제재 날짜
         */

        public String sa_content;
        public String sa_reg_date;
    }
}
