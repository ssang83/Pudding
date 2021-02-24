package com.enliple.pudding.commons.network.vo;

import java.util.List;

/**
 * Created by Kim Joonsung on 2018-11-21.
 */
public class API17 {

    /**
     * result : success
     * nTotalCount : 1
     * data : [{"idx":"댓글 인덱스","mb_nick":"사용자 닉네임","mb_profile":"프로필 URL","comment":"댓글 내용","reg_date":"댓글 등록일","is_mnne":"내 댓글 여부 (Y/N)"}]
     */

    public String result;
    public int nTotalCount;
    public List<BroadcastReplyItem> data;

    public static class BroadcastReplyItem {
        /**
         * idx : 댓글 인덱스
         * mb_nick : 사용자 닉네임
         * mb_profile : 프로필 URL
         * comment : 댓글 내용
         * reg_date : 댓글 등록일
         * is_mnne : 내 댓글 여부 (Y/N)
         */

        public String idx;
        public String mb_id;    // 사용자 ID
        public String mb_nick;
        public String mb_user_img;
        public String comment;
        public String reg_date;
        public String is_mine;
        public String is_use;   // Y:사용, N:삭제
        public String child_cnt;
        public List<ChildReplyItem> child;

        public static class ChildReplyItem {
            public String idx;
            public String mb_id;    // 사용자 ID
            public String mb_nick;
            public String mb_user_img;
            public String comment;
            public String reg_date;
            public String is_mine;
            public String is_use;   // Y:사용, N:삭제
        }
    }
}
