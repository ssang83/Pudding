package com.enliple.pudding.adapter.shoptree;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.enliple.pudding.R;
import com.enliple.pudding.model.ShopTreeOption;

import java.util.ArrayList;

public class ShopTreeSpinnerAdapter extends BaseAdapter {
    private Context mContext = null;
    private LayoutInflater mInflater = null;
    private int mLayoutId = 0;
    private ArrayList<ShopTreeOption> mItems = null;
    private ShopTreeOption[] mItemsArr = null;
    private boolean isStringArrayList = false;
    private int mGravity = Gravity.CENTER;

    public ShopTreeSpinnerAdapter(Context context, int layoutId, ArrayList<ShopTreeOption> items, int gravity) {
        isStringArrayList = true;
        this.mContext = context;
        this.mItems = items;
        this.mLayoutId = layoutId;

        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public ShopTreeSpinnerAdapter(Context context, int layoutId, ArrayList<ShopTreeOption> items) {
        isStringArrayList = true;
        this.mContext = context;
        this.mItems = items;
        this.mLayoutId = layoutId;

        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public ShopTreeSpinnerAdapter(Context context, int layoutId, ShopTreeOption[] items) {
        isStringArrayList = false;
        this.mContext = context;
        this.mItemsArr = items;
        this.mLayoutId = layoutId;

        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public int getCount() {
        if (isStringArrayList)
            return this.mItems.size();
        else
            return this.mItemsArr.length;
    }

    @Override
    public ShopTreeOption getItem(int position) {
        if (isStringArrayList)
            return this.mItems.get(position);
        else
            return this.mItemsArr[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(mLayoutId, parent, false);
        }

        TextView text = (TextView) convertView.findViewById(R.id.spinner_text);
        if (isStringArrayList)
            text.setText(mItems.get(position).getName());
        else
            text.setText(mItemsArr[position].getName());

        return convertView;
    }
}