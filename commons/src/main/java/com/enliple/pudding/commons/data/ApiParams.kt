package com.enliple.pudding.commons.data

import com.google.gson.annotations.SerializedName

/**
 * Created by Kim Joonsung on 2018-11-14.
 */
object ApiParams {

    /**
     * 좋아요 등록 parmas
     */
    data class FavorParams(@SerializedName("strUserId") val userId: String,
                           @SerializedName("strType") val videoType: String,
                           @SerializedName("fa_status_YN") val status: String)

    /**
     * 조회수 등록 parmas
     */
    data class HitsParams(@SerializedName("strUserId") val userId: String,
                          @SerializedName("strStreamKey") val streamKey: String,
                          @SerializedName("strType") val videoType: String,
                          @SerializedName("isStatus") val status: String)

    /**
     * 패스워드 변경 parmas
     */
//    data class PasswdParams(@SerializedName("strPwOld") val oldPasswd: String,
//                            @SerializedName("strPwNew") val newPasswd: String,
//                            @SerializedName("strPwChk") val passwdCheck: String)

    data class PasswdParams(@SerializedName("strType") val strType: String,
                            @SerializedName("strCertNum") val strCertNum: String,
                            @SerializedName("strPwOld") val strPwOld: String,
                            @SerializedName("strPwNew") val strPwNew: String,
                            @SerializedName("strPwChk") val strPwChk: String)

    /**
     * 방송 제목 수정 parmas
     */
    data class TitleModifyParams(@SerializedName("streamKey") val streamKey: String?,
                                 @SerializedName("type") val videoType: String,
                                 @SerializedName("title") val title: String,
                                 @SerializedName("user") val userId: String?)

    /**
     * 방송 공지 수정 parmas
     */
    data class NoticeModifyParams(@SerializedName("streamKey") val streamKey: String?,
                                  @SerializedName("type") val videoType: String,
                                  @SerializedName("notice") val notice: String,
                                  @SerializedName("user") val userId: String?)

    /**
     * 영상 댓글 입력 parmas
     */
    data class ReplyInputParams(@SerializedName("streamKey") val streamKey: String?,
                                @SerializedName("comment") val comment: String,
                                @SerializedName("user") val userId: String?,
                                @SerializedName("idx") val idx: String?)

    /**
     * 영상 댓글 수정 parmas
     */
    data class ReplyModifyParams(@SerializedName("idx") val idx: String?,
                                 @SerializedName("comment") val comment: String,
                                 @SerializedName("user") val userId: String?)

    /**
     * 사용자 정보 수정 parmas
     */
    data class UserModifyParams(@SerializedName("userName") val userName: String?,
                                @SerializedName("userSex") val userSex: String,
                                @SerializedName("userBirth") val userBirth: String?,
                                @SerializedName("userNick") val userNick: String?,
                                @SerializedName("userHp") val userHp: String?,
                                @SerializedName("userProfile") val userProfile: String?)

    /**
     * 영상 스크랩 등록 parmas
     */
    data class ScrapParams(@SerializedName("streamKey") val streamKey: String?,
                           @SerializedName("vod_type") val videoType: String?,
                           @SerializedName("user") val userId: String?)

    /**
     * 방송 공지 수정 parmas
     */
    data class OneByOneQAParams(@SerializedName("user") val user: String?,
                                @SerializedName("category") val category: String,
                                @SerializedName("title") val title: String,
                                @SerializedName("content") val content: String,
                                @SerializedName("notification") val notification: String,
                                @SerializedName("orderId") val orderId: String,
                                @SerializedName("cartId") val cartId: String?)
}