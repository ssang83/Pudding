package com.enliple.pudding.adapter;

import android.content.Context;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.enliple.pudding.R;
import com.enliple.pudding.commons.app.Utils;
import com.enliple.pudding.commons.log.Logger;
import com.enliple.pudding.commons.widget.recyclerview.WrappedLinearLayoutManager;
import com.enliple.pudding.commons.widget.toast.AppToast;
import com.enliple.pudding.model.DetailOptionItem;
import com.enliple.pudding.model.DetailOptionList;
import com.enliple.pudding.model.DetailSubOption;
import com.enliple.pudding.widget.shoptree.DetailDialog;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class DetailOptionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_OPTION = 0X1001;
    private static final int TYPE_ITEM = 0X2001;

    public Context context;

    public ArrayList<DetailOptionList> mainOptionList = new ArrayList<>();
    public ArrayList<DetailOptionItem> fullOptionList = new ArrayList<>();
    public ArrayList<DetailOptionItem> selectedFullOptionList = new ArrayList<>();
    public OptionViewHolder optionViewHolder;
    public OptionItemViewHolder optionItemViewHolder;
    public String productName;
    public int totalQuantity = 0;
    public double totalPrice = 0;
    private Listener listener;

    public interface Listener {
        public void setTotalValue(String totalQuantity, String totalPrice);
    }

    public DetailOptionAdapter(Context context, String productName, Listener listener) {
        Logger.e("DetailOptionAdapter creator");
        this.context = context;
        this.listener = listener;
        this.productName = productName;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_OPTION) {
            Logger.e("type_option");
            View view = LayoutInflater.from(context).inflate(R.layout.detail_option, parent, false);
            optionViewHolder = new OptionViewHolder(context, view);
            return optionViewHolder;
        } else {
            Logger.e("type_item");
            View view = LayoutInflater.from(context).inflate(R.layout.selected_option, parent, false);
            optionItemViewHolder = new OptionItemViewHolder(context, view);
            return optionItemViewHolder;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof OptionViewHolder) {
            bindOptionViewHolder((OptionViewHolder) holder, position);
        } else {
            bindOptionItemViewHolder((OptionItemViewHolder) holder, position - mainOptionList.size());
        }
    }

    private void bindOptionViewHolder(OptionViewHolder holder, int position) {
        DetailOptionList option = mainOptionList.get(position);
        holder.optionName.setText(option.getName());

        holder.adapter.setItems(option, fullOptionList);

        if ( position == 0 ) {
            holder.empty.setVisibility(View.VISIBLE);
        } else {
            holder.empty.setVisibility(View.GONE);
        }

        if ( position == (mainOptionList.size() - 1 ) ) {
            if ( selectedFullOptionList != null && selectedFullOptionList.size() > 0 ) {
                holder.botLine.setVisibility(View.VISIBLE);
                holder.botEmpty.setVisibility(View.GONE);
            } else {
                holder.botLine.setVisibility(View.GONE);
                holder.botEmpty.setVisibility(View.VISIBLE);
                Logger.e("botLine gone");
            }
        } else {
            holder.botLine.setVisibility(View.GONE);
            holder.botEmpty.setVisibility(View.GONE);
        }

        if ( option.isOpen() ) {
            holder.downArrow.setBackgroundResource(R.drawable.item_fold_ico);
            holder.mainOptionLayer.setBackgroundResource(R.drawable.rect_purple_bg);
            holder.recyclerBackground.setVisibility(View.VISIBLE);
        } else {
            if ( option.getSelectedSubOption() != null ) {
                holder.downArrow.setBackgroundResource(R.drawable.item_fold_ico);
                holder.mainOptionLayer.setBackgroundResource(R.drawable.rect_purple_bg);
            } else {
                holder.downArrow.setBackgroundResource(R.drawable.item_spread_off_ico);
                holder.mainOptionLayer.setBackgroundResource(R.drawable.rect_gray_bg);
            }
            holder.recyclerBackground.setVisibility(View.GONE);
        }

        if ( option.getSelectedSubOption() != null ) {
            holder.optionName.setText(option.getSelectedSubOption().getOptionname());
        }

        holder.mainOptionLayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( position > 0 ) {
                    DetailSubOption prevSelectedSubOption = mainOptionList.get(position - 1 ).getSelectedSubOption();
                    if ( prevSelectedSubOption == null ) {
                        return;
                    } else {
                        String key  = prevSelectedSubOption.getOptionname();
                        if (TextUtils.isEmpty(key) )
                            return;
                    }
                }
                if ( holder.recyclerBackground.getVisibility() == View.VISIBLE ) {
                    option.setOpen(false);
                    mainOptionList.set(position, option);
                    holder.downArrow.setBackgroundResource(R.drawable.item_spread_off_ico);
                    holder.mainOptionLayer.setBackgroundResource(R.drawable.rect_gray_bg);
                    holder.recyclerBackground.setVisibility(View.GONE);
                } else {
                    option.setOpen(true);
                    mainOptionList.set(position, option);
                    holder.downArrow.setBackgroundResource(R.drawable.item_fold_ico);
                    holder.mainOptionLayer.setBackgroundResource(R.drawable.rect_purple_bg);
                    holder.recyclerBackground.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void bindOptionItemViewHolder(OptionItemViewHolder holder, int position) {
        Logger.e("position !!!!! :: " + position);
        if ( selectedFullOptionList != null && selectedFullOptionList.size() > 0 ) {
            DetailOptionItem item = selectedFullOptionList.get(position);
            int selectedQuantity = item.getSelectedQuantity();
            double price = selectedQuantity * item.getPrice();
            Logger.e("selectedQuantity = " + selectedQuantity);
            Logger.e("item price :: " + item.getPrice());
            Logger.e("price :: " + price);
            String strPrice = Utils.ToNumFormat(price) + "원";
            String option =item.getName().replace("^|^", " / ");
            String optionStr = "<font color=\"#9f56f2\"> (" + option + ")</font>";

            holder.price.setText(strPrice);
            holder.selectedQuantity.setText("" + selectedQuantity);
            holder.remainQuantity.setText(item.getQuantity() + "개 남음");
            if (TextUtils.isEmpty(option) )
                holder.nameOption.setText(productName);
            else
                holder.nameOption.setText(Html.fromHtml(productName + optionStr));

            if ( position == (selectedFullOptionList.size() - 1) ) {
                holder.botSpace.setVisibility(View.VISIBLE);
            } else {
                holder.botSpace.setVisibility(View.GONE);
            }
            boolean visibleDel = false;
            if  (mainOptionList == null ) {
                visibleDel = true;
            } else {
                if ( mainOptionList.size() <= 0 ) {
                    visibleDel = true;
                } else {
                    visibleDel = false;
                }
            }

            if ( visibleDel ) {
                holder.btnDel.setVisibility(View.GONE);
                int padding = Utils.ConvertDpToPx(context, 10);
                holder.price.setPadding(0, 0, padding, 0);
            } else {
                holder.btnDel.setVisibility(View.VISIBLE);
            }

            holder.btnDel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Logger.e("holder.btnDel Clicked");
                    if ( selectedFullOptionList != null ) {
                        selectedFullOptionList.remove(position);
                        sendTotalValue();
//                        optionItemViewHolder.adapter.setItem(selectedFullOptionList);
                        notifyDataSetChanged();
                    }
                }
            });

            holder.btnMinus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Logger.e("holder.btnMinus Clicked");
                    if ( selectedFullOptionList != null ) {
                        Logger.e("selectedFullOptionList not null");
                        DetailOptionItem item = selectedFullOptionList.get(position);
                        int selectedQuantity = item.getSelectedQuantity();
                        if ( selectedQuantity <= 1 ) {
                            new DetailDialog(context, "알림", "수량을 최소 1개 이상 선택하셔야 합니다.", new DetailDialog.CancelRequestDialogListener() {
                                @Override
                                public void onDismiss() {

                                }
                            }).show();
                        } else {
                            selectedQuantity = selectedQuantity - 1;
                            item.setSelectedQuantity(selectedQuantity);
                            selectedFullOptionList.set(position, item);
                            sendTotalValue();
                            notifyDataSetChanged();
                        }
                    }
                }
            });

            holder.btnPlus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Logger.e("holder.btnPlus Clicked");
                    if ( selectedFullOptionList != null ) {
                        DetailOptionItem item = selectedFullOptionList.get(position);
                        int remained = item.getQuantity();
                        int selectedQuantity = item.getSelectedQuantity();
                        if ( (selectedQuantity + 1) > remained ) {
                            new DetailDialog(context, "재고부족", context.getResources().getString(R.string.msg_not_enough_quantity), new DetailDialog.CancelRequestDialogListener() {
                                @Override
                                public void onDismiss() {

                                }
                            }).show();
                        } else {
                            selectedQuantity = selectedQuantity + 1;
                            item.setSelectedQuantity(selectedQuantity);
                            selectedFullOptionList.set(position, item);
                            sendTotalValue();
                            notifyDataSetChanged();
                        }
                    }
                }
            });
        }

//        boolean hasNoOption = false;
//        if ( mainOptionList == null ) {
//            hasNoOption = true;
//        } else {
//            if ( mainOptionList.size() <= 0 )
//                hasNoOption = true;
//        }
//        Logger.e("hasNoOption :: " + hasNoOption);
//        if ( hasNoOption )
//            holder.adapter.setItem(selectedFullOptionList);
    }

    public class OptionViewHolder extends RecyclerView.ViewHolder {
        public AppCompatTextView optionName;
        public AppCompatImageView downArrow;
        public RelativeLayout mainOptionLayer, recyclerBackground;
        public RecyclerView optionRecyclerView;
        public View botLine, empty, botEmpty;
        public DetailMainOptionAdapter adapter;
        public DetailOptionList selectedOption;
        public DetailSubOption selectedSubOption;
        public OptionViewHolder(Context context, View itemView) {
            super(itemView);
            mainOptionLayer = itemView.findViewById(R.id.mainOptionLayer);
            recyclerBackground = itemView.findViewById(R.id.recyclerBackground);
            optionName = itemView.findViewById(R.id.optionName);
            downArrow = itemView.findViewById(R.id.downArrow);
            optionRecyclerView = itemView.findViewById(R.id.optionRecyclerView);
            botLine = itemView.findViewById(R.id.botLine);
            empty = itemView.findViewById(R.id.empty);
            botEmpty = itemView.findViewById(R.id.botEmpty);
            adapter = new DetailMainOptionAdapter(context, new DetailMainOptionAdapter.Listener() {
                @Override
                public void onItemClck(DetailOptionList sOption, DetailSubOption item, int position) {
                    selectedOption = sOption;
                    selectedSubOption = item;
                    downArrow.setBackgroundResource(R.drawable.item_spread_off_ico);
                    mainOptionLayer.setBackgroundResource(R.drawable.rect_gray_bg);
                    recyclerBackground.setVisibility(View.GONE);
                    for ( int i = 0 ; i < mainOptionList.size() ; i ++ ) {
                        DetailOptionList option = mainOptionList.get(i);
                        if ( option.getKey().equals(selectedOption.getKey()) ) {
                            option.setSelectedSubOption(item);
                            option.setOpen(false);
                            mainOptionList.set(i, option);
                        }
                    }

                    boolean allSelected = true;
                    for ( int i = 0 ; i < mainOptionList.size() ; i ++ ) {
                        DetailSubOption selectedSubOption = mainOptionList.get(i).getSelectedSubOption();
                        if ( selectedSubOption == null ) {
                            allSelected = false;
                        } else {
                            String key = selectedSubOption.getOptionkey();
                            if ( TextUtils.isEmpty(key) )
                                allSelected = false;
                        }
                    }

                    if ( allSelected ) {
                        // 선택된 옵션 add 해야함
                        String optionStr = "";
                        StringBuilder builder = new StringBuilder();
                        for ( int i = 0 ; i < mainOptionList.size() ; i ++ ) {
                            DetailOptionList option = mainOptionList.get(i);
                            builder.append(option.getSelectedSubOption().getOptionkey());
                            if ( i != mainOptionList.size() - 1 )
                                builder.append(",");
                            option.setSelectedSubOption(null);
                            option.setOpen(false);
                            mainOptionList.set(i, option);
                        }
                        optionStr = builder.toString();
                        Logger.e("optionStr : " + optionStr);

                        if ( fullOptionList != null ) {
                            Logger.e("fullOptionList != null ");
                            if ( selectedFullOptionList != null && selectedFullOptionList.size() > 0 ) {
                                Logger.e("selectedOption not null");
                                boolean hasMatchedOption = false;
                                for ( int i = 0 ; i < selectedFullOptionList.size() ; i ++ ) {
                                    DetailOptionItem fOptionItem = selectedFullOptionList.get(i);
                                    String fItemOptionKey = fOptionItem.getOptionkey();
                                    Logger.e("optionStr :: " + optionStr);
                                    Logger.e("fItemOptionKey :: " + fItemOptionKey);
                                    Logger.e("************");
                                    if ( optionStr.equals(fItemOptionKey) ) {
                                        if ( !fOptionItem.isbSoldout() ) {
                                            int quantity = fOptionItem.getQuantity();
                                            int selectedQuantity = fOptionItem.getSelectedQuantity();
                                            if ( (selectedQuantity + 1) <= quantity ) {
                                                selectedQuantity = selectedQuantity + 1;
                                                fOptionItem.setSelectedQuantity(selectedQuantity);
                                                selectedFullOptionList.set(i, fOptionItem);
                                                sendTotalValue();
//                                            optionItemViewHolder.adapter.setItem(selectedFullOptionList);
                                                Logger.e("set item 1");
                                                hasMatchedOption = true;
                                            }
                                        }
                                    }
                                }
                                if ( !hasMatchedOption ) {
                                    for ( int i = 0 ; i < fullOptionList.size() ; i ++ ) {
                                        DetailOptionItem fOptionItem = fullOptionList.get(i);
                                        int quantity = fOptionItem.getQuantity();
                                        int selectedQuantity = fOptionItem.getSelectedQuantity();
                                        if ( quantity > 0 && !fOptionItem.isbSoldout() ) {
                                            String fItemOptionKey = fOptionItem.getOptionkey();
                                            Logger.e("optionStr ::: " + optionStr);
                                            Logger.e("fItemOptionKey ::: " + fItemOptionKey);
                                            Logger.e("************");
                                            if ( optionStr.equals(fItemOptionKey) ) {
                                                selectedQuantity = selectedQuantity + 1;
                                                fOptionItem.setSelectedQuantity(selectedQuantity);
                                                selectedFullOptionList.add(fOptionItem);
                                                sendTotalValue();
//                                                optionItemViewHolder.adapter.setItem(selectedFullOptionList);
                                                Logger.e("set item 2");
                                            }
                                        }
                                    }
                                }
                            } else {
                                selectedFullOptionList = new ArrayList<>();
                                for ( int i = 0 ; i < fullOptionList.size() ; i ++ ) {
                                    DetailOptionItem fOptionItem = fullOptionList.get(i);
                                    int quantity = fOptionItem.getQuantity();
                                    int selectedQuantity = fOptionItem.getSelectedQuantity();
                                    if ( quantity > 0 && !fOptionItem.isbSoldout() ) {
                                        String fItemOptionKey = fOptionItem.getOptionkey();
                                        if ( optionStr.equals(fItemOptionKey) ) {
                                            selectedQuantity = selectedQuantity + 1;
                                            fOptionItem.setSelectedQuantity(selectedQuantity);
                                            selectedFullOptionList.add(fOptionItem);
                                            sendTotalValue();
                                        }
                                    }
                                }
                            }
                        }
                    }
                    notifyDataSetChanged();
                }
            });

            WrappedLinearLayoutManager layoutManager = new WrappedLinearLayoutManager(context);
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            optionRecyclerView.setHasFixedSize(false);
            optionRecyclerView.setLayoutManager(layoutManager);
            optionRecyclerView.setAdapter(adapter);
        }
    }

    public class OptionItemViewHolder extends RecyclerView.ViewHolder {
        public RelativeLayout btnMinus, btnPlus, btnDel;
        public AppCompatTextView selectedQuantity, remainQuantity, price, nameOption;
        public View botSpace;
        public OptionItemViewHolder(Context context, View itemView) {
            super(itemView);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            btnDel = itemView.findViewById(R.id.btnDel);
            selectedQuantity = itemView.findViewById(R.id.selectedQuantity);
            remainQuantity = itemView.findViewById(R.id.remainQuantity);
            price = itemView.findViewById(R.id.price);
            nameOption = itemView.findViewById(R.id.nameOption);
            botSpace = itemView.findViewById(R.id.botSpace);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mainOptionList != null && mainOptionList.size() > 0) {
            if (position < mainOptionList.size()) {
                return TYPE_OPTION;
            } else {
                return TYPE_ITEM;
            }
        } else {
            return TYPE_ITEM;
        }
    }

    @Override
    public int getItemCount() {
        return mainOptionList.size() + selectedFullOptionList.size();
    }

    public void setOptionLists(ArrayList<DetailOptionList> mainList, ArrayList<DetailOptionItem> optionList) {
        if ( mainOptionList == null ) {
            mainOptionList = new ArrayList<>();
        } else {
            mainOptionList.clear();
        }

        if ( fullOptionList == null ) {
            fullOptionList = new ArrayList<>();
        } else {
            fullOptionList.clear();
        }
        mainOptionList.addAll(mainList);
        fullOptionList.addAll(optionList);
        if ( mainOptionList.size() <= 0 ) {
            Logger.e(" init selectedFullOptionList");
            selectedFullOptionList = new ArrayList<>();
            selectedFullOptionList.add(fullOptionList.get(0));
            sendTotalValue();
        }
        Logger.e("setMainOptionList size :: " + mainOptionList.size());
        Logger.e("setFullOptionList size :: " + fullOptionList.size());

        notifyDataSetChanged();
    }

    private void sendTotalValue() {
        if ( selectedFullOptionList != null ) {
            totalQuantity  = 0;
            totalPrice = 0;
            for ( int i = 0 ; i < selectedFullOptionList.size() ; i ++ ) {
                DetailOptionItem item = selectedFullOptionList.get(i);
                int quantity = item.getSelectedQuantity();
                double price = item.getPrice() * quantity;

                totalQuantity = totalQuantity + quantity;
                totalPrice = totalPrice + price;
            }
            String sTotalQuantity = "총 " + totalQuantity + "개의 상품";
            String sTotalPrice = Utils.ToNumFormat(totalPrice) + "원";
            if ( listener != null )
                listener.setTotalValue(sTotalQuantity, sTotalPrice);
        }
    }

    public ArrayList<DetailOptionItem> getSelectedList() {
        return selectedFullOptionList;
    }
}
