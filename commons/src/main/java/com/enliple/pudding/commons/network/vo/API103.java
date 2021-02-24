package com.enliple.pudding.commons.network.vo;

import java.util.List;

/**
 * Created by Kim Joonsung on 2019-01-03.
 */
public class API103 {

    /**
     * result : success
     * nTotalCount : 28
     * data : [{"content":"2018-09-04 첫로그인 // 포인트 내역 내용","od_id":"// 포인트 적립 주문번호","status":"소멸예정 // 포인트 상태","point":"100 // 소멸 예정 포인트","reg_date":"2018-09-04 // 주문번호 있을 경우 상품구매일, 없는경우 포인트 적립일","expired_date":"2018-11-30 // 소멸 예정일"}]
     */

    public String result;
    public int nTotalCount;
    public List<ExpireItem> data;

    public static class ExpireItem {
        /**
         * content : 2018-09-04 첫로그인 // 포인트 내역 내용
         * od_id : // 포인트 적립 주문번호
         * status : 소멸예정 // 포인트 상태
         * point : 100 // 소멸 예정 포인트
         * reg_date : 2018-09-04 // 주문번호 있을 경우 상품구매일, 없는경우 포인트 적립일
         * expired_date : 2018-11-30 // 소멸 예정일
         */

        public String content;
        public String od_id;
        public String status;
        public String point;
        public String reg_date;
        public String expired_date;
    }
}
