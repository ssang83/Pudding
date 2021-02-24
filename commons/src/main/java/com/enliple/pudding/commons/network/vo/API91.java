package com.enliple.pudding.commons.network.vo;

import java.util.List;

/**
 * Created by Kim Joonsung on 2018-12-05.
 */
public class API91 {

    /**
     * result : success
     * nTotalCount : 1
     * data : [{"mb_id":"상품 문의 작성자 아이디","iq_secret":"비밀글 여부 0 , 1","iq_name":"안***  상품 문의 작성자 이름 [뒤에 3글자 *표시]","iq_type":"전체 // 상품 문의 타입","iq_subject":"test  // 상품 문의 제목 [사용안함]","iq_question":"비밀글로 보호된 문의입니다. // 상품 문의 내용","iq_time":"2018-11-30 14:58:17  // 상품 문의 작성 일시","answer":[{"iq_answer":"답변입니다.","iq_time":"2018-11-30 14:58:17 // 상품 문의 답급 내용 작성 일시"}]}]
     */

    public String result;
    public int nTotalCount;
    public int nMyTotalCount;
    public List<QnaItem> data;

    public static class QnaItem {
        /**
         * mb_id : 상품 문의 작성자 아이디
         * iq_secret : 비밀글 여부 0 , 1
         * iq_name : 안***  상품 문의 작성자 이름 [뒤에 3글자 *표시]
         * iq_type : 전체 // 상품 문의 타입
         * iq_subject : test  // 상품 문의 제목 [사용안함]
         * iq_question : 비밀글로 보호된 문의입니다. // 상품 문의 내용
         * iq_time : 2018-11-30 14:58:17  // 상품 문의 작성 일시
         * answer : [{"iq_answer":"답변입니다.","iq_time":"2018-11-30 14:58:17 // 상품 문의 답급 내용 작성 일시"}]
         */

        public String mb_id;
        public String iq_secret;
        public String iq_name;
        public String iq_type;
        public String iq_subject;
        public String iq_question;
        public String iq_time;
        public List<AnswerItem> answer;

        public static class AnswerItem {
            /**
             * iq_answer : 답변입니다.
             * iq_time : 2018-11-30 14:58:17 // 상품 문의 답급 내용 작성 일시
             */

            public String iq_answer;
            public String iq_time;
        }
    }
}
