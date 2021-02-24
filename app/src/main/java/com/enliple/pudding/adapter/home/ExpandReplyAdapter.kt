package com.enliple.pudding.adapter.home

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ExpandableListView
import android.widget.ListAdapter
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.enliple.pudding.R
import com.enliple.pudding.commons.app.ImageLoad
import com.enliple.pudding.commons.app.Utils
import com.enliple.pudding.commons.events.OnSingleClickListener
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.vo.API17
import com.enliple.pudding.widget.main.NickNameModifyDialog

/**
 * Created by Kim Joonsung on 2019-06-11.
 */
class ExpandReplyAdapter : BaseExpandableListAdapter {

    private var mItems: MutableList<API17.BroadcastReplyItem> = mutableListOf()
    private var mContext: Context? = null
    private var mListener: OnPopupPositionListener? = null
    private var mCreatorNickname = ""

    constructor(context: Context) : super() {
        this.mContext = context
    }

    override fun getGroup(groupPosition: Int) = mItems[groupPosition]

    override fun getGroupCount() = mItems.size

    override fun getGroupId(groupPosition: Int) = groupPosition.toLong()

    override fun getChild(groupPosition: Int, childPosition: Int) = mItems[groupPosition].child[childPosition]

    override fun getChildId(groupPosition: Int, childPosition: Int) = childPosition.toLong()

    override fun getChildrenCount(groupPosition: Int) = mItems[groupPosition].child_cnt.toInt()

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        if(view == null) {
            val layoutInflater = mContext!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = layoutInflater.inflate(R.layout.reply_child_item, null)
        }

        val imageViewThumbNail = view!!.findViewById<AppCompatImageView>(R.id.imageViewThumbNail)
        val textViewNickName = view.findViewById<AppCompatTextView>(R.id.textViewNickName)
        val textViewContent = view.findViewById<AppCompatTextView>(R.id.textViewContent)
        val textViewReplyDate = view.findViewById<AppCompatTextView>(R.id.textViewReplyDate)
        val buttonMore = view.findViewById<RelativeLayout>(R.id.buttonMore)
        val contentLayer = view.findViewById<View>(R.id.contentLayer)
        val deleteLayer = view.findViewById<View>(R.id.deleteLayer)

        val item = getChild(groupPosition, childPosition)

        if("N" == item.is_use) {
            deleteLayer.visibility = View.VISIBLE
            contentLayer.visibility = View.GONE
        } else {
            deleteLayer.visibility = View.GONE
            contentLayer.visibility = View.VISIBLE

            ImageLoad.setImage(
                    mContext,
                    imageViewThumbNail,
                    item!!.mb_user_img,
                    null,
                    ImageLoad.SCALE_CIRCLE_CROP,
                    DiskCacheStrategy.ALL)

            if(mCreatorNickname == item.mb_nick) {
                textViewNickName.setTextColor(Color.parseColor("#9f56f2"))
                imageViewThumbNail.setBackgroundResource(R.drawable.bg_reply_child_profile)
            } else {
                textViewNickName.setTextColor(Color.parseColor("#192028"))
                imageViewThumbNail.setBackgroundResource(R.drawable.bg_reply_thumbnail)
            }

            textViewNickName.text = item.mb_nick
            textViewContent.text = item.comment
            textViewReplyDate.text = item.reg_date

            if("Y" == item.is_mine) {
                buttonMore.visibility = View.VISIBLE
            } else {
                buttonMore.visibility = View.GONE
            }

            buttonMore.setOnClickListener {
                val viewHeight = buttonMore.height // imgMore 이미지의 height
                val originalPos = IntArray(2)
                buttonMore.getLocationInWindow(originalPos)
                var yPosition: Int = originalPos[1] // more 이미지의 y좌표 ( dialog의 시작점 기준 )
                var topMargin = yPosition + (viewHeight / 2) - Utils.ConvertDpToPx(mContext, 49)
                if (mListener != null) {
                    mListener!!.getChildPopupStartY(topMargin, item, groupPosition, childPosition)
                }
            }
        }

        return view
    }

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        if(view == null) {
            val layoutInflater = mContext!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = layoutInflater.inflate(R.layout.reply_group_item, null)
        }

        (parent as ExpandableListView).expandGroup(groupPosition)

        val imageViewThumbNail = view!!.findViewById<AppCompatImageView>(R.id.imageViewThumbNail)
        val textViewNickName = view.findViewById<AppCompatTextView>(R.id.textViewNickName)
        val textViewContent = view.findViewById<AppCompatTextView>(R.id.textViewContent)
        val textViewReplyDate = view.findViewById<AppCompatTextView>(R.id.textViewReplyDate)
        val replyButton = view.findViewById<AppCompatTextView>(R.id.replyButton)
        val buttonMore = view.findViewById<RelativeLayout>(R.id.buttonMore)
        val contentLayer = view.findViewById<View>(R.id.contentLayer)
        val deleteLayer = view.findViewById<View>(R.id.deleteLayer)

        val item = getGroup(groupPosition)

        if("N" == item.is_use) {
            deleteLayer.visibility = View.VISIBLE
            contentLayer.visibility = View.GONE
        }  else {
            deleteLayer.visibility = View.GONE
            contentLayer.visibility = View.VISIBLE

            ImageLoad.setImage(
                    mContext,
                    imageViewThumbNail,
                    item!!.mb_user_img,
                    null,
                    ImageLoad.SCALE_CIRCLE_CROP,
                    DiskCacheStrategy.ALL)

            textViewNickName.text = item.mb_nick
            textViewContent.text = item.comment
            textViewReplyDate.text = item.reg_date

            if("Y" == item.is_mine) {
                buttonMore.visibility = View.VISIBLE
            } else {
                buttonMore.visibility = View.GONE
            }

            buttonMore.setOnClickListener {
                val viewHeight = buttonMore.height // imgMore 이미지의 height
                val originalPos = IntArray(2)
                buttonMore.getLocationInWindow(originalPos)
                var yPosition: Int = originalPos[1] // more 이미지의 y좌표 ( dialog의 시작점 기준 )
                var topMargin = yPosition + (viewHeight / 2) - Utils.ConvertDpToPx(mContext, 49)
                if (mListener != null) {
                    mListener!!.getPopupStartY(topMargin, item, groupPosition)
                }
            }

            replyButton.setOnClickListener(object:OnSingleClickListener() {
                override fun onSingleClick(v: View?) {
                    mListener?.onReplyClicked(item, groupPosition)
                }
            })
        }

        return view
    }

    override fun hasStableIds() = false

    override fun isChildSelectable(groupPosition: Int, childPosition: Int) = true

    fun setItems(items:List<API17.BroadcastReplyItem>) {
        this.mItems.clear()
        this.mItems.addAll(items)

        notifyDataSetChanged()
    }

    fun setListener(listener : OnPopupPositionListener) {
        this.mListener = listener
    }

    fun delete(position: Int) {
        Logger.e("delete")
        mListener?.onReplyDel(mItems[position].idx)
    }

    fun setCreatorNickName(nickName:String) {
        this.mCreatorNickname = nickName
    }

    interface OnPopupPositionListener {
        fun getPopupStartY(y: Int, item: API17.BroadcastReplyItem, position: Int)
        fun getChildPopupStartY(y: Int, item: API17.BroadcastReplyItem.ChildReplyItem, gropPos:Int,childPos: Int)
        fun getGroupCount(count: Int)
        fun onReplyDel(idx: String)
        fun onReplyClicked(item: API17.BroadcastReplyItem, position: Int)
    }
}