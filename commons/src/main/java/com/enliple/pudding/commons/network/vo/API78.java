package com.enliple.pudding.commons.network.vo;

import java.util.List;

/**
 * Created by Kim Joonsung on 2018-12-17.
 */
public class API78 {

    /**
     * nTotalCount : 10
     * pageCount : 1
     * data : [{"idx":"사용자 IDX","strUserId":"팔로우 실행자","strToUserId":"팔로우 대상자","isFollow":"Y","dateReg":"등록일","strUserName":"이름","strUserNickname":"닉네임","strMemo":"자기소개","strThumbnail":"프로필 이미지"}]
     */

    public int nTotalCount;
    public int pageCount;
    public List<FollowItem> data;

    public static class FollowItem {
        /**
         * idx : 사용자 IDX
         * strUserId : 팔로우 실행자
         * strToUserId : 팔로우 대상자
         * isFollow : Y
         * dateReg : 등록일
         * strUserName : 이름
         * strUserNickname : 닉네임
         * strMemo : 자기소개
         * strThumbnail : 프로필 이미지
         */

        public String idx;
        public String strUserId;
        public String strToUserId;
        public String isFollow;
        public String isFollow_each_other;
        public String dateReg;
        public String strUserName;
        public String strUserNickname;
        public String strMemo;
        public String strThumbnail;
    }
}
