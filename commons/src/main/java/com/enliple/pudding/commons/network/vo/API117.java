package com.enliple.pudding.commons.network.vo;

import java.util.List;

/**
 * Created by Kim Joonsung on 2019-01-31.
 */
public class API117 {

    /**
     * result : success
     * nTotalCount : 2
     * data : {"evId":"2 // 이벤트 INDEX","evTitle":"동영상 이벤트 // 이벤트 제목","mainImg":"http://lieon.co.kr/data/event/thumb/1548393419_main.jpg // 이벤트 메인 이미지","sub_img1":"http://lieon.co.kr/data/event/thumb/1548393419_sub1.jpg // 이벤트 상세 이미지1","sub_img2":"// 이벤트 상세 이미지2","sub_img3":"// 이벤트 상세 이미지3","nPickTotalCount":3,"evPick":[{"themeName":"상품들   // 이벤트 상세 탭 이름","nThemeDataCount":1,"themeData":[{"idx":3312,"sc_code":"Z97J0Vk3rE","sitename":"반디몰","pcode":"A37e7VznPY","image1":"https://images.shop-tree.com/images/stores/Z97J0Vk3rE/products/1602406144398421_400.jpg","width":600,"height":600,"title":"멀티 자석드라이버","orgprice":0,"price":2100,"it_sc_type":2,"hit":8,"it_time":"2018-06-05T13:39:44.000Z","it_stock_qty":99,"fee_income":10,"shoptree_yn":"Y","pointRate":1,"category":"작업공구","link_url":{},"is_soldout":0,"is_use":1,"feetype":"조건부 무료","review_avg":0,"review_cnt":0,"wish_cnt":0,"is_wish":"N","is_cart":"N","discount":{}}]}]}
     */

    public String result;
    public int nTotalCount;
    public DataItem data;

    public static class DataItem {
        /**
         * evId : 2 // 이벤트 INDEX
         * evTitle : 동영상 이벤트 // 이벤트 제목
         * mainImg : http://lieon.co.kr/data/event/thumb/1548393419_main.jpg // 이벤트 메인 이미지
         * sub_img1 : http://lieon.co.kr/data/event/thumb/1548393419_sub1.jpg // 이벤트 상세 이미지1
         * sub_img2 : // 이벤트 상세 이미지2
         * sub_img3 : // 이벤트 상세 이미지3
         * nPickTotalCount : 3
         * evPick : [{"themeName":"상품들   // 이벤트 상세 탭 이름","nThemeDataCount":1,"themeData":[{"idx":3312,"sc_code":"Z97J0Vk3rE","sitename":"반디몰","pcode":"A37e7VznPY","image1":"https://images.shop-tree.com/images/stores/Z97J0Vk3rE/products/1602406144398421_400.jpg","width":600,"height":600,"title":"멀티 자석드라이버","orgprice":0,"price":2100,"it_sc_type":2,"hit":8,"it_time":"2018-06-05T13:39:44.000Z","it_stock_qty":99,"fee_income":10,"shoptree_yn":"Y","pointRate":1,"category":"작업공구","link_url":{},"is_soldout":0,"is_use":1,"feetype":"조건부 무료","review_avg":0,"review_cnt":0,"wish_cnt":0,"is_wish":"N","is_cart":"N","discount":{}}]}]
         */

        public String evId;
        public String evTitle;
        public String mainImg;
        public String sub_img1;
        public int sub_img1_width;
        public int sub_img1_height;
        public String sub_img2;
        public int sub_img2_width;
        public int sub_img2_height;
        public String sub_img3;
        public int sub_img3_width;
        public int sub_img3_height;
        public int nPickTotalCount;
        public List<EvPickItem> evPick;

        public static class EvPickItem {
            /**
             * themeName : 상품들   // 이벤트 상세 탭 이름
             * nThemeDataCount : 1
             * themeData : [{"idx":3312,"sc_code":"Z97J0Vk3rE","sitename":"반디몰","pcode":"A37e7VznPY","image1":"https://images.shop-tree.com/images/stores/Z97J0Vk3rE/products/1602406144398421_400.jpg","width":600,"height":600,"title":"멀티 자석드라이버","orgprice":0,"price":2100,"it_sc_type":2,"hit":8,"it_time":"2018-06-05T13:39:44.000Z","it_stock_qty":99,"fee_income":10,"shoptree_yn":"Y","pointRate":1,"category":"작업공구","link_url":{},"is_soldout":0,"is_use":1,"feetype":"조건부 무료","review_avg":0,"review_cnt":0,"wish_cnt":0,"is_wish":"N","is_cart":"N","discount":{}}]
             */

            public String themeName;
            public int nThemeDataCount;
            public List<ThemeDataItem> themeData;

            public static class ThemeDataItem {
                /**
                 * idx : 3312
                 * sc_code : Z97J0Vk3rE
                 * sitename : 반디몰
                 * pcode : A37e7VznPY
                 * image1 : https://images.shop-tree.com/images/stores/Z97J0Vk3rE/products/1602406144398421_400.jpg
                 * width : 600
                 * height : 600
                 * title : 멀티 자석드라이버
                 * orgprice : 0
                 * price : 2100
                 * it_sc_type : 2
                 * hit : 8
                 * it_time : 2018-06-05T13:39:44.000Z
                 * it_stock_qty : 99
                 * fee_income : 10
                 * shoptree_yn : Y
                 * pointRate : 1
                 * category : 작업공구
                 * link_url : {}
                 * is_soldout : 0
                 * is_use : 1
                 * feetype : 조건부 무료
                 * review_avg : 0
                 * review_cnt : 0
                 * wish_cnt : 0
                 * is_wish : N
                 * is_cart : N
                 * discount : {}
                 */

                public String idx;
                public String sc_code;
                public String sitename;
                public String pcode;
                public String image1;
                public String width;
                public String height;
                public String title;
                public String orgprice;
                public String price;
                public String it_sc_type;
                public String hit;
                public String it_time;
                public String it_stock_qty;
                public String fee_income;
                public String shoptree_yn;
                public String pointRate;
                public String category;
                public String link_url;
                public String is_soldout;
                public String is_use;
                public String feetype;
                public String review_avg;
                public int review_cnt;
                public int wish_cnt;
                public String is_wish;
                public String is_cart;
                public String discount;
                public String strType;
            }
        }
    }
}
