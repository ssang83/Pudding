package com.enliple.pudding.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Images
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.enliple.pudding.R
import com.enliple.pudding.adapter.home.ImageFileAdapter
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.NetworkApi
import com.enliple.pudding.commons.network.NetworkBus
import com.enliple.pudding.commons.network.NetworkBusResponse
import com.enliple.pudding.commons.network.NetworkHandler
import com.enliple.pudding.commons.widget.toast.AppToast
import com.enliple.pudding.widget.AppAlertDialog
import com.enliple.pudding.widget.main.NothingSelectedSpinnerAdapter
import kotlinx.android.synthetic.main.activity_broadcast_report.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.net.URLEncoder


/**
 * Created by Kim Joonsung on 2018-10-08.
 */
class BroadcastReportActivity : AppCompatActivity(), ImageFileAdapter.Listener {

    companion object {
        private const val REQ_CODE_SELECT_IMAGE = 100

        const val INTENT_EXTRA_KEY_REPORT_TYPE = "report_type"
        const val INTENT_EXTRA_KEY_USER_ID = "user_id"
        const val INTENT_EXTRA_KEY_STREAM_KEY = "stream_key"
        const val INTENT_EXTRA_KEY_VIDEO_TYPE = "video_type"
    }

    var mSpinnerData = arrayOf("음란성", "폭력성", "욕설/비방", "게시물 도배", "개인정보 유출", "기타")

    private var title: String? = ""
    private var content: String? = ""
    private var reportReason: String? = ""
    private var imageFileName: String? = ""
    private var imgFilePath: String = ""
    private var fileItem: FileItem? = null
    private var videoType: String? = null
    private var streamKey: String? = null
    private var reportType = false
    private var userId:String? = null

    private lateinit var mAdapter: ImageFileAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_broadcast_report)

        EventBus.getDefault().register(this)

        buttonBack.setOnClickListener(clickListener)
        buttonFileSelect.setOnClickListener(clickListener)
        buttonCancel.setOnClickListener(clickListener)
        buttonConfirm.setOnClickListener(clickListener)
        buttonComplete.setOnClickListener(clickListener)

        recyclerViewFile.setHasFixedSize(false)
        recyclerViewFile.isNestedScrollingEnabled = false
        mAdapter = ImageFileAdapter()
        mAdapter.setListener(this@BroadcastReportActivity)
        recyclerViewFile.adapter = mAdapter

        setReportReasonSpinner()

        checkIntent(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQ_CODE_SELECT_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    textViewNoFile.visibility = View.GONE

                    imageFileName = getImageNameToUri(data?.data!!)
                    val image_bitmap = Images.Media.getBitmap(contentResolver, data.data)
                    Logger.d("################### imageFileName : ${imageFileName}, imageFilePath : $imgFilePath")

                    fileItem = FileItem(imageFileName, imgFilePath)
                    mAdapter.addItem(fileItem!!)

                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onFileDelete(count: Int) {
        if (count == 0) {
            textViewNoFile.visibility = View.VISIBLE
        } else {
            textViewNoFile.visibility = View.GONE
        }
    }

    @Subscribe()
    fun onMessageEvent(data: NetworkBusResponse) {
        var key = NetworkHandler.getInstance(this@BroadcastReportActivity)
                .getKey(NetworkApi.API7.toString(), AppPreferences.getUserId(this@BroadcastReportActivity)!!, "")
        if (data.arg1.startsWith(key)) {
            if ("ok" == data.arg2) {
                editTextTitle.setText("")
                editTextContent.setText("")
                spinnerReportReason.prompt = getString(R.string.msg_broadcast_report_reason_title)
                textViewNoFile.visibility = View.VISIBLE

                mAdapter.clearItem()

                scrollView.visibility = View.GONE
                layoutComplete.visibility = View.VISIBLE
            } else {
                AppToast(this@BroadcastReportActivity).showToastMessage("방송 신고 중에 오류가 발생했습니다.",
                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                        AppToast.GRAVITY_BOTTOM)
            }
        }
    }

    private fun checkIntent(intent: Intent) {
        if (intent != null) {
            reportType = intent.getBooleanExtra(INTENT_EXTRA_KEY_REPORT_TYPE, reportType)
            userId = intent.getStringExtra(INTENT_EXTRA_KEY_USER_ID)
            videoType = intent.getStringExtra(INTENT_EXTRA_KEY_VIDEO_TYPE)
            streamKey = intent.getStringExtra(INTENT_EXTRA_KEY_STREAM_KEY)

            textViewNickName.text = userId
        }
    }

    private fun getImageNameToUri(data: Uri): String {
        imgFilePath = getRealPathFromURI(data)

        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = managedQuery(data, proj, null, null, null)
        val column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)

        cursor.moveToFirst()

        val imgPath = cursor.getString(column_index)
        val imgName = imgPath.substring(imgPath.lastIndexOf("/") + 1)

        return imgName
    }

    private fun getImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE)
        intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQ_CODE_SELECT_IMAGE)
    }

    private fun setReportReasonSpinner() {
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mSpinnerData)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerReportReason.prompt = getString(R.string.msg_broadcast_report_reason_title)
        spinnerReportReason.adapter = NothingSelectedSpinnerAdapter(adapter, R.layout.spinner_broadcast_report_reason, this@BroadcastReportActivity)
        spinnerReportReason.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                reasonType.visibility = View.GONE

                if (position != 0) {
                    reportReason = parent?.getItemAtPosition(position).toString()
                    Logger.d("REPORT_REASON: $reportReason")
                }
            }
        }
    }

    /**
     * 사진이 저장된 절대경로를 가져온다.
     */
    private fun getRealPathFromURI(contentUri: Uri): String {
        var columnIndex = 0
        var path = ""
        val proj = arrayOf(MediaStore.Images.Media.DATA)

        val cursor = contentResolver.query(contentUri, proj, null, null, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                path = cursor.getString(columnIndex)
            }
        }

        if (cursor != null) {
            cursor.close()
        }

        return path
    }

    private fun broadCastReport(type: Boolean) {
        if (reportReason.isNullOrEmpty()) {
            AppToast(this@BroadcastReportActivity).showToastMessage("신고 항목을 넣어 주세요.",
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM)
            return
        }

        title = editTextTitle.text.toString()
        if (title.isNullOrEmpty()) {
            AppToast(this@BroadcastReportActivity).showToastMessage("제목을 넣어 주세요.",
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM)
            return
        }

        content = editTextContent.text.toString()
        if (content.isNullOrEmpty()) {
            AppToast(this@BroadcastReportActivity).showToastMessage("내용을 넣어 주세요.",
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM)
            return
        }

        var builder = MultipartBody.Builder()
        builder.setType(MultipartBody.FORM)
        builder.addFormDataPart("strToUserId", userId)
        builder.addFormDataPart("strReason", reportReason!!)
        builder.addFormDataPart("strTitle", title!!)
        builder.addFormDataPart("strContent", content!!)
        builder.addFormDataPart("strMediaType", if (!type) videoType else "USER")
        builder.addFormDataPart("strStreamKey", if (!type) streamKey else "USER")

        for (item in mAdapter.getItem()) {
            var f = File(item.imgPath)
            var encodedFileName = URLEncoder.encode(f.name, "UTF-8")
            builder.addFormDataPart("strFileName[]", encodedFileName, RequestBody.create(MediaType.parse("multipart/form-data"), f))
        }

        EventBus.getDefault().post(NetworkBus(NetworkApi.API7.name, builder.build()))
    }

    private val clickListener: View.OnClickListener = View.OnClickListener {
        when (it?.id) {
            R.id.buttonBack, R.id.buttonCancel -> {
                AppAlertDialog(this@BroadcastReportActivity).apply {
                    setTitle("방송 신고 중지")
                    setMessage("방송 신고를 중지하시겠습니까?")
                    setLeftButton("취소", View.OnClickListener { dismiss() })
                    setRightButton("확인", View.OnClickListener { finish() })
                }.show()
            }

            R.id.buttonComplete -> finish()

            R.id.buttonFileSelect -> {
                if (mAdapter != null && mAdapter.itemCount < 3) {
                    getImageFromGallery()
                } else {
                    AppToast(this@BroadcastReportActivity).showToastMessage("파일 첨부는 3까지만 허용됩니다.",
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_BOTTOM)
                }
            }

            R.id.buttonConfirm -> broadCastReport(reportType)
        }
    }

    data class FileItem(var imageName: String?, var imgPath: String?)
}