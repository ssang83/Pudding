package com.enliple.pudding.commons.data

/**
 * Created by Kim Joonsung on 2019-01-02.
 */
data class TempVOD(val id: String,
                   val videoUrl: String,
                   val broadCastInfo: String,
                   val thumbnail: ByteArray,
                   val regDate: String)

/**
 * 최근 검색어 DB VO
 */
data class KeywordItem(val id:String,
                      val keyword:String,
                      val regDate: String)