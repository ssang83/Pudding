package com.enliple.pudding.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.enliple.pudding.AbsBaseActivity;
import com.enliple.pudding.R;
import com.enliple.pudding.adapter.my.CustomerSearchAdapter;
import com.enliple.pudding.commons.app.NetworkStatusUtils;
import com.enliple.pudding.commons.db.DBManager;
import com.enliple.pudding.commons.internal.AppPreferences;
import com.enliple.pudding.commons.log.Logger;
import com.enliple.pudding.commons.network.NetworkApi;
import com.enliple.pudding.commons.network.NetworkBus;
import com.enliple.pudding.commons.network.NetworkBusResponse;
import com.enliple.pudding.commons.network.vo.API41;
import com.enliple.pudding.commons.widget.recyclerview.WrappedLinearLayoutManager;
import com.enliple.pudding.model.CustomerQAModel;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class CustomerCenterSearchActivity extends AbsBaseActivity {
    private static final String FAQLIST_TYPE_SEARCH = "search";
    private static final int REQUEST_GO_CART = 50222;

    private RelativeLayout buttonBack;
    private AppCompatImageButton buttonMessage, buttonCart;
    private AppCompatTextView textViewMessageBadge, textViewCartBadge, result, count;
    private EditText edit;
    private RelativeLayout btnSearch, btnMore;
    private LinearLayout goOneByOne;
    private RecyclerView list;

    private CustomerSearchAdapter adapter;
    private WrappedLinearLayoutManager layoutManager;
    private String searchWord;
    private boolean isLoginUser = false;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_center_search);

        initViews();
    }

    private void initViews() {
        isLoginUser = AppPreferences.Companion.getLoginStatus(CustomerCenterSearchActivity.this);
        adapter = new CustomerSearchAdapter(CustomerCenterSearchActivity.this);

        buttonBack = findViewById(R.id.buttonBack);
        buttonMessage = findViewById(R.id.buttonMessage);
        buttonCart = findViewById(R.id.buttonCart);
        textViewMessageBadge = findViewById(R.id.textViewMessageBadge);
        textViewCartBadge = findViewById(R.id.textViewCartBadge);
        result = findViewById(R.id.result);
        count = findViewById(R.id.count);
        edit = findViewById(R.id.edit);
        btnSearch = findViewById(R.id.btnSearch);
        list = findViewById(R.id.list);
        btnMore = findViewById(R.id.btnMore);
        goOneByOne = findViewById(R.id.goOneByOne);

        layoutManager = new WrappedLinearLayoutManager(CustomerCenterSearchActivity.this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        list.setHasFixedSize(false);
        list.setNestedScrollingEnabled(false);
        list.setLayoutManager(layoutManager);
        list.setAdapter(adapter);

        buttonBack.setOnClickListener(clickListener);
        buttonMessage.setOnClickListener(clickListener);
        buttonCart.setOnClickListener(clickListener);

        btnSearch.setOnClickListener(clickListener);
        btnMore.setOnClickListener(clickListener);

        edit.setImeOptions(EditorInfo.IME_ACTION_DONE);
        edit.setOnEditorActionListener(actionListener);

        EventBus.getDefault().register(this);

//        NetworkBus bus = new NetworkBus(NetworkApi.API41.name());
//        EventBus.getDefault().post(bus);

        if (getIntent() != null) {
            int messageCnt = getIntent().getIntExtra("MESSAGE_CNT", 0);
            int cartCnt = getIntent().getIntExtra("CART_CNT", 0);

            setCartBadgeCount(cartCnt);
            setSmsBadgeCount(messageCnt);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_GO_CART) {
                int cartCnt = data.getIntExtra("CART_CNT", 0);
                setCartBadgeCount(cartCnt);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(NetworkBusResponse data) {
        Logger.e("onMessageEvent faq category data.arg1 " + data.arg1);
//        String e_searchword =  "";
//        try {
//            e_searchword = URLEncoder.encode(searchWord, "UTF-8");
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
        String faqlistKey = "GET/support/faqlist?type=search&keyword=" + searchWord;
        Logger.e("onMessageEvent faq category faqlistKey " + faqlistKey);
        if (data.arg1.equals(faqlistKey)) {
            String str = DBManager.getInstance(this).get(faqlistKey);
            API41 response = new Gson().fromJson(str, API41.class);
            if (adapter != null) {
                adapter.setItems(response.data);
                setResultStr();
            }
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
                    Intent intent = new Intent(CustomerCenterSearchActivity.this, ProductCartActivity.class);
                    startActivityForResult(intent, REQUEST_GO_CART);
                    break;
                case R.id.btnSearch:
                    searchWord = edit.getText().toString();
                    adapter.setSearchWord(searchWord);
                    if (TextUtils.isEmpty(searchWord)) {
                        Toast.makeText(CustomerCenterSearchActivity.this, "검색어를 입력하세요", Toast.LENGTH_SHORT).show();
                    } else {
                        NetworkBus bus = new NetworkBus(NetworkApi.API41.name(), FAQLIST_TYPE_SEARCH, searchWord);
                        EventBus.getDefault().post(bus);

                    }
                    hideKeyboard();
                    break;
                case R.id.btnMore:
//                    adapter.addItems(getData(makeJSON(searchWord)));
//                    setResultStr();
                    break;
                case R.id.goOneByOne:
                    if ( isLoginUser )
                        startActivity(new Intent(getApplicationContext(), CenterAskingActivity.class));
                    else
                        goLogin();
                    break;
            }
        }
    };

    private void goLogin() {
        startActivity(new Intent(CustomerCenterSearchActivity.this, LoginActivity.class));
    }

    private ArrayList<CustomerQAModel> getData(String str) {
        if (TextUtils.isEmpty(str))
            return null;
        ArrayList<CustomerQAModel> modelArray = new ArrayList<CustomerQAModel>();
        try {
            if (TextUtils.isEmpty(str))
                return null;
            JSONObject object = new JSONObject(str);
            if (object != null) {
                JSONArray array = object.optJSONArray("list");
                if (array != null && array.length() > 0) {
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject subObject = array.optJSONObject(i);
                        String question = subObject.optString("question");
                        String answer = subObject.optString("answer");

                        CustomerQAModel model = new CustomerQAModel();
                        model.setQuestion(question);
                        model.setAnswer(answer);

                        modelArray.add(model);
                    }
                }
            }
            btnMore.setVisibility(View.VISIBLE);
            return modelArray;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String makeJSON(String searchWord) {
        if (TextUtils.isEmpty(searchWord))
            return "";
        try {
            JSONObject object = new JSONObject();
            JSONArray array = new JSONArray();

            for (int i = 0; i < 15; i++) {
                JSONObject subObject = new JSONObject();
                String question;
                if (i % 4 == 0)
                    question = "빠른" + searchWord + "서비스란 무엇인가요?";
                else if (i % 4 == 1)
                    question = "휴대폰 결제 후" + searchWord + " 취소되면 " + searchWord + "은 어떻게 받나요?";
                else if (i % 4 == 2)
                    question = "신용카드(체크카드)로 결제했는데 " + searchWord + "은 어찌해야하나요?";
                else
                    question = searchWord + "되었다고 하는데 통장으로 입금된 돈이 없어요";
                subObject.put("question", question);
                subObject.put("answer", "*쿠폰 적용 방법*\n주문서 페이지에서 [쿠폰적용]버튼을 클릭한 후 팝업창에서 이용원하시는 쿠폰을 선택하고 [쿠폰적용]버튼을 누르면 쿠폰 적용이 완료됩니다.\n\n* 일부 카테고리 상품의 경우 쿠폰을 적용할 수 없습니다.\n(순금/골드바/돌반지/상품권 등 환금성 카테고리 및 중고 상품/중고장터 상품)\n*중복쿠폰은 판매자 할인, 옥션 할인, 카드사별 추가할인과 중복으로 적용 가능하며, 상품당 1장의 중복쿠폰만 적용 가능합니다. (단, 할인적용금액이 1,000원 미만일 경우중복쿠폰 적용 불가)");
                array.put(subObject);
            }
            object.put("list", array);
            return object.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private TextView.OnEditorActionListener actionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                searchWord = edit.getText().toString();
                adapter.setSearchWord(searchWord);
                if (TextUtils.isEmpty(searchWord)) {
                    Toast.makeText(CustomerCenterSearchActivity.this, "검색어를 입력하세요", Toast.LENGTH_SHORT).show();
                } else {
                    NetworkBus bus = new NetworkBus(NetworkApi.API41.name(), FAQLIST_TYPE_SEARCH, searchWord);
                    EventBus.getDefault().post(bus);
                }
                hideKeyboard();
            }
            return false;
        }
    };

    private void setResultStr() {
        if (adapter == null)
            return;
        int adapterCount = adapter.getItemCount();
        int ct = 0;
        if (adapterCount > 0) {
            ct = adapter.getItemCount();
        }
        String countStr = ct + "개";
        result.setText(searchWord);
        count.setText(countStr);
    }

    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edit.getWindowToken(), 0);
    }

    @Override
    public void onNetworkStatusChanged(@NotNull NetworkStatusUtils.NetworkStatus status) {

    }

    @Override
    public void onDozeModeStateChanged(boolean dozeEnable) {

    }
}
