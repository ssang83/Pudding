package com.enliple.pudding.commons.network.vo;

import java.util.List;

/**
 * Created by Kim Joonsung on 2019-03-21.
 */
public class API140 {

    /**
     * result : success
     * nTotalCount : 2
     * data : [{"am_idx":"115 // 알림 INDEX","am_msg":"1:1 문의 답변이 등록되었습니다. // 알림 메시지","am_status":"3 // 알림 상태 1=신규  2=읽음  3=레이어 삭제  4=페이지 삭제","am_reg_date":"2019-03-20 13:55:10 // 알림 시간"}]
     */

    public String result;
    public int nTotalCount;
    public List<NoticeItem> data;

    public static class NoticeItem {
        /**
         * am_idx : 115 // 알림 INDEX
         * am_msg : 1:1 문의 답변이 등록되었습니다. // 알림 메시지
         * am_status : 3 // 알림 상태 1=신규  2=읽음  3=레이어 삭제  4=페이지 삭제
         * am_reg_date : 2019-03-20 13:55:10 // 알림 시간
         */

        public String am_idx;
        public String am_msg;
        public String am_status;
        public String am_reg_date;
    }
}
