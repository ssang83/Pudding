package com.enliple.pudding.commons.network.vo;

import java.util.List;

/**
 * Created by Kim Joonsung on 2018-12-18.
 */
public class API72 {

    /**
     * status : true
     * title : 맘에안듬
     * stream_name : rtmp://enlipleup1.xst.kinxcdn.com/enliple/:v/1812181431004QQ6
     * chat_key : 1812181431004QQ6
     * ca_id1 : 5010
     * ca_id2 :  5020
     * ca_id3 :
     * is_live : N
     * live_img : http://api.lieon.co.kr/images/live/android4/Screenshot_20180405-1626123.png
     * room_passwd : null
     * show_YN : N
     * tag : 테스트
     * noti : null
     * stream_key : 1812181431004QQ6
     * relationPrd : {"data":[{"idx":"39","sc_code":"z04vp8vERx","pcode":"8mb3gGB3oE","strType":"1","strPrdCategory":"여성의류","strPrdName":"라라 플라워 시스루 쉬폰 원피스","strPrdMaker":"","strPrdOrigin":"","strPrdBrand":"","strPrdImg":"https://images-stage.shaptree.com/images/stores/z04vp8vERx/products/1605049738101439_400.jpg","nPrdCustPrice":"50000","nPrdSellPrice":"44900","isUse":"1","isSoldOut":"0","nPrdStockQty":"207"}],"nTotalCount":1}
     */

    public boolean status;
    public String title;
    public String stream_name;
    public String chat_key;
    public String ca_id1;
    public String ca_id2;
    public String ca_id3;
    public String is_live;
    public String live_img;
    public Object room_passwd;
    public String show_YN;
    public String tag;
    public Object noti;
    public String stream_key;
    public RelationPrdBean relationPrd;

    public static class RelationPrdBean {
        /**
         * data : [{"idx":"39","sc_code":"z04vp8vERx","pcode":"8mb3gGB3oE","strType":"1","strPrdCategory":"여성의류","strPrdName":"라라 플라워 시스루 쉬폰 원피스","strPrdMaker":"","strPrdOrigin":"","strPrdBrand":"","strPrdImg":"https://images-stage.shaptree.com/images/stores/z04vp8vERx/products/1605049738101439_400.jpg","nPrdCustPrice":"50000","nPrdSellPrice":"44900","isUse":"1","isSoldOut":"0","nPrdStockQty":"207"}]
         * nTotalCount : 1
         */

        public int nTotalCount;
        public List<ProductItem> data;

        public static class ProductItem {
            /**
             * idx : 39
             * sc_code : z04vp8vERx
             * pcode : 8mb3gGB3oE
             * strType : 1
             * strPrdCategory : 여성의류
             * strPrdName : 라라 플라워 시스루 쉬폰 원피스
             * strPrdMaker :
             * strPrdOrigin :
             * strPrdBrand :
             * strPrdImg : https://images-stage.shaptree.com/images/stores/z04vp8vERx/products/1605049738101439_400.jpg
             * nPrdCustPrice : 50000
             * nPrdSellPrice : 44900
             * isUse : 1
             * isSoldOut : 0
             * nPrdStockQty : 207
             */

            public String idx;
            public String sc_code;
            public String pcode;
            public String sc_name;
            public String strType;
            public String strPrdCategory;
            public String strPrdName;
            public String strPrdMaker;
            public String strPrdOrigin;
            public String strPrdBrand;
            public String strPrdImg;
            public String nPrdCustPrice;
            public String nPrdSellPrice;
            public String isUse;
            public String isSoldOut;
            public String nPrdStockQty;
            public String strLinkUrl;
            public String is_wish;
            public String wish_cnt;
            public String is_cart;
        }
    }
}
