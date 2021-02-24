package com.enliple.pudding.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.enliple.pudding.AbsBaseActivity;
import com.enliple.pudding.BuildConfig;
import com.enliple.pudding.R;
import com.enliple.pudding.adapter.my.CustomerMainAdapter;
import com.enliple.pudding.adapter.my.FAQCategoryAdapter;
import com.enliple.pudding.commons.app.NetworkStatusUtils;
import com.enliple.pudding.commons.db.DBManager;
import com.enliple.pudding.commons.internal.AppPreferences;
import com.enliple.pudding.commons.log.Logger;
import com.enliple.pudding.commons.network.NetworkApi;
import com.enliple.pudding.commons.network.NetworkBus;
import com.enliple.pudding.commons.network.NetworkBusResponse;
import com.enliple.pudding.commons.network.NetworkHandler;
import com.enliple.pudding.commons.network.vo.API21;
import com.enliple.pudding.commons.network.vo.API38;
import com.enliple.pudding.commons.network.vo.API40;
import com.enliple.pudding.commons.network.vo.API41;
import com.enliple.pudding.commons.widget.recyclerview.WrappedLinearLayoutManager;
import com.enliple.pudding.model.PermissionObject;
import com.enliple.pudding.widget.MainPermissionDialog;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CustomerCenterMainActivity extends AbsBaseActivity {
    private static final int REQUEST_ONE_BY_ONE = 29131;
    private static final int REQUEST_GO_LOGIN = 26041;
    private static final int NUMBER_COLUMNS = 3;
    private static final String FAQLIST_TYPE_CATEGORY = "category";
    private static final String FAQLIST_TYPE_ALL = "all";
    private static final int REQUEST_GO_CART = 50222;
    private static final int NUMBER_LIST = 10;
    private MainPermissionDialog dialog;
    private String rationalValue;
    private RelativeLayout buttonBack;
    private AppCompatImageButton buttonMessage, buttonCart;
    private AppCompatTextView textViewMessageBadge, textViewCartBadge, textCount, btnGoQNA, callNo, counselingTime;
    private RelativeLayout btnMyQList, btnOneByOne, btnSearchQuestion, btnCall;
    private RecyclerView list, recycler;
    private CustomerMainAdapter adapter;
    private FAQCategoryAdapter faqAdapter;
    private WrappedLinearLayoutManager layoutManager;
    private GridLayoutManager gridLayoutManager;
    private String oneByoneQAListKey = "";
    private String firstCategoryId = "";
    private String selectedCategoryType = "";
    private int cartCnt, messageCnt;
    private long cTime = 0;
    private boolean isLoginUser = false;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_center_main);

        initViews();
    }

    @Override
    public void onResume() {
        super.onResume();
        isLoginUser = AppPreferences.Companion.getLoginStatus(CustomerCenterMainActivity.this);
        if (dialog != null)
            dialog.dismiss();
    }

    private void initViews() {

        list = findViewById(R.id.list);
        recycler = findViewById(R.id.recycler);

        buttonBack = findViewById(R.id.buttonBack);
        buttonMessage = findViewById(R.id.buttonMessage);
        buttonCart = findViewById(R.id.buttonCart);
        textViewMessageBadge = findViewById(R.id.textViewMessageBadge);
        textViewCartBadge = findViewById(R.id.textViewCartBadge);
        textCount = findViewById(R.id.textCount);
        btnGoQNA = findViewById(R.id.btnGoQNA);
        btnMyQList = findViewById(R.id.btnMyQList);
        btnOneByOne = findViewById(R.id.btnOneByOne);
        btnSearchQuestion = findViewById(R.id.btnSearchQuestion);
        callNo = findViewById(R.id.callNo);
        counselingTime = findViewById(R.id.counselingTime);
        btnCall = findViewById(R.id.btnCall);

        buttonBack.setOnClickListener(clickListener);
        buttonMessage.setOnClickListener(clickListener);
        buttonCart.setOnClickListener(clickListener);
        btnGoQNA.setOnClickListener(clickListener);
        btnMyQList.setOnClickListener(clickListener);
        btnOneByOne.setOnClickListener(clickListener);
        btnSearchQuestion.setOnClickListener(clickListener);
        btnCall.setOnClickListener(clickListener);

        gridLayoutManager = new GridLayoutManager(CustomerCenterMainActivity.this, NUMBER_COLUMNS);
        recycler.setLayoutManager(gridLayoutManager);
        faqAdapter = new FAQCategoryAdapter(CustomerCenterMainActivity.this);
        recycler.setAdapter(faqAdapter);
        faqAdapter.setOnItemClickListener(new FAQCategoryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, API38.FAQCategoryData data) {
                selectedCategoryType = FAQLIST_TYPE_CATEGORY;
                firstCategoryId = data.id;
                Logger.e("firstCategory :: " + firstCategoryId);
                NetworkBus bus = new NetworkBus(NetworkApi.API41.name(), selectedCategoryType, firstCategoryId);
                EventBus.getDefault().post(bus);
            }
        });
        adapter = new CustomerMainAdapter(CustomerCenterMainActivity.this);
        layoutManager = new WrappedLinearLayoutManager(CustomerCenterMainActivity.this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        list.setHasFixedSize(false);
        list.setNestedScrollingEnabled(false);
        list.setLayoutManager(layoutManager);
        list.setAdapter(adapter);
//        adapter.setItems(getData(makeJSON()));

        EventBus.getDefault().register(this);

        NetworkBus bus = new NetworkBus(NetworkApi.API38.name());
        EventBus.getDefault().post(bus);

        bus = new NetworkBus(NetworkApi.API40.name(), AppPreferences.Companion.getUserId(CustomerCenterMainActivity.this));
        EventBus.getDefault().post(bus);

        bus = new NetworkBus(NetworkApi.API21.name(), AppPreferences.Companion.getUserId(this));
        EventBus.getDefault().post(bus);
        callNo.setText(AppPreferences.Companion.getCustomerCenterPhone(CustomerCenterMainActivity.this));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Logger.e("onActivityResult");
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_GO_CART) {
                int cartCnt = data.getIntExtra("CART_CNT", 0);
                setCartBadgeCount(cartCnt);
            } else if (requestCode == REQUEST_ONE_BY_ONE) {
                NetworkBus bus = new NetworkBus(NetworkApi.API38.name());
                EventBus.getDefault().post(bus);

                bus = new NetworkBus(NetworkApi.API40.name(), AppPreferences.Companion.getUserId(CustomerCenterMainActivity.this));
                EventBus.getDefault().post(bus);

                bus = new NetworkBus(NetworkApi.API21.name(), AppPreferences.Companion.getUserId(this));
                EventBus.getDefault().post(bus);
            } else if ( requestCode == REQUEST_GO_LOGIN ) {
                NetworkBus bus = new NetworkBus(NetworkApi.API38.name());
                EventBus.getDefault().post(bus);

                bus = new NetworkBus(NetworkApi.API40.name(), AppPreferences.Companion.getUserId(CustomerCenterMainActivity.this));
                EventBus.getDefault().post(bus);

                bus = new NetworkBus(NetworkApi.API21.name(), AppPreferences.Companion.getUserId(this));
                EventBus.getDefault().post(bus);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(NetworkBusResponse data) {
        Logger.e("onMessageEvent faq category data.arg1 " + data.arg1);
        String categoryKey = "GET/support/faqcategory";
        String faqlistKey = "GET/support/faqlist?type=" + selectedCategoryType + "&keyword=" + firstCategoryId;
        oneByoneQAListKey = "GET/support/myqa?user=" + AppPreferences.Companion.getUserId(CustomerCenterMainActivity.this);
        String key = NetworkHandler.Companion.getInstance(this)
                .getKey(NetworkApi.API21.toString(), AppPreferences.Companion.getUserId(this), "");
        if (data.arg1.equals(categoryKey)) {
            String str = DBManager.getInstance(this).get(categoryKey);
            API38 response = new Gson().fromJson(str, API38.class);

            if ("success".equals(response.result)) {
                faqAdapter.setItems(response.data);
//                selectedCategoryType = FAQLIST_TYPE_CATEGORY;
//                firstCategoryId = response.data.get(0).id;
                selectedCategoryType = "all";
                firstCategoryId = "";
                NetworkBus bus = new NetworkBus(NetworkApi.API41.name(), selectedCategoryType, firstCategoryId);
                EventBus.getDefault().post(bus);
            }
        } else if (data.arg1.equals(oneByoneQAListKey)) {
            String str = DBManager.getInstance(this).get(oneByoneQAListKey);
            API40 response = new Gson().fromJson(str, API40.class);
            String totalCount = response.nTotalCount;
            if ("0".equals(totalCount) || TextUtils.isEmpty(totalCount))
                textCount.setVisibility(View.GONE);
            else {
                textCount.setVisibility(View.VISIBLE);
                textCount.setText("(" + totalCount + ")");
            }
        } else if (data.arg1.equals(faqlistKey)) {
            String str = DBManager.getInstance(this).get(faqlistKey);
            API41 response = new Gson().fromJson(str, API41.class);
            if (adapter != null) {
                if (response.data != null) {
                    Logger.e("response.data.size :: " + response.data.size());
                    if (response.data.size() > NUMBER_LIST) {
                        List<API41.FAQList> list = new ArrayList<API41.FAQList>();
                        for (int i = 0; i < NUMBER_LIST; i++) {
                            list.add(response.data.get(i));
                        }
                        adapter.setItems(list);
                    } else {
                        adapter.setItems(response.data);
                    }
                }
            }
        } else if (data.arg1.equals(key)) {
            String str = DBManager.getInstance(this).get(data.arg1);
            API21 response = new Gson().fromJson(str, API21.class);
            try {
                messageCnt = Integer.valueOf(response.newMessage);
                cartCnt = Integer.valueOf(response.cartCount);
            } catch (Exception e) {
                e.printStackTrace();
            }
            setSmsBadgeCount(messageCnt);
            setCartBadgeCount(cartCnt);
        }
    }

    private void setSmsBadgeCount(int count) {
        textViewMessageBadge.setText("" + count);
        if (count > 0) {
//            textViewMessageBadge.setVisibility(View.VISIBLE);
        } else {
            textViewMessageBadge.setVisibility(View.GONE);
        }
    }

    /**
     * 장바구니 Badge 에 설정할 Count 를 지정 (0 개인 경우 숨김처리)
     *
     * @param count
     */
    private void setCartBadgeCount(int count) {
        textViewCartBadge.setText("" + count);
        if (count > 0) {
//            textViewCartBadge.setVisibility(View.VISIBLE);
        } else {
            textViewCartBadge.setVisibility(View.GONE);
        }
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            switch (v.getId()) {

                case R.id.buttonBack:
                    finish();
                    break;

                case R.id.buttonMessage:
                    startActivity(new Intent(getApplicationContext(), MessageActivity.class));
                    break;

                case R.id.buttonCart:
                    Intent intent1 = new Intent(getApplicationContext(), ProductCartActivity.class);
                    startActivityForResult(intent1, REQUEST_GO_CART);
                    break;

                case R.id.btnGoQNA:
                    Intent it = new Intent(getApplicationContext(), CustomerCenterAllActivity.class);
                    startActivity(it);
//                    selectedCategoryType = FAQLIST_TYPE_ALL;
//                    NetworkBus bus = new NetworkBus(NetworkApi.API41.name(), selectedCategoryType, firstCategoryId);
//                    EventBus.getDefault().post(bus);
                    break;

                case R.id.btnMyQList:
                    if ( isLoginUser ) {
                        Intent intent = new Intent(getApplicationContext(), MyInquiryActivity.class);
                        intent.putExtra("KEY", oneByoneQAListKey);
                        intent.putExtra("MESSAGE_CNT", messageCnt);
                        intent.putExtra("CART_CNT", cartCnt);
                        startActivity(intent);
                    } else {
                        goLogin();
                    }

                    break;

                case R.id.btnOneByOne:
                    if ( isLoginUser ) {
                        Intent intent2 = new Intent(getApplicationContext(), CenterAskingActivity.class);
                        intent2.putExtra("MESSAGE_CNT", messageCnt);
                        intent2.putExtra("CART_CNT", cartCnt);
                        startActivityForResult(intent2, REQUEST_ONE_BY_ONE);
                    } else {
                        goLogin();
                    }
                    break;

                case R.id.btnSearchQuestion:
                    Intent intent3 = new Intent(getApplicationContext(), CustomerCenterSearchActivity.class);
                    intent3.putExtra("MESSAGE_CNT", messageCnt);
                    intent3.putExtra("CART_CNT", cartCnt);
                    startActivity(intent3);
                    break;

                case R.id.btnCall:
                    int phoneCallPerm = ContextCompat.checkSelfPermission(CustomerCenterMainActivity.this, Manifest.permission.CALL_PHONE);
                    if (phoneCallPerm != PackageManager.PERMISSION_GRANTED) {
                        PermissionObject object = new PermissionObject();
                        ArrayList<PermissionObject.Objects> array = new ArrayList<PermissionObject.Objects>();
                        PermissionObject.Objects s_object = new PermissionObject.Objects();
                        s_object.setImage(R.drawable.authority_camera_ico);
                        s_object.setTitle("전화걸기 (선택)");
                        s_object.setContent("사용자가 해당 전화번호에 전화걸기를 할 때 사용됩니다.");
                        array.add(s_object);

                        object.setTitle("푸딩 앱을 즐기기 위해\n다음의 앱 권한을 허용해주세요.");
                        object.setObjectArray(array);

                        dialog = new MainPermissionDialog(object);
                        dialog.setListener(new MainPermissionDialog.DialogClickListener() {
                            @Override
                            public void onDialogClick() {
                                cTime = System.currentTimeMillis();
                                String onlyPhoneCallDenied = "NONE";
                                if (phoneCallPerm != PackageManager.PERMISSION_GRANTED) {
                                    if (ActivityCompat.shouldShowRequestPermissionRationale(CustomerCenterMainActivity.this, Manifest.permission.CALL_PHONE)) {
                                        onlyPhoneCallDenied = "TRUE";
                                    } else {
                                        onlyPhoneCallDenied = "FALSE";
                                    }
                                }
                                rationalValue = onlyPhoneCallDenied;

                                ActivityCompat.requestPermissions(CustomerCenterMainActivity.this, new String[]{Manifest.permission.CALL_PHONE}, 0);
                            }

                            @Override
                            public void onDismissed() {
                            }

                        });
                        dialog.show(getSupportFragmentManager(), "");
                    } else {
                        phoneCall();
                    }


//                    if (ContextCompat.checkSelfPermission(CustomerCenterMainActivity.this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
//                        phoneCall();
//                    } else {
//                        if (ActivityCompat.shouldShowRequestPermissionRationale(CustomerCenterMainActivity.this, Manifest.permission.CALL_PHONE)) {
//                            AlertDialog.Builder dialog = new AlertDialog.Builder(CustomerCenterMainActivity.this);
//                            dialog.setTitle("권한이 필요합니다.")
//                                    .setMessage("이 기능을 사용하기 위해서는 단말기의 \"전화걸기\" 권한이 필요합니다. 계속 하시겠습니까?")
//                                    .setPositiveButton("네", new DialogInterface.OnClickListener() {
//                                        @Override
//                                        public void onClick(DialogInterface dialog, int which) {
//                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                                                requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, 1000);
//                                            }
//                                        }
//                                    })
//                                    .setNegativeButton("아니요", new DialogInterface.OnClickListener() {
//                                        @Override
//                                        public void onClick(DialogInterface dialog, int which) {
//                                            Toast.makeText(CustomerCenterMainActivity.this, "기능을 취소했습니다", Toast.LENGTH_SHORT).show();
//                                        }
//                                    })
//                                    .create()
//                                    .show();
//
//                        } else {
//                            ActivityCompat.requestPermissions(CustomerCenterMainActivity.this, new String[]{Manifest.permission.CALL_PHONE}, 0);
//                        }
//                    }
                    break;
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResult) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResult);
        Logger.e("CustomerCenterMainActivity onRequestPermissionsResult");
        if (requestCode == 0) {
            long now = System.currentTimeMillis();
            long gap = now - cTime;
            if (grantResult[0] == 0) {
                phoneCall();
            } else {
                if ("FALSE".equals(rationalValue)) {
                    if (gap < 500) {
                        goSetting();
                    }
                }
            }
//            if (grantResult[0] == 0) {
//                phoneCall();
//            } else {
//                Toast.makeText(CustomerCenterMainActivity.this, "기능을 취소했습니다", Toast.LENGTH_SHORT).show();
//            }
        }
    }

    private void phoneCall() {
        String number = callNo.getText().toString();
        if( !TextUtils.isEmpty(number) ) {
            String phoneNo = "tel:" + number.replaceAll("-", "");
            if (!TextUtils.isEmpty(phoneNo))
                startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse(phoneNo)));
        }

    }

    private void goSetting() {
        Intent i = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID));
        startActivity(i);
    }

    private void goLogin() {
        startActivityForResult(new Intent(CustomerCenterMainActivity.this, LoginActivity.class), REQUEST_GO_LOGIN);
    }

    @Override
    public void onNetworkStatusChanged(@NotNull NetworkStatusUtils.NetworkStatus status) {

    }

    @Override
    public void onDozeModeStateChanged(boolean dozeEnable) {

    }

//    private ArrayList<CustomerQAModel> getData(String str) {
//        ArrayList<CustomerQAModel> modelArray = new ArrayList<CustomerQAModel>();
//        try {
//            if (TextUtils.isEmpty(str))
//                return null;
//            JSONObject object = new JSONObject(str);
//            if (object != null) {
//                JSONArray array = object.optJSONArray("list");
//                if (array != null && array.length() > 0) {
//                    for (int i = 0; i < array.length(); i++) {
//                        JSONObject subObject = array.optJSONObject(i);
//                        String question = subObject.optString("question");
//                        String answer = subObject.optString("answer");
//
//                        CustomerQAModel model = new CustomerQAModel();
//                        model.setQuestion(question);
//                        model.setAnswer(answer);
//
//                        modelArray.add(model);
//                    }
//                }
//            }
//            return modelArray;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }

//    private String makeJSON() {
//        try {
//            JSONObject object = new JSONObject();
//            JSONArray array = new JSONArray();
//
//            for (int i = 0; i < 15; i++) {
//                int val = i + 1;
//                JSONObject subObject = new JSONObject();
//                subObject.put("question", val + ". 결제 시 쿠폰이나 포인트 할인적용은 어떻게 받나요?");
//                subObject.put("answer", "*쿠폰 적용 방법*\n주문서 페이지에서 [쿠폰적용]버튼을 클릭한 후 팝업창에서 이용원하시는 쿠폰을 선택하고 [쿠폰적용]버튼을 누르면 쿠폰 적용이 완료됩니다.\n\n* 일부 카테고리 상품의 경우 쿠폰을 적용할 수 없습니다.\n(순금/골드바/돌반지/상품권 등 환금성 카테고리 및 중고 상품/중고장터 상품)\n*중복쿠폰은 판매자 할인, 옥션 할인, 카드사별 추가할인과 중복으로 적용 가능하며, 상품당 1장의 중복쿠폰만 적용 가능합니다. (단, 할인적용금액이 1,000원 미만일 경우중복쿠폰 적용 불가)");
//                array.put(subObject);
//            }
//            object.put("list", array);
//            return object.toString();
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
}
