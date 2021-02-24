package com.enliple.pudding.adapter.home

import android.content.Context
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.enliple.pudding.R
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.widget.recyclerview.WrappedGridLayoutManager
import com.enliple.pudding.commons.widget.recyclerview.WrappedLinearLayoutManager
import com.enliple.pudding.model.ThreeCategoryItem
import java.util.*

/**
 * Created by Kim Joonsung on 2019-01-24.
 */
class CategoryAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder> {
    companion object {
        private const val TYPE_FIRST_CATEGORY = 0X1001
        private const val TYPE_SECOND_CATEGORY = 0X2001
        private const val TYPE_THIRD_CATEGORY = 0X3001
    }

    var itemFirstCategoryViewHolder: ItemFirstCategoryViewHolder? = null
    var itemSecondCategoryViewHolder: ItemSecondCategoryViewHolder? = null
    var itemThirdCategoryViewHolder: ItemThirdCategoryViewHolder? = null
    private val context: Context
    private var listener:Listener? = null
    private val categoryItems = ArrayList<ThreeCategoryItem>()
    private var selectedFirstCategory: String? = null
    private var selectedSecondCategory: String? = null
    private var selectedThirdCategory: String? = null

    interface Listener {
        fun onCategoryClicked(categoryId: String)
    }

    public fun setListener(clickListener: Listener) {
        listener = clickListener
    }

    constructor(context: Context) {
        this.context = context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == TYPE_FIRST_CATEGORY) {
            val view = LayoutInflater.from(context).inflate(R.layout.item_first_category, parent, false)
            itemFirstCategoryViewHolder = ItemFirstCategoryViewHolder(context, view)
            return itemFirstCategoryViewHolder!!
        } else if (viewType == TYPE_SECOND_CATEGORY) {
            val view = LayoutInflater.from(context).inflate(R.layout.item_second_category, parent, false)
            itemSecondCategoryViewHolder = ItemSecondCategoryViewHolder(context, view)
            return itemSecondCategoryViewHolder!!
        } else {
            val view = LayoutInflater.from(context).inflate(R.layout.item_third_category, parent, false)
            itemThirdCategoryViewHolder = ItemThirdCategoryViewHolder(context, view)
            return itemThirdCategoryViewHolder!!
        }
    }

    fun setCategoryItem(items: ArrayList<ThreeCategoryItem>) {
        categoryItems?.clear()
        Logger.e("items. size :: " + items.size)
        for (i in items.indices) {
            Logger.e("items. val :: " + items[i].categoryName)
        }
        categoryItems.addAll(items)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            TYPE_FIRST_CATEGORY
        } else if (position == 1) {
            TYPE_SECOND_CATEGORY
        } else {
            TYPE_THIRD_CATEGORY
        }
    }

    override fun getItemCount(): Int {
        return 3
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemFirstCategoryViewHolder)
            bindFirstCategoryViewHolder(holder as ItemFirstCategoryViewHolder)
        else if (holder is ItemSecondCategoryViewHolder)
            bindSecondCategoryViewHolder(holder as ItemSecondCategoryViewHolder)
        else
            bindThirdCategoryViewHolder(holder as ItemThirdCategoryViewHolder)
    }

    private fun bindFirstCategoryViewHolder(holder: ItemFirstCategoryViewHolder) {
        if (holder.firstAdapter != null) {
            Logger.e("holder.first adapter not null")
            Logger.e("categoryItems.size :: " + categoryItems.size)
            holder.firstAdapter!!.setItems(categoryItems)
        } else {
            Logger.e("holder.first adapter null")
        }
    }

    private fun bindSecondCategoryViewHolder(holder: ItemSecondCategoryViewHolder) {
        Logger.e("bindSecondCategoryViewHolder")
    }

    private fun bindThirdCategoryViewHolder(holder: ItemThirdCategoryViewHolder) {
        Logger.e("bindThirdCategoryViewHolder")
    }

    inner class ItemFirstCategoryViewHolder(context: Context, itemView: View) : RecyclerView.ViewHolder(itemView) {
        var recyclerViewFirstCategory: RecyclerView
        var firstAdapter: FirstCategoryAdapter? = null

        init {
            firstAdapter = FirstCategoryAdapter(context, FirstCategoryAdapter.FirstCategoryListener { categoryId ->
                if (categoryId == selectedFirstCategory)
                    return@FirstCategoryListener
                selectedFirstCategory = categoryId
                selectedSecondCategory = null
                selectedThirdCategory = null
                if (itemSecondCategoryViewHolder != null) {
                    itemSecondCategoryViewHolder!!.recyclerViewSecondCategory.scrollTo(0, 0)
                }
                if (itemThirdCategoryViewHolder != null) {
                    itemThirdCategoryViewHolder!!.recyclerViewThirdCategory.scrollTo(0, 0)
                }

                if (categoryId.isEmpty()) {
                    Logger.e("categoryEmpty")
                    if (itemSecondCategoryViewHolder != null) {
                        Logger.e("visible second gone f1 ")
                        itemSecondCategoryViewHolder!!.secondCategoryLayer.visibility = View.GONE
                        itemSecondCategoryViewHolder!!.recyclerViewSecondCategory.visibility = View.GONE
                    }


                    for (i in categoryItems.indices) {
                        Logger.e("categoryId :: $categoryId")
                        Logger.e("categoryId1 :: " + categoryItems[i].categoryId)
                        Logger.e("*********")
                        if (categoryId == categoryItems[i].categoryId) {
                            val arr = categoryItems[i].secondCategory
                            if (itemSecondCategoryViewHolder!!.secondAdapter != null) {
                                if (arr == null) {
                                    Logger.e("visible second gone f2 ")
                                    itemSecondCategoryViewHolder!!.secondCategoryLayer.visibility = View.GONE
                                    itemSecondCategoryViewHolder!!.recyclerViewSecondCategory.visibility = View.GONE
                                } else {
                                    Logger.e("visible second visible f2 ")
                                    itemSecondCategoryViewHolder!!.secondCategoryLayer.visibility = View.VISIBLE
                                    itemSecondCategoryViewHolder!!.recyclerViewSecondCategory.visibility = View.VISIBLE
                                    itemSecondCategoryViewHolder!!.secondAdapter!!.setItems(arr)
                                }

                                Handler().post {
                                    itemSecondCategoryViewHolder!!.recyclerViewSecondCategory.scrollToPosition(0)
                                    if (itemThirdCategoryViewHolder != null) {
                                        Logger.e("visible third gone f4 ")
                                        itemThirdCategoryViewHolder!!.thirdCategoryLayer.visibility = View.GONE
                                        itemThirdCategoryViewHolder!!.recyclerViewThirdCategory.visibility = View.GONE
                                        itemThirdCategoryViewHolder!!.f_line.visibility = View.GONE
                                        itemThirdCategoryViewHolder!!.s_line.visibility = View.GONE
                                    }
                                }
                            }

                        }
                    }
                } else {
                    Logger.e("firstCategory clilcked not empty")

                    for (i in categoryItems.indices) {
                        Logger.e("categoryId :: $categoryId")
                        Logger.e("categoryId1 :: " + categoryItems[i].categoryId)
                        Logger.e("*********")
                        if (categoryId == categoryItems[i].categoryId) {
                            val arr = categoryItems[i].secondCategory
                            if (itemSecondCategoryViewHolder!!.secondAdapter != null) {
                                if (arr == null) {
                                    Logger.e("visible second gone f3 ")
                                    itemSecondCategoryViewHolder!!.secondCategoryLayer.visibility = View.GONE
                                    itemSecondCategoryViewHolder!!.recyclerViewSecondCategory.visibility = View.GONE
                                } else {
                                    Logger.e("visible second visible f3 ")
                                    itemSecondCategoryViewHolder!!.secondCategoryLayer.visibility = View.VISIBLE
                                    itemSecondCategoryViewHolder!!.recyclerViewSecondCategory.visibility = View.VISIBLE
                                    itemSecondCategoryViewHolder!!.secondAdapter!!.setItems(arr)
                                }

                                Handler().post {
                                    itemSecondCategoryViewHolder!!.recyclerViewSecondCategory.scrollToPosition(0)
                                    if (itemThirdCategoryViewHolder != null) {
                                        Logger.e("visible third gone f4 ")
                                        itemThirdCategoryViewHolder!!.thirdCategoryLayer.visibility = View.GONE
                                        itemThirdCategoryViewHolder!!.recyclerViewThirdCategory.visibility = View.GONE
                                        itemThirdCategoryViewHolder!!.f_line.visibility = View.GONE
                                        itemThirdCategoryViewHolder!!.s_line.visibility = View.GONE
                                    }
                                }
                            }
                        }
                    }
                }

                if (listener != null) {
                    listener!!.onCategoryClicked(categoryId)
                }
                notifyDataSetChanged()
            })
            recyclerViewFirstCategory = itemView.findViewById(R.id.recyclerViewFirstCategory)
            val layoutManager = WrappedLinearLayoutManager(context)
            layoutManager.orientation = LinearLayoutManager.HORIZONTAL
            recyclerViewFirstCategory.setHasFixedSize(false)
            recyclerViewFirstCategory.layoutManager = layoutManager
            recyclerViewFirstCategory.adapter = firstAdapter
        }
    }

    inner class ItemSecondCategoryViewHolder(context: Context, itemView: View) : RecyclerView.ViewHolder(itemView) {
        var recyclerViewSecondCategory: RecyclerView
        var secondAdapter: SecondCategoryAdapter? = null
        var secondCategoryLayer: LinearLayout

        init {
            secondAdapter = SecondCategoryAdapter(context, SecondCategoryAdapter.SecondCategoryListener { categoryId, categoryName ->
                if (categoryId == selectedSecondCategory)
                    return@SecondCategoryListener
                selectedSecondCategory = categoryId
                selectedThirdCategory = null
                if (itemThirdCategoryViewHolder != null) {
                    itemThirdCategoryViewHolder!!.recyclerViewThirdCategory.scrollTo(0, 0)
                }

                if ("전체" == categoryName) {
                    if (itemThirdCategoryViewHolder != null) {
                        Logger.e("visible third gone s1 ")
                        itemThirdCategoryViewHolder!!.thirdCategoryLayer.visibility = View.GONE
                        itemThirdCategoryViewHolder!!.recyclerViewThirdCategory.visibility = View.GONE
                        itemThirdCategoryViewHolder!!.f_line.visibility = View.GONE
                        itemThirdCategoryViewHolder!!.s_line.visibility = View.GONE
                    }

                } else {
                    if (itemThirdCategoryViewHolder != null) {
                        Logger.e("visible third visible s1 ")
                        itemThirdCategoryViewHolder!!.thirdCategoryLayer.visibility = View.VISIBLE
                        itemThirdCategoryViewHolder!!.recyclerViewThirdCategory.visibility = View.VISIBLE
                        itemThirdCategoryViewHolder!!.f_line.visibility = View.VISIBLE
                        itemThirdCategoryViewHolder!!.s_line.visibility = View.VISIBLE
                    }


                    for (i in categoryItems.indices) {
                        val arr = categoryItems[i].secondCategory
                        if (arr != null && arr.size > 0) {
                            for (j in arr.indices) {
                                val secondCategory = arr[j].categoryId
                                if (categoryId == secondCategory) {
                                    val thirdArray = arr[j].thirdCategory
                                    if (thirdArray == null) {
                                        if (itemThirdCategoryViewHolder != null) {
                                            Logger.e("visible third gone s2 ")
                                            itemThirdCategoryViewHolder!!.thirdCategoryLayer.visibility = View.GONE
                                            itemThirdCategoryViewHolder!!.recyclerViewThirdCategory.visibility = View.GONE
                                            itemThirdCategoryViewHolder!!.f_line.visibility = View.GONE
                                            itemThirdCategoryViewHolder!!.s_line.visibility = View.GONE
                                        }
                                    } else {
                                        if (itemThirdCategoryViewHolder!!.thirdAdapter != null)
                                            itemThirdCategoryViewHolder!!.thirdAdapter!!.setItems(thirdArray)
                                    }
                                }
                            }
                        }
                    }
                }

                if (listener != null) {
                    listener!!.onCategoryClicked(categoryId)
                }
            })
            secondCategoryLayer = itemView.findViewById(R.id.secondCategoryLayer)
            recyclerViewSecondCategory = itemView.findViewById(R.id.recyclerViewSecondCategory)
            val layoutManager = WrappedLinearLayoutManager(context)
            layoutManager.orientation = LinearLayoutManager.HORIZONTAL
            recyclerViewSecondCategory.setHasFixedSize(false)
            recyclerViewSecondCategory.layoutManager = layoutManager
            recyclerViewSecondCategory.adapter = secondAdapter
        }
    }

    inner class ItemThirdCategoryViewHolder(context: Context, itemView: View) : RecyclerView.ViewHolder(itemView) {
        var thirdCategoryLayer: LinearLayout
        var recyclerViewThirdCategory: RecyclerView
        var f_line: View
        var s_line: View
        var thirdAdapter: ThirdCategoryAdapter? = null

        init {
            thirdAdapter = ThirdCategoryAdapter(context, ThirdCategoryAdapter.ThirdCategoryListener { categoryId ->
                if (categoryId == selectedThirdCategory)
                    return@ThirdCategoryListener
                selectedThirdCategory = categoryId
                if (listener != null) {
                    listener!!.onCategoryClicked(categoryId)
                }
            })
            thirdCategoryLayer = itemView.findViewById(R.id.thirdCategoryLayer)
            f_line = itemView.findViewById(R.id.f_line)
            s_line = itemView.findViewById(R.id.s_line)
            recyclerViewThirdCategory = itemView.findViewById(R.id.recyclerViewThirdCategory)
            val layoutManager = WrappedGridLayoutManager(context, 2)
            layoutManager.orientation = LinearLayoutManager.VERTICAL
            recyclerViewThirdCategory.setHasFixedSize(false)
            recyclerViewThirdCategory.layoutManager = layoutManager
            recyclerViewThirdCategory.adapter = thirdAdapter
        }
    }

    /**
    private val items: MutableList<API81.CategoryItem> = mutableListOf()
    private var mListener: Listener? = null
    private var selectedIndex = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryHolder {
        return CategoryHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_category_item, parent, false))
    }

    override fun onBindViewHolder(holder: CategoryHolder, position: Int) {
        items[position].let { item ->
            if (position == 0) {
                holder.itemView.textViewCategory.text = "전체"
                holder.itemView.imageViewSelect.setBackgroundResource(R.drawable.category_all_off)
                holder.itemView.imageViewCategory.visibility = View.GONE
            } else {
                holder.itemView.textViewCategory.text = item.categoryName
                holder.itemView.imageViewSelect.setBackgroundResource(0)
                holder.itemView.imageViewCategory.visibility = View.VISIBLE

                ImageLoad.setImage(
                        holder.itemView.context,
                        holder.itemView.imageViewCategory,
                        item.categoryImage,
                        null,
                        ImageLoad.SCALE_NONE,
                        DiskCacheStrategy.ALL)
            }

            holder.itemView.setOnClickListener {
                mListener?.onCategoryClicked(item.categoryId)

                selectedIndex = position
                notifyDataSetChanged()
            }
        }

        if (selectedIndex == position) {
            holder.itemView.imageViewSelect.setImageResource(R.drawable.bg_home_category_circle)
            holder.itemView.textViewCategory.isEnabled = true
        } else {
            holder.itemView.imageViewSelect.setImageResource(0)
            holder.itemView.textViewCategory.isEnabled = false
        }
    }

    override fun getItemCount(): Int = items.size

    fun setListener(listener: Listener) {
        this.mListener = listener
    }

    fun setItems(list: List<API81.CategoryItem>) {
        this.items.clear()
        this.items.addAll(list)

        notifyDataSetChanged()
    }

    inner class CategoryHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)

    interface Listener {
        fun onCategoryClicked(categoryId: String)
    }
    **/
}