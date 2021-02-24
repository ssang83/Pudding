package com.enliple.pudding.commons.chat.dao

import com.google.gson.*
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type

////////////////////////////////////////////////////////////////////////////////////////////////////
// 사용자 레벨
// 8  : 팬
// 9  : 열혈팬
// 10 : for domain
////////////////////////////////////////////////////////////////////////////////////////////////////

/**
 * 채팅 사용자 DAO
 *
 * @param l         채팅방 위치
 * @param a
 * @param dk
 * @param no        채팅방 번호
 * @param id        아이디 (미지정; 비회원시 Randomize 하게 생성하여 지정)
 * @param nk        닉네임
 * @param lv        레벨 (2 이하는 채팅글 입력제한됨)
 * @param an        관리자 여부 (1: true , 0 : false)
 * @param dt        시간정보(TimeStamp)
 */
data class ChatUser(@Expose @SerializedName("l") var l: String,
                    @Expose @SerializedName("a") var a: String,
                    @Expose @SerializedName("dk") var dk: String,
                    @Expose @SerializedName("no") var no: String,
                    @Expose @SerializedName("id") var id: String,
                    @Expose @SerializedName("nk") var nickName: String,
                    @Expose @SerializedName("lv") var level: String,
                    @Expose @SerializedName("an") var an: String,
                    @Expose @SerializedName("dt") var dt: String,
                    var online: Boolean = true) {
    override fun toString(): String {
        return Gson().toJson(this)
    }
}

/**
 * Element 순서를 맞추어 onLeave, onAdd 이벤트를 정상적으로 받기 위해 수행되는 JSOn Serializer
 */
class ChatUserSerializer : JsonSerializer<ChatUser> {
    override fun serialize(src: ChatUser?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return JsonObject().apply {
            add("l", context?.serialize(src?.l))
            add("a", context?.serialize(src?.a))
            add("dk", context?.serialize(src?.dk))
            add("no", context?.serialize(src?.no))
            add("id", context?.serialize(src?.id))
            add("nk", context?.serialize(src?.nickName))
            add("lv", context?.serialize(src?.level))
            add("an", context?.serialize(src?.an))
            add("dt", context?.serialize(src?.dt))
        }
    }
}