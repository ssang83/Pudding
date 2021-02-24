package com.enliple.pudding.adapter.my

import android.view.ViewGroup
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.fragment.my.MyChannelFragment
import com.enliple.pudding.fragment.my.MyShoppingFragment

/**
 * MY 하단 채널/쇼핑 관련 PagerAdapter
 */
class MyTabPagerAdapter : androidx.fragment.app.FragmentStatePagerAdapter {
    private var mCount = 2
    private val mFragmentManager: androidx.fragment.app.FragmentManager
    private var myChannelFragment: MyChannelFragment? = null
    private var myShoppingFragment: MyShoppingFragment? = null
    constructor(fm: androidx.fragment.app.FragmentManager) : super(fm) {
        mFragmentManager = fm
    }

    override fun getCount(): Int = mCount

    override fun getPageTitle(position: Int): CharSequence? =
            if (position == 0) {
                "My채널"
            } else {
                "My쇼핑"
            }

    override fun getItemPosition(`object`: Any): Int = androidx.fragment.app.FragmentPagerAdapter.POSITION_NONE

    override fun getItem(position: Int): androidx.fragment.app.Fragment {
        if (position == 0) {
            myChannelFragment = MyChannelFragment()
            return myChannelFragment as androidx.fragment.app.Fragment
        } else {
            myShoppingFragment = MyShoppingFragment()
            return myShoppingFragment as androidx.fragment.app.Fragment
        }
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        Logger.d("instantiateItem: $position")

        var fragment = getItem(position)
        var name = makeFragmentName(container.id, position)

        var transaction = mFragmentManager.beginTransaction()
        transaction.add(container.id, fragment, name)
        transaction.commitAllowingStateLoss()
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
    }

    private fun makeFragmentName(viewId: Int, id: Int): String {
        return "android:switcher:$viewId:$id"
    }

    fun setCnt(count:Int) {
        if ( myChannelFragment != null ) {
            myChannelFragment!!.setCount(count)
        }
    }
}