package com.enliple.pudding.widget;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import androidx.annotation.NonNull;
import android.view.View;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.enliple.pudding.R;
import com.enliple.pudding.commons.shoptree.network.ShopTreeAsyncTask;

public class DeliveryConditionDialog extends Dialog {
    private View layoutRoot;
    private TextView textViewDeliverCondition;
    private RelativeLayout buttonClose;
    private Context mContext;

    public DeliveryConditionDialog(@NonNull Context context, String msg) {
        super(context);
        mContext = context;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.dialog_delivery_condition);

        layoutRoot = findViewById(R.id.layoutRoot);
        textViewDeliverCondition = (TextView) findViewById(R.id.textViewDeliverCondition);
        buttonClose = findViewById(R.id.buttonClose);

        textViewDeliverCondition.setText(msg);

        buttonClose.setOnClickListener(mClickListener);
    }

    public DeliveryConditionDialog(@NonNull Context context) {
        super(context);
        mContext = context;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.dialog_delivery_condition);

        layoutRoot = findViewById(R.id.layoutRoot);
        textViewDeliverCondition = (TextView) findViewById(R.id.textViewDeliverCondition);
        buttonClose = findViewById(R.id.buttonClose);
        buttonClose.setOnClickListener(mClickListener);
        loadData();
    }

    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            long viewId = view.getId();
            if (viewId == R.id.buttonClose) {
                dismiss();
            }
        }
    };

    private void loadData() {
        ShopTreeAsyncTask task = new ShopTreeAsyncTask(mContext);
        task.getDeliveryFeeGuide(new ShopTreeAsyncTask.OnDefaultObjectCallbackListener() {
            @Override
            public void onResponse(boolean result, Object obj) {

            }
        });
    }
}
