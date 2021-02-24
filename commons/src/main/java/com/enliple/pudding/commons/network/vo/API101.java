package com.enliple.pudding.commons.network.vo;

import java.util.List;

/**
 * Created by Kim Joonsung on 2019-01-03.
 */
public class API101 {

    /**
     * result : success
     * nTotalCount : 28
     * point : 3053  포인트 정보
     * save_point : 4 적립 예정 포인트
     * exapire_point : 200 소멸 예정 포인트
     * pageCount : 0
     * data : [{"content":"주문 상품 구매 확정 // 포인트 내역 내용","od_id":"2018121109491612 // 포인트 적립 주문번호","status":"적립 // 포인트 상태","point":"2 // 포인트","reg_date":"2018-12-11 // 주문번호 있을 경우 상품구매일, 없는경우 포인트 적립,사용일","expired_date":"9999-12-31 // 포인트 만료일"}]
     */

    public String result;
    public int nTotalCount;
    public String point;
    public String save_point;
    public String expire_point;
    public int pageCount;
    public List<PointItem> data;

    public static class PointItem {
        /**
         * content : 주문 상품 구매 확정 // 포인트 내역 내용
         * od_id : 2018121109491612 // 포인트 적립 주문번호
         * status : 적립 // 포인트 상태
         * point : 2 // 포인트
         * reg_date : 2018-12-11 // 주문번호 있을 경우 상품구매일, 없는경우 포인트 적립,사용일
         * expired_date : 9999-12-31 // 포인트 만료일
         */

        public String content;
        public String od_id;
        public String status;
        public String point;
        public String reg_date;
        public String expired_date;
    }
}
