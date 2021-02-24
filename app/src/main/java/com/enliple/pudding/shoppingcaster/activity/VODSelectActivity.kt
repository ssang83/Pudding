package com.enliple.pudding.shoppingcaster.activity

import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.View
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.ui_compat.PixelUtil
import com.enliple.pudding.commons.widget.recyclerview.GridSpacingItemDecoration
import com.enliple.pudding.commons.widget.recyclerview.WrappedGridLayoutManager
import com.enliple.pudding.R
import com.enliple.pudding.shoppingcaster.adapter.VODSelectAdapter
import com.enliple.pudding.shoppingcaster.data.VODInfo
import kotlinx.android.synthetic.main.activity_vod_select.*
import java.io.File

/**
 */
class VODSelectActivity : AppCompatActivity(), VODSelectAdapter.Listener {

    companion object {
        private const val RECYCLER_VIEW_ITEM_CACHE_COUNT = 20
        private const val RECYCLER_VIEW_GRID_SPAN_COUNT = 3
    }

    private lateinit var mAdapter: VODSelectAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_vod_select)

        recyclerViewGallery.setItemViewCacheSize(RECYCLER_VIEW_ITEM_CACHE_COUNT)
        recyclerViewGallery.setHasFixedSize(true)
        recyclerViewGallery.layoutManager = WrappedGridLayoutManager(this, RECYCLER_VIEW_GRID_SPAN_COUNT).apply {
            orientation = androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
        }

        recyclerViewGallery.addItemDecoration(GridSpacingItemDecoration(RECYCLER_VIEW_GRID_SPAN_COUNT,
                PixelUtil.dpToPx(this, 3), false))

        buttonConfirm.setOnClickListener(clickListener)
        buttonClose.setOnClickListener(clickListener)

        Handler().postDelayed(Runnable { getVideoList() }, 500)
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    override fun onVODItemClicked(position: Int) {
        mAdapter?.setSelectIndex(position)
        buttonConfirm.isEnabled = true
    }

    private fun getVideoList() {
        var videoList = ArrayList<VODInfo>()
        val projection = arrayOf(MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.DATE_ADDED,
                MediaStore.Video.Media.RESOLUTION)

        val order = "${MediaStore.Video.Media.DATE_ADDED} DESC"

        var c: Cursor? = null
        try {
            c = contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, null, null, order)
            if (c == null || c.count == 0) {
                Logger.e("cursor count null or zero")
                return
            }

            Logger.d("count : ${c.count}")
            c.moveToFirst()

            do {
                if (c.getLong(c.getColumnIndex(MediaStore.Video.Media.DURATION)) > 0) {
                    var video = VODInfo(c.getLong(c.getColumnIndex(MediaStore.Video.Media._ID)),
                            c.getString(c.getColumnIndex(MediaStore.Video.Media.TITLE)),
                            c.getString(c.getColumnIndex(MediaStore.Video.Media.DATA)),
                            c.getLong(c.getColumnIndex(MediaStore.Video.Media.SIZE)),
                            c.getLong(c.getColumnIndex(MediaStore.Video.Media.DURATION)))
                    videoList.add(video)
                }
            } while (c.moveToNext())

            Logger.e("videoList : ${videoList.size}")
        } catch (e: Exception) {
            Logger.p(e)
        } finally {
            c?.close()
        }

        mProgress.visibility = View.GONE

        mAdapter = VODSelectAdapter()
        mAdapter.setListener(this@VODSelectActivity)
        recyclerViewGallery.adapter = mAdapter
        mAdapter.setItems(videoList)
    }

    private val clickListener = View.OnClickListener {
        when (it?.id) {
            R.id.buttonClose -> {
                setResult(Activity.RESULT_CANCELED)
                finish()
            }

            R.id.buttonConfirm -> {
                if ((mAdapter?.selectedPosition ?: -1) > -1) {
                    var result = Intent().apply {
                        // 선택된 비디오를 사용할 수 있도록 Uri 정보를 반환
                        data = Uri.fromFile(File(mAdapter?.getItem(mAdapter!!.selectedPosition)?.path))
                        putExtra("path", mAdapter?.getItem(mAdapter!!.selectedPosition)?.path)
                    }
                    setResult(RESULT_OK, result)

                } else {
                    setResult(Activity.RESULT_CANCELED)
                }

                finish()
            }
        }
    }
}