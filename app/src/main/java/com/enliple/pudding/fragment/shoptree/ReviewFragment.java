package com.enliple.pudding.fragment.shoptree;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.enliple.pudding.R;
import com.enliple.pudding.activity.PhotoReviewActivity;
import com.enliple.pudding.activity.PhotoReviewMoreActivity;
import com.enliple.pudding.adapter.shoptree.ReviewAdapter;
import com.enliple.pudding.api.dao.ReviewModel;
import com.enliple.pudding.commons.log.Logger;
import com.enliple.pudding.commons.app.ImageLoad;
import com.enliple.pudding.commons.app.Utils;
import com.enliple.pudding.commons.db.DBManager;
import com.enliple.pudding.commons.internal.AppPreferences;
import com.enliple.pudding.commons.network.NetworkApi;
import com.enliple.pudding.commons.network.NetworkBus;
import com.enliple.pudding.commons.network.NetworkBusResponse;
import com.enliple.pudding.commons.network.vo.API45;
import com.enliple.pudding.commons.widget.recyclerview.WrappedLinearLayoutManager;
import com.enliple.pudding.widget.CustomBar;
import com.enliple.pudding.widget.SingleButtonDialog;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.RequestBody;

public class ReviewFragment extends Fragment {
    public static final int RECOM_STATUS_NONE = 0;
    public static final int RECOM_STATUS_SAME_BUTTON_CLICKED = 1;
    public static final int RECOM_STATUS_DIFF_BUTTON_CLICKED = 2;
    private CustomBar barOne, barTwo, barThree, barFour, barFive;
    private AppCompatTextView pOne, pTwo, pThree, pFour, pFive, ratingNum, btnRecommand, btnRecent, botCount, photo_cnt;
    private RelativeLayout imageLayer1, imageLayer2, imageLayer3, imageLayer4, imageLayer5, imageLayer6, imageLayer7, moreLayer, noList;
    private AppCompatImageView img1, img2, img3, img4, img5, img6, img7, moreBg;
    private LinearLayout photo_firstline, photo_secondline;
    private RatingBar rating;
    private RecyclerView list;
    private View empty;
    private List<API45.ReviewItem> imageArray;
    private ArrayList<ReviewModel> modelArray;
    private WrappedLinearLayoutManager layoutManager;
    private ReviewAdapter adapter;
    private int itemWidth = 0;
    private int width = 0;
    private String it_id, reviewListKey, sort, deleteKey, recom_is_id;
    private boolean deleteSameState = false;
    private int score_one, score_two, score_three, score_four, score_five, totalCount;
    private boolean isPaused = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_review, container, false);
        initViews(view);
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        Logger.e("onPause called");
        isPaused = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        Logger.e("onResume called");
        isPaused = false;
        Logger.e("ReviewFragment it_id :: " + it_id);
        if (!TextUtils.isEmpty(it_id)) {
            sort = "0";
            sendData(sort);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        EventBus.getDefault().register(this);
        Bundle bundle = getArguments();
        if (bundle != null) {
            it_id = bundle.getString("idx");
        }
//        Logger.e("ReviewFragment it_id :: " + it_id);
//        if ( !TextUtils.isEmpty(it_id) ) {
//            sort = "0";
//            sendData(sort);
//        }
//        if(bundle != null) {
//            mInfo = (SellerInfo) bundle.getSerializable("CELLER_INFO");
//        }
//
//        loadData();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(NetworkBusResponse data) {
        if (isPaused) {
            return;
        }
        Logger.e("reviewListKey :: " + reviewListKey);
        Logger.e("deleteKey :: " + deleteKey);
        Logger.e("data.arg1 :: " + data.arg1);
        Logger.e("data.arg2 :: " + data.arg2);
        Logger.e("data.arg3 :: " + data.arg3);
        Logger.e("data.arg4 :: " + data.arg4);
        if (data.arg1.equals(reviewListKey)) {
            score_one = 0;
            score_two = 0;
            score_three = 0;
            score_four = 0;
            score_five = 0;
            String str = DBManager.getInstance(getActivity()).get(data.arg1);
            API45 response = new Gson().fromJson(str, API45.class);
            Logger.e("response.toString :: " + response.toString());
            List<API45.ReviewItem> items = response.data;
            String cntStr = response.nTotalCount+"개";
            String photoCnt = response.nTotalPhotoCount+"개";

            if ( "0".equals(response.nTotalCount) )
                noList.setVisibility(View.VISIBLE);
            else
                noList.setVisibility(View.GONE);

            botCount.setText(cntStr);
            photo_cnt.setText(photoCnt);
            if (items != null && items.size() > 0) {
                Logger.e("items not null size :: " + items.size());
                imageArray = new ArrayList<API45.ReviewItem>();
                modelArray = new ArrayList<ReviewModel>();
                totalCount = items.size();
                for (int i = 0; i < items.size(); i++) {
                    ReviewModel model = new ReviewModel();
                    API45.ReviewItem item = items.get(i);
                    int iScore = 0;
                    try {
                        iScore = Integer.valueOf(item.is_score);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Logger.e("iScore :: " + iScore);
                    if (iScore == 1) {
                        score_one++;
                    } else if (iScore == 2) {
                        score_two++;
                    } else if (iScore == 3) {
                        score_three++;
                    } else if (iScore == 4) {
                        score_four++;
                    } else if (iScore == 5) {
                        score_five++;
                    }
                    List<String> is_photos = item.is_photo;

                    if (is_photos != null && is_photos.size() > 0) {
                        ArrayList<String> subImage = new ArrayList<String>();
                        for (int j = 0; j < is_photos.size(); j++) {
                            subImage.add(is_photos.get(j));
                        }
                        model.setImages(subImage);
                        imageArray.add(item);
                    }
                    model.setIconPath(item.mb_user_img);
                    model.setName(item.mb_nick);
                    model.setDate(item.is_time);
                    model.setOption(item.ct_option);
                    model.setMessage(item.is_content);
                    model.setRecommand(item.recommend);
                    model.setNotRecommand(item.not_recommend);

                    modelArray.add(model);
                }

                setImages(imageArray);
                adapter.setItems(items);

                float percentageOne = (score_one * 100) / totalCount;
                float percentageTwo = (score_two * 100) / totalCount;
                float percentageThree = (score_three * 100) / totalCount;
                float percentageFour = (score_four * 100) / totalCount;
                float percentageFive = (score_five * 100) / totalCount;

                Logger.e("score_one :: " + score_one);
                Logger.e("score_two :: " + score_two);
                Logger.e("score_three :: " + score_three);
                Logger.e("score_four :: " + score_four);
                Logger.e("score_five :: " + score_five);

                Logger.e("percentageOne :: " + percentageOne);
                Logger.e("percentageTwo :: " + percentageTwo);
                Logger.e("percentageThree :: " + percentageThree);
                Logger.e("percentageFour :: " + percentageFour);
                Logger.e("percentageFive :: " + percentageFive);

                Logger.e("totalCount :: " + totalCount);

                barOne.setPercentage(0xFFD9E1EB, 0xFF5774f4, (int) percentageOne);
                barTwo.setPercentage(0xFFD9E1EB, 0xFF5774f4, (int) percentageTwo);
                barThree.setPercentage(0xFFD9E1EB, 0xFF5774f4, (int) percentageThree);
                barFour.setPercentage(0xFFD9E1EB, 0xFF5774f4, (int) percentageFour);
                barFive.setPercentage(0xFFD9E1EB, 0xFF5774f4, (int) percentageFive);

                pOne.setText((int) percentageOne + "%");
                pTwo.setText((int) percentageTwo + "%");
                pThree.setText((int) percentageThree + "%");
                pFour.setText((int) percentageFour + "%");
                pFive.setText((int) percentageFive + "%");

                int totalScore = ((score_one * 1) + (score_two * 2) + (score_three * 3) + (score_four * 4) + (score_five * 5)) / totalCount;
                try {
                    rating.setRating((float) totalScore);
                    ratingNum.setText("" + totalScore);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Logger.e("items null");
                photo_firstline.setVisibility(View.GONE);
                photo_secondline.setVisibility(View.GONE);
            }
        } else if (data.arg1.equals(NetworkApi.API47.toString())) {
            if ("ok".equals(data.arg2)) {
                sendData(sort);
            } else if ("fail".equals(data.arg2) && "400".equals(data.arg3)) {
                if (deleteSameState) {
                    deleteKey = NetworkApi.API50.toString() + "?is_id=" + recom_is_id + "&user=" + AppPreferences.Companion.getUserId(getActivity());
                    NetworkBus bus = new NetworkBus(NetworkApi.API50.name(), recom_is_id, AppPreferences.Companion.getUserId(getActivity()));
                    EventBus.getDefault().post(bus);
                } else {
                    String str = data.arg4;
                    String message = "이미 평가하였습니다. 변경하시려면 앞선 평가를 다시 눌러서 취소하고 시도해주세요.";
                    if (!TextUtils.isEmpty(data.arg4)) {
                        try {
                            JSONObject object = new JSONObject(str);
                            message = object.optString("message");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    DisplayMetrics displayMetrics = new DisplayMetrics();
                    getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                    int screenWidth = displayMetrics.widthPixels;
                    int dialogWidth = screenWidth - Utils.ConvertDpToPx(getActivity(), 40);
                    new SingleButtonDialog(getActivity(), dialogWidth, message, "확인", new SingleButtonDialog.SingleButtonDialogListener() {
                        @Override
                        public void onConfirm() {

                        }
                    }).show();
                }
            }
        } else if (data.arg1.equals(deleteKey)) {
            if ("ok".equals(data.arg2)) {
                Logger.e("data ok 50");
                sendData(sort);
            }
        }
    }

    private void initViews(View v) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        width = displayMetrics.widthPixels;
        itemWidth = (width - Utils.ConvertDpToPx(getActivity(), 30)) / 3;

        noList = v.findViewById(R.id.noList);
        botCount = v.findViewById(R.id.botCount);
        photo_cnt = v.findViewById(R.id.photo_cnt);
        empty = v.findViewById(R.id.empty);
        photo_firstline = v.findViewById(R.id.photo_firstline);
        photo_secondline = v.findViewById(R.id.photo_secondline);

        barOne = v.findViewById(R.id.barOne);
        barTwo = v.findViewById(R.id.barTwo);
        barThree = v.findViewById(R.id.barThree);
        barFour = v.findViewById(R.id.barFour);
        barFive = v.findViewById(R.id.barFive);

        pOne = v.findViewById(R.id.pOne);
        pTwo = v.findViewById(R.id.pTwo);
        pThree = v.findViewById(R.id.pThree);
        pFour = v.findViewById(R.id.pFour);
        pFive = v.findViewById(R.id.pFive);

        rating = v.findViewById(R.id.rating);
        rating.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View view, MotionEvent event)
            {
                // 터치 이벤트 제거
                return true;
            };
        });
        ratingNum = v.findViewById(R.id.ratingNum);

        imageLayer1 = v.findViewById(R.id.imageLayer1);
        imageLayer2 = v.findViewById(R.id.imageLayer2);
        imageLayer3 = v.findViewById(R.id.imageLayer3);
        imageLayer4 = v.findViewById(R.id.imageLayer4);
        imageLayer5 = v.findViewById(R.id.imageLayer5);
        imageLayer6 = v.findViewById(R.id.imageLayer6);
        imageLayer7 = v.findViewById(R.id.imageLayer7);

        img1 = v.findViewById(R.id.img1);
        img2 = v.findViewById(R.id.img2);
        img3 = v.findViewById(R.id.img3);
        img4 = v.findViewById(R.id.img4);
        img5 = v.findViewById(R.id.img5);
        img6 = v.findViewById(R.id.img6);
        img7 = v.findViewById(R.id.img7);
        moreBg = v.findViewById(R.id.moreBg);

        moreLayer = v.findViewById(R.id.moreLayer);

        btnRecent = v.findViewById(R.id.btnRecent);
        btnRecommand = v.findViewById(R.id.btnRecommand);

        list = v.findViewById(R.id.list);

        btnRecent.setOnClickListener(clickListener);
        btnRecommand.setOnClickListener(clickListener);

        adapter = new ReviewAdapter(getActivity(), itemWidth, new ReviewAdapter.OnClickListener() {
            @Override
            public void onRecommendClicked(String is_id, int recomStatus, boolean isLogedinUser) {
                if (!isLogedinUser) {
                    showLoginDialog();
                } else {
                    Logger.e("onRecommendClicked is_id :: " + is_id + " , recomStatus :: " + recomStatus);
                    recom_is_id = is_id;
                    if (recomStatus == RECOM_STATUS_NONE) {
                        deleteSameState = false;
                    } else if (recomStatus == RECOM_STATUS_SAME_BUTTON_CLICKED) {
                        deleteSameState = true;
                    } else {
                        deleteSameState = false;
                    }

                    HashMap<String, String> map = new HashMap<>();
                    map.put("is_id", is_id);
                    map.put("type", "1");
                    map.put("user", AppPreferences.Companion.getUserId(getActivity()));

                    RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), (new JSONObject(map)).toString());

                    NetworkBus bus = new NetworkBus(NetworkApi.API47.name(), body);
                    EventBus.getDefault().post(bus);
                }
            }

            @Override
            public void onNotRecommendClicked(String is_id, int recomStatus, boolean isLogedinUser) {
                if (!isLogedinUser) {
                    showLoginDialog();
                } else {
                    Logger.e("onNotRecommendClicked is_id :: " + is_id + " , recomStatus :: " + recomStatus);
                    recom_is_id = is_id;
                    if (recomStatus == RECOM_STATUS_NONE) {
                        deleteSameState = false;
                    } else if (recomStatus == RECOM_STATUS_SAME_BUTTON_CLICKED) {
                        deleteSameState = true;
                    } else {
                        deleteSameState = false;
                    }

                    HashMap<String, String> map = new HashMap<>();
                    map.put("is_id", is_id);
                    map.put("type", "2");
                    map.put("user", AppPreferences.Companion.getUserId(getActivity()));

                    RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), (new JSONObject(map)).toString());

                    NetworkBus bus = new NetworkBus(NetworkApi.API47.name(), body);
                    EventBus.getDefault().post(bus);
                }
            }
        });
        layoutManager = new WrappedLinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        list.setHasFixedSize(false);
        list.setNestedScrollingEnabled(false);
        list.setLayoutManager(layoutManager);

        list.setAdapter(adapter);
    }

    private void showLoginDialog() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int dialogWidth = screenWidth - Utils.ConvertDpToPx(getActivity(), 40);
        new SingleButtonDialog(getActivity(), dialogWidth, "추천/비추천은 로그인 후 이용 가능합니다.", "확인", new SingleButtonDialog.SingleButtonDialogListener() {
            @Override
            public void onConfirm() {

            }
        }).show();
    }

    private void setImages(List<API45.ReviewItem> modelArray) {
        if ( modelArray.size() <= 0 ) {
            photo_firstline.setVisibility(View.GONE);
            photo_secondline.setVisibility(View.GONE);
        } else {
            photo_firstline.setVisibility(View.VISIBLE);
            photo_secondline.setVisibility(View.VISIBLE);
            int itemWidth = (width - Utils.ConvertDpToPx(getActivity(), 32)) / 4;
            ViewGroup.LayoutParams param = imageLayer1.getLayoutParams();
            param.width = itemWidth;
            param.height = itemWidth;

            RelativeLayout[] arr = {imageLayer1, imageLayer2, imageLayer3, imageLayer4, imageLayer5, imageLayer6, imageLayer7, moreLayer};
            AppCompatImageView[] i_arr = {img1, img2, img3, img4, img5, img6, img7, moreBg};
            ArrayList<RelativeLayout> layoutArray = new ArrayList<RelativeLayout>();
            ArrayList<AppCompatImageView> imageArray = new ArrayList<AppCompatImageView>();

            for (int i = 0; i < arr.length; i++) {
                if (i <= modelArray.size() - 1) {
                    API45.ReviewItem model = modelArray.get(i);
                    layoutArray.add(arr[i]);
                    arr[i].setLayoutParams(param);
                    imageArray.add(i_arr[i]);
                    arr[i].setVisibility(View.VISIBLE);
                    arr[i].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(getActivity(), PhotoReviewActivity.class);
                            intent.putExtra("PHOTO_REVIEW", getReviewStr(model));
                            startActivity(intent);
                        }
                    });
                } else {
                    arr[i].setVisibility(View.GONE);
                }
            }

            if (modelArray.size() >= 7) {
                moreLayer.setLayoutParams(param);
                moreLayer.setVisibility(View.VISIBLE);
                moreLayer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Logger.e("more CLICKED");
                        Intent intent = new Intent(getActivity(), PhotoReviewMoreActivity.class);
                        intent.putExtra("it_id", it_id);
                        startActivity(intent);
                    }
                });
            }

            loadImage(imageArray, modelArray);
        }
    }

    private void loadImage(ArrayList<AppCompatImageView> iv, List<API45.ReviewItem> models) {
        for (int i = 0; i < iv.size(); i++) {
            String path = models.get(i).is_photo.get(0);
//            Glide.with(getActivity())
//                    .load(path)
//                    .centerCrop()
//                    .priority(Priority.HIGH)
//                    .diskCacheStrategy(DiskCacheStrategy.ALL)
//                    .into(iv.get(i));
            ImageLoad.setImage(getActivity(), iv.get(i), path, null, ImageLoad.SCALE_CENTER_CROP, DiskCacheStrategy.ALL);
        }
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnRecent:
                    sort = "0";
                    sendData(sort);
                    btnRecent.setTextColor(0xFF9f56f2);
                    btnRecommand.setTextColor(0xFFBCC6D2);
                    break;
                case R.id.btnRecommand:
                    sort = "1";
                    sendData(sort);
                    btnRecent.setTextColor(0xFFBCC6D2);
                    btnRecommand.setTextColor(0xFF9f56f2);
                    break;
            }
        }
    };

    private void sendData(String order) {
        Logger.e("sendData :: order :: " + order);
        Logger.e("sendData :: it_id :: " + it_id);
        reviewListKey = NetworkApi.API45.toString() + "?it_id=" + it_id + "&order=" + order;
        NetworkBus bus = new NetworkBus(NetworkApi.API45.name(), it_id, order);
        EventBus.getDefault().post(bus);
    }

    private String getReviewStr(API45.ReviewItem model) {
        JSONObject object = new JSONObject();
        try {
            object.put("is_id", model.is_id);
            object.put("ct_id", model.ct_id);
            object.put("is_type", model.is_type);
            object.put("is_score", model.is_score);
            object.put("is_subject", model.is_subject);
            object.put("is_content", model.is_content);
            object.put("is_time", model.is_time);
            object.put("mb_id", model.mb_id);
            object.put("mb_nick", model.mb_nick);
            object.put("mb_user_img", model.mb_user_img);
            object.put("ct_option", model.ct_option);
            object.put("recommend", model.recommend);
            object.put("not_recommend", model.not_recommend);
            object.put("is_mine", model.is_mine);
            object.put("is_recommend", model.is_recommend);

            JSONArray array = new JSONArray();
            if (model.is_photo != null && model.is_photo.size() > 0) {
                for (int i = 0; i < model.is_photo.size(); i++) {
                    array.put(model.is_photo.get(i));
                }
                object.put("is_photo", array);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return object.toString();
    }

    public void setEmptyHeight(int height) {
        ViewGroup.LayoutParams param = empty.getLayoutParams();
        param.height = height;
        empty.setLayoutParams(param);
    }
}
