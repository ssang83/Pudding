package com.enliple.pudding.commons.network.vo;

import java.util.List;

/**
 * Created by Kim Joonsung on 2019-01-07.
 */
public class API110 {

    /**
     * result : success
     * nTotalCount : 1
     * data : [{"notice":"N // 공지 여부 [Y|N]","subject":"공지사항 제목 // 공지 제목","content":"공지사항 내용 // 공지 내용 [HTML]","reg_date":"2018-08-31 11:45:43 // 공지 작성일"}]
     */

    public String result;
    public int nTotalCount;
    public List<NoticeItem> data;

    public static class NoticeItem {
        /**
         * notice : N // 공지 여부 [Y|N]
         * subject : 공지사항 제목 // 공지 제목
         * content : 공지사항 내용 // 공지 내용 [HTML]
         * reg_date : 2018-08-31 11:45:43 // 공지 작성일
         */

        public String id;
        public String notice;
        public String subject;
        public String content;
        public String reg_date;
    }
}
