package com.enliple.pudding.adapter.home

import android.content.Context
import androidx.viewpager.widget.PagerAdapter
import androidx.appcompat.widget.AppCompatImageView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.enliple.pudding.R
import com.enliple.pudding.commons.app.ImageLoad
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.vo.API114

/**
 */
class MainBannerPageAdapter : androidx.viewpager.widget.PagerAdapter, View.OnClickListener {
//    val items: MutableList<Any> = ArrayList()

    private var mContext: Context
    private val items:MutableList<API114.BannerItem> = mutableListOf()

    constructor(context: Context) : super() {
        mContext = context
    }

    override fun getCount(): Int = items.size

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        var view = LayoutInflater.from(container.context).inflate(R.layout.adapter_main_banner_item, container, false)

        items[position].let { item ->
            ImageLoad.setImage(
                    mContext,
                    view.findViewById<AppCompatImageView>(R.id.bannerImage),
                    item.bn_image,
                    null,
                    ImageLoad.SCALE_NONE,
                    DiskCacheStrategy.ALL)
        }

        view.setOnClickListener(this)
        view.tag = position
        container.addView(view)
        return view
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun onClick(v: View?) {
        Logger.e("onClick: ${v?.tag}")
    }

    fun setItems(list:List<API114.BannerItem>) {
        this.items.clear()
        this.items.addAll(list)

        notifyDataSetChanged()
    }
}