package com.enliple.pudding.shoppingcaster.widget

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialog
import android.view.View
import android.view.Window
import com.enliple.pudding.R
import com.enliple.pudding.commons.events.OnSingleClickListener
import com.enliple.pudding.commons.log.Logger
import kotlinx.android.synthetic.main.dialog_caster_title_modify.*
import java.util.logging.Handler

/**
 * Created by Kim Joonsung on 2018-10-31.
 */
class CasterTitleModifyDialog : AppCompatDialog {
    companion object {
        //private const val TAG = "CasterTitleModifyDialog"
    }

    private var canDismissOnBackPressed: Boolean = false
    private var listener: Listener? = null
    private var mTitleText = ""

    constructor(context: Context?, title: String) : super(context, true, null) {
        Logger.e("title: $title")
        mTitleText = title
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        setContentView(R.layout.dialog_caster_title_modify)

        editTextTitle.setText(mTitleText)

        buttonCancel.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                dismiss()
            }
        })

        buttonConfirm.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                if (editTextTitle?.text!!.isNotEmpty()) {
                    listener?.onChangedTitleText(editTextTitle.text.toString())
                }
            }
        })
    }

    override fun onBackPressed() {
        if (canDismissOnBackPressed) {
            dismiss()
        }
    }

    override fun dismiss() {
        if (this.isShowing) {
            super.dismiss()
        }
    }

    fun setListener(listener: Listener) {
        this.listener = listener
    }

    interface Listener {
        fun onChangedTitleText(title: String)
    }
}