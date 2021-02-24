package com.enliple.pudding.commons.shoptree.network;

import android.content.Context;
import android.os.Build;

import com.enliple.pudding.commons.app.Utils;
import com.enliple.pudding.commons.log.Logger;
import com.enliple.pudding.commons.app.ShopTreeKey;
import com.enliple.pudding.commons.internal.AppPreferences;

import org.json.JSONObject;

import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * 푸딩 API를 ShopTreeAsyncTask에서 사용하는 이유는
 * 라이브 및 비디오 영상 재생 시 EventBus로 API 통신을 하게 되면은 영상이 버벅되는
 * 문제가 발생하여 EventBus로 API통신을 사용하지 않고 여기서 사용함.
 * (ex. 좋아요/스크랩/팔로우/젤리 구매 및 전송/닉네임 변경 기타 등등)
 */
public class ShopTreeAsyncTask extends ShopTreeCommonApi {
    private static final int MODE_GET = -1;
    private static final int MODE_POST = -2;
    private static final int MODE_PUT = -3;
    private static final int MODE_DELETE = -4;

    private static final int MODE_DETAIL = 0;
    private static final int MODE_QNA_LIST = 100;
    private static final int MODE_QNA_INSERT = 101;
    private static final int MODE_INSERT_CART = 102;
    private static final int MODE_CERTIFY_INFO = 103;
    private static final int MODE_PURCHASE_INFO = 104;
    private static final int MODE_SEND_PURCHASE = 105;
    private static final int MODE_DELIVERY_GUIDE = 106;
    private static final int MODE_GET_PAYMENT_INFO = 107;
    private static final int MODE_GET_CART_CNT = 108;
    private static final int MODE_SEND_PURCHASE_FINISH = 109;

    private OnDefaultObjectCallbackListener mOnDefaultCallbackListener = null;
    private Context context;
    private int modeIndex;

    public ShopTreeAsyncTask(Context context) {
        this.context = context;
    }

    public void getChatList(String chatUrl, OnDefaultObjectCallbackListener defaultCallbackListener) {
//        url = "https://image.puddinglive.com:8080/chat/2019/06/25/190625172237Cg56.log";
        url = chatUrl;
        mOnDefaultCallbackListener = defaultCallbackListener;

        modeIndex = MODE_GET;
        execute();
    }

    /**
     * 정산신청
     */
    public void postExchange(String userId, RequestBody requestBody, OnDefaultObjectCallbackListener defaultCallbackListener) {
        url = AppPreferences.Companion.getServerAddress(context) + "user/" + userId + "/exchange";
        mOnDefaultCallbackListener = defaultCallbackListener;
        body = requestBody;

        modeIndex = MODE_POST;
        execute();
    }

    public void getUserInfo(OnDefaultObjectCallbackListener defaultObjectCallbackListener) {
        url = AppPreferences.Companion.getServerAddress(context) + ShopTreeUrl.GET_USER_INFO + AppPreferences.Companion.getUserId(context);
        mOnDefaultCallbackListener = defaultObjectCallbackListener;

        modeIndex = MODE_GET;
        execute();
    }

    public void sendLikeInfos(String str, OnDefaultObjectCallbackListener defaultObjectCallbackListener) {
        Logger.e("sendLikeInfos called");
        url = AppPreferences.Companion.getServerAddress(context) + ShopTreeUrl.SEND_LIKES;
        mOnDefaultCallbackListener = defaultObjectCallbackListener;
        try {
            Logger.e("str :: " + str);
            body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), str);

            modeIndex = MODE_POST;
            execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void makeShareLink(String streamKey, String uploader, String shareId, String space, String vodType, OnDefaultObjectCallbackListener defaultCallbackListener ) {
        url = AppPreferences.Companion.getServerAddress(context) + ShopTreeUrl.MAKE_SHARE_LINK;
        mOnDefaultCallbackListener = defaultCallbackListener;

        param = new HashMap<>();
        param.put("streamKey", streamKey);
        param.put("uploader", uploader);
        param.put("shareId", shareId);
        param.put("space", space);
        param.put("vodType", vodType);

        modeIndex = MODE_POST;
        execute();
    }

    public void sendProductLinkView(String streamKey) {
        Logger.e("sendProductLinke called");
        url = AppPreferences.Companion.getServerAddress(context) + ShopTreeUrl.PRODUCTLINK_VIEW;
        try {
            Logger.e("StreamKey :: " + streamKey);
            Logger.e("user :: " + AppPreferences.Companion.getUserId(context));
            Logger.e("deviceId :: " + Utils.getUniqueID(context));

            JSONObject obj = new JSONObject();
            obj.put("streamKey", streamKey);
            obj.put("user", AppPreferences.Companion.getUserId(context));
            obj.put("deviceId", Utils.getUniqueID(context));
            obj.put("type", "android");
            body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), obj.toString());

            modeIndex = MODE_POST;
            execute();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void getShopRankList(OnDefaultObjectCallbackListener defaultCallbackListener) {
        url = "http://stylessum.com:8080/mobile/json//shop_rank_shoptree.json";

        mOnDefaultCallbackListener = defaultCallbackListener;

        modeIndex = MODE_GET;
        execute();
    }

    public void getProductCount(OnDefaultObjectCallbackListener defaultCallbackListener) {
        url = AppPreferences.Companion.getServerAddress(context) + ShopTreeUrl.PRODUCT_COUNT;

        mOnDefaultCallbackListener = defaultCallbackListener;

        modeIndex = MODE_POST;
        execute();
    }

    public void getDeliveryFeeGuide(OnDefaultObjectCallbackListener defaultCallbackListener) {
        url = ShopTreeUrl.DOMAIN + ShopTreeUrl.DELIVERY_GUIDE;
        Logger.e("url :: " + url);
        mOnDefaultCallbackListener = defaultCallbackListener;

        modeIndex = MODE_DELIVERY_GUIDE;
        execute();
    }

    public void sendPurchaseFinish(String orderNo, String stOrderNo, OnDefaultObjectCallbackListener defaultCallbackListener ) {
        url = AppPreferences.Companion.getServerAddress(context) + ShopTreeUrl.SEND_PURCHASE_FINISH;
        Logger.e("url :: " + url);
        mOnDefaultCallbackListener = defaultCallbackListener;

        param = new HashMap<>();
        param.put("orderNumber", orderNo);
        param.put("stOrderNumber", stOrderNo);
        modeIndex = MODE_SEND_PURCHASE_FINISH;
        execute();
    }

    public void sendPurchaseInfo(String jsonBody, String test, OnDefaultObjectCallbackListener defaultCallbackListener) {
        url = AppPreferences.Companion.getServerAddress(context) + ShopTreeUrl.SEND_PURCHASE;
        Logger.e("url :: " + url);
        mOnDefaultCallbackListener = defaultCallbackListener;

        param = new HashMap<>();
        param.put("body", jsonBody);
        param.put("test", test);
        modeIndex = MODE_SEND_PURCHASE;
        execute();
    }

    public void getPurchaseInfo(String jsonBody, OnDefaultObjectCallbackListener defaultCallbackListener) {
        url = AppPreferences.Companion.getServerAddress(context) + ShopTreeUrl.PURCHASE_INFO;
        Logger.e("url :: " + url);
        mOnDefaultCallbackListener = defaultCallbackListener;

        param = new HashMap<>();
        param.put("body", jsonBody);

        modeIndex = MODE_PURCHASE_INFO;
        execute();
    }

    public void insertCart(String jsonBody, OnDefaultObjectCallbackListener defaultCallbackListener) {
        url = AppPreferences.Companion.getServerAddress(context) + ShopTreeUrl.INSERT_CART;
        Logger.e("insertCart cartData:: " + jsonBody);
        mOnDefaultCallbackListener = defaultCallbackListener;
        param = new HashMap<>();
        param.put("cartData", jsonBody);

        modeIndex = MODE_INSERT_CART;
        execute();
    }

    public void qnaInsert(String pCode, String msg, OnDefaultObjectCallbackListener defaultCallbackListener) {
        url = ShopTreeUrl.DOMAIN + ShopTreeUrl.QNA_INSERT;

        mOnDefaultCallbackListener = defaultCallbackListener;
        param = new HashMap<>();
        param.put("method", "INS");
        param.put("product_key", pCode);
        param.put("contents", msg);
        param.put("os_ver", Build.VERSION.RELEASE);
        param.put("device_model", Build.MODEL);

        modeIndex = MODE_QNA_INSERT;
        execute();
    }

    public void getQnAList(String from, String pCode, String seqId, OnDefaultObjectCallbackListener defaultCallbackListener) {
        url = ShopTreeUrl.DOMAIN + ShopTreeUrl.QNA_LIST;

        mOnDefaultCallbackListener = defaultCallbackListener;
        param = new HashMap<String, String>();
        param.put("curr_page", from);
        param.put("product_key", pCode);
        param.put("per_page", "10");
        param.put("last_id", seqId == null ? "-1" : seqId);

        modeIndex = MODE_QNA_LIST;
        execute();
    }

    public void getDetail(String idx, OnDefaultObjectCallbackListener defaultCallbackListener) {
        url = AppPreferences.Companion.getServerAddress(context) + "products/" + idx;
        Logger.e("url :: " + url);
        mOnDefaultCallbackListener = defaultCallbackListener;

        modeIndex = MODE_DETAIL;
        execute();
    }

    /**
     * 장바구니 상품을 조회한다.
     *
     * @param defaultCallbackListener
     */
    public void getCartList(String it_id, OnDefaultObjectCallbackListener defaultCallbackListener) {
        url = AppPreferences.Companion.getServerAddress(context) + ShopTreeUrl.CART_LIST;
        Logger.e("cart list url :: " + url);
        mOnDefaultCallbackListener = defaultCallbackListener;
        param = new HashMap<String, String>();
        param.put("exclude_ct_id", it_id);

        modeIndex = MODE_GET;

        execute();
    }

    /**
     * 장바구니 상품 개수를 수정한다.
     *
     * @param ct_id
     * @param count
     * @param defaultCallbackListener
     */
    public void getCartUpdate(String ct_id, String count, OnDefaultObjectCallbackListener defaultCallbackListener) {
        url = AppPreferences.Companion.getServerAddress(context) + ShopTreeUrl.CART_UPDATE;

        mOnDefaultCallbackListener = defaultCallbackListener;

        param = new HashMap<String, String>();
        Logger.e("ct_id :: "+ ct_id);
        Logger.e("cnt :: "+ count);
        param.put("ct_id", ct_id);
        param.put("cnt", count);

        modeIndex = MODE_POST;

        execute();
    }

    /**
     * 장바구니 상품별로 삭제한다.
     *
     * @param ct_id
     * @param defaultCallbackListener
     */
    public void cartProductDelete(String ct_id, OnDefaultObjectCallbackListener defaultCallbackListener) {
        url = AppPreferences.Companion.getServerAddress(context) + ShopTreeUrl.CART_DELETE;

        mOnDefaultCallbackListener = defaultCallbackListener;

        param = new HashMap<String, String>();
        param.put("ct_id", ct_id);

        modeIndex = MODE_POST;

        execute();
    }

    /**
     * 장바구니에 선택된 상품들을 삭제한다.
     *
     * @param idx
     * @param defaultCallbackListener
     */
    public void cartProductMultiDelete(String[] idx, OnDefaultObjectCallbackListener defaultCallbackListener) {
        url = AppPreferences.Companion.getServerAddress(context) + ShopTreeUrl.CART_MULTI_DELETE;
        Logger.e("cartProductMultiDelete url :: " + url);
        mOnDefaultCallbackListener = defaultCallbackListener;

        param = new HashMap<String, String>();

        if (idx != null && idx.length > 0) {
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < idx.length; ++i) {
                if (i > 0) {
                    sb.append(",");
                }

                sb.append(idx[i]);
            }
            Logger.e("cartProductMultiDelete seq :: " + sb.toString());
            param.put("ct_id", sb.toString());
        } else {
            param.put("ct_id", "");
        }

        modeIndex = MODE_POST;

        execute();
    }

    /**
     * 구매내역 목록을 가져온다.
     *
     * @param defaultCallbackListener
     */
    public void getPurchaseHistoryList(OnDefaultObjectCallbackListener defaultCallbackListener) {
        url = AppPreferences.Companion.getServerAddress(context) + ShopTreeUrl.ORDER_LIST;

        mOnDefaultCallbackListener = defaultCallbackListener;

//        param = new HashMap<String, String>();
//        param.put("status", "주문단계");

        modeIndex = MODE_GET;

        execute();
    }

    /**
     * 구매내역/배송조회 상세보기 목록을 가져온다.
     *
     * @param orderNo
     * @param defaultCallbackListener
     */
    public void getOrderDetailList(String orderNo, OnDefaultObjectCallbackListener defaultCallbackListener) {
        Logger.e("getOrderDetailList orderNumber :: " + orderNo);
        url = AppPreferences.Companion.getServerAddress(context) + ShopTreeUrl.ORDER_DETAIL;
        Logger.e("getOrderDetailList url :: " + url);
        mOnDefaultCallbackListener = defaultCallbackListener;

        param = new HashMap<String, String>();
        param.put("orderno", orderNo);

        modeIndex = MODE_GET;

        execute();
    }

    /**
     * 상품 전체 취소를 요청한다.
     *
     * @param orderNo
     * @param msg
     * @param defaultCallbackListener
     */
    public void requestAllCancel(String orderNo, String msg, OnDefaultObjectCallbackListener defaultCallbackListener) {
        url = AppPreferences.Companion.getServerAddress(context) + ShopTreeUrl.ORDER_COMPLAIN_CANCEL;

        mOnDefaultCallbackListener = defaultCallbackListener;

        param = new HashMap<String, String>();
        param.put("orderNumber", orderNo);
        param.put("type", "order");
        param.put("status", "환불 요청");
        param.put("message", msg == null ? "" : msg);

        modeIndex = MODE_POST;

        execute();
    }

    public void orderComplainCancelForm(String orderNo, String type, OnDefaultObjectCallbackListener defaultCallbackListener) {
        url = AppPreferences.Companion.getServerAddress(context) + ShopTreeUrl.ORDER_COMPLAIN_CANCEL_FORM;

        mOnDefaultCallbackListener = defaultCallbackListener;

        param = new HashMap<String, String>();
        param.put("orderNumber", orderNo);
        param.put("type", type);

        modeIndex = MODE_POST;

        execute();
    }

    /**
     * 사용자 포인트 조회한다.
     *
     * @param from
     * @param defaultCallbackListener
     */
    public void getUserPointList(String from, OnDefaultObjectCallbackListener defaultCallbackListener) {
        url = ShopTreeUrl.DOMAIN + ShopTreeUrl.USER_POINT_LIST;

        mOnDefaultCallbackListener = defaultCallbackListener;

        param = new HashMap<String, String>();
        param.put("start", from);
        param.put("rows", "10");

        modeIndex = MODE_GET;

        execute();
    }

    /**
     * 사용자 포인트 적립예정 및 소멸예정 리스트 조회한다.
     *
     * @param from
     * @param flag
     * @param defaultCallbackListener
     */
    public void getUserPointListSchedule(String from, String flag, OnDefaultObjectCallbackListener defaultCallbackListener) {
        url = ShopTreeUrl.DOMAIN + ShopTreeUrl.USER_POINT_LIST_SCHEDULE;

        mOnDefaultCallbackListener = defaultCallbackListener;

        param = new HashMap<String, String>();
        param.put("start", from);
        param.put("rows", "10");
        param.put("flag", flag);

        modeIndex = MODE_GET;

        execute();
    }

    /**
     * 배송조회 목록을 가져온다.
     *
     * @param status
     * @param defaultCallbackListener
     */
    public void getDeliveryStatusList(String status, OnDefaultObjectCallbackListener defaultCallbackListener) {
        url = AppPreferences.Companion.getServerAddress(context) + ShopTreeUrl.DELEVERY_PRODUCT_LIST;
        Logger.e("url :: " + url);
        Logger.e("status :: " + status);
        mOnDefaultCallbackListener = defaultCallbackListener;

        param = new HashMap<String, String>();
        param.put("status", status);

        modeIndex = MODE_POST;

        execute();
    }

    /**
     * 구매확정을 요청한다.
     *
     * @param itemKey
     * @param defaultCallbackListener
     */
    public void purchaseComplete(String itemKey, OnDefaultObjectCallbackListener defaultCallbackListener) {
        url = AppPreferences.Companion.getServerAddress(context) + ShopTreeUrl.BUY_COMPLETE;
        Logger.e("purchaseComplete itemKey :: " + itemKey);
        mOnDefaultCallbackListener = defaultCallbackListener;
        param = new HashMap<String, String>();
        param.put("key", itemKey);
        param.put("status", "구매완료");
        param.put("test", "N");

        modeIndex = MODE_POST;

        execute();
    }

    /**
     * 상품 취소를 요청한다. 단건
     *
     * @param json
     * @param defaultCallbackListener
     */
    public void requestCancel(JSONObject json, OnDefaultObjectCallbackListener defaultCallbackListener) {
        url = AppPreferences.Companion.getServerAddress(context) + ShopTreeUrl.ORDER_COMPLAIN_CANCEL;

        mOnDefaultCallbackListener = defaultCallbackListener;

        param = new HashMap<>();
        param.put("complainData", json.toString());
        param.put("type", "item");

        modeIndex = MODE_POST;

        execute();
    }

    /**
     * 상품 교환을 요청한다.
     *
     * @param json
     * @param defaultCallbackListener
     */
    public void requestExchange(JSONObject json, OnDefaultObjectCallbackListener defaultCallbackListener) {
        url = ShopTreeUrl.DOMAIN + ShopTreeUrl.ORDER_COMPLAIN_EXCHANGE;

        mOnDefaultCallbackListener = defaultCallbackListener;

        param = new HashMap<>();
        param.put("complainData", json.toString());

        modeIndex = MODE_POST;

        execute();
    }

    /**
     * 상품 환불요청을 한다.
     *
     * @param json
     * @param defaultCallbackListener
     */
    public void requestRefund(JSONObject json, OnDefaultObjectCallbackListener defaultCallbackListener) {
        url = ShopTreeUrl.DOMAIN + ShopTreeUrl.ORDER_COMPLAIN_REFUND;

        mOnDefaultCallbackListener = defaultCallbackListener;

        param = new HashMap<>();
        param.put("complainData", json.toString());

        modeIndex = MODE_POST;

        execute();
    }

    /**
     * 취소/환불/교환내역을 가져온다.
     *
     * @param defaultCallbackListener
     */
    public void getCREList(OnDefaultObjectCallbackListener defaultCallbackListener) {
        url = ShopTreeUrl.DOMAIN + ShopTreeUrl.COMPLAIN_LIST;

        mOnDefaultCallbackListener = defaultCallbackListener;

        param = new HashMap<>();

        modeIndex = MODE_POST;

        execute();
    }

    /**
     * 취소/환불/교환내역 상세정보를 가져온다.
     *
     * @param idx
     * @param defaultCallbackListener
     */
    public void getCREDetail(String idx, OnDefaultObjectCallbackListener defaultCallbackListener) {
        url = ShopTreeUrl.DOMAIN + ShopTreeUrl.COMPLAIN_DETAIL;

        mOnDefaultCallbackListener = defaultCallbackListener;

        param = new HashMap<>();
        param.put("ct_id", idx);

        modeIndex = MODE_POST;

        execute();
    }

    /**
     * 장바구니 상점별로 삭제한다.
     *
     * @param scCode
     * @param defaultCallbackListener
     */
    public void cartShopDelete(String scCode, OnDefaultObjectCallbackListener defaultCallbackListener) {
        url = ShopTreeUrl.DOMAIN + ShopTreeUrl.CART_SHOP_DELETE;

        mOnDefaultCallbackListener = defaultCallbackListener;

        param = new HashMap<>();
        param.put("sc_code", scCode);

        modeIndex = MODE_GET;

        execute();
    }

    /**
     * 장바구니에 담긴 상품 개수를 가져온다.
     *
     * @param defaultCallbackListener
     */
    public void getCartCnt(OnDefaultObjectCallbackListener defaultCallbackListener) {
        url = AppPreferences.Companion.getServerAddress(context) + ShopTreeUrl.CART_COUNT;
        Logger.e("getCartCnt url :: " + url);
        mOnDefaultCallbackListener = defaultCallbackListener;

        param = new HashMap<>();

        modeIndex = MODE_GET_CART_CNT;

        execute();
    }

////////////////////////////////////////////////////////////////////////
//////////////////////////// 푸딩 API ///////////////////////////////////

    /**
     * 링크 상품 파싱
     * @param strUrl
     * @param defaultCallbackListener
     */
    public void getProductLink(String strUrl, OnDefaultObjectCallbackListener defaultCallbackListener) {
        url = AppPreferences.Companion.getServerAddress(context) + "productlink?strUrl=" + strUrl;
        mOnDefaultCallbackListener = defaultCallbackListener;

        modeIndex = MODE_GET;

        execute();
    }

    /**
     * 특정 라이브 방송 정보
     * @param streamKey
     * @param defaultCallbackListener
     */
    public void getLiveInfo(String streamKey, OnDefaultObjectCallbackListener defaultCallbackListener) {
        url = AppPreferences.Companion.getServerAddress(context) + "mui/live/" + streamKey;
        mOnDefaultCallbackListener = defaultCallbackListener;

        modeIndex = MODE_GET;

        execute();
    }

    /**
     * 라이브 채팅방 정보 호출
     */
    public void postChatConnect(RequestBody requestBody, OnDefaultObjectCallbackListener defaultCallbackListener) {
        url = AppPreferences.Companion.getServerAddress(context) + "chatconnect";
        mOnDefaultCallbackListener = defaultCallbackListener;

        body = requestBody;

        modeIndex = MODE_POST;

        execute();
    }

    /**
     * 스크랩 등록
     */
    public void postScrap(RequestBody requestBody, OnDefaultObjectCallbackListener defaultCallbackListener) {
        url = AppPreferences.Companion.getServerAddress(context) + "broadcast/scrap";
        mOnDefaultCallbackListener = defaultCallbackListener;

        body = requestBody;

        modeIndex = MODE_POST;

        execute();
    }

    /**
     * 스크랩 해제
     */
    public void deletetScrap(String streamKey, OnDefaultObjectCallbackListener defaultCallbackListener) {
        url = AppPreferences.Companion.getServerAddress(context) + "user/" + AppPreferences.Companion.getUserId(context) + "/scrap?streamKey=" + streamKey;
        mOnDefaultCallbackListener = defaultCallbackListener;

        modeIndex = MODE_DELETE;

        execute();
    }

    /**
     * 젤리 현황 갱신
     * @param streamKey
     * @param defaultCallbackListener
     */
    public void getCookieInfo(String streamKey, OnDefaultObjectCallbackListener defaultCallbackListener) {
        url = AppPreferences.Companion.getServerAddress(context) + "live/" + streamKey + "/report/cookie";
        mOnDefaultCallbackListener = defaultCallbackListener;

        modeIndex = MODE_GET;

        execute();
    }

    /**
     * 젤리 선물하기
     */
    public void postCookie(RequestBody requestBody, OnDefaultObjectCallbackListener defaultCallbackListener) {
        url = AppPreferences.Companion.getServerAddress(context) + "broadcast/gift";
        mOnDefaultCallbackListener = defaultCallbackListener;

        body = requestBody;

        modeIndex = MODE_POST;

        execute();
    }

    /**
     * 팔로우 정보 갱신
     */
    public void postFollow(RequestBody requestBody, OnDefaultObjectCallbackListener defaultCallbackListener) {
        url = AppPreferences.Companion.getServerAddress(context) + "follow";
        mOnDefaultCallbackListener = defaultCallbackListener;

        body = requestBody;

        modeIndex = MODE_POST;

        execute();
    }

    /**
     * 사용자 닉네임 수정
     */
    public void putUserNick(RequestBody requestBody, OnDefaultObjectCallbackListener defaultCallbackListener) {
        url = AppPreferences.Companion.getServerAddress(context) + "user/" + AppPreferences.Companion.getUserId(context);
        mOnDefaultCallbackListener = defaultCallbackListener;

        body = requestBody;

        modeIndex = MODE_PUT;

        execute();
    }

    /**
     * 사용자 닉네임 체크
     */
    public void getUserNickCheck(String userNick, OnDefaultObjectCallbackListener defaultCallbackListener) {
        url = AppPreferences.Companion.getServerAddress(context) + "check/nick/" + userNick;
        mOnDefaultCallbackListener = defaultCallbackListener;

        modeIndex = MODE_GET;

        execute();
    }

    /**
     * 댓글 목록을 가져온다.
     *
     * @param streamKey
     * @param defaultCallbackListener
     */
    public void getReplyList(String streamKey, OnDefaultObjectCallbackListener defaultCallbackListener) {
        url = AppPreferences.Companion.getServerAddress(context) + "broadcast/comment?streamKey=" + streamKey;
        mOnDefaultCallbackListener = defaultCallbackListener;

        modeIndex = MODE_GET;

        execute();
    }

    /**
     * 링크 상품 DRC Url을 가져온다.
     *
     * @param idx
     * @param defaultCallbackListener
     */
    public void getDRCLink(String idx, String streamKey, String share_user, String strType, OnDefaultObjectCallbackListener defaultCallbackListener) {
        String userId = "";
        if(AppPreferences.Companion.getLoginStatus(context)) {
            userId = AppPreferences.Companion.getUserId(context);
        } else {
            userId = Utils.getUniqueID(context);
        }

        url = AppPreferences.Companion.getServerAddress(context) + "productlink/url?idx=" + idx + "&user=" + userId + "&streamKey=" + streamKey + "&share_user=" + share_user + "&strType=" + strType;
        mOnDefaultCallbackListener = defaultCallbackListener;

        modeIndex = MODE_GET;

        execute();
    }

    /**
     * 편성표 시청 예약
     */
    public void postSchedule(RequestBody requestBody, OnDefaultObjectCallbackListener defaultCallbackListener) {
        String userId = "";
        if(AppPreferences.Companion.getLoginStatus(context)) {
            userId = AppPreferences.Companion.getUserId(context);
        } else {
            userId = Utils.getUniqueID(context);
        }

        url = AppPreferences.Companion.getServerAddress(context) + "formation/" + userId + "/resv";
        mOnDefaultCallbackListener = defaultCallbackListener;

        body = requestBody;

        modeIndex = MODE_POST;

        execute();
    }

    public interface OnDefaultObjectCallbackListener {
        public void onResponse(boolean result, Object obj);
    }

    public void execute() {
        switch (modeIndex) {
            // GET
            case MODE_GET:
            case MODE_DETAIL:
            case MODE_QNA_LIST:
            case MODE_CERTIFY_INFO:
            case MODE_GET_PAYMENT_INFO:
                new ShopTreeAsyncApi(url, param, body, new ShopTreeCommonApi.CallbackObjectResponse() {
                    @Override
                    public void onResponse(Object result) {
                        if (mOnDefaultCallbackListener != null)
                            mOnDefaultCallbackListener.onResponse(true, result);
                    }

                    @Override
                    public void onError(String error) {
                        try {
                            JSONObject object = new JSONObject();
                            object.put(ShopTreeKey.NETWORK_ERROR, error);
                            if (mOnDefaultCallbackListener != null)
                                mOnDefaultCallbackListener.onResponse(false, object);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).execute(this.context);
                break;
            case MODE_POST:
            case MODE_QNA_INSERT:
            case MODE_INSERT_CART:
            case MODE_PURCHASE_INFO:
            case MODE_SEND_PURCHASE:
            case MODE_SEND_PURCHASE_FINISH:
            case MODE_DELIVERY_GUIDE:
            case MODE_GET_CART_CNT:
                new ShopTreeAsyncApi(url, param, body, new ShopTreeCommonApi.CallbackObjectResponse() {
                    @Override
                    public void onResponse(Object result) {
                        if (mOnDefaultCallbackListener != null)
                            mOnDefaultCallbackListener.onResponse(true, result);
                    }

                    @Override
                    public void onError(String error) {
                        try {
                            JSONObject object = new JSONObject();
                            object.put(ShopTreeKey.NETWORK_ERROR, error);
                            if (mOnDefaultCallbackListener != null)
                                mOnDefaultCallbackListener.onResponse(false, object);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).postExecute(this.context);
                break;
            case MODE_PUT:
                new ShopTreeAsyncApi(url, param, body, new ShopTreeCommonApi.CallbackObjectResponse() {
                    @Override
                    public void onResponse(Object result) {

                        if (mOnDefaultCallbackListener != null)
                            mOnDefaultCallbackListener.onResponse(true, result);
                    }

                    @Override
                    public void onError(String error) {
                        try {
                            JSONObject object = new JSONObject();
                            object.put(ShopTreeKey.NETWORK_ERROR, error);
                            if (mOnDefaultCallbackListener != null)
                                mOnDefaultCallbackListener.onResponse(false, object);
                        } catch (Exception e) {
                        }
                    }
                }).putExecute(this.context);
                break;
            case MODE_DELETE:
                new ShopTreeAsyncApi(url, param, body,  new ShopTreeCommonApi.CallbackObjectResponse() {
                    @Override
                    public void onResponse(Object result) {

                        if (mOnDefaultCallbackListener != null)
                            mOnDefaultCallbackListener.onResponse(true, result);
                    }

                    @Override
                    public void onError(String error) {
                        try {
                            JSONObject object = new JSONObject();
                            object.put(ShopTreeKey.NETWORK_ERROR, error);
                            if (mOnDefaultCallbackListener != null)
                                mOnDefaultCallbackListener.onResponse(false, object);
                        } catch (Exception e) {
                        }
                    }
                }).deleteExecute(this.context);
                break;
        }
    }
}
