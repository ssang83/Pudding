package com.enliple.pudding.activity

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.telephony.PhoneNumberUtils
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import androidx.core.content.FileProvider
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.enliple.pudding.AbsBaseActivity
import com.enliple.pudding.PuddingApplication
import com.enliple.pudding.R
import com.enliple.pudding.api.AccountPolicy
import com.enliple.pudding.bus.SoftKeyboardBus
import com.enliple.pudding.commons.app.ImageLoad
import com.enliple.pudding.commons.app.NetworkStatusUtils
import com.enliple.pudding.commons.app.SoftKeyboardUtils
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.NetworkApi
import com.enliple.pudding.commons.network.NetworkBus
import com.enliple.pudding.commons.network.NetworkBusResponse
import com.enliple.pudding.commons.network.NetworkHandler
import com.enliple.pudding.commons.network.vo.API21
import com.enliple.pudding.commons.network.vo.API75
import com.enliple.pudding.commons.network.vo.BaseAPI
import com.enliple.pudding.commons.ui_compat.AsteriskPasswordTransformationMethod
import com.enliple.pudding.commons.widget.toast.AppToast
import com.enliple.pudding.keyboard.KeyboardHeightProvider
import com.enliple.pudding.shoppingcaster.activity.CoverSelectActivity
import com.enliple.pudding.widget.shoptree.AgeSelectDialog
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_profile_edit.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*

/**
 * 프로필 수정 Activity
 * @author hkcha
 * @since 2018.08.30
 */
class ProfileEditActivity : AbsBaseActivity() {

    companion object {
        private const val TAG = "ProfileEditActivity"

        private const val REQUEST_CAMERA_CODE = 1111
        private const val REQUEST_GALLERY_CODE = 1112
        private const val REQUEST_CODE_PROFILE_IMAGE_PICK = 0xBC01
        private const val REQUEST_CODE_BG_IMAGE_PICK = 0xBC02

        private const val PERMISSION_REQUEST_CAMERA = 1
        private const val REQUEST_CODE_CELLPHONE_IDENTIFICATION = 3
        private const val RETRY_CELLPHONE_VALIDATION_MAXIMUM_COUNT = 3

        private const val DRAWABLE_RES_INPUT_NORMAL = R.drawable.input_form_normal
        private const val DRAWABLE_RES_INPUT_ERROR = R.drawable.input_form_error
    }

    private var photoUri: Uri? = null
    private var currentPhotoPaath: String = ""
    private var mImageCaptureName: String = ""
    private var photoFile: File? = null
    private var isPhotoType: Boolean = false
    private var coverImage: Uri? = null
    private var bgImage: Uri? = null
    private var retryCellPhoneValidationCount = 0

    private var file: File? = null
    private var BGFile: File? = null
    private var name: String = ""
    private var phoneNumber: String = ""
    private lateinit var mKeyboardHeightProvider: KeyboardHeightProvider

    private val mAgeList = ArrayList<String>().apply {
        for (i in 14..70)
            add(i.toString())
    }

    private val newPasswdInputChangeListener = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if(s!!.length > 0) {
                if(!AccountPolicy.IsPasswordMatched(s.toString())) {
                    tvPasswordError.visibility = View.VISIBLE
                    etNewPasswd.setBackgroundResource(DRAWABLE_RES_INPUT_ERROR)
                } else {
                    tvPasswordError.visibility = View.GONE
                    etNewPasswd.setBackgroundResource(DRAWABLE_RES_INPUT_NORMAL)
                }
            } else {
                tvPasswordError.visibility = View.GONE
                etNewPasswd.setBackgroundResource(DRAWABLE_RES_INPUT_NORMAL)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_edit)
        EventBus.getDefault().register(this)

        etPreviousPasswd.transformationMethod = AsteriskPasswordTransformationMethod()
        etNewPasswd.transformationMethod = AsteriskPasswordTransformationMethod()
        etNewPasswd.addTextChangedListener(newPasswdInputChangeListener)
        etNewPasswdConfirm.transformationMethod = AsteriskPasswordTransformationMethod()

        buttonBack.setOnClickListener(clickListener)
        buttonConfirm.setOnClickListener(clickListener)
        buttonProfile.setOnClickListener(clickListener)
        textAge.setOnClickListener(clickListener)

        emptyTouch.setOnTouchListener(object:View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                if(event?.action == MotionEvent.ACTION_DOWN)
                    when(true) {
                        etEmail.isFocused -> SoftKeyboardUtils.hideKeyboard(etEmail)
                        etNickName.isFocused -> SoftKeyboardUtils.hideKeyboard(etNickName)
                        etIntroduce.isFocused -> SoftKeyboardUtils.hideKeyboard(etIntroduce)
                        etPreviousPasswd.isFocused -> SoftKeyboardUtils.hideKeyboard(etPreviousPasswd)
                        etNewPasswd.isFocused -> SoftKeyboardUtils.hideKeyboard(etNewPasswd)
                        etNewPasswdConfirm.isFocused -> SoftKeyboardUtils.hideKeyboard(etNewPasswdConfirm)
                    }

                return false
            }
        })

        var bus = NetworkBus(NetworkApi.API21.name, AppPreferences.getUserId(this@ProfileEditActivity)!!)
        EventBus.getDefault().post(bus)

        // 소프트 키보드가 변경 될 경우 Bus 로 data 가 온다.
        mKeyboardHeightProvider = KeyboardHeightProvider(this)
        Handler().postDelayed(Runnable { mKeyboardHeightProvider.start() }, 1000)

        rbMan.setOnCheckedChangeListener{ _, isChecked -> if(isChecked) rbWoman.isChecked = false }
        rbWoman.setOnCheckedChangeListener{ _, isChecked -> if(isChecked) rbMan.isChecked = false }
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)

        if(mKeyboardHeightProvider != null) {
            mKeyboardHeightProvider.close()
        }
    }

    override fun onNetworkStatusChanged(status: NetworkStatusUtils.NetworkStatus) {}
    override fun onDozeModeStateChanged(dozeEnable: Boolean) {}

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectPhotoFromCamera()
            } else {
                Logger.e(TAG, "No CAMERA or AudioRecord permission")
                AppToast(this@ProfileEditActivity).showToastMessage(getString(R.string.msg_my_setting_profile_camera_perm_check),
                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                        AppToast.GRAVITY_MIDDLE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_GALLERY_CODE -> sendPicture(intent!!.data)  // 캘러리에서 가져오기
                REQUEST_CAMERA_CODE -> getPictureForPhoto()     // 카메라에서 가져오기
                REQUEST_CODE_PROFILE_IMAGE_PICK -> {
                    var myDir = File("${cacheDir}/temp_image")

                    var fname = "cropTemp.jpg"

                    var fl = File(myDir, fname)
                    coverImage = Uri.parse(fl.path)

                    file = fl
//                    coverImage = intent?.data
//                    file = File(coverImage?.path)
                    Logger.e("file.path :: " + fl.path)
                    val bitmap = BitmapFactory.decodeFile(fl.path)
//                    imageViewProfile.setImageBitmap(bitmap)
                    ImageLoad.setImage(this@ProfileEditActivity, imageViewProfile, fl.path, null, ImageLoad.SCALE_CIRCLE_CROP, DiskCacheStrategy.NONE)
                    ImageLoad.setImage(this@ProfileEditActivity, imageViewProfileBG, fl.path, null, ImageLoad.SCALE_CENTER_CROP, DiskCacheStrategy.NONE)
//                    Glide.with(this@ProfileEditActivity)
//                            .load(coverImage)
//                            .bitmapTransform(CropCircleTransformation(this@ProfileEditActivity))
//                            .priority(Priority.HIGH)
//                            .diskCacheStrategy(DiskCacheStrategy.NONE)
//                            .into(imageViewProfile)
//                    ImageLoad.setImage(this@ProfileEditActivity, imageViewProfile, coverImage, null, ImageLoad.SCALE_CIRCLE_CROP, DiskCacheStrategy.ALL)
                }

                REQUEST_CODE_BG_IMAGE_PICK -> {
                    bgImage = intent?.data
                    BGFile = File(bgImage?.path)

//                    Glide.with(this@ProfileEditActivity)
//                            .load(bgImage)
//                            .asBitmap()
//                            .priority(Priority.HIGH)
//                            .diskCacheStrategy(DiskCacheStrategy.ALL)
//                            .into(imageViewProfileBG)
                    ImageLoad.setImage(this@ProfileEditActivity, imageViewProfileBG, bgImage, null, ImageLoad.SCALE_NONE, DiskCacheStrategy.ALL)
                }

                REQUEST_CODE_CELLPHONE_IDENTIFICATION -> {
                    if (intent != null) {
                        handleAlReadyRegisterByCellPhoneUser(intent.getStringExtra("mb_name"), intent.getStringExtra("phone_no"))
                    } else {
                        // 앱 내부 간에 직접넘겨줘서 이럴일은 없겠으나.. 재시도를 할 수 있게 사용자에게 여지를 남겨둠
                    }
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, intent)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: NetworkBusResponse) {
        var API21 = NetworkHandler.getInstance(this@ProfileEditActivity)
                .getKey(NetworkApi.API21.toString(), AppPreferences.getUserId(this@ProfileEditActivity)!!, "")

        var API22 = NetworkHandler.getInstance(this@ProfileEditActivity)
                .getKey(NetworkApi.API22.toString(), AppPreferences.getUserId(this@ProfileEditActivity)!!, "")

        var API25 = NetworkHandler.getInstance(this@ProfileEditActivity)
                .getKey(NetworkApi.API25.toString(), AppPreferences.getUserId(this@ProfileEditActivity)!!, "")

        var API75 = NetworkHandler.getInstance(this@ProfileEditActivity)
                .getKey(NetworkApi.API75.toString(), "", "")

        when (data.arg1) {
            API21 -> handleNetworkResultUserInfo(data)
            API22 -> handleNetworkResultUserModify(data)
            API25 -> handleNetworkResultMultiParts(data)
            API75 -> handleAlReadyRegisterByCellPhoneUser(data)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(bus: SoftKeyboardBus) {
        if(bus.height > 100) {
            emptyTouch.visibility = View.VISIBLE
        } else {
            emptyTouch.visibility = View.GONE
        }
    }

    /**
     * 사용자 사진 정보 수정
     */
    private fun handleNetworkResultMultiParts(data: NetworkBusResponse) {
        progressBar.visibility = View.GONE
        if ("ok" == data.arg2) {
            etNewPasswd.text.toString().let {
                if(it.isNotEmpty())
                    AppPreferences.setUserPw(this, it)
            }
            finish()
        } else {
            val responseObj = JSONObject(data.arg4)
            Logger.e("responseObj : ${responseObj}")

            AppToast(this).showToastMessage(responseObj.getString("message"),
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_MIDDLE)
        }
    }

    /**
     * 사용자 정보 수정(프로필 및 배경이미지는 수정 안함)
     */
    private fun handleNetworkResultUserModify(data: NetworkBusResponse) {
        progressBar.visibility = View.GONE
        if ("ok" == data.arg2) {
            etNewPasswd.text.toString().let {
                if(it.isNotEmpty())
                    AppPreferences.setUserPw(this, it)
            }
            finish()
        } else {
            val responseObj = JSONObject(data.arg4)
            Logger.e("responseObj : ${responseObj}")

            AppToast(this).showToastMessage(responseObj.getString("message"),
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_MIDDLE)
        }
    }

    /**
     * 사용자 정보 설정
     */
    private fun handleNetworkResultUserInfo(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            var str = DBManager.getInstance(this@ProfileEditActivity).get(data.arg1)
            var response: API21 = Gson().fromJson(str, API21::class.java)

            initData(response)
            PuddingApplication.mLoginUserData = response // 로그인 유저 정보 저장.
        } else {
            var errorResult: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
            Logger.e("error : $errorResult")
        }
    }

    private fun initData(response: API21) {
        if(response.userIMG.isNotEmpty()) {
            ImageLoad.setImage(this@ProfileEditActivity,
                    imageViewProfile,
                    response.userIMG,
                    null,
                    ImageLoad.SCALE_CIRCLE_CROP,
                    DiskCacheStrategy.ALL)

            ImageLoad.setImage(this@ProfileEditActivity,
                    imageViewProfileBG,
                    response.userIMG,
                    null,
                    ImageLoad.SCALE_CENTER_CROP,
                    DiskCacheStrategy.ALL)


        }

//        if(response.userIMG_bg.isNotEmpty()) {
//            ImageLoad.setImage(this@ProfileEditActivity,
//                    imageViewProfileBG,
//                    response.userIMG_bg,
//                    null,
//                    ImageLoad.SCALE_NONE,
//                    DiskCacheStrategy.ALL)
//        }

        textViewID.text = response.userId
        tvAccount.text = response.userId
        var mail = response.userEmail
        if ( mail == "false")
            mail = ""
        etEmail.setText(mail)

        if(response.userSex == "M") rbMan.isChecked = true else rbWoman.isChecked = true
        if(response.userAge > 0) textAge.text = response.userAge.toString()
        etNickName.setText(response.userNickname)
        etIntroduce.setText(response.userProfile)

        val number = PhoneNumberUtils.formatNumber(response.userHp, Locale.getDefault().country).split("-")
        Logger.e("number.size :: " + number.size)
        if ( number.size == 3 ) {
            tvPhone1.text = number[0]
            tvPhone2.text = number[1]
            tvPhone3.text = number[2]
        }
    }

    /**
     * 완료 버튼을 눌렀을때 변경된 수정사항을 확인하고 변경사항을 저장한 후 종료
     */
    private fun handleConfirm() {

        tvPasswordConfirmError.visibility = View.INVISIBLE
        etNewPasswdConfirm.setBackgroundResource(R.drawable.input_form_normal)
        etNewPasswdConfirm.clearFocus()


        val newPrevConfirm = etPreviousPasswd.text.toString()
        val newPass = etNewPasswd.text.toString()
        val newPassConfirm = etNewPasswdConfirm.text.toString()

        if(newPrevConfirm.isNotEmpty() || newPass.isNotEmpty() || newPassConfirm.isNotEmpty()) {
            if (!AccountPolicy.IsPasswordMatched(newPassConfirm)) {
                Logger.e("pw not matched")
                AppToast(this@ProfileEditActivity).showToastMessage(getString(R.string.msg_signup_error_bad_type_password),
                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                        AppToast.GRAVITY_MIDDLE)
                return
            } else if (newPass != newPassConfirm) {
                Logger.e("pw not same")
                tvPasswordConfirmError.setText(R.string.msg_signup_error_mismatch_password)
                tvPasswordConfirmError.visibility = View.VISIBLE
                etNewPasswdConfirm.setBackgroundResource(R.drawable.input_form_error)
                etNewPasswdConfirm.requestFocus()
                return
            }
        }

        val key = NetworkHandler.getInstance(this).getKey(NetworkApi.API21.toString(), AppPreferences.getUserId(this)!!, "")
        var json = DBManager.getInstance(this@ProfileEditActivity).get(key)
        if (TextUtils.isEmpty(json)) {
            Logger.e("not found info")
            return
        }
        var response: API21 = Gson().fromJson(json, API21::class.java)

        var bus: NetworkBus
        if (coverImage != null || bgImage != null) {
            var builder = MultipartBody.Builder()
            builder.setType(MultipartBody.FORM)
            builder.addFormDataPart("userEmail", etEmail.text.toString())
            builder.addFormDataPart("userHp", tvPhone1.text.toString()+tvPhone2.text.toString()+tvPhone3.text.toString())
            builder.addFormDataPart("userSex", if(rbMan.isChecked) "M" else "F")
            builder.addFormDataPart("userAge", textAge.text.toString())
            builder.addFormDataPart("userNick", etNickName.text.toString())
            builder.addFormDataPart("userProfile", etIntroduce.text.toString())
            builder.addFormDataPart("userPass", newPrevConfirm)
            builder.addFormDataPart("userNewPass", newPassConfirm)

            try {
                if ( file != null && file?.name != null ) {
                    var encodedUserThumbNailName  = URLEncoder.encode(file?.name, "UTF-8")
                    if (file != null) {
                        builder.addFormDataPart("userThumbnail", encodedUserThumbNailName, RequestBody.create(MediaType.parse("multipart/form-data"), file))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            try {
                if ( BGFile != null && BGFile?.name != null ) {
                    Logger.e("BG FILE NAME :: ${BGFile?.name}")
                    var encodedBgThumbNailName  = URLEncoder.encode(BGFile?.name, "UTF-8")
                    if (BGFile != null) {
                        builder.addFormDataPart("backgroundThumbnail", encodedBgThumbNailName, RequestBody.create(MediaType.parse("multipart/form-data"), BGFile))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            var body = builder.build()
            bus = NetworkBus(NetworkApi.API25.name, body)

        } else {
            val obj = JSONObject().apply {
                put("userEmail", etEmail.text.toString())
                put("userHp", tvPhone1.text.toString()+tvPhone2.text.toString()+tvPhone3.text.toString())
                put("userSex", if(rbMan.isChecked) "M" else "F")
                put("userAge", textAge.text.toString())
                put("userNick", etNickName.text.toString())
                put("userProfile", etIntroduce.text.toString())
                put("userPass", etPreviousPasswd.text.toString())
                put("userNewPass", etNewPasswdConfirm.text.toString())
            }

            val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), obj.toString())
            bus = NetworkBus(NetworkApi.API22.name, body)
        }

        progressBar.visibility = View.VISIBLE
        EventBus.getDefault().post(bus)
    }

    /**
     * 카메라로 찍은 사진을 가져온다.
     */
    private fun selectPhotoFromCamera() {
        val state = Environment.getExternalStorageState()
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (intent.resolveActivity(packageManager) != null) {
                try {
                    photoFile = createImageFile() as File
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                if (photoFile != null) {
                    photoUri = FileProvider.getUriForFile(this@ProfileEditActivity, packageName, photoFile as File)
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                    startActivityForResult(intent, REQUEST_CAMERA_CODE)
                }
            }
        }
    }

    /**
     * 갤러리에서 사진은 가져온다.
     */
    private fun selectPhotoFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.setType("image/*")
        startActivityForResult(intent, REQUEST_GALLERY_CODE)
    }

    /**
     * 사진 파일 생성
     */
    private fun createImageFile(): File? {
        var storageDir: File? = null
        try {
            val dir = File(Environment.getExternalStorageDirectory().toString() + "/temp/")
            if (!dir.exists()) {
                dir.mkdir()
            }

            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            mImageCaptureName = "$timeStamp.png"

            storageDir = File(Environment.getExternalStorageDirectory().absoluteFile.toString() + "/temp/" +
                    mImageCaptureName)

            currentPhotoPaath = storageDir.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return storageDir
    }

    /**
     * 카메라로 찍은 사진 적용
     */
    private fun getPictureForPhoto() {
        val bitmap = BitmapFactory.decodeFile(currentPhotoPaath)
        var exif: ExifInterface? = null

        try {
            exif = ExifInterface(currentPhotoPaath)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        var exifOrientation = 0
        var exifDegree = 0

        if (exif != null) {
            exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            exifDegree = exifOrientationToDegree(exifOrientation)
        }

        if (isPhotoType) {
//            Glide.with(this@ProfileEditActivity)
//                    .load(photoFile)
//                    .bitmapTransform(CropCircleTransformation(this@ProfileEditActivity))
//                    .priority(Priority.HIGH)
//                    .diskCacheStrategy(DiskCacheStrategy.ALL)
//                    .into(imageViewProfile)
            ImageLoad.setImage(this@ProfileEditActivity, imageViewProfile, photoFile, null, ImageLoad.SCALE_CIRCLE_CROP, DiskCacheStrategy.ALL)
        } else {
            imageViewProfileBG.setImageBitmap(rotate(bitmap, exifDegree.toFloat()))
        }
    }

    /**
     * 사진의 회전값 가져오기
     */
    private fun exifOrientationToDegree(exifOrientation: Int): Int {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270
        }

        return 0
    }

    /**
     * 사진을 정방향대로 회전하기
     */
    private fun rotate(src: Bitmap, degree: Float): Bitmap {
        // Matrix 객체 생성
        val matrix = Matrix()

        // 회전 각도 세팅
        matrix.postRotate(degree)

        // 이미지와 Matrix를 세팅해서 Bitmap 객체 생성
        return Bitmap.createBitmap(src, 0, 0, src.width, src.height, matrix, true)
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

    /**
     * 캘러리에서 선택한 사진을 가져온다.
     */
    private fun sendPicture(imgUri: Uri) {
        val imagePath = getRealPathFromURI(imgUri)      // path 경로
        var exif: ExifInterface? = null

        try {
            exif = ExifInterface(imagePath)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        var exifOrientation = 0
        var exifDegree = 0

        if (exif != null) {
            exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            exifDegree = exifOrientationToDegree(exifOrientation)
        }

        val bitmap = BitmapFactory.decodeFile(imagePath)    // 경로를 통해 비트맵으로 전환

        if (isPhotoType) {
//            Glide.with(this@ProfileEditActivity)
//                    .load(imgUri)
//                    .bitmapTransform(CropCircleTransformation(this@ProfileEditActivity))
//                    .priority(Priority.HIGH)
//                    .diskCacheStrategy(DiskCacheStrategy.ALL)
//                    .into(imageViewProfile)
            ImageLoad.setImage(this@ProfileEditActivity, imageViewProfile, imgUri, null, ImageLoad.SCALE_CIRCLE_CROP, DiskCacheStrategy.ALL)
        } else {
            imageViewProfileBG.setImageBitmap(rotate(bitmap, exifDegree.toFloat()))
        }
    }

    /**
     * 해당 휴대폰 번호 및 실명을 이용하여 가입된 사용자가 있는지 검사
     */
    private fun handleAlReadyRegisterByCellPhoneUser(userName: String, cellPhoneNumber: String) {
        progressBar.visibility = View.VISIBLE

        name = userName
        phoneNumber = cellPhoneNumber

        var requestObj = JSONObject()
        requestObj.put("userName", userName)
        requestObj.put("userHp", cellPhoneNumber)

        var body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), requestObj.toString())
        val bus = NetworkBus(NetworkApi.API75.name, body)
        EventBus.getDefault().post(bus)
    }

    private fun handleAlReadyRegisterByCellPhoneUser(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            val str = DBManager.getInstance(this@ProfileEditActivity).get(data.arg1)
            var response: API75 = Gson().fromJson(str, API75::class.java)

            progressBar.visibility = View.GONE
            if (response.status || (response.mb_id != null && response.mb_datetime != null)) {
                // 기존에 가입된 사용자가 있음을 알림

            } else {
                // 기존에 사용자가 가입되어 있지 않음
                val number = PhoneNumberUtils.formatNumber(phoneNumber, Locale.getDefault().country).split("-")
                if ( number.size == 3 ) {
                    tvPhone1.text = number[0]
                    tvPhone2.text = number[1]
                    tvPhone3.text = number[2]
                }
            }
        } else {
            var errorResult: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
            Logger.e("Validation ERR $errorResult")

            if (retryCellPhoneValidationCount < RETRY_CELLPHONE_VALIDATION_MAXIMUM_COUNT) {
                ++retryCellPhoneValidationCount
                handleAlReadyRegisterByCellPhoneUser(name, phoneNumber)
            } else {
                AlertDialog.Builder(this@ProfileEditActivity)
                        .setMessage(R.string.error_app)
                        .setPositiveButton(android.R.string.ok) { dialog, _ ->
                            dialog.dismiss()
                            retryCellPhoneValidationCount = 0

                        }.show()
            }
        }
    }



    private val clickListener = View.OnClickListener {
        when (it?.id) {
            R.id.buttonBack -> onBackPressed()
            R.id.buttonConfirm -> handleConfirm()

            R.id.buttonProfile -> {
                var intent = Intent(it!!.context, CoverSelectActivity::class.java)
//                intent.putExtra("cropCircle", true)
                intent.putExtra("ratio_square", true)
                startActivityForResult(intent, REQUEST_CODE_PROFILE_IMAGE_PICK)
            }
            R.id.textAge -> {
                AgeSelectDialog(this, mAgeList, textAge.text.toString(), object : AgeSelectDialog.Listener {
                    override fun onProductItem(item: String) {
                        textAge.setTextColor(Color.parseColor("#192028"))
                        textAge.text = item
                    }
                }).show()
            }
        }
    }
}