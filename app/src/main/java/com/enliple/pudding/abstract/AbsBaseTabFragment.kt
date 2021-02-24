package com.enliple.pudding

/**
 * Created by hkcha on 2018. 2. 8..
 * Tab 기반 BaseFragment
 */
abstract class AbsBaseTabFragment : AbsBaseFragment() {
    abstract fun onTabChanged(tabIndex: Int)
}