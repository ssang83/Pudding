package com.enliple.pudding.widget;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;
import android.widget.ListView;

import com.enliple.pudding.R;

public class ShopTreeListDialog extends Dialog {
    private ListView mListView = null;

    private Context mContext = null;

    public ShopTreeListDialog(Context context) {
        super(context);
        mContext = context;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.shoptree_list_dialog);
        mListView = (ListView) findViewById(R.id.popup_list);
    }
}