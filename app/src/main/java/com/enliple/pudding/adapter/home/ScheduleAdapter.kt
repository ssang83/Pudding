package com.enliple.pudding.adapter.home

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.enliple.pudding.R
import com.enliple.pudding.activity.LoginActivity
import com.enliple.pudding.commons.app.*
import com.enliple.pudding.commons.events.OnSingleClickListener
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.vo.API129
import com.enliple.pudding.commons.network.vo.API81
import com.enliple.pudding.widget.ScheduleProduct
import kotlinx.android.synthetic.main.adapter_schedule_item.view.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by jhs
 */
class ScheduleAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder> {
    private val mItems: MutableList<API129.Schedule> = mutableListOf()
    private var mListener: Listener? = null
    private var mToday = ""
    private var mDay = ""
    private var mMonth = ""
    private var currentMonth = ""

    private lateinit var mContext: Context

    constructor(context: Context) : super() {
        if (context != null) {
            mContext = context
        }

        SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(Date(System.currentTimeMillis())).let {
            currentMonth = it.split("-")[1]
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ItemHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_schedule_item, parent, false))

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        onBindItem(holder as ItemHolder, position)
    }

    override fun getItemViewType(position: Int) = 0

    override fun getItemCount(): Int = mItems.size

    fun setListener(listener: Listener) {
        this.mListener = listener
    }

    fun setItems(list: List<API129.Schedule>) {
        this.mItems.clear()
        this.mItems.addAll(list)

        notifyDataSetChanged()
    }

    fun addItems(list: List<API129.Schedule>) {
        if(list.isNotEmpty()) {
            for(i in 0 until list.size) {
                this.mItems.add(list[i])
            }

            notifyItemInserted(mItems.size - list.size)
        }
    }

    fun setAlarmCount(count:String, holder: ScheduleHolder?) {
        if ( holder != null )
            holder.alarmCount.text = "${count}명"
    }

    fun setScheduleDay(today:String, day:String, month:String) {
        this.mToday = today
        this.mDay = day
        this.mMonth = month
    }

    private fun onBindItem(holder: ItemHolder, position: Int) {
        Logger.d("onBindItem $position")

        if(!mItems[position].time.isNullOrEmpty()) {

            var params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
            var topMargin = 0
            if ( position == 0 )
                topMargin = Utils.ConvertDpToPx(holder.itemView.context, 15)
            else
                topMargin = 0
            params.setMargins(0, topMargin, 0, 0)
            params.addRule(RelativeLayout.CENTER_HORIZONTAL)
            holder.itemView.clock.setLayoutParams(params)

            holder.itemView.clock.text =
                    if(mItems[position].time.toInt() < 12) {
                        "오전 ${mItems[position].time}시 - ${mItems[position].cnt}개"
                    } else if(mItems[position].time.toInt() == 12) {
                        "오후 12시 - ${mItems[position].cnt}개"
                    } else {
                        val time = mItems[position].time.toInt() - 12
                        "오후 ${time}시 - ${mItems[position].cnt}개"
                    }
        }

        if(mMonth.toInt() > currentMonth.toInt()) {
            holder.itemView.clock.setBackgroundResource(R.drawable.bg_schedule_date_round_blue)
        } else {
            if(mToday.isNotEmpty() && mDay.isNotEmpty()) {
                if(mToday.toInt() == mDay.toInt()) {
                    if(mItems[position].time.toInt() == TimeUtils.getHourStringOnlyDaysFromTimeMills(System.currentTimeMillis()).toInt()) {
                        holder.itemView.clock.setBackgroundResource(R.drawable.bg_schedule_date_round)
                    } else if(mItems[position].time.toInt() < TimeUtils.getHourStringOnlyDaysFromTimeMills(System.currentTimeMillis()).toInt()) {
                        holder.itemView.clock.setBackgroundResource(R.drawable.bg_schedule_date_round_gray)
                    } else {
                        holder.itemView.clock.setBackgroundResource(R.drawable.bg_schedule_date_round_blue)
                    }
                } else if(mToday.toInt() > mDay.toInt()) {
                    holder.itemView.clock.setBackgroundResource(R.drawable.bg_schedule_date_round_gray)
                } else {
                    holder.itemView.clock.setBackgroundResource(R.drawable.bg_schedule_date_round_blue)
                }
            }
        }

        holder.itemView.layoutScheduleContainer.removeAllViews()
        if(mItems[position].cnt.toInt() > 0) {
            for(i in 0 until mItems[position].cnt.toInt()) {
                val item = mItems[position].list[i]
                Logger.e("**********************")
                Logger.e("item val1 :: ${item.is_mine}")
                val itemHolder = ScheduleHolder(holder.itemView.context, holder.itemView.layoutScheduleContainer)

                ImageLoad.setImage(
                        holder.itemView.context,
                        itemHolder.thumbnail,
                        item.live_img,
                        null,
                        ImageLoad.SCALE_NONE,
                        DiskCacheStrategy.ALL)

                val d = mContext.getDrawable(R.drawable.bg_rounding_image) as GradientDrawable
                itemHolder.thumbnail.background = d
                itemHolder.thumbnail.clipToOutline = true

                ImageLoad.setImage(
                        holder.itemView.context,
                        itemHolder.profile,
                        item.mb_user_img,
                        null,
                        ImageLoad.SCALE_CIRCLE_CROP,
                        DiskCacheStrategy.ALL)

                itemHolder.title.text = item.title
                itemHolder.date.text = "${item.fo_hour}:00"
                itemHolder.alarmCount.text = "${item.reservation_cnt}명"
                itemHolder.casterName.text = item.mb_nick

                if(!item.items.data.isNullOrEmpty()) {
                    if(item.items.data.size > 0) {
                        val price = PriceFormatter.getInstance()!!.getFormattedValue(item.items.data[0].nPrdSellPrice)
                        val product = item.items.data[0].strPrdName
                        if(item.items.nTotalCount == 1) {
                            itemHolder.price.text = "${price}원"
                        } else {
                            itemHolder.price.text = "${price}원~"
                        }

                        itemHolder.product.text = product
                    }
                }

                when (item.live_type) {
                    "lastlive" -> {
                        itemHolder.broadcastStatus.visibility = View.VISIBLE
                        itemHolder.label.visibility = View.GONE
                        itemHolder.status.text = "지난방송"
                        itemHolder.status.setTextColor(Color.parseColor("#192028"))
                        itemHolder.status.typeface = Typeface.createFromAsset(mContext.assets, "fonts/notosanskr_medium.otf")
                    }

                    "live" -> {
                        itemHolder.broadcastStatus.visibility = View.VISIBLE
                        itemHolder.label.visibility = View.VISIBLE
                        itemHolder.status.text = "방송중"
                        itemHolder.status.setTextColor(Color.parseColor("#ff6c6c"))
                        itemHolder.status.typeface = Typeface.createFromAsset(mContext.assets, "fonts/notosanskr_bold.otf")
                    }

                    else -> {
                        itemHolder.broadcastStatus.visibility = View.GONE
                        itemHolder.label.visibility = View.GONE
                    }

                }

                if(item.live_possible == "Y"
                        && item.is_mine == "Y"
                        && item.live_type == "empty") {
                    itemHolder.liveStart.visibility = View.VISIBLE
                    itemHolder.alarmSwitch.visibility = View.GONE
                } else {
                    itemHolder.liveStart.visibility = View.GONE
                    itemHolder.alarmSwitch.visibility = View.VISIBLE
                }

                if(item.is_mine == "Y") {
                    itemHolder.alarmSwitch.isEnabled = false
                } else {
                    if(item.live_type == "empty") {
                        itemHolder.alarmSwitch.isSelected = if(item.my_alarm == "Y") true else false
                    } else {
                        itemHolder.alarmSwitch.isEnabled = false
                    }
                }

                setScheduleProduct(itemHolder, item.items)

                itemHolder.alarmSwitch.setOnClickListener(object : OnSingleClickListener() {
                    override fun onSingleClick(v: View?) {
                        if(AppPreferences.getLoginStatus(mContext)) {
                            v!!.isSelected = !v!!.isSelected
                            mListener?.onAlarmClicked(v.isSelected, item, itemHolder)
                        } else {
                            mContext.startActivity(Intent(mContext, LoginActivity::class.java))
                        }
                    }
                })

                itemHolder.liveStart.setOnClickListener(object : OnSingleClickListener() {
                    override fun onSingleClick(v: View?) {
                        mListener?.onBroadcasting(item, i)
                    }
                })

                itemHolder.rootView.setOnClickListener(object : OnSingleClickListener() {
                    override fun onSingleClick(v: View?) {
                        mListener?.onPlay(item)
                    }
                })

                holder.itemView.layoutScheduleContainer.addView(itemHolder.rootView)
            }
        }
    }

    private fun setScheduleProduct(holder: ScheduleHolder, productItems: API129.Schedule.ScheduleItem.Product) {
        holder.scheduleProduct.removeAllViews()

        for (i in 0 until productItems.data.size) {
            holder.scheduleProduct.addView(ScheduleProduct(mContext, productItems.data[i].strPrdImg))
        }
    }

    inner class ItemHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)

    inner class ScheduleHolder(context: Context, root:ViewGroup) {
        var rootView: RelativeLayout
        var date:AppCompatTextView
        var status:AppCompatTextView
        var alarmCount:AppCompatTextView
        var liveStart:AppCompatTextView
        var thumbnail:AppCompatImageView
        var profile:AppCompatImageView
        var title:AppCompatTextView
        var scheduleProduct:LinearLayout
        var alarmSwitch: AppCompatImageButton
        var price:AppCompatTextView
        var product:AppCompatTextView
        var broadcastStatus:View
        var label:AppCompatImageView
        var casterName:AppCompatTextView

        init {
            rootView = LayoutInflater.from(context)
                    .inflate(R.layout.layout_schedule_item, root, false) as RelativeLayout

            date = rootView.findViewById(R.id.date)
            status = rootView.findViewById(R.id.status)
            alarmCount = rootView.findViewById(R.id.alarmCount)
            liveStart = rootView.findViewById(R.id.liveStart)
            thumbnail = rootView.findViewById(R.id.thumbnail)
            profile = rootView.findViewById(R.id.profile)
            title = rootView.findViewById(R.id.title)
            scheduleProduct = rootView.findViewById(R.id.scheduleProduct)
            alarmSwitch = rootView.findViewById(R.id.alarmSwitch)
            price = rootView.findViewById(R.id.productPrice)
            product = rootView.findViewById(R.id.product)
            broadcastStatus = rootView.findViewById(R.id.broadcastStatus)
            label = rootView.findViewById(R.id.label)
            casterName = rootView.findViewById(R.id.casterName)
        }
    }

    interface Listener {
        fun onAlarmClicked(isEnable: Boolean, item: API129.Schedule.ScheduleItem, holder: ScheduleHolder)
        fun onPlay(item:API129.Schedule.ScheduleItem)
        fun onBroadcasting(item:API129.Schedule.ScheduleItem, position: Int)
    }
}