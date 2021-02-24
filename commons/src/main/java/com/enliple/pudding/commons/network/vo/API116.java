package com.enliple.pudding.commons.network.vo;

import java.util.List;

/**
 * Created by Kim Joonsung on 2019-01-31.
 */
public class API116 {

    /**
     * result : success
     * nTotalCount : 2
     * data : {"evId":"2 // 이벤트 INDEX","evTitle":"동영상 이벤트 // 이벤트 제목","mainImg":"http://lieon.co.kr/data/event/thumb/1548393419_main.jpg // 이벤트 메인 이미지","sub_img1":"http://lieon.co.kr/data/event/thumb/1548393419_sub1.jpg // 이벤트 상세 이미지1","sub_img2":"// 이벤트 상세 이미지2","sub_img3":"// 이벤트 상세 이미지3","nPickTotalCount":3,"evPick":[{"tagName":{},"nTagDataCount":{},"tagData":[{"id":"idx","videoType":"동영상의 타입 LIVE or MULTILIVE or LASTLIVE or VOD","isOnAir":"생방송 여부 Y or N (삭제 예정)","liveVod":"지난 라이브 여부 Y or N (삭제 예정)","userId":"영상 제공자 아이디","userNick":"영상 제공자 닉네임","userImage":"영상 제공자 사진","stream":"LIVE or VOD 재생 URL","streamHls":"hls 재생 URL","strChatKey":"채팅키","strContentSize":"영상사이즈 [v:세로, h:가로]","videoRato":"720/1280","isScrap":"스크랩 여부 Y or N","isFavorite":"좋아요 여부 Y or N","viewerCount":"LIVE일 경우 시청자수, VOD일 경우 조회수","categoryCode":"1차 카테고리 코드값","title":"방송 제목","notice":"방송 공지","favoriteCount":"좋아요 수","scrapCount":"즐겨찾기 수","thumbnailUrl":"썸네일 URL","share_YN":"Y // 공유 허용","comment_YN":"Y // 댓글 허용","relationPrd":{"nTotalCount":4,"data":[{"idx":"상품 idx","sc_code":"샵트리 상점 KEY","pcode":"샵트리 상품 key","strType":"상품 정보 타입 (1=상품, 2=링크, 3=샘플)","strPrdCategory":"상품 카테고리명","strPrdName":"상품명","strPrdMaker":"상품 제조자","strPrdOrigin":"상품 원산지","strPrdBrand":"상품 브랜드","strPrdImg":"상품 썸네일 URL","nPrdCustPrice":"상품 희망 소비자 가격","nPrdSellPrice":"상품 판매 가격","isUse":"판매여부, 판매중:1, 판매중지:0","isSoldOut":"품절여부, 품절:1, 재고있음:0","nPrdStockQty":"재고수량","strLinkUrl":"http://m.imvely.com/product/detail.html?product_no=15884&cate_no=1&display_group=32"}]}}]}]}
     */

    public String result;
    public int nTotalCount;
    public DataBeanX data;

    public static class DataBeanX {
        /**
         * evId : 2 // 이벤트 INDEX
         * evTitle : 동영상 이벤트 // 이벤트 제목
         * mainImg : http://lieon.co.kr/data/event/thumb/1548393419_main.jpg // 이벤트 메인 이미지
         * sub_img1 : http://lieon.co.kr/data/event/thumb/1548393419_sub1.jpg // 이벤트 상세 이미지1
         * sub_img2 : // 이벤트 상세 이미지2
         * sub_img3 : // 이벤트 상세 이미지3
         * nPickTotalCount : 3
         * evPick : [{"tagName":{},"nTagDataCount":{},"tagData":[{"id":"idx","videoType":"동영상의 타입 LIVE or MULTILIVE or LASTLIVE or VOD","isOnAir":"생방송 여부 Y or N (삭제 예정)","liveVod":"지난 라이브 여부 Y or N (삭제 예정)","userId":"영상 제공자 아이디","userNick":"영상 제공자 닉네임","userImage":"영상 제공자 사진","stream":"LIVE or VOD 재생 URL","streamHls":"hls 재생 URL","strChatKey":"채팅키","strContentSize":"영상사이즈 [v:세로, h:가로]","videoRato":"720/1280","isScrap":"스크랩 여부 Y or N","isFavorite":"좋아요 여부 Y or N","viewerCount":"LIVE일 경우 시청자수, VOD일 경우 조회수","categoryCode":"1차 카테고리 코드값","title":"방송 제목","notice":"방송 공지","favoriteCount":"좋아요 수","scrapCount":"즐겨찾기 수","thumbnailUrl":"썸네일 URL","share_YN":"Y // 공유 허용","comment_YN":"Y // 댓글 허용","relationPrd":{"nTotalCount":4,"data":[{"idx":"상품 idx","sc_code":"샵트리 상점 KEY","pcode":"샵트리 상품 key","strType":"상품 정보 타입 (1=상품, 2=링크, 3=샘플)","strPrdCategory":"상품 카테고리명","strPrdName":"상품명","strPrdMaker":"상품 제조자","strPrdOrigin":"상품 원산지","strPrdBrand":"상품 브랜드","strPrdImg":"상품 썸네일 URL","nPrdCustPrice":"상품 희망 소비자 가격","nPrdSellPrice":"상품 판매 가격","isUse":"판매여부, 판매중:1, 판매중지:0","isSoldOut":"품절여부, 품절:1, 재고있음:0","nPrdStockQty":"재고수량","strLinkUrl":"http://m.imvely.com/product/detail.html?product_no=15884&cate_no=1&display_group=32"}]}}]}]
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
             * tagName : {}
             * nTagDataCount : {}
             * tagData : [{"id":"idx","videoType":"동영상의 타입 LIVE or MULTILIVE or LASTLIVE or VOD","isOnAir":"생방송 여부 Y or N (삭제 예정)","liveVod":"지난 라이브 여부 Y or N (삭제 예정)","userId":"영상 제공자 아이디","userNick":"영상 제공자 닉네임","userImage":"영상 제공자 사진","stream":"LIVE or VOD 재생 URL","streamHls":"hls 재생 URL","strChatKey":"채팅키","strContentSize":"영상사이즈 [v:세로, h:가로]","videoRato":"720/1280","isScrap":"스크랩 여부 Y or N","isFavorite":"좋아요 여부 Y or N","viewerCount":"LIVE일 경우 시청자수, VOD일 경우 조회수","categoryCode":"1차 카테고리 코드값","title":"방송 제목","notice":"방송 공지","favoriteCount":"좋아요 수","scrapCount":"즐겨찾기 수","thumbnailUrl":"썸네일 URL","share_YN":"Y // 공유 허용","comment_YN":"Y // 댓글 허용","relationPrd":{"nTotalCount":4,"data":[{"idx":"상품 idx","sc_code":"샵트리 상점 KEY","pcode":"샵트리 상품 key","strType":"상품 정보 타입 (1=상품, 2=링크, 3=샘플)","strPrdCategory":"상품 카테고리명","strPrdName":"상품명","strPrdMaker":"상품 제조자","strPrdOrigin":"상품 원산지","strPrdBrand":"상품 브랜드","strPrdImg":"상품 썸네일 URL","nPrdCustPrice":"상품 희망 소비자 가격","nPrdSellPrice":"상품 판매 가격","isUse":"판매여부, 판매중:1, 판매중지:0","isSoldOut":"품절여부, 품절:1, 재고있음:0","nPrdStockQty":"재고수량","strLinkUrl":"http://m.imvely.com/product/detail.html?product_no=15884&cate_no=1&display_group=32"}]}}]
             */

            public String tagName;
            public int nTagDataCount;
            public List<TagDataItem> tagData;

            public static class TagDataItem {
                /**
                 * id : idx
                 * videoType : 동영상의 타입 LIVE or MULTILIVE or LASTLIVE or VOD
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
                 * isScrap : 스크랩 여부 Y or N
                 * isFavorite : 좋아요 여부 Y or N
                 * viewerCount : LIVE일 경우 시청자수, VOD일 경우 조회수
                 * categoryCode : 1차 카테고리 코드값
                 * title : 방송 제목
                 * notice : 방송 공지
                 * favoriteCount : 좋아요 수
                 * scrapCount : 즐겨찾기 수
                 * thumbnailUrl : 썸네일 URL
                 * share_YN : Y // 공유 허용
                 * comment_YN : Y // 댓글 허용
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
                public String isScrap;
                public String isFavorite;
                public String viewerCount;
                public String categoryCode;
                public String title;
                public String notice;
                public String favoriteCount;
                public String scrapCount;
                public String thumbnailUrl;
                public String largeThumbnailUrl;
                public String share_YN;
                public String comment_YN;
                public String user_show_YN;
                public String strNoti;
                public String strTag;
                public String min_price;
                public String min_price_product;
                public RelationPrdItem relationPrd;

                public static class RelationPrdItem {
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
                        public String wish_cnt;
                        public String is_wish;
                        public String is_cart;
                    }
                }
            }
        }
    }
}
