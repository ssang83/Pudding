package com.enliple.pudding.fragment.main.pudding

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.enliple.pudding.AbsBaseFragment
import com.enliple.pudding.R
import com.enliple.pudding.activity.CasterProfileActivity
import com.enliple.pudding.adapter.ranking.RankingAdapter
import com.enliple.pudding.commons.app.CategoryModel
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.NetworkApi
import com.enliple.pudding.commons.network.NetworkBus
import com.enliple.pudding.commons.network.NetworkBusResponse
import com.enliple.pudding.commons.network.vo.API81
import com.enliple.pudding.commons.network.vo.API88
import com.enliple.pudding.commons.network.vo.BaseAPI
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_daily_ranking.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 */
class RankingFragment : AbsBaseFragment(), RankingAdapter.AdapterListener {
    private lateinit var mAdapter: RankingAdapter
    private var mIsVisibleToUser = false
    private var mFragmentIndex = 0
    private var mIsFirst = false

    private lateinit var mCategoryList: List<API81.CategoryItem>
    private lateinit var mLayoutManager: LinearLayoutManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_daily_ranking, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Logger.e("onViewCreated index:$mFragmentIndex")

        EventBus.getDefault().register(this)

        mCategoryList = CategoryModel.getCategoryList(context!!, "all")

        topBtn.setOnClickListener { recyclerViewRanking.scrollToPosition(0) }
        
        recyclerViewRanking.setHasFixedSize(false)
        recyclerViewRanking.addOnScrollListener(scrollListener)

        mLayoutManager = LinearLayoutManager(context!!).apply {
            orientation = RecyclerView.VERTICAL
        }
        recyclerViewRanking.layoutManager = mLayoutManager

        mAdapter = RankingAdapter(mCategoryList)
        mAdapter.setListener(this@RankingFragment)
        recyclerViewRanking.adapter = mAdapter
    }

    override fun onResume() {
        super.onResume()

        refresh()
    }

    override fun onDestroy() {
        super.onDestroy()

        EventBus.getDefault().unregister(this)
    }

    override fun onItemClicked(item: API88.DataBean) {
        startActivity(Intent(context!!, CasterProfileActivity::class.java).apply {
            putExtra(CasterProfileActivity.INTENT_KEY_EXTRA_USER_ID, item?.mb_id)
        })
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        Logger.i("setUserVisibleHint: $isVisibleToUser  position:$mFragmentIndex")

        mIsVisibleToUser = isVisibleToUser

        if (context != null && mIsVisibleToUser) {
            if (!mIsFirst) {
                mIsFirst = true  // 최초 한번만 가져온다.
                refresh()
            }
        }
    }

    private fun refresh() {
        if (mIsVisibleToUser && (parentFragment as PuddingHomeTabFragment)?.userVisibleHint) {
            Logger.e("refresh")
            EventBus.getDefault().post(NetworkBus(NetworkApi.API88.name, ""))
        }
    }

    @Subscribe
    fun onMessageEvent(data: String) {
        if (data.startsWith("refresh")) {
            refresh()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: NetworkBusResponse) {
        when (mFragmentIndex) {
            0 -> if (data.arg1 == "GET/rank?type=") {
                loadData(data)
            }

            1 -> if (data.arg1 == "GET/rank?type=week") {
                loadData(data)
            }

            2 -> if (data.arg1 == "GET/rank?type=month") {
                loadData(data)
            }
        }
    }

//    @Subscribe
//    fun onMessageEvent(data: String) {
//        when (mFragmentIndex) {
//            0 -> if (data == "ranking_refresh_day") {
//                Logger.d("onMessageEvent: $data")
//                EventBus.getDefault().post(NetworkBus(NetworkApi.API88.name, ""))
//            }
//
//            1 -> if (data == "ranking_refresh_week") {
//                Logger.d("onMessageEvent: $data")
//                EventBus.getDefault().post(NetworkBus(NetworkApi.API88.name, "week"))
//            }
//
//            2 -> if (data == "ranking_refresh_month") {
//                Logger.d("onMessageEvent: $data")
//                EventBus.getDefault().post(NetworkBus(NetworkApi.API88.name, "month"))
//            }
//        }
//    }

    private fun loadData(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            Logger.e("loadData")

            val str = DBManager.getInstance(context!!).get(data.arg1)
            val response = Gson().fromJson(str, API88::class.java)
            Handler().postDelayed({ mAdapter.setItems(response.data) }, 100)

            mAdapter.setServerSyncTime(response.rankdDate) // sync time

            recyclerViewRanking?.visibility = View.VISIBLE
        } else {
            val errorResult = Gson().fromJson(data.arg4, BaseAPI::class.java)
            Logger.e("error : $errorResult")
        }

        progressBar?.visibility = View.GONE
    }

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (mIsVisibleToUser) {
                val visibleCount = mLayoutManager.findFirstVisibleItemPosition()

                if ( topBtn.visibility == View.GONE ) {
                    if ( visibleCount > 8 )
                        topBtn.visibility = View.VISIBLE
                }
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            var state = newState
            if ( state == RecyclerView.SCROLL_STATE_IDLE ) {
                Handler().postDelayed({
                    if ( state == RecyclerView.SCROLL_STATE_IDLE )
                        topBtn.visibility = View.GONE
                }, 1500)
            }
        }
    }

//    private fun drawHeader(index: Int, data: API88) {
//        Logger.d("drawHeader:$index")
//
//        var layout = view?.findViewById<View>(resources.getIdentifier("layoutRanking$index", "id", context?.packageName))
//        layout?.tag = data.mb_id
//
//        var smallImageView = layout?.findViewById<AppCompatImageView>(resources.getIdentifier("smallRankerImgView", "id", context?.packageName))
//        var rankerImageView = layout?.findViewById<AppCompatImageView>(resources.getIdentifier("rankerImgView", "id", context?.packageName))
//        var ranking = layout?.findViewById<AppCompatTextView>(resources.getIdentifier("rankingTxt", "id", context?.packageName))
//        var category = layout?.findViewById<AppCompatTextView>(resources.getIdentifier("category", "id", context?.packageName))
//        var name = layout?.findViewById<AppCompatTextView>(resources.getIdentifier("rankerName", "id", context?.packageName))
//        var follower = layout?.findViewById<AppCompatTextView>(resources.getIdentifier("follower", "id", context?.packageName))
//
//        smallImageView?.visibility = View.GONE
//        rankerImageView?.visibility = View.GONE
//
//        when (index) {
//            1 -> {
//                ranking?.setBackgroundResource(R.drawable.bg_ranking_1)
//                rankerImageView?.visibility = View.VISIBLE
//                ImageLoad.setImage(context!!, rankerImageView, data.userImage, null, ImageLoad.SCALE_CIRCLE_CROP, DiskCacheStrategy.ALL)
//            }
//
//            2 -> {
//                ranking?.setBackgroundResource(R.drawable.bg_ranking_2)
//                smallImageView?.visibility = View.VISIBLE
//                ImageLoad.setImage(context!!, smallImageView, data.userImage, null, ImageLoad.SCALE_CIRCLE_CROP, DiskCacheStrategy.ALL)
//            }
//
//            3 -> {
//                ranking?.setBackgroundResource(R.drawable.bg_ranking_3)
//                smallImageView?.visibility = View.VISIBLE
//                ImageLoad.setImage(context!!, smallImageView, data.userImage, null, ImageLoad.SCALE_CIRCLE_CROP, DiskCacheStrategy.ALL)
//            }
//        }
//
//        ranking?.text = data.rank
//        name?.text = data.nickName
//        follower?.text = StringUtils.getSnsStyleCount(Integer.valueOf(data.followCnt))
//
//        if (data.ca_id.isNotEmpty()) {
//            category?.visibility = View.VISIBLE
//            setCategoryLabel(data.ca_id, category!!)
//        } else {
//            category?.visibility = View.INVISIBLE
//        }
//    }

//    private fun setCategoryLabel(categoryId: String, view: AppCompatTextView) {
//        if (categoryId.isEmpty() || mCategoryList.isEmpty()) {
//            return
//        }
//
//        for (i in 0 until mCategoryList.size) {
//            if (mCategoryList[i].categoryId == categoryId) {
//                view.text = mCategoryList[i].categoryName
//                view.setTextColor(Color.parseColor(mCategoryList[i].categoryHex))
//
//                (view.background as GradientDrawable).apply {
//                    setStroke(Utils.ConvertDpToPx(context!!, 1), Color.parseColor(mCategoryList[i].categoryHex))
//                }
//            }
//        }
//    }

//    private val clickListener = View.OnClickListener {
//        when (it?.id) {
//            R.id.layoutRanking1,
//            R.id.layoutRanking2,
//            R.id.layoutRanking3 -> {
//                startActivity(Intent(context!!, CasterProfileActivity::class.java).apply {
//                    putExtra(CasterProfileActivity.INTENT_KEY_EXTRA_USER_ID, it?.tag.toString())
//                })
//            }
//        }
//    }
}