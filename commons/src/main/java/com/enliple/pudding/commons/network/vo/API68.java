package com.enliple.pudding.commons.network.vo;

import java.util.List;

/**
 * Created by Kim Joonsung on 2018-12-19.
 */
public class API68 {

    /**
     * result : success
     * nTotalCount : 1
     * data : {"orders":[{"items":[{"item":[{"itemKey":"8YWJ9gyKM1 // 샵트리 구매 상품 키","it_id":"6 // 라이온 상품 INDEX","ct_id":"944 // 장바구니 INDEX","img":"https://images-stage.shaptree.com/images/stores/Jo7nRpZwye/products/1605843950331292_400.jpg // 상품 이미지 URL","title":"미니 피규어 세트 // 상품 이름","option":"100/블루 / 1개  // 상품 옵션","price":"10000 // 상품 가격","deliveryPrice":"0 // 상품 배송비","status":"구매 확정  // 상품 상태","trackingInfo_name":"(NULL) // 상품 배송회사","trackingInfo_code":"(NULL) // 상품 배송 운송코드","trackingInfo_url":"// 상품 배송조회 URL"}],"store_name":"유찬몰"}],"orderNumber":2018111613050329,"regdate":"2018-11-16T13:05:03.000Z"}]}
     */

    public String result;
    public int nTotalCount;
    public DataBean data;

    public static class DataBean {
        public List<OrderItem> orders;

        public static class OrderItem {
            /**
             * items : [{"item":[{"itemKey":"8YWJ9gyKM1 // 샵트리 구매 상품 키","it_id":"6 // 라이온 상품 INDEX","ct_id":"944 // 장바구니 INDEX","img":"https://images-stage.shaptree.com/images/stores/Jo7nRpZwye/products/1605843950331292_400.jpg // 상품 이미지 URL","title":"미니 피규어 세트 // 상품 이름","option":"100/블루 / 1개  // 상품 옵션","price":"10000 // 상품 가격","deliveryPrice":"0 // 상품 배송비","status":"구매 확정  // 상품 상태","trackingInfo_name":"(NULL) // 상품 배송회사","trackingInfo_code":"(NULL) // 상품 배송 운송코드","trackingInfo_url":"// 상품 배송조회 URL"}],"store_name":"유찬몰"}]
             * orderNumber : 2018111613050329
             * regdate : 2018-11-16T13:05:03.000Z
             */

            public String orderNumber;
            public String regdate;
            public List<ItemsBean> items;

            public static class ItemsBean {
                /**
                 * item : [{"itemKey":"8YWJ9gyKM1 // 샵트리 구매 상품 키","it_id":"6 // 라이온 상품 INDEX","ct_id":"944 // 장바구니 INDEX","img":"https://images-stage.shaptree.com/images/stores/Jo7nRpZwye/products/1605843950331292_400.jpg // 상품 이미지 URL","title":"미니 피규어 세트 // 상품 이름","option":"100/블루 / 1개  // 상품 옵션","price":"10000 // 상품 가격","deliveryPrice":"0 // 상품 배송비","status":"구매 확정  // 상품 상태","trackingInfo_name":"(NULL) // 상품 배송회사","trackingInfo_code":"(NULL) // 상품 배송 운송코드","trackingInfo_url":"// 상품 배송조회 URL"}]
                 * store_name : 유찬몰
                 */

                public String store_name;
                public List<ProductItem> item;

                public static class ProductItem {
                    /**
                     * itemKey : 8YWJ9gyKM1 // 샵트리 구매 상품 키
                     * it_id : 6 // 라이온 상품 INDEX
                     * ct_id : 944 // 장바구니 INDEX
                     * img : https://images-stage.shaptree.com/images/stores/Jo7nRpZwye/products/1605843950331292_400.jpg // 상품 이미지 URL
                     * title : 미니 피규어 세트 // 상품 이름
                     * option : 100/블루 / 1개  // 상품 옵션
                     * price : 10000 // 상품 가격
                     * deliveryPrice : 0 // 상품 배송비
                     * status : 구매 확정  // 상품 상태
                     * trackingInfo_name : (NULL) // 상품 배송회사
                     * trackingInfo_code : (NULL) // 상품 배송 운송코드
                     * trackingInfo_url : // 상품 배송조회 URL
                     */

                    public String itemKey;
                    public String it_id;
                    public String ct_id;
                    public String img;
                    public String title;
                    public String option;
                    public String price;
                    public String deliveryPrice;
                    public String status;
                    public String trackingInfo_name;
                    public String trackingInfo_code;
                    public String trackingInfo_url;
                }
            }
        }
    }
}
