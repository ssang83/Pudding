package com.enliple.pudding.widget.shoptree;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import android.view.View;
import android.view.Window;
import android.widget.RelativeLayout;

import com.enliple.pudding.R;
import com.enliple.pudding.commons.app.PriceFormatter;

/**
 * Created by Kim Joonsung on 2018-09-20.
 */

public class DepositCancelDialog extends Dialog implements View.OnClickListener {

    private AppCompatTextView textViewCancelPrice;
    private AppCompatTextView textViewPointRefund;
    private AppCompatButton buttonConfirm;
    private AppCompatButton buttonNo;
    private RelativeLayout buttonClose;

    private Context mContext;

    private DepositCancelDialogListener mListener;

    public interface DepositCancelDialogListener {
        void onDismiss();
    }

    public DepositCancelDialog(@NonNull Context context, double price, int point, DepositCancelDialogListener mListener) {
        super(context);
        this.mContext = context;
        this.mListener = mListener;

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.dialog_deposit_cancel);

        setCancelable(false);
        setCanceledOnTouchOutside(false);

        textViewCancelPrice = findViewById(R.id.textViewCancelPrice);
        textViewPointRefund = findViewById(R.id.textViewPointRefund);
        buttonConfirm = findViewById(R.id.buttonConfirm);
        buttonConfirm.setOnClickListener(this);
        buttonNo = findViewById(R.id.buttonNo);
        buttonNo.setOnClickListener(this);
        buttonClose = findViewById(R.id.buttonClose);
        buttonClose.setOnClickListener(this);

        textViewCancelPrice.setText(String.format(mContext.getString(R.string.msg_price_format),
                PriceFormatter.Companion.getInstance().getFormattedValue(price)));

        textViewPointRefund.setText(String.format(mContext.getString(R.string.msg_price_format),
                PriceFormatter.Companion.getInstance().getFormattedValue(point)));
    }

    @Override
    public void onClick(View v) {
        long viewId = v.getId();
        if (viewId == R.id.buttonNo || viewId == R.id.buttonClose) {
            dismiss();
        } else if (viewId == R.id.buttonConfirm) {
            if (mListener != null) {
                mListener.onDismiss();
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
}
