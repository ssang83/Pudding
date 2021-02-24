package com.enliple.pudding.shoppingcaster.activity

import android.Manifest
import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.enliple.pudding.BuildConfig
import com.enliple.pudding.R
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.ui_compat.PixelUtil
import com.enliple.pudding.commons.widget.recyclerview.GridSpacingItemDecoration
import com.enliple.pudding.commons.widget.recyclerview.WrappedGridLayoutManager
import com.enliple.pudding.commons.widget.recyclerview.WrappedLinearLayoutManager
import com.enliple.pudding.model.PermissionObject
import com.enliple.pudding.shoppingcaster.adapter.PhotoBucketAdapter
import com.enliple.pudding.shoppingcaster.adapter.PhotoSelectAdapter
import com.enliple.pudding.shoppingcaster.data.BucketInfo
import com.enliple.pudding.widget.MainPermissionDialog
import com.isseiaoki.simplecropview.CropImageView
import com.isseiaoki.simplecropview.callback.CropCallback
import com.isseiaoki.simplecropview.callback.LoadCallback
import kotlinx.android.synthetic.main.activity_cover_select.*
import kotlinx.android.synthetic.main.popup_bucket_list.view.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.channels.FileChannel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * 방송에 사용될 Cover Image 를 선택하는 Custom Gallery Activity
 * @author hkcha
 * @since 2018.08.23
 */
class CoverSelectActivity : AppCompatActivity(),
        PhotoBucketAdapter.BucketClickListener,
        PhotoSelectAdapter.Listener {

    companion object {
        private const val TAG = "CoverSelectActivity"
        private const val RECYCLER_VIEW_ITEM_CACHE_COUNT = 20
        private const val RECYCLER_VIEW_GRID_SPAN_COUNT = 3
        private const val BUCKET_ID_ALL = -2L
        private const val INDEX_CAMERA = 0
        private const val INDEX_READ_EXTERNAL_STORAGE = 1
        private const val INDEX_WRITE_EXTERNAL_STORAGE = 2
        private const val TRUE_VALUE = "TRUE"
        private const val FALSE_VALUE = "FALSE"
        private const val NONE_VALUE = "NONE"

        private const val PERMISSION_REQUEST_CODE = 0xBC01
        private const val PERMISSION_REQUEST_RETRY_MAXIMUM = 3
    }

    private val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE)

    private var mBucketListPopupWindow: PopupWindow? = null
    private var mBucketList: MutableList<BucketInfo> = ArrayList()
    private var mSelectedBucketIndex: Int = 0

    private var mMediaAdapter: PhotoSelectAdapter? = null
    private var mGridLayoutManager: WrappedGridLayoutManager? = null
    private var permRequestRetryCnt = 0
    private var alertDialog: AlertDialog? = null

    private var dialog: MainPermissionDialog? = null
    private var rationalArray: ArrayList<String>? = null
    private var requestPermissionArray: ArrayList<String>? = null
    private var cTime = 0L
    private var sourceUri: Uri? = null
    private val mCompressFormat = Bitmap.CompressFormat.JPEG
    private var isCircle = false
    private var isSquare = false
    private var ratioWidth = 360
    private var ratioHeight = 202
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cover_select)

        recyclerViewGallery.setItemViewCacheSize(RECYCLER_VIEW_ITEM_CACHE_COUNT)
        recyclerViewGallery.setHasFixedSize(true)
        recyclerViewGallery.layoutManager = WrappedGridLayoutManager(this, RECYCLER_VIEW_GRID_SPAN_COUNT).apply {
            orientation = androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
        }
        if ( intent != null ) {
            var t_isCircle = intent.getBooleanExtra("cropCircle", false)
            var t_isSquare = intent.getBooleanExtra("ratio_square", false)
            isCircle = t_isCircle
            if ( t_isSquare ) {
                ratioWidth = 360
                ratioHeight = 360
            }
        }


        recyclerViewGallery.addItemDecoration(GridSpacingItemDecoration(RECYCLER_VIEW_GRID_SPAN_COUNT,
                PixelUtil.dpToPx(this, 3), false))

        buttonConfirm.setOnClickListener(clickListener)
        layoutBucket.setOnClickListener(clickListener)
        buttonClose.setOnClickListener(clickListener)
        buttonRotateLeft.setOnClickListener(clickListener)
        buttonRotateRight.setOnClickListener(clickListener)
        buttonDone.setOnClickListener(clickListener)
        buttonCropBack.setOnClickListener(clickListener)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || checkPermissions()) {
            init()
        } else {
//            requestPermissions(permissions, PERMISSION_REQUEST_CODE)
            requestPermissions()
        }
        Logger.e("isCircle :: " + isCircle)
        if ( isCircle )
            cropImageView.setCropMode(CropImageView.CropMode.CIRCLE)
        else {
            cropImageView.setCustomRatio(ratioWidth, ratioHeight)
        }
//        cropImageView.setGuideColor(0x00ffffff)
//        cropImageView.setHandleColor(0x9f56f2)
//        cropImageView.setGuideColor(0x9f56f2)
//        cropImageView.setBackgroundColor(0x192028)
//        cropImageView.setHandleSizeInDp(10)
        cropImageView.setGuideShowMode(CropImageView.ShowMode.SHOW_ON_TOUCH)
        cropImageView.setFrameStrokeWeightInDp(1)
    }

    override fun onResume() {
        super.onResume()
        val cameraPerm = ActivityCompat.checkSelfPermission(this@CoverSelectActivity, Manifest.permission.CAMERA)
        val readPerm = ActivityCompat.checkSelfPermission(this@CoverSelectActivity, Manifest.permission.READ_EXTERNAL_STORAGE)
        val writePerm = ActivityCompat.checkSelfPermission(this@CoverSelectActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        Logger.e("cameraPerm :: " + cameraPerm)
        Logger.e("readPerm :: " + cameraPerm)
        Logger.e("writePerm :: " + cameraPerm)
        if (cameraPerm == PackageManager.PERMISSION_GRANTED && readPerm == PackageManager.PERMISSION_GRANTED && writePerm == PackageManager.PERMISSION_GRANTED) {
            if (dialog != null)
                dialog!!.dismiss()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        var isOpenedCursor = mMediaAdapter?.isClosedCursor() == false
        if (isOpenedCursor) {
            mMediaAdapter?.closeCursor()
        }
    }

    override fun onBackPressed() {
        if ( cropLayer.visibility == View.VISIBLE ) {
            cropLayer.visibility = View.GONE
        } else {
            setResult(Activity.RESULT_CANCELED)
            Logger.e("finish 1")
            finish()
        }
    }

    private fun requestPermissions() {
        val cameraPerm = ActivityCompat.checkSelfPermission(this@CoverSelectActivity, Manifest.permission.CAMERA)
        val readPerm = ActivityCompat.checkSelfPermission(this@CoverSelectActivity, Manifest.permission.READ_EXTERNAL_STORAGE)
        val writePerm = ActivityCompat.checkSelfPermission(this@CoverSelectActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (cameraPerm != PackageManager.PERMISSION_GRANTED || readPerm != PackageManager.PERMISSION_GRANTED || writePerm != PackageManager.PERMISSION_GRANTED) { // 권한 설정이 완료되지 않았을 경우
            val `object` = PermissionObject()
            val array = ArrayList<PermissionObject.Objects>()
            var s_object: PermissionObject.Objects? = null
            requestPermissionArray = ArrayList<String>()
            if (cameraPerm != PackageManager.PERMISSION_GRANTED) {
                s_object = PermissionObject.Objects()
                s_object!!.image = R.drawable.authority_camera_ico
                s_object!!.setTitle("카메라 (필수)")
                s_object!!.setContent("푸딩에서 판매자가 상품 판매 시 사용합니다. 푸딩에서 판매자가 상품 판매 시 사용합니다. 푸딩에서 판매자가 상품 판매 시 사용합니다.")
                array.add(s_object)
                requestPermissionArray!!.add(Manifest.permission.CAMERA)
            }

            if (readPerm != PackageManager.PERMISSION_GRANTED) {
                s_object = PermissionObject.Objects()
                s_object!!.image = R.drawable.authority_pic_ico
                s_object!!.title = "사진/미디어/파일 (필수)"
                s_object!!.setContent("푸딩에서 판매자가 상품 판매 시 사용합니다. 푸딩에서 판매자가 상품 판매 시 사용합니다. 푸딩에서 판매자가 상품 판매 시 사용합니다.")
                array.add(s_object)
                requestPermissionArray!!.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                requestPermissionArray!!.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }

            `object`.setTitle("푸딩 앱을 즐기기 위해\n다음의 앱 권한을 허용해주세요.")
            `object`.setObjectArray(array)

            dialog = MainPermissionDialog(`object`)
            dialog!!.setListener(object : MainPermissionDialog.DialogClickListener {
                override fun onDialogClick() {
                    cTime = System.currentTimeMillis()
                    rationalArray = ArrayList()
                    var onlyCameraDenied = NONE_VALUE
                    var onlyReadExternalStorageDenied = NONE_VALUE
                    var onlyWriteExternalStorageDenied = NONE_VALUE

                    if (cameraPerm != PackageManager.PERMISSION_GRANTED) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this@CoverSelectActivity, Manifest.permission.CAMERA)) {
                            // 사용자가 다시 보지 않기에 체크하지 않고 권한 설정을 거절한 이력이 있을 경우
                            onlyCameraDenied = TRUE_VALUE
                        } else {
                            // 사용자가 다시 보지 않기에 체크하고 권한 설정을 거절한 이력이 있을 경우
                            onlyCameraDenied = FALSE_VALUE
                        }
                    }
                    rationalArray!!.add(INDEX_CAMERA, onlyCameraDenied)

                    if (readPerm != PackageManager.PERMISSION_GRANTED) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this@CoverSelectActivity, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                            // 사용자가 다시 보지 않기에 체크하지 않고 권한 설정을 거절한 이력이 있을 경우
                            onlyReadExternalStorageDenied = TRUE_VALUE
                        } else {
                            // 사용자가 다시 보지 않기에 체크하고 권한 설정을 거절한 이력이 있을 경우
                            onlyReadExternalStorageDenied = FALSE_VALUE
                        }

                    }
                    rationalArray!!.add(INDEX_READ_EXTERNAL_STORAGE, onlyReadExternalStorageDenied)

                    if (writePerm != PackageManager.PERMISSION_GRANTED) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this@CoverSelectActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            // 사용자가 다시 보지 않기에 체크하지 않고 권한 설정을 거절한 이력이 있을 경우
                            onlyWriteExternalStorageDenied = TRUE_VALUE
                        } else {
                            // 사용자가 다시 보지 않기에 체크하고 권한 설정을 거절한 이력이 있을 경우
                            onlyWriteExternalStorageDenied = FALSE_VALUE
                        }
                    }
                    rationalArray!!.add(INDEX_WRITE_EXTERNAL_STORAGE, onlyWriteExternalStorageDenied)
                    for (i in 0 until rationalArray!!.size) {
                        Logger.e("rationalArray value :: " + rationalArray!!.get(i))
                    }
                    if (requestPermissionArray != null) {
                        var permissionArray = requestPermissionArray!!.toTypedArray<String?>()
                        for (j in 0 until permissionArray.size)
                            Logger.e("permissionArray value :: " + permissionArray!!.get(j))
                        ActivityCompat.requestPermissions(this@CoverSelectActivity, permissionArray, PERMISSION_REQUEST_CODE)
                    }
                }

                override fun onDismissed() {
                    Logger.e("onDismissed call")
                    val cameraPerm = ActivityCompat.checkSelfPermission(this@CoverSelectActivity, Manifest.permission.CAMERA)
                    val readPerm = ActivityCompat.checkSelfPermission(this@CoverSelectActivity, Manifest.permission.READ_EXTERNAL_STORAGE)
                    val writePerm = ActivityCompat.checkSelfPermission(this@CoverSelectActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)

                    if (cameraPerm == PackageManager.PERMISSION_GRANTED && readPerm == PackageManager.PERMISSION_GRANTED && writePerm == PackageManager.PERMISSION_GRANTED) {
                    } else {
                        Logger.e("onDismissed called sssss")
                        Logger.e("finish 11");
                        finish()
                    }
                }
            })
            dialog!!.show(supportFragmentManager, "")
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_CODE) {
            val now = System.currentTimeMillis()
            val gap = now - cTime
            var result_t_count = 0
            var isAllGranted = true

            for (i in grantResults.indices) {
                if (grantResults[i] == 0) {
                    result_t_count++
                } else {
                    isAllGranted = false
                }
            }

            if (isAllGranted) {
                // 모든 권한 허용됨
                init()
            } else {
                if (rationalArray != null) {
                    val prevCamera = rationalArray!!.get(INDEX_CAMERA)
                    val prevReadExternalStorage = rationalArray!!.get(INDEX_READ_EXTERNAL_STORAGE)
                    val prevWriteExternalStorage = rationalArray!!.get(INDEX_WRITE_EXTERNAL_STORAGE)
                    if (prevCamera == TRUE_VALUE && prevReadExternalStorage == TRUE_VALUE && prevWriteExternalStorage == TRUE_VALUE) {
                        // 모두 한번씩 봤었던 권한들인데 전부 설정을 안한 경우 사용자가 승인 하지 않은 것으로 봄
                        Logger.e("finish 111");
                        finish()
                    } else {
                        var false_cnt = 0
                        var true_cnt = 0
                        for (i in rationalArray!!.indices) {
                            if (rationalArray!!.get(i) == TRUE_VALUE) {
                                true_cnt++
                            } else if (rationalArray!!.get(i) == FALSE_VALUE) {
                                false_cnt++
                            }
                        }

                        // 설정 이전에 권한이 true false가 섞여 있을 경우
                        // 즉, 사용자가 첫 권한 설정이 아니면서 처음에 다시보지 않기를 한 항목이 있을 경우
                        if (false_cnt > 0 && true_cnt > 0) {
                            if (true_cnt == result_t_count) {
                                // 설정으로 넘김, 사용자가 이전에 설정 안한 갯수(다시보지않기 제외) 권한설정 갯수가 같으면 다시보지 않기 제외하고는 모두 권한 설정했다는 뜻이므로 다시보지않기 했던 권한 설정할 수 있도록
                                goSetting()
                            } else {
                                // dialog 닫고 화면 닫고, 사용자가 이전 설정 안한것들 중 현재 다시 설정을 안한 값이 있다는 뜻이므로
                                Logger.e("finish 1111");
                                finish()
                            }
                        } else if (false_cnt > 0 && true_cnt == 0) {
                            if (gap < 1000) {
                                // 설정으로 넘김, 첫 설정이 모두 false 인데 1초이내 결과가 모두 넘어온 경우 즉, 사용자가 이전에 모두 다시보지 않기 눌렀을 경우
                                goSetting()
                            } else {
                                // dialog 닫고 화면 닫고, 첫설정이 모두 false인데 1초 이후 들어온 경우이므로 첫 설정 다이얼로그 떴음, 그런데 결과가 모두 true가 아니므로 사용자가 권한 사용 안하겠다는거.
                                Logger.e("finish 11111");
                                finish()
                            }
                        } else if (false_cnt == 0 && true_cnt > 0) {
                            Logger.e("finish 111111");
                            finish()
                            // dialog 닫고 화면 닫고, 사용자가 이전에 모두 권한 사용 안함 눌렀는데 결과가 모두 true가 아닌 경우 즉. 사용자가 권한 사용 안하겠다는 의미
                        } else {
                            goSetting()
                            // 설정으로 넘김
                        }
                    }
                }
            }
        }
    }

    private fun goSetting() {
        val i = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID))
        startActivity(i)
    }

    override fun onBucketItemClicked(position: Int) {
        Logger.e("position :: $position")
        if (mBucketList == null || mBucketList.size == 0) {
            return
        }

        mSelectedBucketIndex = position
        var bi = mBucketList[position]
        selectBucket(bi.id)

        if (bi.name.equals("0", true) || bi.name.equals("/", true)) {
            textViewTitle.text = "/"
        } else {
            textViewTitle.text = bi.name
        }

        dismissBucketSelectPopup()
    }

    override fun onMediaItemClicked(position: Int) {
        Logger.e("onMediaItemClicked call position :: $position")
        mMediaAdapter?.setSelectedIndex(position)
        buttonConfirm.isEnabled = true
    }

    private fun init() {
        object : AsyncTask<Void, Void, Void>() {
            override fun onPreExecute() {
                recyclerViewGallery.itemAnimator = null
                mProgress.visibility = View.VISIBLE
            }

            override fun doInBackground(vararg params: Void?): Void? {
                mBucketList = loadBucketList()
                return null
            }

            override fun onPostExecute(result: Void?) {
                mGridLayoutManager = WrappedGridLayoutManager(this@CoverSelectActivity, RECYCLER_VIEW_GRID_SPAN_COUNT)
                recyclerViewGallery.layoutManager = mGridLayoutManager
                mMediaAdapter = PhotoSelectAdapter().apply {
                    listener = this@CoverSelectActivity
                }
                recyclerViewGallery.adapter = mMediaAdapter

                onBucketItemClicked(0)          // 최초 전체보기를 활성화
                mProgress.visibility = View.GONE
            }
        }.execute()
    }

    /**
     * 전체 이미지 보기
     */
    private fun loadAllMedia(): Cursor? {
        return loadMedia()
    }

    /**
     * 버킷 리스트 정보를 로드 하여 반환
     * @return
     */
    @Suppress("LocalVariableName")
    private fun loadBucketList(): MutableList<BucketInfo> {
        Logger.e(TAG, "loadBucketList()")
        val PROJECTION = arrayOf(MediaStore.Images.ImageColumns.BUCKET_ID,
                MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME
        )
        val SELECTION = "1) GROUP BY 1,(2"
        val ORDER_BY = "${MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME} ASC"
        val URI: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        var bucketCursor = contentResolver.query(URI, PROJECTION, SELECTION, null, ORDER_BY)
        var bucketList = ArrayList<BucketInfo>()
        if (bucketCursor == null || bucketCursor.count == 0) {
            Logger.e(TAG, "Bucket List is null")
            return bucketList
        } else {
            val COL_BUCKET_ID = bucketCursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID)
            val COL_BUCKET_NAME = bucketCursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)

            while (bucketCursor.moveToNext()) {
                var id = bucketCursor.getLong(COL_BUCKET_ID)
                var name = bucketCursor.getString(COL_BUCKET_NAME)

                // Bucket 내부에 지원되는 Format(JPG, PNG) Thumbnail 에 표시되도록 조건 검사
                val BUCKET_MEDIA_PROJECTION = arrayOf(MediaStore.Files.FileColumns._ID,
                        MediaStore.Files.FileColumns.DATA)

                val BUCKET_MEDIA_SELECTIONS = "bucket_id = $id " +
                        "AND ${MediaStore.Files.FileColumns.MEDIA_TYPE} = ${MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE}" +
                        " AND ${MediaStore.Files.FileColumns.MIME_TYPE} NOT LIKE '%gif%'"

                val URI = MediaStore.Files.getContentUri("external")
                val ORDER = "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC"

                var itemCursor = contentResolver.query(URI, BUCKET_MEDIA_PROJECTION,
                        BUCKET_MEDIA_SELECTIONS, null, ORDER)

                if (itemCursor != null && itemCursor.count > 0) {
                    itemCursor.moveToFirst()

                    var latestMediaId = itemCursor.getLong(itemCursor.getColumnIndex(MediaStore.Files.FileColumns._ID))
                    var latestMediaPath = itemCursor.getString(itemCursor.getColumnIndex(MediaStore.Files.FileColumns.DATA))
                    var path = latestMediaPath.substring(0, latestMediaPath.lastIndexOf("/"))
                    bucketList.add(BucketInfo(id, name, path, latestMediaId, latestMediaPath, itemCursor.count))
                }

                itemCursor?.close()
            }

            bucketCursor?.close()
        }

        // 전체 Media Count 를 확인
        var allMediaCount = 0
        for (bucket in bucketList) {
            allMediaCount += bucket.mediaCount
        }

        // 전체 보기 bucket 추가
        bucketList.add(0, BucketInfo(BUCKET_ID_ALL, "전체보기", bucketList[0].path, bucketList[0].latestMediaId, bucketList[0].latestMediaPath, allMediaCount))

        return bucketList
    }

    /**
     * 버켓 선택 팝업 윈도우가 활성화 되어있는지 확인
     * @return
     */
    private fun isBucketSelectPopupOpened(): Boolean = mBucketListPopupWindow?.isShowing ?: false

    /**
     * 버켓 선택 리스트 팝업창을 닫기
     */
    private fun dismissBucketSelectPopup() {
        if (mBucketListPopupWindow?.isShowing == true) {
            mBucketListPopupWindow?.dismiss()
        }

        mBucketListPopupWindow = null
    }

    /**
     * 버켓 선택 리스트 팝업창을 표시
     */
    private fun showBucketSelectPopup() {
        Logger.e("mBucketListPopupWindow?.isShowing :: " + mBucketListPopupWindow?.isShowing)
        if (mBucketListPopupWindow?.isShowing == true) return

        var inflater = LayoutInflater.from(this)
        val innerView = inflater.inflate(R.layout.popup_bucket_list, null, false)

        val bucketAdapter = PhotoBucketAdapter()
        bucketAdapter.clickListener = this
        bucketAdapter.setItems(mBucketList)

        val layoutManager = WrappedLinearLayoutManager(this)
        layoutManager.orientation = androidx.recyclerview.widget.LinearLayoutManager.VERTICAL

        innerView.mBucketRecyclerView.layoutManager = layoutManager
        innerView.mBucketRecyclerView.adapter = bucketAdapter

        mBucketListPopupWindow = PopupWindow(innerView, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT, true)

        mBucketListPopupWindow?.isOutsideTouchable = true
        mBucketListPopupWindow?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android Nougat 이상의 버전에서는 showAsDropDown Function 이 올바르게 동작하지 않는다.
            // 전체 Window 를 기준으로 DropDown 을 수동으로 설정해야 한다
            var decorViewHeight = window?.findViewById<View>(android.R.id.content)?.height ?: 0
            var systemNaviHeight = PixelUtil.dpToPx(this, 26)
            var titleBarHeight = PixelUtil.dpToPx(this, 57)
            mBucketListPopupWindow?.height = decorViewHeight - titleBarHeight
            mBucketListPopupWindow?.animationStyle = android.R.style.Animation_Dialog
            mBucketListPopupWindow?.showAtLocation(window?.decorView, Gravity.NO_GRAVITY,
                    0, titleBarHeight + systemNaviHeight)
        } else {
            mBucketListPopupWindow?.showAsDropDown(layoutRoot)
        }

        mBucketListPopupWindow?.update()

        innerView.mBucketRecyclerView.post {
            innerView.mBucketRecyclerView.getChildAt(mSelectedBucketIndex)?.isSelected = true
        }
    }

    /**
     * Bucket ID 를 통한 MediaList 호출
     */
    @Suppress("LocalVariableName")
    private fun loadMedia(bucketId: Long): Cursor? {
        Logger.e(TAG, "loadMedia($bucketId)")
        val PROJECTION = arrayOf(MediaStore.Files.FileColumns._ID,
                "bucket_id",
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.DATE_ADDED,
                MediaStore.Files.FileColumns.MEDIA_TYPE,
                MediaStore.Files.FileColumns.MIME_TYPE,
                MediaStore.Files.FileColumns.TITLE)

        val SELECTIONS = "bucket_id = $bucketId" +
                " AND ${MediaStore.Files.FileColumns.MEDIA_TYPE} = ${MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE}" +
                " AND ${MediaStore.Files.FileColumns.MIME_TYPE} NOT LIKE '%video%'" +
                " AND ${MediaStore.Files.FileColumns.MIME_TYPE} NOT LIKE '%gif%'"

        val URI = MediaStore.Files.getContentUri("external")
        val ORDER_BY = "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"

        var cursor: Cursor?
        var isValidSuccessFully: Boolean
        do {
            isValidSuccessFully = true
            cursor = contentResolver.query(URI, PROJECTION, SELECTIONS, null, ORDER_BY)
            var deleteTargets: MutableMap<Long, Uri> = HashMap()

            if (cursor != null) {
                while (cursor!!.moveToNext()) {
                    var id = cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA))
                    var fileName = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA))

                    if (fileName.isEmpty() || !File(fileName).exists()) {
                        // 삭제된 파일이 발견된 경우 해당 파일에 대한 MediaStore Database 내용을 삭제처리
                        var deletedUri = ContentUris.withAppendedId(URI, id)
                        Logger.e(TAG, "MediaStore file is deleted detect : $deletedUri")
                        deleteTargets[id] = deletedUri
                        isValidSuccessFully = false
                    }
                }

                if (deleteTargets.isNotEmpty()) {
                    deleteTargets.keys.forEach {
                        Logger.d(TAG, "Delete file not founds info : ${deleteTargets[it]}")
                        contentResolver?.delete(deleteTargets[it], null, null)
                    }

                    deleteTargets.clear()
                }
            }

            if (!isValidSuccessFully) cursor?.close()
        } while (!isValidSuccessFully)

        return cursor
    }

    private fun loadMedia(): Cursor? {
        return getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, "${MediaStore.Files.FileColumns.DATE_ADDED} DESC")
    }

    /**
     * Bucket 을 선택
     */
    private fun selectBucket(bucketId: Long): Boolean {
        Logger.e(TAG, "selectBucket($bucketId)")
        if (mBucketList.isEmpty()) return false

        object : AsyncTask<Void, Void, Void>() {
            var loadCursor: Cursor? = null

            override fun onPreExecute() {
                mProgress.visibility = View.VISIBLE
                mMediaAdapter?.setSelectedIndex(-1)
                buttonConfirm.isEnabled = false
                mMediaAdapter?.closeCursor()
            }

            override fun doInBackground(vararg params: Void?): Void? {
                loadCursor = if (bucketId == -2L) {
                    loadAllMedia()
                } else {
                    loadMedia(bucketId)
                }
                return null
            }

            override fun onPostExecute(result: Void?) {
                mMediaAdapter?.setCursor(loadCursor)
                recyclerViewGallery.smoothScrollToPosition(0)
                mProgress.visibility = View.GONE
            }
        }.execute()

        return true
    }

    /**
     * 시스템 디바이스 사용권한을 체크
     */
    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkPermissions(): Boolean {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            return false
        }

        return true
    }

    private val clickListener = View.OnClickListener {
        when (it?.id) {
            R.id.buttonCropBack -> {
                cropLayer.visibility = View.GONE
            }

            R.id.buttonClose -> {
                setResult(Activity.RESULT_CANCELED)
                Logger.e("finish 1111111");
                finish()
            }

            R.id.buttonConfirm -> {
//                if ((mMediaAdapter?.selectedPosition ?: -1) > -1) {
//                    var result = Intent().apply {
//                        // 선택된 이미지를 사용할 수 있도록 Uri 정보를 반환
//                        data = Uri.fromFile(File(mMediaAdapter?.getMediaInfo(mMediaAdapter!!.selectedPosition)?.path))
//                    }
//                    setResult(RESULT_OK, result)
//
//                } else {
//                    setResult(Activity.RESULT_CANCELED)
//                }
//
//                finish()
                try {
                    //Start Crop Activity
                    var picUri = mMediaAdapter?.getMediaInfo(mMediaAdapter!!.selectedPosition)?.path

//                    var dir = "${Environment.getExternalStorageDirectory()}/temp"
//                    var tFile = File(dir)
//                    if ( !tFile.exists() )
//                        tFile.mkdirs()
//                    var tempUri = "${Environment.getExternalStorageDirectory()}/temp/tempImage.png\""
//                    Logger.e("tempUri :: " + tempUri)
//                    var destFile = File(tempUri)
//                    copyFile(File(picUri!!), destFile)
                    cropLayer.visibility = View.VISIBLE
//                    cropImageView.setCustomRatio(360, 202)
                    sourceUri = Uri.fromFile(File(picUri))
                    cropImageView.load(sourceUri).execute(object: LoadCallback {
                        override fun onSuccess() {
                            Logger.e("Load success")
                        }

                        override fun onError(e: Throwable?) {
                            Logger.e("onError : ${e!!.printStackTrace()}")
                        }

                    })
//            var imagePath = File(getFilesDir(), "temp");
//            var imageFile = File(imagePath, "temptest.jpg")
//            var providedUri = FileProvider.getUriForFile(this@CoverSelectActivity, packageName, imageFile)
//            var cropIntent = Intent("com.android.camera.action.CROP");
//
//            cropIntent.setDataAndType(providedUri, "image/*");
//
//            cropIntent.putExtra("crop", "true");
//            cropIntent.putExtra("aspectX", 9);
//            cropIntent.putExtra("aspectY", 16);
//            cropIntent.putExtra("return-data", true);
//            cropIntent.putExtra("scale", true);
//
//            // Exception will be thrown if read permission isn't granted
//            cropIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//
//            startActivityForResult(cropIntent, 9437);
//            var cropIntent = Intent("com.android.camera.action.CROP");
//            // indicate image type and Uri
//            var f = File(tempUri);
//            var contentUri = FileProvider.getUriForFile(this@CoverSelectActivity, packageName, f)
//            cropIntent.setDataAndType(contentUri, "image/*");
//            // set crop properties
//            cropIntent.putExtra("crop", "true");
//            // indicate aspect of desired crop
//            cropIntent.putExtra("aspectX", 1);
//            cropIntent.putExtra("aspectY", 1);
//            // indicate output X and Y
//            cropIntent.putExtra("outputX", 280);
//            cropIntent.putExtra("outputY", 280);
//
//            // retrieve data on return
//            cropIntent.putExtra("return-data", false);
//            // start the activity - we handle returning in onActivityResult
//            startActivityForResult(cropIntent, 9131)
                }
                // respond to users whose devices do not support the crop action
                catch (anfe: ActivityNotFoundException) {
                    // display an error message
                    var errorMessage = "your device doesn't support the crop action!";
                    var toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
                    toast.show();
                }
            }

            R.id.layoutBucket -> {
                Logger.e("isBucketSelectPopupOpened :: " + isBucketSelectPopupOpened())
                if (isBucketSelectPopupOpened()) {
                    dismissBucketSelectPopup()
                } else {
                    showBucketSelectPopup()
                }
            }

            R.id.buttonRotateLeft -> {
                cropImageView.rotateImage(CropImageView.RotateDegrees.ROTATE_M90D)
            }

            R.id.buttonRotateRight -> {
                cropImageView.rotateImage(CropImageView.RotateDegrees.ROTATE_90D)
            }

            R.id.buttonDone -> {
                cropImageView.crop(sourceUri).execute(object: CropCallback{
                    override fun onSuccess(cropped: Bitmap?) {
                        saveTempBitmap(cropped!!)
//                        cropImageView.save(cropped)
//                                .compressFormat(mCompressFormat)
//                                .execute(createSaveUri(), object: SaveCallback {
//                                    override fun onSuccess(uri: Uri?) {
//                                        saveTempBitmap(cropped!!)
////                                        croppedImage.visibility = View.VISIBLE
////                                        croppedImage.setImageBitmap(cropped)
////                                        var tUri = uri
////                                        var intent = Intent()
////                                        intent.putExtra("crop_uri", tUri.toString())
////                                        intent.putExtra("croped_image", cropped)
////                                        setResult(RESULT_OK, intent)
////                                        finish()
//                                    }
//
//                                    override fun onError(e: Throwable?) {
//
//                                    }
//
//                                })
                    }

                    override fun onError(e: Throwable?) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                })
            }
        }
    }

    fun createSaveUri(): Uri? {
        return createNewUri(this@CoverSelectActivity, mCompressFormat)
    }

    fun getMimeType(format: Bitmap.CompressFormat): String {
        Logger.i("getMimeType CompressFormat = $format")
        when (format) {
            Bitmap.CompressFormat.JPEG -> return "jpeg"
            Bitmap.CompressFormat.PNG -> return "png"
        }
        return "png"
    }

    fun getDirPath(): String {
        var dirPath = ""
        var imageDir: File? = null
        val extStorageDir = Environment.getExternalStorageDirectory()
        if (extStorageDir.canWrite()) {
            imageDir = File(extStorageDir.path + "/temp")
        }
        if (imageDir != null) {
            if (!imageDir.exists()) {
                imageDir.mkdirs()
            }
            if (imageDir.canWrite()) {
                dirPath = imageDir.path
            }
        }
        return dirPath
    }

    fun createNewUri(context: Context, format: Bitmap.CompressFormat): Uri? {
        val currentTimeMillis = System.currentTimeMillis()
        val today = Date(currentTimeMillis)
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss")
        val title = dateFormat.format(today)
        val dirPath = getDirPath()
        val fileName = "scv" + title + "." + getMimeType(format)
        val path = dirPath + "/" + fileName
        val file = File(path)
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, title)
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/" + getMimeType(format))
        values.put(MediaStore.Images.Media.DATA, path)
        val time = currentTimeMillis / 1000
        values.put(MediaStore.MediaColumns.DATE_ADDED, time)
        values.put(MediaStore.MediaColumns.DATE_MODIFIED, time)
        if (file.exists()) {
            values.put(MediaStore.Images.Media.SIZE, file.length())
        }

        val resolver = context.getContentResolver()
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        Logger.i("SaveUri = " + uri!!)
        return uri
    }

    private fun copyFile(sourceFile: File, destFile: File) {
        var source: FileChannel? = null
        var destination: FileChannel? = null
        source = FileInputStream(sourceFile).channel
        destination = FileOutputStream(destFile).channel
        if (destination != null && source != null) {
            destination.transferFrom(source, 0, source.size())
        }
        if (source != null)
            source.close()

        if (destination != null)
            destination.close()
    }

    private fun isExternalStorageWritable(): Boolean {
        var state = Environment.getExternalStorageState()
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true
        }
        return false
    }

    private fun saveTempBitmap(bitmap: Bitmap) {
        if (isExternalStorageWritable()) {
            saveImage(bitmap);
        }else{
        //prompt the user or do something
        }
    }

    private fun saveImage(finalBitmap: Bitmap) {

        var root = Environment.getExternalStorageDirectory().toString()
        var myDir = File("${cacheDir}/temp_image")
        if ( myDir.exists())
            myDir.delete()
        myDir.mkdirs()

        var fname = "cropTemp.jpg"

        var file = File(myDir, fname)
        Logger.e("file.path :: " + file.path)
        if (file.exists()) file.delete ()
        try {
            var out = FileOutputStream(file)
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            out.flush()
            out.close()
            setResult(RESULT_OK)
            finish()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}