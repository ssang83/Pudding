package com.enliple.pudding.commons.chat.dao

import com.enliple.pudding.commons.chat.ChatManager

/**
 * 텍스트 채팅 데이터 (기본)
 *
 * @param author            작성자
 * @param text              채팅 텍스트
 * @param image             채팅 이미지
 * @param data              부가 데이터
 * @param time              작성(전송) 시간
 */
data class AdapterChatData(val cmd: String?,
                           val author: String?,
                           val text: String?,
                           val time: Long?) {
    override fun toString(): String {
        return "[${AdapterChatData::class.java.simpleName}] author:$author, text:${text
                ?: "null"}," +
                " time:${time ?: "null"} , cmd:${cmd ?: ChatManager.NORMAL_CHAT_CMD}"
    }
}