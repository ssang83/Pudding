package com.enliple.pudding.commons.network;

import com.enliple.pudding.commons.data.ApiParams;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * The interface Network interface.
 */
public interface NetworkService {
    @GET("auth/ptoken")
    Call<Data.Token> getToken(@Header("hmac") String mac, @Header("timestamp") String timeStamp);

    @POST("auth/utoken")
    Call<Data.Token> getUToken(@Header("hmac") String mac, @Header("timestamp") String timeStamp);

//    @GET("mui/home/{type}")
//    Call<Data.HomeList> getHomeList(@Header("jwt") String jwt, @Path("type") String type, @Query("page") int page,
//                                    @Query("category") String category, @Query("user") String user);

    @GET("mui/home/{type}")
    Call<ResponseBody> getHomeList(@Path("type") String type, @Query("page") int page, @Query("category") String category, @Query("age") String age, @Query("sex") String sex,
                                   @Query("search") String search, @Query("user") String user);

    @GET("vod/upload/{user}/{type}")
    Call<ResponseBody> getUploadedVodList(@Path("user") String user, @Path("type") String type, @Query("order") String order, @Query("page") String page);

    @GET("vod/upload/{user}")
    Call<ResponseBody> getUserUploadList(@Path("user") String user, @Query("page") String page);

    @GET("report/{userId}")
    Call<ResponseBody> getReportList(@Path("userId") String user);

    @Multipart
    @POST("report/{userId}")
    Call<ResponseBody> setReportList(@Path("userId") String user,
                                     @Part("strToUserId") String strToUserId,
                                     @Part("strReason") String strReason,
                                     @Part("strTitle") String strTitle,
                                     @Part("strContent") String strContent,
                                     @Part("strMediaType") String strMediaType,
                                     @Part("strStreamKey") String strStreamKey,
                                     @Part MultipartBody.Part file);

    @POST("report/{userId}")
    Call<ResponseBody> setReportListExt(@Path("userId") String user, @Body RequestBody body);

    @POST("favor/{streamKey}")
    Call<ResponseBody> setFavorStatus(@Path("streamKey") String key, @Body ApiParams.FavorParams body);

    @GET("hits/{streamKey}")
    Call<ResponseBody> getViewCount(@Path("streamKey") String id);

    @POST("hits/{streamKey}")
    Call<ResponseBody> setHitsCount(@Path("streamKey") String key, @Body ApiParams.HitsParams body);

    @GET("event/")
    Call<ResponseBody> getEventList();

    @GET("event/{eventId}")
    Call<ResponseBody> getEventDetail(@Path("eventId") String eventId);

    @PUT("leave/{userId}")
    Call<ResponseBody> userSignOut(@Path("userId") String userId);

    @PUT("password/{userId}")
    Call<ResponseBody> changePassword(@Path("userId") String userId, @Body ApiParams.PasswdParams body);

    @PUT("broadcast/title")
    Call<ResponseBody> modifyBroadCastTitle(@Body ApiParams.TitleModifyParams body);

    @PUT("broadcast/notice")
    Call<ResponseBody> modifyBroadCastNotice(@Body ApiParams.NoticeModifyParams body);

    @GET("broadcast/comment")
    Call<ResponseBody> getVODReplyList(@Query("streamKey") String streamKey);

    @POST("broadcast/comment")
    Call<ResponseBody> setVODReplyComment(@Body ApiParams.ReplyInputParams body);

    @DELETE("broadcast/comment")
    Call<ResponseBody> deleteVODReplyComment(@Query("idx") String idx, @Query("user") String user);

    @PUT("broadcast/comment")
    Call<ResponseBody> modifyVODReplyComment(@Body ApiParams.ReplyModifyParams body);

    @GET("user/{userId}")
    Call<ResponseBody> getUserInfo(@Path("userId") String user);

    @PUT("user/{userId}")
    Call<ResponseBody> modifyUserInfo(@Path("userId") String user, @Body RequestBody body);

    @GET("user/{userId}/scrap")
    Call<ResponseBody> getScrapVOD(@Path("userId") String user, @Query("page") String page);

    @DELETE("user/{userId}/scrap")
    Call<ResponseBody> deleteScrapVOD(@Path("userId") String user, @Query("streamKey") String streamKey, @Query("vod_type") String vod_type);

    @POST("usermodify/{userId}")
    Call<ResponseBody> setUserModifyList(@Path("userId") String user, @Body RequestBody body);

    @POST("broadcast/scrap")
    Call<ResponseBody> setScrap(@Body ApiParams.ScrapParams body);

    @GET("search")
    Call<ResponseBody> getSearchKeyword(@Query("user") String user);

    @GET("search/tagid")
    Call<ResponseBody> getTagId(@Query("t_idx") String tagId, @Query("order") String order, @Query("user") String user, @Query("page") String page);

    @GET("search/vod")
    Call<ResponseBody> getSearchVOD(@Query("keyword") String keyword,
                                    @Query("order") String order,
                                    @Query("user") String user,
                                    @Query("category") String category,
                                    @Query("age") String age,
                                    @Query("sex") String sex,
                                    @Query("page") String page);

    @GET("search/tagname")
    Call<ResponseBody> getSearchHashtag(@Query("keyword") String keyword,
                                        @Query("user") String user,
                                        @Query("category") String category,
                                        @Query("age") String age,
                                        @Query("sex") String sex,
                                        @Query("page") String page);

    @GET("search/user")
    Call<ResponseBody> getSearchUser(@Query("keyword") String keyword,
                                     @Query("category") String category,
                                     @Query("age") String age,
                                     @Query("sex") String sex,
                                     @Query("page") String page);

    @GET("mui/hash")
    Call<ResponseBody> getTagSearchList(@Query("streamKey") String type, @Query("order") String order, @Query("user") String user, @Query("page") String page);

    @PUT("support/myqa")
    Call<ResponseBody> sendOneByOneQA(@Body RequestBody body);

    @GET("support/category")
    Call<ResponseBody> getOneByOneCategory();

    @GET("support/faqcategory")
    Call<ResponseBody> getFAQCategory();

    @GET("user/{userId}/lately_vod")
    Call<ResponseBody> getLatelyVOD(@Path("userId") String userId, @Query("page") String page);

    @GET("support/myorder")
    Call<ResponseBody> getMyOrder(@Query("user") String user);

    @GET("support/myqa")
    Call<ResponseBody> getOnebyOneQAList(@Query("user") String user);

    @GET("support/faqlist")
    Call<ResponseBody> getFAQList(@Query("type") String type, @Query("keyword") String keyword);

    @GET("chatroom")
    Call<ResponseBody> getChatUserList(@Query("chat_key") String chatKey);

    @GET("mui/live/{streamKey}")
    Call<ResponseBody> getBroadcastInfo(@Path("streamKey") String streamKey, @Query("user") String user);

    @PUT("broadcast/user_show_yn")
    Call<ResponseBody> putVODShow(@Body RequestBody body);

    @PUT("broadcast/share_yn")
    Call<ResponseBody> putVODShare(@Body RequestBody body);

    @PUT("broadcast/comment_yn")
    Call<ResponseBody> putVODComment(@Body RequestBody body);

    @GET("config")
    Call<ResponseBody> getAppInfo();

    @GET("review/report_type")
    Call<ResponseBody> getReportType();

    @POST("review/report")
    Call<ResponseBody> postReviewReport(@Body RequestBody body);

    @POST("review/{userId}")
    Call<ResponseBody> postReview(@Path("userId") String user, @Body RequestBody body);

    @GET("search/product")
    Call<ResponseBody> getSearchProducts(@Query("keyword") String keyword,
                                         @Query("order") String order,
                                         @Query("category") String category,
                                         @Query("age") String age,
                                         @Query("sex") String sex,
                                         @Query("page") String page,
                                         @Query("deviceId") String deviceId,
                                         @Query("device_type") String device_type);

    @GET("products/qna_type")
    Call<ResponseBody> getQnaType();

    @POST("products/{productId}/qna")
    Call<ResponseBody> postQna(@Path("productId") String productId, @Body RequestBody body);

    @GET("products/{productId}/qna")
    Call<ResponseBody> gettAllQna(@Path("productId") String productId);

    @GET("products/{productId}/qna")
    Call<ResponseBody> getMyQna(@Path("productId") String productId, @Query("user") String user);

    @GET("products/{productId}/qna")
    Call<ResponseBody> getTypeQna(@Path("productId") String productId, @Query("type") String type);

    @GET("products/{productId}/qna")
    Call<ResponseBody> getSecretQna(@Path("productId") String productId, @Query("secret_out") String secret_out);

    @GET("user/{userId}/product_qna")
    Call<ResponseBody> getUserProductQna(@Path("userId") String userId);

    @GET("rank")
    Call<ResponseBody> getRank(@Query("type") String type);

    @DELETE("broadcast")
    Call<ResponseBody> getDeleteVOD(@Query("streamKey") String streamKey, @Query("user") String user);

    @POST("vod/upload/{user}/{type}")
    Call<ResponseBody> postVODUplaod(@Path("user") String user, @Path("type") String type, @Body RequestBody body);

    @POST("push/regist")
    Call<ResponseBody> postPushRegist(@Body RequestBody body);

    @GET("review/all")
    Call<ResponseBody> getReviewList(@Query("it_id") String it_id, @Query("order") String order);

    @GET("review/photo")
    Call<ResponseBody> getPhotoReviewList(@Query("it_id") String it_id);

    @POST("broadcast/gift")
    Call<ResponseBody> postCookieSend(@Body RequestBody body);

    @GET("live/{streamKey}/report/product")
    Call<ResponseBody> getLiveProduct(@Path("streamKey") String streamKey, @Query("user") String user);

    @GET("live/{streamKey}/report/cookie")
    Call<ResponseBody> getLiveCookie(@Path("streamKey") String streamKey);

    @POST("review/recommend")
    Call<ResponseBody> sendRecommend(@Body RequestBody body);

    @GET("mui/category/{categoryid}")
    Call<ResponseBody> getCategoryList(@Path("categoryid") String categoryid, @Query("sc_code") String scCode);

    @GET("mui/category/{categoryid}")
    Call<ResponseBody> getCategoryList(@Path("categoryid") String categoryid, @Query("sc_code") String scCode, @Query("category") String category);

    @Headers("Cache-control: no-cache")
    @GET("auth/ptoken")
    Call<ResponseBody> getPublicJWT(@Header("hmac") String hmac,
                                    @Header("timestamp") String timeStamp);

    @Headers({
            "Content-Type: application/json; charset=utf-8",
            "Cache-control: no-cache"
    })
    @POST("auth/utoken")
    Call<ResponseBody> postUserJWT(@Header("hmac") String hmac,
                                   @Header("timestamp") String timeStamp,
                                   @Body RequestBody body);

    @GET("follow")
    Call<ResponseBody> getFollowList(@Query("user") String user, @Query("type") int type);

    @GET("follow")
    Call<ResponseBody> getFollowSearchList(@Query("user") String user, @Query("type") int type, @Query("search") String search);

    @POST("user/{userId}")
    Call<ResponseBody> postSignUp(@Path("userId") String userId, @Body RequestBody body);

    @POST("cellphone")
    Call<ResponseBody> postCellphone(@Body RequestBody body);

    @DELETE("productlink/{linkid}")
    Call<ResponseBody> deleteProductLink(@Path("linkid") String linkId);

    @POST("productlink")
    Call<ResponseBody> postProductLink(@Body RequestBody body);

    @POST("live/stream/{user}")
    Call<ResponseBody> postLiveStreamUpload(@Path("user") String user, @Body RequestBody body);

    @Multipart
    @Headers({
            "Accept: */*",
            "cache-control: no-cache",
            "accept-encoding: gzip, deflate"
    })

    @POST("/{path}")
    Call<ResponseBody> postVODFileUpload(@Header("X-Mbus-Token") String xMBusToken,
                                         @Header("X-Mbus-Channel") String xMBusChannel,
                                         @Path(value = "path", encoded = true) String relativePath,
                                         @Part MultipartBody.Part file);

    @GET("products")
    Call<ResponseBody> getProductList(@Query("page") int page, @Query("perPage") int perPage,
                                      @Query("category") String category, @Query("shopKey") String shopKey,
                                      @Query("search") String search, @Query("order") String sort, @Query("strType") String mainSort, @Query("deviceId") String deviceId,
                                      @Query("device_type") String device_type);

    @GET("productstore")
    Call<ResponseBody> getShopList(@Query("search") String search);

    @GET("user/{userId}/not_review")
    Call<ResponseBody> getNotReviewList(@Path("userId") String userId);

    @GET("user/{userId}/review")
    Call<ResponseBody> getReviewList(@Path("userId") String userId);

    @DELETE("review")
    Call<ResponseBody> deleteReview(@Query("userId") String userId, @Query("is_id") String id);

    @POST("share/makeLink")
    Call<ResponseBody> makeShareLink(@Body RequestBody body);

    @GET("share/getData")
    Call<ResponseBody> getShareData(@Query("url") String url);

    @DELETE("review/recommend")
    Call<ResponseBody> deleteReviewRecommend(@Query("is_id") String id, @Query("user") String u);

    @GET("user/{userId}/cookie_history")
    Call<ResponseBody> getCookieHistory(@Path("userId") String userId, @Query("gf_type") String gf_type);

    @GET("user/{userId}/cookie")
    Call<ResponseBody> getCookieInfo(@Path("userId") String userId);

    @GET("user/{userId}/alarm")
    Call<ResponseBody> getAlarmList(@Path("userId") String userId);

    @PUT("user/{userId}/alarm")
    Call<ResponseBody> putAlarm(@Path("userId") String userId, @Body RequestBody body);

    @GET("message/my_message")
    Call<ResponseBody> getMyMessage(@Query("user") String userId, @Query("partner") String partner);

    @GET("message/more_message")
    Call<ResponseBody> getMoreMessage(@Query("user") String userId, @Query("partner") String partner, @Query("lastId") String lastId);

    @GET("message/before_message")
    Call<ResponseBody> getBeforeMessage(@Query("user") String userId, @Query("partner") String partner, @Query("firstId") String lastId);

    @GET("message/new_message")
    Call<ResponseBody> getNewMessage(@Query("user") String userId);

    @PUT("message/message")
    Call<ResponseBody> putMessage(@Body RequestBody body);

    @GET("block")
    Call<ResponseBody> getBlockList(@Query("user") String userId);

    @POST("block")
    Call<ResponseBody> postBlock(@Body RequestBody body);

    @GET("config/company")
    Call<ResponseBody> getCompanyInfo();

    @GET("user/{userId}/lately_product")
    Call<ResponseBody> getLatelyProductList(@Path("userId") String user, @Query("page") int page);

    @POST("follow")
    Call<ResponseBody> postFollow(@Body RequestBody body);

    @GET("message/message_list")
    Call<ResponseBody> getMessageList(@Query("user") String userId);

    @DELETE("user/{userId}/lately_vod")
    Call<ResponseBody> deleteLatelyVOD(@Path("userId") String user, @Query("streamKey") String streamKey);

    @GET("user/{userId}/point")
    Call<ResponseBody> getPointList(@Path("userId") String user);

    @GET("user/{userId}/save_point")
    Call<ResponseBody> getSavePointList(@Path("userId") String user);

    @GET("user/{userId}/expire_point")
    Call<ResponseBody> getExpirePointList(@Path("userId") String user);

    @POST("message/delete")
    Call<ResponseBody> postDeleteMessage(@Body RequestBody body);

    @GET("user/{userId}/shared_vod")
    Call<ResponseBody> getSharedVODList(@Path("userId") String user);

    @GET("user/{userId}/share")
    Call<ResponseBody> getShareList(@Path("userId") String user);

    @GET("user/{userId}/sanction")
    Call<ResponseBody> getSanctionList(@Path("userId") String user);

    @PUT("message/alarm")
    Call<ResponseBody> putMessageAlarm(@Body RequestBody body);

    @DELETE("user/{userId}/lately_product")
    Call<ResponseBody> deleteLatelyProduct(@Path("userId") String user, @Query("it_id") String id);

    @GET("support/notice")
    Call<ResponseBody> getNoticeList();

    @GET("version/{deviceType}")
    Call<ResponseBody> getVersionInfo(@Path("deviceType") String deviceType);

    @GET("check/nick/{val}")
    Call<ResponseBody> getNicknameCheck(@Path("val") String val);

    @GET("mui/recommend/user")
    Call<ResponseBody> getRecommendUser(@Query("category") String category, @Query("age") String age, @Query("sex") String sex);

    @GET("mui/main/{type}")
    Call<ResponseBody> getMainList(@Path("type") String type, @Query("page") String page, @Query("category") String category,
                                   @Query("age") String age, @Query("sex") String sex, @Query("user") String user, @Query("order") String order);

    @GET("event/{eventId}/img")
    Call<ResponseBody> getEventDetailImg(@Path("eventId") String eventId);

    @GET("event/{eventId}/vod")
    Call<ResponseBody> getEventDetailVod(@Path("eventId") String eventId);

    @GET("event/{eventId}/prd")
    Call<ResponseBody> getEventDetailPrd(@Path("eventId") String eventId);

    @PUT("banner/{bn_id}/hit")
    Call<ResponseBody> putBannerHit(@Path("bn_id") String bn_id);

    @GET("productstore")
    Call<ResponseBody> getProductStroe(@Query("search") String search);

    @POST("cert/id/num")
    Call<ResponseBody> postCertIdNum(@Body RequestBody body);

    @POST("cert/id/check")
    Call<ResponseBody> postCertIdCheck(@Body RequestBody body);

    @DELETE("push/regist")
    Call<ResponseBody> deletePushToken(@Query("user") String user, @Query("deviceId") String deviceId, @Query("deviceType") String deviceType);

    @POST("support/hit/{type}/{id}")
    Call<ResponseBody> postNoticeFAQ(@Path("type") String type, @Path("id") String id);

    @GET("search/formation")
    Call<ResponseBody> getSearchSchedule(@Query("keyword") String keyword, @Query("page") String page);

    @POST("formation/{userId}/resv")
    Call<ResponseBody> postScheduleAlarm(@Path("userId") String userId, @Body RequestBody body);

    @POST("products/{productId}/wish")
    Call<ResponseBody> postProductZzim(@Path("productId") String productId, @Body RequestBody body);

    @DELETE("products/wish")
    Call<ResponseBody> deleteZzim(@Query("it_id") String id, @Query("user") String user);

    @GET("formation")
    Call<ResponseBody> getScheduleList(@Query("date") String date);

    @GET("formation/{userId}")
    Call<ResponseBody> getMyScheduleList(@Path("userId") String userId);

    @GET("formation/{userId}/resv")
    Call<ResponseBody> getWatchList(@Path("userId") String userId);

    @POST("formation/{userId}")
    Call<ResponseBody> postReservationInfo(@Path("userId") String user, @Body RequestBody body);

    @DELETE("formation/{userId}")
    Call<ResponseBody> deleteReservation(@Path("userId") String userId, @Query("streamKey") String streamKey);

    @POST("cert/pw/num")
    Call<ResponseBody> postCertPwNum(@Body RequestBody body);

    @POST("cert/pw/check")
    Call<ResponseBody> postCertPwCheck(@Body RequestBody body);

    @POST("formation/{userId}/start")
    Call<ResponseBody> postScheduleBroadcast(@Path("userId") String userId, @Body RequestBody body);

    @GET("productlink/advertiser")
    Call<ResponseBody> getMobionAdvertiser(@Query("title") String keyword);

    @GET("productlink")
    Call<ResponseBody> getLinkProductParsing(@Query("strUrl") String url);

    @GET("productlink/url")
    Call<ResponseBody> getLinkProductDRC(@Query("idx") String idx,
                                         @Query("user") String user,
                                         @Query("streamKey") String streamKey,
                                         @Query("share_user") String shareUser);

    @GET("user/{userId}/notice")
    Call<ResponseBody> getUserNoticeList(@Path("userId") String userId);

    @GET("user/{userId}/notice")
    Call<ResponseBody> deleteUserNotice(@Path("userId") String userId, @Query("am_idx") String am_idx);

    @POST("chatconnect")
    Call<ResponseBody> postChatConnect(@Body RequestBody body);

    @GET("mui/vod/{streamKey}")
    Call<ResponseBody> getVideoInfo(@Path("streamKey") String streamKey, @Query("user") String user);

    @GET("rank/keyword")
    Call<ResponseBody> getHotKeyword();

    @GET("user/{userId}/wish")
    Call<ResponseBody> getZzimList(@Path("userId") String userId);

    @GET("user/{userId}/order")
    Call<ResponseBody> getPurchaseProductList(@Path("userId") String userId);

    @POST("cert/id/check_direct")
    Call<ResponseBody> postFindIdDirect(@Body RequestBody body);

    @POST("cellphone/{userId}")
    Call<ResponseBody> postSaveUser(@Path("userId") String userId, @Body RequestBody body);

    @POST("cert/join/num")
    Call<ResponseBody> postCertJoinNum(@Body RequestBody body);

    @GET("user/{userId}/exchange")
    Call<ResponseBody> getExchangeData(@Path("userId") String userId);

    @GET("mui/popup")
    Call<ResponseBody> getMainPopup();
}
