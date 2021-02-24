package com.enliple.pudding.commons.network

import android.content.Context
import android.text.TextUtils
import com.enliple.pudding.commons.app.Utils
import com.enliple.pudding.commons.data.ApiParams
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Callback
import retrofit2.http.HEAD
import java.io.File


// Network 통신 Retrofit 구현 부
class NetworkHandler {
    companion object {
        @Volatile
        private var instance: NetworkHandler? = null

        private var mUserId: String? = null
        private var mCategoryAge: String? = null
        private var mCategoryGender: String? = null
        private var mCategoryCode: String? = null

        private lateinit var mContext: Context

        private lateinit var mManager: NetworkManager

        fun getInstance(context: Context): NetworkHandler {
            if (instance == null) {
                instance = NetworkHandler()
                mManager = NetworkManager.getInstance(context)
                mContext = context
            }

            mUserId = AppPreferences.getUserId(context)
            mCategoryAge = AppPreferences.getCategoryAge(context)
            mCategoryGender = AppPreferences.getCategoryGender(context)
            mCategoryCode = AppPreferences.getHomeVideoCategoryCode(context)

            return instance as NetworkHandler
        }
    }

//    fun run(api: String, arg2: String, arg3: String, arg4: String, arg5: String, arg6: String) {
//        when (api) {
//            NetworkApi.VOD.name -> getHome(arg2, arg3, arg4, arg5)
//            NetworkApi.VOD1.name -> getHome(arg2, arg3, arg4, arg5)
//            NetworkApi.VOD2.name -> getHome(arg2, arg3, arg4, arg5)
//            NetworkApi.VOD3.name -> getHome(arg2, arg3, arg4, arg5)
//            NetworkApi.API1.name -> {
//            }
//        }
//    }

    fun getHomeKey(index: Int, arg1: String, order: String, categoryId: String): String {
        var argument = arg1
        if (TextUtils.isEmpty(arg1)) {
            argument = "1"
        }

        var key = when (index) {
            1 -> getKey(NetworkApi.API114.toString(), "top", argument)
            2 -> getKey(NetworkApi.API114.toString(), "shopping", argument)
            3 -> getKey(NetworkApi.API114.toString(), "vod", argument)
            4 -> getKey(NetworkApi.API114.toString(), "best", argument)
            5 -> getKey(NetworkApi.API114.toString(), "beauty", argument)
            6 -> getKey(NetworkApi.API114.toString(), "travel", argument)
            else -> getKey(NetworkApi.API114.toString(), "top", argument)
        }

        val age = AppPreferences.getCategoryAge(mContext)
        val sex = AppPreferences.getCategoryGender(mContext)
        val user = AppPreferences.getUserId(mContext)!!
        if (categoryId.isNotEmpty()) {
            return """$key&category=$categoryId&age=$age&sex=$sex&user=$user&order=$order"""
        } else {
            return """$key&category=&age=$age&sex=$sex&user=$user&order=$order"""
        }
    }

    fun getProductKey(api:String, arg1: String, arg2: String, arg3: String, arg4: String, arg5: String, arg6: String, arg7: String): String {
        var key = "$api?page=$arg1&perPage=$arg2&category=$arg3&shopKey=$arg4&search=$arg5&order=$arg6&strType=$arg7&deviceId=${Utils.getUniqueID(mContext)}&device_type=android"
        Logger.e("getProductKey ::: $key")
        return key;
    }

    fun getSearchVODKey(arg1: String, arg2: String, arg3: String): String {
        var key = NetworkApi.API29.toString()

        // GET/search/vod?keyword=자동차&order=0&user=android4&category=&age=all&sex=all&page=1
        key = "$key?keyword=$arg1&order=$arg2&user=${AppPreferences.getUserId(mContext)!!}" +
                "&category=${AppPreferences.getHomeVideoCategoryCode(mContext)}&age=${AppPreferences.getCategoryAge(mContext)}" +
                "&sex=${AppPreferences.getCategoryGender(mContext)}&page=$arg3"

        return key
    }

    fun getKey(api: String, arg1: String, arg2: String, arg3: String): String {
        var key = api
        if (TextUtils.isEmpty(key)) {
            return ""
        }
        if (api == NetworkApi.API81.toString()) {     // 카테고리 정보
            key = key.replace("{categoryid}", arg1)
            key = "$key?sc_code=$arg2&category=$arg3"
        } else {
            key = "$key?user=${arg1}&type=${arg2}&search=${arg3}"
        }


        return key
    }

    fun getKey(api: String, arg1: String, arg2: String): String {
        var key = api
        if (TextUtils.isEmpty(key)) {
            return ""
        }

        if (api.startsWith(NetworkApi.API114.toString()) || api.startsWith(NetworkApi.VOD0.toString())) {
            key = key.replace("{type}", arg1)
            key = "$key?page=$arg2"
        } else if (api == NetworkApi.API27.toString()) {     // 검색 메인
            key = "$key?user=${AppPreferences.getUserId(mContext)!!}"
        } else if (api == NetworkApi.API28.toString()) {     // 태그 ID 검색
            key = "$key?t_idx=$arg1&order=$arg2&user=${AppPreferences.getUserId(mContext)!!}"
        } else if (api == NetworkApi.API30.toString()) {     // 태그명 검색
            key = "$key?keyword=$arg1&user=${AppPreferences.getUserId(mContext)!!}"
        } else if (api == NetworkApi.API31.toString()) {    // 사용자 검색
            key = "$key?keyword=$arg1"
        } else if (api == NetworkApi.API90.toString()) {    // 상품 검색
            key = "$key?keyword=$arg1&order=$arg2"
        } else if (api == NetworkApi.API34.toString()) {     // 방송 태그 연관 검색
            key = "$key?streamKey=$arg1&order=$arg2&user=${AppPreferences.getUserId(mContext)!!}"
        } else if (api == NetworkApi.API69.toString()) {     // 상점 리스트
            key = "$key?search=$arg1"
        } else if (api == NetworkApi.API81.toString()) {     // 카테고리 정보
            key = key.replace("{categoryid}", arg1)
            key = "$key?sc_code=$arg2"
        } else {
            if (!TextUtils.isEmpty(arg2)) {
                if (api == NetworkApi.API91_2.toString()) {   // 문의 타입별 목록
                    key = key.replace("{productId}", arg1)
                    key = "$key$arg2"
                } else if (api == NetworkApi.API91_3.toString()) {   // 비밀글 관련 상품 문의
                    key = key.replace("{productId}", arg1)
                    key = "$key$arg2"
                } else if (api == NetworkApi.API83.toString()) {    // 라이브 방송 상품 판매 현황 - 결제완료, 상품준비중만 해당
                    key = key.replace("{streamKey}", arg1)
                    key = "$key?user=$arg2"
                } else if (api == NetworkApi.API37.toString()) {     // 최근 본 영상 리스트
                    key = key.replace("{userId}", arg1)
                    key = "$key?page=$arg2"
                } else {
                    key = key.replace("{user}", arg1)
                    key = key.replace("{type}", arg2)
                }
            } else {
                key = key.replace("{userId}", arg1)
                key = key.replace("{user}", arg1)
                key = key.replace("{type}", arg1)
                key = key.replace("{eventId}", arg1)
                key = key.replace("{streamKey}", arg1)
                key = key.replace("{productId}", arg1)
                key = key.replace("{categoryid}", arg1)
                key = key.replace("{bn_id}", arg1)

                if (api == NetworkApi.API91_1.toString()) {    // 내 문의 목록
                    key = "$key${AppPreferences.getUserId(mContext)!!}"
                } else if (api == NetworkApi.API74.toString()) {  // 링크 상품 삭제
                    key = "$key/$arg1"
                } else if (api == NetworkApi.API66.toString()) {     // 사용자 작성 리뷰 삭제
                    key = "$key?userId=${AppPreferences.getUserId(mContext)!!}&is_id=$arg1"
                } else if (api == NetworkApi.API88.toString()) {    // 랭킹 가져오기
                    key = "$key?type=$arg1"
                }
            }
        }
        return key
    }

    fun run(n: NetworkBus) {
        Logger.e("run: ${n.arg1}")

        when (n.arg1) {
            NetworkApi.VOD0.name -> getHome(n.arg2, n.arg3, n.arg4)
            //NetworkApi.VOD1.name -> getHome(n.arg2, n.arg3, n.arg4, n.arg5)
            //NetworkApi.VOD2.name -> getHome(n.arg2, n.arg3, n.arg4, n.arg5)
            //NetworkApi.VOD3.name -> getHome(n.arg2, n.arg3, n.arg4, n.arg5)
            NetworkApi.API2.name -> postFollow(n.body)
            NetworkApi.API0.name -> getUploadList(n.arg2)
            NetworkApi.API6.name -> getReport(n.arg2)
            NetworkApi.API7.name -> postReportExt(n)
            NetworkApi.API8.name -> postFavorStatus(n.arg2, n.favorParams)
            NetworkApi.API9.name -> getHitsCount(n.arg2)
            NetworkApi.API10.name -> postHitsStatus(n.arg2, n.hitsParams)
            NetworkApi.API11.name -> getEvent()
            NetworkApi.API12.name -> getEventDetail(n.arg2)
            NetworkApi.API13.name -> putUserSignOut()
            NetworkApi.API14.name -> putPassword(n.arg2, n.passwdParams)
            NetworkApi.API15.name -> putBroadcastTitle(n.titleModifyParams)
            NetworkApi.API16.name -> putBroadcastNotice(n.noticeModifyParams)
            NetworkApi.API17.name -> getBroadcastReply(n.arg2)
            NetworkApi.API18.name -> postBroadcastReply(n.replyInputParams)
            NetworkApi.API19.name -> deleteBroadcastReply(n.arg2)
            NetworkApi.API20.name -> putBroadcastReply(n.modifyParams)
            NetworkApi.API21.name -> getUserInfo(n.arg2)
            NetworkApi.API22.name -> putUserInfo(n.body)
            NetworkApi.API23.name -> getScrapVOD(n.arg2)
            NetworkApi.API24.name -> deleteScrapVOD(n.arg2, n.arg3)
            NetworkApi.API25.name -> postUserModify(n)
            NetworkApi.API26.name -> postScrap(n.scrapParams)
            NetworkApi.API27.name -> getKeyword()
            NetworkApi.API28.name -> getTagId(n.arg2, n.arg3, n.arg4)
            NetworkApi.API29.name -> getSearchVOD(n.arg2, n.arg3, n.arg4)
            NetworkApi.API30.name -> getSearchHashTag(n.arg2, n.arg3)
            NetworkApi.API31.name -> getSearchUser(n.arg2, n.arg3)
            NetworkApi.API32.name -> getUserUploadVOD(n.arg2, n.arg3, n.arg4, n.arg5)
            NetworkApi.API33.name -> getUserUploadVOD(n.arg2, n.arg3, n.arg4, n.arg5)
            NetworkApi.API34.name -> getTagSearchList(n.arg2, n.arg3, n.arg4, n.arg5)
            NetworkApi.API35.name -> sendOneByOneQA(n.body)
            NetworkApi.API36.name -> getOneByOneCategory()
            NetworkApi.API37.name -> getLatelyVOD(n.arg2)
            NetworkApi.API38.name -> getFAQCategory()
            NetworkApi.API39.name -> getMyOrder(n.arg2)
            NetworkApi.API40.name -> getOnebyOneQAList(n.arg2)
            NetworkApi.API41.name -> getFAQList(n.arg2, n.arg3)
            NetworkApi.API42.name -> getReportType()
            NetworkApi.API43.name -> postReviewReport(n.body)
            NetworkApi.API44.name -> postReview(n)
            NetworkApi.API45.name -> getReviewList(n.arg2, n.arg3)
            NetworkApi.API46.name -> getPhotoReviewList(n.arg2)
            NetworkApi.API47.name -> sendRecommend(n.body)
            NetworkApi.API48.name -> makeShareLink(n.body)
            NetworkApi.API49.name -> getShareData(n.arg2)
            NetworkApi.API50.name -> deleteReviewRecommend(n.arg2, n.arg3)
            NetworkApi.API51.name -> getCookieHistory(n.arg2, n.arg3)
            NetworkApi.API52.name -> getCookieInfo(n.arg2)
            NetworkApi.API99.name -> getChatUserList(n.arg2)
            NetworkApi.API98.name -> getBroadcastInfo(n.arg2)
            NetworkApi.API97.name -> putVODShow(n.body)
            NetworkApi.API96.name -> putVODShare(n.body)
            NetworkApi.API95.name -> putVODComment(n.body)
            NetworkApi.API94.name -> getAppInfo()
            NetworkApi.API93.name -> getQnaType()
            NetworkApi.API92.name -> postQna(n.arg2, n.body)
            NetworkApi.API91.name -> getAllQna(n.arg2)
            NetworkApi.API91_1.name -> getMyQna(n.arg2)
            NetworkApi.API91_2.name -> getTypeQna(n.arg2, n.arg3)
            NetworkApi.API91_3.name -> getSecretQna(n.arg2, n.arg3)
            NetworkApi.API90.name -> getSearchProducts(n.arg2, n.arg3, n.arg4)
            NetworkApi.API89.name -> getUserProductQna()
            NetworkApi.API88.name -> getRank(n.arg2)
            NetworkApi.API87.name -> getDeleteVOD(n.arg2)
            NetworkApi.API86.name -> postVODUpload(n.arg2, n.body)
            NetworkApi.API85.name -> postPushRegist(n.body)
            NetworkApi.API84.name -> postCookieSend(n.body)
            NetworkApi.API83.name -> getLiveProduct(n.arg2)
            NetworkApi.API82.name -> getLiveCookie(n.arg2)
            NetworkApi.API81.name -> {
                if ( TextUtils.isEmpty(n.arg4) )
                    getCategoryList(n.arg2, n.arg3)
                else
                    getCategoryList(n.arg2, n.arg3, n.arg4)
            }
            NetworkApi.API80.name -> postUserJWT(n.arg2, n.arg3, n.body)
            NetworkApi.API79.name -> getPublicJWT(n.arg2, n.arg3)
            NetworkApi.API78.name -> getFollowList(n.arg2, n.arg3.toInt())
            NetworkApi.API77.name -> getFollowSearchList(n.arg2, n.arg3.toInt(), n.arg4)
            NetworkApi.API76.name -> postSignUp(n.arg2, n.body)
            NetworkApi.API75.name -> postCellphone(n.body)
            NetworkApi.API74.name -> deleteProductLink(n.arg2)
            NetworkApi.API73.name -> postProductLink(n.body)
            NetworkApi.API72.name -> postLiveStreamUpload(n.body)
            NetworkApi.API70.name -> getProductList(n.arg2.toInt(), n.arg3.toInt(), n.arg4, n.arg5, n.arg6, n.arg7, n.arg8)
            NetworkApi.API69.name -> getShopList(n.arg2)
            NetworkApi.API68.name -> getNotReviewList()
            NetworkApi.API67.name -> getReviewList()
            NetworkApi.API66.name -> deleteReview(n.arg2)
            NetworkApi.API65.name -> getAlarmList()
            NetworkApi.API64.name -> putAlarm(n.body)
            NetworkApi.API63.name -> getMyMessage(n.arg2)
            NetworkApi.API62.name -> getMoreMessage(n.arg2, n.arg3)
            NetworkApi.API61.name -> getBeforeMessage(n.arg2, n.arg3)
            NetworkApi.API60.name -> getNewMessage()
            NetworkApi.API59.name -> putMessage(n.body)
            NetworkApi.API58.name -> getBlockList()
            NetworkApi.API57.name -> postBlock(n.body)
            NetworkApi.API56.name -> getCompanyInfo()
            NetworkApi.API55.name -> getLatelyProductList(n.arg2)
            NetworkApi.API54.name -> getMessageList()
            NetworkApi.API100.name -> deleteLatelyVOD(n.arg2)
            NetworkApi.API101.name -> getPointList()
            NetworkApi.API102.name -> getSavePointList()
            NetworkApi.API103.name -> getExpirePointList()
            NetworkApi.API104.name -> postDeleteMessage(n.body)
            NetworkApi.API105.name -> getSharedVODList()
            NetworkApi.API106.name -> getShareList()
            NetworkApi.API107.name -> getSanctionList()
            NetworkApi.API108.name -> putMessageAlarm(n.body)
            NetworkApi.API109.name -> deleteLatelyProduct(n.arg2)
            NetworkApi.API110.name -> getNoticeList()
            NetworkApi.API111.name -> getVersionInfo()
            NetworkApi.API112.name -> getNicknameCheck(n.arg2)
            NetworkApi.API113.name -> getRecommendUser()
            NetworkApi.API114.name -> getPuddingHome(n.arg2, n.arg3, n.arg4, n.arg5)
            NetworkApi.API115.name -> getEventDetailImg(n.arg2)
            NetworkApi.API116.name -> getEventDetailVod(n.arg2)
            NetworkApi.API117.name -> getEventDetailPrd(n.arg2)
            NetworkApi.API118.name -> putBannerHit(n.arg2)
            NetworkApi.API119.name -> getProductStroe(n.arg2)
            NetworkApi.API120.name -> postCertIdNum(n.body)
            NetworkApi.API121.name -> postCertIdCheck(n.body)
            NetworkApi.API122.name -> deletePushToken(n.arg2, n.arg3)
            NetworkApi.API123.name -> postNoticeFAQ(n.arg2, n.arg3)
            NetworkApi.API124.name -> getSearchSchedule(n.arg2, n.arg3)
            NetworkApi.API125.name -> postScheduleAlarm(n.body)
            NetworkApi.API126.name -> postProductZzim(n.arg2, n.body)
            NetworkApi.API128.name -> deleteZzim(n.arg2)
            NetworkApi.API129.name -> getScheduleList(n.arg2)
            NetworkApi.API130.name -> getMyScheduleList()
            NetworkApi.API131.name -> getWatchList()
            NetworkApi.API132.name -> postReservationInfo(n.body)
            NetworkApi.API133.name -> deleteReservation(n.arg2)
            NetworkApi.API134.name -> postCertPwNum(n.body)
            NetworkApi.API135.name -> postCertPwCheck(n.body)
            NetworkApi.API136.name -> postScheduleBroadcast(n.body)
            NetworkApi.API137.name -> getMobionAdvertiser(n.arg2)
            NetworkApi.API138.name -> getLinkProductParsing(n.arg2)
            NetworkApi.API139.name -> getLinkProductDRC(n.arg2, n.arg3, n.arg4, n.arg5)
            NetworkApi.API140.name -> getUserNoticeList()
            NetworkApi.API141.name -> deleteUserNotice(n.arg2)
            NetworkApi.API142.name -> postChatConnect(n.body)
            NetworkApi.API143.name -> getVideoInfo(n.arg2)
            NetworkApi.API144.name -> getHotKeyword()
            NetworkApi.API145.name -> getZzimList()
            NetworkApi.API151.name -> getPurchaseProductList()
            NetworkApi.API152.name -> postFindIdDirect(n.body)
            NetworkApi.API153.name -> postSaveUser(n.body)
            NetworkApi.API154.name -> postCertJoinNum(n.body)
            NetworkApi.API155.name -> getExchangeData()
            NetworkApi.API156.name -> getMainPopup()
        }
    }

    private fun getHome(type: String, page: String, search: String) {
        mManager.service.getHomeList(type, Integer.parseInt(page), mCategoryCode, mCategoryAge, mCategoryGender, search, mUserId).enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getUserUploadVOD(user: String, type: String, order: String, page: String) {
        mManager.service.getUploadedVodList(user, type, order, page).enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getUploadList(page:String) {
        mManager.service.getUserUploadList(mUserId, page)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getReport(user: String) {
        mManager.service.getReportList(user).enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun postReport(bus: NetworkBus) {

        var path = bus.arg2
        var file = File(path)

        Logger.d("file path : $path")
        var requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file)
        var body = MultipartBody.Part.createFormData("strFileName", file.name, requestFile)

        mManager.service.setReportList(mUserId, "test1", "1", "test1", "test1", "live", "1", body)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun postReportExt(bus: NetworkBus) {

//        var path = "aa"
//        var file = File(path)
//
//        var requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file)
//        var fileBody = MultipartBody.Part.createFormData("strFileName", file.name, requestFile)
//
//        var body = MultipartBody.Builder()
//                .setType(MultipartBody.FORM)
//                .addFormDataPart("strToUserId", "test1")
//                .addFormDataPart("strReason", "1")
//                .addFormDataPart("strTitle", "test1")
//                .addFormDataPart("strContent", "test1")
//                .addFormDataPart("strMediaType", "live")
//                .addFormDataPart("strStreamKey", "1")
//                .addPart(fileBody)
//                .build()


        mManager.service.setReportListExt(mUserId, bus.body)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun postFavorStatus(key: String, body: ApiParams.FavorParams) {
        mManager.service.setFavorStatus(key, body)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getHitsCount(id: String) {
        mManager.service.getViewCount(id)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun postHitsStatus(key: String, body: ApiParams.HitsParams) {
        mManager.service.setHitsCount(key, body)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getEvent() {
        mManager.service.getEventList()
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getEventDetail(eventId: String) {
        mManager.service.getEventDetail(eventId)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun putUserSignOut() {
        mManager.service.userSignOut(mUserId)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun putPassword(userId: String, body: ApiParams.PasswdParams) {
        mManager.service.changePassword(userId, body)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun putBroadcastTitle(body: ApiParams.TitleModifyParams) {
        mManager.service.modifyBroadCastTitle(body)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun putBroadcastNotice(body: ApiParams.NoticeModifyParams) {
        mManager.service.modifyBroadCastNotice(body)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getBroadcastReply(streamKey: String) {
        mManager.service.getVODReplyList(streamKey)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun postBroadcastReply(body: ApiParams.ReplyInputParams) {
        mManager.service.setVODReplyComment(body)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun deleteBroadcastReply(idx: String) {
        mManager.service.deleteVODReplyComment(idx, mUserId)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun putBroadcastReply(body: ApiParams.ReplyModifyParams) {
        mManager.service.modifyVODReplyComment(body)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getUserInfo(userId: String) {
        mManager.service.getUserInfo(userId)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun putUserInfo(body: RequestBody) {
        mManager.service.modifyUserInfo(mUserId, body)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getScrapVOD(page:String) {
        mManager.service.getScrapVOD(mUserId, page)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun deleteScrapVOD(key: String, type: String) {
        mManager.service.deleteScrapVOD(mUserId, key, type)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun postUserModify(bus: NetworkBus) {
        mManager.service.setUserModifyList(mUserId, bus.body)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun postScrap(body: ApiParams.ScrapParams) {
        mManager.service.setScrap(body)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getKeyword() {
        mManager.service.getSearchKeyword(mUserId)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getTagId(tagId: String, order: String, page: String) {
        mManager.service.getTagId(tagId, order, mUserId, page)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getSearchVOD(keyword: String, order: String, page: String) {
        mManager.service.getSearchVOD(keyword, order, mUserId, mCategoryCode, mCategoryAge, mCategoryGender, page)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getSearchHashTag(keyword: String, page: String) {
        mManager.service.getSearchHashtag(keyword, mUserId, mCategoryCode, mCategoryAge, mCategoryGender, page)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getSearchUser(keyword: String, page: String) {
        mManager.service.getSearchUser(keyword, mCategoryCode, mCategoryAge, mCategoryGender, page)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getTagSearchList(streamKey: String, order: String, user: String, page:String) {
        mManager.service.getTagSearchList(streamKey, order, user, page)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun sendOneByOneQA(body: RequestBody) {
        mManager.service.sendOneByOneQA(body)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getOneByOneCategory() {
        mManager.service.getOneByOneCategory()
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getFAQCategory() {
        mManager.service.getFAQCategory()
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getLatelyVOD(page: String) {
        mManager.service.getLatelyVOD(mUserId, page)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getMyOrder(userId: String) {
        mManager.service.getMyOrder(userId)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getOnebyOneQAList(userId: String) {
        mManager.service.getOnebyOneQAList(userId)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getFAQList(type: String, keyword: String) {
        mManager.service.getFAQList(type, keyword)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getChatUserList(chatKey: String) {
        mManager.service.getChatUserList(chatKey)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getBroadcastInfo(streamKey: String) {
        mManager.service.getBroadcastInfo(streamKey, mUserId)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun putVODShow(body: RequestBody) {
        mManager.service.putVODShow(body)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun putVODShare(body: RequestBody) {
        mManager.service.putVODShare(body)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun putVODComment(body: RequestBody) {
        mManager.service.putVODComment(body)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getAppInfo() {
        mManager.service.getAppInfo()
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getReportType() {
        mManager.service.getReportType()
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun postReviewReport(body: RequestBody) {
        mManager.service.postReviewReport(body)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun postReview(bus: NetworkBus) {
        mManager.service.postReview(mUserId, bus.body)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getSearchProducts(keyword: String, order: String, page: String) {
        mManager.service.getSearchProducts(keyword, order, mCategoryCode, mCategoryAge, mCategoryGender, page, Utils.getUniqueID(mContext), "android")
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getQnaType() {
        mManager.service.getQnaType()
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun postQna(productId: String, body: RequestBody) {
        mManager.service.postQna(productId, body)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getAllQna(productId: String) {
        mManager.service.gettAllQna(productId)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getMyQna(productId: String) {
        mManager.service.getMyQna(productId, mUserId)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getTypeQna(productId: String, type: String) {
        mManager.service.getTypeQna(productId, type)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getSecretQna(productId: String, secret: String) {
        mManager.service.getSecretQna(productId, secret)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getUserProductQna() {
        mManager.service.getUserProductQna(mUserId)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getRank(type: String) {
        mManager.service.getRank(type)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getDeleteVOD(key: String) {
        mManager.service.getDeleteVOD(key, mUserId)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun postVODUpload(type: String, body: RequestBody) {
        mManager.service.postVODUplaod(mUserId, type, body)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun postPushRegist(body: RequestBody) {
        mManager.service.postPushRegist(body)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getReviewList(it_id: String, order: String) {
        mManager.service.getReviewList(it_id, order)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getPhotoReviewList(it_id: String) {
        mManager.service.getPhotoReviewList(it_id)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun postCookieSend(body: RequestBody) {
        mManager.service.postCookieSend(body)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getLiveProduct(streamKey: String) {
        mManager.service.getLiveProduct(streamKey, mUserId)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getLiveCookie(streamKey: String) {
        mManager.service.getLiveCookie(streamKey)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun sendRecommend(body: RequestBody) {
        mManager.service.sendRecommend(body)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getCategoryList(categoryid: String, scCode: String) {
        mManager.service.getCategoryList(categoryid, scCode)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getCategoryList(categoryid: String, scCode: String, category: String) {
        mManager.service.getCategoryList(categoryid, scCode, category)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getPublicJWT(hmac: String, timeStamp: String) {
        mManager.service.getPublicJWT(hmac, timeStamp)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun postUserJWT(hmac: String, timeStamp: String, body: RequestBody) {
        mManager.service.postUserJWT(hmac, timeStamp, body)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getFollowList(userId: String, type: Int) {
        mManager.service.getFollowList(userId, type)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getFollowSearchList(userId: String, type: Int, search: String) {
        mManager.service.getFollowSearchList(userId, type, search)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun postSignUp(userId: String, body: RequestBody) {
        mManager.service.postSignUp(userId, body)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun postCellphone(body: RequestBody) {
        mManager.service.postCellphone(body)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun deleteProductLink(linkId: String) {
        mManager.service.deleteProductLink(linkId)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun postProductLink(body: RequestBody) {
        mManager.service.postProductLink(body)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun postLiveStreamUpload(body: RequestBody) {
        mManager.service.postLiveStreamUpload(mUserId, body)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getProductList(page: Int, perPage: Int, category: String, shopKey: String, search: String, sort: String, mainSort: String) {
        mManager.service.getProductList(page, perPage, category, shopKey, search, sort, mainSort, Utils.getUniqueID(mContext), "android")
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getShopList(search: String) {
        mManager.service.getShopList(search)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getNotReviewList() {
        mManager.service.getNotReviewList(mUserId)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getReviewList() {
        mManager.service.getReviewList(mUserId)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun deleteReview(id: String) {
        mManager.service.deleteReview(mUserId, id)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun makeShareLink(body: RequestBody) {
        mManager.service.makeShareLink(body)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getShareData(url: String) {
        mManager.service.getShareData(url)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun deleteReviewRecommend(id: String, u: String) {
        mManager.service.deleteReviewRecommend("$id", "$u")
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getCookieHistory(userId: String, gf_type: String) {
        mManager.service.getCookieHistory(userId, gf_type)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getCookieInfo(userId: String) {
        mManager.service.getCookieInfo(userId)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getAlarmList() {
        mManager.service.getAlarmList(mUserId)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun putAlarm(body: RequestBody) {
        mManager.service.putAlarm(mUserId, body)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getMyMessage(partner: String) {
        mManager.service.getMyMessage(mUserId, partner)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getMoreMessage(partner: String, lastId: String) {
        mManager.service.getMoreMessage(mUserId, partner, lastId)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getBeforeMessage(partner: String, lastId: String) {
        mManager.service.getBeforeMessage(mUserId, partner, lastId)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getNewMessage() {
        mManager.service.getNewMessage(mUserId)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun putMessage(body: RequestBody) {
        mManager.service.putMessage(body)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getBlockList() {
        mManager.service.getBlockList(mUserId)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun postBlock(body: RequestBody) {
        mManager.service.postBlock(body)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getCompanyInfo() {
        mManager.service.getCompanyInfo()
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getLatelyProductList(page:String) {
        mManager.service.getLatelyProductList(mUserId, page.toInt())
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun postFollow(body: RequestBody) {
        mManager.service.postFollow(body)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getMessageList() {
        mManager.service.getMessageList(mUserId)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun deleteLatelyVOD(streamKey: String) {
        mManager.service.deleteLatelyVOD(mUserId, streamKey)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getPointList() {
        mManager.service.getPointList(mUserId)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getSavePointList() {
        mManager.service.getSavePointList(mUserId)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getExpirePointList() {
        mManager.service.getExpirePointList(mUserId)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun postDeleteMessage(body: RequestBody) {
        mManager.service.postDeleteMessage(body)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getSharedVODList() {
        mManager.service.getSharedVODList(mUserId)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getShareList() {
        mManager.service.getShareList(mUserId)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getSanctionList() {
        mManager.service.getSanctionList(mUserId)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun putMessageAlarm(body: RequestBody) {
        mManager.service.putMessageAlarm(body)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun deleteLatelyProduct(id: String) {
        mManager.service.deleteLatelyProduct(mUserId, id)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getNoticeList() {
        mManager.service.getNoticeList()
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getVersionInfo() {
        mManager.service.getVersionInfo("android")
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getNicknameCheck(nickName: String) {
        mManager.service.getNicknameCheck(nickName)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getRecommendUser() {
        mManager.service.getRecommendUser(mCategoryCode, mCategoryAge, mCategoryGender)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getPuddingHome(type: String, page: String, categoryId: String, order: String) {
        var category = categoryId
        if (category.isEmpty()) {
            category = mCategoryCode!!
        } else if (category.equals("all")) {
            category = ""
        }
        mManager.service.getMainList(type, page, category, mCategoryAge, mCategoryGender, mUserId, order)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getEventDetailImg(eventId: String) {
        mManager.service.getEventDetailImg(eventId)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getEventDetailVod(eventId: String) {
        mManager.service.getEventDetailVod(eventId)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getEventDetailPrd(eventId: String) {
        mManager.service.getEventDetailPrd(eventId)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun putBannerHit(bannerId: String) {
        mManager.service.putBannerHit(bannerId)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getProductStroe(search: String) {
        mManager.service.getProductStroe(search)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun postCertIdNum(body: RequestBody) {
        mManager.service.postCertIdNum(body)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun postCertIdCheck(body: RequestBody) {
        mManager.service.postCertIdCheck(body)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun deletePushToken(user: String, deviceId: String) {
        mManager.service.deletePushToken(user, deviceId, "android")
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun postNoticeFAQ(type: String, id: String) {
        mManager.service.postNoticeFAQ(type, id)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getSearchSchedule(keyword: String, page: String) {
        mManager.service.getSearchSchedule(keyword, page)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun postScheduleAlarm(body: RequestBody) {
        mManager.service.postScheduleAlarm(mUserId, body)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun postProductZzim(productId: String, body: RequestBody) {
        mManager.service.postProductZzim(productId, body)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun deleteZzim(id: String) {
        mManager.service.deleteZzim(id, mUserId)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getScheduleList(date: String) {
        mManager.service.getScheduleList(date)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getMyScheduleList() {
        mManager.service.getMyScheduleList(mUserId)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getWatchList() {
        mManager.service.getWatchList(mUserId)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun postReservationInfo(body: RequestBody) {
        mManager.service.postReservationInfo(mUserId, body)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun deleteReservation(streamKey: String) {
        mManager.service.deleteReservation(mUserId, streamKey)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun postCertPwNum(body: RequestBody) {
        mManager.service.postCertPwNum(body)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun postCertPwCheck(body: RequestBody) {
        mManager.service.postCertPwCheck(body)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun postScheduleBroadcast(body: RequestBody) {
        mManager.service.postScheduleBroadcast(mUserId, body)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getMobionAdvertiser(keyword: String) {
        mManager.service.getMobionAdvertiser(keyword)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getLinkProductParsing(url:String) {
        mManager.service.getLinkProductParsing(url)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getLinkProductDRC(idx:String, user:String, streamKey:String, shareUser:String) {
        mManager.service.getLinkProductDRC(idx, user, streamKey, shareUser)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getUserNoticeList() {
        mManager.service.getUserNoticeList(mUserId)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun deleteUserNotice(idx:String) {
        mManager.service.deleteUserNotice(mUserId, idx)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun postChatConnect(body:RequestBody) {
        mManager.service.postChatConnect(body)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getVideoInfo(streamKey: String) {
        mManager.service.getVideoInfo(streamKey, mUserId)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getHotKeyword() {
        mManager.service.getHotKeyword()
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getZzimList() {
        mManager.service.getZzimList(mUserId)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getPurchaseProductList() {
        mManager.service.getPurchaseProductList(mUserId)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun postFindIdDirect(body:RequestBody) {
        mManager.service.postFindIdDirect(body)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun postSaveUser(body:RequestBody) {
        mManager.service.postSaveUser(mUserId, body)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun postCertJoinNum(body: RequestBody) {
        mManager.service.postCertJoinNum(body)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getExchangeData() {
        mManager.service.getExchangeData(mUserId)
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }

    private fun getMainPopup() {
        mManager.service.getMainPopup()
                .enqueue(mManager.callback as Callback<ResponseBody>)
    }
}
