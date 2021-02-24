package com.enliple.pudding.fragment.main.pudding

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.enliple.pudding.R
import com.enliple.pudding.activity.ShoppingPlayerActivity
import com.enliple.pudding.adapter.home.BestAdapter
import com.enliple.pudding.common.VideoDataContainer
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.internal.AppConstants
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.NetworkApi
import com.enliple.pudding.commons.network.NetworkBus
import com.enliple.pudding.commons.network.NetworkBusResponse
import com.enliple.pudding.commons.network.vo.API114
import com.enliple.pudding.commons.network.vo.VOD
import com.enliple.pudding.commons.widget.toast.AppToast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_pudding_best.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Created by Kim Joonsung on 2019-04-05.
 */
class PuddingBestFragment : androidx.fragment.app.Fragment(), BestAdapter.Listener {

    companion object {
        private const val PAGE_DATA_COUNT = 100
        private const val NUMBER_COLUMNS = 2
    }

    private lateinit var mAdapter: BestAdapter
    private lateinit var mLayoutManager: GridLayoutManager

    private var pageCount = 1
    private var isReload = false
    private var isEndOfData = false
    private var mIsVisibleToUser = false
    private var mIsFirst = false
    private var dbKey = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_pudding_best, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        EventBus.getDefault().register(this)

        topBtn.setOnClickListener { recyclerViewBest.scrollToPosition(0) }

        recyclerViewBest.setHasFixedSize(false)
        recyclerViewBest.addOnScrollListener(scrollListener)

        mLayoutManager = GridLayoutManager(context!!, NUMBER_COLUMNS).apply {
            orientation = LinearLayoutManager.VERTICAL
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if(position == 0) {
                        NUMBER_COLUMNS
                    } else {
                        1
                    }
                }
            }
        }

        recyclerViewBest.layoutManager = mLayoutManager

        mAdapter = BestAdapter(context!!).apply {
            setListener(this@PuddingBestFragment)
        }

        recyclerViewBest.adapter = mAdapter
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        Logger.i("setUserVisibleHint: $isVisibleToUser")

        mIsVisibleToUser = isVisibleToUser

        if(isVisibleToUser) {
            if(!mIsFirst) {
                mIsFirst = true // 최초 한번만 가져온다.
                refresh()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        refresh()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onItemClick(item: API114.VideoItem, position: Int) {
        Logger.e("URL : ${item.stream}")

        // 페이징 및 dbKey 포지션 문제 때문에 앞으로 아래와 같이 List 객체를 만들어서 영상 데이터 사용
        val json = Gson().toJson(mAdapter.mItems)
        val videoItems : MutableList<VOD.DataBeanX> = Gson().fromJson(json, object : TypeToken<MutableList<VOD.DataBeanX>>(){}.type)
        VideoDataContainer.getInstance().mVideoData = videoItems

        if (!TextUtils.isEmpty(item.stream)) {
            startActivity(Intent(context!!, ShoppingPlayerActivity::class.java).apply {
                putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_POSITION, position)
                putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_CASTER_ID, item.userId)
                putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_VIDEO_TYPE, item.videoType)

                data = Uri.parse("vcommerce://shopping?url=${item.stream}")
            })
        } else {
            AppToast(context!!).showToastMessage("방송 정보 메타 데이터가 존재하지 않습니다.",
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM)
        }
    }

    override fun onPagerClick(item: API114.VideoItem, position: Int) {
        var url = item?.stream ?: ""
        Logger.d("URL : $url")

        // 페이징 및 dbKey 포지션 문제 때문에 앞으로 아래와 같이 List 객체를 만들어서 영상 데이터 사용
        val json = Gson().toJson(mAdapter.getPagerItems())
        val videoItems : MutableList<VOD.DataBeanX> = Gson().fromJson(json, object : TypeToken<MutableList<VOD.DataBeanX>>(){}.type)
        VideoDataContainer.getInstance().mVideoData = videoItems

        if (!TextUtils.isEmpty(url)) {
            startActivity(Intent(context!!, ShoppingPlayerActivity::class.java).apply {
                putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_POSITION, position)
                putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_CASTER_ID, item.userId)

                data = Uri.parse("vcommerce://shopping?url=${item.stream}")
            })
        } else {
            AppToast(view!!.context).showToastMessage("방송 정보 메타 데이터가 존재하지 않습니다.",
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data:NetworkBusResponse) {
        if(data.arg1.startsWith("GET/mui/main/best?page=")) {
            handleNetworkResult(data)
        }
    }

    @Subscribe
    fun onMessageEvent(data: String) {
        if (data.startsWith("refresh")) {
            refresh()
        }
    }

    private fun refresh() {
        if(mIsVisibleToUser && (parentFragment as PuddingHomeTabFragment)?.userVisibleHint) {
            Logger.d("refresh")
            isReload = false
            pageCount = 1
            progressBar.visibility = View.VISIBLE

            EventBus.getDefault().post(NetworkBus(NetworkApi.API114.name, "best", "1", "", ""))
        }
    }

    private fun handleNetworkResult(data: NetworkBusResponse) {
        progressBar.visibility = View.GONE
        if("ok" == data.arg2) {
            val response: API114 = Gson().fromJson(DBManager.getInstance(context!!).get(data.arg1), API114::class.java)

            isEndOfData = response.data.size < PAGE_DATA_COUNT
            pageCount = response.pageCount.toInt()
            dbKey = data.arg1

            if(!isReload) {
                mAdapter.setItems(response.data)
            } else {
                mAdapter.addItems(response.data)

                // 하단으로 scroll 필요
                recyclerViewBest?.scrollBy(0, 200)

                AppToast.cancelAllToast()
            }
        } else {
            Logger.e("${data.arg3} ${data.arg4}")
        }
    }

    private val scrollListener = object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if(mIsVisibleToUser) {
                val lastVisibleItemPosition = mLayoutManager.findLastCompletelyVisibleItemPosition()
                val visibleCount = mLayoutManager.findFirstVisibleItemPosition()
                val totalItemCount = mAdapter.itemCount - 1

//                if(visibleCount > 8) {
//                    topBtn.visibility = View.VISIBLE
//                } else {
//                    topBtn.visibility = View.GONE
//                }

                if ( topBtn.visibility == View.GONE ) {
                    if ( visibleCount > 8 )
                        topBtn.visibility = View.VISIBLE
                }

                if((lastVisibleItemPosition == totalItemCount) && !isEndOfData) {
                    isReload = true
                    ++pageCount

                    AppToast(context!!).showToastMessage("loading 중...",
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_BOTTOM)

                    EventBus.getDefault().post(NetworkBus(NetworkApi.API114.name, "best", pageCount.toString(), "", ""))
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
}