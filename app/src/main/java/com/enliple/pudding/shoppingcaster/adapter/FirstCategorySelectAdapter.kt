package com.enliple.pudding.shoppingcaster.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.enliple.pudding.commons.data.CategoryItem
import com.enliple.pudding.R
import kotlinx.android.synthetic.main.adapter_caster_category_item.view.*

/**
 * 방송 시작전 첫번째 카테고리 선택 Adapter
 * @author hkcha
 * @since 2018.08.23
 */
class FirstCategorySelectAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<CategoryViewHolder>() {

    private val items = ArrayList<CategoryItem>()
    private var selectedCategory: CategoryItem? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        return CategoryViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.adapter_caster_category_item, parent, false))
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        var item = getItem(position)

        if (item != null) {

            holder.itemView.textViewCategory.text = item.categoryName

            holder.itemView.checkBoxCategory.isChecked =
                    (selectedCategory != null) && (position == findItemViewPositionAtSelectedItem(selectedCategory))

            holder.itemView.setOnClickListener {
                var releaseItemPosition = -1
                if (selectedCategory != null) {
                    releaseItemPosition = findItemViewPositionAtSelectedItem(selectedCategory)
                }

                if (releaseItemPosition != -1) {
                    notifyItemChanged(releaseItemPosition)
                }

                selectedCategory = item
                notifyItemChanged(position)
            }
        }
    }

    fun setItems(items: List<CategoryItem>) {
        this.items.clear()

        if (items != null) {
            this.items.addAll(items)

        }
    }

    /**
     * 선택된 아이템의 List Position 을 획득
     */
    private fun findItemViewPositionAtSelectedItem(item: CategoryItem?): Int {
        if (item != null) {
            for (i in 0 until items.size) {
                if (items[i] == item) {
                    return i
                }
            }
        }

        return -1
    }

    private fun getItem(position: Int): CategoryItem? = if (position in 0 until items.size) items[position] else null

    /**
     * 선택된 카테고리를 반환
     */
    fun getSelectedCategory(): CategoryItem? = selectedCategory
}