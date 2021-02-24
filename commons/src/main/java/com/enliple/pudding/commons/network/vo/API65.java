package com.enliple.pudding.commons.network.vo;

import java.util.List;

/**
 * Created by Kim Joonsung on 2018-12-20.
 */
public class API65 {

    /**
     * result : success
     * nTotalCount : 1
     * data : {"message_YN":"N // 메시지 알림","event_YN":"N // 이벤트 알림","live_yn":"Y // 라이브 방송 알림","reg_date":"2018-12-17 14:45:35\", // 알림 등록 시간","follow_alarm":[{"mb_id":"hoon // 팔로우 회원 ID","mb_nick":"hoooooon // 팔로우 회원 닉네임","mb_user_img":"// 팔로우 회원 이미지 URL","is_alarm":"N // 팔로우 회원 알림 여부"}]}
     */

    public String result;
    public int nTotalCount;
    public AlarmItem data;

    public static class AlarmItem {
        /**
         * message_YN : N // 메시지 알림
         * event_YN : N // 이벤트 알림
         * live_yn : Y // 라이브 방송 알림
         * reg_date : 2018-12-17 14:45:35", // 알림 등록 시간
         * follow_alarm : [{"mb_id":"hoon // 팔로우 회원 ID","mb_nick":"hoooooon // 팔로우 회원 닉네임","mb_user_img":"// 팔로우 회원 이미지 URL","is_alarm":"N // 팔로우 회원 알림 여부"}]
         */

        public String message_YN;
        public String event_YN;
        public String live_YN;
        public String reg_date;
        public List<FollowAlarmItem> follow_alarm;

        public static class FollowAlarmItem {
            /**
             * mb_id : hoon // 팔로우 회원 ID
             * mb_nick : hoooooon // 팔로우 회원 닉네임
             * mb_user_img : // 팔로우 회원 이미지 URL
             * is_alarm : N // 팔로우 회원 알림 여부
             */

            public String mb_id;
            public String mb_nick;
            public String mb_user_img;
            public String is_alarm;
        }
    }
}
