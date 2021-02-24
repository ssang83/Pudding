package com.enliple.pudding.commons.network.vo;

import java.util.List;

/**
 * Created by Kim Joonsung on 2018-11-29.
 */
public class API37 {

    /**
     * result : success
     * nTotalCount : 16
     * data : [{"id":"181113112817WlLp","videoType":"LIVE","isOnAir":"Y","liveVod":"N","userId":"public","stream":"rtmp://enliple.xst.kinxcdn.com/enliple/:v/181113112817WlLp","streamHls":"http://enliple.xst.kinxcdn.com/enliple/_definst_/v/181113112817WlLp/playlist.m3u8","strChatKey":"181113112817WlLp","isFavorite":"N","videoRato":"720/1280","viewerCount":"1","categoryCode":"4040","title":"지난 라이브 나오나요?","favoriteCount":0,"strNoti":"하하하1","strTag":"전망좋은집,아파트","thumbnailUrl":"http://api.lieon.co.kr/images/showthat/thumbnail26.jpg","show_YN":"Y","user_show_YN":"Y","relationPrd":{"nTotalCount":0,"data":[{"idx":"","strType":"","strPrdCategory":"","strPrdName":"","strPrdMaker":"","strPrdOrigin":"","strPrdBrand":"","strPrdImg":"","nPrdCustPrice":"","nPrdSellPrice":"","isUse":"","isSoldOut":"","nPrdStockQty":""}]}}]
     * pageCount : 1
     */

    public String result;
    public int nTotalCount;
    public int pageCount;
    public List<VodItem> data;

    public static class VodItem {
        /**
         * id : 181113112817WlLp
         * videoType : LIVE
         * isOnAir : Y
         * liveVod : N
         * userId : public
         * stream : rtmp://enliple.xst.kinxcdn.com/enliple/:v/181113112817WlLp
         * streamHls : http://enliple.xst.kinxcdn.com/enliple/_definst_/v/181113112817WlLp/playlist.m3u8
         * strChatKey : 181113112817WlLp
         * isFavorite : N
         * videoRato : 720/1280
         * viewerCount : 1
         * categoryCode : 4040
         * title : 지난 라이브 나오나요?
         * favoriteCount : 0
         * strNoti : 하하하1
         * strTag : 전망좋은집,아파트
         * thumbnailUrl : http://api.lieon.co.kr/images/showthat/thumbnail26.jpg
         * show_YN : Y
         * user_show_YN : Y
         * relationPrd : {"nTotalCount":0,"data":[{"idx":"","strType":"","strPrdCategory":"","strPrdName":"","strPrdMaker":"","strPrdOrigin":"","strPrdBrand":"","strPrdImg":"","nPrdCustPrice":"","nPrdSellPrice":"","isUse":"","isSoldOut":"","nPrdStockQty":""}]}
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
        public String categoryCode;
        public String title;
        public String favoriteCount;
        public String strNoti;
        public String strTag;
        public String thumbnailUrl;
        public String largeThumbnailUrl;
        public String show_YN;       // 서비스 정책 위반 여부
        public String user_show_YN;
        public String delete_YN;        // 영상 삭제 여부
        public String share_YN;     // 공유 여부 Y or N
        public String comment_YN;   // 댓글 허용 여부 Y or N
        public String strContentSize;  // 영상사이즈 [v:세로, h:가로]
        public String scrapCount;
        public String isScrap;
        public String userNick;
        public String userImage;
        public String min_price;
        public String min_price_product;
        public String comment_cnt;
        public String order_cnt;
        public String order_price;
        public boolean isSelect = false;
        public RelationPrdBean relationPrd;

        public static class RelationPrdBean {
            /**
             * nTotalCount : 0
             * data : [{"idx":"","strType":"","strPrdCategory":"","strPrdName":"","strPrdMaker":"","strPrdOrigin":"","strPrdBrand":"","strPrdImg":"","nPrdCustPrice":"","nPrdSellPrice":"","isUse":"","isSoldOut":"","nPrdStockQty":""}]
             */

            public int nTotalCount;
            public List<ProductItem> data;

            public static class ProductItem {
                /**
                 * idx :
                 * strType :
                 * strPrdCategory :
                 * strPrdName :
                 * strPrdMaker :
                 * strPrdOrigin :
                 * strPrdBrand :
                 * strPrdImg :
                 * nPrdCustPrice :
                 * nPrdSellPrice :
                 * isUse :
                 * isSoldOut :
                 * nPrdStockQty :
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
}
