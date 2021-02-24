package com.enliple.pudding.commons.network.vo;

import java.util.List;

/**
 * Created by Kim Joonsung on 2019-01-16.
 */
public class API113 {

    /**
     * result : success
     * nTotalCount : 9
     * data : [{"mb_id":"google_80a808261","mb_nick":"권혁노","mb_user_img":"","follow_cnt":"0","favor_cnt":"0,","is_follow":"N","category":10}]
     */

    public String result;
    public int nTotalCount;
    public List<UserItem> data;

    public static class UserItem {
        /**
         * mb_id : google_80a808261
         * mb_nick : 권혁노
         * mb_user_img :
         * follow_cnt : 0
         * favor_cnt : 0,
         * is_follow : N
         * category : 10
         */

        public String mb_id;
        public String mb_nick;
        public String mb_user_img;
        public String follow_cnt;
        public String favor_cnt;
        public String is_follow;
        public String ca_id;
    }
}
