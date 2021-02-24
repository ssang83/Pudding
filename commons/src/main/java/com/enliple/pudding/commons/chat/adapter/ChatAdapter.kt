package com.enliple.pudding.commons.chat.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.enliple.pudding.commons.R
import com.enliple.pudding.commons.chat.ChatManager
import com.enliple.pudding.commons.chat.dao.AdapterChatData
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import kotlinx.android.synthetic.main.recyclerview_chat_data_item.view.*
import org.json.JSONObject
import java.util.*

class ChatAdapter : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {
    companion object {
        // 채팅 메세지 히스토리 스택 사이즈
        private const val CHAT_EVENT_HISTORY_SIZE = 500

        /**
         * JSON 으로 구성된 사용자 ID 에서 닉네임을 추출
         */
        fun getNickName(jsonStr: String?): String? = if (TextUtils.isEmpty(jsonStr)) {
            null
        } else JSONObject(jsonStr).getString("nk")

        fun getUserName(jsonStr: String?): String? = if (TextUtils.isEmpty(jsonStr)) {
            null
        } else JSONObject(jsonStr).getString("id")
    }

    private val mChatList: MutableList<AdapterChatData> = ArrayList()
    private var mListener: Listener? = null
    private var mContext: Context? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        this.mContext = parent?.context
        return ChatViewHolder(LayoutInflater.from(mContext).inflate(R.layout.recyclerview_chat_data_item, parent, false))
    }

    override fun getItemCount(): Int = mChatList.size

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            Logger.e("onBindViewHolder item:$item")

            val nickName = getNickName(item.author)
            val userName = getUserName(item.author)
            Logger.e("cmd:${item.cmd} message:${item.text} nickName:$nickName userId:${AppPreferences.getUserId(mContext!!)}")

            if (ChatManager.NORMAL_CHAT_CMD == item.cmd) {
                holder.itemView.jellyLayer.visibility = View.GONE
                holder.itemView.normalLayer.visibility = View.VISIBLE
                if (!TextUtils.isEmpty(nickName)) {
                    //if (nickName.equals(AppPreferences.getUserId(mContext!!))) {
                    if (userName == AppPreferences.getUserId(mContext!!)) {
                        // 내가 보낸 톡인 경우
                        holder.itemView?.chetStrText.text = ""
                        val message = "$nickName ${item.text}"
                        val lastIndex = nickName!!.length
                        val sp = SpannableStringBuilder(message)
                        sp.setSpan(ForegroundColorSpan(Color.parseColor("#9f56f2")), 0, lastIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        holder.itemView?.chetStrText.append(sp)
                    } else {
                        holder.itemView?.chetStrText.text = ""
                        val message = "$nickName ${item.text}"
                        val lastIndex = nickName!!.length
                        val sp = SpannableStringBuilder(message)
                        sp.setSpan(ForegroundColorSpan(Color.parseColor("#5774f4")), 0, lastIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        holder.itemView?.chetStrText.append(sp)
                    }
                } else {
                    Logger.e("nickName empty")
                    holder.itemView.chetStrText.text = item.text
                }
            } else if (ChatManager.INQUIRE_CHAT_CMD == item.cmd) {
                holder.itemView.jellyLayer.visibility = View.VISIBLE
                holder.itemView.normalLayer.visibility = View.GONE
                holder.itemView?.jellyLayer.setBackgroundResource(R.drawable.listener_red_bg)
                var message = "$nickName ${item.text}"
                holder?.itemView?.chetStr.text = message
            } else if (ChatManager.COOKIE_ETC_CHAT_CMD == item.cmd) {
                holder.itemView.jellyLayer.visibility = View.VISIBLE
                holder.itemView.normalLayer.visibility = View.GONE
                var arr = item.text!!.split("|^|")
                var gubun = arr[0].replace(" ", "")
                when (gubun) {
                    ChatManager.GUBUN_COOKIE -> {
                        val receivedCnt = arr[2].toInt()
                        holder.itemView?.jellyLayer.setBackgroundResource(R.drawable.listener_red_bg)
                        holder?.itemView?.chetStr.text = "${nickName!!}님이 젤리 ${receivedCnt}개를 선물하셨습니다"
                    }

                    ChatManager.GUBUN_CART -> {
                        val font = R.font.notosanskr_bold
                        val typeface = ResourcesCompat.getFont(holder.itemView.context, font)
                        holder.itemView?.jellyLayer.setBackgroundResource(R.drawable.bg_cast_cart_add_common)
                        val sp = SpannableStringBuilder("${nickName}님이 장바구니에 담았습니다.").apply {
                            setSpan(StyleSpan(typeface!!.style), 0, nickName!!.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                            setSpan(StyleSpan(typeface.style), nickName!!.length+3, nickName!!.length+7, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        }
                        holder?.itemView?.chetStr.text = sp
                    }

                    ChatManager.GUBUN_BUY -> {
                        val font = R.font.notosanskr_bold
                        val typeface = ResourcesCompat.getFont(holder.itemView.context, font)
                        holder.itemView?.jellyLayer.setBackgroundResource(R.drawable.bg_cast_purchase_common)
                        val sp = SpannableStringBuilder("${nickName!!}님이 구매완료 했습니다.").apply {
                            setSpan(StyleSpan(typeface!!.style), 0, nickName!!.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                            setSpan(StyleSpan(typeface.style), nickName!!.length+3, nickName!!.length+7, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        }
                        holder?.itemView?.chetStr.text = sp
                    }

                    ChatManager.GUBUN_SYSTEM -> {
                        holder.itemView?.jellyLayer.setBackgroundResource(R.drawable.listener_red_bg)
                        holder?.itemView?.chetStr.text = "***${arr[1]}***\n${arr[2]}"
                    }
                }
            }
        }
    }

    fun setListener(listener: Listener?) {
        this.mListener = listener
    }

    fun getItem(position: Int): AdapterChatData? = if (position >= 0 || position < mChatList.size) {
        mChatList[position]
    } else null

    fun addItem(newItem: AdapterChatData) {
        if (itemCount == CHAT_EVENT_HISTORY_SIZE) {
            // 채팅 히스토리 표시 한도가 초과되면 LIFO 방식으로 밀어냄
            mChatList.removeAt(mChatList.size - 1)
            notifyItemRemoved(mChatList.size - 1)
        }

        mChatList.add(mChatList.size, newItem)
        notifyItemInserted(mChatList.size)
    }

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    interface Listener {
        fun onUserOptionClicked(user: String?)
    }
}