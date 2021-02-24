package com.enliple.pudding.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.enliple.pudding.R
import com.enliple.pudding.adapter.my.ZzimAdapter
import com.enliple.pudding.bus.ZzimStatusBus
import com.enliple.pudding.commons.app.PriceFormatter
import com.enliple.pudding.commons.app.ShopTreeKey
import com.enliple.pudding.commons.app.Utils
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.NetworkApi
import com.enliple.pudding.commons.network.NetworkBus
import com.enliple.pudding.commons.network.NetworkBusResponse
import com.enliple.pudding.commons.network.vo.API127
import com.enliple.pudding.commons.network.vo.API139
import com.enliple.pudding.commons.network.vo.API145
import com.enliple.pudding.commons.shoptree.network.ShopTreeAsyncTask
import com.enliple.pudding.commons.widget.recyclerview.WrappedLinearLayoutManager
import com.enliple.pudding.commons.widget.toast.AppToast
import com.enliple.pudding.widget.AppAlertDialog
import com.enliple.pudding.widget.AppConfirmDialog
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_product_zzim.*
import kotlinx.android.synthetic.main.activity_product_zzim.topBtn
import kotlinx.android.synthetic.main.fragment_daily_ranking.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject


/**
 * Created by Kim Joonsung on 2019-03-13.
 */
class ProductZzimActivity : AppCompatActivity(), ZzimAdapter.Listener {

    companion object {
        private const val REQUEST_GO_BROADCASTING = 50223

        const val INTENT_KEY_FROM_CASTING = "from_casting"
    }

    private lateinit var mAdapter:ZzimAdapter
    private lateinit var mLayoutManager:WrappedLinearLayoutManager
    private var alertDialog: AppAlertDialog? = null
    private var dialog:AppConfirmDialog? = null

    private var fromCasting = false
    private var strStrType = "1"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_zzim)
        EventBus.getDefault().register(this)

        if(intent != null) {
            Logger.e("intent nut null")
            fromCasting = intent.getBooleanExtra(INTENT_KEY_FROM_CASTING, false)
            Logger.e("fromCasting :: " + fromCasting)
        }

        buttonCheckAll.visibility = View.GONE

        topBtn.setOnClickListener { recyclerViewProduct.scrollToPosition(0) }
        buttonBack.setOnClickListener(clickListener)
//        buttonCart.setOnClickListener(clickListener)
        buttonCheckAll.setOnClickListener(clickListener)
        btnEdit.setOnClickListener(clickListener)
        btnCancel.setOnClickListener(clickListener)
        btnDelete.setOnClickListener(clickListener)
        recyclerViewProduct.setHasFixedSize(true)

        recyclerViewProduct.addOnScrollListener(scrollListener)
        mLayoutManager = WrappedLinearLayoutManager(this).apply {
            orientation = RecyclerView.VERTICAL
        }

        recyclerViewProduct.layoutManager = mLayoutManager
        mAdapter = ZzimAdapter().apply {
            setListener(this@ProductZzimActivity)
        }
        recyclerViewProduct.adapter = mAdapter

//        NetworkBus(NetworkApi.API127.name).let {
//            EventBus.getDefault().post(it)
//        }

        if(fromCasting) {
            textViewTitle.text = "찜 상품"
            toplayer.visibility = View.GONE
            divider.visibility = View.GONE
            imgBack.setBackgroundResource(R.drawable.close_black_ico)
        } else {
            toplayer.visibility = View.VISIBLE
            divider.visibility = View.VISIBLE
            textViewTitle.text = "찜한상품"
            imgBack.setBackgroundResource(R.drawable.back_ico)
        }

        empty.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        NetworkBus(NetworkApi.API145.name).let { EventBus.getDefault().post(it) }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == REQUEST_GO_BROADCASTING && resultCode == Activity.RESULT_OK) {
            setResult(Activity.RESULT_OK, data)
            finish()
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onItemClicked(item: API145.ProductItem) {
        Logger.e("fromCasting :: " + fromCasting)
        if(fromCasting) {
            strStrType = item.strType
            Logger.e("item type " + item.strType)
            if ( "1".equals(item.strType) ) {
                startActivityForResult(Intent(this, ProductDetailActivity::class.java).apply {
                    putExtra("from_live", true)
                    putExtra("from_store", false)
                    putExtra("idx", item.idx)
                    putExtra("name", item.title)
                    putExtra("price", "${String.format(getString(R.string.msg_price_format), PriceFormatter.getInstance()!!.getFormattedValue(item.price))}")
                    putExtra("image", item.image1)
                    putExtra("storeName", item.sitename)

                    putExtra(ShopTreeKey.KEY_PCODE, item.pcode)
                    putExtra(ShopTreeKey.KEY_SCCODE, item.sc_code)
                }, REQUEST_GO_BROADCASTING)
            } else {
                if ( "2".equals(item.strType) || "3".equals(item.strType) ) {
                    val task = ShopTreeAsyncTask(this)
                    task.getDRCLink(item.idx, "", "", item.strType) { result, obj ->
                        try {
                            if (result) {
                                val `object` = obj as JSONObject
                                val response = Gson().fromJson(`object`.toString(), API139::class.java)
                                if ("success" == response.result) {
                                    Logger.e("response.url :: " + response.url)
                                    val intent1 = Intent(this, MobonWebActivity::class.java)
                                    Logger.e("idx :: " + item.idx)
                                    Logger.e("image1 :: " + item.image1)
                                    Logger.e("title :: " + item.title)
                                    intent1.putExtra("idx", item.idx)
                                    intent1.putExtra("name", item.title)
                                    intent1.putExtra("price", Utils.ToNumFormat(java.lang.Double.parseDouble(item.price).toInt()) + "원")
                                    intent1.putExtra("image", item.image1)
                                    intent1.putExtra("storeName", item.sitename)
                                    intent1.putExtra("zzim_status", item.is_wish)
                                    intent1.putExtra(ShopTreeKey.KEY_PCODE, item.pcode)
                                    intent1.putExtra(ShopTreeKey.KEY_SCCODE, item.sc_code)
                                    Logger.e("item strType :: " + item.strType)
                                    intent1.putExtra("strType", item.strType)
                                    intent1.putExtra(MobonWebActivity.INTENT_EXTRA_KEY_LINK, response.url)




//                                val intent = Intent(this, LinkWebViewActivity::class.java)
//                                intent.putExtra("LINK", response.url)
//                                intent.putExtra("IDX", item.idx)
//                                intent.putExtra("IS_WISH", item.is_wish)
//                                intent.putExtra("TYPE", item.strType)
//                                intent.putExtra("TITLE", item.title)
//                                intent.putExtra("ITEM_LINK", true)
                                    startActivityForResult(intent1, REQUEST_GO_BROADCASTING)
                                }
                            }
                        } catch (e:Exception) {
                            Logger.p(e)
                        }
                    }
                }
            }
        } else {
            if("1".equals(item.strType)) {
                startActivity(Intent(this, ProductDetailActivity::class.java).apply {
                    putExtra("from_live", false)
                    putExtra("from_store", false)
                    putExtra("idx", item.idx)
                    putExtra("name", item.title)
                    putExtra("price", "${String.format(getString(R.string.msg_price_format), PriceFormatter.getInstance()!!.getFormattedValue(item.price))}")
                    putExtra("image", item.image1)
                    putExtra("storeName", item.sitename)
                    putExtra(ShopTreeKey.KEY_PCODE, item.pcode)
                    putExtra(ShopTreeKey.KEY_SCCODE, item.sc_code)
                })
            } else {
                if ( "2".equals(item.strType) || "3".equals(item.strType) ) {
                    startActivity(Intent(this, LinkWebViewActivity::class.java).apply {
                        putExtra(LinkWebViewActivity.INTENT_EXTRA_KEY_LINK, item.link_url)
                        putExtra("TITLE", item.title)
                        putExtra("IDX", item.idx)
                        putExtra("TYPE", item.strType)
                        putExtra("IS_WISH", item.is_wish)
                    })
                } else {
                    startActivity(Intent(this, LinkWebViewActivity::class.java).apply {
                        putExtra(LinkWebViewActivity.INTENT_EXTRA_KEY_LINK, item.link_url)
                    })
                }
            }
        }
    }

    override fun updateSelectCount(selected: Int, total: Int) {
        textViewSelectCount.text = "${selected}"

        if(selected == total) {
            imageViewCheckAll.isSelected = true
        } else {
            imageViewCheckAll.isSelected = false
        }
    }

    override fun updateTotalCount(count: Int) {
        textViewTotalCount.text = "${count}"
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(bus: ZzimStatusBus) {
        val idx = bus.productId
        val status = bus.status
        mAdapter.setLikeChaged(idx, status)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data:NetworkBusResponse) {
        val userId = AppPreferences.getUserId(this)!!
        if("GET/user/${userId}/wish" == data.arg1) {
            handleNetworkAPI127(data)
        } else if(data.arg1.startsWith(NetworkApi.API128.toString())) {
            if("ok" == data.arg2) {
                alertDialog!!.dismiss()

                dialog = AppConfirmDialog(this)
                dialog!!.setTitle("찜한 상품")
                dialog!!.setMessage("상품이 삭제되었습니다.")
                dialog!!.setButton("닫기", View.OnClickListener {
                    if(imageViewCheckAll.isSelected) {
                        mAdapter.itemDeleteAll()
                        imageViewCheckAll.isSelected = false
                        setEmptyViewVisible(true)
                    } else {
                        mAdapter.selectItemDel()
                    }

                    dialog!!.dismiss()
                })
                dialog!!.show()
            } else {
                val error = JSONObject(data.arg4)
                AppToast(this).showToastMessage(error["message"].toString(),
                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                        AppToast.GRAVITY_BOTTOM)
            }
        }
    }

    private fun setEmptyViewVisible(visible: Boolean) {
        if (visible) {
            layoutEmpty.visibility = View.VISIBLE
            toplayer.visibility = View.GONE
            divider.visibility = View.GONE
            recyclerViewProduct.visibility = View.GONE
        } else {
            layoutEmpty.visibility = View.GONE
            if ( fromCasting ) {
                toplayer.visibility = View.GONE
                divider.visibility = View.GONE
            } else {
                toplayer.visibility = View.VISIBLE
                divider.visibility = View.VISIBLE
            }
            recyclerViewProduct.visibility = View.VISIBLE
        }
    }

    private fun handleNetworkAPI127(data: NetworkBusResponse) {
        if("ok" == data.arg2) {
            val response: API145 = Gson().fromJson(DBManager.getInstance(this).get(data.arg1), API145::class.java)

            textViewTotalCount.text = "${response.nTotalCount}"

            if(response.nTotalCount.toInt() > 0) {
                setEmptyViewVisible(false)
                mAdapter.setItems(response.data)
            } else {
                setEmptyViewVisible(true)
            }
        }
    }

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val visibleCount = mLayoutManager.findFirstVisibleItemPosition()

            if ( topBtn.visibility == View.GONE ) {
                if ( visibleCount > 8 )
                    topBtn.visibility = View.VISIBLE
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            var state = newState
            if ( state == RecyclerView.SCROLL_STATE_IDLE ) {
                Handler().postDelayed({
                    if ( state == RecyclerView.SCROLL_STATE_IDLE )
                        topBtn.visibility = View.GONE
                }, 1500)
            }
        }
    }

    private val clickListener = View.OnClickListener {
        when(it?.id) {
            R.id.buttonBack -> {
                setResult(Activity.RESULT_CANCELED)
                finish()
            }

            R.id.btnEdit -> {
                if ( mAdapter != null ) {
                    empty.visibility = View.GONE
                    twoBtnLayer.visibility = View.VISIBLE
                    btnEdit.visibility = View.GONE
                    buttonCheckAll.visibility = View.VISIBLE
                    textViewSelectCount.visibility = View.VISIBLE
                    slash.visibility = View.VISIBLE

                    textViewAll.text = "전체선택"
                    mAdapter.setEditMode(true)
                }
            }

            R.id.btnCancel -> {
                if ( mAdapter != null ) {
                    empty.visibility = View.VISIBLE
                    twoBtnLayer.visibility = View.GONE
                    btnEdit.visibility = View.VISIBLE
                    buttonCheckAll.visibility = View.GONE
                    textViewSelectCount.visibility = View.GONE
                    slash.visibility = View.GONE

                    textViewSelectCount.text = "0"
                    textViewAll.text = "전체"
                    imageViewCheckAll.isSelected = false
                    mAdapter.itemUnSelectAll()
                    mAdapter.setEditMode(false)
                }
            }

            R.id.btnDelete -> {
                if(mAdapter.getSelectedCount() > 0 ) {
                    Logger.e("productId : ${mAdapter.getSelectedId()}")
                    alertDialog = AppAlertDialog(this)
                    alertDialog?.setTitle("찜한 상품")
                    alertDialog?.setMessage("선택한 상품을 삭제할까요?")
                    alertDialog?.setLeftButton(getString(R.string.msg_cancel), View.OnClickListener {
                        alertDialog?.dismiss()
                    })
                    alertDialog?.setRightButton(getString(R.string.msg_confirm), View.OnClickListener {
                        Logger.e("productId : ${mAdapter.getSelectedId()}")
                        NetworkBus(NetworkApi.API128.name, mAdapter.getSelectedId()).let {
                            EventBus.getDefault().post(it)
                        }
                    })
                    alertDialog?.show()
                }
            }

            R.id.buttonCheckAll -> {
                imageViewCheckAll.isSelected = !imageViewCheckAll.isSelected
                if(imageViewCheckAll.isSelected) {
                    mAdapter.itemSelectAll()
                    textViewSelectCount.text = "${mAdapter.getItemCount()}"
                } else {
                    mAdapter.itemUnSelectAll()
                    textViewSelectCount.text = "0"
                }
            }
        }
    }
}