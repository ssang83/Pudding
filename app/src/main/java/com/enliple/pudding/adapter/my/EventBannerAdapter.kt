package com.enliple.pudding.adapter.my

import androidx.viewpager.widget.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import com.enliple.pudding.R

/**
 * Created by Kim Joonsung on 2018-10-22.
 */
class EventBannerAdapter : androidx.viewpager.widget.PagerAdapter {

    private val imgsArray: Array<Int>
    private var mListener: Listener? = null

    constructor(imgsArray: Array<Int>) : super() {
        this.imgsArray = imgsArray
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val itemView = LayoutInflater.from(container.context)
                .inflate(R.layout.adapter_event_banner_image, container, false) as LinearLayout

        val imageView = itemView.findViewById<ImageView>(R.id.imageViewContent)
        imageView.setImageResource(imgsArray[position])

        itemView.setOnClickListener {
            mListener?.onBannerClicked()
        }

        container.addView(itemView)

        return itemView

    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun getCount(): Int = imgsArray.size

    override fun isViewFromObject(view: View, `object`: Any): Boolean = view == `object` as View

    fun setListener(listener: Listener) {
        mListener = listener
    }

    interface Listener {
        fun onBannerClicked()
    }
}