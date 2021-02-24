package com.enliple.pudding.shoppingcaster.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.enliple.pudding.R
import com.enliple.pudding.activity.LinkWebViewActivity
import com.enliple.pudding.commons.app.SoftKeyboardUtils
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.NetworkApi
import com.enliple.pudding.commons.network.NetworkBus
import com.enliple.pudding.commons.network.NetworkBusResponse
import com.enliple.pudding.commons.network.vo.API137
import com.enliple.pudding.commons.network.vo.API138
import com.enliple.pudding.commons.network.vo.API73
import com.enliple.pudding.commons.network.vo.BaseAPI
import com.enliple.pudding.commons.shoptree.network.ShopTreeAsyncTask
import com.enliple.pudding.commons.widget.toast.AppToast
import com.enliple.pudding.shoppingcaster.adapter.LinkProductAdapter
import com.enliple.pudding.widget.LinkProductDialog
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_link_product.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import java.io.File

/**
 * Created by Kim Joonsung on 2018-10-01.
 */
class LinkProductActivity : AppCompatActivity(), LinkProductAdapter.Listener, LinkProductDialog.Listener {

    companion object {
        private const val OUTLINK_REQUEST = 0x0001
        private const val REQUEST_CODE_IMAGE_PICK = 0xBC02
    }

    private lateinit var mAdapter: LinkProductAdapter
    private var keyword = ""
    private var productDialog: LinkProductDialog? = null
    private var imageUrl = ""
    private var productTitle = ""
    private var price = ""
    private var shopName = ""
    private var productImg: Uri? = null
    private var loadUrl = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_link_product)
        EventBus.getDefault().register(this)

        editTextSearch.imeOptions = EditorInfo.IME_ACTION_SEARCH
        editTextSearch.addTextChangedListener(textChangeListener)
        editTextSearch.setOnEditorActionListener(actionListener)

        buttonSearchBack.setOnClickListener(clickListener)
        textViewConfirm.setOnClickListener(clickListener)
        btnDeleteSearchWord.setOnClickListener(clickListener)
        textViewDirectSearch.setOnClickListener(clickListener)
        buttonConfirm.setOnClickListener(clickListener)

        recyclerViewLink.setHasFixedSize(false)
        mAdapter = LinkProductAdapter().apply {
            setListener(this@LinkProductActivity)
        }

        recyclerViewLink.adapter = mAdapter

        keyword = editTextSearch.text.toString()
        NetworkBus(NetworkApi.API137.name, keyword).let { EventBus.getDefault().post(it) }
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == OUTLINK_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                val image = data?.getStringExtra("image")
                val url = data?.getStringExtra("url")
                val storeName = data?.getStringExtra("storeName")
                val linkid = data?.getStringExtra("linkid")
                val money = data?.getStringExtra("money")
                val price = data?.getStringExtra("price")
                val title = data?.getStringExtra("title")

                val intent = Intent()
                intent.putExtra("url", url)
                intent.putExtra("image", image)
                intent.putExtra("storeName", storeName)
                intent.putExtra("linkid", linkid)
                intent.putExtra("money", money)
                intent.putExtra("price", price)
                intent.putExtra("title", title)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        } else if(requestCode == REQUEST_CODE_IMAGE_PICK) {
            if(resultCode == Activity.RESULT_OK) {
                val myDir = File("$cacheDir/temp_image")
                val name = "cropTemp.jpg"

                val file = File(myDir, name)
                productImg = Uri.parse(file.path)

                if (productImg != null) {
                    productDialog?.setProductImage(productImg!!)
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onItemClicked(item: API137.MobionItem) {
        val intent = Intent(this@LinkProductActivity, LinkWebViewActivity::class.java)
        intent.putExtra(LinkWebViewActivity.INTENT_EXTRA_KEY_LINK, item.ad_site)
        intent.putExtra(LinkWebViewActivity.INTENT_EXTRA_KEY_SHOP_NAME, item.ad_title)
        intent.putExtra(LinkWebViewActivity.INTENT_EXTRA_KEY_FROM_CASTING, true)
        startActivityForResult(intent, OUTLINK_REQUEST)
    }

    override fun onCastingProduct() {
        val body = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("siteurl", loadUrl)
                .addFormDataPart("sitename", productDialog!!.getShopName())
                .addFormDataPart("price", productDialog!!.getPrice())
                .addFormDataPart("imageurl", imageUrl)
                .addFormDataPart("title", productDialog!!.getProduct())

        if (productImg != null) {
            val thumbImg = File(productImg!!.getPath()!!)
            val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), thumbImg)
            val filebody = MultipartBody.Part.createFormData("thumbnail", thumbImg.getName(), requestFile)
            if (thumbImg.exists()) {
                body.addPart(filebody)
            }
        }

        EventBus.getDefault().post(NetworkBus(NetworkApi.API73.name, body.build()))
    }

    override fun onProductImage() {
        startActivityForResult(Intent(this, CoverSelectActivity::class.java).apply {
            putExtra("ratio_square", true)
        }, REQUEST_CODE_IMAGE_PICK)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data:NetworkBusResponse) {
        val key = "${NetworkApi.API137}?title=${keyword}"
        if(data.arg1.startsWith(key)) {
            if(data.arg2 == "ok") {
                val response: API137 = Gson().fromJson(DBManager.getInstance(this).get(data.arg1), API137::class.java)

                if(response.nTotalCount > 0) {
                    recyclerViewLink.visibility = View.VISIBLE
                    layoutHeader.visibility = View.VISIBLE
                    layoutEmpty.visibility = View.GONE

                    mAdapter.setItem(response.data)
                } else {
                    recyclerViewLink.visibility = View.GONE
                    layoutHeader.visibility = View.GONE
                    layoutEmpty.visibility = View.VISIBLE
                }
            }
        } else if(data.arg1 == NetworkApi.API73.toString()) {
            handlePostLink(data)
        }
    }

    private fun handlePostLink(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            val response = Gson().fromJson(DBManager.getInstance(this).get(data.arg1), API73::class.java)

            val intent = Intent()
            intent.putExtra("image", response.imageurl)
            intent.putExtra("url", loadUrl)
            intent.putExtra("storeName", response.sitename)
            intent.putExtra("title", response.title)
            intent.putExtra("linkid", response.linkid.toString())
            intent.putExtra("money", response.ad_usemoney)
            intent.putExtra("price", response.price)
            setResult(Activity.RESULT_OK, intent)

            finish()
        } else {
            val error = Gson().fromJson(data.arg4, BaseAPI::class.java)
            Logger.e("error : $error")

            AppToast(this).showToastMessage(error.message,
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM)
        }
    }

    private val clickListener = View.OnClickListener {
        when (it?.id) {
            R.id.buttonSearchBack -> onBackPressed()

            R.id.textViewConfirm -> {
                keyword = editTextSearch.text.toString()
                NetworkBus(NetworkApi.API137.name, keyword).let { EventBus.getDefault().post(it) }

                SoftKeyboardUtils.hideKeyboard(editTextSearch)
            }

            R.id.btnDeleteSearchWord -> {
                editTextSearch.setText("")
                btnDeleteSearchWord.visibility = View.GONE
            }

            R.id.textViewDirectSearch -> {
                val intent = Intent(this@LinkProductActivity, LinkWebViewActivity::class.java).apply {
                    putExtra(LinkWebViewActivity.INTENT_EXTRA_KEY_LINK, "https://www.naver.com")
                    putExtra(LinkWebViewActivity.INTENT_EXTRA_KEY_FROM_CASTING, true)
                }
                startActivityForResult(intent, OUTLINK_REQUEST)
            }

            R.id.buttonConfirm -> {
                loadUrl = editTextUrl.text.toString()
                val task = ShopTreeAsyncTask(this)
                task.getProductLink(loadUrl) { rt, obj ->
                    try {
                        val response = Gson().fromJson(obj.toString(), API138::class.java)

                        if(response.result != null) {
                            if (response.result.toLowerCase() == "success") {
                                imageUrl = response.imageurl
                                productTitle = response.title
                                price = response.price
                                shopName = response.sitename

                                productDialog = LinkProductDialog(this@LinkProductActivity, shopName, imageUrl, productTitle, price)
                                productDialog!!.setListener(this@LinkProductActivity)
                                productDialog!!.show()
                            }
                        } else {
                            JSONObject(obj.toString()).let {
                                AppToast(this@LinkProductActivity).showToastMessage(it.getString("message"),
                                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                                        AppToast.GRAVITY_BOTTOM)
                            }
                        }
                    } catch (e: Exception) {
                        Logger.p(e)
                    }
                }
            }
        }
    }

    private val textChangeListener = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (s != null) {
//                var str: String = s.toString().replace(" ", "")
//                if (str.isNotEmpty()) {
//                    btnDeleteSearchWord.visibility = View.VISIBLE
//                } else {
//                    btnDeleteSearchWord.visibility = View.GONE
//                }
            }
        }
    }

    private val actionListener = TextView.OnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            keyword = editTextSearch.text.toString()
            NetworkBus(NetworkApi.API137.name, keyword).let { EventBus.getDefault().post(it) }

            SoftKeyboardUtils.hideKeyboard(editTextSearch)
            true
        }

        false
    }
}