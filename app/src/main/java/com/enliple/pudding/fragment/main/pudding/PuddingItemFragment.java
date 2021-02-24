package com.enliple.pudding.fragment.main.pudding;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.enliple.pudding.R;
import com.enliple.pudding.activity.EventDetailActivity;
import com.enliple.pudding.activity.EventDetailTabActivity;
import com.enliple.pudding.activity.LinkWebViewActivity;
import com.enliple.pudding.activity.ProductDetailActivity;
import com.enliple.pudding.adapter.home.ItemAdapter;
import com.enliple.pudding.commons.app.ShopTreeKey;
import com.enliple.pudding.commons.app.Utils;
import com.enliple.pudding.commons.db.DBManager;
import com.enliple.pudding.commons.internal.AppPreferences;
import com.enliple.pudding.commons.log.Logger;
import com.enliple.pudding.commons.network.NetworkApi;
import com.enliple.pudding.commons.network.NetworkBus;
import com.enliple.pudding.commons.network.NetworkBusResponse;
import com.enliple.pudding.commons.network.NetworkConst;
import com.enliple.pudding.commons.network.NetworkHandler;
import com.enliple.pudding.commons.network.vo.API139;
import com.enliple.pudding.commons.network.vo.API70;
import com.enliple.pudding.commons.shoptree.network.ShopTreeAsyncTask;
import com.enliple.pudding.commons.widget.recyclerview.WrappedLinearLayoutManager;
import com.enliple.pudding.model.SecondCategoryItem;
import com.enliple.pudding.model.ThirdCategoryItem;
import com.enliple.pudding.model.ThreeCategoryItem;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class PuddingItemFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private static final int REQUEST_NO = 20;
    private boolean mIsVisibleToUser = false;
    private RecyclerView recyclerLiveProduct;
    private SwipeRefreshLayout refreshLayout;
    private AppCompatImageView goTop;
    private ItemAdapter adapter;
    private String order = "1";
    private String mainOrder = "";
    private int mCount = 1;
    private boolean isMore = false;
    private String selectedCategory = "";
    private String searchWord = "";
    WrappedLinearLayoutManager layoutManager;
    private boolean isEndOfData = false;
    private boolean isFirstLoad = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pudding_item, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EventBus.getDefault().register(this);

        adapter = new ItemAdapter(getActivity(), false, false, new ItemAdapter.Listener() {
            @Override
            public void bannerClicked(API70.EventItem item) {
                if ("Y".equals(item.is_tab)) {
                    Intent intent = new Intent(getActivity(), EventDetailTabActivity.class);
                    intent.putExtra(EventDetailActivity.INTENT_KEY_EVENT_ID, item.ev_id);
                    intent.putExtra(EventDetailActivity.INTENT_KEY_EVENT_TYPE, item.ev_type);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(getActivity(), EventDetailActivity.class);
                    intent.putExtra(EventDetailActivity.INTENT_KEY_EVENT_ID, item.ev_id);
                    intent.putExtra(EventDetailActivity.INTENT_KEY_EVENT_TYPE, item.ev_type);
                    startActivity(intent);
                }
            }

            @Override
            public void categoryClicked(String categoryId) {
                selectedCategory = categoryId;
                mCount = 1;
                isMore = false;
                searchWord = "";
                requestList(mCount, selectedCategory, null, REQUEST_NO, searchWord);
            }

            @Override
            public void onSortClicked(String od) {
                Logger.e("order :: " + order);
                Logger.e("od :: " + od);
                order = od;
                mCount = 1;
                isMore = false;
                requestList(mCount, selectedCategory, null, REQUEST_NO, searchWord);
            }

            @Override
            public void onSort2Clicked(String od) {
                mainOrder = od;
                mCount = 1;
                isMore = false;
                requestList(mCount, selectedCategory, null, REQUEST_NO, searchWord);
            }

            @Override
            public void onLikeClick(API70.ProductItem info, boolean status) {
                String idx = info.idx;
                processZzimStatus(idx, info.strType, status);
            }

            @Override
            public void onItemClick(API70.ProductItem info) {
                Logger.e("name :: " + info.title);
                Logger.e("price :: " + info.price);
                Logger.e("image :: " + info.image1);
                Logger.e("storeName :: " + info.sitename);
                Logger.e("storeName :: " + info.strType);
                if ("1".equals(info.strType)) {
                    //                Intent intent1 = new Intent(Intent.ACTION_VIEW);
//                intent1.setComponent(new ComponentName("com.enliple.pudding", "com.enliple.pudding.activity.ProductDetailActivity"));
                    Intent intent1 = new Intent(getActivity(), ProductDetailActivity.class);
                    Logger.e("idx val :: " + info.idx);
                    intent1.putExtra("from_live", false);
                    intent1.putExtra("from_store", false);
                    intent1.putExtra("idx", info.idx);
                    intent1.putExtra("name", info.title);
                    intent1.putExtra("price", Utils.ToNumFormat((int) Double.parseDouble(info.price)) + "원");
                    intent1.putExtra("image", info.image1);
                    intent1.putExtra("storeName", info.sitename);
                    intent1.putExtra(ShopTreeKey.KEY_PCODE, info.pcode);
                    intent1.putExtra(ShopTreeKey.KEY_SCCODE, info.sc_code);
                    startActivity(intent1);
                } else if ("2".equals(info.strType)) {

                } else if ("3".equals(info.strType)) {
                    ShopTreeAsyncTask task = new ShopTreeAsyncTask(getActivity());
                    task.getDRCLink(info.idx, "", "", info.strType, new ShopTreeAsyncTask.OnDefaultObjectCallbackListener() {
                        @Override
                        public void onResponse(boolean result, Object obj) {
                            if (result) {
                                try {
                                    JSONObject object = (JSONObject) obj;
                                    API139 response = new Gson().fromJson(object.toString(), API139.class);
                                    if ("success".equals(response.result)) {
                                        Intent intent = new Intent(getActivity(), LinkWebViewActivity.class);
                                        intent.putExtra("LINK", response.url);
                                        intent.putExtra("IDX", info.idx);
                                        intent.putExtra("IS_WISH", info.is_wish);
                                        intent.putExtra("TYPE", info.strType);
                                        intent.putExtra("TITLE", info.title);
                                        intent.putExtra("ITEM_LINK", true);
                                        startActivity(intent);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                }
            }
        });
        goTop = view.findViewById(R.id.goTop);
        goTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerLiveProduct.scrollToPosition(0);
            }
        });
        recyclerLiveProduct = view.findViewById(R.id.recyclerLiveProduct);
        layoutManager = new

                WrappedLinearLayoutManager(getActivity());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerLiveProduct.setHasFixedSize(false);
        recyclerLiveProduct.setLayoutManager(layoutManager);
        recyclerLiveProduct.setAdapter(adapter);

        recyclerLiveProduct.addOnScrollListener(scrollListener);

        refreshLayout = view.findViewById(R.id.refreshLayout);
        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
        );
    }

    private RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int lastVisibleItemPosition = layoutManager.findLastCompletelyVisibleItemPosition();
            int visibleCount = layoutManager.findFirstVisibleItemPosition();
            int totalItemCount = adapter.getItemCount() - 1;
            Logger.e("scroll lastVisibleItemPosition :: " + lastVisibleItemPosition);
            Logger.e("scroll totalItemCount :: " + totalItemCount);
            Logger.e("scroll isEndOfData :: " + isEndOfData);
            Logger.e("scroll dx :: " + dx);
            Logger.e("scroll dy :: " + dy);
            if ((lastVisibleItemPosition == totalItemCount) && !isEndOfData && dy > 0) {
                isMore = true;
                mCount++;
                requestList(mCount, selectedCategory, null, REQUEST_NO, searchWord);
            }

            if (goTop.getVisibility() == View.GONE) {
                if (visibleCount > 8)
                    goTop.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            Logger.e("scroll onScrollStateChanged :: newState :: " + newState);
            int state = newState;
            if (state == RecyclerView.SCROLL_STATE_IDLE) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (state == RecyclerView.SCROLL_STATE_IDLE)
                            goTop.setVisibility(View.GONE);
                    }
                }, 1500);
            }
        }
    };

    public void setLikeChaged(String idx, String status) {
        if (adapter != null) {
            adapter.setLikeChaged(idx, status);
        }
    }

//    private androidx.recyclerview.widget.RecyclerView.OnScrollListener scrollListener = new androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
//        @Override
//        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
//            super.onScrollStateChanged(recyclerView, newState);
//            if (!recyclerLiveProduct.canScrollVertically(1)) {
//                Logger.e("bot reach");
//            } else {
//                Logger.w("not reach");
//            }
//        }
//
//        @Override
//        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
//            super.onScrolled(recyclerView, dx, dy);
//        }
//    };

    public void onPause() {
        super.onPause();
    }

    @Override
    public void onRefresh() {
        Logger.d("onRefresh");

        requestList(mCount, selectedCategory, null, REQUEST_NO, searchWord);

        refreshLayout.setRefreshing(false);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Logger.i("setUserVisibleHint: " + isVisibleToUser);

        mIsVisibleToUser = isVisibleToUser;

        if (isVisibleToUser && !isFirstLoad) {
            isFirstLoad = true;
            init();
            requestList(mCount, selectedCategory, null, REQUEST_NO, searchWord);
        }
    }

    private void requestList(int count, String cId, String skey, int requestNo, String searchWord) {
        Logger.e(" ******************** ");
        Logger.e("requestList count :: " + count);
        Logger.e("requestList cId :: " + cId);
        Logger.e("requestList skey :: " + skey);
        Logger.e("requestList isMore :: " + isMore);
        Logger.e("requestList order :: " + order);
        NetworkBus bus = new NetworkBus(NetworkApi.API70.name(), count + "", requestNo + "",
                TextUtils.isEmpty(cId) ? "" : cId,
                skey == null ? "" : skey, searchWord,
                order, mainOrder);
        EventBus.getDefault().post(bus);
    }

    private void getProductList(NetworkBusResponse data) {
        if (data.arg2.equals("ok")) {
            String str = DBManager.getInstance(getActivity()).get(data.arg1);
            API70 response = new Gson().fromJson(str, API70.class);
            Logger.e("totalCount ::::::: " + response.total_cnt);
            isEndOfData = response.goodsList.size() == 0;
//            if (response.goodsList.size() > 0)
//                mCount++;
            if (isMore) {
                adapter.addItems(response.goodsList);
            } else {
                adapter.setItems(response.goodsList, response.event, response.total_cnt);
            }
        }
    }

    private void init() {
        NetworkBus bus = new NetworkBus(NetworkApi.API81.name(), "select", "");// 전체 카테고리 가져오기
        EventBus.getDefault().post(bus);

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(NetworkBusResponse data) {
        Logger.e("mIsVisibleToUser :: " + mIsVisibleToUser);
        // 장바구니 -> 상품상세 -> 스토어 -> 상품상세 경로에서 zzim 상태 변경이 될 수 있음
        // 장바구니 버튼은 메인의 어느 tab에서나 노출될 수 있으므로 PuddingItemFragment가 visible to user 가 false 인 상황에서도 data가 갱신 가능해야함
        if (data.arg1.startsWith("POST/products/") && data.arg1.endsWith("wish")) {
            Logger.e(" data arg1 is :: " + data.arg1);
            String[] arr = data.arg1.split("/");
            String idx = arr[2];
            Logger.e("idx :: " + idx);
            if (adapter != null) {
                adapter.likeSuccess(idx);
            }
        } else {
            if (!mIsVisibleToUser)
                return;
            String API81 = NetworkHandler.Companion.getInstance(getActivity()).getKey(NetworkApi.API81.toString(), "select", "");
            String API70 = "GET/products?page";
            API70 = NetworkHandler.Companion.getInstance(getActivity()).getProductKey(NetworkApi.API70.toString(), "" + mCount, "" + REQUEST_NO, selectedCategory, "", searchWord, order, mainOrder);

            Logger.e("data.arg1 :: " + data.arg1);
            Logger.e("API81 :: " + API81);
            Logger.e("API70 :: " + API70);
            if (data.arg1.equals(API81))
                setCategory(data);
            else if (data.arg1.equals(API70))
                getProductList(data);
        }

//        String API81 = NetworkHandler.Companion.getInstance(getActivity()).getKey(NetworkApi.API81.toString(), "select", "");
//        String API70 = "GET/products?page";
//        API70 = NetworkHandler.Companion.getInstance(getActivity()).getProductKey(NetworkApi.API70.toString(), "" + mCount, "" + REQUEST_NO, selectedCategory, "", searchWord, order);
//
//        Logger.e("data.arg1 :: " + data.arg1);
//        Logger.e("API81 :: " + API81);
//        Logger.e("API70 :: " + API70);
//        if (data.arg1.equals(API81))
//            setCategory(data);
//        else if (data.arg1.equals(API70))
//            getProductList(data);
//        else if (data.arg1.startsWith("POST/products/") && data.arg1.endsWith("wish")) {
//            String[] arr = data.arg1.split("/");
//            String idx = arr[2];
//            Logger.e("idx :: " + idx);
//            if (adapter != null) {
//                adapter.likeSuccess(idx);
//            }
//        }
    }

    private void setCategory(NetworkBusResponse data) {
        if (data.arg2.equals("ok")) {
            String str = DBManager.getInstance(getActivity()).get(data.arg1);
            Logger.e("str :: " + str);
//            str = TempJSON.JSON;
            try {
                JSONObject obj = new JSONObject(str);
                JSONArray array = obj.optJSONArray("data");
                ArrayList<ThreeCategoryItem> itemArray = new ArrayList<>();

                // 1차 카테고리 전체를 add
                ThreeCategoryItem firstAll = new ThreeCategoryItem();
                firstAll.setCategoryId("");
                firstAll.setCategoryName("전체");
                firstAll.setSelected(true);
                firstAll.setCategoryImage(NetworkConst.CATEGORY_ALL_IMAGE_API);
                firstAll.setCategoryImageOn(NetworkConst.CATEGORY_ALL_ON_IMAGE_API);
                firstAll.setCategoryImageOff(NetworkConst.CATEGORY_ALL_OFF_IMAGE_API);
                firstAll.setSecondCategory(null);

                itemArray.add(firstAll);

                if (array != null && array.length() > 0) {
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject object = array.optJSONObject(i);
                        ThreeCategoryItem item = new ThreeCategoryItem();

                        String categoryId = object.optString("categoryId");
                        String categoryName = object.optString("categoryName");
                        String categoryImage = object.optString("categoryImage");
                        String categoryImageOn = object.optString("categoryImageOn");
                        String categoryImageOff = object.optString("categoryImageOff");
                        boolean selected = false;
                        ArrayList<SecondCategoryItem> secondCategoryArray = null;

                        JSONArray secondArray = object.optJSONArray("sub");
                        if (secondArray != null && secondArray.length() > 0) {
                            secondCategoryArray = new ArrayList<>();
                            SecondCategoryItem secondAllItem = new SecondCategoryItem();
                            secondAllItem.setCategoryId(categoryId);
                            secondAllItem.setCategoryName("전체");
                            secondAllItem.setSelected(true);
                            secondAllItem.setThirdCategory(null);

                            secondCategoryArray.add(secondAllItem);

                            for (int j = 0; j < secondArray.length(); j++) {
                                JSONObject secondObject = secondArray.optJSONObject(j);
                                SecondCategoryItem secondItem = new SecondCategoryItem();
                                String s_categoryId = secondObject.optString("categoryId");
                                String s_categoryName = secondObject.optString("categoryName");
                                boolean s_selected = false;

                                ArrayList<ThirdCategoryItem> thirdCategoryArray = null;

                                JSONArray thirdArray = secondObject.optJSONArray("sub");
                                if (thirdArray != null && thirdArray.length() > 0) {
                                    thirdCategoryArray = new ArrayList<>();
                                    ThirdCategoryItem thirdAllItem = new ThirdCategoryItem();
                                    thirdAllItem.setCategoryId(s_categoryId);
                                    thirdAllItem.setCategoryName("전체");
                                    thirdAllItem.setSelected(true);

                                    thirdCategoryArray.add(thirdAllItem);

                                    for (int k = 0; k < thirdArray.length(); k++) {
                                        JSONObject thirdObject = thirdArray.optJSONObject(k);
                                        ThirdCategoryItem thirdItem = new ThirdCategoryItem();
                                        String t_categoryId = thirdObject.optString("categoryId");
                                        String t_categoryName = thirdObject.optString("categoryName");
                                        boolean t_selected = false;

                                        thirdItem.setCategoryId(t_categoryId);
                                        thirdItem.setCategoryName(t_categoryName);
                                        thirdItem.setSelected(t_selected);

                                        thirdCategoryArray.add(thirdItem);
                                    }
                                }
                                secondItem.setCategoryId(s_categoryId);
                                secondItem.setCategoryName(s_categoryName);
                                secondItem.setSelected(s_selected);
                                secondItem.setThirdCategory(thirdCategoryArray);
                                secondCategoryArray.add(secondItem);
                            }
                        }
                        item.setCategoryId(categoryId);
                        item.setCategoryName(categoryName);
                        item.setSelected(selected);
                        item.setCategoryImage(categoryImage);
                        item.setCategoryImageOn(categoryImageOn);
                        item.setCategoryImageOff(categoryImageOff);
                        item.setSecondCategory(secondCategoryArray);
                        itemArray.add(item);
                    }
                }
                adapter.setCategoryItem(itemArray);

                Logger.e("itemArray.size :: " + itemArray.size());


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void processZzimStatus(String idx, String type, boolean status) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("user", AppPreferences.Companion.getUserId(getActivity()));
            obj.put("is_wish", status == true ? "Y" : "N");
            obj.put("type", type);

            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), obj.toString());
            NetworkBus bus = new NetworkBus(NetworkApi.API126.name(), idx, body);
            EventBus.getDefault().post(bus);
        } catch (Exception e) {
            Logger.p(e);
        }
    }
}