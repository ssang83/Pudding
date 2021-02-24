package com.enliple.pudding.shoppingcaster.adapter.productdialog

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.enliple.pudding.R

/**
 * 방송자 상품 팝업 방송중 팔로워 리스트 Adapter
 * @author hkcha
 * @since 2018.09.04
 */
class CastFollowerAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<CastFollowerAdapter.FollowUserHolder>() {

    // API 연동되면 DAO 추가와 함께 Generic 수정요망
    private val items: MutableList<Any> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FollowUserHolder =
            FollowUserHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.adapter_cast_follower_list_item, parent, false))

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: FollowUserHolder, position: Int) {
        val item = getItem(position)

        if (item != null) {

        }
    }

    // API 연동되면 DAO 추가와 함께 Generic 수정요망
    fun getItem(position: Int): Any? = if (position in 0 until items.size) position else null

    fun setItems(items: List<Any>) {
        this.items.clear()
        if (items != null) {
            this.items.addAll(items)
        }
        notifyDataSetChanged()
    }

    class FollowUserHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)
}