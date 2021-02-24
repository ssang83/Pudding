package com.enliple.pudding.widget

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import com.enliple.pudding.AbsBaseDialog
import com.enliple.pudding.R
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.ProgressRequestBody
import kotlinx.android.synthetic.main.dialog_vod_upload.*
import java.io.File

/**
 * Midibus VOD 파일 전송 Progress Dialog (With Retrofit2 - RXJava2)
 * @author hkcha
 * @since 2018.08.06
 */
class VODUploadDialog : AbsBaseDialog, ProgressRequestBody.ProgressCallback {
    companion object {
        private const val TAG = "VODUploadDialog"
        private const val PERCENT_FORMAT = "%d %s"
    }

    private val user: String
    private val uploadFile: File
    //    private val vodUploadPrepare: VODService
    private var mListener: ResultListener? = null

    constructor(context: Context, user: String, uploadLocalFilePath: File, listener: ResultListener?)
            : super(context, false, null) {
        this.user = user
        this.uploadFile = uploadLocalFilePath
        mListener = listener

//        vodUploadPrepare = RetrofitNetworkProvider.createRetrofitInstance(context,
//                AppPreferences.getServerAddress(context),
//                VODService::class.java)
    }

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_vod_upload)
        prepareUpload()
    }

    override fun onBackPressed() {
        // Not Allowed Back button
    }

    override fun onProgress(progress: Long, total: Long) {
        var currentProgress = (100 * progress / total).toInt()
        progressBar.post { progressBar.progress = currentProgress }
        textViewProgress.post { textViewProgress.text = String.format(PERCENT_FORMAT, currentProgress, "%") }
        Logger.d(TAG, "progress : $currentProgress")
    }

    /**
     * 비디오 파일 업로드 준비 및 준비 완료시 업로드 요청 호출
     */
    private fun prepareUpload() {
        progressBar.isIndeterminate = true
        textViewProgress.text = "업로드를 준비중입니다."

/*        vodUploadPrepare.getXMBusToken(AppPreferences.getJWT(context), user)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe( { response ->
                        var baseHost = if (response.uploadHost.lastIndexOf("/") == response.uploadHost.length-1) {
                            response.uploadHost.substring(0, response.uploadHost.length - 1)
                        } else {
                            response.uploadHost
                        }

                        var uploadService = RetrofitNetworkProvider
                                .createRetrofitMultipartInstance(context, baseHost, VODService::class.java)

                        Logger.e(TAG, "UploadFile : ${uploadFile.absolutePath}, Length : ${uploadFile.length()}")
                        Handler().postDelayed({uploadVideo(response, uploadService)}, 1000L)
                    },
                    { error ->
                        Logger.e(TAG, "요청중 오류가 발생하였습니다.\n($error)")
                        progressBar.visibility = View.GONE
                        textViewProgress.text = "요청중 오류가 발생하였습니다.\n($error)"
                        mListener?.onVodUploadFailed(uploadFile.absolutePath)

                        Handler().postDelayed({dismiss()}, 3000L)
                    })*/
    }

    /**
     * 미디버스로 비디오 업로드
     */
//    private fun uploadVideo(response: VODResponse.VODUploadInfoResponse, service:VODService) {
//        progressBar.isIndeterminate = false
//        progressBar.progress = 0
//        textViewProgress.text = String.format(PERCENT_FORMAT, 0,"%")
//
//        var requestBody = RetrofitNetworkProvider
//                .createProgressRequestBody(context, Uri.fromFile(uploadFile),
//                        MediaType.parse("video/*")!!, this@VODUploadDialog)
//
//        var part = RetrofitNetworkProvider
//                .createMultipartBody("file","${response.fileName}.mp4", requestBody)
//
//        service.uploadVOD(response.xMBusToken, response.xMBusChannel, response.uploadPath, part)
//                .enqueue(object: Callback<VODResponse.XMBusUploadResponse> {
//                    override fun onFailure(call: Call<VODResponse.XMBusUploadResponse>?, t: Throwable?) {
//                        Logger.e(TAG, "Upload Response Failure : $t")
//                        Handler().postDelayed({
//                            dismiss()
//                            mListener?.onVodUploadFailed(uploadFile.absolutePath)
//                        }, 3000L)
//                    }
//
//                    override fun onResponse(call: Call<VODResponse.XMBusUploadResponse>?, response: Response<VODResponse.XMBusUploadResponse>?) {
//                        Logger.e(TAG, "Upload Response : $response")
//
//                        val response = response ?: return
//                        val body = response?.body() ?: return
//
//                        Logger.e(TAG, "Upload Result : success : ${body.isSuccess}, mediaId : ${body.mediaId}")
//
//                        Handler().postDelayed({
//                            dismiss()
//                            mListener?.onVodUploadSuccessfully(uploadFile.absolutePath)
//                        }, 1000L)
//                    }
//                })
//    }

    interface ResultListener {
        fun onVodUploadFailed(uploadedAbsolutePath: String)
        fun onVodUploadSuccessfully(uploadedAbsolutePath: String)
    }
}