package com.enliple.pudding.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.enliple.pudding.AbsBaseActivity
import com.enliple.pudding.R
import com.enliple.pudding.adapter.my.LatestViewVODAdapter
import com.enliple.pudding.common.VideoDataContainer
import com.enliple.pudding.commons.app.NetworkStatusUtils
import com.enliple.pudding.commons.app.Utils
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.internal.AppConstants
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.NetworkApi
import com.enliple.pudding.commons.network.NetworkBus
import com.enliple.pudding.commons.network.NetworkBusResponse
import com.enliple.pudding.commons.network.NetworkHandler
import com.enliple.pudding.commons.network.vo.API37
import com.enliple.pudding.commons.network.vo.BaseAPI
import com.enliple.pudding.commons.network.vo.VOD
import com.enliple.pudding.commons.widget.recyclerview.WrappedGridLayoutManager
import com.enliple.pudding.commons.widget.recyclerview.WrappedLinearLayoutManager
import com.enliple.pudding.commons.widget.toast.AppToast
import com.enliple.pudding.widget.AppAlertDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_latest_view_vod.*
import kotlinx.android.synthetic.main.activity_latest_view_vod.progressBar
import kotlinx.android.synthetic.main.activity_latest_view_vod.topBtn
import kotlinx.android.synthetic.main.fragment_daily_ranking.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Created by Kim Joonsung on 2018-10-26.
 */
class LatestViewVODActivity : AbsBaseActivity(), LatestViewVODAdapter.Listener {

    companion object {
        private const val RECYCLER_VIEW_ITEM_CACHE_SIZE = 20
    }

    private lateinit var mAdapter: LatestViewVODAdapter
    private lateinit var dialog: AppAlertDialog
    private lateinit var mLayoutManager: WrappedLinearLayoutManager

    private var dataKey = ""
    private var streamKey = ""
    private var pageCount = 1
    private var isReload = false
    private var isEndOfData = false

    private var extendedImageWidth = 0
    private var extendedImageHeight = 0
    private var imageWidth = 0
    private var imageHeight = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_view_vod)
        EventBus.getDefault().register(this)

        extendedImageWidth = AppPreferences.getScreenWidth(this) - Utils.ConvertDpToPx(this, 30)
        extendedImageHeight = (extendedImageWidth * 167) / 295
        imageWidth = AppPreferences.getScreenWidth(this) - Utils.ConvertDpToPx(this, 65)
        imageHeight = (imageWidth * 167) / 295

        topBtn.setOnClickListener { recyclerViewLatesView.scrollToPosition(0) }
        buttonClose.setOnClickListener(clickListener)
        buttonCheckAll.setOnClickListener(clickListener)
        btnEdit.setOnClickListener(clickListener)
        btnCancel.setOnClickListener(clickListener)
        btnDelete.setOnClickListener(clickListener)

        recyclerViewLatesView.setHasFixedSize(false)
        recyclerViewLatesView.setItemViewCacheSize(RECYCLER_VIEW_ITEM_CACHE_SIZE)
        recyclerViewLatesView.addOnScrollListener(scrollListener)

        mLayoutManager = WrappedLinearLayoutManager(this)
        recyclerViewLatesView.layoutManager = mLayoutManager

        mAdapter = LatestViewVODAdapter(this, extendedImageWidth, extendedImageHeight, imageWidth, imageHeight).apply {
            setListener(this@LatestViewVODActivity)
        }

        recyclerViewLatesView.adapter = mAdapter
    }

    override fun onResume() {
        super.onResume()

        NetworkBus(NetworkApi.API37.name, pageCount.toString()).let {
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: NetworkBusResponse) {
        val API37 = NetworkHandler.getInstance(this).getKey(NetworkApi.API37.toString(), AppPreferences.getUserId(this)!!, pageCount.toString())
        val API100 = NetworkHandler.getInstance(this).getKey(NetworkApi.API100.toString(), AppPreferences.getUserId(this)!!, "")

        if (data.arg1 == API37) {
            handleNetworkResult(data)
        } else if (data.arg1.startsWith(API100)) {
            if ("ok" == data.arg2) {
                if (imageViewCheckAll.isSelected) {
                    mAdapter.itemDeleteAll()
                    setEmptyViewVisible(true)
                    imageViewCheckAll.isSelected = false
                } else {
                    mAdapter.selectItemDel()
                }

                dialog.dismiss()
            } else {
                var errorResult: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
                Logger.e("error : $errorResult")
            }
        }

        progressBar?.visibility = View.GONE
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

    override fun onItemClicked(item: API37.VodItem, position: Int) {
        Logger.e("URL : ${item.stream}")

        if (item.show_YN == "Y") {
            // 페이징 및 dbKey 포지션 문제 때문에 앞으로 아래와 같이 List 객체를 만들어서 영상 데이터 사용
            val json = Gson().toJson(mAdapter.items)
            val videoItems : MutableList<VOD.DataBeanX> = Gson().fromJson(json, object : TypeToken<MutableList<VOD.DataBeanX>>(){}.type)
            VideoDataContainer.getInstance().mVideoData = videoItems

            if (!TextUtils.isEmpty(item.stream)) {
                startActivity(Intent(this@LatestViewVODActivity, ShoppingPlayerActivity::class.java).apply {
                    putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_PLAYER_FLAG, AppConstants.LATEST_VIEW_VOD_PLAYER)
                    putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_MY_VOD_POSITION, position)
                    putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_CASTER_ID, item.userId)
                    putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_VIDEO_TYPE, item.videoType)

                    data = Uri.parse("vcommerce://shopping?url=${item.stream}")
                })
            } else {
                AppToast(this@LatestViewVODActivity).showToastMessage("방송 정보 메타 데이터가 존재하지 않습니다.",
                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                        AppToast.GRAVITY_BOTTOM)
            }
        } else {
            AppToast(this@LatestViewVODActivity).showToastMessage("제재된 영상은 재생 할 수 없습니다.",
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM)
        }
    }

    private fun handleNetworkResult(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            var str = DBManager.getInstance(this@LatestViewVODActivity).get(data.arg1)
            var response: API37 = Gson().fromJson(str, API37::class.java)

            isEndOfData = response.data.size == 0
            pageCount = response.pageCount

            if(response.result == "failure") {
                textViewTotalCount.text = "0개"
                setEmptyViewVisible(true)
                return
            }

            textViewTotalCount.text = "${response.nTotalCount}개"

            if (response.nTotalCount > 0) {
                setEmptyViewVisible(false)
                if (!isReload) {
                    mAdapter.setItems(response.data)
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
    }

    private fun setEmptyViewVisible(visible: Boolean) {
        if (visible) {
            layoutEmpty.visibility = View.VISIBLE
            layoutEdit.visibility = View.GONE
            recyclerViewLatesView.visibility = View.GONE
        } else {
            layoutEmpty.visibility = View.GONE
            layoutEdit.visibility = View.VISIBLE
            recyclerViewLatesView.visibility = View.VISIBLE
        }
    }

    private val clickListener = View.OnClickListener {
        when (it?.id) {
            R.id.buttonClose -> onBackPressed()

            R.id.btnEdit -> {
                if(mAdapter != null) {
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
                if (mAdapter.getSelectedCount() > 0) {
                    dialog = AppAlertDialog(this@LatestViewVODActivity)
                    dialog.setTitle("최근 본 영상")
                    dialog.setMessage("선택한 영상을 삭제할까요?")
                    dialog.setLeftButton(getString(R.string.msg_my_follow_cancel), View.OnClickListener {
                        dialog.dismiss()
                    })
                    dialog.setRightButton(getString(R.string.msg_my_qna_del), View.OnClickListener {
                        streamKey = mAdapter.getSelectItemKey()
                        Logger.e("streamKey : $streamKey")
                        NetworkBus(NetworkApi.API100.name, streamKey).let {
                            EventBus.getDefault().post(it)
                        }
                    })

                    dialog.show()
                }
            }
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

                AppToast(this@LatestViewVODActivity).showToastMessage("loading 중...",
                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                        AppToast.GRAVITY_BOTTOM)

                NetworkBus(NetworkApi.API37.name, pageCount.toString()).let {
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
}