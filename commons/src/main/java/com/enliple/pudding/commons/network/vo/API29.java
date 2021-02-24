package com.enliple.pudding.commons.network.vo;

import java.util.List;

/**
 * Created by Kim Joonsung on 2018-11-26.
 */
public class API29 {

    /**
     * t_idx : 9 (태그 ID)
     * tag : 전망좋은 집
     * vod_cnt : 3
     * vod : [{"id":"idx","videoType":"동영상의 타입 LIVE or LASTLIVE or VOD","isOnAir":"생방송 여부 Y or N (삭제 예정)","liveVod":"지난 라이브 여부 Y or N (삭제 예정)","userId":"영상 제공자 아이디","stream":"LIVE or VOD 재생 URL","streamHls":"hls 재생 URL","strChatKey":"채팅키","isFavorite":"좋아요 여부 Y or N","viewerCount":"LIVE일 경우 시청자수, VOD일 경우 조회수","categoryCode":"1차 카테고리 코드값","title":"방송 제목","favoriteCount":"좋아요 수","thumbnailUrl":"썸네일 URL","relationPrd":{"nTotalCount":4,"data":[{"idx":"상품 idx","sc_code":"샵트리 상점 KEY","pcode":"샵트리 상품 key","strType":"상품 정보 타입 (1=상품, 2=링크, 3=샘플)","strPrdCategory":"상품 카테고리명","strPrdName":"상품명","strPrdMaker":"상품 제조자","strPrdOrigin":"상품 원산지","strPrdBrand":"상품 브랜드","strPrdImg":"상품 썸네일 URL","nPrdCustPrice":"상품 희망 소비자 가격","nPrdSellPrice":"상품 판매 가격","isUse":"판매여부, 판매중:1, 판매중지:0","isSoldOut":"품절여부, 품절:1, 재고있음:0","nPrdStockQty":"재고수량"}]}}]
     */

    public String result;
    public int nTotalCount;
    public String pageCount;
    public String nVodTotalCount;
    public String nTagTotalCount;
    public String nUserTotalCount;
    public String nProductTotalCount;
    public String nFormationTotalCount;
    public List<VodItem> data;

    public static class VodItem {
        /**
         * id : idx
         * videoType : 동영상의 타입 LIVE or LASTLIVE or VOD
         * isOnAir : 생방송 여부 Y or N (삭제 예정)
         * liveVod : 지난 라이브 여부 Y or N (삭제 예정)
         * userId : 영상 제공자 아이디
         * stream : LIVE or VOD 재생 URL
         * streamHls : hls 재생 URL
         * strChatKey : 채팅키
         * isFavorite : 좋아요 여부 Y or N
         * viewerCount : LIVE일 경우 시청자수, VOD일 경우 조회수
         * categoryCode : 1차 카테고리 코드값
         * title : 방송 제목
         * favoriteCount : 좋아요 수
         * thumbnailUrl : 썸네일 URL
         * relationPrd : {"nTotalCount":4,"data":[{"idx":"상품 idx","sc_code":"샵트리 상점 KEY","pcode":"샵트리 상품 key","strType":"상품 정보 타입 (1=상품, 2=링크, 3=샘플)","strPrdCategory":"상품 카테고리명","strPrdName":"상품명","strPrdMaker":"상품 제조자","strPrdOrigin":"상품 원산지","strPrdBrand":"상품 브랜드","strPrdImg":"상품 썸네일 URL","nPrdCustPrice":"상품 희망 소비자 가격","nPrdSellPrice":"상품 판매 가격","isUse":"판매여부, 판매중:1, 판매중지:0","isSoldOut":"품절여부, 품절:1, 재고있음:0","nPrdStockQty":"재고수량"}]}
         */
        public String strChatlog;
        public String id;
        public String videoType;
        public String isOnAir;
        public String liveVod;
        public String userId;
        public String stream;
        public String streamHls;
        public String strChatKey;
        public String isFavorite;
        public String viewerCount;
        public String shareCount;
        public String categoryCode;
        public String title;
        public String favoriteCount;
        public String thumbnailUrl;
        public String largeThumbnailUrl;
        public String user_show_YN;
        public String share_YN;         // 공유 여부 Y or N
        public String comment_YN;       // 댓글 허용 여부 Y or N
        public String scrapCount;
        public String isScrap;
        public String strTag;            // 해시태그 정보
        public String userNick;
        public String userImage;
        public String strContentSize;  // 영상사이즈 [v:세로, h:가로]
        public String min_price;
        public String min_price_product;
        public String comment_cnt;
        public String order_cnt;
        public String order_price;
        public RelationPrdBean relationPrd;

        public static class RelationPrdBean {
            /**
             * nTotalCount : 4
             * data : [{"idx":"상품 idx","sc_code":"샵트리 상점 KEY","pcode":"샵트리 상품 key","strType":"상품 정보 타입 (1=상품, 2=링크, 3=샘플)","strPrdCategory":"상품 카테고리명","strPrdName":"상품명","strPrdMaker":"상품 제조자","strPrdOrigin":"상품 원산지","strPrdBrand":"상품 브랜드","strPrdImg":"상품 썸네일 URL","nPrdCustPrice":"상품 희망 소비자 가격","nPrdSellPrice":"상품 판매 가격","isUse":"판매여부, 판매중:1, 판매중지:0","isSoldOut":"품절여부, 품절:1, 재고있음:0","nPrdStockQty":"재고수량"}]
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
                public String wish_cnt;     // 찜 갯수
                public String is_wish;      // 찜 여부
                public String is_cart;      // 장바구니 여부
            }
        }
    }
}
