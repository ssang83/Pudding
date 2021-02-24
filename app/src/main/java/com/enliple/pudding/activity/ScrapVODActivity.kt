package com.enliple.pudding.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import androidx.recyclerview.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.enliple.pudding.AbsBaseActivity
import com.enliple.pudding.R
import com.enliple.pudding.adapter.my.ScrapVODAdapter
import com.enliple.pudding.adapter.my.ScrapVODAdapter.ScrapContentHolder
import com.enliple.pudding.common.VideoDataContainer
import com.enliple.pudding.commons.app.NetworkStatusUtils
import com.enliple.pudding.commons.app.Utils
import com.enliple.pudding.commons.data.ApiParams
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.internal.AppConstants
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.*
import com.enliple.pudding.commons.network.vo.API23
import com.enliple.pudding.commons.network.vo.BaseAPI
import com.enliple.pudding.commons.network.vo.VOD
import com.enliple.pudding.commons.widget.recyclerview.WrappedGridLayoutManager
import com.enliple.pudding.commons.widget.recyclerview.WrappedLinearLayoutManager
import com.enliple.pudding.commons.widget.toast.AppToast
import com.enliple.pudding.widget.AppAlertDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_scrap_vod.*
import kotlinx.android.synthetic.main.activity_scrap_vod.progressBar
import kotlinx.android.synthetic.main.activity_scrap_vod.topBtn
import kotlinx.android.synthetic.main.fragment_daily_ranking.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Created by Kim Joonsung on 2018-10-25.
 *
 */
class ScrapVODActivity : AbsBaseActivity(), ScrapVODAdapter.Listener {

    companion object {
        private const val RECYCLER_VIEW_ITEM_CACHE_SIZE = 20
        private const val REQUEST_PLAYER = 0
        private const val PAGE_DATA_COUNT = 100
    }

    private lateinit var mAdapter: ScrapVODAdapter
    private lateinit var mLayoutManager:WrappedLinearLayoutManager
    private var dataKey = ""
    private lateinit var dialog: AppAlertDialog

    private var extendedImageWidth = 0
    private var extendedImageHeight = 0
    private var imageWidth = 0
    private var imageHeight = 0
    private var pageCount = 1
    private var isReload = false
    private var isEndOfData = false
    private var mStreamKey = arrayListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scrap_vod)

        EventBus.getDefault().register(this)

        extendedImageWidth = AppPreferences.getScreenWidth(this) - Utils.ConvertDpToPx(this, 30)
        extendedImageHeight = (extendedImageWidth * 167) / 295
        imageWidth = AppPreferences.getScreenWidth(this) - Utils.ConvertDpToPx(this, 65)
        imageHeight = (imageWidth * 167) / 295

        topBtn.setOnClickListener { recyclerViewScrap.scrollToPosition(0) }
        buttonClose.setOnClickListener(clickListener)
        buttonCheckAll.setOnClickListener(clickListener)
        btnEdit.setOnClickListener(clickListener)
        btnCancel.setOnClickListener(clickListener)
        btnDelete.setOnClickListener(clickListener)

        recyclerViewScrap.setHasFixedSize(false)
        recyclerViewScrap.setItemViewCacheSize(RECYCLER_VIEW_ITEM_CACHE_SIZE)
        recyclerViewScrap.addOnScrollListener(scrollListener)

        mLayoutManager = WrappedLinearLayoutManager(this)
        recyclerViewScrap.layoutManager = mLayoutManager

        mAdapter = ScrapVODAdapter(this, extendedImageWidth, extendedImageHeight, imageWidth, imageHeight).apply {
            setListener(this@ScrapVODActivity)
        }

        recyclerViewScrap.adapter = mAdapter
    }

    override fun onResume() {
        super.onResume()

        NetworkBus(NetworkApi.API23.name, pageCount.toString()).let {
            EventBus.getDefault().post(it)
        } 
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onDozeModeStateChanged(dozeEnable: Boolean) {}
    override fun onNetworkStatusChanged(status: NetworkStatusUtils.NetworkStatus) {}

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_PLAYER) {
            EventBus.getDefault().post(NetworkBus(NetworkApi.API23.name))
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun setTotalCount(itemCount: Int) {
        textViewSelectCount.text = "${itemCount}"
    }

    override fun updateSelectCount(selected: Int, total: Int, position: Int) {
        textViewSelectCount.text = "${selected}"

        if (selected == total) {
            imageViewCheckAll.isSelected = true
        } else {
            imageViewCheckAll.isSelected = false
        }
    }

    override fun updateTotalCount(count: Int) {
        textViewTotalCount.text = "${count}개"
    }

    override fun setEmptyView(visible: Boolean) {
        setEmptyViewVisible(visible)
    }

    override fun onItemClicked(item: API23.MyScrapVODItem, position: Int) {
        Logger.e("URL : ${item.stream}")

        if(item.show_YN == "N") {
            AppToast(this@ScrapVODActivity).showToastMessage("제재된 영상은 재생 할 수 없습니다.",
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM)
            return
        }

        if(item.delete_YN == "Y") {
            AppToast(this@ScrapVODActivity).showToastMessage("삭제된 영상은 재생 할 수 없습니다.",
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM)
            return
        }

        // 페이징 및 dbKey 포지션 문제 때문에 앞으로 아래와 같이 List 객체를 만들어서 영상 데이터 사용
        val json = Gson().toJson(mAdapter.items)
        val videoItems : MutableList<VOD.DataBeanX> = Gson().fromJson(json, object : TypeToken<MutableList<VOD.DataBeanX>>(){}.type)
        VideoDataContainer.getInstance().mVideoData = videoItems

        if (!TextUtils.isEmpty(item.stream)) {
            startActivityForResult(Intent(this@ScrapVODActivity, ShoppingPlayerActivity::class.java).apply {
                putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_PLAYER_FLAG, AppConstants.SCRAP_VOD_PLAYER)
                putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_MY_VOD_POSITION, position)
                putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_CASTER_ID, item.userId)
                putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_VIDEO_TYPE, item.videoType)

                data = Uri.parse("vcommerce://shopping?url=${item.stream}")
            }, REQUEST_PLAYER)
        } else {
            AppToast(this@ScrapVODActivity).showToastMessage("방송 정보 메타 데이터가 존재하지 않습니다.",
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: NetworkBusFastResponse) {
        val api24 = NetworkHandler.getInstance(this@ScrapVODActivity).getKey(NetworkApi.API24.toString(), AppPreferences.getUserId(this@ScrapVODActivity)!!, "")
        if (data.arg1.startsWith(api24)) {
            handleNetworkResultVODDel(data)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: NetworkBusResponse) {
        dataKey = NetworkHandler.getInstance(this@ScrapVODActivity).getKey(NetworkApi.API23.toString(), AppPreferences.getUserId(this@ScrapVODActivity)!!, "")

        if(data.arg1.startsWith(dataKey)) {
            handleNetworkResult(data)
        } else if(data.arg1 == NetworkApi.API8.toString()) {
            handleNetworkLikeResult(data)
        }
    }

    private fun handleNetworkLikeResult(data: NetworkBusResponse) {
        if ("fail" == data.arg2) {
            val error: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
            Logger.e("error : $error")

            AppToast(this).showToastMessage("좋아요 등록이 실패하였습니다.",
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM)
        }
    }

    private fun handleNetworkResultVODDel(data: NetworkBusFastResponse) {
        if ("ok" == data.arg2) {
            Logger.e("handleNetworkResultVODDel")

            if (imageViewCheckAll.isSelected) {
                mAdapter.itemDeleteAll()
                setEmptyViewVisible(true)
                imageViewCheckAll.isSelected = false
            } else {
                mAdapter.selectItemDel()
            }

            dialog.dismiss()

            EventBus.getDefault().post(NetworkBus(NetworkApi.API23.name))

            for(i in 0 until mStreamKey.size) {
                EventBus.getDefault().post(NetworkBus(NetworkApi.API143.name, mStreamKey[i])) // 현재 Video 정보 갱신
            }
        } else {
            var errorResult: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
            Logger.e("error : $errorResult")
        }
    }

    private fun handleNetworkResult(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            var str = DBManager.getInstance(this@ScrapVODActivity).get(data.arg1)
            var response: API23 = Gson().fromJson(str, API23::class.java)

            isEndOfData = response.data.size < PAGE_DATA_COUNT
            isEndOfData = response.data.size == 0
            if(response.result == "failure") {
                textViewTotalCount.text = "0개"
                setEmptyViewVisible(true)
                return
            }

            textViewTotalCount.text = "${response.nTotalCount}개"

            if (response.nTotalCount > 0) {
                setEmptyViewVisible(false)
                if(!isReload) {
                    mAdapter.setItem(response.data)
                } else {
                    mAdapter.addItems(response.data, buttonCheckAll!!.isSelected)
                }
            } else {
                setEmptyViewVisible(true)
            }
        } else {
            var errorResult: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
            Logger.e("error : $errorResult")
        }

        progressBar?.visibility = View.GONE
    }

    private fun setEmptyViewVisible(visible: Boolean) {
        if (visible) {
            layoutEmpty.visibility = View.VISIBLE
            layoutEdit.visibility = View.GONE
            recyclerViewScrap.visibility = View.GONE
        } else {
            layoutEmpty.visibility = View.GONE
            layoutEdit.visibility = View.VISIBLE
            recyclerViewScrap.visibility = View.VISIBLE
        }
    }

    private fun getStreamKey(key:String) {
        val streamKey = key.split(",")
        for(i in 0 until streamKey.size) {
            mStreamKey.add(streamKey[i])
        }
    }

    private val scrollListener = object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val lastVisibleItemPosition = mLayoutManager.findLastCompletelyVisibleItemPosition()
            val totalItemCount = mAdapter.itemCount - 1
            val visibleCount = mLayoutManager.findFirstVisibleItemPosition()

            if ( topBtn.visibility == View.GONE ) {
                if ( visibleCount > 8 )
                    topBtn.visibility = View.VISIBLE
            }

            if ((lastVisibleItemPosition == totalItemCount) && !isEndOfData) {
                isReload = true
                ++pageCount

                AppToast(this@ScrapVODActivity).showToastMessage("loading 중...",
                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                        AppToast.GRAVITY_BOTTOM)

                NetworkBus(NetworkApi.API23.name, pageCount.toString()).let {
                    EventBus.getDefault().post(it)
                }
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if ( newState == RecyclerView.SCROLL_STATE_IDLE ) {
                Handler().postDelayed({
                    if ( newState == RecyclerView.SCROLL_STATE_IDLE )
                        topBtn.visibility = View.GONE
                }, 1500)
            }
        }
    }

    private val clickListener = View.OnClickListener {
        when (it?.id) {
            R.id.buttonClose -> onBackPressed()

            R.id.btnEdit -> {
                if ( mAdapter != null ) {
                    empty.visibility = View.GONE
                    twoBtnLayer.visibility = View.VISIBLE
                    btnEdit.visibility = View.GONE
                    buttonCheckAll.visibility = View.VISIBLE
                    textViewSelectCount.visibility = View.VISIBLE
                    slash.visibility = View.VISIBLE

                    textViewAll.text = "전체선택"
                    mAdapter.setEditMode(true)
                }
            }

            R.id.btnCancel -> {
                if ( mAdapter != null ) {
                    empty.visibility = View.VISIBLE
                    twoBtnLayer.visibility = View.GONE
                    btnEdit.visibility = View.VISIBLE
                    buttonCheckAll.visibility = View.GONE
                    textViewSelectCount.visibility = View.GONE
                    slash.visibility = View.GONE

                    textViewSelectCount.text = "0"
                    textViewAll.text = "전체"
                    imageViewCheckAll.isSelected = false
                    mAdapter.itemUnSelectAll()
                    mAdapter.setEditMode(false)
                }
            }

            R.id.buttonCheckAll -> {
                it.isSelected = !it.isSelected
                if (it.isSelected) {
                    mAdapter.itemSelectAll()
                    textViewSelectCount.text = "${mAdapter.getItemCount()}"
                } else {
                    mAdapter.itemUnSelectAll()
                    textViewSelectCount.text = "0"
                }
            }

            R.id.btnDelete -> {
                if(mAdapter.getSelectedCount() > 0 ) {
                    dialog = AppAlertDialog(this)
                    dialog.setTitle("스크랩")
                    dialog.setMessage("선택한 스크랩 영상을 삭제할까요?")
                    dialog.setLeftButton(getString(R.string.msg_my_follow_cancel), View.OnClickListener {
                        dialog.dismiss()
                    })
                    dialog.setRightButton(getString(R.string.msg_my_qna_del), View.OnClickListener {
                        getStreamKey(mAdapter.getSelectItemKey())
                        Logger.e("productId : ${mAdapter.getSelectItemKey()}")
                        NetworkBus(NetworkApi.API24.name, mAdapter.getSelectItemKey(), AppPreferences.getUserId(this)!!).let {
                            EventBus.getDefault().post(it)
                        }
                    })

                    dialog.show()
                }
            }
        }
    }
}