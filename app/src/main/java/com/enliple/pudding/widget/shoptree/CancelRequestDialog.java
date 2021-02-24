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

import com.enliple.pudding.R;

/**
 * Created by Kim Joonsung on 2018-09-20.
 */

public class CancelRequestDialog extends Dialog implements View.OnClickListener {

    private AppCompatTextView textViewCRETitle;
    private AppCompatButton buttonConfirm;

    private Context mContext;

    private CancelRequestDialogListener mListener;

    public interface CancelRequestDialogListener {
        void onDismiss();
    }

    public CancelRequestDialog(@NonNull Context context, CancelRequestDialogListener listener) {
        super(context);
        this.mContext = context;
        this.mListener = listener;

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.dialog_cancel_request);

        textViewCRETitle = findViewById(R.id.textViewCRETitle);
        buttonConfirm = findViewById(R.id.buttonConfirm);
        buttonConfirm.setOnClickListener(this);

        setCancelable(false);
        setCanceledOnTouchOutside(false);

        setTextColor(mContext.getString(R.string.msg_cancel_request_complete), mContext.getString(R.string.msg_my_shopping_order_detail_order_cancel));
    }

    @Override
    public void onClick(View v) {
        long viewId = v.getId();
        if (viewId == R.id.buttonConfirm) {
            if (mListener != null) {
                mListener.onDismiss();
                dismiss();
            }
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
        final SpannableStringBuilder sp = new SpannableStringBuilder(text);
        sp.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.main_color)),
                0, colorSpannableText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        sp.setSpan(new StyleSpan(Typeface.BOLD), 0, colorSpannableText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        textViewCRETitle.setText(sp);
    }
}
