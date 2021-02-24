package com.enliple.pudding.commons.network.vo;

import java.util.List;

/**
 * Created by Kim Joonsung on 2018-11-26.
 */
public class API30 {

    /**
     * result : success
     * nTotalCount : 3
     * data : [{"t_idx":9,"tag_name":"전망좋은집","update_count":7,"vod_cnt":6}]
     * pageCount : 1
     */

    public String result;
    public int nTotalCount;
    public String pageCount;
    public String nVodTotalCount;
    public String nTagTotalCount;
    public String nUserTotalCount;
    public String nProductTotalCount;
    public String nFormationTotalCount;
    public List<HashtagItem> data;

    public static class HashtagItem {
        /**
         * t_idx : 9
         * tag_name : 전망좋은집
         * update_count : 7
         * vod_cnt : 6
         */

        public String t_idx;
        public String tag_name;
        public int update_count;
        public int vod_cnt;
    }
}
