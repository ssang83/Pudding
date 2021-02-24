package com.enliple.pudding.commons.network.vo;

import java.util.List;

/**
 * Created by Kim Joonsung on 2018-12-19.
 */
public class API67 {

    /**
     * result : success
     * nTotalCount : 1
     * data : [{"is_id":"리뷰 아이디","ct_id":"장바구니 INDEX","is_type":"리뷰 타입","is_scroe":"리뷰 점수","is_subject":"리뷰 제목","is_content":"리뷰 내용","is_photo":["http://api.lieon.co.kr/images/live/public/gift106.png"],"is_time":"리뷰 등록일","mb_id":"리뷰 작성자 아이디","mb_nick":"리뷰 작성자 닉네임","mb_user_img":"리뷰 작성자 썸네일","od_id":"2018120618102366 // 주문 번호","od_time":"2018-12-06 18:10:24  // 주문 시간","it_name":"도트 랩 롱 원피스  // 상품 이름","it_img1":"https://images-stage.shaptree.com/images/stores/Jo7nRpZwye/products/1602679623363419_400.gif  // 상품 이미지 URL","ct_option":"리뷰 옵션","ct_price":"10000 // 상품 가격","ct_status":"환불 완료 // 상품 상태","trackingInfo_name":"// 상품 배송회사","trackingInfo_code":"// 상품 배송 운송코드","trackingInfo_url":"// 상품 배송 조회 URL","recommend":"리뷰 추천수","not_recommend":"리뷰 비추천수","is_recommend":"2 // 0:추천안함, 1:추천, 2:비추천"}]
     */

    public String result;
    public int nTotalCount;
    public List<ReviewItem> data;

    public static class ReviewItem {
        /**
         * is_id : 리뷰 아이디
         * ct_id : 장바구니 INDEX
         * is_type : 리뷰 타입
         * is_scroe : 리뷰 점수
         * is_subject : 리뷰 제목
         * is_content : 리뷰 내용
         * is_photo : ["http://api.lieon.co.kr/images/live/public/gift106.png"]
         * is_time : 리뷰 등록일
         * mb_id : 리뷰 작성자 아이디
         * mb_nick : 리뷰 작성자 닉네임
         * mb_user_img : 리뷰 작성자 썸네일
         * od_id : 2018120618102366 // 주문 번호
         * od_time : 2018-12-06 18:10:24  // 주문 시간
         * it_name : 도트 랩 롱 원피스  // 상품 이름
         * it_img1 : https://images-stage.shaptree.com/images/stores/Jo7nRpZwye/products/1602679623363419_400.gif  // 상품 이미지 URL
         * ct_option : 리뷰 옵션
         * ct_price : 10000 // 상품 가격
         * ct_status : 환불 완료 // 상품 상태
         * trackingInfo_name : // 상품 배송회사
         * trackingInfo_code : // 상품 배송 운송코드
         * trackingInfo_url : // 상품 배송 조회 URL
         * recommend : 리뷰 추천수
         * not_recommend : 리뷰 비추천수
         * is_recommend : 2 // 0:추천안함, 1:추천, 2:비추천
         */

        public String is_id;
        public String it_id;
        public String ct_id;
        public String store_name;
        public String is_type;
        public String is_score;
        public String is_subject;
        public String is_content;
        public String is_time;
        public String mb_id;
        public String mb_nick;
        public String mb_user_img;
        public String od_id;
        public String od_time;
        public String it_name;
        public String it_img1;
        public String ct_option;
        public String ct_price;
        public String ct_status;
        public String trackingInfo_name;
        public String trackingInfo_code;
        public String trackingInfo_url;
        public String recommend;
        public String not_recommend;
        public String is_recommend;
        public List<String> is_photo;
    }
}
