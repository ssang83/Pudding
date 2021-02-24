package com.enliple.pudding.widget.shoptree;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.AppCompatTextView;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.view.Window;
import android.widget.RelativeLayout;

import com.enliple.pudding.R;

/**
 * Created by Kim Joonsung on 2018-09-27.
 */

public class DeliveryBasicInfoDialog extends Dialog implements View.OnClickListener {
    private AppCompatTextView textViewRecipient;
    private AppCompatTextView textViewCompany;
    private AppCompatTextView textViewInvoiceNumber;
    private RelativeLayout buttonClose;

    private Context mContext;
    private String deliveryName, deliveryCompany, deliveryCode, delvieryUrl;

    public DeliveryBasicInfoDialog(@NonNull Context context, String name, String company, String code, String url) {
        super(context);
        this.mContext = context;
        this.deliveryName = name;
        this.deliveryCompany = company;
        this.deliveryCode = code;
        this.delvieryUrl = url;

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.dialog_delivery_basic_info);

        textViewRecipient = findViewById(R.id.textViewRecipient);
        textViewCompany = findViewById(R.id.textViewCompany);
        textViewInvoiceNumber = findViewById(R.id.textViewInvoiceNumber);
        textViewInvoiceNumber.setOnClickListener(this);
        buttonClose = findViewById(R.id.buttonClose);
        buttonClose.setOnClickListener(this);

        loadData();
    }

    @Override
    public void onClick(View v) {
        long viewId = v.getId();
        if (viewId == R.id.buttonClose) {
            dismiss();
        } else if (viewId == R.id.textViewInvoiceNumber) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(delvieryUrl));
            mContext.startActivity(intent);
            dismiss();
        }
    }

    public void dialogShow(Fragment fragment) {
        if (!this.isShowing() && fragment != null && fragment.isAdded()) {
            show();
        }
    }

    public void dialogShow(Activity activity) {
        if (!this.isShowing() && activity != null && !activity.isFinishing()) {
            show();
        }
    }

    private void loadData() {
        textViewRecipient.setText(deliveryName);
        if ( deliveryCompany != null ) {
            SpannableString company = new SpannableString(deliveryCompany);
            company.setSpan(new UnderlineSpan(), 0, deliveryCompany.length(), 0);
            textViewCompany.setText(company);
        }

        if(deliveryCode != null) {
            SpannableString invoice = new SpannableString(deliveryCode);
            invoice.setSpan(new UnderlineSpan(), 0, deliveryCode.length(), 0);
            textViewInvoiceNumber.setText(invoice);
        }
    }
}
