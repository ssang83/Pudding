package com.enliple.pudding.widget;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.enliple.pudding.R;
import com.enliple.pudding.commons.log.Logger;

public class CastSelectDialog extends Dialog {
    private Listener listener;
    private Context context;
    private RelativeLayout buttonClose, buttonLive, buttonVod, buttonMultiLive, root;
    private int width, height;
    public interface Listener {
        void liveClicked();

        void vodClicked();

        void multiLiveClicked();
    }

    public CastSelectDialog(Context context, int w, int h, Listener lsn) {
        super(context);
        this.context = context;
        this.listener = lsn;
        this.width = w;
        this.height = h;
        Logger.e("width :: " + width);
        Logger.e("height :: " + height);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.cast_select_dialog);
        root = findViewById(R.id.root);
        root.setLayoutParams(new FrameLayout.LayoutParams(width, height));

        buttonClose = findViewById(R.id.buttonClose);
        buttonLive = findViewById(R.id.buttonLive);
        buttonVod = findViewById(R.id.buttonVod);
//        buttonMultiLive = findViewById(R.id.buttonMultiLive);
        buttonClose.setOnClickListener(clickListener);
        buttonLive.setOnClickListener(clickListener);
        buttonVod.setOnClickListener(clickListener);
//        buttonMultiLive.setOnClickListener(clickListener);
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.buttonClose:
                    dismiss();
                    break;
                case R.id.buttonVod:
                    if (listener != null)
                        listener.vodClicked();
                    dismiss();
                    break;
                case R.id.buttonLive:
                    if (listener != null)
                        listener.liveClicked();
                    dismiss();
                    break;
//                case R.id.buttonMultiLive:
//                    if (listener != null)
//                        listener.multiLiveClicked();
//                    dismiss();
//                    break;
            }
        }
    };

}