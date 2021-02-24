package com.enliple.pudding.commons.network.vo;

import java.util.List;

/**
 * Created by Kim Joonsung on 2019-01-30.
 */
public class API114 {
    public String result;
    public int nTotalCount;
    public int nHotTotalCount;
    public int nBannerTotalCount;
    public int nBestTotalCount;
    public int nEventTotalCount;
    public int nDataCount;
    public MdPickItem mdPick;
    public String pageCount;
    public List<VideoItem> data;
    public List<HotDataItem> hotData;
    public List<BannerItem> banner;
    public List<EventItem> event;
    public List<BestDataItem> bestData;

    public static class MdPickItem {
        public String evId;
        public String evTitle;
        public String mainImg;
        public int nPickDataTotalCount;
        public List<EvPickBean> evPick;

        public static class EvPickBean {
            public String strChatlog;
            public String id;
            public String videoType;
            public String isOnAir;
            public String liveVod;
            public String userId;
            public String stream;
            public String streamHls;
            public String userNick;
            public String userImage;
            public Object strChatKey;
            public String strContentSize;
            public String isFavorite;
            public String scrapCount;
            public String isScrap;
            public String videoRato;
            public String viewerCount;
            public String categoryCode;
            public String secondCategoryCode1;
            public String title;
            public int favoriteCount;
            public String strNoti;
            public String strTag;
            public String thumbnailUrl;
            public String largeThumbnailUrl;
            public String show_YN;
            public String user_show_YN;
            public String gender;
            public String age;
            public String reg_date;
            public String strRegDate;
            public String share_YN;
            public String comment_YN;
            public int shareCount;
            public String min_price;
            public RelationPrdBean relationPrd;
            public List<EventItem> event;

            public static class RelationPrdBean {
                public int nTotalCount;
                public List<DataBean> data;

                public static class DataBean {
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

    public static class VideoItem {
        public String strChatlog;
        public String id;
        public String videoType;
        public String isOnAir;
        public String liveVod;
        public String userId;
        public String stream;
        public String streamHls;
        public String userNick;
        public String userImage;
        public Object strChatKey;
        public String strContentSize;
        public String isFavorite;
        public String scrapCount;
        public String isScrap;
        public String videoRato;
        public String viewerCount;
        public String categoryCode;
        public String secondCategoryCode1;
        public String title;
        public String favoriteCount;
        public String strNoti;
        public String strTag;
        public String thumbnailUrl;
        public String largeThumbnailUrl;
        public String show_YN;
        public String user_show_YN;
        public String delete_YN;
        public String gender;
        public String age;
        public String reg_date;
        public String strRegDate;
        public String share_YN;
        public String comment_YN;
        public int shareCount;
        public String min_price;
        public String min_price_product;
        public RelationPrdBeanX relationPrd;
        public String update_date;
        public String secondCategoryCode2;
        public String secondCategoryCode3;
        public String comment_cnt;
        public String order_cnt;
        public String order_price;
        public String duration;

        public static class RelationPrdBeanX {
            /**
             * data : [{"idx":"3111","sc_code":"11번가","sc_name":"11번가","pcode":"","strType":"2","strPrdCategory":"","strPrdName":"스프레이 윈도우 클리너","strPrdMaker":"","strPrdOrigin":"","strPrdBrand":"","strPrdImg":"http://i.011st.com/t/300_0/pd/19/3/3/1/2/8/5/upcry/2332331285_B.png","nPrdCustPrice":"8910","nPrdSellPrice":"8910","isUse":"1","isSoldOut":"0","nPrdStockQty":"1","strLinkUrl":"http://www.11st.co.kr/product/SellerProductDetail.tmall?method=getSellerProductDetail&prdNo=2332331285&NaPm=ct=jt3rt8og|ci=6f8aad1af6745c7d021696750f76030e870915c4|tr=slsl|sn=17703|hk=c464d4feab9254834153417accd5a6ed3ec0434f&utm_term=&utm_campaign=%B3%D7%C0%CC%B9%F6pc_%B0%A1%B0%DD%BA%F1%B1%B3%B1%E2%BA%BB&utm_source=%B3%D7%C0%CC%B9%F6_PC_PCS&utm_medium=%B0%A1%B0%DD%BA%F1%B1%B3"}]
             * nTotalCount : 1
             */

            public int nTotalCount;
            public List<DataBeanX> data;

            public static class DataBeanX {
                /**
                 * idx : 3111
                 * sc_code : 11번가
                 * sc_name : 11번가
                 * pcode :
                 * strType : 2
                 * strPrdCategory :
                 * strPrdName : 스프레이 윈도우 클리너
                 * strPrdMaker :
                 * strPrdOrigin :
                 * strPrdBrand :
                 * strPrdImg : http://i.011st.com/t/300_0/pd/19/3/3/1/2/8/5/upcry/2332331285_B.png
                 * nPrdCustPrice : 8910
                 * nPrdSellPrice : 8910
                 * isUse : 1
                 * isSoldOut : 0
                 * nPrdStockQty : 1
                 * strLinkUrl : http://www.11st.co.kr/product/SellerProductDetail.tmall?method=getSellerProductDetail&prdNo=2332331285&NaPm=ct=jt3rt8og|ci=6f8aad1af6745c7d021696750f76030e870915c4|tr=slsl|sn=17703|hk=c464d4feab9254834153417accd5a6ed3ec0434f&utm_term=&utm_campaign=%B3%D7%C0%CC%B9%F6pc_%B0%A1%B0%DD%BA%F1%B1%B3%B1%E2%BA%BB&utm_source=%B3%D7%C0%CC%B9%F6_PC_PCS&utm_medium=%B0%A1%B0%DD%BA%F1%B1%B3
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

    public static class HotDataItem {
        /**
         * id : 190305195855n2Tp
         * videoType : LIVE
         * isOnAir : Y
         * liveVod : N
         * userId : nitid12
         * stream : rtmp://enliple.xst.kinxcdn.com/enliple/:v/190305195855n2Tp
         * streamHls : http://enliple.xst.kinxcdn.com/enliple/_definst_/v/190305195855n2Tp/playlist.m3u8
         * userNick : 까망콩백설탕
         * userImage : http://image.lieon.co.kr/user/nitid12/nitid12.gif
         * strChatKey : 9accd7e3788f3d87e6f1d3b0b682949a
         * strContentSize : v
         * isFavorite : N
         * scrapCount : 0
         * isScrap : N
         * videoRato : 720/1280
         * viewerCount : 150
         * categoryCode : 60
         * secondCategoryCode1 : 6060
         * title : 채팅 테스트 방송입니다. 영상은 나오지 않습니다.
         * favoriteCount : 5
         * strNoti : 공지
         * strTag : 고양이
         * thumbnailUrl : http://image.lieon.co.kr/live/nitid12/5d2882bac8474e420e6efe5ccd84ab9f.jpg
         * show_YN : Y
         * user_show_YN : Y
         * delete_YN : N
         * gender : M,F
         * age : 10,20,30
         * reg_date : 2019-03-05 19:56:40
         * strRegDate : 1달 전
         * update_date : 2038-01-19 12:14:07
         * share_YN : Y
         * comment_YN : Y
         * relationPrd : {"data":[{"idx":"6","sc_code":"17mqL938OE","sc_name":"유찬몰","pcode":"W5ezdVDKbk","strType":"1","strPrdCategory":"휴대폰","strPrdName":"[무료배송] 갤럭시S9, S9플러스 우레탄곡면 풀커버2매","strPrdMaker":"","strPrdOrigin":"","strPrdBrand":"","strPrdImg":"https://images.shop-tree.com/images/stores/17mqL938OE/products/1620896730081038_400.jpg","nPrdCustPrice":"15500","nPrdSellPrice":"15500","isUse":"1","isSoldOut":"0","nPrdStockQty":"395"}],"nTotalCount":1}
         */
        public String strChatlog;
        public String id;
        public String videoType;
        public String isOnAir;
        public String liveVod;
        public String userId;
        public String stream;
        public String streamHls;
        public String userNick;
        public String userImage;
        public String strChatKey;
        public String strContentSize;
        public String isFavorite;
        public String scrapCount;
        public String isScrap;
        public String videoRato;
        public String viewerCount;
        public String categoryCode;
        public String secondCategoryCode1;
        public String title;
        public String favoriteCount;
        public String strNoti;
        public String strTag;
        public String thumbnailUrl;
        public String largeThumbnailUrl;
        public String show_YN;
        public String user_show_YN;
        public String delete_YN;
        public String gender;
        public String age;
        public String reg_date;
        public String strRegDate;
        public String update_date;
        public String share_YN;
        public String comment_YN;
        public int shareCount;
        public String min_price;
        public String min_price_product;
        public String order_cnt;
        public String order_price;
        public String comment_cnt;
        public RelationPrdBeanXX relationPrd;

        public static class RelationPrdBeanXX {
            /**
             * data : [{"idx":"6","sc_code":"17mqL938OE","sc_name":"유찬몰","pcode":"W5ezdVDKbk","strType":"1","strPrdCategory":"휴대폰","strPrdName":"[무료배송] 갤럭시S9, S9플러스 우레탄곡면 풀커버2매","strPrdMaker":"","strPrdOrigin":"","strPrdBrand":"","strPrdImg":"https://images.shop-tree.com/images/stores/17mqL938OE/products/1620896730081038_400.jpg","nPrdCustPrice":"15500","nPrdSellPrice":"15500","isUse":"1","isSoldOut":"0","nPrdStockQty":"395"}]
             * nTotalCount : 1
             */

            public int nTotalCount;
            public List<DataBeanXXX> data;

            public static class DataBeanXXX {
                /**
                 * idx : 6
                 * sc_code : 17mqL938OE
                 * sc_name : 유찬몰
                 * pcode : W5ezdVDKbk
                 * strType : 1
                 * strPrdCategory : 휴대폰
                 * strPrdName : [무료배송] 갤럭시S9, S9플러스 우레탄곡면 풀커버2매
                 * strPrdMaker :
                 * strPrdOrigin :
                 * strPrdBrand :
                 * strPrdImg : https://images.shop-tree.com/images/stores/17mqL938OE/products/1620896730081038_400.jpg
                 * nPrdCustPrice : 15500
                 * nPrdSellPrice : 15500
                 * isUse : 1
                 * isSoldOut : 0
                 * nPrdStockQty : 395
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
                public String wish_cnt;     // 찜 갯수
                public String is_wish;      // 찜 여부
                public String is_cart;      // 장바구니 여부
            }
        }
    }

    public static class BannerItem {
        /**
         * bn_id : 1
         * bn_url : puddingPush://Event?ev_id=62&ev_type=vod
         * bn_end_time : 2019-04-16 00:00:00
         * bn_image : http://image.lieon.co.kr/banner/1?_=-62170014600
         */

        public String bn_id;
        public String bn_url;
        public String bn_end_time;
        public String bn_image;
    }

    public static class EventItem {
        /**
         * ev_id : 8
         * ev_title : 뷰티 이벤트1
         * ev_type : prd
         * ev_content1 : 내용
         * ev_content2 : 40%
         * ev_todate : 2019-04-30
         * main_img : http://image.local.com/event/1550196184_main.jpg
         * main_img_width : 360
         * main_img_height : 164
         * ev_hit : 2
         */

        public String ev_id;
        public String ev_title;
        public String ev_type;
        public String ev_content1;
        public String ev_content2;
        public String ev_todate;
        public String main_img;
        public int main_img_width;
        public int main_img_height;
        public String ev_hit;
        public String ev_it_id;
        public String ev_strType;
        public String ev_is_wish;
    }

    public static class BestDataItem {
        /**
         * id : 168db0558460a049
         * videoType : VOD
         * isOnAir : N
         * liveVod : N
         * userId : catnip
         * stream : http://cache.midibus.kinxcdn.com/hls/ch_1648a4c9/168db0558460a049/playlist.m3u8
         * streamHls : http://cache.midibus.kinxcdn.com/hls/ch_1648a4c9/168db0558460a049/playlist.m3u8
         * userNick : 유깻잎
         * userImage : http://image.lieon.co.kr/user/catnip/catnip.gif
         * strChatKey : null
         * strContentSize : h
         * isFavorite : N
         * scrapCount : 6
         * isScrap : N
         * videoRato : 1280/720
         * viewerCount : 1259
         * categoryCode : 20
         * secondCategoryCode1 : 2010
         * title : 로드샵 리빙코랄 제품 리뷰
         * favoriteCount : 6
         * strNoti : 로드샵 리빙코랄 제품 리뷰
         * strTag : 리빙코랄메이크업,화장품리뷰,화장품
         * thumbnailUrl : http://image.lieon.co.kr/vod/catnip/8c0839ff42ba54b0dfd43c2a8e49313a.JPG
         * show_YN : Y
         * user_show_YN : Y
         * delete_YN : N
         * gender : M,F
         * age : 10,20,30
         * reg_date : 2019-02-11 14:19:40
         * strRegDate : 1달 전
         * update_date : 2019-04-08 14:49:13
         * share_YN : Y
         * comment_YN : Y
         * relationPrd : {"data":[{"idx":"1494","sc_code":"11번가","sc_name":"11번가","pcode":"","strType":"2","strPrdCategory":"","strPrdName":"에뛰드하우스 룩앳마이 아이즈 카페_23색상 아이섀도 - 11번가","strPrdMaker":"","strPrdOrigin":"","strPrdBrand":"","strPrdImg":"http://i.011st.com/t/300_0/pd/18/5/1/6/6/8/2/cDeHj/1818516682_B.jpg","nPrdCustPrice":"2800","nPrdSellPrice":"2800","isUse":"1","isSoldOut":"0","nPrdStockQty":"1","strLinkUrl":"http://www.11st.co.kr/product/SellerProductDetail.tmall?method=getSellerProductDetail&prdNo=1818516682&trTypeCd=21&trCtgrNo=585021&lCtgrNo=1001325&mCtgrNo=1002005"},{"idx":"1497","sc_code":"11번가","sc_name":"11번가","pcode":"","strType":"2","strPrdCategory":"","strPrdName":"이니스프리 마이팔레트 마이 블러셔 [크림]1호~5호","strPrdMaker":"","strPrdOrigin":"","strPrdBrand":"","strPrdImg":"http://i.011st.com/t/300_0/pd/17/7/3/4/2/8/5/HECmW/1762734285_B.jpg","nPrdCustPrice":"7000","nPrdSellPrice":"7000","isUse":"1","isSoldOut":"0","nPrdStockQty":"1","strLinkUrl":"http://www.11st.co.kr/product/SellerProductDetail.tmall?method=getSellerProductDetail&prdNo=1762734285&trTypeCd=21&trCtgrNo=585021&lCtgrNo=1001325&mCtgrNo=1002007"},{"idx":"1500","sc_code":"11번가","sc_name":"11번가","pcode":"","strType":"2","strPrdCategory":"","strPrdName":"[더샘] 샘물 싱글 블러셔-CR03(선샤인코랄) - 11번가","strPrdMaker":"","strPrdOrigin":"","strPrdBrand":"","strPrdImg":"http://i.011st.com/t/300_0/pd/18/0/1/9/5/5/7/wTNNG/2033019557_B.jpg","nPrdCustPrice":"3900","nPrdSellPrice":"3900","isUse":"1","isSoldOut":"0","nPrdStockQty":"1","strLinkUrl":"http://www.11st.co.kr/product/SellerProductDetail.tmall?method=getSellerProductDetail&prdNo=2033019557&NaPm=ct=jrzvgud4|ci=a111e2a6173dc53cd6f83f5369918a3ad730f3dd|tr=slsl|sn=17703|hk=95e2d169c46abdc9fe6fc7b87e4a3047c7858274&utm_term=&utm_campaign=%B3%D7..."},{"idx":"1503","sc_code":"11번가","sc_name":"11번가","pcode":"","strType":"2","strPrdCategory":"","strPrdName":"마몽드 하이라이트 립 틴트 10종/택1 - 11번가","strPrdMaker":"","strPrdOrigin":"","strPrdBrand":"","strPrdImg":"http://i.011st.com/t/300_0/an/5/9/8/6/2/5/1535598625_B.jpg","nPrdCustPrice":"3500","nPrdSellPrice":"3500","isUse":"1","isSoldOut":"0","nPrdStockQty":"1","strLinkUrl":"http://www.11st.co.kr/product/SellerProductDetail.tmall?method=getSellerProductDetail&prdNo=1535598625&trTypeCd=21&trCtgrNo=585021&lCtgrNo=1001325&mCtgrNo=1002006#ui_option_layer1"},{"idx":"1506","sc_code":"11번가","sc_name":"11번가","pcode":"","strType":"2","strPrdCategory":"","strPrdName":"에뛰드하우스 해피위드 피글렛  컬러인 리퀴드 립스 에어무스 N - 11번가","strPrdMaker":"","strPrdOrigin":"","strPrdBrand":"","strPrdImg":"http://i.011st.com/t/300_0/pd/19/6/8/8/5/6/3/KFZpy/2291688563_B.jpg","nPrdCustPrice":"7470","nPrdSellPrice":"7470","isUse":"1","isSoldOut":"0","nPrdStockQty":"1","strLinkUrl":"http://www.11st.co.kr/product/SellerProductDetail.tmall?method=getSellerProductDetail&prdNo=2291688563&NaPm=ct=jrzvtzcw|ci=c55efb79d6b6c00076d11571690f402a2dfb1a2a|tr=slsl|sn=17703|hk=8328b28545b8ec46587acddb4fecf1a50bfadaa1&utm_term=&utm_campaign=%B3%D7..."}],"nTotalCount":5}
         * secondCategoryCode2 : 2010
         * secondCategoryCode3 : 2010
         */
        public String strChatlog;
        public String id;
        public String videoType;
        public String isOnAir;
        public String liveVod;
        public String userId;
        public String stream;
        public String streamHls;
        public String userNick;
        public String userImage;
        public Object strChatKey;
        public String strContentSize;
        public String isFavorite;
        public String scrapCount;
        public String isScrap;
        public String videoRato;
        public String viewerCount;
        public String categoryCode;
        public String secondCategoryCode1;
        public String title;
        public String favoriteCount;
        public String strNoti;
        public String strTag;
        public String thumbnailUrl;
        public String largeThumbnailUrl;
        public String show_YN;
        public String user_show_YN;
        public String delete_YN;
        public String gender;
        public String age;
        public String reg_date;
        public String strRegDate;
        public String update_date;
        public String share_YN;
        public String comment_YN;
        public int shareCount;
        public RelationPrdBeanXXX relationPrd;
        public String secondCategoryCode2;
        public String secondCategoryCode3;
        public String min_price;
        public String comment_cnt;
        public String min_price_product;
        public String order_cnt;
        public String order_price;

        public static class RelationPrdBeanXXX {
            /**
             * data : [{"idx":"1494","sc_code":"11번가","sc_name":"11번가","pcode":"","strType":"2","strPrdCategory":"","strPrdName":"에뛰드하우스 룩앳마이 아이즈 카페_23색상 아이섀도 - 11번가","strPrdMaker":"","strPrdOrigin":"","strPrdBrand":"","strPrdImg":"http://i.011st.com/t/300_0/pd/18/5/1/6/6/8/2/cDeHj/1818516682_B.jpg","nPrdCustPrice":"2800","nPrdSellPrice":"2800","isUse":"1","isSoldOut":"0","nPrdStockQty":"1","strLinkUrl":"http://www.11st.co.kr/product/SellerProductDetail.tmall?method=getSellerProductDetail&prdNo=1818516682&trTypeCd=21&trCtgrNo=585021&lCtgrNo=1001325&mCtgrNo=1002005"},{"idx":"1497","sc_code":"11번가","sc_name":"11번가","pcode":"","strType":"2","strPrdCategory":"","strPrdName":"이니스프리 마이팔레트 마이 블러셔 [크림]1호~5호","strPrdMaker":"","strPrdOrigin":"","strPrdBrand":"","strPrdImg":"http://i.011st.com/t/300_0/pd/17/7/3/4/2/8/5/HECmW/1762734285_B.jpg","nPrdCustPrice":"7000","nPrdSellPrice":"7000","isUse":"1","isSoldOut":"0","nPrdStockQty":"1","strLinkUrl":"http://www.11st.co.kr/product/SellerProductDetail.tmall?method=getSellerProductDetail&prdNo=1762734285&trTypeCd=21&trCtgrNo=585021&lCtgrNo=1001325&mCtgrNo=1002007"},{"idx":"1500","sc_code":"11번가","sc_name":"11번가","pcode":"","strType":"2","strPrdCategory":"","strPrdName":"[더샘] 샘물 싱글 블러셔-CR03(선샤인코랄) - 11번가","strPrdMaker":"","strPrdOrigin":"","strPrdBrand":"","strPrdImg":"http://i.011st.com/t/300_0/pd/18/0/1/9/5/5/7/wTNNG/2033019557_B.jpg","nPrdCustPrice":"3900","nPrdSellPrice":"3900","isUse":"1","isSoldOut":"0","nPrdStockQty":"1","strLinkUrl":"http://www.11st.co.kr/product/SellerProductDetail.tmall?method=getSellerProductDetail&prdNo=2033019557&NaPm=ct=jrzvgud4|ci=a111e2a6173dc53cd6f83f5369918a3ad730f3dd|tr=slsl|sn=17703|hk=95e2d169c46abdc9fe6fc7b87e4a3047c7858274&utm_term=&utm_campaign=%B3%D7..."},{"idx":"1503","sc_code":"11번가","sc_name":"11번가","pcode":"","strType":"2","strPrdCategory":"","strPrdName":"마몽드 하이라이트 립 틴트 10종/택1 - 11번가","strPrdMaker":"","strPrdOrigin":"","strPrdBrand":"","strPrdImg":"http://i.011st.com/t/300_0/an/5/9/8/6/2/5/1535598625_B.jpg","nPrdCustPrice":"3500","nPrdSellPrice":"3500","isUse":"1","isSoldOut":"0","nPrdStockQty":"1","strLinkUrl":"http://www.11st.co.kr/product/SellerProductDetail.tmall?method=getSellerProductDetail&prdNo=1535598625&trTypeCd=21&trCtgrNo=585021&lCtgrNo=1001325&mCtgrNo=1002006#ui_option_layer1"},{"idx":"1506","sc_code":"11번가","sc_name":"11번가","pcode":"","strType":"2","strPrdCategory":"","strPrdName":"에뛰드하우스 해피위드 피글렛  컬러인 리퀴드 립스 에어무스 N - 11번가","strPrdMaker":"","strPrdOrigin":"","strPrdBrand":"","strPrdImg":"http://i.011st.com/t/300_0/pd/19/6/8/8/5/6/3/KFZpy/2291688563_B.jpg","nPrdCustPrice":"7470","nPrdSellPrice":"7470","isUse":"1","isSoldOut":"0","nPrdStockQty":"1","strLinkUrl":"http://www.11st.co.kr/product/SellerProductDetail.tmall?method=getSellerProductDetail&prdNo=2291688563&NaPm=ct=jrzvtzcw|ci=c55efb79d6b6c00076d11571690f402a2dfb1a2a|tr=slsl|sn=17703|hk=8328b28545b8ec46587acddb4fecf1a50bfadaa1&utm_term=&utm_campaign=%B3%D7..."}]
             * nTotalCount : 5
             */

            public int nTotalCount;
            public List<DataBeanXXXX> data;

            public static class DataBeanXXXX {
                /**
                 * idx : 1494
                 * sc_code : 11번가
                 * sc_name : 11번가
                 * pcode :
                 * strType : 2
                 * strPrdCategory :
                 * strPrdName : 에뛰드하우스 룩앳마이 아이즈 카페_23색상 아이섀도 - 11번가
                 * strPrdMaker :
                 * strPrdOrigin :
                 * strPrdBrand :
                 * strPrdImg : http://i.011st.com/t/300_0/pd/18/5/1/6/6/8/2/cDeHj/1818516682_B.jpg
                 * nPrdCustPrice : 2800
                 * nPrdSellPrice : 2800
                 * isUse : 1
                 * isSoldOut : 0
                 * nPrdStockQty : 1
                 * strLinkUrl : http://www.11st.co.kr/product/SellerProductDetail.tmall?method=getSellerProductDetail&prdNo=1818516682&trTypeCd=21&trCtgrNo=585021&lCtgrNo=1001325&mCtgrNo=1002005
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