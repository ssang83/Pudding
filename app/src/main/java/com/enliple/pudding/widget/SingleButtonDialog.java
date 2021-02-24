package com.enliple.pudding.widget;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;

import com.enliple.pudding.R;

public class SingleButtonDialog extends Dialog {
    private AppCompatButton buttonYes;
    private AppCompatTextView tvContent;

    private String strContent;
    private String btnStr;

    private SingleButtonDialogListener mListener;

    public interface SingleButtonDialogListener {
        void onConfirm();
    }

    public SingleButtonDialog(@NonNull Context context, int dialogWidth, String content, String btnStr, SingleButtonDialogListener listener) {
        super(context);
        this.strContent = content;
        this.btnStr = btnStr;
        this.mListener = listener;

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        View v = LayoutInflater.from(context).inflate(R.layout.dialog_single_button, null, false);
        setContentView(v);

        LinearLayout root = v.findViewById(R.id.layout_root);
        ViewGroup.LayoutParams param = root.getLayoutParams();
        param.width = dialogWidth;
        root.setLayoutParams(param);

        buttonYes = v.findViewById(R.id.buttonYes);
        tvContent = v.findViewById(R.id.content);
        tvContent.setText(strContent);
        buttonYes.setText(btnStr);
        buttonYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onConfirm();
                    dismiss();
                }
            }
        });
    }
}
