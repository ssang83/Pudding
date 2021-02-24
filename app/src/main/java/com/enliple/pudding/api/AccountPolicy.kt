package com.enliple.pudding.api

import java.util.regex.Pattern
import android.R.attr.password
import com.enliple.pudding.commons.log.Logger


class AccountPolicy {
    companion object {
        const val ACCOUNT_MINIMUM_LENGTH = 5
        const val ACCOUNT_MAXIMUM_LENGTH = 12
//        const val PASSWORD_MINIMUM_LENGTH = 8
//        const val PASSWORD_MAXIMUM_LENGTH = 12
        // 길이제한 제거
//        const val ACCOUNT_MINIMUM_LENGTH = 5
//        const val ACCOUNT_MAXIMUM_LENGTH = 12
//        const val PASSWORD_MINIMUM_LENGTH = 8
//        const val PASSWORD_MAXIMUM_LENGTH = 12

//        val koreanMatcher: Pattern = Pattern.compile(".*[ㄱ-ㅎㅏ-ㅣ가-힣]+.*")
//        val specialPattern: Pattern = Pattern.compile(".*[$&+,:;=?@#|'<>.\\-^*()%!].*")
//        val anotherCharCheckPattern: Pattern = Pattern.compile("^[a-zA-Z0-9!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?~`]+$")
//        val numberPattern: Pattern = Pattern.compile(".*[0-9].*")
//        val upperCasePattern: Pattern = Pattern.compile(".*[A-Z].*")
//        val lowerCasePattern: Pattern = Pattern.compile(".*[a-z].*")
//        val accountPattern: Pattern = Pattern.compile("^[a-zA-Z0-9]$")
//        val accountSpecialPattern: Pattern = Pattern.compile(".*[!@#\$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?~`].*")
//        val emailPattern: Pattern = Pattern.compile("^[_a-zA-Z0-9-\\.]+@[\\.a-zA-Z0-9-]+\\.[a-zA-Z]+$")

//        var passwordPattern = "^(?=.*\\d)(?=.*[~`!@#$%\\^&*()-])(?=.*[a-z])(?=.*[A-Z]).{8,12}$"

        val passwordPattern = "^(?=.*\\d)(?=.*[~`!@#$%\\^&*()-])(?=.*[a-z]).{8,12}$"

//        var idPattern = "(^[A-Za-z0-9]{5,12}$)"
//        var mailPattern = "^[_a-zA-Z0-9-\\\\.]+@[\\\\.a-zA-Z0-9-]+\\\\.[a-zA-Z]+\$"
//        var mailPattern = "^[a-zA-Z0-9]+@[a-zA-Z0-9]+\$ or ^[_0-9a-zA-Z-]+@[0-9a-zA-Z-]+(.[_0-9a-zA-Z-]+)*\$"
        var mpattern = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"
        val numberPattern = Pattern.compile("^[0-9]*$")
        val upperPattern = Pattern.compile("^[A-Z]*$")
        val lowerPattern = Pattern.compile("^[a-z]*$")
        val mixPattern = Pattern.compile("^[a-zA-Z]*$")
        val idAllPattern = Pattern.compile("^[a-zA-Z0-9]*$")
        val mailPattern = Pattern.compile(mpattern)

        fun IsIdMatched(str : String) : Boolean {
            val matcherLower = lowerPattern.matcher(str).matches()
            val matcherUpper = upperPattern.matcher(str).matches()
            val matcherNumber = numberPattern.matcher(str).matches()
            val mPattern = mixPattern.matcher(str).matches()
            val idPattern = idAllPattern.matcher(str).matches()

            return if (str.length in ACCOUNT_MINIMUM_LENGTH..ACCOUNT_MAXIMUM_LENGTH) {
                matcherLower || matcherUpper || matcherNumber || mPattern || idPattern
            } else {
                false
            }
        }

        fun IsPasswordMatched(pw : String) : Boolean {
            var temp = pw.toLowerCase()
            val matcher = Pattern.compile(passwordPattern).matcher(temp)
            return matcher.matches()
        }

        fun IsEMailMatched(email : String) : Boolean {
            val matcher = Pattern.compile(mpattern).matcher(email)
            return matcher.matches()
        }

        fun IsPhoneMatched(phone : String) : Boolean {
            val matcher = numberPattern.matcher(phone)
            return matcher.matches()
        }

    }
}