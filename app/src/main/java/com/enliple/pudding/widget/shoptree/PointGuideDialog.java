package com.enliple.pudding.widget.shoptree;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.RelativeLayout;

import com.enliple.pudding.R;

/**
 * Created by Kim Joonsung on 2018-09-19.
 */

public class PointGuideDialog extends Dialog implements View.OnClickListener {
    private RelativeLayout buttonClose;

    public PointGuideDialog(Context context) {
        super(context);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        View v = LayoutInflater.from(context).inflate(R.layout.layout_dialog_point_guide, null, false);
        setContentView(v);

        buttonClose = v.findViewById(R.id.buttonClose);
        buttonClose.setOnClickListener(this);

        setCancelable(true);
        setCanceledOnTouchOutside(false);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.buttonClose) {
            dialogDismiss();
        }
    }

    @Override
    public void onBackPressed() {
        dialogDismiss();
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
