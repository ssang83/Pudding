package com.enliple.pudding.fragment.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.enliple.pudding.R
import com.enliple.pudding.adapter.search.KeywordHotAdapter
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.network.NetworkApi
import com.enliple.pudding.commons.network.NetworkBus
import com.enliple.pudding.commons.network.NetworkBusResponse
import com.enliple.pudding.commons.network.vo.API144
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_keyword_hot.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

/**
 * Created by Kim Joonsung on 2019-05-08.
 */
class KeywordHotFragment : Fragment() , KeywordHotAdapter.Listener {

    private lateinit var mAdapter:KeywordHotAdapter
    private var mIsVisibleToUser = false
    private var mIsFirst = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
            inflater.inflate(R.layout.fragment_keyword_hot, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        EventBus.getDefault().register(this)

        mAdapter = KeywordHotAdapter().apply {
            setListener(this@KeywordHotFragment)
        }

        recyclerViewKeywords.adapter = mAdapter
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        mIsVisibleToUser = isVisibleToUser

        if(mIsVisibleToUser) {
            if(!mIsFirst) {
                mIsFirst = true  // 최초 한번만 가져온다.
                NetworkBus(NetworkApi.API144.name).let { EventBus.getDefault().post(it) }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onKeywordClick(item: API144.KeywordItem) {
        (parentFragment as SearchKeywordFragment).setKeyword(item.keyword)
    }

    @Subscribe
    fun onMessageEvent(data: NetworkBusResponse) {
        if(data.arg1 == "GET/rank/keyword") {
            if(data.arg2 == "ok") {
                val response:API144 = Gson().fromJson(DBManager.getInstance(context!!).get(data.arg1), API144::class.java)
                mAdapter.setItems(response.data)
            }
        }
    }
}