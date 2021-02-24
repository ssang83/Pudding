package com.enliple.pudding.fragment.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.enliple.pudding.R;
import com.enliple.pudding.activity.ShoppingPlayerActivity;
import com.enliple.pudding.adapter.home.PreviousProductAdapter;
import com.enliple.pudding.common.VideoDataContainer;
import com.enliple.pudding.commons.app.Utils;
import com.enliple.pudding.commons.db.DBManager;
import com.enliple.pudding.commons.internal.AppPreferences;
import com.enliple.pudding.commons.log.Logger;
import com.enliple.pudding.commons.network.NetworkApi;
import com.enliple.pudding.commons.network.NetworkBus;
import com.enliple.pudding.commons.network.NetworkBusResponse;
import com.enliple.pudding.commons.network.NetworkHandler;
import com.enliple.pudding.commons.network.vo.API34;
import com.enliple.pudding.commons.network.vo.VOD;
import com.enliple.pudding.commons.widget.recyclerview.WrappedLinearLayoutManager;
import com.enliple.pudding.commons.widget.toast.AppToast;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.greenfrvr.hashtagview.HashtagView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PreviousProductFragment extends Fragment {
    private static final int RECYCLER_VIEW_ITEM_CACHE_SIZE = 20;
    private static final int PAGE_DATA_COUNT = 100;

    private RecyclerView recyclerView;
    private PreviousProductAdapter adapter;
    private WrappedLinearLayoutManager layoutManager;
    //    private AppCompatTextView quantity, favorite, recent;
    private String mIsFavorite = "0";
    private String streamKey = "";
    private String dataKey;
    private HashtagView hashTag;
    private boolean isEndOfData = true;
    private int pageCount = 1;
    private boolean isReload = false;
    //    private NestedScrollView scrollView;
    private String mMyUserId = "";

    private RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int lastVisibleItemPosition = layoutManager.findLastCompletelyVisibleItemPosition();
            int totalItemCount = adapter.getItemCount() - 1;
            if ((lastVisibleItemPosition == totalItemCount) && !isEndOfData && dy > 0) {
                isReload = true;
                ++pageCount;
                NetworkBus bus = new NetworkBus(NetworkApi.API34.name(), streamKey, mIsFavorite, mMyUserId, String.valueOf(pageCount));
                EventBus.getDefault().post(bus);
            }
        }

        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
        }
    };

//    private NestedScrollView.OnScrollChangeListener scrollChangeListener = (view, scollX, scrollY, oldScrollX, oldScrollY) -> {
//        if (scrollY == (view.getChildAt(0).getMeasuredHeight() - view.getMeasuredHeight())) {
//            Logger.d("Bottom Scroll");
//            if (!isEndOfData) {
//                isReload = true;
//                ++pageCount;
//                NetworkBus bus = new NetworkBus(NetworkApi.API34.name(), streamKey, mIsFavorite, mMyUserId, String.valueOf(pageCount));
//                EventBus.getDefault().post(bus);
//            }
//        }
//    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMyUserId = AppPreferences.Companion.getUserId(getActivity());

        EventBus.getDefault().register(this);
        registerReceiver();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_previous_product, container, false);

//        quantity = view.findViewById(R.id.quantity);
//        favorite = view.findViewById(R.id.favorite);
//        recent = view.findViewById(R.id.recent);

//        scrollView = view.findViewById(R.id.scrollView);
//        scrollView.setOnScrollChangeListener(scrollChangeListener);

        hashTag = view.findViewById(R.id.hashTagView);
        hashTag.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/notosanskr_medium.otf"));

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(false);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setItemViewCacheSize(RECYCLER_VIEW_ITEM_CACHE_SIZE);
        recyclerView.addOnScrollListener(scrollListener);

        view.findViewById(R.id.backButton).setOnClickListener(v -> {
            Intent i = new Intent("close_drawer");
            i.putExtra("isLeftClose", true);
            LocalBroadcastManager.getInstance(getContext()).sendBroadcast(i);
        });

        layoutManager = new WrappedLinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new PreviousProductAdapter(getActivity(), new PreviousProductAdapter.ClickCallbackListener() {
            @Override
            public void onItemClick(API34.Data item, int position) {
                Logger.e("URL : " + item.stream + " userId : " + item.userId);

                // 페이징 및 dbKey 포지션 문제 때문에 앞으로 아래와 같이 List 객체를 만들어서 영상 데이터 사용
                String json = new Gson().toJson(adapter.mItems);
                List<VOD.DataBeanX> videoItems = new Gson().fromJson(json, new TypeToken<List<VOD.DataBeanX>>(){}.getType());
                VideoDataContainer.Companion.getInstance().setMVideoData(videoItems);

                if (!TextUtils.isEmpty(item.stream)) {
                    Intent intent = new Intent(getActivity(), ShoppingPlayerActivity.class);
                    intent.putExtra(ShoppingPlayerActivity.FROM_WHERE, ShoppingPlayerActivity.FROM_LEFT);
                    intent.putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_POSITION, position);
                    intent.putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_CASTER_ID, item.userId);
                    intent.putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_VIDEO_TYPE, item.videoType);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

                    intent.setData(Uri.parse("vcommerce://shopping?url=" + item.stream));

                    startActivity(intent);
                } else {
                    new AppToast(getActivity()).showToastMessage("방송 정보 메타 데이터가 존재하지 않습니다.",
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_BOTTOM);
                }
            }

            @Override
            public void onFavoriteClicked() {
                mIsFavorite = "0";
                pageCount = 1;

                if (!TextUtils.isEmpty(streamKey)) {
                    EventBus.getDefault().post(new NetworkBus(NetworkApi.API34.name(), streamKey, mIsFavorite, mMyUserId, String.valueOf(pageCount)));
                }
            }

            @Override
            public void onRecentClicked() {
                mIsFavorite = "1";
                pageCount = 1;

                if (!TextUtils.isEmpty(streamKey)) {
                    EventBus.getDefault().post(new NetworkBus(NetworkApi.API34.name(), streamKey, mIsFavorite, mMyUserId, String.valueOf(pageCount)));
                }
            }
        });
        recyclerView.setAdapter(adapter);

//        favorite.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                favorite.setTextColor(0xff9f56f2);
//                recent.setTextColor(0xffbcc6d2);
//
//                mIsFavorite = "0";
//                pageCount = 1;
//
//                if (!TextUtils.isEmpty(streamKey)) {
//                    EventBus.getDefault().post(new NetworkBus(NetworkApi.API34.name(), streamKey, mIsFavorite, mMyUserId, String.valueOf(pageCount)));
//                }
//            }
//        });
//
//        recent.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                favorite.setTextColor(0xffbcc6d2);
//                recent.setTextColor(0xff9f56f2);
//
//                mIsFavorite = "1";
//                pageCount = 1;
//
//                if (!TextUtils.isEmpty(streamKey)) {
//                    EventBus.getDefault().post(new NetworkBus(NetworkApi.API34.name(), streamKey, mIsFavorite, mMyUserId, String.valueOf(pageCount)));
//                }
//            }
//        });

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
        unregisterReceiver();
    }

    public void loadData(String key, String tag, String isFavorite) {
//        if (quantity != null) {
//            quantity.setText("");
//        }

        if (adapter != null) {
            adapter.clear();
        }

        if (tag != null) {
            String tempTag = tag.replaceAll(" ", "");
            hashTag.setData(Arrays.asList(tempTag.split(",")), new HashtagView.DataStateTransform<String>() {
                @Override
                public CharSequence prepareSelected(String s) {
                    return null;
                }

                @Override
                public CharSequence prepare(String tag) {
                    return new SpannableString("#" + tag);
                }
            });
        } else {
            Logger.e("tag is null");
        }

        streamKey = key;
        try {
            streamKey = URLEncoder.encode(key, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }

//        mIsFavorite = isFavorite;

//        recent.setTextColor(0xff9f56f2);
//        favorite.setTextColor(0xffbcc6d2);

        EventBus.getDefault().post(new NetworkBus(NetworkApi.API34.name(), streamKey, mIsFavorite, mMyUserId, String.valueOf(pageCount)));
    }

    public void init() {
        Logger.c("init");

        if (hashTag != null) {
            hashTag.setData(new ArrayList<>());
        }

        if (adapter != null) {
            adapter.init();
            adapter.setItems(new ArrayList<>());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(NetworkBusResponse data) {
        Logger.e("onMessageEvent:" + data.arg1);

        dataKey = NetworkHandler.Companion.getInstance(getActivity()).getKey(NetworkApi.API34.toString(), streamKey, mIsFavorite);
        if (data.arg1.startsWith(dataKey)) {
            String str = DBManager.getInstance(getActivity()).get(data.arg1);
            API34 response = new Gson().fromJson(str, API34.class);
            List<API34.Data> listData = response.data;
            if (listData != null) {
                pageCount = response.pageCount;
                adapter.setHeader(response.nTotalCount, mIsFavorite);
            } else {
                Logger.e("data null");
            }

            if (!isReload) {
                adapter.setItems(listData);
            } else {
                adapter.addItems(listData);
            }
        } else if(data.arg1.startsWith("POST/products/") && data.arg1.endsWith("wish")) {
            String[] arr = data.arg1.split("/");
            String s_idx = arr[2];
            Logger.e("s_idx");
            if ( adapter != null ) {
                adapter.changeZzim(s_idx);
            }
        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent it) {
            Logger.e("PreviousProductFragment receiver called");
            String action = it.getAction();
            if ( action == "likeChange" ) {
                String obj = it.getStringExtra("change");
                Logger.e("obj ::: " + obj);
                try {
                    JSONArray arr = new JSONArray(obj);
                    int arrsize = arr.length();
                    if ( arrsize > 0 ) {
                        int listSize = adapter.getItemSize();
                        List<API34.Data> items = adapter.getItems();
                        if ( listSize > 0 && items != null ) {
                            for ( int i = 0 ; i < listSize ; i ++ ) {
                                API34.Data item = items.get(i);
                                for ( int j = 0 ; j < arrsize ; j ++ ) {
                                    JSONObject modelObj = arr.optJSONObject(j);
                                    if ( item.id.equals(modelObj.optString("streamKey", "")) ) {
                                        API34.Data tItem = item;
                                        tItem.favoriteCount = modelObj.optString("cnt");
                                        adapter.setItem(i, tItem);
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    };

    private void registerReceiver() {
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, new IntentFilter("likeChange"));
    }

    private void unregisterReceiver() {
        try {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
/**
 private val receiver = object : BroadcastReceiver() {
 override fun onReceive(context: Context, intent: Intent) {
 val action = intent.action
 if ( action == "likeChange") {
 var obj = intent.getStringExtra("change")
 Logger.e("obj ::: " + obj)
 var arr = JSONArray(obj)
 var arrsize = arr.length()
 if ( arrsize > 0 ) {

 var bestSize = mHomeAdapter.bestItems.size
 var hotSize = mHomeAdapter.hotItems.size
 var itemSize = mHomeAdapter.items.size

 for ( i in 0 until bestSize ) {
 var b_item = mHomeAdapter.bestItems.get(i)
 for ( j in 0 until arr.length() ) {
 var modelObj = arr.optJSONObject(j)
 Logger.e("b_item.id :: " + b_item.id)
 Logger.e("streamKey :: " + modelObj.optString("streamKey"))
 Logger.e("*******************")
 if ( b_item.id == modelObj.optString("streamKey") ) {
 var item = b_item
 item.favoriteCount = modelObj.optString("cnt")
 mHomeAdapter.bestItems.set(i, item)
 Logger.e("best exist data set")
 }
 }
 }

 for ( i in 0 until hotSize ) {
 var h_item = mHomeAdapter.hotItems.get(i)
 for ( j in 0 until arr.length() ) {
 var modelObj = arr.optJSONObject(j)
 if ( h_item.id == modelObj.optString("streamKey") ) {
 var item = h_item
 item.favoriteCount = modelObj.optString("cnt")
 mHomeAdapter.hotItems.set(i, item)
 Logger.e("hot exist data set")
 }
 }
 }

 for ( i in 0 until itemSize ) {
 var item = mHomeAdapter.items.get(i)
 for ( j in 0 until arr.length() ) {
 var modelObj = arr.optJSONObject(j)
 Logger.d("item.id :: " + item.id)
 Logger.d("stream  :: " + modelObj.optString("streamKey"))
 if ( item.id == modelObj.optString("streamKey") ) {
 var it = item
 it.favoriteCount = modelObj.optString("cnt")
 mHomeAdapter.items.set(i, it)
 Logger.e("item exist data set")
 }
 }
 }
 mHomeAdapter.notifyDataSetChanged()
 }
 }
 }
 }
**/
}