package com.enliple.pudding.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.enliple.pudding.AbsBaseActivity;
import com.enliple.pudding.PuddingApplication;
import com.enliple.pudding.R;
import com.enliple.pudding.commons.chat.ChatManager;
import com.enliple.pudding.commons.log.Logger;
import com.enliple.pudding.commons.app.ImageLoad;
import com.enliple.pudding.commons.app.NetworkStatusUtils;
import com.enliple.pudding.commons.app.Utils;
import com.enliple.pudding.commons.network.NetworkConst;
import com.enliple.pudding.commons.shoptree.network.ShopTreeAsyncTask;
import com.enliple.pudding.fragment.main.ShoppingLiveFragment;
import com.enliple.pudding.model.BuyItemModel;
import com.enliple.pudding.model.BuyModel;
import com.enliple.pudding.model.DeliveryInfo;
import com.enliple.pudding.model.ProductModel;
import com.enliple.pudding.model.PurchaseInfosModel;
import com.enliple.pudding.widget.DeliveryConditionDialog;
import com.enliple.pudding.widget.PostAddressDialog;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class PurchaseActivity extends AbsBaseActivity implements View.OnClickListener {
    private static final boolean IS_PAY_TEST = false;
    public static final int REQUEST_GO_PAYMENT = 9621;
    public static final int ACTIVITY_REQUEST_AUTH = 8518;
    public static Activity mActivity;
    public static final String EXTRA_ONLY_POINT = "EXTRA_ONLY_POINT";
    public static final String EXTRA_ORDER_NUMBER = "EXTRA_ORDER_NUMBER";
    public static final String EXTRA_L_ORDER_NUMBER = "EXTRA_ORDER_NUMBER";
    public static final String EXTRA_PRODUCT_PRICE = "EXTRA_PRODUCT_PRICE";
    public static final String EXTRA_DELIVERY_PRICE = "EXTRA_DELILVERY_PRICE";
    public static final String EXTRA_USED_POINT = "EXTRA_USED_POINT";
    public static final String EXTRA_TOTAL_PRRICE = "EXTRA_TOTAL_PRRICE";
    public static final String EXTRA_SAVE_POINT = "EXTRA_SAVE_POINT";
    public static final String EXTRA_DUE_DATE = "EXTRA_DUE_DATE";
    public static final String EXTRA_NUM_ITEMS = "EXTRA_NUM_ITEMS";
    public static final String EXTRA_PAYMENT = "EXTRA_PAYMENT";
    private static final int STATUS_BASE = 0;
    private static final int STATUS_RECENT = 1;
    private static final int STATUS_NEW = 2;

    private static final String MID = "withpangt0";
    //    private static final String MID = "INIpayTest";

    private AppCompatTextView buyerName, buyerPhone;

    private TextView textViewPostNumber;                // 우편번호
    private TextView textViewOldAddress;                // 지번주소
    private TextView textViewNewAddress;                // 도로명 주소
    private TextView textViewAvailablePoint;            // 가용 포인트
    private TextView textViewProductPrice;              // 상품금액
    private TextView textViewTotalDeliveryPrice;        // 총 배송비
    private TextView textViewPoint;                     // 포인트
    private TextView textViewTotalPaymentPrice;         // 총 결제금액

    private EditText editTextMemo;                      // 반송메모
    private EditText editTextRecipient;                 // 수령인
    private EditText editTextCellPhone;                 // 연락처
    private EditText editTextDetailAddress;             // 상세주소
    private EditText editTextPoint;                     // 사용 포인트

    private RadioGroup radioGroup;                      // 배송지 그룹
    private RadioButton radioDeliveryRecentyl;          // 최근 배송지
    private RadioButton radioDeliveryNew;               // 신규 배송지
    private RadioButton radioInputSelf;                 // 직접 입력
    private RadioGroup radioGroup1;                     // 결제수단
    private RadioButton radioCard;                      // 신용/체크 카드
    private RadioButton radioDeposit;                   // 계좌이체
    //    private RadioButton radioNoBankbook;                   // 무통장
    private RadioButton radioPhone;                   // 휴대폰 결제
    private Button buttonSearchAddress;     // 주소찾기
    private Button buttonPointUseAll;       // 포인트 전액 사용
    private Button buttonDone;              // 구매하기

    private RelativeLayout buttonBack;
    private LinearLayout mProductLayer;
    private TextView mNotiSavePoint;

    private LinearLayout mNoBankBookLayer;

    private EditText edit_depositName;
    private EditText edit_depositBank;
    private EditText edit_depositAccount;

    private AppCompatButton buttonBasicDelivery;        // 기본 배송지 설정

    private DeliveryInfo mBasicModel;
    private DeliveryInfo mRecentModel;
    private DeliveryInfo mInputModel;
    private PostAddressDialog postAddressDialog;
    private double mMyPoint;
    private double mUsePoint = 0;
    private double mTotalPrice = 0;
    private double mPurchasePrice = 0;
    private String mEdited;
    private PurchaseInfosModel mModel;
    private ArrayList<ProductModel> mProductModelArray;
    private String mPaymentStr = "";
    private int mPaymentCount = 0;
    private String mUserName;
    private double mSavePoint;
    private double mGoodsPrice;
    private double mDeliveryPrice;
    private boolean mIsDoneClicked = false;
    private int numOfItems = 0;
    private boolean isFromCart = false;
    private String orderNumber = "";
    private String stOrderNumber = "";
    private String ct_id = "";
    private String streamKey = "";
    private String vodType = "";
    private String recommend_mb_id = "";
    private String payment = "";
    private String payment_type = "";
    private boolean canPointUse = false;
    private boolean isAuth = false;
    private String body = "";
    private void initViews() {
        buyerName = findViewById(R.id.buyerName);
        buyerPhone = findViewById(R.id.buyerPhone);
        textViewPostNumber = (TextView) findViewById(R.id.textViewPostNumber);
        textViewOldAddress = (TextView) findViewById(R.id.textViewOldAddress);
        textViewNewAddress = (TextView) findViewById(R.id.textViewNewAddress);
        textViewAvailablePoint = (TextView) findViewById(R.id.textViewAvailablePoint);
        textViewProductPrice = (TextView) findViewById(R.id.textViewProductPrice);
        textViewTotalDeliveryPrice = (TextView) findViewById(R.id.textViewTotalDeliveryPrice);
        textViewPoint = (TextView) findViewById(R.id.textViewPoint);
        textViewTotalPaymentPrice = (TextView) findViewById(R.id.textViewTotalPaymentPrice);

        editTextMemo = (EditText) findViewById(R.id.editTextMemo);
        editTextRecipient = (EditText) findViewById(R.id.editTextRecipient);
        editTextCellPhone = (EditText) findViewById(R.id.editTextCellPhone);
        editTextDetailAddress = (EditText) findViewById(R.id.editTextDetailAddress);
        editTextPoint = (EditText) findViewById(R.id.editTextPoint);
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        radioDeliveryRecentyl = (RadioButton) findViewById(R.id.radioDeliveryRecentyl);
        radioDeliveryNew = (RadioButton) findViewById(R.id.radioDeliveryNew);
        radioInputSelf = (RadioButton) findViewById(R.id.radioInputSelf);
        radioGroup1 = (RadioGroup) findViewById(R.id.radioGroup1);
        radioCard = (RadioButton) findViewById(R.id.radioCard);
        radioDeposit = (RadioButton) findViewById(R.id.radioDeposit);
//        radioNoBankbook = (RadioButton) findViewById(R.id.radioNoBankbook);
        radioPhone = (RadioButton) findViewById(R.id.radioPhone);
        buttonSearchAddress = (Button) findViewById(R.id.buttonSearchAddress);
        buttonPointUseAll = (Button) findViewById(R.id.buttonPointUseAll);
        buttonPointUseAll.setClickable(false);
        buttonDone = (Button) findViewById(R.id.buttonDone);

        buttonBack = findViewById(R.id.buttonBack);

        mProductLayer = (LinearLayout) findViewById(R.id.product_layer);
        mNotiSavePoint = (TextView) findViewById(R.id.noti_save_point);

        mNoBankBookLayer = (LinearLayout) findViewById(R.id.no_bankbook_layer);

        edit_depositName = (EditText) findViewById(R.id.edit_depositName);
        edit_depositBank = (EditText) findViewById(R.id.edit_depositBank);
        edit_depositAccount = (EditText) findViewById(R.id.edit_depositAccount);
        buttonBasicDelivery = findViewById(R.id.buttonBasicDelivery);

        if (Build.VERSION.SDK_INT >= 21) {

            ColorStateList colorStateList = new ColorStateList(
                    new int[][]{

                            new int[]{-android.R.attr.state_checked}, //disabled
                            new int[]{android.R.attr.state_checked} //enabled
                    },
                    new int[]{
                            0xffbcc6d2
                            , 0xff9f56f2
                    }
            );
            radioDeliveryRecentyl.setButtonTintList(colorStateList);
            radioDeliveryRecentyl.invalidate();
            radioDeliveryNew.setButtonTintList(colorStateList);
            radioDeliveryNew.invalidate();
            radioInputSelf.setButtonTintList(colorStateList);
            radioInputSelf.invalidate();
            radioCard.setButtonTintList(colorStateList);
            radioCard.invalidate();
            radioDeposit.setButtonTintList(colorStateList);
            radioDeposit.invalidate();
            radioPhone.setButtonTintList(colorStateList);
            radioPhone.invalidate();
        }

        radioCard.setChecked(true);
        payment = PaymentActivity.INTENT_KEY_PAYMENT_CARD;
        payment_type = PaymentActivity.PAYMENT_TYPE_C;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase);
        initViews();

        mModel = new PurchaseInfosModel();

        buttonSearchAddress.setOnClickListener(this);
        buttonBack.setOnClickListener(this);
        buttonDone.setOnClickListener(this);
        buttonPointUseAll.setOnClickListener(this);
        buttonBasicDelivery.setOnClickListener(this);

        editTextPoint.addTextChangedListener(watcher);

        Intent intent = getIntent();
        body = intent.getStringExtra("PRODUCT_BODY");
        isFromCart = intent.getBooleanExtra("FROM_CART", false);
        Logger.d("body :::::: " + body);
        setData();
//        if (!TextUtils.isEmpty(body)) {
//            if ( "N".equals(PuddingApplication.getApplication().Companion.getMLoginUserData().isAuth) ) {
//                Intent it = new Intent(PurchaseActivity.this, PGAuthActivity.class);
//                it.putExtra(PGAuthActivity.INTENT_EXTRA_KEY_CALL_MODE, PGAuthActivity.INTENT_EXTRA_VALUE_MODE_IDENTIFICATION);
//                startActivityForResult(it, ACTIVITY_REQUEST_AUTH);
//            } else {
//                setData();
//            }
//        } else
//            finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void onClick(View v) {
        long viewId = v.getId();

        if (viewId == R.id.buttonBack) {
            onBackPressed();
        } else if (viewId == R.id.buttonDone) {
            if (mIsDoneClicked)
                return;

            mIsDoneClicked = true;

            String zipCode = "";
            String newAddr = "";
            String oldAddr = "";
            String detail = "";
            String deliveryName = "";
            String deliveryPhone = "";
            String memo = "";

            deliveryName = editTextRecipient.getText().toString();
            deliveryPhone = editTextCellPhone.getText().toString();
            zipCode = textViewPostNumber.getText().toString();
            oldAddr = textViewOldAddress.getText().toString();
            newAddr = textViewNewAddress.getText().toString();
            detail = editTextDetailAddress.getText().toString();
            memo = editTextMemo.getText().toString();

            if (ValidStr(deliveryName) && ValidStr(deliveryPhone) && ValidStr(zipCode) && ValidStr(oldAddr) && ValidStr(newAddr) && ValidStr(detail)) {

            } else {
                mIsDoneClicked = false;
                Toast.makeText(PurchaseActivity.this, getResources().getString(R.string.msg_check_delivery_info), Toast.LENGTH_SHORT).show();
                return;
            }

            boolean cardChecked = radioCard.isChecked();
            boolean depositChecked = radioDeposit.isChecked();
//            boolean noBankBookChecked = radioNoBankbook.isChecked();
            boolean mobileChecked = radioPhone.isChecked();
//            if ( !cardChecked && !depositChecked && !noBankBookChecked ) {
            if (!cardChecked && !depositChecked && !mobileChecked) {
                mIsDoneClicked = false;
                Toast.makeText(PurchaseActivity.this, getResources().getString(R.string.msg_select_payway), Toast.LENGTH_SHORT).show();
                return;
            }


            if (cardChecked) {
                payment = PaymentActivity.INTENT_KEY_PAYMENT_CARD;
                payment_type = PaymentActivity.PAYMENT_TYPE_C;
            } else if (depositChecked) {
                payment = PaymentActivity.INTENT_KEY_PAYMENT_BANK_TRANSFER;
                payment_type = PaymentActivity.PAYMENT_TYPE_M;
            } else if (mobileChecked) {
                payment = PaymentActivity.INTENT_KEY_PAYMENT_MOBILE;
                payment_type = PaymentActivity.PAYMENT_TYPE_B; // TODO :: 휴대폰 결제 용 type이 정의 되어야 함
            } else {
                payment = PaymentActivity.INTENT_KEY_PAYMENT_NO_BANKBOOK;
                payment_type = PaymentActivity.PAYMENT_TYPE_B;
            }

            final String f_payment = payment;

            String s_userPoint = editTextPoint.getText().toString();
            double d_userPoint = 0;
            try {
                String e_userPoint = s_userPoint.replaceAll(",", "");
                d_userPoint = Double.parseDouble(e_userPoint);
            } catch (Exception e) {
                e.printStackTrace();
                mIsDoneClicked = false;
                return;
            }

            try {
                JSONObject object = new JSONObject();
                JSONArray iArray = new JSONArray();
                for (int i = 0; i < mProductModelArray.size(); i++) {
                    ProductModel model = mProductModelArray.get(i);
                    JSONObject sObject = new JSONObject();
                    sObject.put("pcode", model.getPCode());
                    sObject.put("quantity", model.getQuantity());
                    sObject.put("price", model.getPrice());
                    sObject.put("itemKey", model.getKey());
                    sObject.put("option_price", model.getOptionPrice());
                    iArray.put(sObject);
                }

                numOfItems = mProductModelArray.size();
                object.put("products", iArray);

                JSONObject d_object = new JSONObject();
                d_object.put("name", deliveryName);
                d_object.put("tel", deliveryPhone);
                d_object.put("zipCode", zipCode);
                d_object.put("addr1", newAddr);
                d_object.put("addr2", oldAddr);
                d_object.put("addr3", detail);
                d_object.put("message", memo);
                d_object.put("default", buttonBasicDelivery.isSelected() ? "1" : "0");
                object.put("deliveryInfo", d_object);
                object.put("point", "" + d_userPoint);
                object.put("payment_type", payment_type);
                if (isFromCart) {
                    object.put("ct_id", ct_id);
                } else {
                    object.put("stream_key", streamKey);
                    object.put("vod_type", vodType);
                    object.put("recommend_mb_id", recommend_mb_id);
                }
                String body = object.toString();
                Logger.d("sendPurchaseInfo body :::::: " + body);
                if (!TextUtils.isEmpty(body)) {
                    if (f_payment == PaymentActivity.INTENT_KEY_PAYMENT_NO_BANKBOOK) {
                        String deposit_name = edit_depositName.getText().toString();
                        String deposit_bank = edit_depositBank.getText().toString();
                        String deposit_account = edit_depositAccount.getText().toString();
                        if (TextUtils.isEmpty(deposit_name) || TextUtils.isEmpty(deposit_bank) || TextUtils.isEmpty(deposit_account)) {
                            Toast.makeText(PurchaseActivity.this, getResources().getString(R.string.msg_insert_refundinfo), Toast.LENGTH_SHORT).show();
                            mIsDoneClicked = false;
                            return;
                        }
                        object.put("deposit_name", deposit_name);
                        object.put("deposit_bank", deposit_bank);
                        object.put("deposit_account", deposit_account);
                        body = object.toString();
                        Logger.d("body ::::::::: " + body);
                    }
                    String val = "N";
                    if (IS_PAY_TEST)
                        val = "Y";
                    else
                        val = "N";
                    ShopTreeAsyncTask task = new ShopTreeAsyncTask(PurchaseActivity.this);
                    task.sendPurchaseInfo(body, val, new ShopTreeAsyncTask.OnDefaultObjectCallbackListener() {
                        @Override
                        public void onResponse(boolean rt, Object obj) {
                            mIsDoneClicked = false;
                            try {
                                JSONObject r_object = (JSONObject) obj;
                                String str = r_object.toString();
                                Logger.e("1231231231231 str :: " + str);
                                String result = r_object.optString("Result").toLowerCase();
                                String orderFlow = r_object.optString("orderFlow");
                                orderNumber = r_object.optString("stOrderNumber");
                                stOrderNumber = r_object.optString("orderNumber");
                                String due_date = r_object.optString("due_date");
                                Logger.d("result :: " + result);
                                Logger.d("order_num :: " + orderNumber);
                                Logger.d("due_date :: " + due_date);
                                result = result.toLowerCase();
                                Logger.d("sendPurchaseInfo result :::: " + result);
                                if ("success".equals(result)) {
                                    /**
                                     * 결제모듈 관련해서 v커머스에 맞게 변경되어야 함
                                     if ( f_payment == PaymentActivity.INTENT_KEY_PAYMENT_NO_BANKBOOK ) {
                                     String use_point = textViewPoint.getText().toString();
                                     Logger.d("before intent save_point :: " + mSavePoint);
                                     Intent intent = new Intent(PurchaseActivity.this, PurchaseCompleteActivity.class);
                                     intent.putExtra(PurchaseActivity.EXTRA_ORDER_NUMBER, order_num);
                                     intent.putExtra(PurchaseActivity.EXTRA_DUE_DATE, due_date);
                                     intent.putExtra(PurchaseActivity.EXTRA_PRODUCT_PRICE, mGoodsPrice);
                                     intent.putExtra(PurchaseActivity.EXTRA_DELIVERY_PRICE, mDeliveryPrice);
                                     intent.putExtra(PurchaseActivity.EXTRA_USED_POINT, use_point);
                                     intent.putExtra(PurchaseActivity.EXTRA_TOTAL_PRRICE, mTotalPrice);
                                     intent.putExtra(PurchaseActivity.EXTRA_SAVE_POINT, mSavePoint);
                                     intent.putExtra(PurchaseActivity.EXTRA_NUM_ITEMS, numOfItems);
                                     startActivity(intent);
                                     finish();
                                     } else {
                                     sendPayment(f_payment, order_num);
                                     }
                                     **/

                                    if ("finish".equals(orderFlow)) {
                                        ShopTreeAsyncTask task = new ShopTreeAsyncTask(PurchaseActivity.this);
                                        task.sendPurchaseFinish(stOrderNumber, orderNumber, new ShopTreeAsyncTask.OnDefaultObjectCallbackListener() {
                                            @Override
                                            public void onResponse(boolean rt, Object obj) {
                                                try {
                                                    JSONObject r_object = (JSONObject) obj;
                                                    String result = r_object.optString("result").toLowerCase();
                                                    String resultCode = r_object.optString("resultCode");
                                                    String message = r_object.optString("resultMessage");
                                                    if ("success".equals(result)) {
                                                        if ("401".equals(resultCode) || "402".equals(resultCode)) {
                                                            Toast.makeText(PurchaseActivity.this, message, Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            double d_userPoint = 0;
                                                            try {
                                                                String e_userPoint = s_userPoint.replaceAll(",", "");
                                                                d_userPoint = Double.parseDouble(e_userPoint);
                                                            } catch (Exception e) {
                                                                e.printStackTrace();
                                                            }

                                                            Intent intent = new Intent(PurchaseActivity.this, PurchaseCompleteCardActivity.class);
                                                            intent.putExtra(PurchaseActivity.EXTRA_ONLY_POINT, true);
                                                            intent.putExtra(PurchaseActivity.EXTRA_ORDER_NUMBER, orderNumber);
                                                            intent.putExtra(PurchaseActivity.EXTRA_L_ORDER_NUMBER, stOrderNumber);
                                                            intent.putExtra(PurchaseActivity.EXTRA_PRODUCT_PRICE, mGoodsPrice);
                                                            intent.putExtra(PurchaseActivity.EXTRA_DELIVERY_PRICE, mDeliveryPrice);
                                                            intent.putExtra(PurchaseActivity.EXTRA_USED_POINT, d_userPoint + "");
                                                            intent.putExtra(PurchaseActivity.EXTRA_TOTAL_PRRICE, mTotalPrice);
                                                            intent.putExtra(PurchaseActivity.EXTRA_SAVE_POINT, mSavePoint);
                                                            intent.putExtra(PurchaseActivity.EXTRA_NUM_ITEMS, numOfItems);
                                                            intent.putExtra(PurchaseActivity.EXTRA_PAYMENT, mPaymentStr);
                                                            startActivityForResult(intent, REQUEST_GO_PAYMENT);
                                                        }
                                                    }
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        });
                                    } else if ("inicis".equals(orderFlow)) {
                                        sendPayment(f_payment, orderNumber, stOrderNumber);
                                    }
                                } else {

                                }
                                Logger.d("str :: " + str);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } else {
                    mIsDoneClicked = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (viewId == R.id.buttonSearchAddress) {
            showSearchByPostAddressDialog();
        } else if (viewId == R.id.buttonPointUseAll) {
            mEdited = editTextPoint.getText().toString();
            try {
                double d_edited = 0;
                if (TextUtils.isEmpty(mEdited)) {
                    d_edited = 0;
                } else {
                    String s_edit = mEdited.replaceAll(",", "");
                    d_edited = Double.valueOf(s_edit);
                }
                Logger.e("d_edited :: " + d_edited);
                d_edited = d_edited - d_edited%10;
                Logger.e("d_edited :: " + d_edited);
                if (mMyPoint >= d_edited) { // 내 포인트 보다 작성한 포인트가 적을 경우 (정상)
                    mUsePoint = d_edited;
                } else { // 사용자가 작성한 포인트가 보유 포인트보다 많을 경우
                    mUsePoint = mMyPoint;
                }

                if (mTotalPrice < mUsePoint) { // 전체 구매 가격보다 사용자가 설정한 포인트가 적을 경우
                    mUsePoint = mTotalPrice;
                }

                mPurchasePrice = mTotalPrice - mUsePoint; // 최종 구매 가격은 전체 가격에서 포인트를 뺀 금액
                Logger.d("mPurchasePrice:: " + mPurchasePrice);
                mEdited = Utils.ToNumFormat((int) mUsePoint);

                textViewPoint.setText(mEdited + "원");
                String s_purchase = Utils.ToNumFormat((int) mPurchasePrice) + "원";
                textViewTotalPaymentPrice.setText(s_purchase);

            } catch (Exception e) {
                e.printStackTrace();
            }


            Logger.e("buttonPointUseAll button clicked");
//            mUsePoint = mMyPoint;
            if (mTotalPrice < mUsePoint) { // 전체 구매 가격보다 사용자가 설정한 포인트가 적을 경우
                mUsePoint = mTotalPrice;
            }
            Logger.d("mTotalPrice:: " + mTotalPrice);
            Logger.d("mUsePoint:: " + mUsePoint);

            mPurchasePrice = mTotalPrice - mUsePoint; // 최종 구매 가격은 전체 가격에서 포인트를 뺀 금액
            Logger.d("mPurchasePrice:: " + mPurchasePrice);
            mEdited = Utils.ToNumFormat((int) mUsePoint);

            editTextPoint.setText(mEdited);
            editTextPoint.setSelection(mEdited.length()); // 컴마가 붙으면서 커서 위치가 맨 앞으로 이동하는 현상 막기 위해
            textViewPoint.setText(mEdited + "원");
            String s_purchase = Utils.ToNumFormat((int) mPurchasePrice) + "원";
            textViewTotalPaymentPrice.setText(s_purchase);
        } else if (viewId == R.id.buttonBasicDelivery) {
            if (!buttonBasicDelivery.isSelected()) {
                buttonBasicDelivery.setSelected(true);
            } else {
                buttonBasicDelivery.setSelected(false);
            }
        }
    }

    /**
     * 우편번호 검색 팝업화면을 표시
     */
    private void showSearchByPostAddressDialog() {
        releaseSearchByPostAddressDialog();
        postAddressDialog = new PostAddressDialog(PurchaseActivity.this);


        postAddressDialog.setResultListener((postAddress, newAddress, oldAddress) -> {
            Logger.d("Find Post Address Successfully.");
            if (postAddress != null) {
                textViewPostNumber.setText(postAddress);
            }

            if (newAddress != null) {
                textViewNewAddress.setText(newAddress);
            }

            if (oldAddress != null) {
                textViewOldAddress.setText(oldAddress);
            }
        });
        postAddressDialog.dialogShow(PurchaseActivity.this);
    }

    /**
     * 우편번호 검색 팝업 화면을 해제
     */
    private void releaseSearchByPostAddressDialog() {
        if (postAddressDialog != null) {
            postAddressDialog.dialogDismiss();
            postAddressDialog = null;
        }
    }

    private void getInfo(String body) {
        setProductData(body);
        setBuyerInfo(body);
        setAddress(body);
    }

    private void setBuyerInfo(String jsonStr) {
        try {
            JSONObject object = new JSONObject(jsonStr);
            mUserName = object.optString("user_name").replaceAll("null", "");
            String phone = object.optString("phone").replaceAll("null", "");
            mMyPoint = object.optDouble("userPoint");

            buyerName.setText(mUserName);
            if ( "false".equals(phone) )
                phone = "";
            buyerPhone.setText(phone);

            String sPoint = Utils.ToNumFormat((int) mMyPoint) + "원";
//            if (mMyPoint == 0) {
//                buttonPointUseAll.setBackgroundResource(R.drawable.gray_btn);
//                buttonPointUseAll.setClickable(false);
//            } else {
//                buttonPointUseAll.setBackgroundResource(R.drawable.gray_btn);
//            }
            textViewAvailablePoint.setText(sPoint);
            textViewPoint.setText("0원");
            editTextPoint.setText("0");
            if (mMyPoint < 1000) {
                editTextPoint.setEnabled(false);
                editTextPoint.setClickable(false);
                buttonPointUseAll.setClickable(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void setProductData(String jsonStr) {
        ArrayList<BuyModel> buyModelArray = null;
        try {
            JSONObject object = new JSONObject(jsonStr);
            mTotalPrice = object.optDouble("totalPrice");
            mGoodsPrice = object.optDouble("goodsPrice");
            mDeliveryPrice = object.optDouble("totalDeliveryPrice");
            mSavePoint = object.optDouble("save_point");
            String s_goodsPrice = Utils.ToNumFormat((int) mGoodsPrice) + "원";
            String s_feePrice = Utils.ToNumFormat((int) mDeliveryPrice) + "원";
            String s_totalPrice = Utils.ToNumFormat((int) mTotalPrice) + "원";
            String s_save_noti = Utils.ToNumFormat((int) mSavePoint);
            mNotiSavePoint.setText(getResources().getString(R.string.save_point_noti, s_save_noti));
            textViewProductPrice.setText(s_goodsPrice);
            textViewTotalDeliveryPrice.setText(s_feePrice);
            textViewTotalPaymentPrice.setText(s_totalPrice);
            mPurchasePrice = mTotalPrice;

            JSONArray j_items = object.optJSONArray("items");
            if (j_items.length() > 0) {

                mProductModelArray = new ArrayList<ProductModel>();

                buyModelArray = new ArrayList<BuyModel>();
                for (int i = 0; i < j_items.length(); i++) {
                    JSONObject i_object = j_items.optJSONObject(i); // 상점 object
                    BuyModel b_model = null;
                    if (i_object != null) {
                        b_model = new BuyModel();
                        String storeName = i_object.optString("store_name");
                        String sc = i_object.optString("sc_code");
                        double orderPrice = i_object.optDouble("order_price");
                        double deliveryPrice = i_object.optDouble("delivery_price");
                        double deliveryFeeRange = i_object.optDouble("delivery_feeRange");
                        String deliveryFeeType = i_object.optString("delivery_feetype");
                        double totalChapterPrice = i_object.optDouble("totalChapterPrice");
                        JSONArray item_array = i_object.optJSONArray("item");
                        ArrayList<BuyItemModel> itemModelArray = null;
                        if (item_array.length() > 0) {
                            itemModelArray = new ArrayList<BuyItemModel>();
                            for (int j = 0; j < item_array.length(); j++) {
                                JSONObject item_object = item_array.optJSONObject(j);
                                BuyItemModel b_itemModel = null;
                                if (item_object != null) {
                                    b_itemModel = new BuyItemModel();
                                    String productKey = item_object.optString("product_key");
                                    String itemKey = item_object.optString("itemKey");
                                    String image = item_object.optString("img");
                                    String title = item_object.optString("title");
                                    Logger.e("mPaymentStr :: " + mPaymentStr);
                                    Logger.e("title :: " + title);
                                    if (TextUtils.isEmpty(mPaymentStr))
                                        mPaymentStr = title;
                                    else {
                                        mPaymentCount++;
                                    }
                                    Logger.e("af mPaymentStr :: " + mPaymentStr);
                                    double goodPrice = item_object.optDouble("goodprice");
                                    String optionName = item_object.optString("option_name");
                                    int optionQuantity = item_object.optInt("option_quantity", 0);
                                    double totalPrice = item_object.optDouble("totalprice", 0);
                                    double optionPrice = item_object.optDouble("option_price", 0);

                                    b_itemModel.setProductKey(productKey);
                                    b_itemModel.setOptionKey(itemKey);
                                    b_itemModel.setImage(image);
                                    b_itemModel.setName(title);
                                    b_itemModel.setOptionName(optionName);
                                    b_itemModel.setOptionQuantity(optionQuantity);
                                    b_itemModel.setTotalPrice(totalPrice);

                                    ProductModel model = new ProductModel();
                                    model.setKey(itemKey);
                                    model.setOptionPrice(optionPrice);
                                    model.setPrice(goodPrice);
                                    model.setPCode(productKey);
                                    model.setQuantity(optionQuantity);

                                    mProductModelArray.add(model);
                                }
                                itemModelArray.add(b_itemModel);
                            }
                        }
                        b_model.setModelArray(itemModelArray);
                        b_model.setDeliveryPrice(deliveryPrice);
                        b_model.setDeliveryFeeRange(deliveryFeeRange);
                        b_model.setDeliveryFeeType(deliveryFeeType);
                        b_model.setStoreName(storeName);
                        b_model.setSc(sc);
                        b_model.setOrderPrice(orderPrice);
                        b_model.setTotalChapterPrice(totalChapterPrice);
                    }
                    buyModelArray.add(b_model);

//                    logInitiatedCheckoutEvent("", Constants.PARAM_CONTENT_TYPE, buyModelArray.size(), true, Constants.PARAM_CONTENT_CURRENCY, mTotalPrice);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (buyModelArray != null && buyModelArray.size() > 0)
            addProductView(buyModelArray);
    }

    private void addProductView(ArrayList<BuyModel> array) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final ArrayList<String> deliveryMsg = new ArrayList<String>();
        for (int i = 0; i < array.size(); i++) {
            BuyModel model = array.get(i);
            if (model != null) {
                LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.buy_item, null);
                ImageView delivery_qa = (ImageView) layout.findViewById(R.id.delivery_qa);
                delivery_qa.setTag(i);
                TextView title = (TextView) layout.findViewById(R.id.title);
                TextView storeName = (TextView) layout.findViewById(R.id.storeName);
                TextView productPrice = (TextView) layout.findViewById(R.id.product_price);
                TextView deliveryPrice = (TextView) layout.findViewById(R.id.delivery_price);
                TextView totalPrice = (TextView) layout.findViewById(R.id.order_price);
                LinearLayout buyItem = (LinearLayout) layout.findViewById(R.id.item_layer);
                int c = i + 1;
                String str = "주문상품 정보[" + c + "]";
                String store_name = model.getStoreName();
                double product_price = model.getOrderPrice();
                double delivery_price = model.getDeliveryPrice();
                double total_price = product_price + delivery_price;
                String s_product_price = Utils.ToNumFormat((int) product_price) + "원";
                String s_delivery_price = Utils.ToNumFormat((int) delivery_price) + "원";
                String s_total_price = Utils.ToNumFormat((int) total_price) + "원";

                double feeRange = model.getDeliveryFeeRange();
                String feeType = model.getDeliveryFeeType();
                String strFee = "";
                if ("무료배송".equals(feeType)) {
                    strFee = "무료배송";
                } else {
                    try {
                        strFee = Utils.ToNumFormat((int) feeRange) + "원 이상 구매 시 무료배송";
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                deliveryMsg.add(strFee);

                ArrayList<BuyItemModel> modelItemArray = model.getModelArray();
                if (modelItemArray != null && modelItemArray.size() > 0) {
                    for (int j = 0; j < modelItemArray.size(); j++) {
                        BuyItemModel item_model = modelItemArray.get(j);
                        RelativeLayout o_layout = (RelativeLayout) inflater.inflate(R.layout.buy_option_item, null);
                        ImageView image = (ImageView) o_layout.findViewById(R.id.image);
                        TextView name = (TextView) o_layout.findViewById(R.id.name);
                        TextView option = (TextView) o_layout.findViewById(R.id.option);
                        TextView itemPrice = (TextView) o_layout.findViewById(R.id.item_price);
                        String i_image = item_model.getImage();
                        String i_name = item_model.getName();
                        String i_option = "";
                        String optionName = item_model.getOptionName();
                        if (TextUtils.isEmpty(optionName)) {
                            i_option = item_model.getOptionQuantity() + "개";
                        } else {
                            if ("null".equals(optionName)) {
                                i_option = item_model.getOptionQuantity() + "개";
                            } else {
                                i_option = item_model.getOptionName() + " / " + item_model.getOptionQuantity() + "개";
                            }
                        }
                        double i_price = item_model.getTotalPrice();
                        String s_price = Utils.ToNumFormat((int) i_price) + "원";
                        loadImage(i_image, image);

                        name.setText(i_name);
                        option.setText(i_option);
                        itemPrice.setText(s_price);

                        buyItem.addView(o_layout);

                    }
                }

                delivery_qa.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int tag = (int) delivery_qa.getTag();
                        String msg = deliveryMsg.get(tag);
                        DeliveryConditionDialog dialog = new DeliveryConditionDialog(PurchaseActivity.this, msg);
                        dialog.show();
                    }
                });

                title.setText(str);
                storeName.setText(store_name);
                productPrice.setText(s_product_price);
                deliveryPrice.setText(s_delivery_price);
                totalPrice.setText(s_total_price);


                mProductLayer.addView(layout);
            }
        }
    }

    private void setAddressCheck(int status) {
        if (status == STATUS_BASE) {
            buttonSearchAddress.setEnabled(false);
            radioGroup.check(R.id.radioDeliveryRecentyl);
        } else if (status == STATUS_RECENT) {
            buttonSearchAddress.setEnabled(false);
            radioGroup.check(R.id.radioDeliveryNew);
        } else if (status == STATUS_NEW) {
            buttonSearchAddress.setEnabled(true);
            radioGroup.check(R.id.radioInputSelf);
        }
    }

    private void setAddress(String str) {
        try {
            JSONObject object = new JSONObject(str);
            if (object != null) {
                JSONObject basic_object = object.optJSONObject("basicAddrList");
                JSONObject new_object = object.optJSONObject("newAddrList");
                boolean isBasicAddressSet = false;
                if (basic_object != null) {
                    String name = basic_object.optString("user_name", "").replaceAll("null", "");
                    String phone = basic_object.optString("phone", "").replaceAll("null", "");
                    String addr1 = basic_object.optString("addr1", "").replaceAll("null", ""); // 우편번호
                    String addr3 = basic_object.optString("addr2", "").replaceAll("null", ""); // 상세주소
                    String addr2 = basic_object.optString("addr3", "").replaceAll("null", ""); // 지번 주소
                    String addr4 = basic_object.optString("addr4", "").replaceAll("null", ""); // 도로명 주소
                    if (isAddressValid(addr1, addr2, addr3, addr4, name, phone)) {
                        mBasicModel = new DeliveryInfo();
                        mBasicModel.setName(name);
                        mBasicModel.setPhone(phone);
                        mBasicModel.setZipCode(addr1);
                        mBasicModel.setDetailAddress(addr2);
                        mBasicModel.setAddress(addr3);
                        mBasicModel.setNewAddress(addr4);

                        setAddressData(mBasicModel, false);

                        isBasicAddressSet = true;
                    }
                }

                if (new_object != null) {
                    String name = new_object.optString("user_name", "").replaceAll("null", "");
                    String phone = new_object.optString("phone", "").replaceAll("null", "");
                    String addr1 = new_object.optString("addr1", "").replaceAll("null", ""); // 우편번호
                    String addr3 = new_object.optString("addr2", "").replaceAll("null", ""); // 상세주소
                    String addr2 = new_object.optString("addr3", "").replaceAll("null", ""); // 지번 주소
                    String addr4 = new_object.optString("addr4", "").replaceAll("null", ""); // 도로명 주소
                    if (isAddressValid(addr1, addr2, addr3, addr4, name, phone)) {
                        mRecentModel = new DeliveryInfo();
                        mRecentModel.setName(name);
                        mRecentModel.setPhone(phone);
                        mRecentModel.setZipCode(addr1);
                        mRecentModel.setDetailAddress(addr2);
                        mRecentModel.setAddress(addr3);
                        mRecentModel.setNewAddress(addr4);
                        if (!isBasicAddressSet)
                            setAddressData(mRecentModel, false);
                    }
                }

                int flag = STATUS_NEW;
                if (mBasicModel != null) {
                    setAddressCheck(STATUS_BASE);
                } else if (mRecentModel != null) {
                    setAddressCheck(STATUS_RECENT);
                } else {
                    setAddressCheck(STATUS_NEW);
                }

                if (mBasicModel == null) {
                    radioDeliveryRecentyl.setEnabled(false);
                    radioDeliveryRecentyl.setClickable(false);
                }

                if (mRecentModel == null) {
                    radioDeliveryNew.setEnabled(false);
                    radioDeliveryNew.setClickable(false);
                }
            }

            radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if (checkedId == R.id.radioDeliveryRecentyl) {
                        buttonSearchAddress.setEnabled(false);
                        setAddressData(mBasicModel, false);
                    } else if (checkedId == R.id.radioDeliveryNew) {
                        buttonSearchAddress.setEnabled(false);
                        setAddressData(mRecentModel, false);
                    } else if (checkedId == R.id.radioInputSelf) {
                        buttonSearchAddress.setEnabled(true);
                        setAddressData(mInputModel, true);
                    }
                }
            });

            radioGroup1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
//                    if ( checkedId == R.id.radioNoBankbook ) {
//                        mNoBankBookLayer.setVisibility(View.VISIBLE);
//                    } else {
//                        mNoBankBookLayer.setVisibility(View.GONE);
//                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isAddressValid(String addr1, String addr2, String addr3, String addr4, String name, String phone) {
        if (TextUtils.isEmpty(addr1) || TextUtils.isEmpty(addr2) || TextUtils.isEmpty(addr3) || TextUtils.isEmpty(addr4) || TextUtils.isEmpty(name) || TextUtils.isEmpty(phone))
            return false;
        else
            return true;
    }

    private void setAddressData(DeliveryInfo model, boolean isEditable) {
        editTextRecipient.setEnabled(isEditable);
        editTextCellPhone.setEnabled(isEditable);
        editTextDetailAddress.setEnabled(isEditable);

        if (model != null) {
            editTextRecipient.setText(model.getName());
            editTextCellPhone.setText(model.getPhone());
            textViewPostNumber.setText(model.getZipCode());
            textViewOldAddress.setText(model.getAddress());
            editTextDetailAddress.setText(model.getDetailAddress());
            textViewNewAddress.setText(model.getNewAddress());
        } else {
            editTextRecipient.setText("");
            editTextCellPhone.setText("");
            textViewPostNumber.setText("");
            textViewOldAddress.setText("");
            editTextDetailAddress.setText("");
            textViewNewAddress.setText("");
        }
    }

    /**
     * This function assumes logger is an instance of AppEventsLogger and has been
     * created using AppEventsLogger.newLogger() call.
     */
//    public void logInitiatedCheckoutEvent (String contentId, String contentType, int numItems, boolean paymentInfoAvailable, String currency, double totalPrice) {
//        Bundle params = new Bundle();
//        params.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, contentId);
//        params.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, contentType);
//        params.putInt(AppEventsConstants.EVENT_PARAM_NUM_ITEMS, numItems);
//        params.putInt(AppEventsConstants.EVENT_PARAM_PAYMENT_INFO_AVAILABLE, paymentInfoAvailable ? 1 : 0);
//        params.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, currency);
//        AppEventsLogger.newLogger(BaseApplication.getGlobalApplicationContext())
//                .logEvent(AppEventsConstants.EVENT_NAME_INITIATED_CHECKOUT, totalPrice, params);
//    }

    private TextWatcher watcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            try {
                if (s.toString().length() >= 4) {
                    buttonPointUseAll.setClickable(true);
                    buttonPointUseAll.setBackgroundResource(R.drawable.purple_btn);
                } else {
                    buttonPointUseAll.setClickable(false);
                    buttonPointUseAll.setBackgroundResource(R.drawable.gray_btn);
                }

                String s_edited = s.toString().replaceAll(",", "");
                double d_edited = 0;
                if (TextUtils.isEmpty(s_edited)) {
                    d_edited = 0;
                } else {
                    d_edited = Double.parseDouble(s_edited);
                }
//                mEdited = Utils.ToNumFormat((int) d_edited);
//
//                if (mMyPoint >= d_edited) { // 내 포인트 보다 작성한 포인트가 적을 경우 (정상)
//                    mUsePoint = d_edited;
//                } else { // 사용자가 작성한 포인트가 보유 포인트보다 많을 경우
//                    mUsePoint = mMyPoint;
//                }
//
//                if (mTotalPrice < mUsePoint) { // 전체 구매 가격보다 사용자가 설정한 포인트가 적을 경우
//                    mUsePoint = mTotalPrice;
//                }
//                Logger.d("mTotalPrice:: " + mTotalPrice);
//                Logger.d("mUsePoint:: " + mUsePoint);
//
//
//                mPurchasePrice = mTotalPrice - mUsePoint; // 최종 구매 가격은 전체 가격에서 포인트를 뺀 금액
//                Logger.d("mPurchasePrice:: " + mPurchasePrice);
//                mEdited = Utils.ToNumFormat((int) mUsePoint);

                Logger.d("edited :::::: " + mEdited + " , s.toString() :::::: " + s.toString());
                if (!s.toString().equals(Utils.ToNumFormat((int) d_edited))) {
                    Logger.d("edited changed");
                    editTextPoint.setText(Utils.ToNumFormat((int) d_edited));
                    editTextPoint.setSelection(Utils.ToNumFormat((int) d_edited).length()); // 컴마가 붙으면서 커서 위치가 맨 앞으로 이동하는 현상 막기 위해
                }

//                textViewPoint.setText(mEdited + "원");
//                String s_purchase = Utils.ToNumFormat((int) mPurchasePrice) + "원";
//                textViewTotalPaymentPrice.setText(s_purchase);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private boolean ValidStr(String val) {
        if (TextUtils.isEmpty(val))
            return false;
        else {
            if ("null".equals(val))
                return false;
            else
                return true;
        }
    }

    private void sendPayment(String payment, final String orderNumber, final String lOrderNumber) {
        Logger.e("sendPayment IS_PAY_TEST :: " + IS_PAY_TEST);
        if (IS_PAY_TEST) {
//            Intent chatIntent = new Intent(ShoppingLiveFragment.SEND_CHAT);
//            chatIntent.putExtra("GUBUN", ChatManager.GUBUN_BUY);
//            chatIntent.putExtra("NAME", mPaymentStr);
//            sendBroadcast(chatIntent);
//
//            Intent intent = new Intent(PurchaseActivity.this, PurchaseCompleteCardActivity.class);
//            intent.putExtra(PurchaseActivity.EXTRA_ORDER_NUMBER, orderNumber);
//            intent.putExtra(PurchaseActivity.EXTRA_L_ORDER_NUMBER, lOrderNumber);
//            intent.putExtra(PurchaseActivity.EXTRA_PRODUCT_PRICE, mGoodsPrice);
//            intent.putExtra(PurchaseActivity.EXTRA_DELIVERY_PRICE, mDeliveryPrice);
//            intent.putExtra(PurchaseActivity.EXTRA_USED_POINT, "0원");
//            intent.putExtra(PurchaseActivity.EXTRA_TOTAL_PRRICE, mTotalPrice);
//            intent.putExtra(PurchaseActivity.EXTRA_SAVE_POINT, mSavePoint);
//            intent.putExtra(PurchaseActivity.EXTRA_NUM_ITEMS, numOfItems);
//            startActivity(intent);
//            finish();
        } else {
            String pay_msg = "";
            if (mPaymentCount <= 0) {
                pay_msg = mPaymentStr;
            } else {
                pay_msg = mPaymentStr + " 외 " + mPaymentCount + "건";
            }

            String use_point = textViewPoint.getText().toString();

            Intent intent = new Intent(PurchaseActivity.this, PaymentActivity.class);
            intent.putExtra(PaymentActivity.INTENT_KEY_PAYMENT_TYPE, payment);
            intent.putExtra(PaymentActivity.INTENT_KEY_PAYMENT_MID, MID);
            intent.putExtra(PaymentActivity.INTENT_KEY_PAYMENT_OID, orderNumber);
            intent.putExtra(PurchaseActivity.EXTRA_L_ORDER_NUMBER, lOrderNumber);
            intent.putExtra(PaymentActivity.INTENT_KEY_PAYMENT_AMT, "" + (int) mPurchasePrice);
            intent.putExtra(PaymentActivity.INTENT_KEY_PAYMENT_UNAME, "" + mUserName);
            intent.putExtra(PaymentActivity.INTENT_KEY_PAYMENT_GOODS, pay_msg);
            intent.putExtra(PaymentActivity.INTENT_KEY_PAYMENT_NOTI_URL, NetworkConst.NOTI_URL);
            intent.putExtra(PaymentActivity.INTENT_KEY_PAYMENT_RETURN_URL, NetworkConst.RETURN_URL);
            intent.putExtra(PaymentActivity.INTENT_KEY_PAYMENT_NEXT_URL, NetworkConst.NEXT_URL);

            intent.putExtra(EXTRA_PRODUCT_PRICE, mGoodsPrice);
            intent.putExtra(EXTRA_DELIVERY_PRICE, mDeliveryPrice);
            intent.putExtra(EXTRA_USED_POINT, use_point);
            intent.putExtra(EXTRA_TOTAL_PRRICE, mTotalPrice);
            intent.putExtra(EXTRA_SAVE_POINT, mSavePoint);
            intent.putExtra(EXTRA_NUM_ITEMS, numOfItems);
            intent.putExtra(PurchaseActivity.EXTRA_PAYMENT, mPaymentStr);

            startActivityForResult(intent, REQUEST_GO_PAYMENT);
        }


//        ShopTreeAsyncTask task = new ShopTreeAsyncTask(PurchaseActivity.this);
//        task.getPaymentInfo(new ShopTreeAsyncTask.OnDefaultObjectCallbackListener() {
//            @Override
//            public void onResponse(boolean rt, Object obj) {
//                try {
//                    JSONObject object = (JSONObject) obj;
//                    if ( object != null ) {
//                        boolean result = object.optBoolean("Result");
//                        String errStr = object.optString("errstr");
//                        String errCode = object.optString("errcode");
//                        JSONObject info = object.optJSONObject("info");
//                        if (info != null) {
//                            String notiStr = info.optString("noti");
//                            String returnStr = info.optString("return");
//                            String nextStr = info.optString("next");
//                            String midStr = info.optString("mid");
////                                        Intent intent = new Intent(this, PhoneAuthActivity.class);
////                                        startActivity(intent);
//                            String pay_msg = "";
//                            if ( mPaymentCount <= 0 ) {
//                                pay_msg = mPaymentStr;
//                            } else {
//                                pay_msg = mPaymentStr + " 외 " + mPaymentCount + "건";
//                            }
//
//                            String use_point = textViewPoint.getText().toString();
//
//                            Intent intent = new Intent(PurchaseActivity.this, PaymentActivity.class);
//                            intent.putExtra(PaymentActivity.INTENT_KEY_PAYMENT_TYPE, payment);
//                            intent.putExtra(PaymentActivity.INTENT_KEY_PAYMENT_MID, midStr);
//                            intent.putExtra(PaymentActivity.INTENT_KEY_PAYMENT_OID, orderNumber);
//                            intent.putExtra(PaymentActivity.INTENT_KEY_PAYMENT_AMT, ""+(int)mPurchasePrice);
//                            intent.putExtra(PaymentActivity.INTENT_KEY_PAYMENT_UNAME, ""+mUserName);
//                            intent.putExtra(PaymentActivity.INTENT_KEY_PAYMENT_GOODS, pay_msg);
//                            intent.putExtra(PaymentActivity.INTENT_KEY_PAYMENT_NOTI_URL, notiStr);
//                            intent.putExtra(PaymentActivity.INTENT_KEY_PAYMENT_RETURN_URL, returnStr);
//                            intent.putExtra(PaymentActivity.INTENT_KEY_PAYMENT_NEXT_URL, nextStr);
//
//                            intent.putExtra(EXTRA_PRODUCT_PRICE, mGoodsPrice);
//                            intent.putExtra(EXTRA_DELIVERY_PRICE, mDeliveryPrice);
//                            intent.putExtra(EXTRA_USED_POINT, use_point);
//                            intent.putExtra(EXTRA_TOTAL_PRRICE, mTotalPrice);
//                            intent.putExtra(EXTRA_SAVE_POINT, mSavePoint);
//                            intent.putExtra(EXTRA_NUM_ITEMS, numOfItems);
//
//                            startActivity(intent);
//                        }
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });
        /**
         * 결제모듈 관련해서 v커머스에 맞게 변경되어야 함
         CommonRequest.getInstance().getPaymentInfo(
         (Object response) -> {
         try {
         JSONObject object = new JSONObject((String) response);
         if (object != null) {
         boolean result = object.optBoolean("Result");
         String errStr = object.optString("errstr");
         String errCode = object.optString("errcode");
         JSONObject info = object.optJSONObject("info");
         if (info != null) {
         String notiStr = info.optString("noti");
         String returnStr = info.optString("return");
         String nextStr = info.optString("next");
         String midStr = info.optString("mid");
         String pay_msg = "";
         if ( mPaymentCount <= 0 ) {
         pay_msg = mPaymentStr;
         } else {
         pay_msg = mPaymentStr + " 외 " + mPaymentCount + "건";
         }

         String use_point = textViewPoint.getText().toString();

         Intent intent = new Intent(this, PaymentActivity.class);
         intent.putExtra(PaymentActivity.INTENT_KEY_PAYMENT_TYPE, payment);
         intent.putExtra(PaymentActivity.INTENT_KEY_PAYMENT_MID, midStr);
         intent.putExtra(PaymentActivity.INTENT_KEY_PAYMENT_OID, orderNumber);
         intent.putExtra(PaymentActivity.INTENT_KEY_PAYMENT_AMT, ""+(int)mPurchasePrice);
         intent.putExtra(PaymentActivity.INTENT_KEY_PAYMENT_UNAME, ""+mUserName);
         intent.putExtra(PaymentActivity.INTENT_KEY_PAYMENT_GOODS, pay_msg);
         intent.putExtra(PaymentActivity.INTENT_KEY_PAYMENT_NOTI_URL, notiStr);
         intent.putExtra(PaymentActivity.INTENT_KEY_PAYMENT_RETURN_URL, returnStr);
         intent.putExtra(PaymentActivity.INTENT_KEY_PAYMENT_NEXT_URL, nextStr);

         intent.putExtra(EXTRA_PRODUCT_PRICE, mGoodsPrice);
         intent.putExtra(EXTRA_DELIVERY_PRICE, mDeliveryPrice);
         intent.putExtra(EXTRA_USED_POINT, use_point);
         intent.putExtra(EXTRA_TOTAL_PRRICE, mTotalPrice);
         intent.putExtra(EXTRA_SAVE_POINT, mSavePoint);
         intent.putExtra(EXTRA_NUM_ITEMS, numOfItems);

         startActivity(intent);

         }
         }
         } catch (Exception e) {
         e.printStackTrace();
         }
         },
         (VolleyError error) -> {
         }
         );
         **/
    }

    @Override
    public void onNetworkStatusChanged(@NotNull NetworkStatusUtils.NetworkStatus status) {
    }

    @Override
    public void onDozeModeStateChanged(boolean dozeEnable) {
    }

    private void loadImage(String path, ImageView view) {
//        DrawableRequestBuilder builder = Glide.with(PurchaseActivity.this).load(path)
//                .diskCacheStrategy(DiskCacheStrategy.ALL)
//                .signature(new StringSignature("" + System.currentTimeMillis() / (24 * 60 * 60 * 1000)));
//        builder.into(view);
        ImageLoad.setImage(PurchaseActivity.this, view, path, "" + System.currentTimeMillis() / (24 * 60 * 60 * 1000));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_GO_PAYMENT && resultCode == RESULT_OK) {
            finish();
        } else if ( requestCode == ACTIVITY_REQUEST_AUTH ) {
            if ( resultCode == RESULT_OK ) {
                setData();
            } else {
                finish();
            }
        }
    }

    private void setData() {
        ShopTreeAsyncTask task = new ShopTreeAsyncTask(PurchaseActivity.this);
        task.getPurchaseInfo(body, new ShopTreeAsyncTask.OnDefaultObjectCallbackListener() {
            @Override
            public void onResponse(boolean rt, Object obj) {
                try {
                    JSONObject object = (JSONObject) obj;
                    if (object != null) {
                        String result = object.toString();
                        Logger.d("result :: " + result);
                        if (isFromCart) {
                            StringBuilder builder = new StringBuilder();
                            JSONArray array = object.optJSONArray("items");
                            if (array != null && array.length() > 0) {
                                for (int i = 0; i < array.length(); i++) {
                                    JSONObject subObject = array.getJSONObject(i);
                                    JSONArray subArray = subObject.optJSONArray("item");
                                    if (subArray != null && subArray.length() > 0) {
                                        for (int j = 0; j < subArray.length(); j++) {
                                            JSONObject sObj = subArray.optJSONObject(j);
                                            builder.append(sObj.optString("ct_id"));
                                            builder.append(",");
                                        }
                                    }
                                }
                            }
                            ct_id = builder.toString();
                            Logger.e("ct_id bf :: " + ct_id);
                            ct_id = ct_id.substring(0, ct_id.length() - 1);
                            Logger.e("ct_id :: " + ct_id);
                        } else {
                            JSONArray array = object.optJSONArray("items");
                            if (array != null && array.length() > 0) {
                                JSONObject firstObject = array.optJSONObject(0);
                                if (firstObject != null) {
                                    JSONArray subArray = firstObject.optJSONArray("item");
                                    if (subArray != null && subArray.length() > 0) {
                                        JSONObject firstItem = subArray.optJSONObject(0);
                                        streamKey = firstItem.optString("stream_key");
                                        vodType = firstItem.optString("vod_type");
                                        recommend_mb_id = firstItem.optString("recommend_mb_id");
                                    }
                                }
                            }
                        }
                        getInfo(result);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
