package com.enliple.pudding.widget

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.enliple.pudding.R
import com.enliple.pudding.commons.app.ImageLoad
import kotlinx.android.synthetic.main.view_schedule_product.view.*

/**
 * Created by Kim Joonsung on 2019-03-07.
 */
class ScheduleProduct : FrameLayout {

    constructor(context: Context, image:String) : super(context) {
        init(context, image)
    }

    private fun init(context: Context, image: String) {
        val view = LayoutInflater.from(context).inflate(R.layout.view_schedule_product, null, false)

        if(image.isNotEmpty()) {
            ImageLoad.setImage(
                    context,
                    view.imageViewProduct,
                    image,
                    null,
                    ImageLoad.SCALE_NONE,
                    DiskCacheStrategy.ALL)
        }

        val d = context.getDrawable(R.drawable.bg_rounding_image) as GradientDrawable
        view.imageViewProduct.background = d
        view.imageViewProduct.clipToOutline = true

        addView(view)
    }
}