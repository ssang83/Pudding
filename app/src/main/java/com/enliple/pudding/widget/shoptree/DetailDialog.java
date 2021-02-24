package com.enliple.pudding.widget.shoptree;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.Window;
import android.widget.RelativeLayout;

import com.enliple.pudding.R;
import com.enliple.pudding.commons.log.Logger;

public class DetailDialog extends Dialog implements View.OnClickListener {

    private AppCompatTextView textViewCRETitle;
    private AppCompatButton buttonConfirm;
    private AppCompatTextView textViewMsg;
    private RelativeLayout buttonClose;
    private Context mContext;

    private CancelRequestDialogListener mListener;

    public interface CancelRequestDialogListener {
        void onDismiss();
    }

    public DetailDialog(@NonNull Context context, String title, String msg, CancelRequestDialogListener listener) {
        super(context);
        this.mContext = context;
        this.mListener = listener;

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.dialog_detail);

        textViewCRETitle = findViewById(R.id.textViewCRETitle);
        buttonConfirm = findViewById(R.id.buttonConfirm);
        buttonClose = findViewById(R.id.buttonClose);
        textViewMsg = findViewById(R.id.msg);
        buttonConfirm.setOnClickListener(this);
        buttonClose.setOnClickListener(this);

        setCancelable(false);
        setCanceledOnTouchOutside(false);
        String highlightStr = "";
        if (msg.contains("재고"))
            highlightStr = "재고";
        else if (msg.contains("초과"))
            highlightStr = "초과";
        setTextColor(msg, highlightStr);
        textViewCRETitle.setText(title);
    }

    @Override
    public void onClick(View v) {
        long viewId = v.getId();
        if (viewId == R.id.buttonConfirm) {
            if (mListener != null) {
                dismiss();
                mListener.onDismiss();
            }
        } else if (viewId == R.id.buttonClose) {
            dismiss();
        }
    }

    public void dialogShow(Fragment fragment) {
        if (!this.isShowing() && fragment != null && fragment.isAdded()) {
            show();
        }
    }

    public void dialogShow(Activity activity) {
        if (!this.isShowing() && activity != null && !activity.isFinishing()) {
            show();
        }
    }

    /**
     * 문구 색상 설정
     *
     * @param text
     * @param colorSpannableText
     */
    private void setTextColor(@NonNull String text, @NonNull String colorSpannableText) {
        Logger.e("index text :: " + text.indexOf(colorSpannableText));
        int startIndex = text.indexOf(colorSpannableText);
        final SpannableStringBuilder sp = new SpannableStringBuilder(text);
        sp.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.main_color)), startIndex, startIndex + colorSpannableText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        sp.setSpan(new StyleSpan(Typeface.BOLD), startIndex, startIndex + colorSpannableText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        textViewMsg.setText(sp);
    }
}