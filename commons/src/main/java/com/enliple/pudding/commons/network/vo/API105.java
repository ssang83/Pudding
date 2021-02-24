package com.enliple.pudding.commons.network.vo;

import java.util.List;

/**
 * Created by Kim Joonsung on 2019-01-04.
 */
public class API105 {

    /**
     * result : success
     * nTotalCount : 13
     * data : [{"stream_key":"16589831f86613f8 // 영상 스트림 키","thumb":"https://thumb.midibus.kinxcdn.com/262/165898b934c916dd.png // 영상 썸네일 URL","title":"세로영상샘플2 // 영상 제목","share_user_cnt":"1 // 영상 공유한 유저 수","order_price":"110300// 상품 판매 총 금액","save_price":"11030 // 정산 예정 금액","click":"0 // 상품 클릭 수 [로직 미구현]"}]
     */

    public String result;
    public int nTotalCount;
    public List<SharedItem> data;

    public static class SharedItem {
        /**
         * stream_key : 16589831f86613f8 // 영상 스트림 키
         * thumb : https://thumb.midibus.kinxcdn.com/262/165898b934c916dd.png // 영상 썸네일 URL
         * title : 세로영상샘플2 // 영상 제목
         * share_user_cnt : 1 // 영상 공유한 유저 수
         * order_price : 110300// 상품 판매 총 금액
         * save_price : 11030 // 정산 예정 금액
         * click : 0 // 상품 클릭 수 [로직 미구현]
         */

        public String stream_key;
        public String thumb;
        public String title;
        public String share_user_cnt;
        public String order_price;
        public String save_price;
        public String click;
    }
}
