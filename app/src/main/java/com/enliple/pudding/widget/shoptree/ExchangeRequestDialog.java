package com.enliple.pudding.widget.shoptree;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.RelativeLayout;

import com.enliple.pudding.R;

/**
 * Created by Kim Joonsung on 2018-09-20.
 */

public class ExchangeRequestDialog extends Dialog implements View.OnClickListener {
    private static final int REQUEST_DETAIL = 0;

    private AppCompatTextView textViewTitle;
    private AppCompatTextView textViewGuide1;
    private AppCompatButton buttonConfirm;
    private AppCompatButton buttonCancel;
    private RelativeLayout buttonClose;

    private ExchangeRequestDialogListener mListener;

    public interface ExchangeRequestDialogListener {
        void onConfirm(Context context);
    }

    private Context mContext;

    public ExchangeRequestDialog(@NonNull Context context, ExchangeRequestDialogListener listener) {
        super(context);
        this.mContext = context;
        this.mListener = listener;

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        View v = LayoutInflater.from(context).inflate(R.layout.dialog_exchange_request, null, false);
        setContentView(v);

        textViewGuide1 = v.findViewById(R.id.textViewGuide1);
        textViewTitle = v.findViewById(R.id.textViewTitle);
        buttonCancel = v.findViewById(R.id.buttonCancel);
        buttonCancel.setOnClickListener(this);
        buttonConfirm = v.findViewById(R.id.buttonConfirm);
        buttonConfirm.setOnClickListener(this);
        buttonClose = v.findViewById(R.id.buttonClose);
        buttonClose.setOnClickListener(this);

        setCancelable(true);
        setCanceledOnTouchOutside(false);

        setTextColor();
    }

    @Override
    public void onClick(View v) {
        long viewId = v.getId();
        if (viewId == R.id.buttonCancel || viewId == R.id.buttonClose) {
            dialogDismiss();
        } else if (viewId == R.id.buttonConfirm) {
            if (mListener != null) {
                mListener.onConfirm(mContext);
                dismiss();
            }
        }
    }

    @Override
    public void onBackPressed() {
        dialogDismiss();
    }

    private void setTextColor() {
        final SpannableStringBuilder sp = new SpannableStringBuilder(mContext.getString(R.string.msg_exchange_request_guide));
        sp.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.main_color)),
                0, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        sp.setSpan(new StyleSpan(Typeface.BOLD), 0, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        textViewTitle.setText(sp);

        final SpannableStringBuilder sp1 = new SpannableStringBuilder(mContext.getString(R.string.msg_cre_request_exchange_guide1));
        sp1.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.main_color)),
                0, 6, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        sp1.setSpan(new StyleSpan(Typeface.BOLD), 0, 6, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        sp1.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.main_color)),
                11, 13, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        sp1.setSpan(new StyleSpan(Typeface.BOLD), 11, 13, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        textViewGuide1.setText(sp1);
    }

    public void dialogShow(Activity activity) {
        if (!this.isShowing() && activity != null && !activity.isFinishing()) {
            show();
        }
    }

    public void dialogShow(Context context) {
        if (context != null) {
            show();
        }
    }

    public void dialogDismiss() {
        if (this.isShowing()) {
            dismiss();
        }
    }
}
