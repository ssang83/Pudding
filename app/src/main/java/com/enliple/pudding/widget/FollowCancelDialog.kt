package com.enliple.pudding.widget

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatDialog
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import android.view.Window
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.enliple.pudding.R
import com.enliple.pudding.commons.app.ImageLoad
import com.enliple.pudding.commons.network.vo.API78
import kotlinx.android.synthetic.main.dialog_follow_cancel.*

/**
 * Created by Kim Joonsung on 2018-10-24.
 */
class FollowCancelDialog : AppCompatDialog, View.OnClickListener {

    private var mListener: Listener? = null
    private var mType: String? = null
    constructor(context: Context, item: API78.FollowItem, type: String, listener: Listener?) : super(context) {
        mListener = listener
        mType = type
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(R.layout.dialog_follow_cancel)

        buttonCancel.setOnClickListener(this)
        buttonConfirm.setOnClickListener(this)

        setTitleText(context.getString(R.string.msg_my_follow_cancel_title),
                context.getString(R.string.msg_my_follow_cancel))
        if ( "1".equals(mType) )
            textViewID.text = item.strUserId
        else
            textViewID.text = item.strToUserId

        ImageLoad.setImage(context, imageViewThumbnail, item.strThumbnail, null, ImageLoad.SCALE_CIRCLE_CROP, DiskCacheStrategy.ALL)
    }

    override fun onBackPressed() {
        dismiss()
    }

    override fun dismiss() {
        if (this.isShowing) {
            super.dismiss()
        }
    }

    private fun setTitleText(titleText: String, colorSpannableText: String) {
        val sp = SpannableStringBuilder(titleText)
        sp.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.main_color)),
                5, colorSpannableText.length + 5, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        sp.setSpan(StyleSpan(Typeface.NORMAL), 5, colorSpannableText.length + 5, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        textViewTitle.setText(sp)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.buttonCancel -> onBackPressed()

            R.id.buttonConfirm -> {
                mListener?.onDismiss()
                dismiss()
            }
        }
    }

    interface Listener {
        fun onDismiss()
    }
}