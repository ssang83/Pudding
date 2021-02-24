package com.enliple.pudding.shoppingplayer

/**
 * 재생이 지원되는 스트리밍 타입 열거
 * @author hkcha
 * @since 2018.05.15
 */
enum class StreamingType {
    FILE,
    HLS,
    RTMP,
    RTSP,
    MPEG_DASH,
    UNKNOWN                     // Error CASE
}