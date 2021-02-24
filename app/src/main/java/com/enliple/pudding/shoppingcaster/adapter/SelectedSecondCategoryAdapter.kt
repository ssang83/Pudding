package com.enliple.pudding.shoppingcaster.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.enliple.pudding.commons.data.CategoryItem
import com.enliple.pudding.commons.ui_compat.PixelUtil
import com.enliple.pudding.R
import kotlinx.android.synthetic.main.adapter_selected_second_category_item.view.*

/**
 * 방송 시작전 두번째 카테고리 리스트를 보여주는 RecyclerView.Adapter
 * @author hkcha
 * @since 2018.08.24
 */
class SelectedSecondCategoryAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<SelectedSecondCategoryAdapter.SelectedHolder>() {

    private val items: MutableList<CategoryItem> = ArrayList()
    private var mListener: Listener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectedHolder {
        return SelectedHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.adapter_selected_second_category_item, parent, false))
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: SelectedHolder, position: Int) {
        val item = getItem(position)

        // 아이템 위치에 따른 좌측 여백 설정
        var params = holder.itemView.layoutParams as ViewGroup.MarginLayoutParams
        if (position == 0) {
            params.width = ViewGroup.MarginLayoutParams.WRAP_CONTENT
            params.leftMargin = 0
        } else {
            params.width = ViewGroup.MarginLayoutParams.WRAP_CONTENT
            params.leftMargin = PixelUtil.dpToPx(holder.itemView.context, 8)
        }
        holder.itemView.layoutParams = params

        if (item != null) {
            holder.itemView.textViewTitle.text = item.categoryName
            holder.itemView.setOnClickListener {
                mListener?.onSelectedSecondCategoryItemRemove(item)
                items.remove(item)
                notifyDataSetChanged()
            }
        }
    }

    fun setListener(listener: Listener?) {
        mListener = listener
    }

    fun setItems(items: List<CategoryItem>) {
        this.items.clear()
        if (items != null) {
            this.items.addAll(items)
        }
        notifyDataSetChanged()
    }

    fun clearItems() {
        this.items.clear()
        notifyDataSetChanged()
    }

    private fun getItem(position: Int): CategoryItem? = if (position in 0 until items.size) items[position] else null

    class SelectedHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)

    interface Listener {
        fun onSelectedSecondCategoryItemRemove(item: CategoryItem)
    }
}