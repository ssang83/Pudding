package com.enliple.pudding.shoppingcaster.fragment.productdialog

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.enliple.pudding.R
import com.enliple.pudding.shoppingcaster.adapter.productdialog.CastFollowerAdapter
import kotlinx.android.synthetic.main.fragment_dialog_cast_follower.*

/**
 * 방송 상품 팝업 방송중 팔로워 Fragment
 * @author hkcha
 * @since 2018.09.04
 */
class CastFollowerFragment : androidx.fragment.app.Fragment() {
    companion object {

        const val BUNDLE_EXTRA_KEY_CAST_ID = "cast_id"

        private const val TAG = "CastFollowerFragment"
        private const val RECYCLER_VIEW_ITEM_CACHE_SIZE = 20
    }

    private var castId: String? = null
    private lateinit var mAdapter: CastFollowerAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        castId = arguments?.getString(BUNDLE_EXTRA_KEY_CAST_ID)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_dialog_cast_follower, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerViewFollower.setHasFixedSize(true)
        recyclerViewFollower.setItemViewCacheSize(RECYCLER_VIEW_ITEM_CACHE_SIZE)
        mAdapter = CastFollowerAdapter()
        recyclerViewFollower.adapter = mAdapter

        loadData()
    }

    private fun loadData() {
        // TODO : 방송중 팔로워 API 통신 호출
    }
}