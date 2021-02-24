package com.enliple.pudding.fragment.main.pudding;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.couchbase.lite.MutableDocument;
import com.enliple.pudding.R;
import com.enliple.pudding.activity.EventDetailActivity;
import com.enliple.pudding.activity.ShoppingPlayerActivity;
import com.enliple.pudding.adapter.home.FoodAdapter;
import com.enliple.pudding.common.VideoDataContainer;
import com.enliple.pudding.commons.db.DBManager;
import com.enliple.pudding.commons.internal.AppPreferences;
import com.enliple.pudding.commons.log.Logger;
import com.enliple.pudding.commons.network.NetworkApi;
import com.enliple.pudding.commons.network.NetworkBus;
import com.enliple.pudding.commons.network.NetworkBusResponse;
import com.enliple.pudding.commons.network.NetworkHandler;
import com.enliple.pudding.commons.network.vo.API114;
import com.enliple.pudding.commons.network.vo.API98;
import com.enliple.pudding.commons.network.vo.VOD;
import com.enliple.pudding.commons.shoptree.network.ShopTreeAsyncTask;
import com.enliple.pudding.commons.widget.recyclerview.WrappedLinearLayoutManager;
import com.enliple.pudding.commons.widget.toast.AppToast;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class PuddingFoodFragment extends Fragment implements FoodAdapter.Listener {
    private LinearLayoutManager layoutManager;
    private RecyclerView itemRecyclerView;
    private ProgressBar progressBar;
    private AppCompatImageView topBtn;
    private FoodAdapter itemAdapter;
    private List<API114.EventItem> bannerItems;
    private List<API114.VideoItem> items;
    private API114.VideoItem likeClickItem;
    private String categoryStr = null;
    private boolean mIsFirst = false;
    private boolean mIsVisibleToUser = false;
    private String selectedCategory = "";
    private boolean isMore = false;
    private API114 tempResponse = null;
    private boolean isEndOfData = false;
    private int mCount = 1;
    private String order = "1";
    private int mPosition = -1;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pudding_travel, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        EventBus.getDefault().register(this);
        registerReceiver();
        itemRecyclerView = view.findViewById(R.id.itemRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        topBtn = view.findViewById(R.id.topBtn);
        topBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemRecyclerView.scrollToPosition(0);
            }
        });

        itemRecyclerView.addOnScrollListener(scrollListener);
        layoutManager = new WrappedLinearLayoutManager(getActivity());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        itemRecyclerView.setHasFixedSize(false);
        itemRecyclerView.setLayoutManager(layoutManager);

        itemAdapter = new FoodAdapter(getActivity());
        itemRecyclerView.setAdapter(itemAdapter);
        itemAdapter.setListener(PuddingFoodFragment.this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        unregisterReceiver();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        mIsVisibleToUser = isVisibleToUser;
        Logger.i("setUserVisibleHint: " + mIsVisibleToUser);

        if (mIsVisibleToUser) {
            if (!mIsFirst) {
                if ( progressBar != null )
                    progressBar.setVisibility(View.VISIBLE);
                mIsFirst = true;
                EventBus.getDefault().post(new NetworkBus(NetworkApi.API81.name(), "select_union", "", "food"));
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        refresh();
    }

    @Subscribe
    public void onMessageEvent(String data) {
        if (data.startsWith("refresh")) {
            refresh();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(NetworkBusResponse data) {
        if(data.arg1.startsWith("POST/products/") && data.arg1.endsWith("wish")) {
            String[] arr = data.arg1.split("/");
            String s_idx = arr[2];
            Logger.e("s_idx");
            if ( itemAdapter != null ) {
                itemAdapter.changeZzim(s_idx);
            }
        }

        if (!mIsVisibleToUser)
            return;

        if(getParentFragment() != null && !getParentFragment().getUserVisibleHint()) {
            return;
        }

        String API81 = NetworkHandler.Companion.getInstance(getActivity()).getKey(NetworkApi.API81.toString(), "select_union", "", "food");

        API114 response = null;
        if (data.arg1.startsWith("GET/mui/main/food?page=")) {
            if ( progressBar != null )
                progressBar.setVisibility(View.GONE);
            response = new Gson().fromJson(DBManager.getInstance(getActivity()).get(data.arg1), API114.class);
            if (tempResponse == null)
                tempResponse = response;
            bannerItems = response.event;
            items = response.data;

            if (!isMore) {
                itemAdapter.setCategory(categoryStr);
                Logger.e("categoryStr :: " + categoryStr);
                if( itemRecyclerView.getVisibility() == View.INVISIBLE )
                    itemRecyclerView.setVisibility(View.VISIBLE);
                itemAdapter.setItem(response.data, response.event);
            } else {
                itemAdapter.addItems(response.data);
            }

            Logger.e("banner item size = " + bannerItems.size());
            Logger.e("item size = " + items.size());
            isEndOfData = response.data.size() < response.nDataCount;
        } else if (data.arg1.equals(API81)) {
            categoryStr = DBManager.getInstance(getActivity()).get(data.arg1);
            Logger.e("categoryStr :: " + categoryStr);
            if (TextUtils.isEmpty(selectedCategory) ) {
                try {
                    JSONObject object = new JSONObject(categoryStr);
                    JSONArray array = object.optJSONArray("data");
                    JSONObject firstData = array.optJSONObject(0);
                    selectedCategory = firstData.optString("categoryId");
                    itemAdapter.setFirstCategory(selectedCategory);
                    Logger.e("selectedCategory :: " + selectedCategory);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            EventBus.getDefault().post(new NetworkBus(NetworkApi.API114.name(), "food", mCount + "", selectedCategory, order));
        }
    }

    @Override
    public void onItemClick(API114.VideoItem item, int position) {
        mPosition = position;

        // 페이징 및 dbKey 포지션 문제 때문에 앞으로 아래와 같이 List 객체를 만들어서 영상 데이터 사용
        String json = new Gson().toJson(itemAdapter.getMItems());
        List<VOD.DataBeanX> videoItems = new Gson().fromJson(json, new TypeToken<List<VOD.DataBeanX>>(){}.getType());
        VideoDataContainer.Companion.getInstance().setMVideoData(videoItems);

        if(item.videoType.equals("LIVE")) {
            ShopTreeAsyncTask task = new ShopTreeAsyncTask(getActivity());
            task.getLiveInfo(item.id, new ShopTreeAsyncTask.OnDefaultObjectCallbackListener() {
                @Override
                public void onResponse(boolean result, Object obj) {
                    try {
                        API98 response = new Gson().fromJson(obj.toString(), API98.class);
                        if(response.data.size() > 0) {
                            if("Y" == response.data.get(0).isOnAir) {
                                Intent intent = new Intent(getActivity(), ShoppingPlayerActivity.class);
                                intent.putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_POSITION, mPosition);
                                intent.putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_CASTER_ID, item.userId);
                                startActivity(intent);
                            } else {
                                new AppToast(getActivity()).showToastMessage("종료된 라이브 방송입니다.",
                                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                                        AppToast.GRAVITY_BOTTOM);
                            }
                        } else {
                            new AppToast(getActivity()).showToastMessage("종료된 라이브 방송입니다.",
                                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                                    AppToast.GRAVITY_BOTTOM);
                        }
                    } catch (Exception e) {
                        Logger.p(e);
                    }
                }
            });
        } else {
            // ShoppingPlayerActivity로 데이터 넘길 때 position 은 position -2 값을 넘긴다.
            // 실제 첫번째 데이터의 포지션이 2 이므로 (0 : event layer, 1 : category layer)
            Intent intent = new Intent(getActivity(), ShoppingPlayerActivity.class);
            intent.putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_POSITION, position);
            intent.putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_CASTER_ID, item.userId);
            startActivity(intent);
        }
    }

    @Override
    public void onBannerClick(API114.EventItem item) {
        Intent intent = new Intent(getActivity(), EventDetailActivity.class);
        intent.putExtra(EventDetailActivity.INTENT_KEY_EVENT_ID, item.ev_id);
        intent.putExtra(EventDetailActivity.INTENT_KEY_EVENT_TYPE, item.ev_type);
        Logger.e("ev_id :: " + item.ev_id);
        Logger.e("ev_type :: " + item.ev_type);
        startActivity(intent);
    }

    @Override
    public void categoryClicked(String category) {
        if (!category.equals(selectedCategory)) {
            itemAdapter.setCategoryClick(true);
            selectedCategory = category;
            // 카테고리 클릭 시 아이템 리스트 갱신 시킴
            mCount = 1;
            isMore = false;
            EventBus.getDefault().post(new NetworkBus(NetworkApi.API114.name(), "food", mCount + "", selectedCategory, order));
        }
    }

    private void refresh() {
        if (mIsVisibleToUser && getParentFragment().getUserVisibleHint()) {
            progressBar.setVisibility(View.VISIBLE);
            mCount = 1;
            isMore = false;
            EventBus.getDefault().post(new NetworkBus(NetworkApi.API114.name(), "food", mCount + "", selectedCategory, order));
        }
    }

    private RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int lastVisibleItemPosition = layoutManager.findLastCompletelyVisibleItemPosition();
            int visibleCount = layoutManager.findFirstVisibleItemPosition();
            int totalItemCount = itemAdapter.getItemCount() - 1;
            Logger.e("scroll lastVisibleItemPosition :: " + lastVisibleItemPosition);
            Logger.e("scroll totalItemCount :: " + totalItemCount);
            Logger.e("scroll isEndOfData :: " + isEndOfData);
            Logger.e("scroll dx :: " + dx);
            Logger.e("scroll dy :: " + dy);

//            if (visibleCount > 8) {
//                topBtn.setVisibility(View.VISIBLE);
//            } else {
//                topBtn.setVisibility(View.GONE);
//            }

            if ( topBtn.getVisibility() == View.GONE ) {
                if ( visibleCount > 8 )
                    topBtn.setVisibility(View.VISIBLE);
            }

            if ((lastVisibleItemPosition == totalItemCount) && !isEndOfData && dy > 0) {
                isMore = true;
                mCount++;

                new AppToast(getActivity()).showToastMessage("loading 중...",
                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                        AppToast.GRAVITY_BOTTOM);

                EventBus.getDefault().post(new NetworkBus(NetworkApi.API114.name(), "food", mCount + "", selectedCategory, order));
            }
        }

        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            if (newState == androidx.viewpager.widget.ViewPager.SCROLL_STATE_IDLE) {
                ((PuddingHomeTabFragment)getParentFragment()).enableSwipeRefresh(true);
//                (parentFragment as PuddingHomeTabFragment).enableSwipeRefresh(true)
            } else {
//                (parentFragment as PuddingHomeTabFragment).enableSwipeRefresh(false)
                ((PuddingHomeTabFragment)getParentFragment()).enableSwipeRefresh(false);
            }

            int state = newState;
            if (state == RecyclerView.SCROLL_STATE_IDLE) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (state == RecyclerView.SCROLL_STATE_IDLE)
                            topBtn.setVisibility(View.GONE);
                    }
                }, 1500);
            }
        }
    };

    @Override
    public void onSortClicked(@NotNull String order) {
        this.order = order;
        mCount = 1;
        isMore = false;
        EventBus.getDefault().post(new NetworkBus(NetworkApi.API114.name(), "food", mCount + "", selectedCategory, order));
    }

    private void registerReceiver() {
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, new IntentFilter("likeChange"));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, new IntentFilter("PAGE_CHANGED"));
    }

    private void unregisterReceiver() {
        try {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private BroadcastReceiver receiver = new  BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ( action == "likeChange") {
                if ( itemAdapter == null )
                    return;
                else {
                    if ( itemAdapter.getItems() == null )
                        return;
                    else {
                        if ( itemAdapter.getItems().size() <= 0 )
                            return;
                    }
                }

                String obj = intent.getStringExtra("change");
                Logger.e("obj ::: " + obj);
                try {
                    JSONArray arr = new JSONArray(obj);
                    int arrsize = arr.length();
                    if ( arrsize > 0 ) {
                        int itemSize = itemAdapter.getItemSize();
                        int length = arr.length();
                        for ( int i = 0 ; i < itemSize ; i ++ ) {
                            API114.VideoItem item = itemAdapter.getItems().get(i);
                            for ( int j = 0 ; j < length ; j ++  ) {
                                JSONObject modelObj = arr.optJSONObject(j);
                                if ( item.id == modelObj.optString("streamKey") ) {
                                    API114.VideoItem it = item;
                                    it.favoriteCount = modelObj.optString("cnt");
                                    itemAdapter.setItem(i, it);
                                }
                            }
                        }
                        itemAdapter.notifyDataSetChanged();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (action == "PAGE_CHANGED") {
                int page = intent.getIntExtra("page", -1);
                String gubun = intent.getStringExtra("gubun");
                if ( gubun == "run") {
                    if ( page == PuddingHomeTabFragment.PAGE_FOOD && itemAdapter != null ) {
                        itemAdapter.runTimer();
                    }
                } else if ( gubun == "cancel") {
                    itemAdapter.cancelTimer();
                } else if ( gubun == "paging") {
                    if (itemAdapter != null ) {
                        if ( page == PuddingHomeTabFragment.PAGE_FOOD ) {
                            itemAdapter.runTimer();
                        } else {
                            itemAdapter.cancelTimer();
                        }
                    }
                }
            }
        }
    };
}