package com.enliple.pudding.commons.app

import android.animation.ValueAnimator
import android.content.Context
import android.text.Spannable
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.util.Patterns
import android.widget.TextView
import com.enliple.pudding.commons.R
import com.enliple.pudding.commons.log.Logger
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.Charset
import java.text.DecimalFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by hkcha on 2017-12-22.
 * 문자열 관련 유틸리티 메소드 모음
 */
open class StringUtils {
    companion object {
        val TAG = "StringUtils"

        /**
         * 숫자로 구성된 문자열인지 확인
         * @param number
         * @return
         */
        fun isNumber(number: String?): Boolean {
            if (number == null || number == "") {
                return false
            }

            var size = number.length
            var stNo = 0

            if (number.toCharArray()[0].toInt() == 45) {
                stNo = 1
            }

            var charArr: CharArray = number.toCharArray()
            return ((stNo..size).any { charArr[it].toInt() !in 48..57 })
        }

        /**
         * 입력 텍스트가 이메일 주소인지 확인
         */
        fun isEmailAddress(email: CharSequence): Boolean {
            return Patterns.EMAIL_ADDRESS.matcher(email).matches()
        }

        /**
         * 문자열에 통화적용을 위해 컴마를 표기한다.
         * <p>
         * param string
         * 통화적용을 위한 문자열
         * param ignoreZero
         * 값이 0일 경우 공백을 리턴한다.
         * return 통화적용이 된 문자열
         */
        fun makeStringWithComma(value: String?, ignoreZero: Boolean): String? {
            try {
                if (value == null || value.isEmpty()) {
                    return "0"
                } else if (value.indexOf(".") >= 0) {
                    var valueNum = value.toDouble()
                    return if (ignoreZero && valueNum == 0.0) return "0" else DecimalFormat("###,##0.00").format(valueNum)
                } else {
                    var valueNum = value.toLong()
                    return if (ignoreZero && valueNum == 0L) return "0" else DecimalFormat("###,##0.00").format(valueNum)
                }
            } catch (e: Exception) {
                return null
            }
        }

        /**
         * 포맷을 주어 원하는 문자열로 변경한다. 소수점 이하 문자열 자르기 등 "0.#"
         * <p>
         * param value
         * param pattern
         * return
         */
        fun getDecimalFormatStr(value: String?, pattern: String?): String? {
            try {
                if (value?.indexOf(".") != -1) {
                    var valueNum: Double? = value?.toDouble()

                    if (pattern != null) {
                        return DecimalFormat(pattern).format(valueNum)
                    }
                }
            } catch (e: Exception) {
                return ""
            }

            return value
        }

        /**
         * 주소의 시군을 추출해 낸다.
         *
         * @param addr
         * @return
         */
        fun getSiGun(addr: String?): String {
            var result = ""
            var addrArray = addr?.split(" ")

            if (addrArray != null) {
                if (addrArray.size > 2 && addrArray[2].indexOf("동") != -1
                        || addrArray.size > 2 && addrArray[2].indexOf("읍") != -1
                        || addrArray.size > 2 && addrArray[2].indexOf("면") != -1) {
                    result = addrArray[2]
                } else if (addrArray.size > 2 && addrArray[3].indexOf("동") != -1
                        || addrArray.size > 2 && addrArray[3].indexOf("읍") != -1
                        || addrArray.size > 2 && addrArray[3].indexOf("면") != -1) {
                    result = addrArray[3]
                }
            }

            return result
        }

        /**
         * 해당 URL 전체 택스트 내에서 대상 키 혹은 값이 중복되는 것이 n개 이상 존재하는지 확인
         * @param fullText
         * @param searchTarget
         * @return
         */
        fun getUrlTextOverlapCount(fullText: String?, searchTarget: String): Int {
            var cnt = 0
            if (fullText != null && "" != fullText) {
                var arr: List<String> = fullText.split("&")
                arr.indices
                        .filter { arr[it].contains(searchTarget) }
                        .forEach { cnt++ }
            }

            return cnt
        }

        /**
         * 신 주소 방식을 사용하고 있는지 검사한다.
         *
         * @param address
         * @return
         */
        fun isNewAddress(address: String?): Boolean {
            var result = false
            var addrArray = address?.split(" ")

            if (addrArray != null) {
                for (addr in addrArray) {
                    if (addr.lastIndexOf("로") > 0 || addr.lastIndexOf("길") > 0) {
                        result = true
                    }
                }
            }

            return result
        }

        /**
         * 'yyyy년 m월 n주차' 텍스트 문구를 만들어 반환
         * @param context
         * @return 현재 날짜에 해당하는 'yyyy년 m월 n주차 랭킹' 문자열.
         */
        fun makeWeekInfoString(context: Context, yearAppend: Boolean): String {
            var calendar = GregorianCalendar.getInstance(Locale.getDefault())

            if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                calendar.add(Calendar.DATE, -2)
            }

            return (if (yearAppend) calendar.get(Calendar.YEAR).toString() else "") +
                    (calendar.get(Calendar.MONTH) + 1 - Calendar.JANUARY) + context.getString(R.string.utils_month) + " " +
                    calendar.get(Calendar.WEEK_OF_MONTH) + context.getString(R.string.utils_month_of_week)
        }

        /**
         * 문자열의 Encoding 상태를 확인하여 해당 인코딩으로 변환하여 반환
         * @param word
         * @return
         */
        fun encodeChecked(word: String): String {
            Logger.d(TAG, ("인코딩 체크 전 문자열 : $word"))
            var returnWord: String = ""

            try {
                try {
                    var charSet: Array<String> = arrayOf("utf-8", "euc-kr", "8859_1", "x-windows-949", "iso-8859-1")

                    for (i in charSet.indices) {
                        for (j in charSet.indices) {
                            returnWord = String(word.toByteArray(), Charset.forName(charSet[j]))

                            if (returnWord.contains("¿") || returnWord.contains("¢")
                                    || returnWord.contains("�") || returnWord.contains("Ã")
                                    || returnWord.contains("帮") || returnWord.contains("«")) {
                                Logger.d(TAG, "returnWord : " + returnWord)
                            } else {
                                Logger.d(TAG, "returnWord : " + returnWord)
                                return returnWord
                            }
                        }
                    }
                } catch (e: Exception) {
                } finally {
                    Logger.e(TAG, "returnWord : " + returnWord)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return word
        }

        /**
         * UTF-8 형식으로 UrlEncoding
         *
         * @param originStr
         * @return
         */
        fun encodeUTF8(originStr: String?): String? {
            return try {
                URLEncoder.encode(originStr, "UTF-8")
            } catch (e: Exception) {
                originStr
            }
        }

        /**
         * URLEncoding 된 텍스트를 UTF-8 텍스트로 변환
         *
         * @param encodeStr
         * @return
         */
        fun decodeUTF8(encodeStr: String?): String? {
            return try {
                URLDecoder.decode(encodeStr, "UTF-8")
            } catch (e: Exception) {
                encodeStr
            }
        }

        fun encodeUTF8HttpGetParameter(addr: String?): String {
            try {
                var e = StringBuffer()
                var index = 0

                if (addr != null) {
                    while (index < addr.length) {
                        var first: Int = addr.indexOf("=", index)
                        if (first > -1) {
                            var second = addr.indexOf("&", first)
                            ++first
                            e.append(addr.substring(index, first))
                            if (second == -1) {
                                second = addr.length
                            }

                            index = if (first == second) {
                                second
                            } else {
                                val target = addr.substring(first, second)
                                e.append(encodeUTF8(target))
                                second
                            }
                        } else {
                            if (index == 0) {
                                return addr
                            }

                            index = addr.length
                        }
                    }
                }

                return e.toString()
            } catch (e: Exception) {
                return "INVALID"
            }
        }

        /**
         * '-' 가 들어간 전화 번호나 국제전화 번호가 포함된 경우 국내 번호로 치환
         * @param phoneNumber
         */
        fun phoneNumberReplace(phoneNumber: String?): String? {
            var trimPhoneNumber: String? = phoneNumber?.replace("-", "", true)
                    ?.replace(" ", "", true)
                    ?.trim()

            if (trimPhoneNumber != null) {
                if (trimPhoneNumber.contains("+82")) {
                    trimPhoneNumber = "0" + trimPhoneNumber.replace(oldValue = "+82", newValue = "0")
                } else if (trimPhoneNumber.substring(startIndex = 0, endIndex = 2) == "82") {
                    trimPhoneNumber = "0" + trimPhoneNumber.substring(startIndex = 2, endIndex = trimPhoneNumber.length)
                } else {
                    if (trimPhoneNumber.contains(other = "+1")) {
                        trimPhoneNumber = trimPhoneNumber.replace(oldValue = "+1", newValue = "")
                    }
                }
            } else {
                trimPhoneNumber = null
            }

            return trimPhoneNumber
        }

        /**
         * TextView 에 ColorSpannable 을 적용 하여 설정
         * @param view
         * @param fullText
         * @param subText
         * @param color
         */
        fun setTextViewColorPartial(view: TextView?, fullText: String?, subText: String?, color: Int) {
            view?.setText(fullText, TextView.BufferType.SPANNABLE)

            try {
                var spannableStr: Spannable? = view?.text as Spannable
                if (spannableStr != null) {
                    var index: Int? = fullText?.indexOf(string = subText!!)
                    spannableStr.setSpan(ForegroundColorSpan(color), index!!, subText?.length!!, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            } catch (e: Exception) {
            }
        }

        /**
         * 문자열의 해당 Index 에 대한 한글 초성을 획득
         * @param str
         * @param index
         * @return
         */
        fun getUTF8Chosung(str: String, index: Int): String {
            var cho = arrayOf("ㄱ", "ㄲ", "ㄴ", "ㄷ", "ㄸ", "ㄹ", "ㅁ", "ㅂ",
                    "ㅃ", "ㅅ", " ㅆ", "ㅇ", "ㅈ", "ㅉ", "ㅊ", "ㅋ", "ㅌ", "ㅍ", "ㅎ")

            var code: Int = str.codePointAt(index = index)
            if (code in 44032..55203) {
                var unicode: Int = code - 44032
                var choIndex: Int = unicode / 21 / 28
                return cho[choIndex]
            }

            return str.substring(startIndex = index, endIndex = index + 1)
        }

        /**
         * 문자열의 각 문자당 초성만을 획득
         * @param str
         * @return
         */
        fun getUTF8Chosung(str: String): String {
            var cho = arrayOf("ㄱ", "ㄲ", "ㄴ", "ㄷ", "ㄸ", "ㄹ", "ㅁ", "ㅂ",
                    "ㅃ", "ㅅ", " ㅆ", "ㅇ", "ㅈ", "ㅉ", "ㅊ", "ㅋ", "ㅌ", "ㅍ", "ㅎ")

            var code: Int
            var result = ""

            for (i: Int in str.indices) {
                code = str.codePointAt(index = i)
                if (code in 44032..55203) {
                    var unicode: Int = code - 44032
                    var choIndex: Int = unicode / 21 / 28
                    result += cho[choIndex]
                }
            }

            return if ("" == result) str.substring(startIndex = 0, endIndex = 1) else result
        }

        /**
         * 랜덤한 문자열을 원하는 길이만큼 반환
         * @param length
         * @return
         */
        fun getRandomString(length: Int): String {
            var buffer: StringBuffer = StringBuffer()
            var random: Random = Random()

            var chars: List<String> = "a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,amount,u,v,w,x,y,z,0,1,2,3,4,5,6,7,8,9"
                    .split(",")

            var loop = 0
            while (loop < length) {
                buffer.append(chars[random.nextInt(chars.size)])
                ++loop
            }

            return buffer.toString()
        }

        /**
         * 해당 문자열의 각 Char 에 대한 Code Number 를 획득
         */
        fun parseInt(value: String?): IntArray? {
            var result: IntArray? = null

            if (value != null && value.indexOf(string = "/") != -1) {
                var valueArray: List<String> = value.split("/")
                result = IntArray(valueArray.size)

                for (i in valueArray.indices) {
                    result[i] = Integer.parseInt(valueArray[i])
                }
            }

            return result
        }

        /**
         * 숫자로된 배열을 Int 타입의 배열로 변환하여 반환
         * @param pArray
         * @return
         */
        @Throws(Exception::class)
        fun convertStringArrayToIntArray(pArray: Array<String>?): IntArray? {
            if (pArray == null) return null

            var intArray = IntArray(pArray.size)
            for (i in pArray.indices) {
                intArray[i] = Integer.parseInt(pArray[i])
            }
            return intArray
        }

        /**
         * 인스타 그램 형식의 카운트수를 반환
         * @param count
         * @return
         */
        fun getSnsStyleCount(countText: String): String {
            if (TextUtils.isEmpty(countText)) {
                return ""
            }

            return getSnsStyleCount(countText.toInt())
        }

        fun getSnsStyleCount(count: Int): String {
            return when {
                count == 0 -> ""
                count > 1000000 -> String.format("%.1f", count.toDouble() / 1000000.toDouble(), Locale.US) + "M"
                count > 1000 -> String.format("%.1f", count.toDouble() / 1000.toDouble(), Locale.US) + "k"
                else -> count.toString()
            }
        }

        fun getSnsStyleCountZeroBase(count: Int): String {
            return when {
                count == 0 -> "0"
                count > 1000000 -> String.format("%.1f", count.toDouble() / 1000000.toDouble(), Locale.US) + "M"
                count > 1000 -> String.format("%.1f", count.toDouble() / 1000.toDouble(), Locale.US) + "k"
                else -> count.toString()
            }
        }

        fun convertHashTagText(tag: String): String {
            if (!tag.isNullOrEmpty()) {
                var hashTag = ""
                var split = tag?.split(",")
                if (split!!.isNotEmpty()) {
                    for (result in split!!) {
                        hashTag += "#$result "
                    }
                }

                return hashTag
            }

            return ""
        }

        /**
         * 인스타 그램 형식의 카운트수를 애니메이션으로 설정
         * @param initialValue
         * @param finalValue
         * @param textView
         */
        fun getSnsStyleCountWithAnimation(initialValue: Int, finalValue: Int, textView: TextView) {
            val animator = ValueAnimator.ofInt(initialValue, finalValue)
            animator.duration = 1000
            animator.addUpdateListener { animation: ValueAnimator -> textView.text = getSnsStyleCount(animation.animatedValue as Int) }
            animator.start()
        }

        /**
         * 댓글이 작성된 시기를 Message Style 에 맞도록 처리하여 Text 를 반환
         * @param replyTimeMills
         */
        fun getMessengerStyleDateString(ctx: Context, replyTimeMills: Long): String {
            // 표시 범위 : 방금(1분 이내), x 분전, x 시간전, x일전, 일주일전, 등록 일시
            val secondsTimeMillis = 1000
            val minTimeMillis = secondsTimeMillis * 60
            val hourTimeMillis = minTimeMillis * 60
            val dayTimeMillis = hourTimeMillis * 24
            val weekTimeMillis = dayTimeMillis * 7

            val ago = System.currentTimeMillis() - replyTimeMills

            val ret = LongArray(5)

            if (ago >= weekTimeMillis)
                ret[0] = ago / weekTimeMillis          // Week
            else if (ago >= dayTimeMillis)
                ret[1] = ago / dayTimeMillis           // Day
            else if (ago >= hourTimeMillis)
                ret[2] = ago / hourTimeMillis          // hour
            else if (ago >= minTimeMillis)
                ret[3] = ago / minTimeMillis           // Min
            else if (ago >= secondsTimeMillis) ret[4] = ago / secondsTimeMillis       // Seconds

            return if (ret[0] > 0) {
                if (ret[0] > 4) {
                    // 4주 이상된 댓글 게시물은 등록일자 원본을 표시
                    TimeUtils.Companion.getDateStringFromDateMills(replyTimeMills)
                } else {
                    // 4주 미만의 댓글 게시물을 "x주 전" 으로 표시
                    String.format(ctx.getString(R.string.msg_reply_time_weeks), ret[0])
                }
            } else if (ret[1] > 0) {
                String.format(ctx.getString(R.string.msg_reply_time_days), ret[1])
            } else if (ret[2] > 0) {
                String.format(ctx.getString(R.string.msg_reply_time_hours), ret[2])
            } else if (ret[3] > 0) {
                String.format(ctx.getString(R.string.msg_reply_time_minute), ret[3])
            } else if (ret[3] >= 10) {
                String.format(ctx.getString(R.string.msg_reply_time_seconds), ret[4])
            } else {
                ctx.getString(R.string.msg_reply_time_now)
            }
        }

        fun getDateTimeFromString(dateString: String): Long {
            var simpleDateFormat: SimpleDateFormat? = null

            if (TextUtils.isEmpty(dateString)) {
                return 0
            }

            //2016-03-07 12:20:32.0
            //2016-03-07 12:20:32
            if (simpleDateFormat == null) {
                simpleDateFormat = SimpleDateFormat()
            }

            if (dateString.contains(".")) {
                simpleDateFormat.applyPattern("yyyy-MM-dd HH:mm:ss.S")
            } else {
                simpleDateFormat.applyPattern("yyyy-MM-dd HH:mm:ss")
            }

            var timeMills: Long = 0
            try {
                timeMills = simpleDateFormat.parse(dateString).getTime()
            } catch (e: ParseException) {
                timeMills = 0
                Logger.p(e)
            } catch (e: StringIndexOutOfBoundsException) {
                timeMills = 0
                Logger.p(e)
            }

            //간혹 내려주는 시간이 hh 포맷으로는 파싱안될때가 있음.
            if (timeMills == 0L) {
                if (dateString.contains(".")) {
                    simpleDateFormat.applyPattern("yyyy-MM-dd HH:mm:ss.S")
                } else {
                    simpleDateFormat.applyPattern("yyyy-MM-dd HH:mm:ss")
                }

                try {
                    timeMills = simpleDateFormat.parse(dateString).getTime()
                } catch (e: ParseException) {
                    timeMills = 0
                    Logger.p(e)
                }

            }

            return timeMills
        }
    }
}