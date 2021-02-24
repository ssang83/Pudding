package com.enliple.pudding.widget.shoptree;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import android.view.View;
import android.view.Window;
import android.widget.RelativeLayout;

import com.enliple.pudding.R;
import com.enliple.pudding.commons.shoptree.network.ShopTreeAsyncTask;

/**
 * Created by Kim Joonsung on 2018-09-27.
 */

public class DeliveryConditionDialog extends Dialog implements View.OnClickListener {

    private AppCompatTextView textViewDeliverCondition;
    private RelativeLayout buttonClose;
    private Context mContext;

    public DeliveryConditionDialog(@NonNull Context context, String msg) {
        super(context);
        mContext = context;

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.dialog_delivery_condition);

        textViewDeliverCondition = findViewById(R.id.textViewDeliverCondition);
        buttonClose = findViewById(R.id.buttonClose);
        buttonClose.setOnClickListener(this);

        textViewDeliverCondition.setText(msg);
    }

    public DeliveryConditionDialog(@NonNull Context context) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.dialog_delivery_condition);

        loadData();
    }

    @Override
    public void onClick(View v) {
        long viewId = v.getId();
        if (viewId == R.id.buttonClose) {
            dismiss();
        }
    }

    private void loadData() {
        ShopTreeAsyncTask task = new ShopTreeAsyncTask(mContext);
        task.getDeliveryFeeGuide(
                (result, obj) -> {

                });
    }
}
