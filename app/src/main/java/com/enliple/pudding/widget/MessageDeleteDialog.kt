package com.enliple.pudding.widget

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import android.view.Window
import androidx.appcompat.app.AppCompatDialog
import androidx.core.content.ContextCompat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.enliple.pudding.R
import com.enliple.pudding.commons.app.ImageLoad
import com.enliple.pudding.commons.network.vo.API54
import kotlinx.android.synthetic.main.dialog_message_delete.*

class MessageDeleteDialog : AppCompatDialog, View.OnClickListener {

    private var mListener: Listener? = null

    constructor(context: Context, item: API54.MessageItem, mListener: Listener) : super(context) {
        this.mListener = mListener

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(R.layout.dialog_message_delete)

        buttonCancel.setOnClickListener(this)
        buttonBlock.setOnClickListener(this)
        guide.text = "${item.send_mb_nick}와 대화한 내용을\n삭제하시겠습니까?\n삭제 시 모든 대화내용이 삭제됩니다."
        setTitleText(context.getString(R.string.msg_message_delete_title),
                context.getString(R.string.msg_my_qna_del))

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
                mListener?.onDelete()
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
        fun onDelete()
    }
}