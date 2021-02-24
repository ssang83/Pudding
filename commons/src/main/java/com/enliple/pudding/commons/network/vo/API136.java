package com.enliple.pudding.commons.network.vo;

import java.util.List;

/**
 * Created by Kim Joonsung on 2019-03-19.
 */
public class API136 {

    /**
     * status : true
     * title : 방송 제목
     * stream_name : rtmp://enlipleup1.xst.kinxcdn.com/enliple/:v/YZ6TejebDtpI
     * chat_key : YZ6TejebDtpI
     * ca_id1 : 1010
     * ca_id2 : 1020
     * ca_id3 : 1030
     * is_live : N
     * live_img : http://api.lieon.co.kr/images/test/large.jpg
     * room_passwd : 미사용 컬럼 추후 적용
     * show_YN : Y
     * tag : 전망좋은집,아파트,전원주택
     * noit : 공지사항입니다
     * stream_key : 생방송 키
     * relationPrd : {"nTotalCount":4,"data":[{"idx":"상품 idx","sc_code":"샵트리 상점 KEY","pcode":"샵트리 상품 key","strType":"상품 정보 타입 (1=상품, 2=링크, 3=샘플)","strPrdCategory":"상품 카테고리명","strPrdName":"상품명","strPrdMaker":"상품 제조자","strPrdOrigin":"상품 원산지","strPrdBrand":"상품 브랜드","strPrdImg":"상품 썸네일 URL","nPrdCustPrice":"상품 희망 소비자 가격","nPrdSellPrice":"상품 판매 가격","isUse":"판매여부, 판매중:1, 판매중지:0","isSoldOut":"품절여부, 품절:1, 재고있음:0","nPrdStockQty":"재고수량","strLinkUrl":"http://m.imvely.com/product/detail.html?product_no=15884&cate_no=1&display_group=32"}]}
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
    public String room_passwd;
    public String show_YN;
    public String tag;
    public String noti;
    public String stream_key;
    public Products relationPrd;

    public static class Products {
        /**
         * nTotalCount : 4
         * data : [{"idx":"상품 idx","sc_code":"샵트리 상점 KEY","pcode":"샵트리 상품 key","strType":"상품 정보 타입 (1=상품, 2=링크, 3=샘플)","strPrdCategory":"상품 카테고리명","strPrdName":"상품명","strPrdMaker":"상품 제조자","strPrdOrigin":"상품 원산지","strPrdBrand":"상품 브랜드","strPrdImg":"상품 썸네일 URL","nPrdCustPrice":"상품 희망 소비자 가격","nPrdSellPrice":"상품 판매 가격","isUse":"판매여부, 판매중:1, 판매중지:0","isSoldOut":"품절여부, 품절:1, 재고있음:0","nPrdStockQty":"재고수량","strLinkUrl":"http://m.imvely.com/product/detail.html?product_no=15884&cate_no=1&display_group=32"}]
         */

        public int nTotalCount;
        public List<ProductItem> data;

        public static class ProductItem {
            /**
             * idx : 상품 idx
             * sc_code : 샵트리 상점 KEY
             * pcode : 샵트리 상품 key
             * strType : 상품 정보 타입 (1=상품, 2=링크, 3=샘플)
             * strPrdCategory : 상품 카테고리명
             * strPrdName : 상품명
             * strPrdMaker : 상품 제조자
             * strPrdOrigin : 상품 원산지
             * strPrdBrand : 상품 브랜드
             * strPrdImg : 상품 썸네일 URL
             * nPrdCustPrice : 상품 희망 소비자 가격
             * nPrdSellPrice : 상품 판매 가격
             * isUse : 판매여부, 판매중:1, 판매중지:0
             * isSoldOut : 품절여부, 품절:1, 재고있음:0
             * nPrdStockQty : 재고수량
             * strLinkUrl : http://m.imvely.com/product/detail.html?product_no=15884&cate_no=1&display_group=32
             */

            public String idx;
            public String sc_code;
            public String pcode;
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
        }
    }
}
