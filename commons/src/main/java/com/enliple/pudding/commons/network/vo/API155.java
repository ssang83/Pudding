package com.enliple.pudding.commons.network.vo;

import java.util.List;

public class API155 {

    public String result;
    public int nPointTotalCount;
    public int nCookieTotalCount;
    public ExchangeData data;


    public static class ExchangeData {
        /**
         * ev_id : 1542088929
         * ev_title : 동영상 이벤트 // 이벤트 제목
         * ev_type : vod // 이벤트 타입
         * ev_content : // 이벤트 내용
         * ev_todate : 2019-01-31 // 이벤트 종료일
         * main_img : http://lieon.co.kr/data/event/thumb/1548393419_main.jpg // 이벤트 메인 이미지
         * ev_hit : 0 // 이벤트 조회수
         */

        public String point;
        public String cookie;
        public List<PointHistoryItem> point_history;
        public List<CookieHistoryItem> cookie_history;
    }

    public static class PointHistoryItem {
        public String reg_date;
        public String point;
        public String status;
        public String price;
    }

    public static class CookieHistoryItem {
        public String reg_date;
        public String point;
        public String status;
        public String price;
    }
}
