package com.enliple.pudding.common

import com.enliple.pudding.commons.network.vo.API114
import com.enliple.pudding.commons.network.vo.VOD

/**
 * Created by Kim Joonsung on 2019-04-26.
 *
 * 비디오 재생 시 공통으로 사용하는 영상 데이터를 담는 Container
 */
class VideoDataContainer {
    companion object {
        @Volatile
        private var instance:VideoDataContainer? = null

        @Synchronized fun getInstance() : VideoDataContainer {
            if(instance == null) {
                instance = VideoDataContainer()
            }

            return instance as VideoDataContainer
        }
    }

    var mVideoData:MutableList<VOD.DataBeanX> = mutableListOf()             // 공통으로 사용하는 영상 리스트
    var mHomeHotData:MutableList<API114.HotDataItem> = mutableListOf()      // 홈 뷰페이저에서만 사용하는 영상 리스트(dbKey가 변경될 경우 영상 리스트를 못가져오는 문제 발생)
}