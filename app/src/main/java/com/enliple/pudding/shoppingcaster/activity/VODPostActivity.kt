package com.enliple.pudding.shoppingcaster.activity

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import com.couchbase.lite.Blob
import com.couchbase.lite.MutableDocument
import com.enliple.pudding.AbsBaseActivity
import com.enliple.pudding.R
import com.enliple.pudding.activity.MainActivity
import com.enliple.pudding.activity.MyVODActivity
import com.enliple.pudding.commons.app.NetworkStatusUtils
import com.enliple.pudding.commons.data.CategoryItem
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.db.VodDBManager
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.*
import com.enliple.pudding.commons.network.vo.API86
import com.enliple.pudding.commons.widget.toast.AppToast
import com.google.gson.Gson
import com.ksyun.media.streamer.kit.StreamerConstants
import kotlinx.android.synthetic.main.activity_vod_post.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Kim Joonsung on 2018-11-01.
 */
class VODPostActivity : AbsBaseActivity() {
    companion object {
        private const val TAG = "VODPostActivity"

        const val INTENT_EXTRA_KEY_VIDEO_URI = "videoUri"
        const val INTENT_EXTRA_KEY_VIDEO_FILE_PATH = "videoFilePath"
        const val INTENT_EXTRA_KEY_VOD_TEMP_ID = "vod_temp_id"
        const val INTENT_EXTRA_KEY_FROM_VOD_TEMP = "from_vod_temp"
        const val INTENT_EXTRA_KEY_FROM_GALLERY = "from_gallery"

        private const val LANDSCAPE = "0"
        private const val TEMP_VOD_ID = 1
    }

    private var uploadHost: String = ""
    private var videoUri: String? = null
    private var thumbnails: Bitmap? = null
    private var vodResponse: API86? = null
    private var vodTempId: String = ""
    private var fromVodTemp: Boolean = false
    private var broadCastInfo: String = ""
    private var uploadUri: Uri? = null
    private var videoPath: String = ""
    private var videoRotation: String = ""
    private var fromGallery: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_vod_post)

        mActivityList.add(this)

        EventBus.getDefault().register(this)

        buttonClose.setOnClickListener(clickListener)
        buttonSaveTemp.setOnClickListener(clickListener)
        buttonPost.setOnClickListener(clickListener)
        buttonOpenSwitch.setOnClickListener(clickListener)
        buttonShareSwitch.setOnClickListener(clickListener)
        buttonReplySwitch.setOnClickListener(clickListener)
        textViewPreView.setOnClickListener(clickListener)

        checkIntent(intent)
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        mActivityList.remove(this)
        EventBus.getDefault().unregister(this)
    }

    override fun onDozeModeStateChanged(dozeEnable: Boolean) {}
    override fun onNetworkStatusChanged(status: NetworkStatusUtils.NetworkStatus) {}

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: NetworkBusResponse) {
        Logger.e("onMessageEvent:  ${data.arg1}  ${data.arg2}  ${data.arg3}")

        var api86 = NetworkHandler.getInstance(this@VODPostActivity).getKey(NetworkApi.API86.toString(),
                AppPreferences.getUserId(this@VODPostActivity)!!, "VOD")

        when (data.arg1) {
            api86 -> handleUploadFileInformation(data)
            "ok" -> handleUploadFile(data)
        }
    }

    private fun checkIntent(intent: Intent) {
        if (intent != null) {
            fromVodTemp = intent.getBooleanExtra(INTENT_EXTRA_KEY_FROM_VOD_TEMP, fromVodTemp)
            fromGallery = intent.getBooleanExtra(INTENT_EXTRA_KEY_FROM_GALLERY, fromGallery)
            if (!fromVodTemp) {
                videoUri = intent.getStringExtra(INTENT_EXTRA_KEY_VIDEO_URI)
                uploadUri = Uri.parse(videoUri)
                videoPath = intent.getStringExtra(INTENT_EXTRA_KEY_VIDEO_FILE_PATH)
                if (uploadUri == null) {
                    uploadUri = Uri.fromFile(File(videoPath))
                }

                Logger.e("checkIntent uri: $videoUri  path:$videoPath")

                thumbnails = ThumbnailUtils.createVideoThumbnail(videoPath, MediaStore.Video.Thumbnails.MINI_KIND)
                imageViewSelected.setImageBitmap(thumbnails)

                if (fromGallery) {
                    videoRotation = getVideoRotation(videoPath)
                } else {
                    videoRotation = "v"
                }
            } else {
                vodTempId = intent.getStringExtra(INTENT_EXTRA_KEY_VOD_TEMP_ID)
                getVodTempId()
            }
        }
    }

    /**
     * 미디버스로 VOD 비디오 업로드
     */
    private fun uploadVideo(response: API86) {
        var vodFile = File(uploadUri!!.path)
        var requestFile = RequestBody.create(MediaType.parse("video/*"), vodFile)
        var part = MultipartBody.Part.createFormData("file", "${response.FileName}.mp4", requestFile)
        val bus = NetworkMidiBus(NetworkApi.API71.name, response.XMbusToken, response.XMbusChannel, response.UploadPath, uploadHost, part)
        EventBus.getDefault().post(bus)
    }

    private fun handleUploadFileInformation(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            var str = DBManager.getInstance(this@VODPostActivity).get(data.arg1)
            var response: API86 = Gson().fromJson(str, API86::class.java)

            Logger.e(TAG, "Create Broadcast VOD response : $response")

            var baseHost = if (response.UploadHost.lastIndexOf("/") == response.UploadHost.length - 1) {
                response.UploadHost.substring(0, response.UploadHost.length - 1)
            } else {
                response.UploadHost
            }

            uploadHost = baseHost
            vodResponse = response

            uploadVideo(response)
        } else {
            progressBar.visibility = View.GONE
            Logger.e("error: ${data.arg1} ${data.arg2} ${data.arg3}")

            AppToast(this@VODPostActivity).showToastMessage("방송 정보 등록이 실패 하였습니다. 잠시 후 다시 시도해 주세요",
                    AppToast.DURATION_MILLISECONDS_LONG,
                    AppToast.GRAVITY_MIDDLE)
        }
    }

    private fun handleUploadFile(data: NetworkBusResponse) {
        Logger.e("handleUploadFile: ${data.arg1} ${data.arg2} ${data.arg3}")

        if ("ok" == data.arg1) {
            AppToast(this@VODPostActivity).showToastMessage(getString(R.string.msg_vod_upload_complete),
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM)

            if(fromVodTemp) {
                VodDBManager.getInstance(this).getDoc(vodTempId).let {
                    VodDBManager.getInstance(this).delete(it)
                }
            }

            EventBus.getDefault().unregister(this)

            startActivity(Intent(this, MainActivity::class.java))   // 메인으로 이동
            activityFinish()    // MainActivity로 이동하면 스택에 쌓여 있는 모든 Activity를 종료한다.
        } else {
            progressBar.visibility = View.GONE
            Logger.e("error: ${data.arg1} ${data.arg2} ${data.arg3}")

            AppToast(this@VODPostActivity).showToastMessage("파일 업로드에 실패 하였습니다. 잠시 후 다시 시도해 주세요",
                    AppToast.DURATION_MILLISECONDS_LONG,
                    AppToast.GRAVITY_MIDDLE)
        }
    }

    /**
     * VOD 방송정보 등록하기
     */
    private fun registerBroadCastVODInfo() {
        var info = getBroadcastInfo(fromVodTemp)
        if (info == null) {
            AppToast(this@VODPostActivity).showToastMessage("방송정보가 잘못 되었습니다.",
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM)
        } else {
            var coverImg = File(Uri.parse(info.image).path)

            var requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), coverImg)
            var fileBody = MultipartBody.Part.createFormData("strThumbnail", coverImg.name, requestFile)
            var body = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("strTitle", info.subject)
                    .addFormDataPart("strCategory", info.categoryCode)
                    .addFormDataPart("strTag", info.tag)
                    .addFormDataPart("strNoti", info.registration)
                    .addFormDataPart("strMulti", info.strMulti)
                    .addFormDataPart("strAge", info.strAge)
                    .addFormDataPart("strSex", info.strSex)
                    .addFormDataPart("user_show_YN", if (buttonOpenSwitch.isSelected) "Y" else "N")
                    .addFormDataPart("share_YN", if (buttonShareSwitch.isSelected) "Y" else "N")
                    .addFormDataPart("comment_YN", if (buttonReplySwitch.isSelected) "Y" else "N")
                    .addFormDataPart("strContentSize", videoRotation)
                    .addFormDataPart("strContentRate", getVideoResolution(uploadUri!!.path))
                    .addPart(fileBody)

            if (info.productItems != null && info.productItems.size > 0) {
                for (i in info.productItems.indices) {
                    var key = "strItems${i + 1}"
                    body.addFormDataPart(key, info.productItems[i])
                }
            }

            EventBus.getDefault().post(NetworkBus(NetworkApi.API86.name, "VOD", body.build()))

            progressBar.visibility = View.VISIBLE
        }
    }

    /**
     * 방송 필수 정보를 가져온다.
     */
    private fun getBroadcastInfo(fromTemp: Boolean): com.enliple.pudding.shoppingcaster.data.BroadcastInfo? {
        var info = ""
        if (!fromTemp) {
            info = AppPreferences.getBroadcastInfo(this@VODPostActivity)
        } else {
            info = broadCastInfo
        }
        Logger.e("getBroadcastInfo :: $info")
        try {
            var infoModel = com.enliple.pudding.shoppingcaster.data.BroadcastInfo()
            var obj = JSONObject(info)
            infoModel.subject = obj.optString(com.enliple.pudding.shoppingcaster.data.BroadcastInfo.KEY_SUBJECT)
            infoModel.registration = obj.optString(com.enliple.pudding.shoppingcaster.data.BroadcastInfo.KEY_REGISTRATION)
            infoModel.tag = obj.optString(com.enliple.pudding.shoppingcaster.data.BroadcastInfo.KEY_TAG)
            infoModel.categoryCode = obj.optString(com.enliple.pudding.shoppingcaster.data.BroadcastInfo.KEY_CATEGORY_CODE)
            infoModel.image = obj.optString(com.enliple.pudding.shoppingcaster.data.BroadcastInfo.KEY_IMAGE)
            infoModel.type = obj.optString(com.enliple.pudding.shoppingcaster.data.BroadcastInfo.KEY_TYPE)
            infoModel.strMulti = obj.optString(com.enliple.pudding.shoppingcaster.data.BroadcastInfo.KEY_MULTI)
            infoModel.strSex = obj.optString(com.enliple.pudding.shoppingcaster.data.BroadcastInfo.KEY_SEX)
            infoModel.strAge = obj.optString(com.enliple.pudding.shoppingcaster.data.BroadcastInfo.KEY_AGE)

            var productArray = ArrayList<String>()
            var jsonArray = obj.optJSONArray(com.enliple.pudding.shoppingcaster.data.BroadcastInfo.KEY_PRODUCT_ITEMS)
            if (jsonArray != null && jsonArray.length() > 0) {
                for (i in 0 until jsonArray.length()) {
                    var subObject = jsonArray.get(i)
                    var item = subObject.toString()
                    productArray.add(item)
                }
            }

            var firstCategoryObj = obj.optJSONObject(com.enliple.pudding.shoppingcaster.data.BroadcastInfo.KEY_FIRST_CATEGORY)
            var firstCategoryItem: CategoryItem? = null
            if (firstCategoryObj != null) {
                var categoryId = firstCategoryObj.optString(com.enliple.pudding.shoppingcaster.data.BroadcastInfo.KEY_CATEGORY_ID)
                var categoryName = firstCategoryObj.optString(com.enliple.pudding.shoppingcaster.data.BroadcastInfo.KEY_CATEGORY_NAME)
                var categoryImage = firstCategoryObj.optString(com.enliple.pudding.shoppingcaster.data.BroadcastInfo.KEY_CATEGORY_IMAGE)
                firstCategoryItem = CategoryItem(0L, categoryId, categoryName, "", "", categoryImage)
            }

            var secondCategoryObj = obj.optJSONObject(com.enliple.pudding.shoppingcaster.data.BroadcastInfo.KEY_SECOND_CATEGORY)
            var secondCategoryItem: CategoryItem? = null
            if (secondCategoryObj != null) {
                var categoryId = secondCategoryObj.optString(com.enliple.pudding.shoppingcaster.data.BroadcastInfo.KEY_CATEGORY_ID)
                var categoryName = secondCategoryObj.optString(com.enliple.pudding.shoppingcaster.data.BroadcastInfo.KEY_CATEGORY_NAME)
                var categoryImage = secondCategoryObj.optString(com.enliple.pudding.shoppingcaster.data.BroadcastInfo.KEY_CATEGORY_IMAGE)
                secondCategoryItem = CategoryItem(0L, categoryId, categoryName, "", "", categoryImage)
            }

            var thirdCategoryArray = ArrayList<CategoryItem>()
            var thirdCategoryJsonArray = obj.optJSONArray(com.enliple.pudding.shoppingcaster.data.BroadcastInfo.KEY_THIRD_CATEGORY)
            if (thirdCategoryJsonArray != null && thirdCategoryJsonArray.length() > 0) {
                for (i in 0 until thirdCategoryJsonArray.length()) {
                    var subObject: JSONObject = thirdCategoryJsonArray.getJSONObject(i)
                    var categoryId = subObject.optString(com.enliple.pudding.shoppingcaster.data.BroadcastInfo.KEY_CATEGORY_ID)
                    var categoryName = subObject.optString(com.enliple.pudding.shoppingcaster.data.BroadcastInfo.KEY_CATEGORY_NAME)
                    var categoryImage = subObject.optString(com.enliple.pudding.shoppingcaster.data.BroadcastInfo.KEY_CATEGORY_IMAGE)
                    var categoryItem = CategoryItem(0L, categoryId, categoryName, "", "", categoryImage)
                    thirdCategoryArray.add(categoryItem)
                }
            }
            infoModel.thirdCategory = thirdCategoryArray
            infoModel.secondCategory = secondCategoryItem
            infoModel.firstCategory = firstCategoryItem
            infoModel.productItems = productArray
            return infoModel
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * 임시 보관함에 VOD 를 저장한다.
     */
    private fun saveTempVOD() {
        val broadcastInfo = AppPreferences.getBroadcastInfo(this@VODPostActivity)
        val thumb = getBitmapAsByteArray(thumbnails)

        var nextVodId = TEMP_VOD_ID
        var tempID = -1
        VodDBManager.getInstance(this).loadAllExt().forEach {
            tempID = it.getDictionary(VodDBManager.KEY).getString("id").toInt()
            if (nextVodId <= tempID) {
                nextVodId = tempID + 1
            }
        }

        if (tempID == -1) {
            nextVodId = TEMP_VOD_ID
        }

        Logger.e("tempVodID:$nextVodId")

        val document = MutableDocument(nextVodId.toString())
        document.setString("id", nextVodId.toString())
        document.setString("videoUrl", uploadUri!!.toString())
        document.setString("broadCastInfo", broadcastInfo)
        document.setString("reg_date", SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date(System.currentTimeMillis())))
        document.setBlob("thumbnailImage", Blob("image/jpeg", thumb))
        VodDBManager.getInstance(this@VODPostActivity).put(document)
    }

    /**
     * 임시 보관함에 해당 ID의 DB를 가져온다.
     */
    private fun getVodTempId() {
        VodDBManager.getInstance(this).loadAllExt().forEach {
            val id = it.getDictionary(VodDBManager.KEY).getString("id")
            if (vodTempId == id) {
                val image = it.getDictionary(VodDBManager.KEY).getBlob("thumbnailImage")
                val video = it.getDictionary(VodDBManager.KEY).getString("videoUrl")
                val thumb = image.content
                broadCastInfo = it.getDictionary(VodDBManager.KEY).getString("broadCastInfo")

                uploadUri = Uri.parse(video)
                imageViewSelected.setImageBitmap(getThumbnailImage(thumb))
                videoRotation = getVideoRotation(uploadUri!!.path)
                return
            }
        }
    }

    private fun getBitmapAsByteArray(bitmap: Bitmap?): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap!!.compress(CompressFormat.PNG, 0, outputStream)
        return outputStream.toByteArray()
    }

    private fun getThumbnailImage(images: ByteArray?): Bitmap {
        return BitmapFactory.decodeByteArray(images, 0, images!!.size)
    }

    /**
     * 업로드 할 영상의 orientation 값을 가져온다.
     */
    private fun getVideoRotation(path: String): String {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(path)
        val rotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
        Logger.e("######## ROTATION : $rotation") // 0 : landscape, 90 : portrait
        retriever.release()
        if (rotation == LANDSCAPE) return "h" else return "v"
    }

    /**
     * 업로드 할 영상의 해상도 값을 가져온다.
     */
    private fun getVideoResolution(path: String): String {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(path)
        val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT).toInt()
        val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH).toInt()
        retriever.release()
        return if (height > width) width.toString() else height.toString()
    }

    private val clickListener = View.OnClickListener {
        when (it?.id) {
            R.id.buttonClose -> onBackPressed()

            R.id.buttonSaveTemp -> {
                if (!fromVodTemp) {
                    saveTempVOD()
                    startActivity(Intent(it.context, MyVODActivity::class.java))

                    finish()
                } else {
                    AppToast(it.context).showToastMessage("이미 임시저장된 영상입니다.",
                            AppToast.DURATION_MILLISECONDS_DEFAULT, AppToast.GRAVITY_BOTTOM)
                }
            }

            R.id.buttonPost -> registerBroadCastVODInfo()

            R.id.buttonOpenSwitch -> {
                it.isSelected = !it.isSelected
            }

            R.id.buttonShareSwitch -> {
                it.isSelected = !it.isSelected
            }

            R.id.buttonReplySwitch -> {
                it.isSelected = !it.isSelected
            }

            R.id.textViewPreView -> {
                startActivity(Intent(this@VODPostActivity, VODPreviewActivity::class.java).apply {
                    if (!fromVodTemp) {
                        putExtra(VODPreviewActivity.INTENT_KEY_VIDEO_URI, videoUri.toString())
                    } else {
                        putExtra(VODPreviewActivity.INTENT_KEY_VIDEO_URI, uploadUri.toString())
                    }
                })
            }
        }
    }
}