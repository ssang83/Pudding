package com.enliple.pudding.shoppingcaster.data

/**
 * 버킷 내 미디어 정보
 * Created by hkcha on 2018. 3. 6..
 *
 * @param id                    MediaStore 상의 Image ID
 * @param path                  실제 파일이 위치해 있는 경로
 * @param mimeType              미디어 타입 MIME
 */
@Suppress("MemberVisibilityCanBePrivate")
data class MediaInfo(val id: Long, val path: String?, val mimeType: String?) {
    override fun toString(): String {
        return "[MediaInfo] id:$id, path:$path, mimeType:$mimeType"
    }
}