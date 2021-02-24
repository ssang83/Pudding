package com.enliple.pudding.fragment;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.enliple.pudding.R;
import com.enliple.pudding.activity.LinkWebViewActivity;
import com.enliple.pudding.activity.LoginActivity;
import com.enliple.pudding.activity.ProductDetailActivity;
import com.enliple.pudding.adapter.DialogProductListAdapter;
import com.enliple.pudding.bus.VideoMuteBus;
import com.enliple.pudding.bus.VideoPipBus;
import com.enliple.pudding.bus.ZzimStatusBus;
import com.enliple.pudding.commons.app.ShopTreeKey;
import com.enliple.pudding.commons.data.DialogModel;
import com.enliple.pudding.commons.internal.AppPreferences;
import com.enliple.pudding.commons.log.Logger;
import com.enliple.pudding.commons.network.NetworkApi;
import com.enliple.pudding.commons.network.NetworkBus;
import com.enliple.pudding.commons.network.NetworkBusResponse;
import com.enliple.pudding.commons.network.vo.API139;
import com.enliple.pudding.commons.shoptree.network.ShopTreeAsyncTask;
import com.enliple.pudding.commons.widget.toast.AppToast;
import com.enliple.pudding.widget.PlayerNewProductDialog;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class DialogProductFragment extends Fragment implements DialogProductListAdapter.Listener {
    private static final int RECYCLER_VIEW_ITEM_CACHE_COUNT = 20;
    private VideoPipBus videoPipBus;
    private List<DialogModel> items;
    private boolean isCast = false; // 방송자인지 아닌지 구별하는 변수
    private RecyclerView recyclerView;
    private DialogProductListAdapter adapter;
    private DialogModel selectItem;
    private String selectUrl;
    private String linkTitle = "";
    private String productId = "";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.video_dialog_product, container, false);
        Bundle bundle = getArguments();
        items = bundle.getParcelableArrayList("list");
        videoPipBus = (VideoPipBus) bundle.getSerializable("bus");
        isCast = bundle.getBoolean("isCast");
        Logger.e("items size :: " + items.size());
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recyclerViewProducts);
        Logger.e("items size :: " + items.size());
        EventBus.getDefault().register(this);
        adapter = new DialogProductListAdapter();
        adapter.setListener(this);

        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(RECYCLER_VIEW_ITEM_CACHE_COUNT);
        recyclerView.setAdapter(adapter);
        Logger.e("items size :: " + items.size());
        adapter.setItems(items);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(NetworkBusResponse data) {
        String dt = "POST/products/" + productId + "/wish";
        if ( dt.equals(data.arg1) ) {
            if ( "ok".equals(data.arg2) ) {
                sendZzimStatusBus(productId);
                adapter.updateZzim(productId);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ZzimStatusBus bus) {
        String idx = bus.getProductId();
        String status = bus.getStatus();
        productId = idx;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(String bus) {
        Logger.e("bus :: " + bus);
        if ( "canDrawOverlays==true".equals(bus) ) {
            if ( "1".equals(selectItem.getType()) ) {
                startProductDetailActivity();
            } else {
                startLinkWebViewActivity();
            }


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(getActivity()))
                sendVideoPipBus();
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onProductItemClicked(int position, @NotNull DialogModel item) {
        selectItem = item;
        if(!isCast) {
            if ( "1".equals(item.getType()) ) {
                if (videoPipBus != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                    if( ! AppPreferences.Companion.getPipPermission(getActivity())) {
//                        AppPreferences.Companion.setPipPermission(getActivity(), true);
//                        EventBus.getDefault().post("startActivityForResult_OVERLAY");
//                    } else {
//                        startProductDetailActivity();
//                        if (Settings.canDrawOverlays(getActivity()))
//                            sendVideoPipBus();
//                    }
                    startProductDetailActivity();
                    if (Settings.canDrawOverlays(getActivity()))
                        sendVideoPipBus();
                } else {
                    startProductDetailActivity();
                    sendVideoPipBus();
                }
            } else {
                linkTitle = item.getName();

                ShopTreeAsyncTask task = new ShopTreeAsyncTask(getActivity());
                task.getDRCLink(item.getIdx(), item.getStreamKey(), "", item.getType(), new ShopTreeAsyncTask.OnDefaultObjectCallbackListener() {
                    @Override
                    public void onResponse(boolean result, Object obj) {
                        try {
                            API139 response = new Gson().fromJson(obj.toString(), API139.class);
                            if ( "success".equals(response.result) ) {
                                selectUrl = response.url;

                                if (videoPipBus != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                                    if( ! AppPreferences.Companion.getPipPermission(getActivity())) {
//                                        AppPreferences.Companion.setPipPermission(getActivity(), true);
//                                        EventBus.getDefault().post("startActivityForResult_OVERLAY");
//                                    } else {
//                                        startLinkWebViewActivity();
//                                        if (Settings.canDrawOverlays(getActivity()))
//                                            sendVideoPipBus();
//                                    }
                                    startLinkWebViewActivity();
                                    if (Settings.canDrawOverlays(getActivity()))
                                        sendVideoPipBus();
                                } else {
                                    startLinkWebViewActivity();
                                    sendVideoPipBus();
                                }
                            } else {
                                JSONObject errorJson = new JSONObject(obj.toString());
                                new AppToast(getActivity()).showToastMessage(errorJson.optString("message"), AppToast.DURATION_MILLISECONDS_DEFAULT, AppToast.GRAVITY_BOTTOM);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
    }

    @Override
    public void onProductCartClicked(int position, @NotNull DialogModel item) {
        String idx = item.getIdx();
        String pCode = item.getPcode();
        String scCode = item.getScCode();
        String streamKey = item.getStreamKey();
        String vodType = item.getVodType();
        String recommendId = item.getRecommendId();
        Intent intent = new Intent("DIALOG_CLICKED");
        intent.putExtra("idx", idx);
        intent.putExtra("pCode", pCode);
        intent.putExtra("scCode", scCode);
        intent.putExtra("streamKey", streamKey);
        intent.putExtra("vodType", vodType);
        intent.putExtra("recommendId", recommendId);
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
//        Animation slideUp = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_up);
//        optionLayer.setVisibility(View.VISIBLE);
//        optionLayer.startAnimation(slideUp);

    }

    @Override
    public void onProductZzimClicked(int position, @NotNull DialogModel item) {
        productId = item.getIdx();

        if ( AppPreferences.Companion.getLoginStatus(getActivity()) ) {
            try {
                String isWish = "Y";
                if ( "Y".equals(item.getIs_wish()))
                    isWish = "N";
                else
                    isWish = "Y";
                JSONObject object = new JSONObject();
                object.put("user", AppPreferences.Companion.getUserId(getActivity()));
                object.put("is_wish", isWish);
                object.put("type", item.getType());

                RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), object.toString());
                EventBus.getDefault().post(new NetworkBus(NetworkApi.API126.name(), item.getIdx(), body));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            getActivity().startActivity(new Intent(getActivity(), LoginActivity.class));
        }
    }

    private void startProductDetailActivity() {
        Intent intent = new Intent(getActivity(), ProductDetailActivity.class);
        intent.putExtra(ShopTreeKey.KEY_IDX, selectItem.getIdx());
        intent.putExtra(ShopTreeKey.KEY_PCODE, selectItem.getPcode());
        intent.putExtra(ShopTreeKey.KEY_SCCODE, selectItem.getScCode());
        intent.putExtra(ShopTreeKey.KEY_STREAM_KEY, selectItem.getStreamKey());
        intent.putExtra(ShopTreeKey.KEY_VOD_TYPE, selectItem.getVodType());
        intent.putExtra(ShopTreeKey.KEY_RECOMMEND_ID, selectItem.getRecommendId());
        getActivity().startActivity(intent);
    }

    private void startLinkWebViewActivity() {
        Intent intent = new Intent(getActivity(), LinkWebViewActivity.class);
        intent.putExtra("LINK", selectUrl);
        intent.putExtra("IDX", selectItem.getIdx());
        intent.putExtra("TYPE", selectItem.getType());
        intent.putExtra("IS_WISH", selectItem.getIs_wish());
        intent.putExtra("TITLE", linkTitle);
        intent.putExtra("ITEM_LINK", true);
        getActivity().startActivity(intent);
    }

    private void sendVideoPipBus() {
        if ( videoPipBus != null ) {
            Logger.e("sendVideoPipBus : " + videoPipBus.url);
            EventBus.getDefault().post(videoPipBus);
        }
    }

    private void sendZzimStatusBus(String productId) {
        String is_wish = "";
        String wish_cnt = "";
        try {
            for ( int i = 0 ; i < items.size() ; i ++ ) {
                DialogModel data = items.get(i);
                if ( data.getIdx().equals(productId) ) {
                    int count = Integer.valueOf(data.getWish_cnt());
                    if ( "Y".equals(data.getIs_wish()) ) {
                        if ( count > 0 ) {
                            --count;
                        }
                        is_wish = "N";
                        wish_cnt = count + "";
                    } else {
                        ++count;
                        is_wish = "Y";
                        wish_cnt = count + "";
                    }
                }
            }

            EventBus.getDefault().post(new ZzimStatusBus(productId, is_wish, wish_cnt));
//            dismissDialog();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void dismissDialog() {
        if ( this.getParentFragment() instanceof DialogFragment )
            ((DialogFragment) this.getParentFragment()).dismiss();
    }

//    public void closeSubPop() {
//        if ( optionLayer.getVisibility() == View.VISIBLE ) {
//            Logger.e("optionLayer visible");
//
//            recyclerView.setVisibility(View.VISIBLE);
//            optionLayer.setVisibility(View.GONE);
//            Animation slideDown = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_down);
//            optionLayer.startAnimation(slideDown);
//        } else {
//            Logger.e("optionLayer gone");
//            dismissDialog();
//
//        }
//    }
}
