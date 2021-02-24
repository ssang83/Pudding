package com.enliple.pudding.activity

import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.View
import com.enliple.pudding.AbsBaseActivity
import com.enliple.pudding.R
import com.enliple.pudding.adapter.my.TempVODAdapter
import com.enliple.pudding.commons.app.NetworkStatusUtils
import com.enliple.pudding.commons.data.TempVOD
import com.enliple.pudding.commons.db.VodDBManager
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.ui_compat.PixelUtil
import com.enliple.pudding.commons.widget.recyclerview.GridSpacingItemDecoration
import com.enliple.pudding.commons.widget.recyclerview.WrappedGridLayoutManager
import com.enliple.pudding.commons.widget.recyclerview.WrappedLinearLayoutManager
import com.enliple.pudding.shoppingcaster.activity.VODPostActivity
import kotlinx.android.synthetic.main.activity_vod_temp.*

/**
 * Created by Kim Joonsung on 2018-11-05.
 */
class TempVODActivity : AbsBaseActivity(), TempVODAdapter.Listener {

    companion object {
        private const val RECYCLER_VIEW_ITEM_CACHE_COUNT = 20
    }

    lateinit var mAdater: TempVODAdapter
    private val tempVODItems: MutableList<TempVOD> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vod_temp)
        mActivityList.add(this)     // 강제 Activity를 종료 시키기 위해서 리스트에 추가

        recyclerVeiwTempVOD.setItemViewCacheSize(RECYCLER_VIEW_ITEM_CACHE_COUNT)
        recyclerVeiwTempVOD.setHasFixedSize(true)
        recyclerVeiwTempVOD.layoutManager = WrappedLinearLayoutManager(this)

        mAdater = TempVODAdapter(this).apply {
            setListener(this@TempVODActivity)
        }
        recyclerVeiwTempVOD.adapter = mAdater

        buttonClose.setOnClickListener(clickListener)

        loadData()
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        mActivityList.remove(this)      // 정상적인 Activity종료인 경우 리스트에서 제거
    }

    override fun onDozeModeStateChanged(dozeEnable: Boolean) {}
    override fun onNetworkStatusChanged(status: NetworkStatusUtils.NetworkStatus) {}

    override fun onSetEmptyView() {
        recyclerVeiwTempVOD.visibility = View.GONE
        textViewEmpty.visibility = View.VISIBLE
    }

    override fun onItemClicked(position: Int) {
        try {
            val vodId = tempVODItems.get(position).id
            Logger.e("TEMP VOD ID : $vodId")

            startActivity(Intent(this@TempVODActivity, VODPostActivity::class.java).apply {
                putExtra(VODPostActivity.INTENT_EXTRA_KEY_VOD_TEMP_ID, vodId)
                putExtra(VODPostActivity.INTENT_EXTRA_KEY_FROM_VOD_TEMP, true)
            })
        } catch (e: Exception) {
            Logger.p(e)
        }
    }

    private fun loadData() {
        VodDBManager.getInstance(this).loadAllByOrder().forEach {
            Logger.e("########## id : ${it.getDictionary(VodDBManager.KEY).getString("id")}")

            val image = it.getDictionary(VodDBManager.KEY).getBlob("thumbnailImage")
            tempVODItems.add(TempVOD(
                    it.getDictionary(VodDBManager.KEY).getString("id"),
                    it.getDictionary(VodDBManager.KEY).getString("videoUrl"),
                    it.getDictionary(VodDBManager.KEY).getString("broadCastInfo"),
                    image.content,
                    it.getDictionary(VodDBManager.KEY).getString("reg_date")))
        }

        Logger.e("######### TempVOD : ${tempVODItems.size}")
        if (tempVODItems.size > 0) {
            recyclerVeiwTempVOD.visibility = View.VISIBLE
            textViewEmpty.visibility = View.GONE

            mAdater.setItems(tempVODItems)
        } else {
            recyclerVeiwTempVOD.visibility = View.GONE
            textViewEmpty.visibility = View.VISIBLE
        }
    }

    private val clickListener = View.OnClickListener {
        when (it?.id) {
            R.id.buttonClose -> onBackPressed()
        }
    }
}