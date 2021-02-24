package com.enliple.pudding.shoppingcaster.data

/**
 * 미디어 버킷 정보
 * Created by hkcha on 2018. 3. 6..
 *
 * @param id                    MediaStore 상의 Bucket ID
 * @param name                  해당 버켓의 이름
 * @param path                  해당 버켓의 기본 Path
 * @param latestMediaId         최종적으로 해당 버켓에 생성된 이미지의 MediaStore ID
 * @param latestMediaPath       최종적으로 해당 버켓에 생성된 이미지의 Path 경로
 * @param mediaCount            해당 버켓 내 위치해 있는 미디어 개수
 */
@Suppress("MemberVisibilityCanBePrivate")
data class BucketInfo(val id: Long, val name: String, val path: String, val latestMediaId: Long,
                      val latestMediaPath: String, val mediaCount: Int) {
    override fun toString(): String {
        return "[MediaInfo] id:$id, name:$name, path:$path, " +
                "latestMediaId:$latestMediaId, latestMediaPath:$latestMediaPath, " +
                "mediaCount:$mediaCount"
    }
}