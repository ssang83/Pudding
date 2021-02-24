package com.enliple.pudding.commons.network.vo;

import java.util.List;

/**
 * Created by Kim Joonsung on 2018-12-26.
 */
public class API63{

    /**
     * result : success
     * nTotalCount : 2
     * data : [{"no":1101,"send_mb_id":"보낸사람","recv_mb_id":"받는 사람","datetime":"2018-12-18 17:28:36","content":"메시지 내용"}]
     */

    public String result;
    public int nTotalCount;
    public List<MyMessageItem> data;

    public static class MyMessageItem {
        /**
         * no : 1101
         * send_mb_id : 보낸사람
         * recv_mb_id : 받는 사람
         * datetime : 2018-12-18 17:28:36
         * content : 메시지 내용
         */

        public int no;
        public String send_mb_id;
        public String send_mb_nick;
        public String send_mb_img;
        public String datetime;
        public String content;
    }
}
