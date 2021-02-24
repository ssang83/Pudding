package com.enliple.pudding.widget;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.enliple.pudding.R;
import com.enliple.pudding.adapter.CustomSpinnerAdapter;
import com.enliple.pudding.commons.widget.recyclerview.WrappedLinearLayoutManager;
import com.enliple.pudding.model.SpinnerSelectModel;

import java.util.List;

public class SpinnerListDialog extends Dialog {

    private Listener listener;
    private Context context;
    private String strTitle;
    private List<SpinnerSelectModel> category;
    private int dialogWidth = 0;
    private String selectedCategory = "";

    private AppCompatTextView title, buttonCancel, buttonOk;
    private RecyclerView list;
    private WrappedLinearLayoutManager layoutManager;
    private CustomSpinnerAdapter adapter;
    public interface Listener {
        void onConfirm(String category);
        void cancel();
    }

    public SpinnerListDialog(@NonNull Context context, int dialogWidth, String strTitle, List<SpinnerSelectModel> category, Listener listener) {
        super(context);
        this.context = context;
        this.dialogWidth = dialogWidth;
        this.category = category;
        this.listener = listener;
        this.strTitle = strTitle;

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        View v = LayoutInflater.from(context).inflate(R.layout.spinner_dialog, null, false);
        setContentView(v);

        list = v.findViewById(R.id.list);
        layoutManager = new WrappedLinearLayoutManager(context);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        list.setHasFixedSize(false);
        list.setLayoutManager(layoutManager);

        adapter = new CustomSpinnerAdapter(context, category, new CustomSpinnerAdapter.Listener() {
            @Override
            public void onItemClicked(String category) {
                selectedCategory = category;
            }
        });

        list.setAdapter(adapter);

        title = v.findViewById(R.id.title);
        buttonCancel = v.findViewById(R.id.buttonCancel);
        buttonOk = v.findViewById(R.id.buttonOk);
        buttonCancel.setOnClickListener(clickListener);
        buttonOk.setOnClickListener(clickListener);

        title.setText(strTitle);
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if ( v.getId() == R.id.buttonCancel ) {
                dismiss();
            } else if ( v.getId() == R.id.buttonOk ) {
                if (TextUtils.isEmpty(selectedCategory) ) {
                    Toast.makeText(context, "사유를 선택해주세요", Toast.LENGTH_SHORT).show();
                } else {
                    if ( listener != null ) {
                        listener.onConfirm(selectedCategory);
                    }
                    dismiss();
                }
            }
        }
    };
}
