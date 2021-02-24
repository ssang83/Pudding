package com.enliple.pudding.adapter.home

import android.content.Context
import android.os.Bundle
import android.util.SparseArray
import android.view.ViewGroup
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.fragment.main.MainVideoFragment

/**
 */
class MainListAdapter : androidx.fragment.app.FragmentStatePagerAdapter {
    private val mFragmentManager: androidx.fragment.app.FragmentManager
    private var mContext: Context
    private var mNameMap = SparseArray<String>()
    private var mCount = 0
    private var mTabIndex = 0

    constructor(context: Context, fm: androidx.fragment.app.FragmentManager, tab: Int) : super(fm) {
        mContext = context
        mFragmentManager = fm
        mTabIndex = tab
    }

    fun setCount(count: Int) {
        mCount = count

        notifyDataSetChanged()
    }

    override fun getCount(): Int = mCount

    override fun getPageTitle(position: Int): CharSequence? = ""

    override fun getItemPosition(`object`: Any): Int = androidx.fragment.app.FragmentPagerAdapter.POSITION_NONE

    override fun getItem(position: Int): androidx.fragment.app.Fragment =
            MainVideoFragment().apply {
                arguments = Bundle().apply {
                    putInt("position", position)
                    putInt("tab", mTabIndex)
                }
            }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        Logger.d("instantiateItem: $position")

        var fragment = getItem(position)
        var name = makeFragmentName(container.id, position)

        var transaction = mFragmentManager.beginTransaction()
        transaction.add(container.id, fragment, name)
        transaction.commitAllowingStateLoss()

        mNameMap.put(position, name)
        return fragment
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        Logger.d("destroyItem: $position")
        var fragment = mFragmentManager.findFragmentByTag(makeFragmentName(container.id, position))
        if (fragment != null) {
            var transaction = mFragmentManager.beginTransaction()
            transaction.remove(fragment)
            transaction.commitAllowingStateLoss()
        }

        mNameMap.delete(position)
    }

    fun getFragment(position: Int): androidx.fragment.app.Fragment? {
        return mFragmentManager.findFragmentByTag(mNameMap[position])
    }

    private fun makeFragmentName(viewId: Int, id: Int): String {
        return "android:switcher:$viewId:$id"
    }
}