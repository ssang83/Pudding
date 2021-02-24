package com.enliple.pudding.commons.network.vo;

import java.util.List;

/**
 * Created by Kim Joonsung on 2018-11-26.
 */
public class API31 {

    /**
     * result : success
     * nTotalCount : 9
     * data : [{"mb_id":"사용자 아이디","mb_nick":"사용자 닉네임","mb_user_img":"사용자 이미지 URL 정보","follow_cnt":9,"favor_cnt":4,"is_follow":"Y"}]
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
    public List<UserItem> data;

    public static class UserItem {
        /**
         * mb_id : 사용자 아이디
         * mb_nick : 사용자 닉네임
         * mb_user_img : 사용자 이미지 URL 정보
         * follow_cnt : 9
         * favor_cnt : 4
         * is_follow : Y
         */

        public String mb_id;
        public String mb_nick;
        public String mb_user_img;
        public int follow_cnt;
        public String ca_id;
        public int favor_cnt;
        public String is_follow;
    }
}
