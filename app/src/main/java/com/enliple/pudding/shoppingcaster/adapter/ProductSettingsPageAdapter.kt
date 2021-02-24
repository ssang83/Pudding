package com.enliple.pudding.shoppingcaster.adapter

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewpager.widget.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.enliple.pudding.R

/**
 * 방송 간 상품 설정 ViewPager Adapter
 * @author hkcha
 * @since 2018.07.09
 */
class ProductSettingsPageAdapter() : androidx.viewpager.widget.PagerAdapter() {

    val items: MutableList<Any> = ArrayList()

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val itemView = LayoutInflater.from(container.context)
                .inflate(R.layout.adapter_product_settings_item, container, false)

        container.addView(itemView)

        return itemView
    }

    override fun destroyItem(container: ViewGroup, position: Int, `viewObject`: Any) {
        container.removeView(viewObject as View)
    }

    override fun isViewFromObject(view: View, `viewObject`: Any): Boolean {
        return try {
            view == viewObject as ConstraintLayout
        } catch (e: Exception) {
            false
        }
    }

    override fun getCount(): Int = items.size
}