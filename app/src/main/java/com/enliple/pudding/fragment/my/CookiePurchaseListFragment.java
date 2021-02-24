package com.enliple.pudding.fragment.my;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.enliple.pudding.R;
import com.enliple.pudding.adapter.my.CookiePurchaseListAdapter;
import com.enliple.pudding.commons.network.vo.API51;
import com.enliple.pudding.commons.widget.recyclerview.WrappedLinearLayoutManager;

import java.util.List;

public class CookiePurchaseListFragment extends Fragment {

    private WrappedLinearLayoutManager linearLayoutManager;
    private RecyclerView recyclerView;
    private AppCompatTextView textViewEmpty;
    private CookiePurchaseListAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cookie_purchase_list, container, false);


        recyclerView = view.findViewById(R.id.list);
        textViewEmpty = view.findViewById(R.id.textViewEmpty);

        linearLayoutManager = new WrappedLinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setHasFixedSize(false);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(linearLayoutManager);

        adapter = new CookiePurchaseListAdapter(getActivity());
        recyclerView.setAdapter(adapter);

//        loadData(makeJSON());

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void loadData(List<API51.Data> data) {
        if (data.size() > 0) {
            recyclerView.setVisibility(View.VISIBLE);
            textViewEmpty.setVisibility(View.GONE);
            adapter.setItems(data);
        } else {
            recyclerView.setVisibility(View.GONE);
            textViewEmpty.setVisibility(View.VISIBLE);
        }
    }


//    private void loadData(String jsonStr) {
//        try {
//            JSONObject object = new JSONObject(jsonStr);
//            JSONArray array = object.optJSONArray("list");
//            if ( array != null && array.length() > 0 ) {
//                ArrayList<CookiePurchaseListModel> modelArray = new ArrayList<>();
//                for ( int i = 0 ; i < array.length() ; i ++ ) {
//                    JSONObject subObj = array.getJSONObject(i);
//                    CookiePurchaseListModel model = new CookiePurchaseListModel();
//                    if ( subObj != null ) {
//                        model.setDate(subObj.optString("date"));
//                        model.setQuantity(subObj.optString("quantity"));
//                        model.setMeans(subObj.optString("means"));
//                        model.setPrice(subObj.optString("price"));
//                        modelArray.add(model);
//                    }
//                }
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
//                String quantity = 6*i + "개";
//                String means;
//                if ( i%3 == 0 ) {
//                    means = "신용카드";
//                } else if ( i%3 == 1 ) {
//                    means = "계좌이체";
//                } else {
//                    means = "휴대폰결제";
//                }
//                String price = "110,000원";
//                subObj.put("date", date);
//                subObj.put("quantity", quantity);
//                subObj.put("means", means);
//                subObj.put("price", price);
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
