package com.enliple.pudding.shoppingcaster.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.enliple.pudding.commons.data.CategoryItem
import com.enliple.pudding.R
import com.enliple.pudding.commons.log.Logger
import kotlinx.android.synthetic.main.adapter_caster_category_item_second.view.*
import java.util.*

/**
 * 방송 시작전 두번째 카테고리 선택 Adapter
 * @author hkcha
 * @since 2018.08.23
 */
class SecondCategorySelectAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<CategoryViewHolder>() {

    companion object {
        private const val SELECT_ITEM_MAXIMUM_COUNT = 3
    }

    private val items = ArrayList<CategoryItem>()
    private val selectedCategoryList = Collections.synchronizedList(ArrayList<CategoryItem>())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        return CategoryViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.adapter_caster_category_item_second, parent, false))
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            holder.itemView.checkBoxCategory.isChecked = (isSelectedFortItem(item) == true)
            holder.itemView.textViewCategory.text = item.categoryName

            holder.itemView.checkBoxCategory.setOnCheckedChangeListener { _, isChecked ->
                Logger.e("second category checked change listener called")
                var item = getItem(position)

                if (item != null) {
                    if (selectedCategoryList.size < SELECT_ITEM_MAXIMUM_COUNT
                            && !isSelectedFortItem(item!!)
                            && isChecked) {
                        selectedCategoryList.add(item)
                    } else {
                        selectedCategoryList.remove(item)
                    }
                }
            }

            holder.itemView.setOnClickListener {
                Logger.e("second category clicked")
                if (selectedCategoryList.size < SELECT_ITEM_MAXIMUM_COUNT
                        && !isSelectedFortItem(item)) {
                    // 기존에 선택되지 않은 경우 선택 개수가 3개 이하 일때에만 추가처리
                    holder.itemView.checkBoxCategory.isChecked = true
                } else if (isSelectedFortItem(item)) {
                    // 기존에 선택된 경우 선택 취소 처리
                    holder.itemView.checkBoxCategory.isChecked = false
                }
            }
        }
    }

    fun setItems(items: ArrayList<CategoryItem>) {
        Logger.e("second category setItems")
        this.items.clear()
        this.selectedCategoryList.clear()

        if (items != null) {
            this.items.addAll(items)
        }

        notifyDataSetChanged()
    }

    fun clearItems() {
        this.items.clear()
        notifyDataSetChanged()
    }

    /**
     * 선택되어 있는 2차 카테고리를 사용자 UI에 의한 취소 처리
     */
    fun removeSelectedCategory(item: CategoryItem) {
        Logger.e("second category removeSelectedCategory")
        selectedCategoryList.remove(item)
        notifyDataSetChanged()
    }

    /**
     * 해당 카테고리 아이템이 선택된 상태인지 확인
     * @param item
     * @return
     */
    @Synchronized
    private fun isSelectedFortItem(item: CategoryItem): Boolean {
        if (selectedCategoryList.size > 0) {
            for (itemData in selectedCategoryList) {
                if (item == itemData) {
                    return true
                }
            }
        }

        return false
    }

    /**
     * 선택된 아이템의 List Position 을 획득
     */
    @Synchronized
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
    fun getSelectedCategory(): ArrayList<CategoryItem> = ArrayList(selectedCategoryList)

    fun setSelectedCategory(list: ArrayList<CategoryItem>) {
        selectedCategoryList.addAll(0, list)
    }
}