package com.enliple.pudding.commons.network.vo;

import java.util.List;

public class API0 {

    /**
     * nTotalCount : 10
     * pageCount : 1
     * data : [{"id":"idx","videoType":"동영상의 타입 LIVE or LASTLIVE or VOD","isOnAir":"생방송 여부 Y or N (삭제 예정)","liveVod":"지난 라이브 여부 Y or N (삭제 예정)","userId":"영상 제공자 아이디","userNick":"영상 제공자 닉네임","userImage":"영상 제공자 사진","stream":"LIVE or VOD 재생 URL","streamHls":"hls 재생 URL","strChatKey":"채팅키","strContentSize":"영상사이즈 [v:세로, h:가로]","videoRato":"720/1280","isFavorite":"좋아요 여부 Y or N","isScrap":"즐겨찾기 여부 Y or N","scrapCount":"즐겨찾기 수","viewerCount":"LIVE일 경우 시청자수, VOD일 경우 조회수","categoryCode":"1차 카테고리 코드값","title":"방송 제목","favoriteCount":"좋아요 수","strNoti":"공지글","strTag":"해시태그 정보","thumbnailUrl":"썸네일 URL","show_YN":"N  // 서비스 정책 위반  여부","user_show_YN":"N  //  사용자 공개 여부","share_YN":"Y // 공유 여부","comment_YN":"Y // 댓글 허용 여부","relationPrd":{"nTotalCount":4,"data":[{"idx":"상품 idx","sc_code":"샵트리 상점 KEY","pcode":"샵트리 상품 key","strType":"상품 정보 타입 (1=상품, 2=링크, 3=샘플)","strPrdCategory":"상품 카테고리명","strPrdName":"상품명","strPrdMaker":"상품 제조자","strPrdOrigin":"상품 원산지","strPrdBrand":"상품 브랜드","strPrdImg":"상품 썸네일 URL","nPrdCustPrice":"상품 희망 소비자 가격","nPrdSellPrice":"상품 판매 가격","isUse":"판매여부, 판매중:1, 판매중지:0","isSoldOut":"품절여부, 품절:1, 재고있음:0","nPrdStockQty":"재고수량","strLinkUrl":"http://m.imvely.com/product/detail.html?product_no=15884&cate_no=1&display_group=32"}]}}]
     */

    public int nTotalCount;
    public int pageCount;
    public List<DataBeanX> data;
    public VODSummary summary;

    public static class DataBeanX {
        /**
         * id : idx
         * videoType : 동영상의 타입 LIVE or LASTLIVE or VOD
         * isOnAir : 생방송 여부 Y or N (삭제 예정)
         * liveVod : 지난 라이브 여부 Y or N (삭제 예정)
         * userId : 영상 제공자 아이디
         * userNick : 영상 제공자 닉네임
         * userImage : 영상 제공자 사진
         * stream : LIVE or VOD 재생 URL
         * streamHls : hls 재생 URL
         * strChatKey : 채팅키
         * strContentSize : 영상사이즈 [v:세로, h:가로]
         * videoRato : 720/1280
         * isFavorite : 좋아요 여부 Y or N
         * isScrap : 즐겨찾기 여부 Y or N
         * scrapCount : 즐겨찾기 수
         * viewerCount : LIVE일 경우 시청자수, VOD일 경우 조회수
         * categoryCode : 1차 카테고리 코드값
         * title : 방송 제목
         * favoriteCount : 좋아요 수
         * strNoti : 공지글
         * strTag : 해시태그 정보
         * thumbnailUrl : 썸네일 URL
         * show_YN : N  // 서비스 정책 위반  여부
         * user_show_YN : N  //  사용자 공개 여부
         * share_YN : Y // 공유 여부
         * comment_YN : Y // 댓글 허용 여부
         * relationPrd : {"nTotalCount":4,"data":[{"idx":"상품 idx","sc_code":"샵트리 상점 KEY","pcode":"샵트리 상품 key","strType":"상품 정보 타입 (1=상품, 2=링크, 3=샘플)","strPrdCategory":"상품 카테고리명","strPrdName":"상품명","strPrdMaker":"상품 제조자","strPrdOrigin":"상품 원산지","strPrdBrand":"상품 브랜드","strPrdImg":"상품 썸네일 URL","nPrdCustPrice":"상품 희망 소비자 가격","nPrdSellPrice":"상품 판매 가격","isUse":"판매여부, 판매중:1, 판매중지:0","isSoldOut":"품절여부, 품절:1, 재고있음:0","nPrdStockQty":"재고수량","strLinkUrl":"http://m.imvely.com/product/detail.html?product_no=15884&cate_no=1&display_group=32"}]}
         */
        public String strChatlog;
        public String id;
        public String videoType;
        public String isOnAir;
        public String liveVod;
        public String userId;
        public String userNick;
        public String userImage;
        public String stream;
        public String streamHls;
        public String strChatKey;
        public String strContentSize;
        public String videoRato;
        public String isFavorite;
        public String isScrap;
        public String scrapCount;
        public String viewerCount;
        public String categoryCode;
        public String title;
        public String favoriteCount;
        public String strNoti;
        public String strTag;
        public String thumbnailUrl;
        public String largeThumbnailUrl;
        public String show_YN;
        public String user_show_YN;
        public String share_YN;
        public String comment_YN;
        public String shareCount;
        public boolean isSelect;
        public String order_cnt;
        public String order_price;
        public String reg_date;
        public String shop_total_cnt;
        public String shop_total_price;
        public String shop_total_fee;
        public String shop_max_video_ratio;
        public String link_total_click;
        public String link_total_fee;
        public RelationPrdBean relationPrd;

        public static class RelationPrdBean {
            /**
             * nTotalCount : 4
             * data : [{"idx":"상품 idx","sc_code":"샵트리 상점 KEY","pcode":"샵트리 상품 key","strType":"상품 정보 타입 (1=상품, 2=링크, 3=샘플)","strPrdCategory":"상품 카테고리명","strPrdName":"상품명","strPrdMaker":"상품 제조자","strPrdOrigin":"상품 원산지","strPrdBrand":"상품 브랜드","strPrdImg":"상품 썸네일 URL","nPrdCustPrice":"상품 희망 소비자 가격","nPrdSellPrice":"상품 판매 가격","isUse":"판매여부, 판매중:1, 판매중지:0","isSoldOut":"품절여부, 품절:1, 재고있음:0","nPrdStockQty":"재고수량","strLinkUrl":"http://m.imvely.com/product/detail.html?product_no=15884&cate_no=1&display_group=32"}]
             */

            public int nTotalCount;
            public List<DataBean> data;

            public static class DataBean {
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
                public String wish_cnt;     // 찜 갯수
                public String is_wish;      // 찜 여부
                public String is_cart;      // 장바구니 여부
            }
        }
    }

    public static class VODSummary {
        public String total_order;
        public String total_hit;
        public String total_share;
        public String total_favor;
        public String total_scrap;
    }
}