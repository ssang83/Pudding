package com.enliple.pudding.shoppingcaster.fragment

// teem

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.os.Handler
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.enliple.pudding.BuildConfig
import com.enliple.pudding.R
import com.enliple.pudding.activity.BroadcastSettingActivity
import com.enliple.pudding.activity.ProductZzimActivity
import com.enliple.pudding.activity.ReservationFinishActivity
import com.enliple.pudding.adapter.home.ThreeCategorySelectAdapter
import com.enliple.pudding.bus.SoftKeyboardBus
import com.enliple.pudding.commons.app.*
import com.enliple.pudding.commons.data.CategoryItem
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.events.OnSingleClickListener
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.NetworkApi
import com.enliple.pudding.commons.network.NetworkBus
import com.enliple.pudding.commons.network.NetworkBusResponse
import com.enliple.pudding.commons.network.NetworkHandler
import com.enliple.pudding.commons.network.vo.API130
import com.enliple.pudding.commons.network.vo.API81
import com.enliple.pudding.commons.network.vo.BaseAPI
import com.enliple.pudding.commons.widget.PendingProgressDialog
import com.enliple.pudding.commons.widget.toast.AppToast
import com.enliple.pudding.shoppingcaster.activity.CoverSelectActivity
import com.enliple.pudding.shoppingcaster.activity.LinkProductActivity
import com.enliple.pudding.shoppingcaster.activity.ShoppingCastActivity
import com.enliple.pudding.shoppingcaster.adapter.SalingAdapter
import com.enliple.pudding.shoppingcaster.data.BroadcastInfo
import com.enliple.pudding.shoppingcaster.data.MainCategoryModel
import com.enliple.pudding.shoppingcaster.data.SalingItem
import com.enliple.pudding.shoppingcaster.widget.EditTag
import com.enliple.pudding.timepicker.RadialPickerLayout
import com.enliple.pudding.timepicker.TimePickerDialog
import com.enliple.pudding.timepicker.Timepoint
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_ready_step1.*
import kotlinx.android.synthetic.main.fragment_ready_step2.*
import kotlinx.android.synthetic.main.fragment_temp.*
import kotlinx.android.synthetic.main.layout_category_select.*
import kotlinx.android.synthetic.main.view_default_input_tag.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * 비디오 방송 시작전 방송설정을 위한 Fragment
 * @author hkcha
 * @since 2018.07.06
 */
class CasterInfoFragment : androidx.fragment.app.Fragment(), SalingAdapter.Listener, TimePickerDialog.OnTimeSetListener {
    companion object {

        private const val ACTIVITY_REQUEST_CODE_IMAGE_PICK = 0xBC01
        private const val ACTIVITY_REQUEST_CODE_PRODUCT_ADDED = 0XBC30
        private const val ACTIVITY_REQUEST_CODE_PRODUCT_ADDED_FROM_STORE = 0XBC31
        private const val ACTIVITY_REQUEST_CODE_PRODUCT_ADDED_FROM_LINK = 0XBC32
        private const val ACTIVITY_REQUEST_CODE_PRODUCT_ADDED_ZZIM = 0XBC33

        private const val DATE_TERM = 13
    }

    private var coverImage: Uri? = null
    private var mHandler = Handler()
    private var mTimer: CountDownTimer? = null
    private lateinit var mAdapter: SalingAdapter

    private var alertDialog: AlertDialog? = null
    private var selectedFirstCategory: CategoryItem? = null
    private var selectedSCategory: CategoryItem? = null
    private var selectedTCategory: ArrayList<CategoryItem>? = null
    private var pendingDialog: PendingProgressDialog? = null

    private var productItem1: String = ""
    private var productItem2: String = ""
    private var productItem3: String = ""
    private var productItem4: String = ""
    private var productItem5: String = ""
    private var productItem6: String = ""
    private var productItem7: String = ""
    private var productItem8: String = ""
    private var productItem9: String = ""
    private var productItem10: String = ""

    private var productLinkId: String = ""
    private var productLinkItem: com.enliple.pudding.shoppingcaster.data.SalingItem? = null
    private var castGubun: String? = ""
    private var chatAccount: String? = ""
    private var chatNickName: String? = ""
    private var chatRoomId: String? = ""

    private var fromStore: String? = null
    private var idx: String? = null
    private var name: String? = null
    private var image: String? = null
    private var price: String? = null
    private var storeName: String? = null
    private var pCode: String? = null
    private var scCode: String? = null

    private var isTenSelected = true
    private var isTwentySelected = true
    private var isThirtySelected = true
    private var isMenSelected = true
    private var isWomenSelected = true
    private var firstCategoryStr = ""
    private var sCategoryStr = ""
    private var tCategoryStr = ""
    private var fullCategoryStr = ""
    private var fullCategoryCodeStr = ""
    private var reservationDate = ""
    private var reservationTime = ""
    private var spannerbleString: SpannableString? = null
    // 편성표 예약 수정 관련 방송정보 데이터
    private var mItem: API130.ReservationItem? = null
    private var mCategoryItems: List<API81.CategoryItem> = mutableListOf()
    private var schedulePos = -1
    private var scheduleStreamKey = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        chatAccount = arguments?.getString(BroadcastSettingActivity.INTENT_EXTRA_KEY_CHAT_ACCOUNT)
        chatNickName = arguments?.getString(BroadcastSettingActivity.INTENT_EXTRA_KEY_CHAT_NICKNAME)
        chatRoomId = arguments?.getString(BroadcastSettingActivity.INTENT_EXTRA_KEY_CHAT_ROOM_ID)
        castGubun = arguments?.getString(BroadcastSettingActivity.INTENT_EXTRA_KEY_TAB)

        fromStore = arguments?.getString("fromStore")
        idx = arguments?.getString("idx")
        name = arguments?.getString("name")
        image = arguments?.getString("image")
        price = arguments?.getString("price")
        storeName = arguments?.getString("storeName")
        pCode = arguments?.getString(ShopTreeKey.KEY_PCODE)
        scCode = arguments?.getString(ShopTreeKey.KEY_SCCODE)
        schedulePos = arguments!!.getInt(BroadcastSettingActivity.INTENT_EXTRA_KEY_SCHEDULE_POSITION, schedulePos)
        scheduleStreamKey = arguments?.getString(BroadcastSettingActivity.INTENT_EXTRA_KEY_SCHEDULE_STREAM_KEY)
                ?: ""

//        initStartAnimationScenario()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_caster_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Logger.e("onViewCreated")
        var screenWidth = AppPreferences.getScreenWidth(context!!)
        var topWidth = screenWidth - Utils.ConvertDpToPx(context!!, 150)
        var topHeight = (topWidth * 202) / 360
        var topParam = top.layoutParams
        topParam.width = topWidth
        topParam.height = topHeight
        top.layoutParams = topParam
//
//        var imgParam = imgLayer.layoutParams
//        var imgWidth = topWidth - Utils.ConvertDpToPx(context!!, 2)
//        var imgHeight = topHeight - Utils.ConvertDpToPx(context!!, 25)
//        imgParam.width = imgWidth
//        imgParam.height = imgHeight
//        imgLayer.layoutParams = imgParam



        EventBus.getDefault().register(this)

        emptyTouch.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                if (event?.getAction() == MotionEvent.ACTION_DOWN) {
                    if(editTextSubject.isFocused) {
                        val imm = context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                        imm!!.hideSoftInputFromWindow(editTextSubject.getWindowToken(), 0)
                    }

                    if(editRegi.isFocused) {
                        val imm1 = context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                        imm1!!.hideSoftInputFromWindow(editRegi.getWindowToken(), 0)
                    }

                    if(editTag.isFocused) {
                        val imm2 = context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                        imm2!!.hideSoftInputFromWindow(editTagView.getWindowToken(), 0)
                    }
                }
                return false
            }
        })

        editTextSubject.onFocusChangeListener = subjectFocusChangeListener
        editRegi.onFocusChangeListener = registrationFocusChangeListener
        textViewCategory1.onFocusChangeListener = firstCategoryFocusChangeListener

        step1.visibility = View.VISIBLE
        step2.visibility = View.GONE
        categoryLayer.visibility = View.GONE

        textSelectedDate.setOnClickListener(clickListener)
        textSelectedTime.setOnClickListener(clickListener)
        buttonNext1.setOnClickListener(clickListener)
        buttonNext2.setOnClickListener(clickListener)
        buttonPrev1.setOnClickListener(clickListener)
        buttonPrev2.setOnClickListener(clickListener)
        top.setOnClickListener(clickListener)
        layoutFirstCategory.setOnClickListener(clickListener)
//        layoutSecondCategory.setOnClickListener(clickListener)
        buttonCategorySelected.setOnClickListener(clickListener)
        if (BuildConfig.DEBUG) {
            testButton.visibility = View.GONE
        } else {
            testButton.visibility = View.GONE
        }
        //test용
        testButton.setOnClickListener(clickListener)
//        textViewCategory2Hint.setOnClickListener(clickListener)

        addProduct.setOnClickListener(clickListener)
        addLink.setOnClickListener(clickListener)
        addPurchaseList.setOnClickListener(clickListener)
        addZzim.setOnClickListener(clickListener)

        editTagView.setTagAddCallBack(object : EditTag.TagAddCallback {
            override fun onTagAdd(tagValue: String): Boolean {
                return true
            }
        })
        editTagView.setTagDeletedCallback(object : EditTag.TagDeletedCallback {
            override fun onTagDelete(deletedTagValue: String) {
            }
        })

        val bus = NetworkBus(NetworkApi.API81.name, "select", "")
        EventBus.getDefault().post(bus)

        recyclerSalingProduct.setItemViewCacheSize(5)
        recyclerSalingProduct.setHasFixedSize(true)
        recyclerSalingProduct.isNestedScrollingEnabled = false
        mAdapter = SalingAdapter(castGubun!!)
        mAdapter.setListener(this@CasterInfoFragment)
        recyclerSalingProduct.adapter = mAdapter
        mAdapter.setFullListener(object : SalingAdapter.FullListener {
            override fun onItemFull(isFull: Boolean) {
                if (isFull) {
                    //layoutProductContainer.visibility = View.VISIBLE
                } else {
                    //layoutProductContainer.visibility = View.GONE
                }
            }
        })

        if (castGubun == BroadcastSettingActivity.TAB_PRODUCT) {
            if (idx != null) {
                if (!mAdapter.hasSameItem(1, idx!!, "")) {
                    var salingItem = com.enliple.pudding.shoppingcaster.data.SalingItem()
                    salingItem.image = image
                    salingItem.name = name
                    salingItem.price = price
                    salingItem.storeName = storeName
                    salingItem.productType = 1
                    salingItem.productIndex = idx
                    salingItem.setLink(false)
                    mAdapter.addItem(salingItem)

                    secondScrollView.post(Runnable { secondScrollView.fullScroll(View.FOCUS_DOWN) })
                    textViewGuide.visibility = View.GONE
                } else {
                    Toast.makeText(context!!, "동일한 상품이 목록에 존재합니다.", Toast.LENGTH_SHORT).show();
                }
            }
        }

//        recyclerSalingProduct.post {
//            recyclerSalingProduct.setItemViewCacheSize(5)
//            recyclerSalingProduct.setHasFixedSize(true)
//            mAdapter = SalingAdapter()
//            recyclerSalingProduct.adapter = mAdapter
//        }

        when (castGubun) {
            BroadcastSettingActivity.TAB_LIVE,
            BroadcastSettingActivity.TAB_MULTI_LIVE,
            BroadcastSettingActivity.TAB_PRODUCT -> {
                editTextSubject.hint = getString(R.string.msg_casting_ready_subject_hint)
                reserveInfoLayer.visibility = View.GONE
                buttonNext2.text = "라이브 시작"
            }

            BroadcastSettingActivity.TAB_VOD_UPLOAD,
            BroadcastSettingActivity.TAB_VOD -> {
                editTextSubject.hint = "VOD 제목을 입력해주세요"
                reserveInfoLayer.visibility = View.GONE
                buttonNext2.text = "영상 선택"
            }

            BroadcastSettingActivity.TAB_RESERVE_BROADCAST -> {
                editTextSubject.hint = getString(R.string.msg_casting_ready_subject_hint)
                reserveInfoLayer.visibility = View.VISIBLE
                buttonNext2.text = "예약신청하기"
            }

            BroadcastSettingActivity.SCHEDULE_LIST -> {
                editTextSubject.hint = getString(R.string.msg_casting_ready_subject_hint)
                reserveInfoLayer.visibility = View.VISIBLE
                buttonNext2.text = "예약수정하기"
                textViewGuide.visibility = View.GONE // 처음에는 상품이 있을것이다.

                val dbKey = NetworkHandler.getInstance(context!!).getKey(NetworkApi.API130.toString(), AppPreferences.getUserId(context!!)!!, "")
                val response: API130 = Gson().fromJson(DBManager.getInstance(context!!).get(dbKey), API130::class.java)
                mItem = response.data[schedulePos]

                setCastingInfo(mItem!!)
            }

            else -> {
                reserveInfoLayer.visibility = View.GONE
                buttonNext2.text = "다음"
            }
        }
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onDetach() {
        super.onDetach()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    private fun getFileName(uri: Uri?): String? {
        if (uri == null)
            return null
        var path = uri.toString()
        var paths = path.split("/")
        return paths.get(paths.size - 1)
    }

    private fun saveResizedImage(context: Context, uri: Uri?, name: String?, resize: Int): Uri? {
        if (uri == null || name == null)
            return null
        var fileType: Bitmap.CompressFormat? = null
        var lName = name.toLowerCase()
        if (lName.endsWith("png")) {
            fileType = Bitmap.CompressFormat.PNG
        } else if (lName.endsWith("jpg")) {
            fileType = Bitmap.CompressFormat.JPEG
        }
        if (fileType == null)
            return null
        var path: Uri? = null
        var bitmap = resize(context, uri, resize)
        if (bitmap != null) {

            var storage = "${context.cacheDir}/temp_image/"
            var file = File(storage)
            if (!file.exists())
                file.mkdir()
            var fileName = name
            var tempFile = File(storage, fileName)

            try {
                tempFile.createNewFile()
                var out = FileOutputStream(tempFile)
                bitmap.compress(fileType, 90, out)
                out.flush()
                out.close()
                path = Uri.fromFile(tempFile)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return path
    }

    private fun resize(context: Context, uri: Uri, resize: Int): Bitmap {
        var resizeBitmap: Bitmap? = null

        var options = BitmapFactory.Options()
        try {
            BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri), null, options) // 1번

            var width = options.outWidth
            var height = options.outHeight
            var samplesize = 1

            while (true) {//2번
                if (width / 2 < resize)
                    break
                width /= 2
                height /= 2
                samplesize *= 2
            }

            options.inSampleSize = samplesize;
            var bitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, options) //3번
            try {
                var exif = ExifInterface(uri.path)
                var orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)
                var matrix = Matrix()
                if (orientation == 6) {
                    matrix.postRotate(90f)
                } else if (orientation == 3) {
                    matrix.postRotate(180f)
                } else if (orientation == 8) {
                    matrix.postRotate(270f)
                }
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true) // rotating bitmap
            } catch (e: Exception) {
                e.printStackTrace()
            }
            resizeBitmap = bitmap
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        return resizeBitmap!!
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Logger.e("onActivityResult requestCode :: " + requestCode)
        if (requestCode == ACTIVITY_REQUEST_CODE_IMAGE_PICK) {
            if (resultCode == Activity.RESULT_OK) {
//                var strUri = data?.getStringExtra("crop_uri")
//                var bitmap: Bitmap? = data?.getParcelableExtra("croped_image")
//                coverImage = data?.data

                var root = Environment.getExternalStorageDirectory().toString()
                var myDir = File("${context!!.cacheDir}/temp_image")

                var fname = "cropTemp.jpg"

                var file = File(myDir, fname)
                coverImage = Uri.parse(file.path)

//                Logger.e("strUri :: $strUri")
//                coverImage = Uri.parse(strUri)
                if (coverImage != null && !isDetached) {
//                    var fileName = getFileName(coverImage)
//                    val displayMetrics = DisplayMetrics()
//                    activity!!.windowManager.defaultDisplay.getMetrics(displayMetrics)
//                    var screenWidth = displayMetrics.widthPixels - Utils.ConvertDpToPx(context!!, 30)
//                    coverImage = saveResizedImage(context!!, data?.data, fileName, screenWidth)
//                    ImageLoad.setImage(view!!.context, imageViewSelected, coverImage, null, ImageLoad.SCALE_NONE, null)


//                    var root = Environment.getExternalStorageDirectory().toString()
//                    var myDir = File(root + "/temp")
//
//                    var fname = "cropTemp.jpg"
//
//                    var file = File(myDir, fname)
                    val bitmap = BitmapFactory.decodeFile(file.path)
                    imageViewSelected.setImageBitmap(bitmap)
                }
            }
        } else if (requestCode == ACTIVITY_REQUEST_CODE_PRODUCT_ADDED) {
            Logger.e("ACTIVITY_REQUEST_CODE_PRODUCT_ADDED")
            if (resultCode == Activity.RESULT_OK) {
                var image: String = data!!.getStringExtra("image")
                var name: String = data!!.getStringExtra("name")
                var price: String = data!!.getStringExtra("price")
                var storeName: String = data!!.getStringExtra("storeName")
                var index: String = data!!.getStringExtra("idx")
                Logger.e("index :: " + index)
                Logger.e("storeName :: " + storeName)
                Logger.e("price :: " + price)
                Logger.e("name :: " + name)
                var strType = "1"
                try {
                    strType = data!!.getStringExtra("strType")
                } catch(e:Exception) {
                    e.printStackTrace()
                }

                var iType = -1;
                if ("1".equals(strType) )
                    iType = 1
                else if ( "3".equals(strType) ) {
                    iType = 3
                } else if ( "2".equals(strType) ) {
                    iType = 2
                }
                Logger.e("iType :: " + iType)
                if (index != null) {
                    if (!mAdapter.hasSameItem(iType, index!!, "")) {
                        var salingItem = com.enliple.pudding.shoppingcaster.data.SalingItem()
                        salingItem.image = image
                        salingItem.name = name
                        salingItem.price = price
                        salingItem.storeName = storeName
                        salingItem.productType = iType
                        salingItem.productIndex = index
                        if ( iType == 2 || iType == 3 )
                            salingItem.setLink(true)
                        else
                            salingItem.setLink(false)
                        mAdapter.addItem(salingItem)

                        secondScrollView.post(Runnable { secondScrollView.fullScroll(View.FOCUS_DOWN) })
                        textViewGuide.visibility = View.GONE
                    } else {
                        Toast.makeText(context!!, "동일한 상품이 목록에 존재합니다.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } else if (requestCode == ACTIVITY_REQUEST_CODE_PRODUCT_ADDED_ZZIM) {
            Logger.e("ACTIVITY_REQUEST_CODE_PRODUCT_ADDED_ZZIM")
            if (resultCode == Activity.RESULT_OK) {
                var image: String = data!!.getStringExtra("image")
                var name: String = data!!.getStringExtra("name")
                var price: String = data!!.getStringExtra("price")
                var storeName: String = data!!.getStringExtra("storeName")
                var index: String = data!!.getStringExtra("idx")
                Logger.e("index :: " + index)
                Logger.e("storeName :: " + storeName)
                Logger.e("price :: " + price)
                Logger.e("name :: " + name)
                var strType = "1"
                try {
                    strType = data!!.getStringExtra("strType")
                } catch(e:Exception) {
                    e.printStackTrace()
                }

                var iType = -1;
                if ("1".equals(strType) )
                    iType = 1
                else if ( "3".equals(strType) ) {
                    iType = 3
                } else if ( "2".equals(strType) ) {
                    iType = 2
                }
                Logger.e("iType :: " + iType)
                if (index != null) {
                    if (!mAdapter.hasSameItem(iType, index!!, "")) {
                        var salingItem = com.enliple.pudding.shoppingcaster.data.SalingItem()
                        salingItem.image = image
                        salingItem.name = name
                        salingItem.price = price
                        salingItem.storeName = storeName
                        salingItem.productType = iType
                        salingItem.productIndex = index
                        salingItem.isFromZzim = true
                        if ( iType == 2 || iType == 3 )
                            salingItem.setLink(true)
                        else
                            salingItem.setLink(false)
                        mAdapter.addItem(salingItem)

                        secondScrollView.post(Runnable { secondScrollView.fullScroll(View.FOCUS_DOWN) })
                        textViewGuide.visibility = View.GONE
                    } else {
                        Toast.makeText(context!!, "동일한 상품이 목록에 존재합니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else if (requestCode == ACTIVITY_REQUEST_CODE_PRODUCT_ADDED_FROM_STORE) {
            Logger.e("ACTIVITY_REQUEST_CODE_PRODUCT_ADDED_FROM_STORE")
            if (resultCode == Activity.RESULT_OK) {
                var image: String = data!!.getStringExtra("image")
                var name: String = data!!.getStringExtra("name")
                var price: String = data!!.getStringExtra("price")
                var storeName: String = data!!.getStringExtra("storeName")
                var index: String = data!!.getStringExtra("idx")
//                var index:String = data!!.getStringExtra("idx")


                if (index != null) {
                    if (!mAdapter.hasSameItem(1, index!!, "")) {
                        var salingItem: com.enliple.pudding.shoppingcaster.data.SalingItem = com.enliple.pudding.shoppingcaster.data.SalingItem()
                        salingItem.image = image
                        salingItem.name = name
                        salingItem.price = price
                        salingItem.storeName = storeName
                        salingItem.productType = 1
                        salingItem.productIndex = index
                        salingItem.setLink(false)
                        mAdapter.addItem(salingItem)

                        secondScrollView.post(Runnable { secondScrollView.fullScroll(View.FOCUS_DOWN) })
                    } else {
                        Toast.makeText(context!!, "동일한 상품이 목록에 존재합니다.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } else if (requestCode == ACTIVITY_REQUEST_CODE_PRODUCT_ADDED_FROM_LINK) {
            Logger.e("ACTIVITY_REQUEST_CODE_PRODUCT_ADDED_FROM_LINK")
            if (resultCode == Activity.RESULT_OK) {
                var image: String = data!!.getStringExtra("image")
                var url: String = data!!.getStringExtra("url")
                var storeName: String = data!!.getStringExtra("storeName")
                var linkId: String = data!!.getStringExtra("linkid")
                var useMoney: String = data!!.getStringExtra("money")
                var price: String = data!!.getStringExtra("price")
                var title: String = data!!.getStringExtra("title")
//                var index:String = data!!.getStringExtra("idx")

                if (title != null) {
                    if (!mAdapter.hasSameItem(2, "", title!!)) {
                        var salingItem: com.enliple.pudding.shoppingcaster.data.SalingItem = com.enliple.pudding.shoppingcaster.data.SalingItem()
                        salingItem.image = image
                        salingItem.storeName = storeName
                        salingItem.name = title
                        salingItem.price = price
                        salingItem.productType = 2
                        salingItem.linkId = linkId
                        salingItem.adUsemoney = useMoney
                        salingItem.setLink(true)
                        mAdapter.addItem(salingItem)

                        secondScrollView.post(Runnable { secondScrollView.fullScroll(View.FOCUS_DOWN) })
                        textViewGuide.visibility = View.GONE
                    } else {
                        Toast.makeText(context!!, "동일한 링크가 목록에 존재합니다.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

//    override fun onSelectedSecondCategoryItemRemove(item: CategoryItem) {
//        Logger.e("Selected Second Category Item : $item")
//        secondCategoryAdapter.removeONvIEWCREATEDategory(item)
//
//        // 전부 다 지워졌을 경우 다시 선택할 수 있도록 UI 를 초기화
//        if (secondCategoryAdapter.getONvIEWCREATEDategory().isEmpty()) {
//            textViewCategory2Hint.visibility = View.VISIBLE
//        }
//    }

    override fun onDelClicked(item: com.enliple.pudding.shoppingcaster.data.SalingItem) {
        if(item.linkId != null) {
            productLinkId = item.linkId
            productLinkItem = item

            EventBus.getDefault().post(NetworkBus(NetworkApi.API74.name, productLinkId))
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(bus: SoftKeyboardBus) {
        Logger.e("CasterInfoFragment SoftKeyboardBus height: ${bus.height}")
        if (bus.height > 100) {
            btnLayer.visibility = View.GONE
            emptyTouch.visibility = View.VISIBLE
        } else {
            btnLayer.visibility = View.VISIBLE
            emptyTouch.visibility = View.GONE
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: NetworkBusResponse) {
        Logger.e("CasterInfoFragment onMessageEvent called")
        val api74 = NetworkHandler.getInstance(context!!).getKey(NetworkApi.API74.toString(), productLinkId, "")
        val api81 = NetworkHandler.getInstance(context!!).getKey(NetworkApi.API81.toString(), "select", "")
        val api132 = NetworkHandler.getInstance(context!!)
                .getKey(NetworkApi.API132.toString(), AppPreferences.getUserId(context!!)!!, "")
        when (data.arg1) {
            api74 -> deleteProductLink(data)
            api81 -> {
                if ("ok" == data.arg2) {
                    if (step1.visibility == View.VISIBLE) { // 상품 선택 화면에서 카테고리 클릭 시에도 여기로 들어와서 SECOND CATEGORY LIST를 초기화 시키므로 STEP1이 화면에 보일 경우에만 SECOND CATEGORY ADAPTER 를 초기화 시키도록 함
                        var layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context!!)
                        layoutManager.orientation = RecyclerView.VERTICAL
                        var adapter = ThreeCategorySelectAdapter(context!!, object : ThreeCategorySelectAdapter.Listener {
                            override fun setSecondCategory(secondCategory: CategoryItem?) {
                                if (secondCategory != null) {
                                    selectedSCategory = secondCategory!!
                                    Logger.e("selectedSCategory 1 :: " + secondCategory.categoryId)
                                    selectedTCategory = null
                                    tCategoryStr = ""
                                    sCategoryStr = secondCategory.categoryName
                                    fullCategoryCodeStr = ""
                                    fullCategoryStr = "$firstCategoryStr > $sCategoryStr >"
                                    var ssb = SpannableString(fullCategoryStr)
                                    var font = R.font.notosanskr_bold
                                    spannerbleString = setCustomFontTypeSpan(context!!, firstCategoryStr.length + 3, fullCategoryStr.length - 2, font, ssb)
                                    strCategory.text = spannerbleString
                                }
                            }

                            override fun setThirdCategory(thirdCategories: ArrayList<CategoryItem>?) {
                                if (thirdCategories != null) {
                                    Logger.e("thirdCategories not null")
                                    if (thirdCategories != null && thirdCategories.size > 0) {
                                        selectedTCategory = thirdCategories!!
                                        var builder = StringBuilder()
                                        for (i in 0 until thirdCategories.size) {
                                            builder.append(thirdCategories.get(i).categoryName)
                                            if ((thirdCategories.size - 1) != i)
                                                builder.append(", ")
                                        }
                                        tCategoryStr = builder.toString()
                                        fullCategoryStr = "$firstCategoryStr > $sCategoryStr > $tCategoryStr"
                                        var ssb = SpannableString(fullCategoryStr)
                                        var font = R.font.notosanskr_bold
                                        spannerbleString = setCustomFontTypeSpan(context!!, fullCategoryStr.length - tCategoryStr.length, fullCategoryStr.length, font, ssb)
                                        strCategory.text = spannerbleString
                                    }
                                } else {
                                    Logger.e("thirdCategories null")
                                    selectedTCategory = null
                                    tCategoryStr = ""
                                    fullCategoryStr = "$firstCategoryStr > $sCategoryStr >"
                                    var ssb = SpannableString(fullCategoryStr)
                                    var font = R.font.notosanskr_bold
                                    spannerbleString = setCustomFontTypeSpan(context!!, firstCategoryStr.length + 3, fullCategoryStr.length - 2, font, ssb)
                                    strCategory.text = spannerbleString
                                }
                            }

                            override fun setFirstCategory(firstCategory: CategoryItem) {
                                if (firstCategory != null) {
                                    selectedFirstCategory = firstCategory!!
                                    selectedSCategory = null
                                    Logger.e("selectedSCategory 2 null:: ")
                                    sCategoryStr = ""
                                    selectedTCategory = null
                                    tCategoryStr = ""
                                    fullCategoryCodeStr = ""
                                    firstCategoryStr = firstCategory.categoryName!!
                                    fullCategoryStr = "$firstCategoryStr > "
                                    var ssb = SpannableString(fullCategoryStr)
                                    var font = R.font.notosanskr_bold
                                    spannerbleString = setCustomFontTypeSpan(context!!, 0, fullCategoryStr.length - 3, font, ssb)
                                    strCategory.text = spannerbleString
                                }
                            }
                        })
//                        var adapter = CategorySelectAdapter(context!!, object : Listener {
//                            override fun setFirstCategory(firstCategory: CategoryItem) {
//                                if (firstCategory != null) {
//                                    selectedFirstCategory = firstCategory!!
//                                    selectedSecondCategory = null
//                                    secondCategoryStr = ""
//                                    fullCategoryCodeStr = ""
//                                    firstCategoryStr = firstCategory.categoryName!!
//                                    fullCategoryStr = "$firstCategoryStr > "
//                                    strCategory.text = fullCategoryStr
////                                textViewCategory1.text = firstCategoryStr
////                                textViewCategory2.text = secondCategoryStr
//                                }
//                            }
//
//                            override fun setSecondCategory(secondCategory: ArrayList<CategoryItem>) {
//                                if (secondCategory != null && secondCategory.size > 0) {
//                                    selectedSecondCategory = secondCategory!!
//                                    var builder = StringBuilder()
//                                    for (i in 0 until secondCategory.size) {
//                                        builder.append(secondCategory.get(i).categoryName)
//                                        if ((secondCategory.size - 1) != i)
//                                            builder.append(",")
//                                    }
//                                    secondCategoryStr = builder.toString()
//                                    fullCategoryStr = "$firstCategoryStr > $secondCategoryStr"
//                                    strCategory.text = fullCategoryStr
//                                }
//                            }
//                        })
                        categoryRecyclerView.setHasFixedSize(false)
                        categoryRecyclerView.isNestedScrollingEnabled = false
                        categoryRecyclerView.layoutManager = layoutManager
                        categoryRecyclerView.adapter = adapter
                        var str = DBManager.getInstance(context!!).get(data.arg1)
                        Logger.e("3 category str :: " + str)
//                        str = getTestStr()
                        val response: API81 = Gson().fromJson(str, API81::class.java)
                        var array = ArrayList<MainCategoryModel.CategoryItem>()
                        Logger.e("array size :: " + array.size)
                        if (response != null) {
                            Logger.e("response.data.size :: " + response.data.size)
                            for (i in 0 until response.data.size) {
                                var item = response.data.get(i)
                                var t_item = MainCategoryModel.CategoryItem()
                                t_item.categoryId = item.categoryId
                                t_item.categoryName = item.categoryName
                                t_item.isSelected = false
                                var sArray = ArrayList<MainCategoryModel.SubItem>()
                                for (j in 0 until item.sub.size) {
                                    var sItem = item.sub.get(j)
                                    var s_item = MainCategoryModel.SubItem()
                                    s_item.categoryId = sItem.categoryId;
                                    s_item.categoryName = sItem.categoryName;
                                    s_item.isSelected = false
                                    var tArray = ArrayList<MainCategoryModel.ThirdItem>()
                                    for (k in 0 until sItem.sub.size) {
                                        var tItem = sItem.sub.get(k)
                                        var th_item = MainCategoryModel.ThirdItem()
                                        th_item.categoryId = tItem.categoryId
                                        th_item.categoryName = tItem.categoryName;
                                        th_item.isSelected = false
                                        tArray.add(th_item)
                                    }
                                    s_item.sub = tArray
                                    sArray.add(s_item)
                                }
                                t_item.sub = sArray
                                array.add(t_item)
                            }
                        }
                        adapter.setItems(array)
//                    setSecondCategory(data)
                    }

                    if (castGubun == BroadcastSettingActivity.SCHEDULE_LIST) {
                        mCategoryItems = CategoryModel.getCategoryList(context!!, "select")
                        for (i in 0 until mCategoryItems.size) {
                            if (mItem!!.category == mCategoryItems[i].categoryId) {
                                val firstCategory = mCategoryItems[i].categoryName
                                var secondCategory = ""
                                for (j in 0 until mCategoryItems[i].sub.size) {
                                    if (mItem!!.ca_id == mCategoryItems[i].sub[j].categoryId) {
                                        secondCategory = mCategoryItems[i].sub[j].categoryName
                                    }
                                }

                                fullCategoryStr = "${firstCategory} > ${secondCategory}"
                                textViewCategory1.text = fullCategoryStr
                                if (fullCategoryStr.length > 18)
                                    textViewCategory1.textSize = 14.0f
                                else
                                    textViewCategory1.textSize = 16.0f

                                break
                            }
                        }
                    }
                }
            }
            api132 -> {
                pendingDialog?.dismiss()
                if ("ok" == data.arg2) {
                    var intent = Intent(activity, ReservationFinishActivity::class.java)
                    startActivity(intent)
                    (activity as BroadcastSettingActivity)?.finish()
                } else {
                    val error = JSONObject(data.arg4)
                    AppToast(context!!).showToastMessage(error["message"].toString(),
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_BOTTOM)
                }
            }
        }
    }

    private fun setCastingInfo(item: API130.ReservationItem) {
        ImageLoad.setImage(
                context!!,
                imageViewSelected,
                item.live_img,
                null,
                ImageLoad.SCALE_NONE,
                null)

        editTextSubject.setText(item.title)
        textSelectedDate.text = "${item.fo_year}-${item.fo_month}-${item.fo_day}"
        if (RadialPickerLayout.is24End) {
            if (item.fo_hour == "00") {
                textSelectedTime.text = "24:00"
            } else {
                textSelectedTime.text = "${item.fo_hour}:00"
            }
        } else {
            textSelectedTime.text = "${item.fo_hour}:00"
        }


        editRegi.setText(item.notice)

        if (!item.tag.isNullOrEmpty()) {
            var tagList = item.tag.split(",") as MutableList<String>
            if (tagList.isNotEmpty()) {
                editTagView.tagList = tagList
            }
        }

        for (i in 0 until item.items.nTotalCount) {
            var salingItem = SalingItem()
            salingItem.image = item.items.data[i].strPrdImg
            salingItem.name = item.items.data[i].strPrdName
            salingItem.price = item.items.data[i].nPrdSellPrice
            salingItem.storeName = item.items.data[i].sc_name
            salingItem.productType = item.items.data[i].strType.toInt()
            salingItem.productIndex = item.items.data[i].idx
            salingItem.setLink(if (item.items.data[i].strType == "1") false else true)
            mAdapter.addItem(salingItem)
        }
    }

    private fun stopTimer() {
        if (mHandler != null && mTimer != null) {
            mTimer!!.cancel()
            mHandler.removeCallbacksAndMessages(null)
        }
    }

    fun handleBackPressed() {
        SoftKeyboardUtils.hideKeyboard(activity)

        try {
            when {
                categoryLayer.visibility == View.VISIBLE -> {
                    step1.visibility = View.VISIBLE
                    step2.visibility = View.GONE
                    categoryLayer.visibility = View.GONE
                }
//                layoutFirstCategorySelect?.visibility == View.VISIBLE -> {
//                    layoutFirstCategorySelect.visibility = View.GONE
//                    step1.visibility = View.VISIBLE
//                    step2.visibility = View.GONE
//                }
//
//                layoutSecondCategorySelect?.visibility == View.VISIBLE -> {
//                    layoutSecondCategorySelect.visibility = View.GONE
//                    step1.visibility = View.VISIBLE
//                    step2.visibility = View.GONE
//                }

                step2?.visibility == View.VISIBLE -> {
                    //mainScrollView.visibility = View.VISIBLE
                    step2.visibility = View.GONE
                    step1.visibility = View.VISIBLE
                }

                else -> {
//                    stopTimer()
                    activity?.finish()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun deleteProductLink(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            if (mAdapter != null) {
                mAdapter.deleteLink(productLinkItem!!)
            }
        } else {
            var errorResult: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
            Logger.e("error : $errorResult")
        }
    }

//    private fun setSecondCategory(data: NetworkBusResponse) {
//        if ("ok" == data.arg2) {
//            val str = DBManager.getInstance(context!!)
//                    .get(data.arg1)
//            val response: API81 = Gson().fromJson(str, API81::class.java)
//
//            var items = ArrayList<CategoryItem>()
//            for (i in 0 until response.data.size) {
//                items.add(CategoryItem(
//                        null,
//                        response.data.get(i).categoryId,
//                        response.data.get(i).categoryName,
//                        "",
//                        "",
//                        response.data.get(i).categoryImage))
//            }
//
//            secondCategoryAdapter.setItems(items)
//        } else {
//            var errorResult: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
//            Logger.e("error : $errorResult")
//        }
//    }

    /**
     * (준비요청 1단계) 편성표 정보 등록 요청
     */
    private fun registerReservationInfo() {
        var context = view!!.context

        if (pendingDialog == null) {
            pendingDialog?.dismiss()
            pendingDialog = PendingProgressDialog(context)
            pendingDialog?.show()
        }

        var categoryCode = ""

        if (castGubun == BroadcastSettingActivity.SCHEDULE_LIST) {
            if (selectedTCategory != null) {
                var selectedCategory = selectedTCategory!!
                for (i in 0 until selectedCategory.size) {
                    categoryCode += if (selectedCategory.size > 1 && i < selectedCategory.size - 1) {
                        "${selectedCategory[i].categoryId}, "
                    } else {
                        "${selectedCategory[i].categoryId}"
                    }
                }
            } else {
                categoryCode = mItem!!.ca_id
            }
        } else {
            var selectedCategory = selectedTCategory!!
            for (i in 0 until selectedCategory.size) {
                categoryCode += if (selectedCategory.size > 1 && i < selectedCategory.size - 1) {
                    "${selectedCategory[i].categoryId}, "
                } else {
                    "${selectedCategory[i].categoryId}"
                }
            }
        }

        setProductInfo()

        var info = BroadcastInfo()
        var productItemArray = ArrayList<String>()
        productItemArray.add(productItem1)
        productItemArray.add(productItem2)
        productItemArray.add(productItem3)
        productItemArray.add(productItem4)
        productItemArray.add(productItem5)
        productItemArray.add(productItem6)
        productItemArray.add(productItem7)
        productItemArray.add(productItem8)
        productItemArray.add(productItem9)
        productItemArray.add(productItem10)

        info.subject = editTextSubject.text.toString()
        info.categoryCode = categoryCode
        info.productItems = productItemArray
        info.tag = getTagStr()
        info.registration = editRegi.text.toString()

        var revTime = reservationTime.toIntOrNull()

        if (castGubun == BroadcastSettingActivity.SCHEDULE_LIST) {
            if (coverImage != null) {
                info.image = coverImage!!.toString()
            } else {
                info.image = mItem!!.live_img
            }

            if (reservationDate.isNotEmpty()) {
                if (revTime != null) {
                    val date = reservationDate.replace("-", "")
                    if (revTime < 10) {
                        info.date = "${date}0$reservationTime"
                    } else {
                        info.date = "$date$reservationTime"
                    }
                }
            } else {
                val date = mItem!!.fo_date.substring(0, mItem!!.fo_date.length - 2)
                if (revTime != null) {
                    if (revTime < 10) {
                        info.date = "${date}0$reservationTime"
                    } else {
                        info.date = "$date$reservationTime"
                    }
                } else {
                    if(mItem!!.fo_hour.toInt() < 10) {
                        info.date = "${date}0${mItem!!.fo_hour}"
                    } else {
                        info.date = "${date}${mItem!!.fo_hour}"
                    }
                }
            }
        } else {
            info.firstCategory = selectedFirstCategory!!
            info.secondCategory = selectedSCategory!!
            info.thirdCategory = selectedTCategory!!
            info.image = coverImage!!.toString()

            if (revTime != null) {
                val date = reservationDate.replace("-", "")
                if (revTime < 10) {
                    info.date = "${date}0$reservationTime"
                } else {
                    info.date = "$date$reservationTime"
                }
            }
        }

        sendReservationInfo(info)
    }

    /**
     * (준비요청 1단계) 방송 정보 등록 요청
     */
    private fun registerBroadcastInfo() {
        Logger.e("registerBroadcastInfo")
        var context = view!!.context

        pendingDialog?.dismiss()
        pendingDialog = PendingProgressDialog(context)
        pendingDialog?.show()

        var categoryCode = ""

        var selectedCategory = selectedTCategory!!
        for (i in 0 until selectedCategory.size) {
            categoryCode += if (selectedCategory.size > 1 && i < selectedCategory.size - 1) {
                "${selectedCategory[i].categoryId}, "
            } else {
                "${selectedCategory[i].categoryId}"
            }
        }

        setProductInfo()

        var info = BroadcastInfo()
        var productItemArray = ArrayList<String>()
        productItemArray.add(productItem1)
        productItemArray.add(productItem2)
        productItemArray.add(productItem3)
        productItemArray.add(productItem4)
        productItemArray.add(productItem5)
        productItemArray.add(productItem6)
        productItemArray.add(productItem7)
        productItemArray.add(productItem8)
        productItemArray.add(productItem9)
        productItemArray.add(productItem10)

        info.subject = editTextSubject.text.toString()
        info.categoryCode = categoryCode
        info.productItems = productItemArray
        info.tag = getTagStr()
        info.registration = editRegi.text.toString()
        info.image = coverImage!!.toString()
        info.firstCategory = selectedFirstCategory!!
        info.secondCategory = selectedSCategory!!
        info.thirdCategory = selectedTCategory!!
        saveInfo(info)
    }

    /**
     * (준비요청 1단계) VOD 방송 정보 등록 요청
     */
    private fun registerBroadcastVODInfo() {
//        buttonNext.setOnClickListener(null)

        var categoryCode = ""

        var selectedCategory = selectedTCategory!!
        for (i in 0 until selectedCategory.size) {
            categoryCode += if (selectedCategory.size > 1 && i < selectedCategory.size - 1) {
                "${selectedCategory[i].categoryId}, "
            } else {
                "${selectedCategory[i].categoryId}"
            }
        }

        setProductInfo()

        var info = BroadcastInfo()
        var productItemArray = ArrayList<String>()
        productItemArray.add(productItem1)
        productItemArray.add(productItem2)
        productItemArray.add(productItem3)
        productItemArray.add(productItem4)
        productItemArray.add(productItem5)
        productItemArray.add(productItem6)
        productItemArray.add(productItem7)
        productItemArray.add(productItem8)
        productItemArray.add(productItem9)
        productItemArray.add(productItem10)

        info.subject = editTextSubject.text.toString()
        info.categoryCode = categoryCode
        info.productItems = productItemArray
        info.tag = getTagStr()
        info.registration = editRegi.text.toString()
        info.image = coverImage!!.toString()
        info.firstCategory = selectedFirstCategory!!
        info.secondCategory = selectedSCategory!!
        info.thirdCategory = selectedTCategory!!
        info.type = "VOD"
        saveInfo(info)
    }

    /**
     * stop1 필터링
     */
    private fun handleNextFirst() {
        Logger.e("handleNextFirst")

        alertDialog?.dismiss()
        alertDialog = null
        categoryLayer.visibility = View.GONE
        step2.visibility = View.GONE

        if (castGubun == BroadcastSettingActivity.SCHEDULE_LIST) {
            step1.visibility = View.GONE
            step2.visibility = View.VISIBLE

            return
        }

        var titleStr = editTextSubject.text?.toString()
        var notiStr = editRegi.text?.toString()
        var titleLength = 0
        var notiLength = 0
        if ( !TextUtils.isEmpty(titleStr) )
            titleLength = titleStr!!.replace(" ", "").length
        if ( !TextUtils.isEmpty(notiStr))
            notiLength = notiStr!!.replace(" ", "").length
        when {
            castGubun == BroadcastSettingActivity.TAB_RESERVE_BROADCAST && textSelectedDate.text.isEmpty() ->
                alertDialog = AlertDialog.Builder(view!!.context)
                        .setMessage("방송일을 선택해주세요.")
                        .setPositiveButton(android.R.string.ok) { dialog, _ ->
                            dialog.dismiss()
                        }.create().apply {
                            show()
                        }

            castGubun == BroadcastSettingActivity.TAB_RESERVE_BROADCAST && textSelectedTime.text.isEmpty() ->
                alertDialog = AlertDialog.Builder(view!!.context)
                        .setMessage("방송시간을 선택해주세요.")
                        .setPositiveButton(android.R.string.ok) { dialog, _ ->
                            dialog.dismiss()
                        }.create().apply {
                            show()
                        }

            coverImage == null ->
                alertDialog = AlertDialog.Builder(view!!.context)
                        .setMessage(R.string.msg_casting_ready_select_cover_image)
                        .setPositiveButton(android.R.string.ok) { dialog, _ ->
                            dialog.dismiss()
                        }.create().apply {
                            show()
                        }

            titleLength <= 0  -> // 제목이 없는 경우
                alertDialog = AlertDialog.Builder(view!!.context)
                        .setMessage(R.string.msg_casting_ready_subject_hint)
                        .setPositiveButton(android.R.string.ok) { dialog, _ ->
                            dialog.dismiss()
                        }.create().apply {
                            show()
                        }

            notiLength <= 0 -> // 공지를 지정하지 않은 경우
                alertDialog = AlertDialog.Builder(view!!.context)
                        .setMessage("공지사항을 입력하세요")
                        .setPositiveButton(android.R.string.ok) { dialog, _ ->
                            dialog.dismiss()
                        }.create().apply {
                            show()
                        }

            (selectedTCategory == null || selectedTCategory?.size == 0) -> {
                alertDialog = AlertDialog.Builder(view!!.context)
                        .setMessage("카테고리를 선택해주세요")
                        .setPositiveButton(android.R.string.ok) { dialog, _ ->
                            dialog.dismiss()
                        }.create().apply {
                            show()
                        }
            }

            editTagView.tagList.size <= 0 -> // 해시테그를 지정하지 않은 경우
                alertDialog = AlertDialog.Builder(view!!.context)
                        .setMessage(R.string.msg_casting_ready_hashtag)
                        .setPositiveButton(android.R.string.ok) { dialog, _ ->
                            dialog.dismiss()
                        }.create().apply {
                            show()
                        }
            else -> {
                //mainScrollView.visibility = View.GONE
                step1.visibility = View.GONE
                step2.visibility = View.VISIBLE
            }
        }
    }

    /**
     * 방송 준비 전 Validation 체크 및 방송 생성 요청
     */
    private fun handleNext() {
        Logger.e("handleNext Clicked")
        alertDialog?.dismiss()
        alertDialog = null

        if (castGubun == BroadcastSettingActivity.SCHEDULE_LIST) {
            registerReservationInfo()
            return
        }

        when {
            coverImage == null ->
                alertDialog = AlertDialog.Builder(view!!.context)
                        .setMessage(R.string.msg_casting_ready_select_cover_image)
                        .setPositiveButton(android.R.string.ok) { dialog, _ ->
                            dialog.dismiss()
                        }.create().apply {
                            show()
                        }

            editRegi.text.isEmpty() -> // 해시테그를 지정하지 않은 경우
                alertDialog = AlertDialog.Builder(view!!.context)
                        .setMessage("공지사항을 입력하세요")
                        .setPositiveButton(android.R.string.ok) { dialog, _ ->
                            dialog.dismiss()
                        }.create().apply {
                            show()
                        }

            TextUtils.isEmpty(editTextSubject.text?.toString()) -> // 제목이 없는 경우
                alertDialog = AlertDialog.Builder(view!!.context)
                        .setMessage(R.string.msg_casting_ready_subject_hint)
                        .setPositiveButton(android.R.string.ok) { dialog, _ ->
                            dialog.dismiss()
                        }.create().apply {
                            show()
                        }

            selectedTCategory!!.size <= 0 -> {
                alertDialog = AlertDialog.Builder(view!!.context)
                        .setMessage("카테고리를 선택해주세요")
                        .setPositiveButton(android.R.string.ok) { dialog, _ ->
                            dialog.dismiss()
                        }.create().apply {
                            show()
                        }
            }

            editTagView.tagList.size <= 0 -> // 해시테그를 지정하지 않은 경우
                alertDialog = AlertDialog.Builder(view!!.context)
                        .setMessage(R.string.msg_casting_ready_hashtag)
                        .setPositiveButton(android.R.string.ok) { dialog, _ ->
                            dialog.dismiss()
                        }.create().apply {
                            show()
                        }

            mAdapter.itemCount == 0 -> {   // 상품을 선택하지 않은 경우
                alertDialog = AlertDialog.Builder(view!!.context)
                        .setMessage(R.string.msg_casting_ready_product_add_error)
                        .setPositiveButton(android.R.string.ok) { dialog, _ ->
                            dialog.dismiss()
                        }.create().apply {
                            show()
                        }
            }
            else -> when (castGubun) {
                BroadcastSettingActivity.TAB_LIVE,
                BroadcastSettingActivity.TAB_MULTI_LIVE,
                BroadcastSettingActivity.TAB_PRODUCT -> {
                    registerBroadcastInfo()
                }

                BroadcastSettingActivity.TAB_VOD_UPLOAD,
                BroadcastSettingActivity.TAB_VOD -> {
                    registerBroadcastVODInfo()
                }

                BroadcastSettingActivity.TAB_RESERVE_BROADCAST -> {
                    registerReservationInfo()
                }
            }
        }
    }

    /**
     * 선태된 1차 카테고리에 따른 2차 카테고리 리스트를 요청
     * @param firstCategory
     */
//    private fun initSecondCategorys(firstCategory: CategoryItem?) {
//        if (firstCategory != null && !isDetached) {
//            secondCategoryAdapter?.clearItems()
//
//            firstCategoryId = firstCategory.categoryId
//            val bus = NetworkBus(NetworkApi.API81.name, firstCategoryId, "")
//            EventBus.getDefault().post(bus)
//        }
//    }

    /**
     * 방송 상품 정보 입력
     */
    private fun setProductInfo() {
        var itemCount = mAdapter.itemCount
        Logger.e("itemCount : $itemCount")
        if (itemCount > 0) {
            val item = mAdapter.getItem(0)
            if (item!!.productType == 2) {
                if ( item!!.isFromZzim )
                    productItem1 = "${item.productType},${item.productIndex}"
                else
                    productItem1 = "${item.productType},${item.linkId}"
            } else {
                productItem1 = "${item.productType},${item.productIndex}"
            }

            Logger.e("productItem1 : $productItem1")
            --itemCount
        }

        if (itemCount > 0) {
            val item = mAdapter.getItem(1)
            if (item!!.productType == 2) {
                if ( item!!.isFromZzim )
                    productItem2 = "${item.productType},${item.productIndex}"
                else
                    productItem2 = "${item.productType},${item.linkId}"
            } else {
                productItem2 = "${item.productType},${item.productIndex}"
            }

            Logger.e("productItem2 : $productItem2")
            --itemCount
        }

        if (itemCount > 0) {
            val item = mAdapter.getItem(2)
            if (item!!.productType == 2) {
                if ( item!!.isFromZzim )
                    productItem3 = "${item.productType},${item.productIndex}"
                else
                    productItem3 = "${item.productType},${item.linkId}"
            } else {
                productItem3 = "${item.productType},${item.productIndex}"
            }

            Logger.e("productItem3 : $productItem3")
            --itemCount
        }

        if (itemCount > 0) {
            val item = mAdapter.getItem(3)
            if (item!!.productType == 2) {
                if ( item!!.isFromZzim )
                    productItem4 = "${item.productType},${item.productIndex}"
                else
                    productItem4 = "${item.productType},${item.linkId}"
            } else {
                productItem4 = "${item.productType},${item.productIndex}"
            }

            Logger.e("productItem4 : $productItem4")
            --itemCount
        }

        if (itemCount > 0) {
            val item = mAdapter.getItem(4)
            if (item != null) {
                productItem5 = "${item.productType},${item.productIndex}"
                if (item!!.productType == 2) {
                    if ( item!!.isFromZzim )
                        productItem5 = "${item.productType},${item.productIndex}"
                    else
                        productItem5 = "${item.productType},${item.linkId}"
                }
                Logger.e("productItem5 : $productItem5")
                --itemCount
            }
        }

        if (itemCount > 0) {
            val item = mAdapter.getItem(5)
            if (item != null) {
                productItem6 = "${item.productType},${item.productIndex}"
                if (item!!.productType == 2) {
                    if ( item!!.isFromZzim )
                        productItem6 = "${item.productType},${item.productIndex}"
                    else
                        productItem6 = "${item.productType},${item.linkId}"
                }
                Logger.e("productItem6 : $productItem6")
                --itemCount
            }
        }

        if (itemCount > 0) {
            val item = mAdapter.getItem(6)
            if (item!!.productType == 2) {
                if ( item!!.isFromZzim )
                    productItem7 = "${item.productType},${item.productIndex}"
                else
                    productItem7 = "${item.productType},${item.linkId}"
            } else {
                productItem7 = "${item.productType},${item.productIndex}"
            }

            Logger.e("productItem7 : $productItem7")
            --itemCount
        }

        if (itemCount > 0) {
            val item = mAdapter.getItem(7)
            if (item!!.productType == 2) {
                if ( item!!.isFromZzim )
                    productItem8 = "${item.productType},${item.productIndex}"
                else
                    productItem8 = "${item.productType},${item.linkId}"
            } else {
                productItem8 = "${item.productType},${item.productIndex}"
            }

            Logger.e("productItem8 : $productItem8")
            --itemCount
        }

        if (itemCount > 0) {
            val item = mAdapter.getItem(8)
            if (item!!.productType == 2) {
                if ( item!!.isFromZzim )
                    productItem9 = "${item.productType},${item.productIndex}"
                else
                    productItem9 = "${item.productType},${item.linkId}"
            } else {
                productItem9 = "${item.productType},${item.productIndex}"
            }

            Logger.e("productItem9 : $productItem9")
            --itemCount
        }

        if (itemCount > 0) {
            val item = mAdapter.getItem(9)
            if (item!!.productType == 2) {
                if ( item!!.isFromZzim )
                    productItem10 = "${item.productType},${item.productIndex}"
                else
                    productItem10 = "${item.productType},${item.linkId}"
            } else {
                productItem10 = "${item.productType},${item.productIndex}"
            }

            Logger.e("productItem10 : $productItem10")
            --itemCount
        }
    }

    private fun hideKeyboard() {
        //SoftKeyboardUtils.hideKeyboard(activity)
        try {
            activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        } catch (e: Exception) {
            Logger.p(e)
        }
    }

//    private var normalClickListener = View.OnClickListener { v ->
//        when (v?.id) {
//            R.id.buttonTargetAllSex -> {
//                if (isMenSelected && isWomenSelected) {
//                    isMenSelected = false
//                    isWomenSelected = false
//                    menSelected()
//                    womenSelected()
//                    buttonTargetAllSex.setBackgroundResource(R.drawable.targeting_unselected)
//                    strTargetAllSex.setTextColor(Color.parseColor("#bcc6d2"))
//                } else {
//                    isMenSelected = true
//                    isWomenSelected = true
//                    menSelected()
//                    womenSelected()
//                    buttonTargetAllSex.setBackgroundResource(R.drawable.targeting_selected)
//                    strTargetAllSex.setTextColor(Color.parseColor("#ff6c6c"))
//                }
//            }
//            R.id.buttonTargetMen -> {
//                toggleMen()
//                menSelected()
//            }
//            R.id.buttonTargetWomen -> {
//                toggleWomen()
//                womenSelected()
//            }
//            R.id.buttonTargetAllAge -> {
//                if (isTenSelected && isTwentySelected && isThirtySelected) {
//                    isTenSelected = false
//                    isTwentySelected = false
//                    isThirtySelected = false
//                    tenSelected()
//                    twentySelected()
//                    thirtySelected()
//                    buttonTargetAllAge.setBackgroundResource(R.drawable.targeting_unselected)
//                    strTargetAllAge.setTextColor(Color.parseColor("#bcc6d2"))
//                } else {
//                    isTenSelected = true
//                    isTwentySelected = true
//                    isThirtySelected = true
//                    tenSelected()
//                    twentySelected()
//                    thirtySelected()
//                    buttonTargetAllAge.setBackgroundResource(R.drawable.targeting_selected)
//                    strTargetAllAge.setTextColor(Color.parseColor("#ff6c6c"))
//                }
//            }
//            R.id.buttonTargetTen -> {
//                toggleTen()
//                tenSelected()
//            }
//            R.id.buttonTargetTwenty -> {
//                toggleTwenty()
//                twentySelected()
//            }
//            R.id.buttonTargetThirty -> {
//                toggleThirty()
//                thirtySelected()
//            }
//        }
//    }

    private val clickListener = object : OnSingleClickListener() {
        override fun onSingleClick(v: View?) {
            when (v?.id) {
                R.id.buttonNext1 -> handleNextFirst()
                R.id.buttonNext2 -> handleNext()

                R.id.buttonPrev1 -> (activity as BroadcastSettingActivity).finishWithDialog()

                R.id.buttonPrev2 -> {
                    step1.visibility = View.VISIBLE
                    step2.visibility = View.GONE
                }

                //R.id.buttonNextTest -> handleNextTest()
                R.id.top -> startActivityForResult(Intent(v.context, CoverSelectActivity::class.java), ACTIVITY_REQUEST_CODE_IMAGE_PICK)

                R.id.layoutFirstCategory -> {
                    val imm = context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                    imm!!.hideSoftInputFromWindow(editTextSubject.getWindowToken(), 0)
                    //mainScrollView.fullScroll(View.FOCUS_DOWN) // scrollview bottom 으로 이동

                    step1.visibility = View.GONE
                    categoryLayer.visibility = View.VISIBLE

//                    textViewCategory1.setBackgroundResource(R.drawable.bg_broadcast_info)
                }

//                R.id.layoutSecondCategory -> {
//                    val imm = context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
//                    imm!!.hideSoftInputFromWindow(editTextSubject.getWindowToken(), 0)
//                    if (!TextUtils.isEmpty(textViewCategory1.text?.toString())) {
//                        // 1차 카테고리가 선택되어 있을 때에만 2차 카테고리를 선택할 수 있도록 설정
//                        step1.visibility = View.GONE
//                        layoutSecondCategorySelect.visibility = View.VISIBLE
//
//                        textViewCategory2.setBackgroundResource(R.drawable.bg_broadcast_info)
//                    }
//                }

                R.id.buttonCategorySelected -> {
                    if (selectedTCategory == null || selectedTCategory?.size == 0) {
                        alertDialog = AlertDialog.Builder(view!!.context)
                                .setMessage("카테고리를 선택해주세요")
                                .setPositiveButton(android.R.string.ok) { dialog, _ ->
                                    dialog.dismiss()
                                }.create().apply {
                                    show()
                                }
                    } else {
                        step1.visibility = View.VISIBLE
                        categoryLayer.visibility = View.GONE
                        if ( spannerbleString != null ) {
                            if ( spannerbleString!!.isNotEmpty() ) {
                                textViewCategory1.text = spannerbleString
                            } else {
                                textViewCategory1.text = fullCategoryStr
                            }
                        } else
                            textViewCategory1.text = fullCategoryStr
                        if (fullCategoryStr.length > 18)
                            textViewCategory1.textSize = 14.0f
                        else
                            textViewCategory1.textSize = 16.0f
//                        textViewCategory2.text = secondCategoryStr
                    }
                }

//                R.id.buttonFirstCategoryBack -> {
//                    step1.visibility = View.VISIBLE
//                    layoutFirstCategorySelect.visibility = View.GONE
//                }
//
//                R.id.buttonSecondCategoryBack -> {
//                    step1.visibility = View.VISIBLE
//                    layoutSecondCategorySelect.visibility = View.GONE
//                }
//
//                R.id.buttonFirstCategoryConfirm -> {
//                    textViewCategory1Hint.visibility = View.GONE
//                    step1.visibility = View.VISIBLE
//                    layoutFirstCategorySelect.visibility = View.GONE
//                    textViewCategory1.text = firstCategoryAdapter?.getSelectedCategory()?.categoryName
//                    textViewCategory1.visibility = View.VISIBLE
//
//                    if (firstCategoryAdapter?.getSelectedCategory() != null) {
//                        if (selectedFirstCategory != null && selectedFirstCategory != firstCategoryAdapter?.getSelectedCategory()) {
////                            selectedSecondCategoryAdapter.clearItems()
//                            textViewCategory2Hint.visibility = View.VISIBLE
//                            textViewCategory2.text = ""
//                        }
//                        selectedFirstCategory = firstCategoryAdapter?.getSelectedCategory()
//                        initSecondCategorys(firstCategoryAdapter.getSelectedCategory())
//                    }
//                }
//
//                R.id.buttonSecondCategoryConfirm -> {
//                    var selectedSecondCategory = secondCategoryAdapter.getSelectedCategory()
//
//                    if (selectedSecondCategory.isNotEmpty()) {
//                        textViewCategory1.text = firstCategoryAdapter?.getSelectedCategory()?.categoryName
//                        textViewCategory1.visibility = View.VISIBLE
//                        textViewCategory2Hint.visibility = View.GONE
//                        step1.visibility = View.VISIBLE
//                        layoutSecondCategorySelect.visibility = View.GONE
//                        var secondStr = ""
//                        var builder: StringBuilder = StringBuilder()
//                        for (i in 0 until selectedSecondCategory.size) {
//                            if (i == selectedSecondCategory.size - 1) {
//                                builder.append(selectedSecondCategory.get(i).categoryName)
//                            } else {
//                                builder.append(selectedSecondCategory.get(i).categoryName)
//                                builder.append(",")
//                            }
//                        }
//                        secondStr = builder.toString()
//                        textViewCategory2.text = secondStr
//                        textViewCategory2.visibility = View.VISIBLE
//                    } else {
//                        textViewCategory2Hint.visibility = View.VISIBLE
//                        textViewCategory2.text = ""
//                        textViewCategory2.visibility = View.GONE
////                        selectedSecondCategoryAdapter.clearItems()
//                    }
//                }

                R.id.addProduct -> {
                    var intent: Intent = Intent(v.context, com.enliple.pudding.shoppingcaster.activity.LiveProductActivity::class.java)
                    intent.apply {
                        putExtra("is_sample", false)
                    }
                    startActivityForResult(intent, ACTIVITY_REQUEST_CODE_PRODUCT_ADDED)
                }

                R.id.addLink -> startActivityForResult(Intent(v.context, LinkProductActivity::class.java), ACTIVITY_REQUEST_CODE_PRODUCT_ADDED_FROM_LINK)

                R.id.addPurchaseList -> {
//                    AppToast(context!!).showToastMessage("추후 버전에 적용될 예정입니다.",
//                            AppToast.DURATION_MILLISECONDS_DEFAULT,
//                            AppToast.GRAVITY_BOTTOM)

                    var intent: Intent = Intent(v.context, com.enliple.pudding.shoppingcaster.activity.LiveProductActivity::class.java)
                    intent.apply {
                        putExtra("is_sample", true)
                    }
                    startActivityForResult(intent, ACTIVITY_REQUEST_CODE_PRODUCT_ADDED)
                }

                R.id.addZzim -> {
                    startActivityForResult(Intent(v.context, ProductZzimActivity::class.java).apply {
                        putExtra(ProductZzimActivity.INTENT_KEY_FROM_CASTING, true)
                    }, ACTIVITY_REQUEST_CODE_PRODUCT_ADDED_ZZIM)
                }

                R.id.testButton -> {
                    test()
                }

                R.id.textSelectedDate -> {
                    showDatePicker()
                }

                R.id.textSelectedTime -> {
                    if (textSelectedDate.text.toString().isEmpty()) {
                        Toast.makeText(activity, "날짜를 선택해주세요", Toast.LENGTH_SHORT).show()
                    } else {
                        showTimePicker(textSelectedDate.text.toString())
                    }

                }
            }
        }
    }

    private fun test() {
        var info = BroadcastInfo()
        var productItemArray = ArrayList<String>()
        productItemArray.add("1,243")
        productItemArray.add(productItem2)
        productItemArray.add(productItem3)
        productItemArray.add(productItem4)

        info.subject = "Test"
        editTextSubject.setText(info.subject)
        info.categoryCode = "1010"
        info.productItems = productItemArray
        info.tag = "Tag"
        info.registration = "공지 공지 공지~"
        if (coverImage == null) {
            AppToast(context!!).showToastMessage("커버이미지를 선택해 주세요",
                    AppToast.DURATION_MILLISECONDS_FAST,
                    AppToast.GRAVITY_MIDDLE)
            return
        }

        info.image = coverImage.toString()
        info.firstCategory = CategoryItem(0L, "10", "패션", "", "", "")
        selectedFirstCategory = info.firstCategory

        info.secondCategory = CategoryItem(0L, "1010", "여성의류", "", "", "")
        selectedSCategory = info.secondCategory
        Logger.e("selectedSCategory 3 :: " + selectedSCategory!!.categoryId)
        var list: ArrayList<CategoryItem> = ArrayList()
        list.add(CategoryItem(0L, "101010", "원피스/정장", "", "", ""))
        selectedTCategory = list
        info.thirdCategory = list

        saveInfo(info)
    }

    private val firstCategoryFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
        if (hasFocus) {
            textViewCategory1.setBackgroundResource(R.drawable.bg_broadcast_info)
        } else {
            textViewCategory1.setBackgroundResource(R.drawable.bg_customer_center_search)
        }
    }

    private val subjectFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
        if (hasFocus) {
            viewLine.setBackgroundColor(Color.parseColor("#5774f4"))
            viewLine.layoutParams = viewLine.layoutParams.apply {
                height = Utils.ConvertDpToPx(context!!, 2)
            }

            SoftKeyboardUtils.showKeyboard(editTextSubject)
        } else {
            SoftKeyboardUtils.hideKeyboard(editTextSubject)
            viewLine.setBackgroundColor(Color.parseColor("#d9e1eb"))
            viewLine.layoutParams = viewLine.layoutParams.apply {
                height = Utils.ConvertDpToPx(context!!, 1)
            }
        }
    }

    private val registrationFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
        if (hasFocus) {
            SoftKeyboardUtils.showKeyboard(editTextSubject)
            noti_line.setBackgroundColor(Color.parseColor("#5774f4"))
            noti_line.layoutParams = noti_line.layoutParams.apply {
                height = Utils.ConvertDpToPx(context!!, 2)
            }
        } else {
            SoftKeyboardUtils.hideKeyboard(editTextSubject)
            noti_line.setBackgroundColor(Color.parseColor("#d9e1eb"))
            noti_line.layoutParams = noti_line.layoutParams.apply {
                height = Utils.ConvertDpToPx(context!!, 1)
            }
        }
    }

    private fun sendReservationInfo(info: BroadcastInfo) {
        Logger.d("sendReservationInfo")
        val body = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("strTitle", info?.subject)
                .addFormDataPart("strCategory", info?.categoryCode)
                .addFormDataPart("strTag", info?.tag)
                .addFormDataPart("strNoti", info?.registration)
                .addFormDataPart("strDate", info?.date)

        if (info?.productItems != null && info?.productItems.size > 0) {
            for (i in info.productItems.indices) {
                var key = "strItems${i + 1}"
                body.addFormDataPart(key, info?.productItems[i])
            }
        }

        if (scheduleStreamKey.isNotEmpty()) {
            body.addFormDataPart("streamKey", scheduleStreamKey)
        }

        val coverImg = File(Uri.parse(info.image).path)
        val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), coverImg)
        val fileBody = MultipartBody.Part.createFormData("strThumbnail", coverImg.name, requestFile)
        if (coverImg.exists()) {
            body.addPart(fileBody)
        }

        EventBus.getDefault().post(NetworkBus(NetworkApi.API132.name, body.build()))
    }

    private fun saveInfo(info: BroadcastInfo) {
        if (info == null) {
            return
        }

        try {
            var obj = JSONObject()
            var jsonArray = JSONArray()
            obj.put(BroadcastInfo.KEY_SUBJECT, info.subject)
            obj.put(BroadcastInfo.KEY_CATEGORY_CODE, info.categoryCode)
            obj.put(BroadcastInfo.KEY_REGISTRATION, info.registration)
            obj.put(BroadcastInfo.KEY_IMAGE, info.image)
            obj.put(BroadcastInfo.KEY_TAG, info.tag)
            obj.put(BroadcastInfo.KEY_TYPE, info.type)

            if (info.productItems != null && info.productItems.size > 0) {
                for (i in 0 until info.productItems.size) {
                    jsonArray.put(info.productItems[i])
                }
                obj.put(BroadcastInfo.KEY_PRODUCT_ITEMS, jsonArray)
            }

            if (info.firstCategory != null) {
                var firstCategoryObject = JSONObject()
                firstCategoryObject.put(BroadcastInfo.KEY_CATEGORY_ID, info.firstCategory.categoryId)
                firstCategoryObject.put(BroadcastInfo.KEY_CATEGORY_NAME, info.firstCategory.categoryName)
                firstCategoryObject.put(BroadcastInfo.KEY_CATEGORY_IMAGE, info.firstCategory.categoryImage)
                obj.put(BroadcastInfo.KEY_FIRST_CATEGORY, firstCategoryObject)
            }

            if (info.secondCategory != null) {
                var secondCategoryObject = JSONObject()
                Logger.e("info.secondCategory.categoryId ::  " + info.secondCategory.categoryId);
                secondCategoryObject.put(BroadcastInfo.KEY_CATEGORY_ID, info.secondCategory.categoryId)
                secondCategoryObject.put(BroadcastInfo.KEY_CATEGORY_NAME, info.secondCategory.categoryName)
                secondCategoryObject.put(BroadcastInfo.KEY_CATEGORY_IMAGE, info.secondCategory.categoryImage)
                obj.put(BroadcastInfo.KEY_SECOND_CATEGORY, secondCategoryObject)
            }

            var thirdArray = JSONArray()
            if (info.thirdCategory != null && info.thirdCategory.size > 0) {
                for (i in 0 until info.thirdCategory.size) {
                    var item = info.thirdCategory[i]
                    var jObj = JSONObject()
                    jObj.put(BroadcastInfo.KEY_CATEGORY_ID, item.categoryId)
                    jObj.put(BroadcastInfo.KEY_CATEGORY_NAME, item.categoryName)
                    jObj.put(BroadcastInfo.KEY_CATEGORY_IMAGE, item.categoryImage)
                    thirdArray.put(jObj)
                }
                obj.put(BroadcastInfo.KEY_THIRD_CATEGORY, thirdArray)
            }
            var strAge = getAgeStr()
            var strSex = getSexStr()
            var strMulti = getMultiStr()
            obj.put(BroadcastInfo.KEY_AGE, strAge)
            obj.put(BroadcastInfo.KEY_SEX, strSex)
            obj.put(BroadcastInfo.KEY_MULTI, strMulti)
            Logger.e("broadcast json :: " + obj.toString())
            AppPreferences.setBroadcastInfo(context!!, obj.toString()) // preference 에 방송 정보 저장 !!

            var gubun = when (castGubun) {
                BroadcastSettingActivity.TAB_LIVE,
                BroadcastSettingActivity.TAB_PRODUCT -> CasterReadyFragment.BUNDLE_EXTRA_VALUE_CASTING_LIVE
                //BroadcastSettingActivity.TAB_MULTI_LIVE -> CasterReadyFragment.BUNDLE_EXTRA_VALUE_CASTING_MULTI_LIVE
                BroadcastSettingActivity.TAB_VOD_UPLOAD -> CasterReadyFragment.BUNDLE_EXTRA_VALUE_CASTING_VOD_UPLOAD
                else -> CasterReadyFragment.BUNDLE_EXTRA_VALUE_CASTING_VOD
            }

            pendingDialog?.dismiss()
            pendingDialog = null

            var intent = Intent(activity, ShoppingCastActivity::class.java)
            intent.putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_CHAT_ACCOUNT, chatAccount)
            intent.putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_CHAT_NICKNAME, chatNickName)
            intent.putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_CHAT_ROOM_ID, chatRoomId)
            intent.putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_TAB, castGubun)
            intent.putExtra(BroadcastSettingActivity.INFO_SUBJECT, editTextSubject.text!!.toString())
            intent.putExtra(BroadcastSettingActivity.INFO_REGI, editRegi.text.toString())
            intent.putExtra(BroadcastSettingActivity.INFO_COVER_IMAGE, coverImage.toString())
            intent.putExtra(BroadcastSettingActivity.INFO_GUBUN, gubun)
            intent.putExtra(BroadcastSettingActivity.INFO_FIRST_CATEGORY, selectedFirstCategory!!)
            intent.putExtra(BroadcastSettingActivity.INFO_SECOND_CATEGORY, selectedSCategory!!)
            intent.putParcelableArrayListExtra(BroadcastSettingActivity.INFO_THIRD_CATEGORY, selectedTCategory!!)
            if (selectedTCategory!! != null) {
                for (item in selectedTCategory!!) {
                    Logger.e("third category value: $item")
                }
            }

            startActivity(intent)
            (activity as BroadcastSettingActivity)?.finish()
        } catch (e: Exception) {
            Logger.p(e)
        }
    }

    private fun getTagStr(): String? {
        var tagStr = ""
        var tagList = editTagView.tagList
        if (tagList != null && tagList.size > 0) {
            var builder = StringBuilder()
            for (i in 0 until tagList.size) {
                var value = tagList.get(i).replace("#", "")
                builder.append(value)
                if (i != tagList.size - 1)
                    builder.append(",")
            }
            tagStr = builder.toString()
        }
        Logger.e("tagStr :: $tagStr")
        return tagStr
    }

//    private fun menSelected() {
//        if (isMenSelected) {
//            buttonTargetMen.setBackgroundResource(R.drawable.targeting_selected)
//            strTargetMen.setTextColor(Color.parseColor("#ff6c6c"))
//        } else {
//            buttonTargetMen.setBackgroundResource(R.drawable.targeting_unselected)
//            strTargetMen.setTextColor(Color.parseColor("#bcc6d2"))
//        }
//        isAllSexSelected()
//    }
//
//    private fun womenSelected() {
//        if (isWomenSelected) {
//            buttonTargetWomen.setBackgroundResource(R.drawable.targeting_selected)
//            strTargetWomen.setTextColor(Color.parseColor("#ff6c6c"))
//        } else {
//            buttonTargetWomen.setBackgroundResource(R.drawable.targeting_unselected)
//            strTargetWomen.setTextColor(Color.parseColor("#bcc6d2"))
//        }
//        isAllSexSelected()
//    }
//
//    private fun tenSelected() {
//        if (isTenSelected) {
//            buttonTargetTen.setBackgroundResource(R.drawable.targeting_selected)
//            strTargetTen.setTextColor(Color.parseColor("#ff6c6c"))
//        } else {
//            buttonTargetTen.setBackgroundResource(R.drawable.targeting_unselected)
//            strTargetTen.setTextColor(Color.parseColor("#bcc6d2"))
//        }
//        isAllAgeSelected()
//    }
//
//    private fun twentySelected() {
//        if (isTwentySelected) {
//            buttonTargetTwenty.setBackgroundResource(R.drawable.targeting_selected)
//            strTargetTwenty.setTextColor(Color.parseColor("#ff6c6c"))
//        } else {
//            buttonTargetTwenty.setBackgroundResource(R.drawable.targeting_unselected)
//            strTargetTwenty.setTextColor(Color.parseColor("#bcc6d2"))
//        }
//        isAllAgeSelected()
//    }
//
//    private fun thirtySelected() {
//        if (isThirtySelected) {
//            buttonTargetThirty.setBackgroundResource(R.drawable.targeting_selected)
//            strTargetThirty.setTextColor(Color.parseColor("#ff6c6c"))
//        } else {
//            buttonTargetThirty.setBackgroundResource(R.drawable.targeting_unselected)
//            strTargetThirty.setTextColor(Color.parseColor("#bcc6d2"))
//        }
//        isAllAgeSelected()
//    }
//
//    private fun isAllAgeSelected() {
//        if (isTenSelected && isTwentySelected && isThirtySelected) {
//            buttonTargetAllAge.setBackgroundResource(R.drawable.targeting_selected)
//            strTargetAllAge.setTextColor(Color.parseColor("#ff6c6c"))
//        } else {
//            buttonTargetAllAge.setBackgroundResource(R.drawable.targeting_unselected)
//            strTargetAllAge.setTextColor(Color.parseColor("#bcc6d2"))
//        }
//    }
//
//    private fun isAllSexSelected() {
//        if (isMenSelected && isWomenSelected) {
//            buttonTargetAllSex.setBackgroundResource(R.drawable.targeting_selected)
//            strTargetAllSex.setTextColor(Color.parseColor("#ff6c6c"))
//        } else {
//            buttonTargetAllSex.setBackgroundResource(R.drawable.targeting_unselected)
//            strTargetAllSex.setTextColor(Color.parseColor("#bcc6d2"))
//        }
//    }

    private fun toggleMen() {
        isMenSelected = !isMenSelected
    }

    private fun toggleWomen() {
        isWomenSelected = !isWomenSelected
    }

    private fun toggleTen() {
        isTenSelected = !isTenSelected
    }

    private fun toggleTwenty() {
        isTwentySelected = !isTwentySelected
    }

    private fun toggleThirty() {
        isThirtySelected = !isThirtySelected
    }

    private fun getAgeStr(): String {
        var ageStr = "all"
        if (isTenSelected && isTwentySelected && isThirtySelected)
            ageStr = "all"
        else if (isTenSelected && !isTwentySelected && !isThirtySelected)
            ageStr = "10"
        else if (isTenSelected && isTwentySelected && !isThirtySelected)
            ageStr = "10,20"
        else if (isTenSelected && !isTwentySelected && isThirtySelected)
            ageStr = "10,30"
        else if (!isTenSelected && isTwentySelected && isThirtySelected)
            ageStr = "20,30"
        else if (!isTenSelected && isTwentySelected && !isThirtySelected)
            ageStr = "20"
        else if (!isTenSelected && !isTwentySelected && isThirtySelected)
            ageStr = "30"
        else if (!isTenSelected && !isTwentySelected && isThirtySelected)
            ageStr = "all"
        Logger.e("ageStr :: $ageStr")
        return ageStr
    }

    private fun getSexStr(): String {
        var sexStr = "all"
        if (isMenSelected && isWomenSelected)
            sexStr = "all"
        else if (isMenSelected && !isWomenSelected)
            sexStr = "m"
        else if (!isMenSelected && isWomenSelected)
            sexStr = "f"
        else if (!isMenSelected && !isWomenSelected)
            sexStr = "all"
        Logger.e("sexStr :: $sexStr")
        return sexStr
    }

    private fun getMultiStr(): String {
        Logger.i("getMultiStr $castGubun")

        return when (castGubun) {
            BroadcastSettingActivity.TAB_MULTI_LIVE -> "Y"
            else -> "N"
        }
    }

    private fun showDatePicker() {
        var sdf = SimpleDateFormat("yyyy-MM-dd")

        var calendar = Calendar.getInstance()
        var minDate = Calendar.getInstance()
        var maxDate = Calendar.getInstance()

        var date = Date(System.currentTimeMillis()) // 현재 시간의 date 가져오기
        calendar.time = date

        var currentTime = calendar.get(Calendar.HOUR_OF_DAY)
        Logger.e("currentTime :::::: $currentTime")
        if (currentTime >= 23) // 밤 11시 이후에는 1시간 이후 반영 시 다음날로 날짜가 넘어가므로 1일 추가해준다.
            calendar.add(Calendar.DATE, 1)

        var datePickerDialog = DatePickerDialog(activity, R.style.DatePickerDialogTheme, DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            var selectedDate = sdf.format(calendar.time)
            textSelectedDate.text = selectedDate
            reservationDate = selectedDate

            reservationTime = ""
            textSelectedTime.text = ""
        },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        )

        minDate.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        datePickerDialog.datePicker.minDate = minDate.time.time

        calendar.add(Calendar.DATE, DATE_TERM)
        maxDate.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        datePickerDialog.datePicker.maxDate = maxDate.timeInMillis

        datePickerDialog.show()
    }

    private fun showTimePicker(selectedDate: String) {
        var sdf = SimpleDateFormat("yyyy-MM-dd")
        var calendar = Calendar.getInstance()
        calendar.time = Date(System.currentTimeMillis()) // 현재 시간의 date 가져오기
        var todayDate = sdf.format(calendar.time)
        var startTime = 1
        if (selectedDate == todayDate) {
            startTime = calendar.get(Calendar.HOUR_OF_DAY) + 1
            reservationTime = startTime.toString()
            if (startTime == 24) {
                startTime = 0
                reservationTime = "0"
            }
        } else {
            startTime = 1
            reservationTime = "1"
        }

        var timePicker = TimePickerDialog.newInstance(this, startTime, 0, true)
        timePicker.setOkColor(0x9f56f2)
        timePicker.setCancelColor(0x546170)
        timePicker.accentColor = 0x5774f4
        timePicker.setOnCancelListener { Log.d("TimePicker", "Dialog was cancelled") }
        timePicker.version = TimePickerDialog.Version.VERSION_1
        timePicker.isCancelable = false

        val array = ArrayList<Timepoint>()
        for (i in 1 until startTime) {
            array.add(Timepoint(i))
        }
        if (selectedDate == todayDate) {
            array.add(Timepoint(0))
        }
        timePicker.setDisabledTimes(array)
        timePicker.show(fragmentManager!!, "Timepickerdialog")
    }

    override fun onTimeSet(view: TimePickerDialog?, hourOfDay: Int, minute: Int, second: Int) {
        if (hourOfDay < 0) {
            textSelectedTime.text = ""
            return
        }
        reservationTime = hourOfDay.toString()
        if (RadialPickerLayout.is24End) {
            if (hourOfDay == 0) {
                textSelectedTime.text = "24:00"
            } else {
                textSelectedTime.text = "${hourOfDay}:00"
            }
        } else {
            textSelectedTime.text = "${hourOfDay}:00"
        }

        if (hourOfDay == 0) {
            // date를 다음날로 넘긴다
            if (reservationDate != null && !reservationDate.isEmpty()) {
                var sdf = SimpleDateFormat("yyyy-MM-dd")
                var arr = reservationDate.split("-")
                var year = arr[0].toInt()
                var month = arr[1].toInt()
                var day = arr[2].toInt()
                var calendar = Calendar.getInstance()
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month - 1)
                calendar.set(Calendar.DAY_OF_MONTH, day + 1)

                var selectedDate = sdf.format(calendar.time)
                textSelectedDate.text = selectedDate
                reservationDate = selectedDate
            }
        }
        Logger.e("reservationTime :: $reservationTime")
    }

    fun setCoverImage(uri: Uri) {
        ImageLoad.setImage(context!!, imageViewSelected, coverImage, null, ImageLoad.SCALE_NONE, null)
    }

    private fun setCustomFontTypeSpan(context: Context, startIndex: Int, endIndex: Int, font: Int, spannableString: SpannableString): SpannableString {
        var typeface = ResourcesCompat.getFont(context, font)
        spannableString.setSpan(StyleSpan(typeface!!.style), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(ForegroundColorSpan(Color.parseColor("#5774f4")), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return spannableString
    }
}