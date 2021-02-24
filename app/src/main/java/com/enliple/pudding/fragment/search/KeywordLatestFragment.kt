package com.enliple.pudding.fragment.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.enliple.pudding.R
import com.enliple.pudding.adapter.search.KeywordLatestAdapter
import com.enliple.pudding.commons.data.KeywordItem
import com.enliple.pudding.commons.db.KeywordDBManager
import com.enliple.pudding.commons.network.NetworkBusResponse
import kotlinx.android.synthetic.main.fragment_keyword_recently.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

/**
 * Created by Kim Joonsung on 2019-05-08.
 */
class KeywordLatestFragment : Fragment(), KeywordLatestAdapter.Listener {

    private lateinit var mAdapter:KeywordLatestAdapter
    private val keywodItems:MutableList<KeywordItem> = mutableListOf()

    private var mIsVisibleToUser = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
            inflater.inflate(R.layout.fragment_keyword_recently, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAdapter = KeywordLatestAdapter().apply {
            setListener(this@KeywordLatestFragment)
        }

        recyclerViewKeywords.adapter = mAdapter
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        mIsVisibleToUser = isVisibleToUser

        if(mIsVisibleToUser) {
            loadKeyword()
        }
    }

    override fun onKeywordClicked(keyword: String) {
        (parentFragment as SearchKeywordFragment).setKeyword(keyword)
    }

    override fun onSetEmptyView() {
        recyclerViewKeywords.visibility = View.GONE
        empty.visibility = View.VISIBLE
    }

    private fun loadKeyword() {
        keywodItems.clear()

        KeywordDBManager.getInstance(context!!).loadAllByOrder().forEach {
            keywodItems.add(KeywordItem(
                    it.getDictionary(KeywordDBManager.KEY).getString("id"),
                    it.getDictionary(KeywordDBManager.KEY).getString("keyword"),
                    it.getDictionary(KeywordDBManager.KEY).getString("reg_date")))
        }

        if(keywodItems.size > 0) {
            recyclerViewKeywords.visibility = View.VISIBLE
            empty.visibility = View.GONE

            mAdapter.setItems(keywodItems)
        } else {
            recyclerViewKeywords.visibility = View.GONE
            empty.visibility = View.VISIBLE
        }
    }
}