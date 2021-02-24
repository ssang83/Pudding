package com.enliple.pudding.commons.network.vo;

/**
 * Created by Kim Joonsung on 2019-01-31.
 */
public class API115 {

    /**
     * result : success
     * nTotalCount : 1
     * data : {"evId":"5 // 이벤트 INDEX","evTitle":"test 이미지형 // 이벤트 제목","mainImg":"http://lieon.co.kr/data/event/thumb/1548737371_main.jpg // 이벤트 메인 이미지","sub_img1":"// 이벤트 상세 이미지1","sub_img2":"// 이벤트 상세 이미지2","sub_img3":"// 이벤트 상세 이미지3"}
     */

    public String result;
    public int nTotalCount;
    public EventImageItem data;

    public static class EventImageItem {
        /**
         * evId : 5 // 이벤트 INDEX
         * evTitle : test 이미지형 // 이벤트 제목
         * mainImg : http://lieon.co.kr/data/event/thumb/1548737371_main.jpg // 이벤트 메인 이미지
         * sub_img1 : // 이벤트 상세 이미지1
         * sub_img2 : // 이벤트 상세 이미지2
         * sub_img3 : // 이벤트 상세 이미지3
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
    }
}
