package com.enliple.pudding.commons.app

import android.text.TextUtils
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

/**
 * Created by hkcha on 2017-12-29.
 *
 */
class PriceFormatter {

    private var fmt: DecimalFormat = DecimalFormat()
    private var fmts: DecimalFormatSymbols = DecimalFormatSymbols()

    companion object {
        var formatter: PriceFormatter? = null

        fun getInstance(): PriceFormatter? {
            if (formatter == null) {
                formatter = PriceFormatter()
            }

            return formatter
        }
    }

    constructor() {
        fmts.groupingSeparator = ','
        fmt.groupingSize = 3
        fmt.isGroupingUsed = true
        fmt.currency = getLocalCurrency()
        fmt.decimalFormatSymbols = fmts
    }

    /**
     * 넘겨받은 값을 포맷에 맞춰 변경한다.
     * @param value     포맷을 적용시킬 값
     * @return          포맷이 적용된 문자열
     */
    fun getFormattedValue(value: Int): String {
        return fmt.format(value)
    }

    /**
     * 넘겨받은 값을 포맷에 맞춰 변경한다.
     * @param value     포맷을 적용시킬 값
     * @return          포맷이 적용된 문자열
     */
    fun getFormattedValue(value: Long): String {
        return fmt.format(value)
    }

    /**
     * 넘겨받은 값을 포맷에 맞춰 변경한다.
     * @param value     포맷을 적용시킬 값
     * @return          포맷이 적용된 문자열
     */
    fun getFormattedValue(value: Double): String {
        return fmt.format(value)
    }

    /**
     * 넘겨받은 문자열을 포맷에 맞춰 변경한다.  숫자가 아닌 문자는 자동으로 삭제함.
     * @param value     포맷을 적용시킬 문자열
     * @return          포맷이 적용된 문자열
     */
    fun getFormattedValue(value: String): String {
        return if (TextUtils.isEmpty(value)) {
            "0"
        } else {
            var tempValue: String? = value.trim()
            tempValue = tempValue?.replace(".", "")
            tempValue = tempValue?.replace(",", "")
            tempValue = tempValue?.replace("원", "")

            try {
                fmt.format(Integer.parseInt(tempValue))
            } catch (e: Exception) {
                "0"
            }
        }
    }

    private fun getLocalCurrency(): Currency {
        return try {
            Currency.getInstance(Locale.getDefault())
        } catch (e: IllegalArgumentException) {
            Currency.getInstance(Locale("en", "US"))
        }
    }

}