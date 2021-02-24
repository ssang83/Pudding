package com.enliple.pudding.commons.shoppingcommons.api.request

import com.enliple.pudding.commons.shoppingcommons.api.response.CookieRemainResponse
import com.google.gson.annotations.SerializedName
import io.reactivex.Flowable
import retrofit2.http.*

/**
 * 방송간 사용자 푸딩 전송 요청 서비스
 * @author hkcha
 * @since 2018.08.28
 */
interface BroadcastCookieService {

    /**
     * 해당 userId 사용자의 잔여 푸딩상태를 조회
     */
    @GET("cookie/{userId}")
    fun requestRemainedCookie(@Header("jwt") jwt: String,
                              @Path("userId") userId: String): Flowable<CookieRemainResponse>

    /**
     * 타 사용자에게 푸딩를 전송
     */
    @POST("cookie/{userId}")
    fun transferCookie(@Header("jwt") jwt: String,
                       @Path("userId") userId: String,
                       @Body params: CookieTransferData): Flowable<CookieRemainResponse>

    /**
     * 푸딩 전송 Parameter
     * @param strUserId     푸딩 보내는 측 사용자 아이디
     * @param strToUserId   푸딩 받는 측 사용자 아이디
     * @param quantity      보낼 푸딩 수량
     * @param transferType  전송방법 (give, ...)
     */
    data class CookieTransferData(@SerializedName("strUserId") val sendUserId: String,
                                  @SerializedName("strToUserId") val receiveUserId: String,
                                  @SerializedName("nQuantity") val quantity: Int)
}