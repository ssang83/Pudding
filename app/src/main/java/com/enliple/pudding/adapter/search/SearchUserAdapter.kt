package com.enliple.pudding.adapter.search

import android.os.Build
import androidx.recyclerview.widget.RecyclerView
import android.text.Html
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.enliple.pudding.R
import com.enliple.pudding.commons.app.ImageLoad
import com.enliple.pudding.commons.app.StringUtils
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.vo.API31
import com.enliple.pudding.commons.widget.toast.AppToast
import kotlinx.android.synthetic.main.adapter_search_user_item.view.*

/**
 * Created by Kim Joonsung on 2018-11-26.
 */
class SearchUserAdapter : RecyclerView.Adapter<SearchUserAdapter.SearchUserHolder>() {

    private val items: MutableList<API31.UserItem> = mutableListOf()
    private var mListener: Listener? = null
    private var mTag = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchUserHolder {
        return SearchUserHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.adapter_search_user_item, parent, false))
    }

    override fun onBindViewHolder(holder: SearchUserHolder, position: Int) {
        val item = getItem(position)

        if (item != null) {
            var nickName = item.mb_nick
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val tag = "<font color='#9f56f2'>${mTag}</font>"
                nickName = nickName.replace(mTag, tag, true)

                holder.itemView.textViewName.text = Html.fromHtml(nickName, Html.FROM_HTML_MODE_LEGACY)
            } else {
                holder.itemView.textViewName.text = Html.fromHtml(nickName)
            }

            holder.itemView.textViewFollowerCount.text = StringUtils.getSnsStyleCountZeroBase(item.follow_cnt)
            holder.itemView.textViewLikeCount.text = StringUtils.getSnsStyleCountZeroBase(item.favor_cnt)

            if ("Y" == item.is_follow) {
                holder.itemView.buttonFollowing.isSelected = false
                holder.itemView.buttonFollowing.text = "팔로잉"
            } else {
                holder.itemView.buttonFollowing.isSelected = true
                holder.itemView.buttonFollowing.text = "팔로우"
            }

            holder.itemView.buttonFollowing.setOnClickListener {
                Logger.e("item,mb_id :: ${item.mb_id} , item.mb_nick :: ${item.mb_nick}")
                var user = AppPreferences.getUserId(holder.itemView.context!!)
                if ( user == item.mb_id ) {
                    AppToast(holder.itemView.context!!).showToastMessage("자기 자신은 팔로우 할 수 없습니다.",
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_BOTTOM)
                } else {
                    var followTemp = "N"
                    if ( "Y" == item.is_follow )
                        followTemp = "N"
                    else
                        followTemp = "Y"
                    mListener?.onFollowClicked(followTemp, item)
                }

            }

            if (TextUtils.isEmpty(item.mb_user_img)) {
                holder.itemView.imageViewThumbnail.setBackgroundResource(R.drawable.profile_default_img)
            } else {
                ImageLoad.setImage(
                        holder.itemView.context,
                        holder.itemView.imageViewThumbnail,
                        item.mb_user_img,
                        null,
                        ImageLoad.SCALE_CIRCLE_CROP,
                        DiskCacheStrategy.ALL)
            }

            holder.itemView.setOnClickListener {
                mListener?.onItemClicked(item)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun setListener(listener: Listener) {
        this.mListener = listener
    }

    fun setItems(list: List<API31.UserItem>) {
        Logger.e("SearchUserAdapter setItems")
        this.items.clear()
        if (list != null) this.items.addAll(list) else {
        }

        notifyDataSetChanged()
    }

    fun addItems(list:List<API31.UserItem>) {
        Logger.e("SearchUserAdapter addItems")
        if(list != null) {
            for(i in 0 until list.size) {
                this.items.add(list[i])
            }
        }

        notifyItemInserted(items.size - list.size)
    }

    fun setKeyword(keyword: String) {
        this.mTag = keyword
    }

    fun getItem(position: Int): API31.UserItem? = if (position in 0..items.size) items[position] else null

    class SearchUserHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    interface Listener {
        fun onItemClicked(item: API31.UserItem)
        fun onFollowClicked(status: String, item: API31.UserItem)
    }
}