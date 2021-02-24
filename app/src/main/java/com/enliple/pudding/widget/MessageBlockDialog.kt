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
import com.enliple.pudding.commons.network.vo.API54
import kotlinx.android.synthetic.main.dialog_block.*

/**
 * Created by Kim Joonsung on 2018-10-24.
 */
class MessageBlockDialog : AppCompatDialog, View.OnClickListener {

    private var mListener: Listener? = null

    constructor(context: Context, item: API54.MessageItem, mListener: Listener) : super(context) {
        this.mListener = mListener

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(R.layout.dialog_block)

        buttonCancel.setOnClickListener(this)
        buttonBlock.setOnClickListener(this)

        setTitleText(context.getString(R.string.msg_my_block),
                context.getString(R.string.msg_my_block_color_span))

        textViewNickName.text = item.send_mb_nick

        ImageLoad.setImage(
                context, imageViewThumbnail,
                item.send_mb_img,
                null,
                ImageLoad.SCALE_CIRCLE_CROP,
                DiskCacheStrategy.ALL)
    }

    override fun onBackPressed() {
        dismiss()
    }

    override fun dismiss() {
        if (this.isShowing) {
            super.dismiss()
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.buttonCancel -> {
                mListener?.onDismiss()
                dismiss()
            }

            R.id.buttonBlock -> {
                mListener?.onBlock()
                dismiss()
            }
        }
    }

    private fun setTitleText(titleText: String, colorSpannableText: String) {
        val sp = SpannableStringBuilder(titleText)
        sp.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.main_color)),
                3, colorSpannableText.length.plus(4), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        sp.setSpan(StyleSpan(Typeface.NORMAL), 0, colorSpannableText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        textViewTitle.setText(sp)
    }

    interface Listener {
        fun onDismiss()
        fun onBlock()
    }
}