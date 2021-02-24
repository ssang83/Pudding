package com.enliple.pudding.commons.network.vo;

import java.util.List;

/**
 * Created by Kim Joonsung on 2018-11-13.
 */
public class API6 {
    /**
     * nTotalCount : 10
     * pageCount : 1
     * data : [{"strUserId":"신고 실행자","strToUserId":"신고 대상자","strReason":"신고 사유","strTitle":"신고 제목","strContent":"신고 내용","strFileName":"신고 첨부파일","strMediaType":"live or VOD","strStreamKey":"live 일 경우 해당 피신고자의 stream_key, VOD 일 경우 해당 영상의 mediaId","dateReg":"등록일","strUserName":"이름","strUserNickname":"닉네임","strMemo":"자기소개","strThumbnail":"프로필 이미지"}]
     */

    public int nTotalCount;
    public int pageCount;
    public List<ReportItem> data;

    public static class ReportItem {
        /**
         * strUserId : 신고 실행자
         * strToUserId : 신고 대상자
         * strReason : 신고 사유
         * strTitle : 신고 제목
         * strContent : 신고 내용
         * strFileName : 신고 첨부파일
         * strMediaType : live or VOD
         * strStreamKey : live 일 경우 해당 피신고자의 stream_key, VOD 일 경우 해당 영상의 mediaId
         * dateReg : 등록일
         * strUserName : 이름
         * strUserNickname : 닉네임
         * strMemo : 자기소개
         * strThumbnail : 프로필 이미지
         */

        public String strUserId;
        public String strToUserId;
        public String strReason;
        public String strTitle;
        public String strContent;
        public String strFileName;
        public String strMediaType;
        public String strStreamKey;
        public String dateReg;
        public String re_status_YN;     // 처리 유형 : N:신고, Y:처리완료, B: 보류
        public String re_manager_reply;
        public String re_manager_reg_date;
        public String strUserName;
        public String strUserNickname;
        public String strMemo;
        public String strThumbnail;
    }
}
