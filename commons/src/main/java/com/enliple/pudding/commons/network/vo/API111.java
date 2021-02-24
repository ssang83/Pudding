package com.enliple.pudding.commons.network.vo;

/**
 * Created by Kim Joonsung on 2019-01-08.
 */
public class API111 {

    /**
     * result : success
     * nTotalCount : 1
     * data : {"version":"1.0.0 // 버전","is_force":"Y // 강제 여부","url":"// 스토어 URL","reg_date":"2019-01-07 14:26:32 // 등록일"}
     */

    public String result;
    public int nTotalCount;
    public VersionInfo data;

    public static class VersionInfo {
        /**
         * version : 1.0.0 // 버전
         * is_force : Y // 강제 여부
         * url : // 스토어 URL
         * reg_date : 2019-01-07 14:26:32 // 등록일
         */

        public String version;
        public String is_force;
        public String url;
        public String reg_date;
    }
}
