package com.enliple.pudding.commons.shoppingcommons.adapter

import android.graphics.Color
import androidx.recyclerview.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.enliple.pudding.commons.R
import com.enliple.pudding.commons.log.Logger
import kotlinx.android.synthetic.main.adapter_user_list.view.*
import net.grandcentrix.tray.AppPreferences
import org.json.JSONObject
import java.util.*

/**
 * 사용자 리스트 팝업에 List Adapter
 * @author hkcha
 * @since 2018.08.27
 */
class LiveUserListAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<LiveUserListAdapter.UserListHolder>() {

    private val userList:MutableList<String> = mutableListOf()

    companion object {
        private const val ODD_ITEM_BACKGROUND = 0xffffff.toInt()
        private const val EVEN_ITEM_BACKGROUND = 0xFFf3f8fd.toInt()

        /**
         * JSON 으로 구성된 사용자 ID 에서 닉네임을 추출
         */
        fun getNickName(jsonStr: String?): String? = if (TextUtils.isEmpty(jsonStr)) {
            null
        } else JSONObject(jsonStr)?.getString("nk")

        /**
         * JSON 으로 구성된 사용자 ID 에서 서비스 ID 를 추출
         */
        fun getAccount(jsonStr: String?): String? = if (TextUtils.isEmpty(jsonStr)) {
            null
        } else JSONObject(jsonStr)?.getString("id")

        /**
         * 사용자의 아이디를 전체 길이의 3분의 2만 표시하도록 설정
         */
        fun getInvisibleAccountStr(account: String): String {
            var result = ""

            if (!TextUtils.isEmpty(account)) {

                var visibleCharCount = (account.length.toFloat() / 2.5f).toInt()

                for (i in 0 until account.length) {
                    result += if (i < visibleCharCount) {
                        account[i]
                    } else {
                        "*"
                    }
                }
            }

            return result
        }
    }

    private val items: MutableList<String> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserListHolder =
            UserListHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_user_list, parent, false))

    // 기획대로 사용자 수는 최대 1000명만 표시하도록 설정
    override fun getItemCount(): Int = if (items.size > 1000) 1000 else items.size

    private fun getItemByPosition(position: Int): String? = if (position in 0 until items.size) items[position] else null

    override fun onBindViewHolder(holder: UserListHolder, position: Int) {
        val item = getItemByPosition(position)

        // Background 설정
        if (position % 2 == 0) {
            holder.itemView.setBackgroundColor(EVEN_ITEM_BACKGROUND)
        } else {
            holder.itemView.setBackgroundColor(ODD_ITEM_BACKGROUND)
        }

        if (item != null) {
            var userId = getAccount(item)
            var nickName = getNickName(item)

            if(userId == com.enliple.pudding.commons.internal.AppPreferences.getUserId(holder.itemView.context)) {
                holder.itemView.textViewNickName.setTextColor(Color.parseColor("#ff6c6c"))
                holder.itemView.textViewAccount.setTextColor(Color.parseColor("#ff6c6c"))
                holder.itemView.textViewIdLeftGuide.setTextColor(Color.parseColor("#ff6c6c"))
                holder.itemView.textViewIdRightGuide.setTextColor(Color.parseColor("#ff6c6c"))
            } else {
                holder.itemView.textViewNickName.setTextColor(Color.parseColor("#192028"))
                holder.itemView.textViewAccount.setTextColor(Color.parseColor("#192028"))
                holder.itemView.textViewIdLeftGuide.setTextColor(Color.parseColor("#192028"))
                holder.itemView.textViewIdRightGuide.setTextColor(Color.parseColor("#192028"))
            }

            if (!TextUtils.isEmpty(nickName)) {
                holder.itemView.textViewNickName.text = nickName!!
            }

            if (!TextUtils.isEmpty(userId)) {
                holder.itemView.textViewAccount.text = getInvisibleAccountStr(userId!!)
            }
        }
    }

    fun setItems(items: List<String>) {
        this.items.clear()
        this.userList.clear()

        if (items != null) {
            // 닉네임을 기준으로 정렬
            var sortedList = items.sortedWith(compareBy { getNickName(it) })
            this.items.addAll(sortedList)
            this.userList.addAll(sortedList)
        }

        notifyDataSetChanged()
    }

    fun getFilter(charText:String) {
        var text = charText.toLowerCase(Locale.getDefault())

        items.clear()
        if(text.length == 0) {
            items.addAll(userList)
        } else {
            for(i in 0 until userList.size) {
                val nickName = getNickName(userList[i])
                if(nickName!!.contains(charText)) {
                    items.add(userList[i])
                }
            }
        }

        notifyDataSetChanged()
    }

    class UserListHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)
}