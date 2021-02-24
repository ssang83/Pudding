package com.enliple.pudding.commons.shoppingcommons.api.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * 현재 사용자의 잔여 푸딩 응답
 * @param status        성공 / 실패 여부
 * @param message       싪패 : 실패 사유
 * @param userId        성공 : 조회된 사용자 아이디
 * @param remain        성공 : 잔여 푸딩 수량
 */
data class CookieRemainResponse(@Expose @SerializedName("status") val status: Boolean = true,
                                @Expose @SerializedName("message") val message: String?,
                                @Expose @SerializedName("strUserId") val userId: String,
                                @Expose @SerializedName("nRemain") val remain: Int = 0) {
    override fun toString(): String {
        return "[CookieRemainResponse] status:$status, message:$message, strUserId:$userId," +
                "remain:$remain"
    }
}