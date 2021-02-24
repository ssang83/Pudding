package com.enliple.pudding.fragment.shoptree;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.enliple.pudding.R;
import com.enliple.pudding.commons.events.OnSingleClickListener;
import com.enliple.pudding.model.SellerInfo;

/**
 * Created by Kim Joonsung on 2018-05-30.
 */

public class SalerInfoFragment extends Fragment {

    private AppCompatTextView textViewPhoneNumber;
    private AppCompatTextView textViewAddress;
    private AppCompatTextView textViewDeliveryGuide;
    private AppCompatTextView textViewExchangeGuide;
    private AppCompatButton buttonCall;
    private SellerInfo mInfo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_saler_info, container, false);
        initViews(view);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle != null) {
            mInfo = (SellerInfo) bundle.getSerializable("CELLER_INFO");
        }



        loadData();
    }

    private void loadData() {
        if (mInfo != null) {
            textViewDeliveryGuide.setText(mInfo.getDeliveryInfo());
            textViewExchangeGuide.setText(mInfo.getExchangeinfo());
            textViewPhoneNumber.setText(mInfo.getPhone());
            textViewAddress.setText(mInfo.getAddress());
        }
    }

    private void initViews(View v) {
        textViewPhoneNumber = v.findViewById(R.id.textViewPhoneNumber);
        textViewAddress = v.findViewById(R.id.textViewAddress);
        textViewDeliveryGuide = v.findViewById(R.id.textViewDeliveryGuide);
        textViewExchangeGuide = v.findViewById(R.id.textViewExchangeGuide);
        buttonCall = v.findViewById(R.id.buttonCall);

        buttonCall.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + textViewPhoneNumber.getText().toString()));
                startActivity(intent);
            }
        });
    }
}
