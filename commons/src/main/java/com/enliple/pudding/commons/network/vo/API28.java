package com.enliple.pudding.commons.network.vo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kim Joonsung on 2018-11-23.
 */
public class API28 {

    /**
     * result : success
     * nTotalCount : 1
     * data : [{"id":"1811232008138pDw","videoType":"LASTLIVE","isOnAir":"N","liveVod":"Y","userId":"android5","stream":"http://cache.midibus.kinxcdn.com/hls/ch_1648a4c9/167404d07705a4d5/playlist.m3u8","streamHls":"http://cache.midibus.kinxcdn.com/hls/ch_1648a4c9/167404d07705a4d5/playlist.m3u8","strChatKey":"1811232008138pDw","isFavorite":"N","videoRato":"720/1280","viewerCount":"1","categoryCode":"6060","title":"ㅕㅕ야댝","favoriteCount":0,"thumbnailUrl":"https://thumb.midibus.kinxcdn.com/262/167404fa545c049c.png","relationPrd":{"data":[{"idx":"33","sc_code":"z04vp8vERx","pcode":"4wxZnK6eOm","strType":"1","strPrdCategory":"여성의류","strPrdName":"설렘 레이스 원피스","strPrdMaker":"","strPrdOrigin":"","strPrdBrand":"","strPrdImg":"https://images-stage.shaptree.com/images/stores/z04vp8vERx/products/1605139134505041_400.jpg","nPrdCustPrice":"40000","nPrdSellPrice":"36800","isUse":"1","isSoldOut":"0","nPrdStockQty":"9"}],"nTotalCount":1}}]
     * pageCount : 1
     */

    public String result;
    public int nTotalCount;
    public int pageCount;
    public List<VodItem> data = new ArrayList<VodItem>();

    public static class VodItem {
        /**
         * id : 1811232008138pDw
         * videoType : LASTLIVE
         * isOnAir : N
         * liveVod : Y
         * userId : android5
         * stream : http://cache.midibus.kinxcdn.com/hls/ch_1648a4c9/167404d07705a4d5/playlist.m3u8
         * streamHls : http://cache.midibus.kinxcdn.com/hls/ch_1648a4c9/167404d07705a4d5/playlist.m3u8
         * strChatKey : 1811232008138pDw
         * isFavorite : N
         * videoRato : 720/1280
         * viewerCount : 1
         * categoryCode : 6060
         * title : ㅕㅕ야댝
         * favoriteCount : 0
         * thumbnailUrl : https://thumb.midibus.kinxcdn.com/262/167404fa545c049c.png
         * relationPrd : {"data":[{"idx":"33","sc_code":"z04vp8vERx","pcode":"4wxZnK6eOm","strType":"1","strPrdCategory":"여성의류","strPrdName":"설렘 레이스 원피스","strPrdMaker":"","strPrdOrigin":"","strPrdBrand":"","strPrdImg":"https://images-stage.shaptree.com/images/stores/z04vp8vERx/products/1605139134505041_400.jpg","nPrdCustPrice":"40000","nPrdSellPrice":"36800","isUse":"1","isSoldOut":"0","nPrdStockQty":"9"}],"nTotalCount":1}
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
        public String videoRato;
        public String viewerCount;
        public String shareCount;
        public String categoryCode;
        public String title;
        public String favoriteCount;
        public String thumbnailUrl;
        public String user_show_YN;
        public String share_YN;     // 공유 여부 Y or N
        public String comment_YN;   // 댓글 허용 여부 Y or N
        public String scrapCount;
        public String isScrap;
        public String strTag;            // 해시태그 정보
        public String userNick;
        public String userImage;
        public String strContentSize;  // 영상사이즈 [v:세로, h:가로]
        public String largeThumbnailUrl;
        public String min_price;
        public String min_price_product;
        public String comment_cnt;
        public String order_cnt;
        public String order_price;
        public RelationPrdBean relationPrd;

        public static class RelationPrdBean {
            /**
             * data : [{"idx":"33","sc_code":"z04vp8vERx","pcode":"4wxZnK6eOm","strType":"1","strPrdCategory":"여성의류","strPrdName":"설렘 레이스 원피스","strPrdMaker":"","strPrdOrigin":"","strPrdBrand":"","strPrdImg":"https://images-stage.shaptree.com/images/stores/z04vp8vERx/products/1605139134505041_400.jpg","nPrdCustPrice":"40000","nPrdSellPrice":"36800","isUse":"1","isSoldOut":"0","nPrdStockQty":"9"}]
             * nTotalCount : 1
             */

            public int nTotalCount;
            public List<ProductITem> data;

            public static class ProductITem {
                /**
                 * idx : 33
                 * sc_code : z04vp8vERx
                 * pcode : 4wxZnK6eOm
                 * strType : 1
                 * strPrdCategory : 여성의류
                 * strPrdName : 설렘 레이스 원피스
                 * strPrdMaker :
                 * strPrdOrigin :
                 * strPrdBrand :
                 * strPrdImg : https://images-stage.shaptree.com/images/stores/z04vp8vERx/products/1605139134505041_400.jpg
                 * nPrdCustPrice : 40000
                 * nPrdSellPrice : 36800
                 * isUse : 1
                 * isSoldOut : 0
                 * nPrdStockQty : 9
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
