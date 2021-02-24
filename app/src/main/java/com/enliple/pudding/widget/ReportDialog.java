package com.enliple.pudding.widget;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.enliple.pudding.R;
import com.enliple.pudding.adapter.shoptree.PhotoReviewReportAdapter;
import com.enliple.pudding.commons.app.SoftKeyboardUtils;
import com.enliple.pudding.commons.db.DBManager;
import com.enliple.pudding.commons.internal.AppPreferences;
import com.enliple.pudding.commons.log.Logger;
import com.enliple.pudding.commons.network.NetworkApi;
import com.enliple.pudding.commons.network.NetworkBus;
import com.enliple.pudding.commons.network.NetworkBusResponse;
import com.enliple.pudding.commons.network.vo.API42;
import com.enliple.pudding.commons.network.vo.API43;
import com.enliple.pudding.commons.widget.recyclerview.WrappedLinearLayoutManager;
import com.enliple.pudding.model.PhotoReviewReportData;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.RequestBody;

public class ReportDialog extends Dialog {

    private Context context;
    private AppCompatButton btnCancel, btnReport;
    private RecyclerView recyclerView;
    private AppCompatEditText edit;
    private PhotoReviewReportAdapter adapter;
    private WrappedLinearLayoutManager layoutManager;
    private ArrayList<PhotoReviewReportData> mItems;
    private PhotoReviewReportData selectedItem;
    private String is_id;
    private String type;
    private View viewLine;

    private View.OnFocusChangeListener focusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if(hasFocus) {
                viewLine.setBackgroundColor(Color.parseColor("#9f56f2"));
                SoftKeyboardUtils.Companion.showKeyboard(edit);
            } else {
                viewLine.setBackgroundColor(Color.parseColor("#bcc6d2"));
                SoftKeyboardUtils.Companion.hideKeyboard(edit);
            }
        }
    };

    public ReportDialog(Context context, String is_id) {
        super(context);
        this.is_id = is_id;
        this.context = context;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.report_dialog);

        EventBus.getDefault().register(this);
        NetworkBus bus = new NetworkBus(NetworkApi.API42.name());
        EventBus.getDefault().post(bus);

        adapter = new PhotoReviewReportAdapter(context);
        adapter.setCallback(new PhotoReviewReportAdapter.OnCallback() {
            @Override
            public void onSelected(PhotoReviewReportData selected) {
                selectedItem = selected;
                type = selectedItem.getKey();
                edit.setText("");
                if (mItems != null && mItems.size() > 0) {
                    String lastGubun = mItems.get(mItems.size() - 1).getGubun();
                    if (lastGubun.equals(selectedItem.getGubun())) {
                        edit.setEnabled(true);
                    } else {
                        edit.setEnabled(false);
                    }
                }
            }
        });
        recyclerView = findViewById(R.id.recyclerView);
        edit = findViewById(R.id.edit);
        edit.setOnFocusChangeListener(focusChangeListener);

        btnCancel = findViewById(R.id.btnCancel);
        btnReport = findViewById(R.id.btnReport);
        viewLine = findViewById(R.id.viewLine);

        btnCancel.setOnClickListener(clickListener);
        btnReport.setOnClickListener(clickListener);

        layoutManager = new WrappedLinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setHasFixedSize(false);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

//        setData();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        Logger.e("onDetachedFromWindow");
        EventBus.getDefault().unregister(this);
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnCancel:
                    dismiss();
                    break;
                case R.id.btnReport:
                    String message = edit.getText().toString();
                    HashMap<String, String> map = new HashMap<>();
                    map.put("is_id", is_id);
                    map.put("type", type);
                    map.put("comment", message);
                    map.put("user", AppPreferences.Companion.getUserId(context));

                    RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), (new JSONObject(map)).toString());

                    NetworkBus bus = new NetworkBus(NetworkApi.API43.name(), body);
                    EventBus.getDefault().post(bus);

//                    if ( !TextUtils.isEmpty(message) )
//                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(NetworkBusResponse data) {
        String dataKey = "GET/review/report_type";
        String reportKey = "POST/review/report";
        Logger.e("key check" + data.arg1);
        if (data.arg1.equals(dataKey)) {
            mItems = new ArrayList<PhotoReviewReportData>();
            String str = DBManager.getInstance(context).get(dataKey);
            API42 response = new Gson().fromJson(str, API42.class);
            if (response != null && response.data.size() > 0) {
                PhotoReviewReportData reportData;
                for (int i = 0; i < response.data.size(); i++) {
                    reportData = new PhotoReviewReportData();
                    reportData.setSelected(false);
                    reportData.setGubun(response.data.get(i).value);
                    reportData.setKey(response.data.get(i).key);
                    mItems.add(reportData);
                }

                adapter.setItems(mItems);

                selectedItem = mItems.get(0);
                type = selectedItem.getKey();
                edit.setEnabled(false);
            }
        } else if (data.arg1.equals(reportKey)) {
            String str = DBManager.getInstance(context).get(reportKey);
            API43 response = new Gson().fromJson(str, API43.class);
            String result = response.result;
            if ("success".equals(result)) {
                dismiss();
            } else {
                String message = response.message;
                if (!TextUtils.isEmpty(message)) {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
