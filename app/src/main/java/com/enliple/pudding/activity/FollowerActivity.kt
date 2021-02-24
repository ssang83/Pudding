package com.enliple.pudding.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import com.enliple.pudding.AbsBaseActivity
import com.enliple.pudding.R
import com.enliple.pudding.adapter.my.FollowerAdapter
import com.enliple.pudding.commons.app.NetworkStatusUtils
import com.enliple.pudding.commons.app.SoftKeyboardUtils
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.*
import com.enliple.pudding.commons.network.vo.API78
import com.enliple.pudding.commons.network.vo.BaseAPI
import com.enliple.pudding.commons.widget.toast.AppToast
import com.enliple.pudding.widget.FollowCancelDialog
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_follower.*
import kotlinx.android.synthetic.main.buy_option_item.*
import kotlinx.android.synthetic.main.store_first_category_item.*
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

/**
 * Created by Kim Joonsung on 2018-10-24.
 */
class FollowerActivity : AbsBaseActivity(), FollowerAdapter.Listener {

    companion object {
        private const val REQUEST_LOGIN = 0x0001

        const val INTENT_EXTRA_KEY_IS_FOLLOWER = "is_follower"
        const val INTENT_EXTRA_KEY_USERID = "follower_userid"

    }

    private lateinit var mAdapter: FollowerAdapter

    private var isFollower: Boolean = true

    private var listPage = 1
    private var totalPageCount = 1
    private var type = ""
    private var userId = ""
    private var isFollowType = ""
    private var strToUserId = ""
    private var followItem:API78.FollowItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_follower)
        EventBus.getDefault().register(this)

        buttonClose.setOnClickListener(clickListener)
        editTextSearch.imeOptions = EditorInfo.IME_ACTION_DONE
        editTextSearch.addTextChangedListener(textWaterListener)
        editTextSearch.setOnEditorActionListener(editorActionListener)
        editTextSearch.setCompoundDrawablesWithIntrinsicBounds(R.drawable.follower_search, 0, 0, 0)

        recyclerViewFollower.setHasFixedSize(false)

        checkIntent(intent)

        type = if (isFollower) "1" else "2"

        mAdapter = FollowerAdapter(this@FollowerActivity, type)
        mAdapter.setListener(this)
        recyclerViewFollower.adapter = mAdapter

        val bus = NetworkBus(NetworkApi.API77.name, userId, type, "")
        EventBus.getDefault().post(bus)
    }

    override fun onDozeModeStateChanged(dozeEnable: Boolean) {}

    override fun onNetworkStatusChanged(status: NetworkStatusUtils.NetworkStatus) {}

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == REQUEST_LOGIN && resultCode == Activity.RESULT_OK) {
            if("N" == isFollowType) {
                FollowCancelDialog(this, followItem!!, type, object : FollowCancelDialog.Listener {
                    override fun onDismiss() {
                        var map = HashMap<String, String>()
                        map.put("strUserId", AppPreferences.getUserId(this@FollowerActivity)!!)
                        map.put("strToUserId", strToUserId)
                        map.put("isFollow", isFollowType)
                        var body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), (JSONObject(map)).toString())

                        NetworkBus(NetworkApi.API2.name, body).let {
                            EventBus.getDefault().post(it)
                        }
                    }
                }).show()
            } else {
                JSONObject().apply {
                    put("strUserId", AppPreferences.getUserId(this@FollowerActivity)!!)
                    put("strToUserId", strToUserId)
                    put("isFollow", isFollowType)
                }.let {
                    val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), it.toString())
                    NetworkBus(NetworkApi.API2.name, body).let {
                        EventBus.getDefault().post(it)
                    }
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun goProfile(item: API78.FollowItem) {
        if(type == "1") {
            startActivity(Intent(this, CasterProfileActivity::class.java).apply {
                putExtra(CasterProfileActivity.INTENT_KEY_EXTRA_USER_ID, item.strUserId)
            })
        } else {
            startActivity(Intent(this, CasterProfileActivity::class.java).apply {
                putExtra(CasterProfileActivity.INTENT_KEY_EXTRA_USER_ID, item.strToUserId)
            })
        }
    }

    override fun onFollowStatusChanged(toUserId:String, isFollow:String, item:API78.FollowItem) {
        isFollowType = isFollow
        strToUserId = toUserId
        followItem = item

        if(AppPreferences.getLoginStatus(this)) {
            if("N" == isFollow) {
                FollowCancelDialog(this, item, type, object : FollowCancelDialog.Listener {
                    override fun onDismiss() {
                        var map = HashMap<String, String>()
                        map.put("strUserId", AppPreferences.getUserId(this@FollowerActivity)!!)
                        map.put("strToUserId", toUserId)
                        map.put("isFollow", isFollow)
                        var body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), (JSONObject(map)).toString())

                        NetworkBus(NetworkApi.API2.name, body).let {
                            EventBus.getDefault().post(it)
                        }
                    }
                }).show()
            } else {
                JSONObject().apply {
                    put("strUserId", AppPreferences.getUserId(this@FollowerActivity)!!)
                    put("strToUserId", toUserId)
                    put("isFollow", isFollow)
                }.let {
                    val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), it.toString())
                    NetworkBus(NetworkApi.API2.name, body).let {
                        EventBus.getDefault().post(it)
                    }
                }
            }
        } else {
            startActivityForResult(Intent(this, LoginActivity::class.java), REQUEST_LOGIN)
        }
    }

    private fun checkIntent(intent: Intent) {
        isFollower = intent.getBooleanExtra(INTENT_EXTRA_KEY_IS_FOLLOWER, false)
        userId = intent.getStringExtra(INTENT_EXTRA_KEY_USERID)

        if (isFollower) {
            textViewTitle.text = getString(R.string.msg_my_follower)
            editTextSearch.setHint(getString(R.string.msg_my_follower_search))
        } else {
            textViewTitle.text = getString(R.string.msg_my_following)
            editTextSearch.setHint(getString(R.string.msg_my_following_search))
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: NetworkBusResponse) {
        val API77 = NetworkHandler.getInstance(this@FollowerActivity)
                .getKey(NetworkApi.API77.toString(), userId, type, editTextSearch.text.toString())

        var m_userId = AppPreferences.getUserId(this@FollowerActivity)
        var key = NetworkHandler.getInstance(this@FollowerActivity).getKey(NetworkApi.API21.toString(), m_userId!!, "")
        when (data.arg1) {
            API77 -> loadData(data)
            key -> {
                val bus = NetworkBus(NetworkApi.API77.name, userId, type, editTextSearch.text.toString())
                EventBus.getDefault().post(bus)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: NetworkBusFastResponse) {
        var followChange = NetworkApi.API2.toString()
        if(data.arg1 == followChange) {
            val bus = NetworkBus(NetworkApi.API77.name, userId, type, editTextSearch.text.toString())
            EventBus.getDefault().post(bus)
        }
    }

    private fun loadData(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            val str = DBManager.getInstance(this@FollowerActivity)
                    .get(data.arg1)
            val response: API78 = Gson().fromJson(str, API78::class.java)

            totalPageCount = response.nTotalCount
            listPage = response.pageCount
            if (response.nTotalCount > 0) {
                mAdapter.setItems(response.data)
            } else {
                mAdapter.removeItems()
            }
        } else {
            var errorResult: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
            Logger.e("$errorResult")

            AppToast(this@FollowerActivity).showToastMessage(errorResult.message,
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM)
        }
    }

    private val editorActionListener = TextView.OnEditorActionListener { _, actionId, _ ->

        if (actionId == EditorInfo.IME_ACTION_DONE) {
            val bus = NetworkBus(NetworkApi.API77.name, userId, type, editTextSearch.text.toString())
            EventBus.getDefault().post(bus)

            SoftKeyboardUtils.hideKeyboard(editTextSearch)
        }

        false
    }


    private val clickListener = View.OnClickListener {
        when (it?.id) {
            R.id.buttonClose -> onBackPressed()

        }
    }

    private val touchListener = object : View.OnTouchListener {
        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            val DRAWABLE_LEFT = 0
            val DRAWABLE_TOP = 1
            val DRAWABLE_RIGHT = 2
            val DRAWABLE_BOTTOM = 3

            when (event?.action) {
                MotionEvent.ACTION_UP -> {
                    if (event.rawX >= (editTextSearch.right - editTextSearch.compoundDrawables[DRAWABLE_LEFT].bounds.width())) {
                        editTextSearch.setText("")
                        return true
                    }
                }
            }
            return false
        }
    }

    private val textWaterListener = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            val text = s?.toString()
            if (text!!.length > 0) {
                editTextSearch.setCompoundDrawablesWithIntrinsicBounds(R.drawable.follower_search, 0, 0, 0)
                editTextSearch.setOnTouchListener(touchListener)
            } else {
                editTextSearch.setCompoundDrawablesWithIntrinsicBounds(R.drawable.follower_search, 0, 0, 0)
                editTextSearch.setOnTouchListener(null)
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }
}