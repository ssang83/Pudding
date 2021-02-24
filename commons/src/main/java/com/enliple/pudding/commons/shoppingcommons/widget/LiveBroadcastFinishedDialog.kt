package com.enliple.pudding.commons.shoppingcommons.widget

import android.app.Dialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.enliple.pudding.commons.R
import com.enliple.pudding.commons.app.ImageLoad
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.NetworkApi
import com.enliple.pudding.commons.network.NetworkBusFastResponse
import com.enliple.pudding.commons.shoptree.network.ShopTreeAsyncTask
import kotlinx.android.synthetic.main.dialog_live_broadcast_finished.*
import kotlinx.android.synthetic.main.dialog_live_broadcast_finished.view.*
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

/**
 * 라이브 방송 종료 안내 팝업
 * @author hkcha
 * @since 2018.08.22
 */
class LiveBroadcastFinishedDialog(context: Context,
                                  userNick:String,
                                  profile:String,
                                  userId:String,
                                  isFollowing:Boolean,
                                  isForce: Boolean) : Dialog(context) , View.OnClickListener {

    private var mIsFollowing = false
    private var mUserNick = ""
    private var mProfileImg = ""
    private var mUserId = ""

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val root = LayoutInflater.from(context).inflate(R.layout.dialog_live_broadcast_finished, null, false)
        setContentView(root)
        setCancelable(false)

        this.mUserNick = userNick
        this.mProfileImg = profile
        this.mUserId = userId
        this.mIsFollowing = isFollowing

        if(isForce) {
            textViewTitle.text = context.getString(R.string.msg_live_broadcast_force_finished)
            textViewGuide.text = context.getString(R.string.msg_live_broadcast_force_finish_message)
        } else {
            textViewTitle.text = context.getString(R.string.msg_live_broadcast_finished)
            textViewGuide.text = context.getString(R.string.msg_live_broadcast_finish_message)
        }

        ImageLoad.setImage(
                context,
                casterProfile,
                mProfileImg,
                null,
                ImageLoad.SCALE_CIRCLE_CROP,
                DiskCacheStrategy.ALL)

        nickName.text = mUserNick

        root.buttonConfirm.setOnClickListener(this)
        root.following.setOnClickListener(this)

        if(mIsFollowing) {
            root.following.setBackgroundResource(R.drawable.end_check_btn)
        } else {
            root.following.setBackgroundResource(R.drawable.end_plus_ico)
        }

        if(AppPreferences.getUserId(context) == mUserId) {
            root.following.visibility = View.GONE
        } else {
            root.following.visibility = View.VISIBLE
        }
    }

    override fun dismiss() {
        if (this.isShowing) {
            super.dismiss()
        }
    }

    override fun show() {
        if (!isShowing) {
            super.show()
        }
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.buttonConfirm -> dismiss()
            R.id.following -> {
                if(AppPreferences.getLoginStatus(context!!)) {
                    val obj = JSONObject().apply {
                        put("strUserId", AppPreferences.getUserId(context))
                        put("strToUserId", mUserId)
                        put("isFollow", if(mIsFollowing) "N" else "Y")
                    }

                    val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), obj.toString())
                    val task = ShopTreeAsyncTask(context!!)
                    task.postFollow(body, { result, obj ->
                        try {
                            mIsFollowing = !mIsFollowing
                            if(mIsFollowing) {
                                following.setBackgroundResource(R.drawable.end_check_btn)
                            } else {
                                following.setBackgroundResource(R.drawable.end_plus_ico)
                            }
                        } catch (e:Exception) {
                            Logger.p(e)
                        }
                    })
                } else {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setComponent(ComponentName("com.enliple.pudding", "com.enliple.pudding.activity.LoginActivity"))
                    }
                    context.startActivity(intent)
                }
            }
        }
    }
}