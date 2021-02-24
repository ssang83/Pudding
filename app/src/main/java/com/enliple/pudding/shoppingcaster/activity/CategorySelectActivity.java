package com.enliple.pudding.shoppingcaster.activity;

import androidx.appcompat.app.AppCompatActivity;

public class CategorySelectActivity extends AppCompatActivity {
//    private RecyclerView categoryRecyclerView;
//    private LinearLayoutManager layoutManager;
//    private CategorySelectAdapter adapter;
//    private String selectedMain = "";
//    private String selectedSub = "";
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.layout_category_select);
//        checkIntent();
//        categoryRecyclerView = findViewById(R.id.categoryRecyclerView);
//
//        NetworkBus bus = new NetworkBus(NetworkApi.API81.name(), "select");
//        EventBus.getDefault().post(bus);
//    }
//
//    public void onBackPressed() {
//        finish();
//    }
//
//    private void checkIntent() {
//        Intent intent = getIntent();
//        selectedMain = intent.getStringExtra("MAIN_CATEGORY");
//        selectedSub = intent.getStringExtra("SUB_CATEGORY");
//    }
//
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onMessageEvent(NetworkBusResponse data) {
////        String api81 = NetworkHandler.Companion.getInstance(CategorySelectActivity.this).getKey(NetworkApi.API81.toString(), "select", "");
////        if ( data.arg1.equals(api81) ) {
////            layoutManager = new LinearLayoutManager(CategorySelectActivity.this);
////            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
////            adapter = new CategorySelectAdapter(CategorySelectActivity.this, new CategorySelectAdapter.Listener() {
////                @Override
////                public void setFirstCategory(MainCategory firstCategory) {
////
////                }
////
////                @Override
////                public void setSecondCategory(ArrayList<SubCategory> subCategory) {
////
////                }
////            });
////            categoryRecyclerView.setHasFixedSize(false);
////            categoryRecyclerView.setNestedScrollingEnabled(false);
////            categoryRecyclerView.setLayoutManager(layoutManager);
////            categoryRecyclerView.setAdapter(adapter);
////
////            String str = DBManager.getInstance(CategorySelectActivity.this).get(data.arg1);
////            API81 response = new Gson().fromJson(str, API81.class);
////            if ( response != null ) {
////                List<MainCategory> mainArray = new ArrayList<>();
////                List<SubCategory> subArray = new ArrayList<>();
////                MainCategory main = new MainCategory();
////                SubCategory sub = new SubCategory();
////                for ( int i = 0 ; i < response.data.size() ; i ++ ) {
////                    API81.CategoryItem item = response.data.get(i);
////                    String categoryId = item.categoryId;
////                    String categoryName = item.categoryName;
////                    boolean isSelected = false;
////                    if ( selectedMain.equals(categoryName))
////                        isSelected = true;
////                    else
////                        isSelected = false;
////                    subArray = new ArrayList<>();
////                    main = new MainCategory();
////                    for ( int j = 0 ; j < item.sub.size() ; j ++ ) {
////                        sub = new SubCategory();
////                        boolean subSelected = false;
////                        if ( item.sub.get(i).categoryId.equals(selectedSub) )
////                            subSelected = true;
////                        else
////                            subSelected = false;
////                        sub.setCategoryName(item.sub.get(i).categoryName);
////                        sub.setCategoryId(item.sub.get(i).categoryId);
////                        sub.setSelected(subSelected);
////                        subArray.add(sub);
////                    }
////                    main.setCategoryId(categoryId);
////                    main.setCategoryName(categoryName);
////                    main.setSubArray(subArray);
////                    main.setSelected(isSelected);
////
////                    mainArray.add(main);
////                }
////                adapter.setItems(mainArray);
////            }
////        }
//    }
}
