package com.enliple.pudding.fragment.main.pudding

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.enliple.pudding.PuddingApplication
import com.enliple.pudding.R
import com.enliple.pudding.activity.*
import com.enliple.pudding.commons.app.ImageLoad
import com.enliple.pudding.commons.app.PriceFormatter
import com.enliple.pudding.commons.app.StringUtils
import com.enliple.pudding.commons.app.Utils
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.events.OnSingleClickListener
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.*
import com.enliple.pudding.commons.network.vo.API21
import com.enliple.pudding.commons.network.vo.API56
import com.enliple.pudding.commons.network.vo.API60
import com.enliple.pudding.commons.network.vo.BaseAPI
import com.enliple.pudding.commons.shoptree.network.ShopTreeAsyncTask
import com.enliple.pudding.commons.widget.toast.AppToast
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_pudding_shopping.*
import kotlinx.android.synthetic.main.layout_my_delivery_header.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

/**
 * Created by Kim Joonsung on 2019-03-07.
 */
class PuddingShoppingFragment : androidx.fragment.app.Fragment() {

    companion object {
        private const val REQUEST_GO_CART = 50222
    }

    private var cartCnt = 0
    private var mIsVisibleToUser = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_pudding_shopping, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        EventBus.getDefault().register(this)

        buttonCart.setOnClickListener(clickListener)
        buttonNoti.setOnClickListener(clickListener)
        layoutFollower.setOnClickListener(clickListener)
        layoutFollowing.setOnClickListener(clickListener)
        layoutPudding.setOnClickListener(clickListener)
        layoutPoint.setOnClickListener(clickListener)
        buttonMessage.setOnClickListener(clickListener)
        imageViewProfile.setOnClickListener(clickListener)
        buttonPurchaseHistory.setOnClickListener(clickListener)
        btnPayCompleted.setOnClickListener(clickListener)
        btnDeliveryPending.setOnClickListener(clickListener)
        btnDelivering.setOnClickListener(clickListener)
        btnDeliveryComplete.setOnClickListener(clickListener)
        buttonCRE.setOnClickListener(clickListener)
        buttonProductViewHistory.setOnClickListener(clickListener)
        layoutCompany.setOnClickListener(clickListener)
        buttonCustomerCenter.setOnClickListener(clickListener)
        buttonNotice.setOnClickListener(clickListener)
        buttonTerms.setOnClickListener(clickListener)
        buttonPrivacy.setOnClickListener(clickListener)
        buttonQnA.setOnClickListener(clickListener)
        buttonMyReview.setOnClickListener(clickListener)
        setting.setOnClickListener(clickListener)
        buttonFavoriteProducts.setOnClickListener(clickListener)
        buttonScrap.setOnClickListener(clickListener)
        buttonRecentVOD.setOnClickListener(clickListener)
        layoutID.setOnClickListener(clickListener)
        addProduct.setOnClickListener(clickListener)
        requestPayback.setOnClickListener(clickListener)
        layoutShareVOD.setOnClickListener(clickListener)
        buttonCalc.setOnClickListener(clickListener)
        var btnWidth = getBtnWidth()
        var param = buttonPurchaseHistory.layoutParams
        param.width = btnWidth
        param.height = btnWidth
        buttonPurchaseHistory.layoutParams = param

        param = buttonCRE.layoutParams
        param.width = btnWidth
        param.height = btnWidth
        buttonCRE.layoutParams = param

        param = buttonQnA.layoutParams
        param.width = btnWidth
        param.height = btnWidth
        buttonQnA.layoutParams = param

        param = buttonMyReview.layoutParams
        param.width = btnWidth
        param.height = btnWidth
        buttonMyReview.layoutParams = param
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        Logger.i("setUserVisibleHint: $isVisibleToUser")

        mIsVisibleToUser = isVisibleToUser

        if (isVisibleToUser && context != null) {
            refresh()
        }
    }

    override fun onResume() {
        super.onResume()
        if (mIsVisibleToUser) {
            refresh()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        EventBus.getDefault().unregister(this)
    }

    private fun refresh() {
        NetworkBus(NetworkApi.API21.name, AppPreferences.getUserId(context!!)).let {
            EventBus.getDefault().post(it)
        }

        NetworkBus(NetworkApi.API60.name, AppPreferences.getUserId(context!!)).let {
            EventBus.getDefault().post(it)
        }

        if (textViewPayCompleteCount == null) return

        val task = ShopTreeAsyncTask(context!!)
        task.getProductCount { result, obj ->
            val data = obj as JSONObject
            if (data != null) {
                var subObject: JSONObject
                var payCompleteCount: String
                var pendingProductCount: String
                var deliveringCount: String
                var completeCount: String
                var wishCount: String
                var scrapCnt: String
                var productCount: String
                var vodCount: String

                try {
                    subObject = data.optJSONObject("deliveryType")
                    payCompleteCount = subObject.optString("count_pay")
                    pendingProductCount = subObject.optString("ready_pay")
                    deliveringCount = subObject.optString("delivery_pay")
                    completeCount = subObject.optString("complete_pay")
                    wishCount = subObject.optString("wish_cnt")
                    scrapCnt = subObject.optString("scrap_cnt")
                    productCount = subObject.optString("product_cnt")
                    vodCount = subObject.optString("vod_cnt")
                } catch (e: Exception) {
                    Logger.p(e)
                    return@getProductCount
                }

                Logger.e("payCompleteCount $payCompleteCount")
                Logger.e("pendingProductCount $pendingProductCount")
                Logger.e("deliveringCount $deliveringCount")
                Logger.e("completeCount $completeCount")
                Logger.e("wish_cnt $wishCount")
                Logger.e("scrap_cnt $scrapCnt")
                Logger.e("product_cnt $productCount")
                Logger.e("vod_cnt $vodCount")

                textViewPayCompleteCount.text = if (payCompleteCount != "null") "$payCompleteCount" else "0"
                textViewDeliveryPendingCount.text = if (pendingProductCount != "null") "$pendingProductCount" else "0"
                textViewDeliveringCount.text = if (deliveringCount != "null") "$deliveringCount" else "0"
                textViewDeliveryCompleteCount.text = if (completeCount != "null") "$completeCount" else "0"

                if ("0" == textViewPayCompleteCount.text) {
                    textViewPayCompleteCount.setTextColor(Color.parseColor("#bcc6d2"))
                } else {
                    textViewPayCompleteCount.setTextColor(Color.parseColor("#ff6c6c"))
                }

                if ("0" == textViewDeliveryPendingCount.text) {
                    textViewDeliveryPendingCount.setTextColor(Color.parseColor("#bcc6d2"))
                } else {
                    textViewDeliveryPendingCount.setTextColor(Color.parseColor("#ff6c6c"))
                }

                if ("0" == textViewDeliveringCount.text) {
                    textViewDeliveringCount.setTextColor(Color.parseColor("#bcc6d2"))
                } else {
                    textViewDeliveringCount.setTextColor(Color.parseColor("#ff6c6c"))
                }

                if ("0" == textViewDeliveryCompleteCount.text) {
                    textViewDeliveryCompleteCount.setTextColor(Color.parseColor("#bcc6d2"))
                } else {
                    textViewDeliveryCompleteCount.setTextColor(Color.parseColor("#192028"))
                }

/*                if (scrapCnt != "0") {
                    scrapCount.visibility = View.VISIBLE
                    scrapCount.text = "$scrapCnt"
                } else {
                    scrapCount.visibility = View.GONE
                }

                if (vodCount != "0") {
                    recentVODCount.visibility = View.VISIBLE
                    recentVODCount.text = "$vodCount"
                } else {
                    recentVODCount.visibility = View.GONE
                }

                if (productCount != "0") {
                    textViewProductHistoryCount.visibility = View.VISIBLE
                    textViewProductHistoryCount.text = productCount
                } else {
                    textViewProductHistoryCount.visibility = View.GONE
                }

                if (wishCount != "0") {
                    textViewFavoriteProductCount.visibility = View.VISIBLE
                    textViewFavoriteProductCount.text = wishCount
                } else {
                    textViewFavoriteProductCount.visibility = View.GONE
                }*/
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_GO_CART && resultCode == Activity.RESULT_OK) {
            cartCnt = data!!.getIntExtra("CART_CNT", 0)
            setCartBadgeCount(cartCnt)
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: NetworkBusResponse) {
        val key = NetworkHandler.getInstance(context!!).getKey(NetworkApi.API21.toString(), AppPreferences.getUserId(context!!)!!, "")

        if (data.arg1 == key) {
            handleNetworkResultUserInfo(data)
        } else if (data.arg1.startsWith(NetworkApi.API60.toString())) {  // 새로운 메시지 건수
            handleNetworkAPI60(data)
        } else if (data.arg1 == NetworkApi.API56.toString()) {
            setCompanyInfo()
        }
    }

    @Subscribe
    fun onMessageEvent(data: String) {
        if ("onMessageReceived" == data) {
            EventBus.getDefault().post(NetworkBus(NetworkApi.API60.name))
        }
    }

    /**
     * 사용자 정보 설정
     */
    private fun handleNetworkResultUserInfo(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            var str = DBManager.getInstance(context!!).get(data.arg1)
            var response: API21 = Gson().fromJson(str, API21::class.java)

            initData(response)

            // 로그인 사용자 정보를 저장한다.
            PuddingApplication.mLoginUserData = response
        } else {
            var errorResult: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
            Logger.e("error : $errorResult")
        }
    }

    /**
     * 새로운 메시지 개수 Response
     */
    private fun handleNetworkAPI60(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            val response: API60 = Gson().fromJson(DBManager.getInstance(context!!).get(data.arg1), API60::class.java)
            //Logger.e("message totalCount : ${response.nTotalCount}")
            if (response?.data != null && response.data.size > 0) {
                var sumCount = 0
                for (data in response.data) {
                    sumCount += data.cnt
                }
                setMessageBadgeCount(sumCount)
            } else {
                setMessageBadgeCount(0)
            }
        } else {
            var errorResult: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
            Logger.e("error : $errorResult")
        }
    }

    /**
     * 사용자 프로필을 설정한다.
     */
    private fun initData(response: API21) {
        if (TextUtils.isEmpty(response.userIMG)) {
            imageViewProfile.setBackgroundResource(R.drawable.profile_default_img)
        } else {
            ImageLoad.setImage(context!!, imageViewProfile, response.userIMG, null, ImageLoad.SCALE_CIRCLE_CROP, DiskCacheStrategy.ALL)
        }

        setNickName(response.userNickname)
        setAccount(response.userId)
        setFollowerCount(response.userFollower.toInt())
        setFollowingCount(response.userFollowing.toInt())
        setCartBadgeCount(response.cartCount.toInt())

        textViewPuddingCnt.text = StringUtils.getSnsStyleCountZeroBase(response.userCookie.toInt())
        textViewPoint.text = PriceFormatter.getInstance()!!.getFormattedValue(response.userPoint)

        if(response.userProfile.isNotEmpty()) {
            textViewMySelf.text = response.userProfile
            textViewMySelf.setTextColor(Color.parseColor("#b076d4"))
        } else {
            textViewMySelf.text = "자기 소개글이 없습니다."
            textViewMySelf.setTextColor(Color.parseColor("#8192a5"))
        }
    }

    /**
     * 팔로워 갯수 텍스트를 설정
     * @param count
     */
    private fun setFollowerCount(count: Int) {
        textViewFollowerCount.text = StringUtils.getSnsStyleCountZeroBase(count)
    }

    /**
     * 팔로잉 갯수 텍스트를 설정
     * @param count
     */
    private fun setFollowingCount(count: Int) {
        textViewFollowingCount.text = StringUtils.getSnsStyleCountZeroBase(count)
    }

    /**
     * 메세지 Badge 에 설정할 Count 를 지정 (0 개인 경우 숨김처리)
     * @param count
     */
    private fun setMessageBadgeCount(count: Int) {
        messageCount.text = count.toString()
        messageCount.visibility = if (count > 0) View.VISIBLE else View.GONE
    }

    /**
     * 장바구니 Badge 에 설정할 Count 를 지정 (0 개인 경우 숨김처리)
     * @param count
     */
    private fun setCartBadgeCount(count: Int) {
        textViewCartBadge.text = count.toString()
        textViewCartBadge.visibility = if (count > 0) View.VISIBLE else View.GONE
    }

    /**
     * 프로필 UI 상에 닉네임 텍스트를 설정
     * @param nickName
     */
    private fun setNickName(nickName: String?) {
        if (TextUtils.isEmpty(nickName)) {
            textViewNickName.visibility = View.INVISIBLE
        } else {
            textViewNickName.text = nickName
            textViewNickName.visibility = View.VISIBLE
        }
    }

    /**
     * 프로필 UI 상에 계정아이디 텍스트를 설정
     * @param id
     */
    private fun setAccount(id: String?) {
        if (TextUtils.isEmpty(id)) {
            textViewAccount.visibility = View.INVISIBLE
        } else {
            textViewAccount.text = "$id"
            textViewAccount.visibility = View.VISIBLE
        }
    }

    private fun setCompanyInfo() {
        try {
            var str = DBManager.getInstance(context!!).get(NetworkApi.API56.toString())
            if (!TextUtils.isEmpty(str)) {
                val response: API56 = Gson().fromJson(str, API56::class.java)

                textViewCompanyName.text = response.data.name
                textViewCompanyOwner.text = response.data.owner
                textViewCompanyNumber.text = response.data.company_no
                textViewCommunicationNumber.text = response.data.communication_no
                textViewCustomerCenter.text = response.data.tel
                textViewCompanyMail.text = response.data.email
                textViewCompanyAddr.text = response.data.addr
                textViewCompanyHosting.text = response.data.hosting
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val clickListener = object : OnSingleClickListener() {
        override fun onSingleClick(v: View?) {
            when (v?.id) {
                R.id.buttonRecentVOD -> {
                    startActivity(Intent(v.context, LatestViewVODActivity::class.java))
                }
                R.id.buttonScrap -> {
                    startActivity(Intent(v.context, ScrapVODActivity::class.java))
                }
                R.id.imageViewProfile, R.id.layoutID -> startActivity(Intent(v.context, ProfileEditActivity::class.java))
                R.id.buttonMessage -> startActivity(Intent(v.context, MessageActivity::class.java))
                R.id.buttonCart -> startActivityForResult(Intent(v.context, ProductCartActivity::class.java), REQUEST_GO_CART)
                R.id.buttonNoti -> startActivity(Intent(v.context, MyAlarmActivity::class.java))

                R.id.layoutFollower -> {
                    startActivity(Intent(v.context, FollowerActivity::class.java).apply {
                        putExtra(FollowerActivity.INTENT_EXTRA_KEY_IS_FOLLOWER, true)
                        putExtra(FollowerActivity.INTENT_EXTRA_KEY_USERID, AppPreferences.getUserId(v.context))
                    })
                }

                R.id.layoutFollowing -> {
                    startActivity(Intent(v.context, FollowerActivity::class.java).apply {
                        putExtra(FollowerActivity.INTENT_EXTRA_KEY_IS_FOLLOWER, false)
                        putExtra(FollowerActivity.INTENT_EXTRA_KEY_USERID, AppPreferences.getUserId(v.context))
                    })
                }

                R.id.layoutPudding -> startActivity(Intent(v.context, CookieManagementActivity::class.java))
                R.id.layoutPoint -> startActivity(Intent(v.context, PointHistoryActivity::class.java))
                R.id.buttonPurchaseHistory -> startActivity(Intent(v.context, PurchaseHistoryActivity::class.java))
                R.id.btnPayCompleted -> {
                    var intent = Intent(v.context, DeliveryStatusActivity::class.java).apply {
                        putExtra("STATUS", DeliveryStatusActivity.PAYMENT_COMPLETED)
                    }
                    startActivity(intent)
                }

                R.id.btnDeliveryPending -> {
                    var intent = Intent(v.context, DeliveryStatusActivity::class.java).apply {
                        putExtra("STATUS", DeliveryStatusActivity.PRODUCT_READY)
                    }
                    startActivity(intent)
                }

                R.id.btnDelivering -> {
                    var intent = Intent(v.context, DeliveryStatusActivity::class.java).apply {
                        putExtra("STATUS", DeliveryStatusActivity.PRODUCT_SENDING)
                    }
                    startActivity(intent)
                }

                R.id.btnDeliveryComplete -> {
                    var intent = Intent(v.context, DeliveryStatusActivity::class.java).apply {
                        putExtra("STATUS", DeliveryStatusActivity.DELIVERY_FINISHED)
                    }
                    startActivity(intent)
                }

                R.id.buttonCRE -> startActivity(Intent(v.context, CREActivity::class.java))
                R.id.buttonProductViewHistory -> startActivity(Intent(v.context, RecentProductsActivity::class.java))
                R.id.layoutCompany -> {
                    if (!buttonDromDown.isSelected) {
                        buttonDromDown.isSelected = true
                        layoutCompanyInfo.visibility = View.VISIBLE

                        NetworkBus(NetworkApi.API56.name).let {
                            EventBus.getDefault().post(it)
                        }
                    } else {
                        buttonDromDown.isSelected = false
                        layoutCompanyInfo.visibility = View.GONE
                    }
                }

                R.id.buttonCustomerCenter -> startActivity(Intent(v.context, CustomerCenterMainActivity::class.java))
                R.id.buttonNotice -> startActivity(Intent(v.context, NoticeListActivity::class.java))
                R.id.buttonTerms -> {
                    startActivity(Intent(v.context, AgreementActivity::class.java).apply {
                        putExtra(AgreementActivity.INTENT_EXTRA_KEY_MODE, AgreementActivity.INTENT_EXTRA_VALUE_TERM)
                    })
                }

                R.id.buttonPrivacy -> {
                    startActivity(Intent(v.context, AgreementActivity::class.java).apply {
                        putExtra(AgreementActivity.INTENT_EXTRA_KEY_MODE, AgreementActivity.INTENT_EXTRA_VALUE_PRIVACY_USE)
                    })
                }

                R.id.buttonQnA -> startActivity(Intent(v.context, MyProductQnaActivity::class.java))
                R.id.buttonMyReview -> startActivity(Intent(v.context, MyReviewActivity::class.java))
                R.id.setting -> startActivity(Intent(v.context, SettingsActivity::class.java))
                R.id.buttonFavoriteProducts -> startActivity(Intent(v.context, ProductZzimActivity::class.java))
                R.id.layoutShareVOD -> startActivity(Intent(v.context, SharingVODActivity::class.java))

                R.id.addProduct -> {
                    var browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://shop-tree.com/"))
                    startActivity(browserIntent)
                }
                R.id.requestPayback -> {
                    var browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://puddinglive.com/bbs/login.php?url=https://puddinglive.com/shop/mypage/my_adjustment.php"))
                    startActivity(browserIntent)
                }
                R.id.buttonCalc -> {
//                    var browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://puddinglive.com/bbs/login.php?url=https://puddinglive.com/shop/mypage/my_adjustment.php"))
//                    startActivity(browserIntent)
                    var it = Intent(context!!, CalculateRequestActivity::class.java)
                    startActivity(it)
                }
            }
        }
    }

    private fun getBtnWidth() : Int {
        var screenWidth = AppPreferences.getScreenWidth(context!!)
        var viewWidth = screenWidth - Utils.ConvertDpToPx(context!!, 72)
        var eachBtnWidth = viewWidth / 4
        return eachBtnWidth;
    }
}