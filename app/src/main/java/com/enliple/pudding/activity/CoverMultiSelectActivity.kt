package com.enliple.pudding.activity

import android.Manifest
import android.app.Activity
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.database.MergeCursor
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.enliple.pudding.BuildConfig
import com.enliple.pudding.R
import com.enliple.pudding.adapter.my.PhotoMultiSelectAdapter
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.ui_compat.PixelUtil
import com.enliple.pudding.commons.widget.recyclerview.GridSpacingItemDecoration
import com.enliple.pudding.commons.widget.recyclerview.WrappedGridLayoutManager
import com.enliple.pudding.commons.widget.recyclerview.WrappedLinearLayoutManager
import com.enliple.pudding.model.CoverMultiSelectModel
import com.enliple.pudding.model.PermissionObject
import com.enliple.pudding.shoppingcaster.adapter.PhotoBucketAdapter
import com.enliple.pudding.shoppingcaster.data.BucketInfo
import com.enliple.pudding.shoppingcaster.data.MediaInfo
import com.enliple.pudding.widget.MainPermissionDialog
import kotlinx.android.synthetic.main.activity_cover_multi_select.*
import kotlinx.android.synthetic.main.popup_multi_bucket_list.view.*
import java.io.File
import java.util.*

/**
 * 방송에 사용될 Cover Image 를 선택하는 Custom Gallery Activity
 * @author hkcha
 * @since 2018.08.23
 */
class CoverMultiSelectActivity : AppCompatActivity(),
        PhotoBucketAdapter.BucketClickListener,
        PhotoMultiSelectAdapter.Listener {
    companion object {
        private const val TAG = "CoverMultiSelectActivity"
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

    private var mMediaAdapter: PhotoMultiSelectAdapter? = null
    private var mGridLayoutManager: WrappedGridLayoutManager? = null
    private var permRequestRetryCnt = 0
    private var alertDialog: AlertDialog? = null
    private var selectedImageCount = 0

    private var dialog: MainPermissionDialog? = null
    private var rationalArray: ArrayList<String>? = null
    private var requestPermissionArray: ArrayList<String>? = null
    private var cTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cover_multi_select)
        selectedImageCount = intent.getIntExtra("selected_image_count", 0)
        recyclerViewGallery.setItemViewCacheSize(RECYCLER_VIEW_ITEM_CACHE_COUNT)
        recyclerViewGallery.setHasFixedSize(true)
        recyclerViewGallery.layoutManager = WrappedGridLayoutManager(this, RECYCLER_VIEW_GRID_SPAN_COUNT).apply {
            orientation = RecyclerView.VERTICAL
        }

        recyclerViewGallery.addItemDecoration(GridSpacingItemDecoration(RECYCLER_VIEW_GRID_SPAN_COUNT,
                PixelUtil.dpToPx(this, 3), false))

        buttonConfirm.setOnClickListener(clickListener)
        layoutBucket.setOnClickListener(clickListener)
        buttonClose.setOnClickListener(clickListener)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || checkPermissions()) {
            init()
        } else {
//            requestPermissions(permissions, PERMISSION_REQUEST_CODE)
            requestPermissions()
        }
    }

    override fun onResume() {
        super.onResume()
        val cameraPerm = ActivityCompat.checkSelfPermission(this@CoverMultiSelectActivity, Manifest.permission.CAMERA)
        val readPerm = ActivityCompat.checkSelfPermission(this@CoverMultiSelectActivity, Manifest.permission.READ_EXTERNAL_STORAGE)
        val writePerm = ActivityCompat.checkSelfPermission(this@CoverMultiSelectActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (cameraPerm == PackageManager.PERMISSION_GRANTED && readPerm == PackageManager.PERMISSION_GRANTED && writePerm == PackageManager.PERMISSION_GRANTED) {
            if (dialog != null)
                dialog!!.dismiss()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

//        var isOpenedCursor = mMediaAdapter?.isClosedCursor() == false
//        if (isOpenedCursor) {
//            mMediaAdapter?.closeCursor()
//        }
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    private fun requestPermissions() {
        val cameraPerm = ActivityCompat.checkSelfPermission(this@CoverMultiSelectActivity, Manifest.permission.CAMERA)
        val readPerm = ActivityCompat.checkSelfPermission(this@CoverMultiSelectActivity, Manifest.permission.READ_EXTERNAL_STORAGE)
        val writePerm = ActivityCompat.checkSelfPermission(this@CoverMultiSelectActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
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
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this@CoverMultiSelectActivity, Manifest.permission.CAMERA)) {
                            // 사용자가 다시 보지 않기에 체크하지 않고 권한 설정을 거절한 이력이 있을 경우
                            onlyCameraDenied = TRUE_VALUE
                        } else {
                            // 사용자가 다시 보지 않기에 체크하고 권한 설정을 거절한 이력이 있을 경우
                            onlyCameraDenied = FALSE_VALUE
                        }
                    }
                    rationalArray!!.add(INDEX_CAMERA, onlyCameraDenied)

                    if (readPerm != PackageManager.PERMISSION_GRANTED) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this@CoverMultiSelectActivity, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                            // 사용자가 다시 보지 않기에 체크하지 않고 권한 설정을 거절한 이력이 있을 경우
                            onlyReadExternalStorageDenied = TRUE_VALUE
                        } else {
                            // 사용자가 다시 보지 않기에 체크하고 권한 설정을 거절한 이력이 있을 경우
                            onlyReadExternalStorageDenied = FALSE_VALUE
                        }

                    }
                    rationalArray!!.add(INDEX_READ_EXTERNAL_STORAGE, onlyReadExternalStorageDenied)

                    if (writePerm != PackageManager.PERMISSION_GRANTED) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this@CoverMultiSelectActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
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
                        ActivityCompat.requestPermissions(this@CoverMultiSelectActivity, permissionArray, PERMISSION_REQUEST_CODE)
                    }
                }

                override fun onDismissed() {
                    val cameraPerm = ActivityCompat.checkSelfPermission(this@CoverMultiSelectActivity, Manifest.permission.CAMERA)
                    val readPerm = ActivityCompat.checkSelfPermission(this@CoverMultiSelectActivity, Manifest.permission.READ_EXTERNAL_STORAGE)
                    val writePerm = ActivityCompat.checkSelfPermission(this@CoverMultiSelectActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)

                    if (cameraPerm == PackageManager.PERMISSION_GRANTED && readPerm == PackageManager.PERMISSION_GRANTED && writePerm == PackageManager.PERMISSION_GRANTED) {
                    } else {
                        Logger.e("onDismissed called sssss")
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
                                finish()
                            }
                        } else if (false_cnt > 0 && true_cnt == 0) {
                            if (gap < 1000) {
                                // 설정으로 넘김, 첫 설정이 모두 false 인데 1초이내 결과가 모두 넘어온 경우 즉, 사용자가 이전에 모두 다시보지 않기 눌렀을 경우
                                goSetting()
                            } else {
                                // dialog 닫고 화면 닫고, 첫설정이 모두 false인데 1초 이후 들어온 경우이므로 첫 설정 다이얼로그 떴음, 그런데 결과가 모두 true가 아니므로 사용자가 권한 사용 안하겠다는거.
                                finish()
                            }
                        } else if (false_cnt == 0 && true_cnt > 0) {
                            finish()
                            // dialog 닫고 화면 닫고, 사용자가 이전에 모두 권한 사용 안함 눌렀는데 결과가 모두 true가 아닌 경우 즉. 사용자가 권한 사용 안하겠다는 의미
                        } else {
                            goSetting()
                            // 설정으로 넘김
                        }
                    }
                }
            }
//            var permissionGranted = true
//            for (i in 0 until grantResults.size) {
//                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
//                    permissionGranted = false
//                }
//            }
//
//            if (!permissionGranted) {
//                if (permRequestRetryCnt in 0..(PERMISSION_REQUEST_RETRY_MAXIMUM - 1)) {
//                    requestPermissions(permissions, PERMISSION_REQUEST_CODE)
//                    ++permRequestRetryCnt
//                } else {
//                    alertDialog?.dismiss()
//                    alertDialog = null
//
//                    alertDialog = AlertDialog.Builder(this).apply {
//                        setMessage("사진 파일에 접근하려면 권한이 있어야 선택 가능합니다.")
//                        setPositiveButton(android.R.string.ok) { dialog, _ ->
//                            dialog.dismiss()
//                            finish()
//                        }
//                        setCancelable(false)
//                        setFinishOnTouchOutside(false)
//                    }.create()
//                    alertDialog!!.show()
//                }
//
//                return
//            } else {
//                init()
//            }
        }
    }

    private fun goSetting() {
        val i = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID))
        startActivity(i)
    }

    override fun onBucketItemClicked(position: Int) {
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
//        mMediaAdapter?.setSelectedIndex(position)
//        buttonConfirm.isEnabled = true
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
                mGridLayoutManager = WrappedGridLayoutManager(this@CoverMultiSelectActivity, RECYCLER_VIEW_GRID_SPAN_COUNT)
                recyclerViewGallery.layoutManager = mGridLayoutManager
                mMediaAdapter = PhotoMultiSelectAdapter(selectedImageCount).apply {
                    listener = this@CoverMultiSelectActivity
                }
                mMediaAdapter!!.setHasStableIds(true)
                recyclerViewGallery.adapter = mMediaAdapter

                onBucketItemClicked(0)          // 최초 전체보기를 활성화
                mProgress.visibility = View.INVISIBLE
            }
        }.execute()
    }

    /**
     * 전체 이미지 보기
     */
    private fun loadAllMedia(): Cursor? {
        var cursors: MutableList<Cursor> = LinkedList()
        for (bucket in mBucketList) {
            if (bucket.id != BUCKET_ID_ALL) {
                var cursor = loadMedia(bucket.id)
                if (cursor != null) {
                    cursors.add(cursor)
                }
            }
        }

        return if (cursors != null && cursors.size > 0) MergeCursor(cursors.toTypedArray()) else null
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
                var latestMediaId: Long
                var latestMediaPath: String
                var mediaCount: Int
                var path: String

                // Bucket 내부에 지원되는 Format(JPG, PNG) Thumbnail 에 표시되도록 조건 검사
                val BUCKET_MEDIA_PROJECTION = arrayOf(MediaStore.Files.FileColumns._ID,
                        MediaStore.Files.FileColumns.DATA)

                val BUCKET_MEDIA_SELECTIONS = "bucket_id = $id " +
                        "AND ${MediaStore.Files.FileColumns.MEDIA_TYPE} = ${MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE}" +
                        " AND ${MediaStore.Files.FileColumns.MIME_TYPE} NOT LIKE '%gif%'"
                val BUCKET_MEDIA_URI = MediaStore.Files.getContentUri("external")
                val BUCKET_MEDIA_ORDER_BY = "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"

                var itemCursor = contentResolver.query(BUCKET_MEDIA_URI, BUCKET_MEDIA_PROJECTION,
                        BUCKET_MEDIA_SELECTIONS, null, BUCKET_MEDIA_ORDER_BY)

                if (itemCursor != null && itemCursor.count > 0) {
                    itemCursor.moveToFirst()

                    latestMediaId = itemCursor.getLong(itemCursor.getColumnIndex(MediaStore.Files.FileColumns._ID))
                    latestMediaPath = itemCursor.getString(itemCursor.getColumnIndex(MediaStore.Files.FileColumns.DATA))
                    mediaCount = itemCursor.count
                    path = latestMediaPath.substring(0, latestMediaPath.lastIndexOf("/"))


                    var bi = BucketInfo(id, name, path, latestMediaId, latestMediaPath, mediaCount)
                    bucketList.add(bi)
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
        if (mBucketListPopupWindow?.isShowing == true) return

        var inflater = LayoutInflater.from(this)
        val innerView = inflater.inflate(R.layout.popup_multi_bucket_list, null, false)

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // Android Nougat 이상의 버전에서는 showAsDropDown Function 이 올바르게 동작하지 않는다.
            // 전체 Window 를 기준으로 DropDown 을 수동으로 설정해야 한다
            var decorViewHeight = window?.findViewById<View>(android.R.id.content)?.height ?: 0
            var systemNaviHeight = PixelUtil.dpToPx(this, 26)
            var titleBarHeight = PixelUtil.dpToPx(this, 56)
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
//                mMediaAdapter?.setSelectedIndex(-1)
//                buttonConfirm.isEnabled = false
//                mMediaAdapter?.closeCursor()
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
                if (loadCursor != null && loadCursor?.isClosed == false) {
                    var array = ArrayList<CoverMultiSelectModel>()
                    var columnIndexId = loadCursor?.getColumnIndex(PhotoMultiSelectAdapter.COLUMN_INDEX_KEY_ID)!!
                    var columnIndexData = loadCursor?.getColumnIndex(PhotoMultiSelectAdapter.COLUMN_INDEX_KEY_DATA)!!
                    var columnIndexMimeType = loadCursor?.getColumnIndex(PhotoMultiSelectAdapter.COLUMN_INDEX_KEY_MIME_TYPE)!!

                    for (i in 0 until loadCursor!!.count) {
                        loadCursor?.moveToPosition(i)

                        var id = loadCursor?.getLong(columnIndexId)
                        var path = loadCursor?.getString(columnIndexData)
                        var mimeType = loadCursor?.getString(columnIndexMimeType)
                        var info = MediaInfo(id!!, path, mimeType)

                        var model = CoverMultiSelectModel()
                        model.setSelected(false)
                        model.setInfo(info);
                        array.add(model)
                    }

                    mMediaAdapter?.setItems(array)
                    loadCursor!!.close()
                }

//                mMediaAdapter?.setCursor(loadCursor)
                recyclerViewGallery.smoothScrollToPosition(0)
                mProgress.visibility = View.INVISIBLE
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
            R.id.buttonClose -> {
                setResult(Activity.RESULT_CANCELED)
                finish()
            }

            R.id.buttonConfirm -> {
                var items = mMediaAdapter!!.getSelectedItem()
                if (items != null) {
                    var result = Intent().apply {
                        // 선택된 이미지를 사용할 수 있도록 Uri 정보를 반환
//                        data = Uri.fromFile(File(mMediaAdapter?.getMediaInfo(mMediaAdapter!!.selectedPosition)?.path))
                        var bundle = Bundle()
                        bundle.putParcelableArrayList("URI_ARRAY", items)
                        putExtras(bundle)
//                        data = items
                    }
                    setResult(RESULT_OK, result)
                } else {
                    setResult(Activity.RESULT_CANCELED)
                }

                finish()
            }

            R.id.layoutBucket -> {
                if (isBucketSelectPopupOpened()) {
                    dismissBucketSelectPopup()
                } else {
                    showBucketSelectPopup()
                }
            }
        }
    }
}