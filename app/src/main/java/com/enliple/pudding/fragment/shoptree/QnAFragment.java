
package com.enliple.pudding.fragment.shoptree;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.enliple.pudding.R;
import com.enliple.pudding.adapter.shoptree.QnaAdapter;
import com.enliple.pudding.bus.QnABus;
import com.enliple.pudding.commons.db.DBManager;
import com.enliple.pudding.commons.events.OnSingleClickListener;
import com.enliple.pudding.commons.log.Logger;
import com.enliple.pudding.commons.network.NetworkApi;
import com.enliple.pudding.commons.network.NetworkBus;
import com.enliple.pudding.commons.network.NetworkBusResponse;
import com.enliple.pudding.commons.network.NetworkHandler;
import com.enliple.pudding.commons.network.vo.API91;
import com.enliple.pudding.commons.network.vo.API93;
import com.enliple.pudding.commons.widget.recyclerview.WrappedLinearLayoutManager;
import com.enliple.pudding.widget.NothingSelectedSpinnerAdapter;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

/**
 * Created by Kim Joonsung on 2018-05-30.
 */

public class QnAFragment extends Fragment implements QnaAdapter.AdapterListener {

    private RecyclerView recyclerVeiwQnA;
    private AppCompatImageButton buttonSecretCheck;
    private Spinner askingSpinner;
    private AppCompatTextView textViewAskingType;
    private AppCompatTextView textViewEmpty;

    WrappedLinearLayoutManager linearLayoutManager;
    QnaAdapter qnaAdapter;
    NothingSelectedSpinnerAdapter mAdapter;

    ArrayList<String> mSpinnerData = new ArrayList<>();
    ArrayList<API93.QnaType> qnaData = new ArrayList<>();

    String productId, qnaType, API91, API91_2, API91_3;
    boolean isLoadMore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_qna, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        EventBus.getDefault().register(this);

        if (getArguments() != null) {
            productId = getArguments().getString("idx");
        }

        recyclerVeiwQnA = view.findViewById(R.id.recyclerVeiwQnA);
        textViewAskingType = view.findViewById(R.id.textViewAskingType);
        textViewEmpty = view.findViewById(R.id.textViewEmpty);

        askingSpinner = view.findViewById(R.id.askingSpinner);
        askingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                textViewAskingType.setVisibility(View.GONE);

                if (position > 0) {
                    qnaType = String.valueOf(qnaData.get(position - 1).key);

                    NetworkBus bus = new NetworkBus(NetworkApi.API91_2.name(), productId, qnaType);
                    EventBus.getDefault().post(bus);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        buttonSecretCheck = view.findViewById(R.id.buttonSecretCheck);
        buttonSecretCheck.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                v.setSelected(!v.isSelected());
                if (v.isSelected()) { // 비밀글 제외
                    NetworkBus bus = new NetworkBus(NetworkApi.API91_3.name(), productId, "1");
                    EventBus.getDefault().post(bus);
                } else {
                    NetworkBus bus = new NetworkBus(NetworkApi.API91_3.name(), productId, "0");
                    EventBus.getDefault().post(bus);
                }
            }
        });

        linearLayoutManager = new WrappedLinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerVeiwQnA.setHasFixedSize(false);
        recyclerVeiwQnA.setNestedScrollingEnabled(false);
        recyclerVeiwQnA.setLayoutManager(linearLayoutManager);

        qnaAdapter = new QnaAdapter(getActivity());
        qnaAdapter.setAdapterListener(QnAFragment.this);
        recyclerVeiwQnA.setAdapter(qnaAdapter);

        isLoadMore = false;

        setQnaType();

//        if (getActivity() != null) {
//            API91 = NetworkHandler.Companion.getInstance(getActivity()).getKey(NetworkApi.API91.toString(), productId, "");
//        }

        buttonSecretCheck.setSelected(true);
        NetworkBus bus = new NetworkBus(NetworkApi.API91_3.name(), productId, "1");
        EventBus.getDefault().post(bus);
//        loadData(API91);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onLoadMoreClicked() {

        isLoadMore = true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(NetworkBusResponse data) {
        if (qnaType != null) {
            API91_2 = NetworkHandler.Companion.getInstance(getActivity()).getKey(NetworkApi.API91_2.toString(), productId, qnaType);
        }

        API91_3 = NetworkHandler.Companion.getInstance(getActivity()).getKey(NetworkApi.API91_3.toString(), productId, buttonSecretCheck.isSelected() ? "1" : "0");

        if (data.arg1.equals(API91_2)) {
            loadData(API91_2);
        } else if (data.arg1.equals(API91_3)) {
            loadData(API91_3);
        }
    }

    private void loadData(String key) {
        if (getContext() != null) {
            String str = DBManager.getInstance(getActivity()).get(key);
            API91 response = new Gson().fromJson(str, API91.class);

            if (response.nTotalCount > 0) {
                recyclerVeiwQnA.setVisibility(View.VISIBLE);
                textViewEmpty.setVisibility(View.GONE);

                qnaAdapter.setItems(response.data);
                qnaAdapter.setEndOfData(true);
            } else {
                recyclerVeiwQnA.setVisibility(View.GONE);
                textViewEmpty.setVisibility(View.VISIBLE);
            }

            EventBus.getDefault().post(new QnABus(0, response.nTotalCount));
        }
    }

    private void setQnaType() {
        if (getActivity() != null) {
            String key = NetworkHandler.Companion.getInstance(getActivity()).getKey(NetworkApi.API93.toString(), "", "");
            String str = DBManager.getInstance(getActivity()).get(key);

            API93 response = null;
            if (!TextUtils.isEmpty(str)) {
                response = new Gson().fromJson(str, API93.class);
            }
            if (response == null) {
                Logger.e("setQnaType response null error!! ");
                return;
            }

            for (int i = 0; i < response.data.size(); i++) {
                mSpinnerData.add(response.data.get(i).value);
            }

            qnaData.addAll(response.data);

            ArrayAdapter<String> adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_item, mSpinnerData);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            mAdapter = new NothingSelectedSpinnerAdapter(adapter, R.layout.spinner_product_qna, getActivity());
            askingSpinner.setAdapter(mAdapter);
        }
    }

    public void refreshData() {
        isLoadMore = false;
        if (getActivity() != null) {
            API91 = NetworkHandler.Companion.getInstance(getActivity()).getKey(NetworkApi.API91.toString(), productId, "");
        }

        loadData(API91);
    }

    public String getSecretStatus() {
        if(buttonSecretCheck.isSelected()) {
            return "1";
        } else {
            return "0";
        }
    }
}
