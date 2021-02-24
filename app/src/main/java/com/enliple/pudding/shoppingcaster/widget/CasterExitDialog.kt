package com.enliple.pudding.shoppingcaster.widget

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.enliple.pudding.R
import com.enliple.pudding.commons.events.OnSingleClickListener
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.widget.toast.AppToast
import kotlinx.android.synthetic.main.dialog_caster_finish.*
import okhttp3.MultipartBody
import okhttp3.RequestBody

/**
 * 방송송출자 전용 방송 종료 팝업
 */
class CasterExitDialog : DialogFragment() {
    private var mListener: Listener? = null
    private var mTitle = ""
    private var mTag = ""
    private var mStreamKey = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_caster_finish, container)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setStyle(STYLE_NO_FRAME, R.style.PlayerProductListDialog)

        mStreamKey = arguments!!.getString("stream")
        mTag = arguments!!.getString("tag")
        mTitle = arguments!!.getString("title")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        if (!mTag.isNullOrEmpty()) {
            var tagList = mTag.split(",") as MutableList<String>
            if (tagList.isNotEmpty()) {
                editTagView.tagList = tagList
            }
        }

        if (!mTitle.isNullOrEmpty()) {
            editTextTitle.setText(mTitle)
        }

        buttonYes.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                if (editTextTitle.text.isNullOrEmpty()) {
                    AppToast(context!!).showToastMessage("제목을 입력해 주세요",
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_MIDDLE)
                    return
                }

                var tagList = editTagView.tagList
                if (tagList.isEmpty()) {
                    AppToast(context!!).showToastMessage("태그를 입력해 주세요",
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_MIDDLE)
                    return
                }

                var body = MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("strTitle", editTextTitle.text.toString())
                        .addFormDataPart("strTag", getTagString(tagList))
                        .addFormDataPart("user_show_YN", if (openSwitch.isChecked) "Y" else "N")
                        .addFormDataPart("share_YN", if (shareSwitch.isChecked) "Y" else "N")
                        .addFormDataPart("comment_YN", if (commentSwitch.isChecked) "Y" else "N")
                        .addFormDataPart("streamKey", mStreamKey)
                mListener?.onCastingFinish(body.build())

                dismiss()
            }
        })

        buttonNo.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                dismiss()
            }
        })
    }

    private fun getTagString(list: List<String>): String? {
        var tagStr = ""
        if (list.isNotEmpty()) {
            var builder = StringBuilder()
            for (i in 0 until list.size) {
                var value = list[i].replace("#", "")
                builder.append(value)
                if (i != list.size - 1) {
                    builder.append(",")
                }
            }
            tagStr = builder.toString()
        }

        Logger.e("getTagString:$tagStr")
        return tagStr
    }

    fun setListener(listener: Listener) {
        this.mListener = listener
    }

    interface Listener {
        fun onCastingFinish(body: RequestBody)
    }
}