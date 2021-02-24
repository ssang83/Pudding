package com.enliple.pudding.commons.network.vo;

import java.util.List;

/**
 * Created by Kim Joonsung on 2018-12-26.
 */
public class API60 {

    /**
     * result : success
     * nTotalCount : 2
     * data : [{"send_mb_id":"보낸사람","cnt":6}]
     */

    public String result;
    public int nTotalCount;
    public List<DataBean> data;

    public static class DataBean {
        /**
         * send_mb_id : 보낸사람
         * cnt : 6
         */

        public String send_mb_id;
        public int cnt;
    }
}
