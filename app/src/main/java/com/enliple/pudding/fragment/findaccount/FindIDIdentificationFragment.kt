package com.enliple.pudding.fragment.findaccount

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import com.enliple.pudding.AbsBaseFragment
import com.enliple.pudding.R
import com.enliple.pudding.activity.PGAuthActivity
import com.enliple.pudding.enumeration.FindIdentificationSearchType
import kotlinx.android.synthetic.main.fragment_find_identication.*

class FindIDIdentificationFragment : AbsBaseFragment() {
    companion object {
        private const val REQUEST_CODE_CELLPHONE_IDENTIFICATION = 0xAC01
    }

    private var findIdType: FindIdentificationSearchType = FindIdentificationSearchType.CELLPHONE
    private var isRequestIdentification = false


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_find_identication, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        radioGroupFindType.setOnCheckedChangeListener(findTypeChangeListener)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CODE_CELLPHONE_IDENTIFICATION -> {
                if (resultCode == Activity.RESULT_OK) {
                    // Success

                    radioButtonTypeCellPhone.isEnabled = false
                    radioButtonTypeIPin.isEnabled = false

                } else {
                    // Failed
                    radioButtonTypeCellPhone.isChecked = false
                    radioButtonTypeIPin.isChecked = false
                }
            }
        }

        isRequestIdentification = false
        super.onActivityResult(requestCode, resultCode, data)
    }

    private val findTypeChangeListener = RadioGroup.OnCheckedChangeListener { buttonView, checkedId ->
        when (checkedId) {
            R.id.radioButtonTypeCellPhone -> {
                if (!isRequestIdentification) {
                    findIdType = FindIdentificationSearchType.CELLPHONE

                    startActivityForResult(Intent(buttonView.context, PGAuthActivity::class.java).apply {
                        putExtra(PGAuthActivity.INTENT_EXTRA_KEY_CALL_MODE, PGAuthActivity.INTENT_EXTRA_VALUE_MODE_IDENTIFICATION)
                    }, REQUEST_CODE_CELLPHONE_IDENTIFICATION)

                    isRequestIdentification = true
                }
            }

            R.id.radioButtonTypeIPin -> {
                findIdType = FindIdentificationSearchType.IPIN
                // TODO : 아이핀 인증
            }
        }
    }
}