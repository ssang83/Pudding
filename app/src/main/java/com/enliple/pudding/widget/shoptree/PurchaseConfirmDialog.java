package com.enliple.pudding.widget.shoptree;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;

import com.enliple.pudding.R;

public class PurchaseConfirmDialog extends Dialog {

    private PurchaseConfirmDialogListener mListener;

    public interface PurchaseConfirmDialogListener {
        void onConfirm(Context context);
    }

    private Context mContext;
    private AppCompatButton buttonNo, buttonYes;

    public PurchaseConfirmDialog(@NonNull Context context, int dialogWidth, PurchaseConfirmDialogListener listener) {
        super(context);
        this.mContext = context;
        this.mListener = listener;

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        View v = LayoutInflater.from(context).inflate(R.layout.dialog_purchase_confirm, null, false);
        setContentView(v);

        LinearLayout root = v.findViewById(R.id.layout_root);
        ViewGroup.LayoutParams param = root.getLayoutParams();
        param.width = dialogWidth;
        root.setLayoutParams(param);

        buttonNo = v.findViewById(R.id.buttonNo);
        buttonYes = v.findViewById(R.id.buttonYes);
        buttonNo.setOnClickListener(clickListener);
        buttonYes.setOnClickListener(clickListener);
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.buttonNo:
                    dialogDismiss();
                    break;
                case R.id.buttonYes:
                    if (mListener != null) {
                        mListener.onConfirm(mContext);
                        dismiss();
                    }
                    break;
            }
        }
    };

//    @Override
//    public void onClick(View v) {
//        long viewId = v.getId();
//        if (viewId == R.id.buttonNo) {
//            dialogDismiss();
//        } else if (viewId == R.id.buttonYes) {
//            if (mListener != null) {
//                mListener.onConfirm(mContext);
//                dismiss();
//            }
//        }
//    }

    @Override
    public void onBackPressed() {
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
