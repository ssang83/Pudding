package com.enliple.pudding.model

import com.enliple.pudding.commons.chat.ChatManager

/**
 * 텍스트 채팅 데이터 (기본)
 *
 * @param userId            작성자
 * @param nickName          작성자 닉네임
 * @param text              채팅 텍스트
 * @param data              부가 데이터
 * @param time              작성(전송) 시간
 */
data class LastLiveChatData(val cmd: String?,
                           val userId: String?,
                            val nickName: String?,
                           val text: String?,
                           val time: Long?) {
    override fun toString(): String {
        return "[${LastLiveChatData::class.java.simpleName}] userId:$userId, nickName:$nickName, text:${text
                ?: "null"}," +
                " time:${time ?: "null"} , cmd:${cmd ?: ChatManager.NORMAL_CHAT_CMD}"
    }
}