package com.enliple.pudding.commons.network.vo;

import java.util.List;

/**
 * Created by Kim Joonsung on 2019-01-03.
 */
public class API102 {

    /**
     * result : success
     * nTotalCount : 28
     * data : [{"content":"상품구매 // 포인트 내역 내용","od_id":"2018121017472518 // 포인트 적립 예정 주문번호","status":"배송완료일로부터 15일 후 적립예정 // 포인트 상태","point":"2 // 적립 예정 포인트","reg_date":"2018-12-10 // 주문번호 있을 경우 상품구매일  없는경우 포인트 적립 사용일"}]
     */

    public String result;
    public int nTotalCount;
    public List<SaveItem> data;

    public static class SaveItem {
        /**
         * content : 상품구매 // 포인트 내역 내용
         * od_id : 2018121017472518 // 포인트 적립 예정 주문번호
         * status : 배송완료일로부터 15일 후 적립예정 // 포인트 상태
         * point : 2 // 적립 예정 포인트
         * reg_date : 2018-12-10 // 주문번호 있을 경우 상품구매일  없는경우 포인트 적립 사용일
         */

        public String content;
        public String od_id;
        public String status;
        public String point;
        public String reg_date;
    }
}
