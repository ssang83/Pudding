package com.enliple.pudding.fragment.my;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.enliple.pudding.R;
import com.enliple.pudding.adapter.my.CookieReceivedListAdapter;
import com.enliple.pudding.commons.log.Logger;
import com.enliple.pudding.commons.network.vo.API51;
import com.enliple.pudding.commons.widget.recyclerview.WrappedLinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

public class CookieReceivedListFragment extends Fragment {
    public static final int CHECK_ALL = 0;
    public static final int CHECK_LIVE = 1;
    public static final int CHECK_VOD = 2;

    private WrappedLinearLayoutManager linearLayoutManager;
    private RelativeLayout checkAll, checkLive, checkVOD;
    private AppCompatTextView imgAll, imgLive, imgVOD, textViewEmpty;
    private RecyclerView recyclerView;
    private CookieReceivedListAdapter adapter;

    private int currentPosition = -1;
    private List<API51.Data> datum;
    private List<API51.Data> vod_datum;
    private List<API51.Data> live_datum;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cookie_received_list, container, false);
        Logger.e("CookieReceivedListFragment onCreateView");
        currentPosition = -1;
        recyclerView = view.findViewById(R.id.list);
        checkAll = view.findViewById(R.id.checkAll);
        checkLive = view.findViewById(R.id.checkLive);
        checkVOD = view.findViewById(R.id.checkVOD);
        imgAll = view.findViewById(R.id.imgAll);
        imgLive = view.findViewById(R.id.imgLive);
        imgVOD = view.findViewById(R.id.imgVOD);

        checkAll.setOnClickListener(clickListener);
        checkLive.setOnClickListener(clickListener);
        checkVOD.setOnClickListener(clickListener);

        linearLayoutManager = new WrappedLinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setHasFixedSize(false);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(linearLayoutManager);

        adapter = new CookieReceivedListAdapter(getActivity());
        recyclerView.setAdapter(adapter);


        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Logger.e("CookieReceivedListFragment onViewCreated");
    }

    private void setSelect(int position) {
        Logger.e("currentPosition 2222 :: " + currentPosition);
        if (currentPosition == position)
            return;
        currentPosition = position;
        if (position == CHECK_ALL) {
            imgAll.setBackgroundResource(R.drawable.radio_btn_on);
            imgLive.setBackgroundResource(R.drawable.radio_btn_off);
            imgVOD.setBackgroundResource(R.drawable.radio_btn_off);
            if (datum != null && datum.size() > 0) {
                adapter.setItems(datum);
            } else {
                adapter.clearItems();
            }
        } else if (position == CHECK_LIVE) {
            imgAll.setBackgroundResource(R.drawable.radio_btn_off);
            imgLive.setBackgroundResource(R.drawable.radio_btn_on);
            imgVOD.setBackgroundResource(R.drawable.radio_btn_off);
            if (live_datum != null && live_datum.size() > 0) {
                adapter.setItems(live_datum);
            } else {
                adapter.clearItems();
            }
        } else if (position == CHECK_VOD) {
            imgAll.setBackgroundResource(R.drawable.radio_btn_off);
            imgLive.setBackgroundResource(R.drawable.radio_btn_off);
            imgVOD.setBackgroundResource(R.drawable.radio_btn_on);
            if (vod_datum != null && vod_datum.size() > 0) {
                adapter.setItems(vod_datum);
            } else {
                adapter.clearItems();
            }
        }
//        loadData(makeJSON());


    }

    public void loadData(List<API51.Data> datum) {
        this.datum = datum;
        if (datum != null && datum.size() > 0) {
            textViewEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            live_datum = new ArrayList<API51.Data>();
            vod_datum = new ArrayList<API51.Data>();
            for (int i = 0; i < datum.size(); i++) {
                if ("vod".equals(datum.get(i).vod_type)) {
                    vod_datum.add(datum.get(i));
                } else if ("live".equals(datum.get(i).vod_type)) {
                    live_datum.add(datum.get(i));
                }
            }
        } else {
            textViewEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
        setSelect(CHECK_ALL);
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.checkAll:
                    setSelect(CHECK_ALL);
                    break;
                case R.id.checkLive:
                    setSelect(CHECK_LIVE);
                    break;
                case R.id.checkVOD:
                    setSelect(CHECK_VOD);
                    break;
            }
        }
    };

//    private void loadData(String jsonStr) {
//        try {
//            JSONObject object = new JSONObject(jsonStr);
//            JSONArray array = object.optJSONArray("list");
//            if ( array != null && array.length() > 0 ) {
//                ArrayList<CookieReceivedListModel> modelArray = new ArrayList<>();
//                for ( int i = 0 ; i < array.length() ; i ++ ) {
//                    JSONObject subObj = array.getJSONObject(i);
//                    CookieReceivedListModel model = new CookieReceivedListModel();
//                    if ( subObj != null ) {
//                        model.setDate(subObj.optString("date"));
//                        model.setFanName(subObj.optString("fan_name"));
//                        model.setCookieNo(subObj.optString("cookie_no"));
//                        model.setDueDate(subObj.optString("due_date"));
//                        modelArray.add(model);
//                    }
//                }
//
//                adapter.setItems(modelArray);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    private String makeJSON() {
//        try {
//            JSONObject object = new JSONObject();
//            JSONArray array = new JSONArray();
//            for (int i = 0 ; i < 30 ; i ++ ) {
//                JSONObject subObj = new JSONObject();
//                String date = "2018.10.29\n22:31";
//                String fanName = "보낸언냐\n(kkss***)";
//                String cookieNo = "100개";
//                String dueDate = "2023.08.13";
//                subObj.put("date", date);
//                subObj.put("fan_name", fanName);
//                subObj.put("cookie_no", cookieNo);
//                subObj.put("due_date", dueDate);
//                array.put(subObj);
//            }
//            object.put("list", array);
//            return object.toString();
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
}
