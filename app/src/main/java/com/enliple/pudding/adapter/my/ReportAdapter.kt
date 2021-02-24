package com.enliple.pudding.adapter.my

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.enliple.pudding.R
import com.enliple.pudding.commons.network.vo.API6
import kotlinx.android.synthetic.main.adapter_report_list_item.view.*

/**
 * Created by Kim Joonsung on 2018-10-29.
 */
class ReportAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<ReportAdapter.ReportHolder>() {

    private val items: MutableList<API6.ReportItem> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportHolder {
        return ReportHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.adapter_report_list_item, parent, false))
    }

    override fun onBindViewHolder(holder: ReportHolder, position: Int) {
        val item = getItem(position)

        if (item != null) {
            if (item.re_status_YN == "N") {
                holder.itemView.textViewReportType.text = "신고"
                holder.itemView.textViewReportType.setBackgroundResource(R.drawable.bg_report)
            } else if(item.re_status_YN == "B")  {
                holder.itemView.textViewReportType.text = "보류"
                holder.itemView.textViewReportType.setBackgroundResource(R.drawable.bg_report_hold)
            } else {
                holder.itemView.textViewReportType.text = "처리\n완료"
                holder.itemView.textViewReportType.setBackgroundResource(R.drawable.bg_report_complete)
                holder.itemView.textViewReportReply.text = item.re_manager_reply

                val date = item.re_manager_reg_date.split(" ")
                holder.itemView.textViewReplyDate.text = date[0]
            }

            holder.itemView.textViewReportComment.text = item.strContent
            holder.itemView.textViewPerson.text = "${String.format(holder.itemView.context.getString(R.string.msg_my_setting_person_to_report),
                    item.strToUserId)}"
            holder.itemView.textViewReason.text = "${String.format(holder.itemView.context.getString(R.string.msg_my_setting_report_reason),
                    item.strReason)}"

            val regDate = item.dateReg.split(" ")
            holder.itemView.textViewDate.text = regDate[0]

            holder.itemView.setOnClickListener {
                if (item.re_status_YN == "Y") {
                    if(holder.itemView.layoutContent.isVisible) {
                        holder.itemView.layoutContent.visibility = View.GONE
                        holder.itemView.textViewReportComment.visibility = View.GONE
                        holder.itemView.layoutReply.visibility = View.GONE
                    } else {
                        holder.itemView.layoutContent.visibility = View.VISIBLE
                        holder.itemView.textViewReportComment.visibility = View.VISIBLE
                        holder.itemView.layoutReply.visibility = View.VISIBLE
                    }
                } else {
                    if(holder.itemView.layoutContent.isVisible) {
                        holder.itemView.layoutContent.visibility = View.GONE
                        holder.itemView.textViewReportComment.visibility = View.GONE
                        holder.itemView.layoutReply.visibility = View.GONE
                    } else {
                        holder.itemView.layoutContent.visibility = View.VISIBLE
                        holder.itemView.textViewReportComment.visibility = View.VISIBLE
                        holder.itemView.layoutReply.visibility = View.GONE
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun setItems(list: List<API6.ReportItem>) {
        this.items.clear()
        if (list != null) {
            this.items.addAll(list)
        }

        notifyDataSetChanged()
    }

    private fun getItem(position: Int): API6.ReportItem? = if (position in 0..items.size) items[position] else null

    class ReportHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)

}