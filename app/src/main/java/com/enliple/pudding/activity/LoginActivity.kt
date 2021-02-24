package com.enliple.pudding.activity

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.enliple.pudding.AbsBaseActivity
import com.enliple.pudding.PuddingApplication
import com.enliple.pudding.R
import com.enliple.pudding.adapter.login.KakaoSessionCallBack
import com.enliple.pudding.bus.SoftKeyboardBus
import com.enliple.pudding.commons.app.BuildHashUtils
import com.enliple.pudding.commons.app.NetworkStatusUtils
import com.enliple.pudding.commons.app.SoftKeyboardUtils
import com.enliple.pudding.commons.app.Utils
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.db.VideoDBManager
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.NetworkApi
import com.enliple.pudding.commons.network.NetworkBus
import com.enliple.pudding.commons.network.NetworkBusResponse
import com.enliple.pudding.commons.network.NetworkHandler
import com.enliple.pudding.commons.network.vo.API80
import com.enliple.pudding.commons.ui_compat.AsteriskPasswordTransformationMethod
import com.enliple.pudding.commons.widget.toast.AppToast
import com.enliple.pudding.keyboard.KeyboardHeightProvider
import com.facebook.*
import com.facebook.appevents.AppEventsLogger
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceId
import com.google.gson.Gson
import com.kakao.auth.AuthType
import com.kakao.auth.ISessionCallback
import com.kakao.auth.Session
import com.kakao.network.ErrorResult
import com.kakao.usermgmt.UserManagement
import com.kakao.usermgmt.callback.LogoutResponseCallback
import com.kakao.usermgmt.callback.MeResponseCallback
import com.kakao.usermgmt.callback.MeV2ResponseCallback
import com.kakao.usermgmt.callback.UnLinkResponseCallback
import com.kakao.usermgmt.response.MeV2Response
import com.kakao.usermgmt.response.model.UserProfile
import com.kakao.util.exception.KakaoException
import kotlinx.android.synthetic.main.activity_login.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

/**
 * ID / Password 형태 로그인 Activity
 * @author hkcha
 * @since 2018.08.02
 */
class LoginActivity : AbsBaseActivity() {

    companion object {
        const val INTENT_EXTRA_KEY_SPLASH_MODE = "called_splash"

        private const val TAG = "LoginActivity"
        private const val ACTIVITY_REQUEST_CODE_JOIN = 0x3284
        private const val GOOGLE_SIGN_IN = 0x3285
    }

    private var currentFocusEditText: View? = null
    private var calledSplash = false
    private var isRequestLogin = false
    private var checkedChangeLock = false

    private var errorDividerResId = -1
    private var alertDialog: Dialog? = null

    private var timer: Timer? = null
    private var mPressFirstBackKey = false

    private lateinit var callbackManager: CallbackManager
    private lateinit var kakaoSessionCallBack: SessionCallback
    private lateinit var mGoogleApiClient: GoogleApiClient
    //private lateinit var mAuth: FirebaseAuth

    private var decorView: View? = null
    private var uiOption: Int? = -1
    private lateinit var mKeyboardHeightProvider: KeyboardHeightProvider
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var hash = BuildHashUtils.getKeyHash(this@LoginActivity, "com.enliple.pudding")
        Logger.e("hash :: " + hash)
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this)
//        kakaoLogout()

//        decorView = window.decorView
//        uiOption = window.decorView.systemUiVisibility
//        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH )
//            uiOption = uiOption!! or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN )
//            uiOption = uiOption!! or View.SYSTEM_UI_FLAG_FULLSCREEN
//        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT )
//            uiOption = uiOption!! or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

//        decorView!!.systemUiVisibility = uiOption!!
//
//        window.decorView.apply {
//            // Hide both the navigation bar and the status bar.
//            // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
//            // a general rule, you should design your app to hide the status bar whenever you
//            // hide the navigation bar.
//            systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
//        }

        EventBus.getDefault().register(this)

        calledSplash = intent.getBooleanExtra(INTENT_EXTRA_KEY_SPLASH_MODE, false)
        setContentView(R.layout.activity_login)


        initGoogle()
        unlink()
//        initKakao()
        initFacebook()

        val options = RequestOptions()
        options.centerCrop()
        Glide.with(this@LoginActivity).setDefaultRequestOptions(options).load(R.drawable.back_login).into(loginBg)

        editTextPassword.setOnEditorActionListener(editorActionListener)
        editTextPassword.transformationMethod = AsteriskPasswordTransformationMethod()

        checkBoxAutoLogin.setOnCheckedChangeListener(checkedChangeListener)
        checkBoxSaveId.setOnCheckedChangeListener(checkedChangeListener)
        buttonLogin.setOnClickListener(clickListener)
        textViewFindAccount.setOnClickListener(clickListener)
        textViewFindPassword.setOnClickListener(clickListener)
        textViewJoin.setOnClickListener(clickListener)

        login_kakao_real.setOnClickListener(clickListener)
        facebook.setOnClickListener(clickListener)
        google.setOnClickListener(clickListener)
        insta.setOnClickListener(clickListener)
        naver.setOnClickListener(clickListener)

        when(AppPreferences.getUserLoginType(this)) {
            AppPreferences.LOGIN_TYPE_ALL -> {
                checkBoxSaveId.isChecked = true
                checkBoxAutoLogin.isChecked = true

                editTextAccount.setText(AppPreferences.getUserId(this))
            }

            AppPreferences.LOGIN_TYPE_AUTO -> {
                checkBoxAutoLogin.isChecked = true
                checkBoxSaveId.isChecked = false

                editTextAccount.setText("")
            }

            AppPreferences.LOGIN_TYPE_SAVE_ID -> {
                checkBoxAutoLogin.isChecked = false
                checkBoxSaveId.isChecked = true

                editTextAccount.setText(AppPreferences.getUserId(this))
            }

            else -> {
                // 로그아웃 한 경우 Preference가 삭제되기 때문에 글로벌 변수로 로그인 타입 체크
                when(PuddingApplication.mLoginData?.userLoginType) {
                    AppPreferences.LOGIN_TYPE_ALL -> {
                        checkBoxSaveId.isChecked = true
                        checkBoxAutoLogin.isChecked = true

                        editTextAccount.setText(PuddingApplication.mLoginData?.userLoginId)
                    }

                    AppPreferences.LOGIN_TYPE_AUTO -> {
                        checkBoxAutoLogin.isChecked = true
                        checkBoxSaveId.isChecked = false

                        editTextAccount.setText("")
                    }

                    AppPreferences.LOGIN_TYPE_SAVE_ID -> {
                        checkBoxAutoLogin.isChecked = false
                        checkBoxSaveId.isChecked = true

                        editTextAccount.setText(PuddingApplication.mLoginData?.userLoginId)
                    }

                    else -> {
                        checkBoxAutoLogin.isChecked = true
                        checkBoxSaveId.isChecked = true
                    }
                }
            }
        }

        emptyTouch.setOnTouchListener(object:View.OnTouchListener{
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                if(event?.action == MotionEvent.ACTION_DOWN) {
                    if(editTextAccount.isFocused) {
                        SoftKeyboardUtils.hideKeyboard(editTextAccount)
                    }

                    if(editTextPassword.isFocused) {
                        SoftKeyboardUtils.hideKeyboard(editTextPassword)
                    }
                }

                return false
            }
        })

        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener(this) { instanceIdResult ->
            val newToken = instanceIdResult.token
            Logger.e("newToken", newToken)
            AppPreferences.setPushToken(this, newToken)
        }

        mKeyboardHeightProvider = KeyboardHeightProvider(this)
        Handler().postDelayed(Runnable { mKeyboardHeightProvider.start() }, 1000)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
//        if( hasFocus ) {
////            decorView!!.systemUiVisibility = uiOption!!
//            val options = RequestOptions()
//            options.centerCrop()
//            Glide.with(this@LoginActivity).setDefaultRequestOptions(options).load(R.drawable.back_login).into(loginBg)
//        }
    }

    override fun onBackPressed() {
        if (!calledSplash) {
            setResult(Activity.RESULT_CANCELED)
            finish()
        } else if (!mPressFirstBackKey) {
            showSecondBackKey()
        } else {
            PuddingApplication.getApplication()?.finishWithProcessKill()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)

        if (mKeyboardHeightProvider != null) {
            mKeyboardHeightProvider.close()
        }
        try {
            if ( kakaoSessionCallBack != null )
                Session.getCurrentSession().removeCallback(kakaoSessionCallBack)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //TODO Android 9.0 에서 crash 발생... 다시 만들어야 한다.
//    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
//        if (ev != null) {
//            val action = ev.action
//
//            // 현재 포커스 된 영역 내 터치가 발생하였는지 검사
//            val eventX = ev.rawX
//            val eventY = ev.rawY
//            val locationOnScreen = IntArray(2)
//
//            val currentFocus = currentFocus
//            if (currentFocus != null) {
//                currentFocus.getLocationOnScreen(locationOnScreen)
//                val realRight = locationOnScreen[0] + currentFocus.width
//                val realBottom = locationOnScreen[1] + currentFocus.height
//
//                val isFocusedTouch = (eventX >= locationOnScreen[0]
//                        && eventX <= realRight
//                        && eventY >= locationOnScreen[1]
//                        && eventY <= realBottom)
//
//                // 포커스 영역 이외의 터치가 발생한 경우 OutSideTouchEvent 로 간주
//                if ((action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_MOVE)
//                        && !isFocusedTouch
//                        && !currentFocus.javaClass.name.startsWith("android.webKit.")) {
//
//                    if (currentFocus is AppCompatEditText || currentFocus is EditText) {
//                        // 기존의 포커스를 해제
//                        currentFocus.clearFocus()
//                        getCurrentFocus().post {
//                            // 변경된 포지션이 EditText 계열이 아닌경우 SoftKeyboardBus 를 닫음
//                            if (getCurrentFocus() !is EditText && getCurrentFocus() !is AppCompatEditText) {
//                                SoftKeyboardUtils.hideKeyboard(currentFocus)
//                            }
//                        }
//
//                        return true
//                    }
//                }
//            }
//        }
//
//        return super.dispatchTouchEvent(ev)
//    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(bus: SoftKeyboardBus) {
//        decorView!!.systemUiVisibility = uiOption!!
//        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
//                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
//                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
//                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
//                View.SYSTEM_UI_FLAG_FULLSCREEN or
//                View.SYSTEM_UI_FLAG_LOW_PROFILE or
//                View.SYSTEM_UI_FLAG_IMMERSIVE
//        val options = RequestOptions()
//        options.centerCrop()
//        Glide.with(this@LoginActivity).setDefaultRequestOptions(options).load(R.drawable.back_login).into(loginBg)
        if ( bus.height > 100 ) {
            empty.visibility = View.VISIBLE
            emptyTouch.visibility = View.VISIBLE
        } else {
            empty.visibility = View.GONE
            emptyTouch.visibility = View.GONE
//            window.decorView.apply {
//                // Hide both the navigation bar and the status bar.
//                // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
//                // a general rule, you should design your app to hide the status bar whenever you
//                // hide the navigation bar.
//                systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
//            }

        }
    }

    override fun onNetworkStatusChanged(status: NetworkStatusUtils.NetworkStatus) {}

    override fun onDozeModeStateChanged(dozeEnable: Boolean) {}

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == ACTIVITY_REQUEST_CODE_JOIN && resultCode == RESULT_OK) {
            return
        } else if (requestCode == GOOGLE_SIGN_IN ) {
            if ( resultCode == RESULT_OK ) {
                val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)

                if (result != null) {
                    if (result.isSuccess) {                      // 로그인이 성공했을 경우
                        val acct = result.signInAccount
                        val name = acct?.displayName
                        val email = acct?.email
                        val userId = acct?.id
                        val tokenKey = acct?.idToken

                        mGoogleApiClient.disconnect()

                        Logger.e("name : $name, email : $email, userId : $userId, tokenKey : $tokenKey")
                    } else { // 로그인이 실패했을 경우
                        Logger.e("login fail cause : ${result.status.statusMessage}")
                    }
                }
            } else {
                Logger.e("google login fail")
            }
        } else {
            if (requestCode == ACTIVITY_REQUEST_CODE_JOIN) {
                super.onActivityResult(requestCode, resultCode, data)
            } else if ( Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data) ) {
                Logger.e("callback called kakao")
                return
            } else {
                Logger.e("facebook onActivityResult")
                if ( callbackManager != null )
                    callbackManager?.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: NetworkBusResponse) {
        val api80 = NetworkHandler.getInstance(this@LoginActivity).getKey(NetworkApi.API80.toString(), "", "")
        when (data.arg1) {
            api80 -> getUserJWTToken(data)
        }
    }

    fun KakaoRequestMe() {
        UserManagement.getInstance().requestMe(object : MeResponseCallback() {
            override fun onSuccess(result: UserProfile?) {
                Logger.e("profile : " + result?.toString())
                Logger.e("profile id : " + result?.getId())
            }

            override fun onFailure(errorResult: ErrorResult?) {
                val errCode = errorResult?.errorCode
                Logger.e("onFailure errorResult :: " + errorResult?.errorCode + " , message :: " + errorResult?.errorMessage)
                val clientErrCode = -777

                if (errCode == clientErrCode) {
                    AppToast(this@LoginActivity).showToastMessage("카카오톡 서버의 네트워크가 불안정합니다. 잠시 후 다시 시도해주세요.",
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_BOTTOM)
                } else {
                    AppToast(this@LoginActivity).showToastMessage("알 수 없는 오류로 카카오로그인 실패",
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_BOTTOM)
                }
            }

            override fun onSessionClosed(errorResult: ErrorResult?) {
                Logger.d("onSessionClosed errorResult :: " + errorResult?.errorCode + " , message :: " + errorResult?.errorMessage)
            }

            override fun onNotSignedUp() {
                // 자동가입이 아닐경우 동의창
                Logger.e("onNotSignedUp")
            }
        })
    }

    private fun getUserJWTToken(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            val str = DBManager.getInstance(this@LoginActivity).get(data.arg1)
            val response: API80 = Gson().fromJson(str, API80::class.java)

            isRequestLogin = false
            var token: String = response!!.JWT
            Logger.e("login success: jwttoken:: $token")
            AppPreferences.setJWT(this@LoginActivity, response.JWT)
            AppPreferences.setLoginStatus(this@LoginActivity, true)
            AppPreferences.setUserAccountType(this@LoginActivity, AppPreferences.USER_ACCOUNT_TYPE_ACCOUNT)
            AppPreferences.setUserId(this@LoginActivity, editTextAccount.text.toString())

            if(checkBoxAutoLogin.isChecked && checkBoxSaveId.isChecked) {
                AppPreferences.setUserLoginType(this@LoginActivity, AppPreferences.LOGIN_TYPE_ALL)
                AppPreferences.setUserPw(this@LoginActivity, editTextPassword.text.toString())
            } else if(checkBoxAutoLogin.isChecked) {
                AppPreferences.setUserLoginType(this@LoginActivity, AppPreferences.LOGIN_TYPE_AUTO)
                AppPreferences.setUserPw(this@LoginActivity, editTextPassword.text.toString())
            } else if(checkBoxSaveId.isChecked) {
                AppPreferences.setUserLoginType(this@LoginActivity, AppPreferences.LOGIN_TYPE_SAVE_ID)
            } else {
                AppPreferences.setUserLoginType(this@LoginActivity, AppPreferences.LOGIN_TYPE_NORMAL)
            }

            editTextAccount.setText("")
            editTextPassword.setText("")

            if (calledSplash) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                setResult(RESULT_OK)
                finish()
            }

            // 로그인 성공 한 경우 push token 등록
            registerPushToken()

            // video 정보 갱신
            //EventBus.getDefault().post(NetworkBus("refresh_all_video"))

            Logger.e("delete all video db")
            VideoDBManager.getInstance(this).deleteAll()

            EventBus.getDefault().post(NetworkBus(NetworkApi.API21.name, AppPreferences.getUserId(this)))
        } else {
            var msg = getString(R.string.error_login)
            if ("401" == data.arg3 && data.arg4.contains("탈퇴한")) {
                msg = "탈퇴한 아이디 입니다.\n다시 가입해 주세요."
            }

            isRequestLogin = false
            alertDialog?.dismiss()
            alertDialog = null
            alertDialog = AlertDialog.Builder(this@LoginActivity)
                    .setMessage(msg)
                    .setCancelable(true)
                    .setPositiveButton(android.R.string.ok, null)
                    .create()
                    .apply { show() }
        }
    }

    private fun handleIdPwLogin() {
        Logger.d("handle Login")
        var id = editTextAccount.text?.toString() ?: ""
        var pw = editTextPassword.text?.toString() ?: ""

        if (TextUtils.isEmpty(id)) {
            isRequestLogin = false
            AppToast(this@LoginActivity).showToastMessage(R.string.msg_error_id_not_input,
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM)
        } else if (TextUtils.isEmpty(pw)) {
            isRequestLogin = false
            AppToast(this@LoginActivity).showToastMessage(R.string.msg_error_pw_not_input,
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM)
        }
//        // 아이디는 길이제한만 둔다.
//        else if ( !AccountPolicy.IsIdMatched(id) ) {
//            isRequestLogin = false
//            AppToast(this@LoginActivity).showToastMessage(R.string.msg_id_rule,
//                    AppToast.DURATION_MILLISECONDS_DEFAULT,
//                    AppToast.GRAVITY_BOTTOM)
//        }
//        // 비밀번호는 길이제한만 둔다.
//        else if ( !AccountPolicy.IsPasswordMatched(pw) ) {
//            isRequestLogin = false
//            AppToast(this@LoginActivity).showToastMessage(R.string.msg_password_rule,
//                    AppToast.DURATION_MILLISECONDS_DEFAULT,
//                    AppToast.GRAVITY_BOTTOM)
//        }
        else {
            requestIdPwLogin()
        }
    }

    /**
     * 아이디 패스워드를 이용한 로그인 요청
     */
    private fun requestIdPwLogin() {
        Logger.d("requestIdPwLogin")

        var timeStamp = SimpleDateFormat("yyyyMMdd").format(Date(System.currentTimeMillis()))

        var requestObj = JSONObject()
        requestObj.put("id", editTextAccount.text.toString())
        requestObj.put("pw", editTextPassword.text.toString())
        requestObj.put("timestamp", timeStamp)

        var ha: String = AppPreferences.generateUserHMAC(requestObj.toString())
        Logger.e("timeStamp :: $timeStamp")
        Logger.e("ha :: $ha")
        Logger.e("requestObj :: $requestObj")

        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), requestObj.toString())
        EventBus.getDefault().post(NetworkBus(NetworkApi.API80.name, ha, timeStamp, body))
    }

    /**
     * SNS 계정을 이용한 로그인(회원가입) 시도
     */
    private fun requestSocialLogin(loginType: Int, id: String, nickName: String, email: String) {
        // TODO : 소셜 로그인 API 가 나오면 처리
    }

    /**
     * '뒤로가기' 버튼 2회 연속 입력을 통한 종료를 사용자에게 안내하고 처리
     */
    private fun showSecondBackKey() {
        AppToast(this).showToastMessage(R.string.msg_exit_program_double_back,
                AppToast.DURATION_MILLISECONDS_DEFAULT,
                AppToast.GRAVITY_BOTTOM)

        mPressFirstBackKey = true
        val cancelTask: TimerTask = object : TimerTask() {
            override fun run() {
                timer?.cancel()
                timer = null
                mPressFirstBackKey = false
            }
        }

        timer?.cancel()
        timer = null

        timer = Timer()
        timer!!.schedule(cancelTask, AppPreferences.DOUBLE_BACK_PRESS_EXITING_TIME_LIMIT)
    }

    private fun registerPushToken() {
        var token = AppPreferences.getPushToken(this@LoginActivity)
        Logger.i("registerPushToken getPushToken:$token")

        if (!TextUtils.isEmpty(token)) {
            var uuid = Utils.getUniqueID(this@LoginActivity)
            Logger.i("registerPushToken getUniqueID:$uuid")

            var body = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("token", token)
                    .addFormDataPart("deviceId", uuid)
                    .addFormDataPart("user", AppPreferences.getUserId(this@LoginActivity)!!)
                    .addFormDataPart("deviceType", "android")
                    .build()

            EventBus.getDefault().post(NetworkBus(NetworkApi.API85.name, body))
        } else {
            Logger.e("registerPushToken token null error")
        }
    }

    /**
     * 페이스북 로그인 초기화
     */
    private fun initFacebook() {
        facebookLogout()
        callbackManager = CallbackManager.Factory.create()

        LoginManager.getInstance().registerCallback(callbackManager,
                object : FacebookCallback<LoginResult> {
                    override fun onSuccess(result: LoginResult) {
                        Logger.e("Success : ${result.accessToken}")
//                        Logger.e("Success : ${Profile.getCurrentProfile().id}")
//                        Logger.e("Success : ${Profile.getCurrentProfile().name}")
//                        Logger.e("Success : ${Profile.getCurrentProfile().getProfilePictureUri(200, 200)}")

                        requestUserProfile(result)
                    }

                    override fun onCancel() {
                        AppToast(this@LoginActivity).showToastMessage("onCancel",
                                AppToast.DURATION_MILLISECONDS_DEFAULT,
                                AppToast.GRAVITY_BOTTOM)
                    }

                    override fun onError(error: FacebookException?) {
                        AppToast(this@LoginActivity).showToastMessage("onError",
                                AppToast.DURATION_MILLISECONDS_DEFAULT,
                                AppToast.GRAVITY_BOTTOM)
                    }
                })
    }

    /**
     * 카카오 로그인 초기화
     */
    private fun initKakao() {
        kakaoSessionCallBack = SessionCallback()
        Session.getCurrentSession().addCallback(kakaoSessionCallBack)
        Session.getCurrentSession().checkAndImplicitOpen()
        requestMe()
    }

    private fun kakaoLogout() {
        UserManagement.getInstance().requestLogout(object: LogoutResponseCallback() {
            override fun onCompleteLogout() {
                Logger.e("onCompleteLogout")
            }

        })
    }

    private fun facebookLogout() {
    if (AccessToken.getCurrentAccessToken() == null) {
        return // already logged out
    }

    GraphRequest(AccessToken.getCurrentAccessToken(), "/me/permissions/", null, HttpMethod.DELETE, object: GraphRequest.Callback {
        override fun onCompleted( graphResponse: GraphResponse ) {
            Logger.e("logout called")
            LoginManager.getInstance().logOut()

        }
    }).executeAsync()
}

    private fun unlink() {
        UserManagement.getInstance().requestUnlink(object: UnLinkResponseCallback(){
            override fun onSuccess(result: Long?) {
                Logger.e("unlink success")
                initKakao()
            }

            override fun onSessionClosed(errorResult: ErrorResult?) {
                Logger.e("unlink onSessionClosed")
            }

            override fun onNotSignedUp() {
                Logger.e("unlink onNotSignedUp")
            }

        })
    }

    fun requestMe() {
        var keys = ArrayList<String>()
        keys.add("properties.nickname")
        keys.add("properties.profile_image")
        keys.add("properties.thumbnail_image")
        keys.add("kakao_account.email")
        keys.add("kakao_account.age_range")
        keys.add("kakao_account.birthday")
        keys.add("kakao_account.gender")
        //유저의 정보를 받아오는 함수
        UserManagement.getInstance().me(keys, object: MeV2ResponseCallback() {
            override fun onSuccess(result: MeV2Response?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                Logger.e("result.value :: " + result.toString())
            }

            override fun onFailure( errorResult: ErrorResult) {
                Logger.e("requestMe onFailure errorCode :: ${errorResult.errorCode} and errorMessage :: ${errorResult.errorMessage}")
//                super.onFailure(errorResult);
            }
            override fun onSessionClosed( errorResult: ErrorResult) {
                Logger.e("requestMe onSessionClosed errorCode :: ${errorResult.errorCode} and errorMessage :: ${errorResult.errorMessage}")
            }
        });
    }


    /**
     * 구글 로그인 초기화
     * 클라이언트 ID
     * 611546471197-ah0lee4cfqqrgn85snisrr9o1scdlsb9.apps.googleusercontent.com     // Android
     * 611546471197-4deoamtlchafm8qk9hpu2pfs0e12gjgj.apps.googleusercontent.com     // 웹어플리케이션
     */
    private fun initGoogle() {
        FirebaseAuth.getInstance().signOut()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        mGoogleApiClient = GoogleApiClient.Builder(this@LoginActivity)
                .enableAutoManage(this@LoginActivity,
                        object : GoogleApiClient.OnConnectionFailedListener {
                            override fun onConnectionFailed(connectResult: ConnectionResult) {
                                Logger.e("onConnectionFailed")
                            }
                        })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build()

    }

    /**
     * 구글 로그인
     */
    private fun loginGoogle() {
        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN)
    }

    /**
     * 사용자가 정상적으로 로그인 한 후에 Firebase사용자 인증 정보로 교환한다.
     */
    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Logger.e("firebaseAuthWithGoogle : ${acct.id}")

//        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
//        mAuth.signInWithCredential(credential)
//                .addOnCompleteListener(this@LoginActivity,
//                        object : OnCompleteListener<AuthResult> {
//                            override fun onComplete(task: Task<AuthResult>) {
//                                Logger.e("signInWithCredential : onComplete : ${task.isSuccessful}")
//
//                                // If sign in fails, display a message to the user. If sign in succeeds
//                                // the auth state listener will be notified and logic to handle the
//                                // signed in user can be handled in the listener.
//                                if (!task.isSuccessful) {
//                                    Logger.e("signInWithCredential : ${task.exception}")
//
//                                    AppToast(this@LoginActivity).showToastMessage("Authentication failed",
//                                            AppToast.DURATION_MILLISECONDS_DEFAULT,
//                                            AppToast.GRAVITY_BOTTOM)
//                                }
//                            }
//                        })
    }

    private fun requestUserProfile(loginResult: LoginResult) {
        val request = GraphRequest.newMeRequest(
                loginResult.accessToken,
                object : GraphRequest.GraphJSONObjectCallback {
                    override fun onCompleted(`object`: JSONObject?, response: GraphResponse?) {
                        try {
                            Logger.e("response?.jsonObject? :: " + response?.jsonObject?.toString())
                            val email = response?.jsonObject?.getString("email").toString()
                            Logger.e("Login Email : $email")
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                })

        val parameters = Bundle()
        parameters.putString("fields", "id,name,birthday,gender,email")
        request.parameters = parameters
        request.executeAsync()
    }

    private val checkedChangeListener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->

        when (buttonView?.id) {
            R.id.checkBoxSaveId -> {
                if(isChecked) {
                    checkBoxSaveId.isChecked = true
                } else {
                    checkBoxSaveId.isChecked = false
                }
            }

            R.id.checkBoxAutoLogin -> {
                if(isChecked) {
                    checkBoxAutoLogin.isChecked = true
                } else {
                    checkBoxAutoLogin.isChecked = false
                }
            }
        }

    }

    private val editorActionListener = TextView.OnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            handleIdPwLogin()
        }

        false
    }

    private val clickListener = View.OnClickListener {
        when (it?.id) {
            R.id.login_kakao_real -> {
                Session.getCurrentSession().open(AuthType.KAKAO_TALK_EXCLUDE_NATIVE_LOGIN, this)
            }

            R.id.facebook -> {
                LoginManager.getInstance().logInWithReadPermissions(this@LoginActivity, Arrays.asList("public_profile", "email", "user_friends"))
            }

            R.id.google -> {
                loginGoogle()
            }

            R.id.insta -> {

            }

            R.id.naver -> {

            }

            R.id.buttonLogin -> {
                if (!isRequestLogin) {
                    isRequestLogin = true
                    handleIdPwLogin()
                }
            }

            R.id.textViewFindAccount -> {
                startActivity(Intent(it.context, FindAccountActivity::class.java).apply {
                    putExtra(FindAccountActivity.INTENT_EXTRA_KEY_CALL_MODE, FindAccountActivity.CALL_MODE_ID)
                })
            }

            R.id.textViewFindPassword -> {
                startActivity(Intent(it.context, FindAccountActivity::class.java).apply {
                    putExtra(FindAccountActivity.INTENT_EXTRA_KEY_CALL_MODE, FindAccountActivity.CALL_MODE_PW)
                })
            }

            R.id.textViewJoin -> {
                startActivityForResult(Intent(it.context, SignUpActivity::class.java),
                        ACTIVITY_REQUEST_CODE_JOIN)
            }


/*            R.id.buttonGoogle -> {
            }

            R.id.buttonFacebook -> {
//                LoginManager.getInstance().logInWithReadPermissions(this@LoginActivity, Arrays.asList("public_profile", "email", "user_friends"))
            }

            R.id.buttonKakao -> {
//                Session.getCurrentSession().open(AuthType.KAKAO_TALK_EXCLUDE_NATIVE_LOGIN, this)
            }*/
        }
    }

    class SessionCallback : ISessionCallback {
        override fun onSessionOpened() {
            UserManagement.getInstance().me(object: MeV2ResponseCallback() {
                override fun onSuccess(result: MeV2Response?) {
                    Logger.e("onSessionOpened result to String :: " + result.toString())
                }

                override fun onFailure( errorResult: ErrorResult) {
                    Logger.e("onSessionOpened onFailure :: ${errorResult.errorCode} and errorMessage :: ${errorResult.errorMessage}")
//                    String message = "failed to get user info. msg=" + errorResult;
//
//                    ErrorCode result = ErrorCode.valueOf(errorResult.getErrorCode());
//                    if (result == ErrorCode.CLIENT_ERROR_CODE) {
//                        //에러로 인한 로그인 실패
//                        // finish();
//                    } else {
//                        //redirectMainActivity();
//                    }
                }

                override fun onSessionClosed( errorResult: ErrorResult ) {
                    Logger.e("onSessionOpened onSessionClosed :: ${errorResult.errorCode} and errorMessage :: ${errorResult.errorMessage}")
                }
            })
        }
// 세션 실패시
        override fun onSessionOpenFailed( exception: KakaoException) {


        }
    }

}