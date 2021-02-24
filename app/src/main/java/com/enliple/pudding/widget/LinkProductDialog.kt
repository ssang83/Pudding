package com.enliple.pudding.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatDialog
import android.view.View
import android.view.Window
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.enliple.pudding.R
import com.enliple.pudding.commons.app.ImageLoad
import com.enliple.pudding.commons.app.PriceFormatter
import com.enliple.pudding.commons.events.OnSingleClickListener
import com.enliple.pudding.commons.widget.toast.AppToast
import kotlinx.android.synthetic.main.dialog_link_product.*
import kotlinx.android.synthetic.main.store_first_category_item.*

/**
 * Created by Kim Joonsung on 2019-03-13.
 */
class LinkProductDialog(context: Context, storeName:String, image:String, title:String, price:String) : AppCompatDialog(context, true, null) {

    private var mListener:Listener? = null
    private var mContext: Context
    private var productImage:Uri? = null
    private var imageUrl:String = ""
    private var mPrice:String = ""

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        setContentView(R.layout.dialog_link_product)

        mContext = context
        imageUrl = image
        mPrice = price
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        buttonCancel.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                dismiss()
            }
        })

        buttonConfirm.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                if(validation()) {
                    mListener?.onCastingProduct()
                    dismiss()
                }
            }
        })

        buttonLinkProduct.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                mListener?.onProductImage()
            }
        })

        editTextShopName.setText(storeName)
        editTextProduct.setText(title)

        if(price != "0") {
            editTextPrice.setText("${String.format(mContext.getString(R.string.msg_price_format),
                    PriceFormatter.getInstance()!!.getFormattedValue(price))}")
        }

        if(image.isNotEmpty()) {
            ImageLoad.setImage(mContext,
                    imageViewLinkProduct,
                    image,
                    null,
                    ImageLoad.SCALE_NONE,
                    DiskCacheStrategy.ALL)
        }
    }

    fun setListener(listener: Listener) {
        this.mListener = listener
    }

    fun setProductImage(image: Uri) {
        productImage = image

        val bitmap = BitmapFactory.decodeFile(image.path)
        imageViewLinkProduct.setImageBitmap(bitmap)
    }

    fun getPrice() : String = editTextPrice.text.toString()
    fun getShopName() : String = editTextShopName.text.toString()
    fun getProduct() : String = editTextProduct.text.toString()

    override fun onBackPressed() {
        dismiss()
    }

    override fun dismiss() {
        if(this.isShowing) {
            super.dismiss()
        }
    }

    private fun validation() : Boolean {
        var success = false

        if(imageUrl.isEmpty() && productImage == null) {
            AppToast(mContext).showToastMessage("상품 이미지를 입력해주세요.",
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM)
        } else if(editTextShopName.text.toString().isEmpty()) {
            AppToast(mContext).showToastMessage("상점명을 입력해주세요.",
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM)
        } else if(editTextProduct.text.toString().isEmpty()) {
            AppToast(mContext).showToastMessage("상품명을 입력해주세요.",
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM)
        } else if(editTextPrice.text.toString().isEmpty()) {
            AppToast(mContext).showToastMessage("가격을 입력해주세요.",
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM)
        } else {
            success = true
        }

        return success
    }

    interface Listener {
        fun onCastingProduct()
        fun onProductImage()
    }
}