package com.enliple.pudding.activity

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.enliple.pudding.R
import com.enliple.pudding.commons.app.ImageLoad
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.network.NetworkApi
import com.enliple.pudding.commons.network.NetworkBus
import com.enliple.pudding.commons.network.NetworkBusResponse
import com.enliple.pudding.commons.network.NetworkHandler
import com.enliple.pudding.commons.network.vo.API116
import com.enliple.pudding.commons.network.vo.API117
import com.enliple.pudding.fragment.EventTabFragment
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_tab_event_detail.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Created by Kim Joonsung on 2019-05-23.
 */
class EventDetailTabActivity : AppCompatActivity() {

    companion object {
        const val INTENT_KEY_EVENT_ID = "event_id"
        const val INTENT_KEY_EVENT_TYPE = "event_type"

        private const val EVENT_TYPE_PRODUCT    = "prd"
        private const val EVENT_TYPE_VOD        = "vod"
    }

    private lateinit var mPagerAdapter: PagerAdapter

    private var eventId = ""
    private var eventType = ""
    private var dbKey = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tab_event_detail)
        EventBus.getDefault().register(this)

        buttonClose.setOnClickListener { finish() }

        checkIntent(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data:NetworkBusResponse) {
        val API116 = NetworkHandler.getInstance(this).getKey(NetworkApi.API116.toString(), eventId, "")
        val API117 = NetworkHandler.getInstance(this).getKey(NetworkApi.API117.toString(), eventId, "")

        if(data.arg1 == API116) {
            handleNetworkAPI116(data)
        } else if(data.arg1 == API117) {
            handleNetworkAPI117(data)
        }
    }

    private fun checkIntent(intent:Intent?) {
        if(intent != null) {
            eventId = intent.getStringExtra(INTENT_KEY_EVENT_ID)
            eventType = intent.getStringExtra(INTENT_KEY_EVENT_TYPE)
        }

        if(eventType == EVENT_TYPE_VOD) {
            NetworkBus(NetworkApi.API116.name, eventId).let { EventBus.getDefault().post(it) }
        } else if(eventType == EVENT_TYPE_PRODUCT) {
            NetworkBus(NetworkApi.API117.name, eventId).let { EventBus.getDefault().post(it) }
        }
    }

    private fun handleNetworkAPI116(data:NetworkBusResponse) {
        if("ok" == data.arg2) {
            dbKey = data.arg1
            val response:API116 = Gson().fromJson(DBManager.getInstance(this).get(data.arg1), API116::class.java)

            textViewTitle.text = response.data.evTitle
            eventBanner.setRatio(response.data.sub_img1_width.toFloat() / response.data.sub_img1_height.toFloat())

            ImageLoad.setImage(
                    this,
                    eventBanner,
                    response.data.sub_img1,
                    null,
                    ImageLoad.SCALE_NONE,
                    DiskCacheStrategy.ALL)

            for(i in 0 until response.data.evPick.size) {
                val title = response.data.evPick[i].tagName
                tabLayout.addTab(tabLayout.newTab().setText(title))
            }

            tabLayout.setupWithViewPager(viewPagerContainer)
            mPagerAdapter = PagerAdapter(supportFragmentManager)
            tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabReselected(tab: TabLayout.Tab?) {}

                override fun onTabUnselected(tab: TabLayout.Tab?) {}

                override fun onTabSelected(tab: TabLayout.Tab?) {
                    setTabLayoutFont()
                    if(tab != null) {
                        viewPagerContainer.currentItem = tab.position
                    }
                }
            })

            mPagerAdapter.setCount(response.data.evPick.size)

            tabLayout.getTabAt(0)?.select()
            setTabLayoutFont()
        }
    }

    private fun handleNetworkAPI117(data:NetworkBusResponse) {
        if("ok" == data.arg2) {
            dbKey = data.arg1
            val response:API117 = Gson().fromJson(DBManager.getInstance(this).get(data.arg1), API117::class.java)

            textViewTitle.text = response.data.evTitle
            eventBanner.setRatio(response.data.sub_img1_width.toFloat() / response.data.sub_img1_height.toFloat())

            ImageLoad.setImage(
                    this,
                    eventBanner,
                    response.data.sub_img1,
                    null,
                    ImageLoad.SCALE_NONE,
                    DiskCacheStrategy.ALL)

            for(i in 0 until response.data.evPick.size) {
                val title = response.data.evPick[i].themeName
                tabLayout.addTab(tabLayout.newTab().setText(title))
            }

            tabLayout.setupWithViewPager(viewPagerContainer)
            mPagerAdapter = PagerAdapter(supportFragmentManager)
            tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabReselected(tab: TabLayout.Tab?) {}

                override fun onTabUnselected(tab: TabLayout.Tab?) {}

                override fun onTabSelected(tab: TabLayout.Tab?) {
                    setTabLayoutFont()
                    if(tab != null) {
                        viewPagerContainer.currentItem = tab.position
                    }
                }
            })

            mPagerAdapter.setCount(response.data.evPick.size)

            tabLayout.getTabAt(0)?.select()
            setTabLayoutFont()
        }
    }

    private fun setTabLayoutFont() {
        val tabPosition = tabLayout.selectedTabPosition
        val vg = tabLayout.getChildAt(0) as ViewGroup
        val tabCnt = vg.childCount

        for (i in 0 until tabCnt) {
            val vgTab = vg.getChildAt(i) as ViewGroup
            val tabChildCnt = vgTab.childCount
            for (j in 0 until tabChildCnt) {
                val tabViewChild = vgTab.getChildAt(j)
                if (tabViewChild is AppCompatTextView) {
                    if (i == tabPosition) {
                        tabViewChild.typeface = Typeface.createFromAsset(assets, "fonts/notosanskr_bold.otf")
                    } else {
                        tabViewChild.typeface = Typeface.createFromAsset(assets, "fonts/notosanskr_regular.otf")
                    }
                }
            }
        }
    }

    inner class PagerAdapter : FragmentStatePagerAdapter {
        private var mCount = 0
        private var mFragmentManager: FragmentManager

        fun setCount(count:Int) {
            this.mCount = count
            notifyDataSetChanged()
        }

        constructor(fm:FragmentManager) : super(fm) {
            mFragmentManager = fm
        }

        override fun getItem(position: Int) = EventTabFragment().apply {
            arguments = Bundle().apply {
                putInt("position", position)
                putString("dbKey", dbKey)
                putString("eventType", eventType)
            }
        }

        override fun getCount() = mCount

        override fun getPageTitle(position: Int) = ""
    }
}