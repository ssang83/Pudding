package com.enliple.pudding.widget.main;

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatDialog
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import com.enliple.pudding.R
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.NetworkApi
import com.enliple.pudding.commons.network.NetworkBus
import com.enliple.pudding.commons.network.NetworkBusResponse
import com.enliple.pudding.commons.network.NetworkHandler
import com.enliple.pudding.commons.shoptree.network.ShopTreeAsyncTask
import com.enliple.pudding.commons.widget.toast.AppToast
import kotlinx.android.synthetic.main.dialog_nick_name_modify.*
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

/**
 * Created by Kim Joonsung on 2018-10-08.
 */
class NickNameModifyDialog : AppCompatDialog, View.OnClickListener {

    private var isNickCheck = false
    private var mListener:Listener? = null

    interface Listener {
        fun onConfirm(nickName:String)
    }

    constructor(context: Context, listener: Listener) : super(context) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        this.mListener = listener

        setCancelable(true)
        setCanceledOnTouchOutside(true)

        val view = LayoutInflater.from(context).inflate(R.layout.dialog_nick_name_modify, null, false)
        setContentView(view)

        buttonCheck.setOnClickListener(this)
        buttonCancel.setOnClickListener(this)
        buttonConfirm.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.buttonCancel -> dismiss()

            R.id.buttonConfirm -> {
                if (isNickCheck) {
                    val obj = JSONObject().apply {
                        put("userNick", editTextNickName.text.toString())
                    }
                    val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), obj.toString())

                    val task = ShopTreeAsyncTask(context!!)
                    task.putUserNick(body, { result, obj ->
                        try {
                            val response = JSONObject(obj.toString())
                            if(response["result"] == "success") {
                                isNickCheck = false
                                AppToast(context).showToastMessage("닉네임이 변경되었습니다.",
                                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                                        AppToast.GRAVITY_MIDDLE)

                                mListener?.onConfirm(editTextNickName.text.toString())
                                dismiss()
                            } else {
                                if (response["error"] == "08") {
                                    textViewNickNameStatus.visibility = View.VISIBLE
                                } else {
                                    textViewNickNameStatus.visibility = View.GONE
                                    AppToast(context).showToastMessage(response["message"].toString(),
                                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                                            AppToast.GRAVITY_MIDDLE)
                                }
                            }
                        } catch (e:Exception) {
                            Logger.p(e)
                        }
                    })
                } else {
                    AppToast(context).showToastMessage("닉네임 중복확인을 눌러주세요.",
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_MIDDLE)
                }
            }

            R.id.buttonCheck -> {
                val task = ShopTreeAsyncTask(context!!)
                task.getUserNickCheck(editTextNickName.text.toString(), {
                    result, obj ->
                    try {
                        val response = JSONObject(obj.toString())
                        if(response["result"] == "success") {
                            isNickCheck = true
                            AppToast(context).showToastMessage("사용 가능한 닉네임입니다.",
                                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                                    AppToast.GRAVITY_MIDDLE)
                        } else {
                            AppToast(context).showToastMessage(response["message"].toString(),
                                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                                    AppToast.GRAVITY_MIDDLE)
                        }
                    } catch (e:Exception) {
                        Logger.p(e)
                    }
                })
            }
        }
    }
}