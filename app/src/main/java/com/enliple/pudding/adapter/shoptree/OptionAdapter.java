package com.enliple.pudding.adapter.shoptree;

import android.content.Context;
import androidx.appcompat.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;

import com.enliple.pudding.R;
import com.enliple.pudding.model.PurchaseShopTreeModel;
import com.enliple.pudding.widget.shoptree.DetailDialog;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class OptionAdapter extends BaseAdapter {
    // 문자열을 보관 할 ArrayList
    private ArrayList<PurchaseShopTreeModel> mModelArray;
    private Context mContext = null;
    private OnCallbackListener mCallbackListener;
    private AppCompatTextView optionStr = null;
    private AppCompatTextView price = null;
    private AppCompatTextView quantity = null;
    private AppCompatTextView remain = null;
    private AppCompatTextView name = null;
    private RelativeLayout count_up = null;
    private RelativeLayout count_down = null;
    private RelativeLayout del = null;

    public OptionAdapter(Context context) {
        mContext = context;
        mModelArray = new ArrayList<PurchaseShopTreeModel>();
    }

    public interface OnCallbackListener {
        public void onCallback(boolean hasItem);
    }

    public void setOnCallbackListener(OnCallbackListener callbackListener) {
        this.mCallbackListener = callbackListener;
    }

    @Override
    public int getCount() {
        return mModelArray.size();
    }

    @Override
    public PurchaseShopTreeModel getItem(int position) {
        return mModelArray.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public ArrayList<PurchaseShopTreeModel> getItems() {
        return mModelArray;
    }

    public void setItemList(ArrayList<PurchaseShopTreeModel> array) {
        mModelArray.addAll(array);

    }

    public void setItemList(PurchaseShopTreeModel model) {
        mModelArray.add(model);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Context context = parent.getContext();

        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.shoptree_option_item, parent, false);

            optionStr = (AppCompatTextView) convertView.findViewById(R.id.option_str);
            price = (AppCompatTextView) convertView.findViewById(R.id.price);
            quantity = (AppCompatTextView) convertView.findViewById(R.id.quantity);
            count_up = (RelativeLayout) convertView.findViewById(R.id.count_up);
            count_down = (RelativeLayout) convertView.findViewById(R.id.count_down);
            del = (RelativeLayout) convertView.findViewById(R.id.btn_del);
            remain = (AppCompatTextView) convertView.findViewById(R.id.remained);
            name = (AppCompatTextView) convertView.findViewById(R.id.name);
            holder = new ViewHolder();

            holder.mOptionStr = optionStr;
            holder.mQuantity = quantity;
            holder.mCountDown = count_down;
            holder.mCountUp = count_up;
            holder.mPrice = price;
            holder.mDel = del;
            holder.mRemain = remain;
            holder.mName = name;

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();

            optionStr = holder.mOptionStr;
            quantity = holder.mQuantity;
            count_down = holder.mCountDown;
            count_up = holder.mCountUp;
            price = holder.mPrice;
            del = holder.mDel;
            remain = holder.mRemain;
            name = holder.mName;
        }

        try {
            final int fPosition = position;
            final PurchaseShopTreeModel model = mModelArray.get(position);

            int o_quantity = model.getOptionQuantity();
            final int r_quantity = model.getRemainedQuantity();
            String sName = model.getProductName();
            double i_price = model.getPrice();
            double option_price = model.getOptionPrice();
            double s_price = i_price + option_price;
            double t_price = (double) (s_price * o_quantity);
            name.setText("" + sName);
            remain.setText(r_quantity + "개 남음");
            optionStr.setText(model.getOptionName());
            quantity.setText("" + o_quantity);
            price.setText(toNumFormat((int) t_price) + " 원");
            model.setTotalPrice(t_price);
            mModelArray.remove(position);
            mModelArray.add(position, model);

            del.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mModelArray.remove(fPosition);
                    notifyList();
                }
            });

            count_up.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int o_quantity = model.getOptionQuantity();
                    if (r_quantity >= (o_quantity + 1)) {
                        o_quantity++;
                        model.setOptionQuantity(o_quantity);

                        double i_price = model.getPrice();
                        double option_price = model.getOptionPrice();
                        double s_price = i_price + option_price;
                        double t_price = (double) (s_price * o_quantity);
                        model.setTotalPrice(t_price);

                        mModelArray.remove(fPosition);
                        mModelArray.add(fPosition, model);
                        notifyList();
                    } else {
//                        Toast.makeText(mContext, mContext.getResources().getString(R.string.msg_not_enough_quantity), Toast.LENGTH_SHORT).show();
                        new DetailDialog(mContext, "재고부족", mContext.getResources().getString(R.string.msg_not_enough_quantity), new DetailDialog.CancelRequestDialogListener() {
                            @Override
                            public void onDismiss() {

                            }
                        }).show();
                    }
                }
            });

            count_down.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int o_quantity = model.getOptionQuantity();
                    if (o_quantity > 1) {
                        o_quantity--;
                        model.setOptionQuantity(o_quantity);

                        double i_price = model.getPrice();
                        double option_price = model.getOptionPrice();
                        double s_price = i_price + option_price;
                        double t_price = (double) (s_price * o_quantity);
                        model.setTotalPrice(t_price);

                        mModelArray.remove(fPosition);
                        mModelArray.add(fPosition, model);
                        notifyList();
                    } else {
//                        Toast.makeText(mContext, mContext.getResources().getString(R.string.msg_must_over_one), Toast.LENGTH_SHORT).show();
                        new DetailDialog(mContext, "알림", mContext.getResources().getString(R.string.msg_not_enough_quantity), new DetailDialog.CancelRequestDialogListener() {
                            @Override
                            public void onDismiss() {

                            }
                        }).show();
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

        return convertView;
    }

    private class ViewHolder {
        AppCompatTextView mOptionStr;
        AppCompatTextView mPrice;
        AppCompatTextView mQuantity;
        AppCompatTextView mRemain;
        AppCompatTextView mName;
        RelativeLayout mCountUp;
        RelativeLayout mCountDown;
        RelativeLayout mDel;
    }

    private String toNumFormat(int num) {
        DecimalFormat df = new DecimalFormat("#,###");
        return df.format(num);
    }

    public void notifyList() {
        boolean hasItem = false;
        if (mModelArray.size() <= 0)
            hasItem = false;
        else
            hasItem = true;

        notifyDataSetChanged();

        if (mCallbackListener != null) {
            mCallbackListener.onCallback(hasItem);
        }
    }

    public void increaseCount(int position) {
        PurchaseShopTreeModel model = mModelArray.get(position);
        int o_quantity = model.getOptionQuantity();
        int r_quantity = model.getRemainedQuantity();
        if (r_quantity >= (o_quantity + 1)) {
            o_quantity++;
            model.setOptionQuantity(o_quantity);

            double i_price = model.getPrice();
            double option_price = model.getOptionPrice();
            double s_price = i_price + option_price;
            double t_price = (double) (s_price * o_quantity);
            model.setTotalPrice(t_price);

            mModelArray.remove(position);
            mModelArray.add(position, model);
            notifyList();
        } else {
//            Toast.makeText(mContext, mContext.getResources().getString(R.string.msg_not_enough_quantity), Toast.LENGTH_SHORT).show();
            new DetailDialog(mContext, "재고부족", mContext.getResources().getString(R.string.msg_not_enough_quantity), new DetailDialog.CancelRequestDialogListener() {
                @Override
                public void onDismiss() {

                }
            }).show();
        }
    }
}