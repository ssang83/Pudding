package com.enliple.pudding.adapter.my

import android.app.Activity
import android.graphics.Color
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.enliple.pudding.R
import com.enliple.pudding.commons.app.ImageLoad
import com.enliple.pudding.commons.events.OnSingleClickListener
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.vo.API78
import kotlinx.android.synthetic.main.adapter_follower_item.view.*

/**
 * Created by Kim Joonsung on 2018-10-24.
 */
class FollowerAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<FollowerAdapter.FollowerHolder> {

    private val items: MutableList<API78.FollowItem> = ArrayList()
    private var mListener: Listener? = null
    private var mType: String? = null
    constructor(context: Activity, type: String) : super() {
        mType = type;
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FollowerHolder {
        return FollowerHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.adapter_follower_item, parent, false))
    }

    override fun onBindViewHolder(holder: FollowerHolder, position: Int) {
        val item = getItem(position)

        if (item != null) {
            Logger.e("mType :: " + mType)
            if ( "1".equals(mType) ) {
                holder.itemView.textViewID.text = item.strUserId
            } else {
                holder.itemView.textViewID.text = item.strToUserId
            }

            holder.itemView.textViewNickName.text = item.strUserNickname
            // Y : 맞팔 즉 나와 상대가 같이 팔로우 상태 , N : 상대는 나를 FOLLOW 나는 상대를 FOLLOW 안한상태
            if ( "Y".equals(item.isFollow_each_other) ) {
                holder.itemView.buttonFollowing.setBackgroundResource(R.drawable.bg_following)
                holder.itemView.buttonFollowing.setTextColor(Color.parseColor("#8192a5"))
                holder.itemView.buttonFollowing.text = "팔로잉"
            } else {
                holder.itemView.buttonFollowing.setBackgroundResource(R.drawable.bg_follow)
                holder.itemView.buttonFollowing.setTextColor(Color.parseColor("#ffffff"))
                holder.itemView.buttonFollowing.text = "팔로우"
            }

            ImageLoad.setImage(holder.itemView.context, holder.itemView.imageViewThumbnail, item.strThumbnail, null, ImageLoad.SCALE_CIRCLE_CROP, DiskCacheStrategy.ALL)

            holder.itemView.buttonFollowing.setOnClickListener {
                var userId = AppPreferences.getUserId(holder.itemView.context)
                if ( "1".equals(mType) && (userId == item.strUserId)) {

                } else {
                    var sId = ""
                    if ( "1".equals(mType) ) {
                        sId = item.strUserId
                    } else {
                        sId = item.strToUserId
                    }

                    if (holder.itemView.buttonFollowing.text.toString().equals("팔로잉")) {
                        mListener?.onFollowStatusChanged(sId, "N", item)
                    } else {
                        mListener?.onFollowStatusChanged(sId, "Y", item)
                    }
                }
            }

            holder.itemView.imageViewThumbnail.setOnClickListener(object: OnSingleClickListener(){
                override fun onSingleClick(v: View?) {
                    mListener?.goProfile(item)
                }
            })
        }
    }

    override fun getItemCount(): Int = items.size

    fun setItems(list: List<API78.FollowItem>) {
        this.items.clear()
        if (list != null) {
            this.items.addAll(list)
        }

        notifyDataSetChanged()
    }

    fun removeItems() {
        if ( this.items != null ) {
            this.items.clear()
            notifyDataSetChanged()
        }
    }

    private fun getItem(position: Int): API78.FollowItem? = if (position in 0 until items.size) items[position] else null

    class FollowerHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)

    fun setListener(listener: Listener) {
        mListener = listener
    }

    interface Listener {
        fun onFollowStatusChanged(toUserId:String, isFollow:String, item:API78.FollowItem)
        fun goProfile(item:API78.FollowItem)
    }
}