package com.enliple.pudding.adapter.home

import android.content.Context
import android.os.Bundle
import android.util.SparseArray
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter
import com.enliple.pudding.common.VideoDataContainer
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.vo.VOD
import com.enliple.pudding.fragment.main.ShoppingLiveFragment
import com.enliple.pudding.fragment.main.ShoppingVideoFragment

class ShoppingListAdapter : FragmentStatePagerAdapter {
    private val mFragmentManager: FragmentManager
    //private lateinit var mData: VOD.DataBeanX
    private var mCount = 0
    private var mMyVODPosition = -1
    private var mNameMap = SparseArray<String>()
    private var callbackListener: OnCallbackListener? = null
    private var mPlayerFlag = -1
    private var mVideoType = ""
    private var mShareKey = ""
    private var mVideoData:MutableList<VOD.DataBeanX> = mutableListOf()

    constructor(context: Context, fm: FragmentManager, mData:MutableList<VOD.DataBeanX>, vodPos: Int, pos: Int, playerFlag: Int, videoType: String, shareKey: String,
                listener: OnCallbackListener) : super(fm) {
        this.mFragmentManager = fm

        mMyVODPosition = vodPos
        callbackListener = listener
        mPlayerFlag = playerFlag
        mVideoType = videoType
        mShareKey = shareKey
        mVideoData = mData

        try {
            if (mPlayerFlag > 0) {
                mCount = 1
            } else {
                var data = mData[pos]
                mCount = mData.size
                if (callbackListener != null) {
                    callbackListener!!.onCallback(data)
                }
            }

            Logger.e("result totalCount: $mCount")
        } catch (e: Exception) {
            Logger.p(e)
            mCount = 0
        }
    }

    override fun getCount(): Int = mCount

    override fun getPageTitle(position: Int): CharSequence? = ""

    override fun getItemPosition(`object`: Any): Int = FragmentPagerAdapter.POSITION_NONE

    override fun getItem(position: Int): Fragment {
        Logger.d("getItem $position")

        var pos = position

        val args = Bundle()
        args.putInt("position", pos)
        args.putInt("player_flag", mPlayerFlag)
        args.putInt("my_vod_position", mMyVODPosition)
        args.putString("share_key", mShareKey)

        if (mPlayerFlag > 0) {
            return when (mVideoType) {
                "LIVE" -> ShoppingLiveFragment().apply { arguments = args }
                else -> ShoppingVideoFragment().apply { arguments = args }
            }
        } else {
            return when (mVideoData[pos].videoType) {
                "LIVE" -> ShoppingLiveFragment().apply { arguments = args }
                else -> ShoppingVideoFragment().apply { arguments = args }
            }
        }
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val fragment = getItem(position)
        val name = makeFragmentName(container.id, position)

        val transaction = mFragmentManager.beginTransaction()
        transaction.add(container.id, fragment, name)
        transaction.commitAllowingStateLoss()   // 앱 죽는이슈로 commitAllowingStateLoss로 수정 (Can not perform this action after onSaveInstanceState)

        mNameMap.put(position, name)
        return fragment
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        val fragment = mFragmentManager.findFragmentByTag(makeFragmentName(container.id, position))
        if (fragment != null) {
            val transaction = mFragmentManager.beginTransaction()
            transaction.remove(fragment)
            transaction.commit()
        }

        mNameMap.delete(position)
    }

    fun getFragment(position: Int): Fragment? {
        return mFragmentManager.findFragmentByTag(mNameMap[position])
    }

    fun getData(position: Int): VOD.DataBeanX? {
        if (mVideoData.isNullOrEmpty()) {
            return null
        }

        return if (position < mVideoData.size) {
            mVideoData[position]
        } else {
            null
        }
    }

    private fun makeFragmentName(viewId: Int, id: Int): String {
        return "android:player:$viewId:$id"
    }

    interface OnCallbackListener {
        fun onCallback(data: VOD.DataBeanX)
    }
}