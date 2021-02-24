package com.enliple.pudding.commons.network.vo;

import java.util.List;

/**
 * Created by Kim Joonsung on 2018-12-12.
 */
public class API82 {

    /**
     * result : success
     * nTotalCount : 1006
     * data : [{"rank":1,"mb_id":"hoon // 푸딩 선물한 사용자 ID","mb_nick":"hoooooon // 푸딩 선물한 사용자 닉네임","mb_user_img":"http://api.lieon.co.kr/images/test/test.png","cnt":"503 // 푸딩 선물한 수량"}]
     */

    public String result;
    public int nTotalCount;
    public List<CookieItem> data;

    public static class CookieItem {
        /**
         * rank : 1
         * mb_id : hoon // 푸딩 선물한 사용자 ID
         * mb_nick : hoooooon // 푸딩 선물한 사용자 닉네임
         * mb_user_img : http://api.lieon.co.kr/images/test/test.png
         * cnt : 503 // 푸딩 선물한 수량
         */

        public int rank;
        public String mb_id;
        public String mb_nick;
        public String mb_user_img;
        public String cnt;
    }
}
