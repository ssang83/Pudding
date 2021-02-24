package com.enliple.pudding.shoppingcaster.data

/**
 * Created by Kim Joonsung on 2018-11-01.
 */
data class VODInfo(val id: Long, val title: String, val path: String, val videoSize: Long,
                   val videoDuration: Long) {
    override fun toString(): String {
        return "[VODInfo] id:$id, title:$title, path:$path, " +
                "videoDuration:$videoDuration"
    }
}