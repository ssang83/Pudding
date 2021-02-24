package com.enliple.pudding.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.enliple.pudding.AbsBaseActivity
import com.enliple.pudding.R
import com.enliple.pudding.adapter.my.MyVODAdapter
import com.enliple.pudding.common.VideoDataContainer
import com.enliple.pudding.commons.app.NetworkStatusUtils
import com.enliple.pudding.commons.app.Utils
import com.enliple.pudding.commons.data.TempVOD
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.db.VodDBManager
import com.enliple.pudding.commons.internal.AppConstants
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.NetworkApi
import com.enliple.pudding.commons.network.NetworkBus
import com.enliple.pudding.commons.network.NetworkBusResponse
import com.enliple.pudding.commons.network.NetworkHandler
import com.enliple.pudding.commons.network.vo.API0
import com.enliple.pudding.commons.network.vo.BaseAPI
import com.enliple.pudding.commons.network.vo.VOD
import com.enliple.pudding.commons.widget.recyclerview.WrappedLinearLayoutManager
import com.enliple.pudding.commons.widget.toast.AppToast
import com.enliple.pudding.widget.AppAlertDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_my_vod.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

/**
 * Created by Kim Joonsung on 2018-10-26.
 */
class MyVODActivity : AbsBaseActivity(), MyVODAdapter.Listener {

    companion object {
        private const val TAG = "MyVODActivity"

        private const val RECYCLER_VIEW_ITEM_CACHE_SIZE = 20

        private val REQUEST_PLAYER = 0
    }

    private lateinit var mAdapter: MyVODAdapter
    private lateinit var mLayoutManager:WrappedLinearLayoutManager

    private var totalPageCount = 1
    private var dataKey = ""
    private lateinit var dialog: AppAlertDialog
    private var isTempVOD = false

    private var extendedImageWidth = 0
    private var extendedImageHeight = 0
    private var imageWidth = 0
    private var imageHeight = 0

    private var selectedCount = 0
    private var totalCount = 0
    private var isEditMode = false
    private var allStr = "전체"
    private var pageCount = 1
    private var isReload = false
    private var isEndOfData = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_vod)
        EventBus.getDefault().register(this)
        mActivityList.add(this)     // 강제 Activity 종료시키기 위해 리스트에 추가

        extendedImageWidth = AppPreferences.getScreenWidth(this@MyVODActivity) - Utils.ConvertDpToPx(this@MyVODActivity, 30)
        extendedImageHeight = (extendedImageWidth * 167) / 295
        imageWidth = AppPreferences.getScreenWidth(this@MyVODActivity) - Utils.ConvertDpToPx(this@MyVODActivity, 65)
        imageHeight = (imageWidth * 167) / 295

        buttonClose.setOnClickListener(clickListener)
        buttonCheckAll.setOnClickListener(clickListener)
        btnDelete.setOnClickListener(clickListener)
        btnCancel.setOnClickListener(clickListener)
        btnEdit.setOnClickListener(clickListener)

        recyclerViewMyContent.setHasFixedSize(false)
        recyclerViewMyContent.setItemViewCacheSize(RECYCLER_VIEW_ITEM_CACHE_SIZE)
        recyclerViewMyContent.addOnScrollListener(scrollListener)

        mLayoutManager = WrappedLinearLayoutManager(this)
        recyclerViewMyContent.layoutManager = mLayoutManager

        mAdapter = MyVODAdapter(this, extendedImageWidth, extendedImageHeight, imageWidth, imageHeight).apply {
            setListener(this@MyVODActivity)
        }

        recyclerViewMyContent.adapter = mAdapter

//        var bus = NetworkBus(NetworkApi.API0.name)
//        EventBus.getDefault().post(bus)
        setEditMode(isEditMode)
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onResume() {
        super.onResume()
        Logger.e("onResume")
        isReload = false
        EventBus.getDefault().post(NetworkBus(NetworkApi.API0.name, "1"))
    }

    override fun onDestroy() {
        super.onDestroy()
        mActivityList.remove(this)      // 정상적인 Activity 종료인 경우 리스트에서 제거
        EventBus.getDefault().unregister(this)
    }

    override fun onDozeModeStateChanged(dozeEnable: Boolean) {}
    override fun onNetworkStatusChanged(status: NetworkStatusUtils.NetworkStatus) {}

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK &&
                requestCode == REQUEST_PLAYER) {
            var bus = NetworkBus(NetworkApi.API0.name)
            EventBus.getDefault().post(bus)
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onItemClicked(item: API0.DataBeanX, position: Int) {
        Logger.e(TAG, "URL : ${item.stream}")

        // 페이징 및 dbKey 포지션 문제 때문에 앞으로 아래와 같이 List 객체를 만들어서 영상 데이터 사용
        val json = Gson().toJson(mAdapter.items)
        val videoItems : MutableList<VOD.DataBeanX> = Gson().fromJson(json, object : TypeToken<MutableList<VOD.DataBeanX>>(){}.type)
        VideoDataContainer.getInstance().mVideoData = videoItems

        if (!TextUtils.isEmpty(item.stream) && item.show_YN == "Y") {
            startActivityForResult(Intent(this@MyVODActivity, ShoppingPlayerActivity::class.java).apply {
                putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_PLAYER_FLAG, AppConstants.MY_UPLOAD_VOD_PLAYER)
                putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_MY_VOD_POSITION, position)
                putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_CASTER_ID, item.userId)
                putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_VIDEO_TYPE, item.videoType)

                data = Uri.parse("vcommerce://shopping?url=${item.stream}")
            }, REQUEST_PLAYER)
        } else {
            if (item != null && item.show_YN == "N") {
                AppToast(this@MyVODActivity).showToastMessage("삭제된 동영상은 재생할 수 없습니다.",
                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                        AppToast.GRAVITY_BOTTOM)
            } else {
                AppToast(this@MyVODActivity).showToastMessage("방송 정보 메타 데이터가 존재하지 않습니다.",
                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                        AppToast.GRAVITY_BOTTOM)
            }
        }
    }

    override fun onTemVODClicked() {
        startActivity(Intent(this@MyVODActivity, TempVODActivity::class.java))
    }

    override fun updateSelectCount(selected: Int, total: Int, position: Int) {
        selectedCount = selected
        textViewSelectCount.text = "$selected"
        if( selected == total )
            imageViewCheckAll.isSelected = true
        else
            imageViewCheckAll.isSelected = false
    }

    override fun updateTotalCount(count: Int) {
        textViewTotalCount.text = "${count}"
        totalCount = count
        if( selectedCount == count )
            imageViewCheckAll.isSelected = true
        else
            imageViewCheckAll.isSelected = false
    }

    override fun setTotalCount(itemCount: Int) {
        textViewSelectCount.text = itemCount.toString()
    }

    override fun setEmptyView(visible: Boolean) {
        setEmptyViewVisible(visible)
    }

    @Subscribe
    fun onMessageEvent(data: NetworkBusResponse) {
        dataKey = NetworkHandler.getInstance(this@MyVODActivity).getKey(NetworkApi.API0.toString(), AppPreferences.getUserId(this@MyVODActivity)!!, "")
        with(data.arg1) {
            when {
                startsWith(dataKey) -> handleNetworkResult(data)
                startsWith(NetworkApi.API87.toString()) -> handleNetworkAPI87(data)
            }
        }
    }

    private fun setEmptyViewVisible(visible: Boolean) {
        if (visible) {
            textViewEmpty.visibility = View.VISIBLE
            recyclerViewMyContent.visibility = View.GONE
        } else {
            textViewEmpty.visibility = View.GONE
            recyclerViewMyContent.visibility = View.VISIBLE
        }
    }

    private fun handleNetworkResult(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            var str = DBManager.getInstance(this@MyVODActivity).get(data.arg1)
            var result: API0 = Gson().fromJson(str, API0::class.java)

            Logger.e("############ data :${result.data.size}")
            totalPageCount = result.nTotalCount
            pageCount = result.pageCount
            isEndOfData = result.data.size == 0

            checkTempVOD()

            if (result.nTotalCount > 0 || isTempVOD) {
                setEmptyViewVisible(false)

                if(!isReload) {
                    mAdapter.setItem(result.data, result.summary)
                } else {
                    mAdapter.addItem(result.data, buttonCheckAll.isSelected)
                }

                if (result.nTotalCount == 0 && isTempVOD) {
                    textViewTotalCount.text = "1"
                } else if (result.nTotalCount > 0 && isTempVOD) {
                    textViewTotalCount.text = "${totalPageCount + 1}"
                } else {
                    textViewTotalCount.text = "${totalPageCount}"
                }
            } else {
                textViewTotalCount.text = "0"
                setEmptyViewVisible(true)
            }
        } else {
            var errorResult: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
            Logger.e("error : $errorResult")
        }
    }

    private fun handleNetworkAPI87(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            if(isTempVOD) {
                mAdapter.selectItemDel()
            } else {
                if (imageViewCheckAll.isSelected) {
                    mAdapter.itemDeleteAll()
                    setEmptyViewVisible(true)
                } else {
                    mAdapter.selectItemDel()
                }
            }

            dialog.dismiss()
        } else {
            var errorResult: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
            Logger.e("error : $errorResult")
        }
    }

    private fun checkTempVOD() {
        val items: MutableList<TempVOD> = ArrayList()
        VodDBManager.getInstance(this).loadAllByOrder().forEach {
            val image = it.getDictionary(VodDBManager.KEY).getBlob("thumbnailImage")
            items.add(TempVOD(
                    it.getDictionary(VodDBManager.KEY).getString("id"),
                    it.getDictionary(VodDBManager.KEY).getString("videoUrl"),
                    it.getDictionary(VodDBManager.KEY).getString("broadCastInfo"),
                    image.content,
                    it.getDictionary(VodDBManager.KEY).getString("reg_date")))
        }

        Logger.e("######### TempVOD : ${items.size}")
        if (items.size > 0) {
            isTempVOD = true
            mAdapter.setTempVODVisible(true)
            mAdapter.setTempVODThumnailImages(items.get(0).thumbnail)
            mAdapter.setBroadCastInfo(items.get(0).broadCastInfo)
            mAdapter.setTempVodDate(items.get(0).regDate)
        } else {
            isTempVOD = false
            mAdapter.setTempVODVisible(false)
        }
    }

    private fun setEditMode(isEditMode : Boolean) {
        if ( isEditMode ) {
            allStr = "전체선택"
            textViewAll.text = allStr
            mAdapter.setEditMode(true)
            divider.visibility = View.VISIBLE
            textViewSelectCount.visibility = View.VISIBLE
            buttonCheckAll.visibility = View.VISIBLE
            empty.visibility = View.GONE
        } else {
            allStr = "전체"
            textViewAll.text = allStr
            mAdapter.setEditMode(false)
            divider.visibility = View.GONE
            textViewSelectCount.visibility = View.GONE
            buttonCheckAll.visibility = View.GONE
            empty.visibility = View.VISIBLE
        }
//        var param = empty.layoutParams
//        param.width = margin
//        empty.layoutParams = param
//
//        param = buttonCheckAll.layoutParams
//        param.width = width
//        buttonCheckAll.layoutParams = param
    }

    private val scrollListener = object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val lastVisibleItemPosition = mLayoutManager.findLastCompletelyVisibleItemPosition()
            val totalItemCount = mAdapter.itemCount.minus(1)
            if ((lastVisibleItemPosition == totalItemCount) && !isEndOfData && dy > 0) {
                isReload = true
                ++pageCount

                AppToast(this@MyVODActivity).showToastMessage("loading 중...",
                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                        AppToast.GRAVITY_BOTTOM)

                NetworkBus(NetworkApi.API0.name, pageCount.toString()).let { EventBus.getDefault().post(it) }
            }
        }
    }


    private val clickListener = View.OnClickListener {
        when (it?.id) {
            R.id.buttonClose -> onBackPressed()

            R.id.btnEdit -> {
                btnEdit.visibility = View.GONE
                twoBtnLayer.visibility = View.VISIBLE
                isEditMode = true
                setEditMode(isEditMode)
            }

            R.id.btnCancel -> {
                btnEdit.visibility = View.VISIBLE
                twoBtnLayer.visibility = View.GONE
                isEditMode = false
                setEditMode(isEditMode)
            }

            R.id.btnDelete -> {
                if (mAdapter.getSelectedCount() > 0) {
                    dialog = AppAlertDialog(this@MyVODActivity)
                    dialog.setTitle("내 영상")
                    dialog.setMessage("선택한 영상을 삭제할까요?")
                    dialog.setLeftButton(getString(R.string.msg_my_follow_cancel), View.OnClickListener {
                        dialog.dismiss()
                    })
                    dialog.setRightButton(getString(R.string.msg_my_qna_del), View.OnClickListener {
                        Logger.e("streamKey : ${mAdapter.getSelectedKey()}")
                        var bus = NetworkBus(NetworkApi.API87.name, mAdapter.getSelectedKey())
                        EventBus.getDefault().post(bus)
                    })

                    dialog.show()
                }
            }

            R.id.buttonCheckAll -> {
                if ( imageViewCheckAll.isSelected ) {
                    imageViewCheckAll.isSelected = false
                    textViewSelectCount.text = "0"
                    mAdapter.itemUnSelectAll()
                } else {
                    imageViewCheckAll.isSelected = true
                    textViewSelectCount.text = "${mAdapter.itemCount.minus(1)}"
                    mAdapter.itemSelectAll()
                }
            }
        }
    }
}