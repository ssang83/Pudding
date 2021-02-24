package com.enliple.pudding.shoppingcaster.widget


import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatDialog
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import com.enliple.pudding.R
import com.enliple.pudding.commons.app.Utils
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.vo.API72
import com.enliple.pudding.shoppingcaster.adapter.CasterLiveProductAdapter
import kotlinx.android.synthetic.main.dialog_caster_live_product.*


/**
 * 시청간 상품 버튼을 눌렀을때 상품정보 리스트를 표시시키는 팝업
 * @author hkcha
 * @since 2018.08.29
 */
class CasterLiveProductDialog : AppCompatDialog, CasterLiveProductAdapter.Listener {

    companion object {
        private const val TAG = "CasterLiveProductDialog"
        private const val RECYCLER_VIEW_ITEM_CACHE_COUNT = 20
    }

    private lateinit var mAdapter: CasterLiveProductAdapter


    constructor(context: Context, items: List<API72.RelationPrdBean.ProductItem>) : super(context, R.style.PlayerProductListDialog) {
        if (items == null)
            return
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        var count = 0
        if (items.size >= 4) {
            count = 4
        } else
            count = items.size
        var viewHeight = Utils.ConvertDpToPx(context, 103) * count

        var windowParams = window.attributes
        windowParams.gravity = Gravity.BOTTOM
        windowParams.width = WindowManager.LayoutParams.MATCH_PARENT
        windowParams.height = viewHeight
        window.attributes = windowParams

        setCancelable(true)
        setCanceledOnTouchOutside(true)

        val view = LayoutInflater.from(context).inflate(R.layout.dialog_caster_live_product, null, false)
        setContentView(view)

        view.post {
            recyclerViewProducts.setHasFixedSize(true)
            recyclerViewProducts.setItemViewCacheSize(RECYCLER_VIEW_ITEM_CACHE_COUNT)

            mAdapter = CasterLiveProductAdapter().apply {
                setListener(this@CasterLiveProductDialog)
            }
            recyclerViewProducts.adapter = mAdapter
            mAdapter.setItems(items)
        }
    }

    override fun onLinkClicked(position: Int, item: API72.RelationPrdBean.ProductItem) {
//        context.startActivity(Intent(Intent.ACTION_VIEW)
//                .setComponent(ComponentName("com.enliple.pudding", "com.enliple.pudding.activity.LinkWebViewActivity")).apply {
//                    putExtra("LINK", item!!.strLinkUrl)
//                    putExtra("TITLE", item!!.strPrdName)
//                })
    }

    override fun onProductItemClicked(position: Int, item: API72.RelationPrdBean.ProductItem) {
        Logger.e(TAG, "onProductItemClicked()")
    }

    override fun onProductCartClicked(position: Int, item: API72.RelationPrdBean.ProductItem) {
        Logger.e(TAG, "onProductCartClicked()")
    }


}