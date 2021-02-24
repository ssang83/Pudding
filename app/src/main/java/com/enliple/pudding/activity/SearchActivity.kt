package com.enliple.pudding.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import com.couchbase.lite.MutableDocument
import com.enliple.pudding.AbsBaseActivity
import com.enliple.pudding.R
import com.enliple.pudding.commons.app.NetworkStatusUtils
import com.enliple.pudding.commons.app.SoftKeyboardUtils
import com.enliple.pudding.commons.db.KeywordDBManager
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.fragment.search.SearchKeywordFragment
import com.enliple.pudding.fragment.search.SearchMainFragment
import com.enliple.pudding.fragment.search.SearchResultFragment
import kotlinx.android.synthetic.main.activity_search.*
import java.text.SimpleDateFormat
import java.util.*

class SearchActivity : AbsBaseActivity() {

    companion object {
        private const val KEYWORD_ID = 1
    }

    private var mainFragment: SearchMainFragment? = null
    private var resultFragment: SearchResultFragment? = null
    private var keywordFragment: SearchKeywordFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.d("onCreate()")

        setContentView(R.layout.activity_search)

        editTextSearch.setOnEditorActionListener(actionListener)
        editTextSearch.addTextChangedListener(textChangeListener)
        editTextSearch.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                if (event!!.action == MotionEvent.ACTION_DOWN) {
                    editTextSearch.requestFocus()
                    searchKeyword()
                }
                return false
            }
        })

        buttonSearchBack.setOnClickListener(clickListener)
        textViewConfirm.setOnClickListener(clickListener)
        btnDeleteSearchWord.setOnClickListener(clickListener)

        initFragment()
    }

    private val textChangeListener = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (s != null) {
//                var str: String = s.toString().replace(" ", "")
//                if (str.isNotEmpty()) {
//                    btnDeleteSearchWord.visibility = View.VISIBLE
//                } else {
//                    btnDeleteSearchWord.visibility = View.GONE
//                }
            }
        }
    }

    private val actionListener = TextView.OnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            searchResult(editTextSearch.text.toString())
            SoftKeyboardUtils.hideKeyboard(editTextSearch)
            true
        }

        false
    }

    override fun onNetworkStatusChanged(status: NetworkStatusUtils.NetworkStatus) {}

    override fun onDozeModeStateChanged(dozeEnable: Boolean) {}

    override fun onBackPressed() {
        finish()
    }

    private fun initFragment() {
        mainFragment = SearchMainFragment()
        resultFragment = SearchResultFragment()
        keywordFragment = SearchKeywordFragment()

        var transaction = supportFragmentManager.beginTransaction()

        transaction.add(layoutContainer.id, mainFragment!!, mainFragment!!::class.java.name)
        transaction.add(layoutContainer.id, resultFragment!!, resultFragment!!::class.java.name)
        transaction.add(layoutContainer.id, keywordFragment!!, keywordFragment!!::class.java.name)

        transaction.hide(resultFragment!!)
        transaction.hide(keywordFragment!!)
        transaction.attach(mainFragment!!)
        transaction.show(mainFragment!!)

        transaction.commitAllowingStateLoss()
    }

    fun searchResult(keyword: String) {
        if ( keyword.isEmpty() ) {
            SoftKeyboardUtils.hideKeyboard(editTextSearch)

            if(mainFragment == null) {
                mainFragment = SearchMainFragment()
            }

            supportFragmentManager.beginTransaction().apply {
                hide(keywordFragment!!)
                hide(resultFragment!!)
                attach(mainFragment!!)
                show(mainFragment!!)

                commitAllowingStateLoss()
            }
        } else {
            SoftKeyboardUtils.hideKeyboard(editTextSearch)
            editTextSearch.setText(keyword)
            saveKeyword(keyword)

            if (resultFragment == null) {
                resultFragment = SearchResultFragment()
            }

            (resultFragment as SearchResultFragment).setKeyword(keyword)

            var transaction = supportFragmentManager.beginTransaction()

            transaction.hide(mainFragment!!)
            transaction.hide(keywordFragment!!)
            transaction.attach(resultFragment!!)
            transaction.show(resultFragment!!)

            transaction.commitAllowingStateLoss()
        }
    }

    private fun hideFragment() {
        if (mainFragment == null) {
            mainFragment = SearchMainFragment()
        }

        editTextSearch.setText("")
        btnDeleteSearchWord.visibility = View.GONE

        var transaction = supportFragmentManager.beginTransaction()
        transaction.hide(resultFragment!!)
        transaction.hide(keywordFragment!!)
        transaction.attach(mainFragment!!)
        transaction.show(mainFragment!!)

        transaction.commitAllowingStateLoss()
    }

    private fun searchKeyword() {
        if(keywordFragment == null) {
            keywordFragment = SearchKeywordFragment()
        }

        supportFragmentManager.beginTransaction().apply {
            hide(mainFragment!!)
            hide(resultFragment!!)
            attach(keywordFragment!!)
            show(keywordFragment!!)

            commitAllowingStateLoss()
        }
    }

    private fun saveKeyword(keyword: String) {
        var nextKeywordId = KEYWORD_ID
        var tempId = -1

        KeywordDBManager.getInstance(this).loadAllExt().forEach {
            if(keyword == it.getDictionary(KeywordDBManager.KEY).getString("keyword")) {
                nextKeywordId = it.getDictionary(KeywordDBManager.KEY).getString("id").toInt()
                return@forEach
            }

            tempId = it.getDictionary(KeywordDBManager.KEY).getString("id").toInt()
            if(nextKeywordId <= tempId) {
                nextKeywordId = tempId + 1
            }
        }

        if(tempId == -1) {
            nextKeywordId = KEYWORD_ID
        }

        val document = MutableDocument(nextKeywordId.toString()).apply {
            setString("id", nextKeywordId.toString())
            setString("keyword", keyword)
            setString("reg_date", SimpleDateFormat("MM-dd").format(Date(System.currentTimeMillis())))
        }

        Logger.e("keywordID : $nextKeywordId, keyword:$keyword, date:${SimpleDateFormat("MM-dd").format(Date(System.currentTimeMillis()))}")
        KeywordDBManager.getInstance(this).put(document)
    }

    private val clickListener = View.OnClickListener {
        when (it?.id) {
            R.id.buttonSearchBack -> {
                val fragment = supportFragmentManager.findFragmentByTag(resultFragment!!::class.java.name)
                if (fragment != null && fragment.isVisible) {
                    hideFragment()
                } else {
                    onBackPressed()
                }
            }

            R.id.textViewConfirm -> {
                searchResult(editTextSearch.text.toString())
                SoftKeyboardUtils.hideKeyboard(editTextSearch)
                editTextSearch.clearFocus()
            }

            R.id.btnDeleteSearchWord -> {
                editTextSearch.setText("")
                btnDeleteSearchWord.visibility = View.GONE
            }
        }
    }
}