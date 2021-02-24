package com.enliple.pudding.commons.network.vo;

import java.util.List;

public class API39 {
    public String result;
    public String nTotalCount;
    public List<MyOrderData> data;

    public class MyOrderData {
        public String order_id; // 주문 아이디
        public String cart_id; // 카트 아이디
        public String item_id; // 아이템 아이디
        public String item_name; // 상품 이름
        public String option; // 상품 옵션명
        public String ct_status; // 상품 진행 내용
        public String time; // 구입 시기
    }
}
