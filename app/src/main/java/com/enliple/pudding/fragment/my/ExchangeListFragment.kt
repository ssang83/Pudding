package com.enliple.pudding.fragment.my

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.enliple.pudding.R
import com.enliple.pudding.adapter.my.ExchangeListAdapter
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.network.vo.API155
import com.enliple.pudding.commons.widget.recyclerview.WrappedLinearLayoutManager
import com.enliple.pudding.model.ExchangeListModel
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_exchange_list.*

class ExchangeListFragment   : androidx.fragment.app.Fragment() {


    companion object {
        const val BUNDLE_EXTRA_KEY_USER_ID = "ExchangeListFragment_userId"
        const val BUNDLE_EXTRA_KEY_USER_COOKIE = "ExchangeListFragment_userCookie"
        const val BUNDLE_EXTRA_KEY_USER_POINT = "ExchangeListFragment_point"
        const val BUNDLE_EXTRA_KEY_IS_JELLY = "ExchangeListFragment_isJelly"
    }

    private var userId = ""
    private var userCookie = "0"
    private var userPoint = "0"
    private var isJelly = false
    private var eAdapter: ExchangeListAdapter? = null
    private var layoutManager: WrappedLinearLayoutManager? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_exchange_list, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (arguments != null) {
            userId = arguments!!.getString(BUNDLE_EXTRA_KEY_USER_ID)
            userCookie = arguments!!.getString(BUNDLE_EXTRA_KEY_USER_COOKIE)
            userPoint = arguments!!.getString(BUNDLE_EXTRA_KEY_USER_POINT)
            isJelly = arguments!!.getBoolean(BUNDLE_EXTRA_KEY_IS_JELLY)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if ( isJelly ) {
            titlePrice.text = "환전 젤리 갯수"
        } else {
            titlePrice.text = "환전 포인트"
        }
        eAdapter = ExchangeListAdapter(context!!, isJelly)
        layoutManager = WrappedLinearLayoutManager(activity)
        layoutManager!!.orientation = RecyclerView.VERTICAL
        list.setHasFixedSize(false)
        list.layoutManager = layoutManager
        list.adapter = eAdapter

        var key = "GET/user/" + AppPreferences.getUserId(context!!) + "/exchange"
        var str = DBManager.getInstance(context!!).get(key)

        if ( str != null ) {
            val response = Gson().fromJson(str, API155::class.java)
            if ( response != null ) {
                if( isJelly ) {
                    var cookieHistoryArray = response.data.cookie_history
                    if ( cookieHistoryArray != null && cookieHistoryArray.size > 0 ) {
                        var modelArray = ArrayList<ExchangeListModel>()
                        for (i in 0 until cookieHistoryArray.size ) {
                            var model = ExchangeListModel()
                            var itemModel = cookieHistoryArray[i]
                            model.point = itemModel.point
                            model.reg_date = itemModel.reg_date
                            model.price = itemModel.price
                            model.status = itemModel.status
                            modelArray.add(model)
                        }
                        if ( modelArray.size > 0 ) {
                            list.visibility = View.VISIBLE
                            emptyLayer.visibility = View.GONE
                        } else {
                            list.visibility = View.GONE
                            emptyLayer.visibility = View.VISIBLE
                        }
                        eAdapter!!.setItems(modelArray)
                    } else {
                        list.visibility = View.GONE
                        emptyLayer.visibility = View.VISIBLE
                    }
                } else {
                    var pointHistoryArray = response.data.point_history
                    if ( pointHistoryArray != null && pointHistoryArray.size > 0 ) {
                        var modelArray = ArrayList<ExchangeListModel>()
                        for (i in 0 until pointHistoryArray.size ) {
                            var model = ExchangeListModel()
                            var itemModel = pointHistoryArray[i]
                            model.point = itemModel.point
                            model.reg_date = itemModel.reg_date
                            model.price = itemModel.price
                            model.status = itemModel.status
                            modelArray.add(model)
                        }
                        if ( modelArray.size > 0 ) {
                            list.visibility = View.VISIBLE
                            emptyLayer.visibility = View.GONE
                        } else {
                            list.visibility = View.GONE
                            emptyLayer.visibility = View.VISIBLE
                        }
                        eAdapter!!.setItems(modelArray)
                    } else {
                        list.visibility = View.GONE
                        emptyLayer.visibility = View.VISIBLE
                    }
                }
            } else {
                list.visibility = View.GONE
                emptyLayer.visibility = View.VISIBLE
            }
        }
    }
}