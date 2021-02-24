package com.enliple.pudding.commons.network.vo;

import java.util.List;

/**
 * Created by Kim Joonsung on 2018-11-20.
 */
public class API11 {

    /**
     * result : success
     * events_cnt : 1
     * eventsList : [{"ev_id":1542088929,"ev_title":"동영상 이벤트 // 이벤트 제목","ev_type":"vod // 이벤트 타입","ev_content":"// 이벤트 내용","ev_todate":"2019-01-31 // 이벤트 종료일","main_img":"http://lieon.co.kr/data/event/thumb/1548393419_main.jpg // 이벤트 메인 이미지","ev_hit":"0 // 이벤트 조회수"}]
     */

    public String result;
    public int events_cnt;
    public List<EventsLisItem> eventsList;

    public static class EventsLisItem {
        /**
         * ev_id : 1542088929
         * ev_title : 동영상 이벤트 // 이벤트 제목
         * ev_type : vod // 이벤트 타입
         * ev_content : // 이벤트 내용
         * ev_todate : 2019-01-31 // 이벤트 종료일
         * main_img : http://lieon.co.kr/data/event/thumb/1548393419_main.jpg // 이벤트 메인 이미지
         * ev_hit : 0 // 이벤트 조회수
         */

        public String ev_id;
        public String ev_title;
        public String ev_type;
        public String ev_content;
        public String ev_todate;
        public String main_img;
        public String main_img_width;
        public String main_img_height;
        public String ev_hit;
        public String is_tab;
    }
}
