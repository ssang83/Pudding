/*
 * Copyright (C) 2014 nohana, Inc.
 * Copyright 2017 Zhihu Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an &quot;AS IS&quot; BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.enliple.pudding.commons.network;

public enum NetworkApi {
    VOD0("GET/mui/home/{type}"),   // 홈 구성의 인기, Live, VOD 리스트 정보
    //VOD1("GET/mui/home/follow"),   // 홈 구성의 인기, Live, VOD 리스트 정보
    //VOD2("GET/mui/home/live"),   // 홈 구성의 인기, Live, VOD 리스트 정보
    //VOD3("GET/mui/home/vod"),   // 홈 구성의 인기, Live, VOD 리스트 정보
    API32("GET/vod/upload/{user}/{type}"),     // user 업로드 영상에 대한 리스트 요청
    API33("GET/vod/upload/{user}/{type}"),     // user 업로드 상품에 대한 리스트 요청
    API34("GET/mui/hash"),     // 방송 태그 연관 검색
    API0("GET/vod/upload/{user}"),     // user 업로드 영상에 대한 리스트 요청
    API1("GET/follow"),      //사용자 팔로우 정보 요청
    API2("POST/follow"),     //사용자 팔로우 정보 갱신
    //API3("POST/follow"),     //사용자 팔로우 정보 갱신
    //API4("POST/follow"),     //사용자 팔로우 정보 갱신
    //API5("POST/follow"),     //사용자 팔로우 정보 갱신
    API6("GET/report/{userId}"),     // 사용자 신고 내역 요청
    API7("POST/report/{userId}"),     // 사용자 신고 내역 요청 필요한가..
    API8("POST/favor"),    // 좋아요 등록
    API9("GET/hits"),    // 조회수 요청
    API10("POST/hits"),    // 조회수 등록
    API11("GET/event/"),    // 이벤트 목록을 가져온다.
    API12("GET/{eventId}"),    // 이벤트 상세 내역을 가져온다.
    API13("PUT/leave/{userId}"),    // 사용자 탈퇴 처리
    API14("PUT/password/{userId}"),    // 사용자 비밀번호 갱신
    API15("PUT/broadcast/title"),    // 방송 제목 수정
    API16("PUT/broadcast/notice"),    // 방송 공지 수정
    API17("GET/broadcast/comment"),    // VOD 댓글 목록을 가져온다.
    API18("POST/broadcast/comment"),    // VOD 댓글을 입력한다.
    API19("DELETE/broadcast/comment"),    // VOD 댓글을 삭제한다.
    API21("GET/user/{userId}"),    // 사용자 정보 요청.
    API20("PUT/broadcast/comment"),    // VOD 댓글을 수정한다.
    API22("PUT/user/{userId}"),    // 사용자 정보 수정(Multi-parts 제외).
    API23("GET/user/{userId}/scrap"),    // 사용자 영상 스크랩 목록.
    API24("DELETE/user/{userId}/scrap"),    // 사용자 영상 스크랩 삭제.
    API25("POST/usermodify/{userId}"),    // 사용자 프로필 및 배경 사진 수정
    API26("POST/broadcast/scrap"),    // 영상 스크랩 등록
    API28("GET/search/tagid"),    // 태그ID 검색
    API29("GET/search/vod"),    // 영상 검색
    API30("GET/search/tagname"),    // 태그명 검색
    API31("GET/search/user"),    // 사용자 검색
    API27("GET/search"),    // 검색 메인창
    API35("PUT/support/myqa"),    // 1:1문의 작성
    API36("GET/support/category"),    // 고객센터 1:1문의 작성 시 카테고리 조회
    API38("GET/support/faqcategory"), // FAQ 카테고리 조회
    API37("GET/user/{userId}/lately_vod"), // 사용자 최근 본 영상가져온다.
    API39("GET/support/myorder"), // 1:1문의 작성시 상품조회 클릭 - 주문한상품의 삼품리스트(최근3개월) 노출
    API40("GET/support/myqa"),    // 고객 센터 1:1 문의 리스트
    API41("GET/support/faqlist"),    // 자주 묻는 질문 리스트
    API42("GET/review/report_type"),    // 상품에 대한 리뷰의 신고 타입 목록
    API43("POST/review/report"),    // 리뷰 신고
    API44("POST/review/{userId}"),    // 상품에 대한 리뷰 작성
    API45("GET/review/all"),    // 상품에 대한 리뷰 목록
    API46("GET/review/photo"),    // 상품에 대한 포토리뷰 목록
    API47("POST/review/recommend"),    // 리뷰 추천/비추천 등록
    API48("POST/share/makeLink"),    // 공유 링크 생성
    API49("GET/share/getData"),    // 공유 링크 파싱 - 로그인시에는 Utoken 정보를 넣어야함.
    API50("DELETE/review/recommend"),    // 리뷰 추천/비추천 삭제
    API51("GET/user/{userId}/cookie_history"),    // 푸딩 내역 정보
    API52("GET/user/{userId}/cookie"),    // 나의 푸딩 정보
    API99("GET/chatroom"),    // 채팅 사용자 리스트 정보
    API98("GET/mui/live/{streamKey}"),    // 특정 방송 정보가져오기
    API97("PUT/broadcast/user_show_yn"),    // VOD 사용자 공개 여부
    API96("PUT/broadcast/share_yn"),    // VOD 공유 여부
    API95("PUT/broadcast/comment_yn"),    // VOD 댓글 허용 여부
    API94("GET/config"),    // 앱 기본 설정 값 정보 가져온다.
    API93("GET/products/qna_type"),    // 상품 문의 타입
    API92("POST/products/{productId}/qna"),    // 상품 문의 등록
    API91("GET/products/{productId}/qna"),    // 상품 전체 문의 목록
    API91_1("GET/products/{productId}/qna?user="),    // 상품 내 문의 목록
    API91_2("GET/products/{productId}/qna?type="),    // 상품 타입별 문의 목록
    API91_3("GET/products/{productId}/qna?secret_out="),    // 상품 문의 비밀글 제외
    API90("GET/search/product"),    // 상품 검색
    API89("GET/user/{userId}/product_qna"),    // 사용자 상품문의 목록
    API88("GET/rank"),    // 랭킹 목록
    API87("DELETE/broadcast"),    // 방송 영상 삭제
    API86("POST/vod/upload/{user}/{type}"),    // VOD 업로드
    API85("POST/push/regist"),    // FCM Push 토근 저장
    API84("POST/broadcast/gift"),    // 영상 푸딩 선물하기
    API83("GET/live/{streamKey}/report/product"),    // 라이브 방송 상품 판매 현황 - 결제완료, 상품준비중만 해당
    API82("GET/live/{streamKey}/report/cookie"),    // 라이브 방송 푸딩 선물 현황
    API81("GET/mui/category/{categoryid}"),    // 카테고리 정보 요청
    API80("POST/auth/utoken"),    // 사용자 로그인 처리
    API79("GET/auth/ptoken"),    // Public Token 요청
    API78("GET/follow"),    // 사용자 팔로우 정보 요청
    API77("GET/follow"),    // 사용자 팔로우 검색정보 요청
    API76("POST/user/{userId}"),    // 사용자 회원가입
    API75("POST/cellphone"),    // 본인인증 처리 후 기존 회원 가입 여부 체크
    API74("DELETE/productlink"),    // 링크에 대한 정보 삭제 처리
    API73("POST/productlink"),    // 링크에 대한 정보 등록 처리
    API72("POST/live/stream/{user}"),    // 사용자의 방송정보 등록
    API71("POST/{path}"),    // MIDIBUS Server 로 VOD 파일을 전송, (주의) RetroFit 개체의 BaseURL 을 Midibus HOST 로 설정해야 함
    API70("GET/products"),    // 상품 리스트 정보요청
    API69("GET/productstore"),    // 상점 정보 요청
    API68("GET/user/{userId}/not_review"),    // 사용자 미작성 리뷰 리스트
    API67("GET/user/{userId}/review"),    // 사용자 작성 리뷰 리스트
    API66("DELETE/review"),    // 사용자 작성 리뷰 삭제
    API65("GET/user/{userId}/alarm"),    // 사용자 알림 설정 가져오기
    API64("PUT/user/{userId}/alarm"),    // 사용자 알림 설정하기
    API63("GET/message/my_message"),    // 최근 메시지 정보 가져오기
    API62("GET/message/more_message"),    // 신규 메시지 가져오기(현재 보고 있는 메시지 이후 DATA 가져온다)
    API61("GET/message/before_message"),    // 이전 메시지 가져오기(현재 보고 있는 메시지 이전 DATA 가져온다)
    API60("GET/message/new_message"),    // 새로운 메시지 건수 확인
    API59("PUT/message/message"),    // 1:1 메시지 작성
    API58("GET/block?user="),    // 메시시 차단 내역
    API57("POST/block"),    // 메시시 차단 하기
    API56("GET/config/company"),    // 사업자 정보 가져오기
    API55("GET/user/{userId}/lately_product"),    // 최근 본 상품 가져오기
    API54("GET/message/message_list"),    // 메시지 리스트 가져오기
    API100("DELETE/user/{userId}/lately_vod"),    // 사용자 최근 본 영상 삭제
    API101("GET/user/{userId}/point"),    // 사용자 포인트 내역 가져오기
    API102("GET/user/{userId}/save_point"),    // 사용자 적립 포인트 가져오기
    API103("GET/user/{userId}/expire_point"),    // 사용자 소멸 포인트 가져오기
    API104("POST/message/delete"),    // 1:1 메시지 삭제
    API105("GET/user/{userId}/shared_vod"),    // 공유된 영상 목록
    API106("GET/user/{userId}/share"),    // 공유한 영상 목록
    API107("GET/user/{userId}/sanction"),    // 제재내역
    API108("PUT/message/alarm"),    // 1:1 메시지 알람 설정/해제
    API109("DELETE/user/{userId}/lately_product"),    // 사용자 최근 본 상품 삭제
    API110("GET/support/notice"),    // 공지사항 리스트
    API111("GET/version/android"),    // 버전 정보
    API112("GET/check/nick/"),    // 닉네임 중복 체크
    API113("GET/mui/recommend/user"),    // Follow 없을 경우 추천 유저 목록 가져오기
    API114("GET/mui/main/{type}"),    // 신규 홈 리스트 가져오기
    API115("GET/event/{eventId}/img"),    // 이벤트 이미지 타입 상세
    API116("GET/event/{eventId}/vod"),    // 이벤트 영상 타입 상세
    API117("GET/event/{eventId}/prd"),    // 이벤트 상품 타입 상세
    API118("PUT/banner/{bn_id}/hit"),    // 배너 조회수 업데이트
    API119("GET/productstore"),    // 상품 정보 요청
    API120("POST/cert/id/num"),    // 아이디 찾기 - 인증번호 전송
    API121("POST/cert/id/check"),    // 아이디 찾기 - 인증번호 확인
    API122("DELETE/push/regist"),    // FCM Push 토근 삭제
    API123("POST/support/hit/{type}/{id}"),    // 공지사항 및 자주 묻는 질문의 조회수 증가
    API124("GET/search/formation"),    // 편성표 관련 검색
    API125("POST/formation/{userId}/resv"),    // 편성표 시청 예약
    API126("POST/products/{productId}/wish"),    //  상품 찜 설정
    API127("GET/products"),    //  상품 찜 목록 가져오기
    API128("DELETE/products/wish"),    //  상품 찜 삭제
    API129("GET/formation"),    //  편성표 리스트
    API130("GET/formation/{userId}"),    //  내 편성표 내역
    API131("GET/formation/{userId}/resv"),    //  내 시청 예약 내역
    API132("POST/formation/{userId}"),    //  편성표 등록 및 수정
    API133("DELETE/formation/{userId}"),    //  편성표 삭제
    API134("POST/cert/pw/num"),    //  비밀번호 찾기 - 인증번호 전송
    API135("POST/cert/pw/check"),    //  비밀번호 찾기 - 인증번호 확인
    API136("POST/formation/{userId}/start"),    //  편성표 방송 시작
    API137("GET/productlink/advertiser"),    //  모비온 광고주 리스트
    API138("GET/productlink"),    //  링크 상품 URL 파싱
    API139("GET/productlink/url"),    //  링크 상품 DRC URL
    API140("GET/user/{userId}/notice"),    //  사용자 알림 내역
    API141("DELETE/user/{userId}/notice"),    //  사용자 알림 내역 삭제
    API142("POST/chatconnect"),    //  채팅방 정보 호출
    API143("GET/mui/vod/{streamKey}"),    // 특정 비디오 정보가져오기
    API144("GET/rank/keyword"),    // 인기 검색어 정보
    API145("GET/user/{userId}/wish"),    // 내 찜상품 목록
    API151("GET/user/{userId}/order"),
    API152("POST/cert/id/check_direct"),     // 인증번호 없이 아이디 찾기
    API153("POST/cellphone/{userId}"),     // 인증정보 저장
    API154("POST/cert/join/num"),    // 회원가입 - 인증번호 전송
    API155("GET/user/{userId}/exchange"), // 포인트 및 쿠키 환전 목록
    API156("GET/mui/popup");     // 메인 팝업 가져오기

    private final String mMimeTypeName;

    NetworkApi(String mimeTypeName) {
        mMimeTypeName = mimeTypeName;
    }

    @Override
    public String toString() {
        return mMimeTypeName;
    }

    public String getKey(String api, String arg) {
        return "";
    }
}

//    POST
///live/stream/{user}
//    사용자의 방송정보 등록 (utoken 필요) - Swagger Test 불가
//
//            GET
///vod/upload/{user}
//    user 업로드 영상에 대한 리스트 요청
//
//    POST
///vod/upload/{user}
//    VOD 업로드 관련 API (utoken 필요)
//
//    GET
///products
//    상품 리스트 정보 요청 (스타일썸 shop_deatil.php 동일)
//
//    GET
///products/{productId}
//    특정 상품 정보 요청 (스타일썸 shop_detail.php 와 동일 )
//
//    GET
///productstore
//    상점 정보 요청
//
//            POST
///productlink
//    링크에 대한 정보 처리 (추후 모비온 코드 적용할때 해당 api 로 처리 예정)
//
//    Check API'S Stylessum
//
//    POST
///stylessum/cart_insert
//    장바구니 담기 (스타일썸)
//
//    GET
///stylessum/cart_list
//    장바구니 리스트 요청 (스타일썸)
//
//    POST
///stylessum/cart_update
//    장바구니 상품 갱신(수량 수정) (스타일썸)
//
//    POST
///stylessum/cart_delete
//    장바구니 삭제 (상품별) (스타일썸)
//
//    POST
///stylessum/cart_multi_delete
//    장바구니 다중 삭제 (상품별) (스타일썸)
//
//    GET
///stylessum/order_list
//    주문 리스트 (스타일썸)
//
//    GET
///stylessum/order_detail
//    주문 상세 정보 (스타일썸)
//
//    POST
///stylessum/goods_buy_list
//    주문서 작성 (스타일썸)
//
//    POST
///stylessum/goods_buy_ad
//    주문 결제전 (스타일썸)
//
//    POST
///stylessum/goods_buy_complete
//    구매 확정 (스타일썸)
//
//    POST
///stylessum/product_delivery
//    배송 조회 (스타일썸)
//
//    POST
///stylessum/complaints_list
//    환불 취소 내역 (스타일썸)
//
//    POST
///stylessum/complaints_detail
//    환불취소 상세내역 (스타일썸)
//
//    POST
///stylessum/complaints_cancel
//    주문 취소 요청 (스타일썸)
//
//    POST
///stylessum/complaints_refund
//    반품 요청 (스타일썸)
//
//    POST
///stylessum/complaints_exchange
//    교환 요청 (스타일썸)
//
//    Developer
//
//            GET
///report/{userId}
//    사용자 신고 내역 요청
//
//    POST
///report/{userId}
//    사용자 신고 등록
//
//            GET
///favor/{streamKey}
//    좋아요 수 요청
//
//            POST
///favor/{streamKey}
//    좋아요 등록
//
//    GET
///hits/{streamKey}
//    조회수 요청
//
//    POST
///hits/{streamKey}
//    조회수 등록
//
//    GET
///cookie/{userId}
//    사용자 푸딩 보유량
//
//            POST
///cookie/{userId}
//    푸딩 선물하기
//
//    GET
///cookiehistory/{userId}
//    사용자 푸딩 히스토리
//
//            POST
///cookiehistory/{userId}
//    푸딩 히스토리 등록(구매, 환전)
//
//    GET
///board/{type}
//    게시글 리스트 요청
//
//            PUT
///leave/{userId}
//    사용자 탈퇴 처리 (utoken 필요)
//
//    PUT
///password/{userId}
//    사용자 비밀번호 갱신 (utoken 필요)
//
//    GET
///products/media/{streamKey}
//    해당 영상에 첨부된 상품정보 요청 (추후 내용추가 있을수 있음)
//
//    Test Swagger
//
//    GET
///auth/token
//    웹 Swagger 용 테스트용 token 요청
//    POST
///auth/makehmac
//    모바일과의 HMAC 정보를 확인하는 테스트 API
//    GET
///auth/user
//    테스트용으로 jwt 에 대한 user 정보를 확인할수 있는 테스트 API
//
//    Auth
//
//            GET
///auth/ptoken
//    Public token 요청
//            POST
///auth/utoken
//    사용자 로그인 처리
//            User
//
//    GET
///user/{userId}
//    사용자 정보 요청 (utoken 필요)
//
//    PUT
///user/{userId}
//    사용자 정보 갱신 (utoken 필요)
//
//    POST
///user/{userId}
//    사용자 회원 가입
//
//    Mobile UI
//
//    GET
///mui/category/{categoryid}
//    카테고리 정보 요청