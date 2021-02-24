package com.enliple.pudding.commons.network.vo;

import java.util.List;

/**
 * Created by Kim Joonsung on 2019-03-18.
 */
public class API130 {

    /**
     * result : success
     * nTotalLastCount : 28 // 이전 내역 갯수
     * lastData : [{"mb_id":"rinno83  // 편성표 등록 ID","fo_date":"2019030303  // 편성표 예약일자[format:yyyymmddH]","fo_year":"2019  // 편성표 예약년도[format:yyyy]","fo_month":"03  // 편성표 예약월[mm]","fo_day":"03  // 편성표 예약일[dd]","fo_hour":"03  // 편성표 예약시간[H]","title":"편성표 테스트5  // 편성표 제목","stream_key":"190110151951HpGC  // 편성표 StreamKey","category":"10  // 편성표 1차 카테고리","ca_id":"1020  // 편성표 2차 카테고리","ca_id2":"1010  // 편성표 2차 카테고리","ca_id3":"null, // 편성표 2차 카테고리","live_img_width":"290  // 편성표 이미지 가로 사이즈","live_img_height":"218  // 편성표 이미지 세로 사이즈","live_img":"http://api.lieon.co.kr/images/live/showthat/ff8a543ffc546780a8ce9683f1dddcab.jpg  // 편성표 이미지 URL","tag":"formation,test  // 편성표 태그[,로 구분]","reservation_cnt":"0  // 편성표 시청예약 수","notice":"test  // 편성표 공지사항","fo_status":"1  // 편성표 상태[0:승인대기중,1:승인완료,2:승인거절]","status_date":"2019-03-07 15:48:08  // 편성표 상태 변경 일자[format:yyyy-mm-dd H:i:s]","cancel_reason":"null, // 편성표 승인 거절 사유","mb_nick":"rinno83  // 편성표 등록 회원 ID","mb_user_img":"// 편성표 등록 회원 프로필 이미지 URL","live_type":"empty  // 편성표 방송 상태[empty:방송전, live:방송중, lastlive:지난방송]","is_mine":"N  // 내 편성표 여부","live_possible":"N  // 편성표 방송 가능 여부","my_alarm":"Y  // 내 시청 예약 여부","items":{"nTotalCount":0,"data":[{"idx":"상품 idx","strType":"상품 타입","strPrdCategory":"상품 카테고리명","strPrdName":"상품명","strPrdMaker":"상품 제조자","strPrdOrigin":"상품 원산지","strPrdBrand":"상품 브랜드","strPrdImg":"상품 이미지 URL","nPrdCustPrice":"희망 소비자 가격","nPrdSellPrice":"판매 가격","isUse":"판매중:1, 판매중지:0","isSoldOut":"품절:1, 재고있음:0","nPrdStockQty":"상품 재고 수량","strLinkUrl":"http://m.imvely.com/product/detail.html?product_no=15884&cate_no=1&display_group=32"}]}}]
     * nTotalCount : 0 // 예약 내역 갯수
     * data : [{"mb_id":"rinno83  // 편성표 등록 ID","fo_date":"2019030303  // 편성표 예약일자[format:yyyymmddH]","fo_year":"2019  // 편성표 예약년도[format:yyyy]","fo_month":"03  // 편성표 예약월[mm]","fo_day":"03  // 편성표 예약일[dd]","fo_hour":"03  // 편성표 예약시간[H]","title":"편성표 테스트5  // 편성표 제목","stream_key":"190110151951HpGC  // 편성표 StreamKey","category":"10  // 편성표 1차 카테고리","ca_id":"1020  // 편성표 2차 카테고리","ca_id2":"1010  // 편성표 2차 카테고리","ca_id3":"null, // 편성표 2차 카테고리","live_img_width":"290  // 편성표 이미지 가로 사이즈","live_img_height":"218  // 편성표 이미지 세로 사이즈","live_img":"http://api.lieon.co.kr/images/live/showthat/ff8a543ffc546780a8ce9683f1dddcab.jpg  // 편성표 이미지 URL","tag":"formation,test  // 편성표 태그[,로 구분]","reservation_cnt":"0  // 편성표 시청예약 수","notice":"test  // 편성표 공지사항","fo_status":"1  // 편성표 상태[0:승인대기중,1:승인완료,2:승인거절]","status_date":"2019-03-07 15:48:08  // 편성표 상태 변경 일자[format:yyyy-mm-dd H:i:s]","cancel_reason":"null, // 편성표 승인 거절 사유","mb_nick":"rinno83  // 편성표 등록 회원 ID","mb_user_img":"// 편성표 등록 회원 프로필 이미지 URL","live_type":"empty  // 편성표 방송 상태[empty:방송전, live:방송중, lastlive:지난방송]","is_mine":"N  // 내 편성표 여부","live_possible":"N  // 편성표 방송 가능 여부","my_alarm":"Y  // 내 시청 예약 여부","items":{"nTotalCount":0,"data":[{"idx":"상품 idx","strType":"상품 타입","strPrdCategory":"상품 카테고리명","strPrdName":"상품명","strPrdMaker":"상품 제조자","strPrdOrigin":"상품 원산지","strPrdBrand":"상품 브랜드","strPrdImg":"상품 이미지 URL","nPrdCustPrice":"희망 소비자 가격","nPrdSellPrice":"판매 가격","isUse":"판매중:1, 판매중지:0","isSoldOut":"품절:1, 재고있음:0","nPrdStockQty":"상품 재고 수량","strLinkUrl":"http://m.imvely.com/product/detail.html?product_no=15884&cate_no=1&display_group=32"}]}}]
     */

    public String result;
    public int nTotalLastCount;
    public int nTotalCount;
    public List<PreviousItem> lastData;
    public List<ReservationItem> data;

    public static class PreviousItem {
        /**
         * mb_id : rinno83  // 편성표 등록 ID
         * fo_date : 2019030303  // 편성표 예약일자[format:yyyymmddH]
         * fo_year : 2019  // 편성표 예약년도[format:yyyy]
         * fo_month : 03  // 편성표 예약월[mm]
         * fo_day : 03  // 편성표 예약일[dd]
         * fo_hour : 03  // 편성표 예약시간[H]
         * title : 편성표 테스트5  // 편성표 제목
         * stream_key : 190110151951HpGC  // 편성표 StreamKey
         * category : 10  // 편성표 1차 카테고리
         * ca_id : 1020  // 편성표 2차 카테고리
         * ca_id2 : 1010  // 편성표 2차 카테고리
         * ca_id3 : null, // 편성표 2차 카테고리
         * live_img_width : 290  // 편성표 이미지 가로 사이즈
         * live_img_height : 218  // 편성표 이미지 세로 사이즈
         * live_img : http://api.lieon.co.kr/images/live/showthat/ff8a543ffc546780a8ce9683f1dddcab.jpg  // 편성표 이미지 URL
         * tag : formation,test  // 편성표 태그[,로 구분]
         * reservation_cnt : 0  // 편성표 시청예약 수
         * notice : test  // 편성표 공지사항
         * fo_status : 1  // 편성표 상태[0:승인대기중,1:승인완료,2:승인거절]
         * status_date : 2019-03-07 15:48:08  // 편성표 상태 변경 일자[format:yyyy-mm-dd H:i:s]
         * cancel_reason : null, // 편성표 승인 거절 사유
         * mb_nick : rinno83  // 편성표 등록 회원 ID
         * mb_user_img : // 편성표 등록 회원 프로필 이미지 URL
         * live_type : empty  // 편성표 방송 상태[empty:방송전, live:방송중, lastlive:지난방송]
         * is_mine : N  // 내 편성표 여부
         * live_possible : N  // 편성표 방송 가능 여부
         * my_alarm : Y  // 내 시청 예약 여부
         * items : {"nTotalCount":0,"data":[{"idx":"상품 idx","strType":"상품 타입","strPrdCategory":"상품 카테고리명","strPrdName":"상품명","strPrdMaker":"상품 제조자","strPrdOrigin":"상품 원산지","strPrdBrand":"상품 브랜드","strPrdImg":"상품 이미지 URL","nPrdCustPrice":"희망 소비자 가격","nPrdSellPrice":"판매 가격","isUse":"판매중:1, 판매중지:0","isSoldOut":"품절:1, 재고있음:0","nPrdStockQty":"상품 재고 수량","strLinkUrl":"http://m.imvely.com/product/detail.html?product_no=15884&cate_no=1&display_group=32"}]}
         */

        public String mb_id;
        public String fo_date;
        public String fo_year;
        public String fo_month;
        public String fo_day;
        public String fo_hour;
        public String title;
        public String stream_key;
        public String category;
        public String ca_id;
        public String ca_id2;
        public String ca_id3;
        public String live_img_width;
        public String live_img_height;
        public String live_img;
        public String tag;
        public String reservation_cnt;
        public String notice;
        public String fo_status;
        public String status_date;
        public String cancel_reason;
        public String mb_nick;
        public String mb_user_img;
        public String live_type;
        public String is_mine;
        public String live_possible;
        public String my_alarm;
        public Products items;

        public static class Products {
            /**
             * nTotalCount : 0
             * data : [{"idx":"상품 idx","strType":"상품 타입","strPrdCategory":"상품 카테고리명","strPrdName":"상품명","strPrdMaker":"상품 제조자","strPrdOrigin":"상품 원산지","strPrdBrand":"상품 브랜드","strPrdImg":"상품 이미지 URL","nPrdCustPrice":"희망 소비자 가격","nPrdSellPrice":"판매 가격","isUse":"판매중:1, 판매중지:0","isSoldOut":"품절:1, 재고있음:0","nPrdStockQty":"상품 재고 수량","strLinkUrl":"http://m.imvely.com/product/detail.html?product_no=15884&cate_no=1&display_group=32"}]
             */

            public int nTotalCount;
            public List<ProductItem> data;

            public static class ProductItem {
                /**
                 * idx : 상품 idx
                 * strType : 상품 타입
                 * strPrdCategory : 상품 카테고리명
                 * strPrdName : 상품명
                 * strPrdMaker : 상품 제조자
                 * strPrdOrigin : 상품 원산지
                 * strPrdBrand : 상품 브랜드
                 * strPrdImg : 상품 이미지 URL
                 * nPrdCustPrice : 희망 소비자 가격
                 * nPrdSellPrice : 판매 가격
                 * isUse : 판매중:1, 판매중지:0
                 * isSoldOut : 품절:1, 재고있음:0
                 * nPrdStockQty : 상품 재고 수량
                 * strLinkUrl : http://m.imvely.com/product/detail.html?product_no=15884&cate_no=1&display_group=32
                 */

                public String idx;
                public String sc_code;
                public String sc_name;
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

    public static class ReservationItem {
        /**
         * mb_id : rinno83  // 편성표 등록 ID
         * fo_date : 2019030303  // 편성표 예약일자[format:yyyymmddH]
         * fo_year : 2019  // 편성표 예약년도[format:yyyy]
         * fo_month : 03  // 편성표 예약월[mm]
         * fo_day : 03  // 편성표 예약일[dd]
         * fo_hour : 03  // 편성표 예약시간[H]
         * title : 편성표 테스트5  // 편성표 제목
         * stream_key : 190110151951HpGC  // 편성표 StreamKey
         * category : 10  // 편성표 1차 카테고리
         * ca_id : 1020  // 편성표 2차 카테고리
         * ca_id2 : 1010  // 편성표 2차 카테고리
         * ca_id3 : null, // 편성표 2차 카테고리
         * live_img_width : 290  // 편성표 이미지 가로 사이즈
         * live_img_height : 218  // 편성표 이미지 세로 사이즈
         * live_img : http://api.lieon.co.kr/images/live/showthat/ff8a543ffc546780a8ce9683f1dddcab.jpg  // 편성표 이미지 URL
         * tag : formation,test  // 편성표 태그[,로 구분]
         * reservation_cnt : 0  // 편성표 시청예약 수
         * notice : test  // 편성표 공지사항
         * fo_status : 1  // 편성표 상태[0:승인대기중,1:승인완료,2:승인거절]
         * status_date : 2019-03-07 15:48:08  // 편성표 상태 변경 일자[format:yyyy-mm-dd H:i:s]
         * cancel_reason : null, // 편성표 승인 거절 사유
         * mb_nick : rinno83  // 편성표 등록 회원 ID
         * mb_user_img : // 편성표 등록 회원 프로필 이미지 URL
         * live_type : empty  // 편성표 방송 상태[empty:방송전, live:방송중, lastlive:지난방송]
         * is_mine : N  // 내 편성표 여부
         * live_possible : N  // 편성표 방송 가능 여부
         * my_alarm : Y  // 내 시청 예약 여부
         * items : {"nTotalCount":0,"data":[{"idx":"상품 idx","strType":"상품 타입","strPrdCategory":"상품 카테고리명","strPrdName":"상품명","strPrdMaker":"상품 제조자","strPrdOrigin":"상품 원산지","strPrdBrand":"상품 브랜드","strPrdImg":"상품 이미지 URL","nPrdCustPrice":"희망 소비자 가격","nPrdSellPrice":"판매 가격","isUse":"판매중:1, 판매중지:0","isSoldOut":"품절:1, 재고있음:0","nPrdStockQty":"상품 재고 수량","strLinkUrl":"http://m.imvely.com/product/detail.html?product_no=15884&cate_no=1&display_group=32"}]}
         */

        public String mb_id;
        public String fo_date;
        public String fo_year;
        public String fo_month;
        public String fo_day;
        public String fo_hour;
        public String title;
        public String stream_key;
        public String category;
        public String ca_id;
        public String ca_id2;
        public String ca_id3;
        public String live_img_width;
        public String live_img_height;
        public String live_img;
        public String tag;
        public String reservation_cnt;
        public String notice;
        public String fo_status;
        public String status_date;
        public String cancel_reason;
        public String mb_nick;
        public String mb_user_img;
        public String live_type;
        public String is_mine;
        public String live_possible;
        public String my_alarm;
        public Products items;

        public static class Products {
            /**
             * nTotalCount : 0
             * data : [{"idx":"상품 idx","strType":"상품 타입","strPrdCategory":"상품 카테고리명","strPrdName":"상품명","strPrdMaker":"상품 제조자","strPrdOrigin":"상품 원산지","strPrdBrand":"상품 브랜드","strPrdImg":"상품 이미지 URL","nPrdCustPrice":"희망 소비자 가격","nPrdSellPrice":"판매 가격","isUse":"판매중:1, 판매중지:0","isSoldOut":"품절:1, 재고있음:0","nPrdStockQty":"상품 재고 수량","strLinkUrl":"http://m.imvely.com/product/detail.html?product_no=15884&cate_no=1&display_group=32"}]
             */

            public int nTotalCount;
            public List<ProductItem> data;

            public static class ProductItem {
                /**
                 * idx : 상품 idx
                 * strType : 상품 타입
                 * strPrdCategory : 상품 카테고리명
                 * strPrdName : 상품명
                 * strPrdMaker : 상품 제조자
                 * strPrdOrigin : 상품 원산지
                 * strPrdBrand : 상품 브랜드
                 * strPrdImg : 상품 이미지 URL
                 * nPrdCustPrice : 희망 소비자 가격
                 * nPrdSellPrice : 판매 가격
                 * isUse : 판매중:1, 판매중지:0
                 * isSoldOut : 품절:1, 재고있음:0
                 * nPrdStockQty : 상품 재고 수량
                 * strLinkUrl : http://m.imvely.com/product/detail.html?product_no=15884&cate_no=1&display_group=32
                 */

                public String idx;
                public String sc_code;
                public String sc_name;
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
}
