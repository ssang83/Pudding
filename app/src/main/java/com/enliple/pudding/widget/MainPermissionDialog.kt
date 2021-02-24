package com.enliple.pudding.widget

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.enliple.pudding.R
import com.enliple.pudding.adapter.PermissionAdapter
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.model.PermissionObject
import kotlinx.android.synthetic.main.permission_dialog.*
import java.util.*

class MainPermissionDialog(objects: PermissionObject) : DialogFragment() {

    private var array: ArrayList<PermissionObject.Objects>? = null
    private var title: String
    private var listener: DialogClickListener? = null

    interface DialogClickListener {
        fun onDialogClick()

        fun onDismissed()
    }

    init {
        array = objects.objectArray
        title = objects.title
        Logger.e("title:$title")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.permission_dialog, container)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setStyle(STYLE_NO_FRAME, R.style.AppTheme)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        var adapter = PermissionAdapter(context, array)
        val manager = LinearLayoutManager(context)
        manager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = manager
        recyclerView.adapter = adapter

        buttonConfirm.setOnClickListener {
            if (listener != null)
                listener!!.onDialogClick()
        }

        tvTitle.text = title
    }

    fun setListener(listener: DialogClickListener) {
        this.listener = listener
    }

    override fun onStop() {
        super.onStop()
        Logger.e("PermissionDialog onStop")
        listener?.onDismissed()
    }
}
