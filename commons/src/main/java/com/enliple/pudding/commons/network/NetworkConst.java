package com.enliple.pudding.commons.network;

import com.enliple.pudding.commons.BuildConfig;

public class NetworkConst {
    // api 주소
    public static final String SERVER_API_URL = BuildConfig.API_URL + "v1/";

    // 카테고리 전체 이미지
    public static final String CATEGORY_ALL_IMAGE_API = BuildConfig.IMAGE_URL + "app/category/category_all.png";

    // 카테고리 전체 on 이미지
    public static final String CATEGORY_ALL_ON_IMAGE_API = BuildConfig.IMAGE_URL + "app/category/item/category_all_on.png";

    // 카테고리 전체 off 이미지
    public static final String CATEGORY_ALL_OFF_IMAGE_API = BuildConfig.IMAGE_URL + "app/category/item/category_all_off.png";

    // 쿠키결제 이니시스 주소
    public static final String COOKIE_PAYMENT_API = BuildConfig.API_URL + "inip/m_gift.php?u=";

    // 이니시스 결제 RETURN URL
    public static final String RETURN_URL = BuildConfig.API_URL + "inip/INIPayment_return.php";
    public static final String NEXT_URL = BuildConfig.API_URL + "inip/INIPayment_next.php";
    public static final String NOTI_URL = BuildConfig.API_URL + "inip/INIPayment_next.php";

    // 통계 페이지 URL
    public static final String STATISTICS_DAILY_URL = BuildConfig.URL + "statistics2/m_daily_info.php?id=";
    public static final String STATISTICS_BOARD_URL = BuildConfig.URL + "statistics2/m_video_info.php?id=";
    public static final String STATISTICS_SALE_URL = BuildConfig.URL + "statistics2/m_sale_info.php?id=";
    public static final String STATISTICS_RANK_URL = BuildConfig.URL + "statistics/m_rank_info.php?id=";

    // 우편 번호
    public static final String POST_ADDRESS_FINDER_URL = BuildConfig.URL + "postcode/search_address.html";

    // KCB 인증
    public static final String KCB_IDENTIFICATION_REQUEST_URL = BuildConfig.URL + "plugin/okname/hpcert1.php?device=mobile";

    // 개인정보 수집 및 이용동의
    public static final String PRIVACY_POLICY_URL = BuildConfig.URL + "shop/policy_privacy.php?device=mobile";

    // 서비스 이용약관 URL
    public static final String TERM_OF_USE_URL = BuildConfig.URL + "shop/policy_service.php?device=mobile";

    // 회원탈퇴 안내 URL
    public static final String WITHDRAWAL_NOTIFICATION_URL = "https://puddinglive.com/shop/policy_leave.php?device=mobile";
}
