package com.enliple.pudding.commons.network.vo;

import java.util.List;

/**
 * Created by Kim Joonsung on 2018-12-03.
 */
public class API94 {

    /**
     * result : success
     * nTotalCount : 1
     * data : [{"key":"hit_time","value":10}]
     */

    public String result;
    public int nTotalCount;
    public List<AppInfo> data;

    public static class AppInfo {
        /**
         * key : hit_time
         * value : 10
         */

        public String key;
        public int value;
    }
}
