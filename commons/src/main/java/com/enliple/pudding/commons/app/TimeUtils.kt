package com.enliple.pudding.commons.app

import android.text.TextUtils
import com.enliple.pudding.commons.log.Logger
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by hkcha on 2018-01-03.
 */
class TimeUtils {
    companion object {

        /**
         * DateFormat 형식 String 을 Unix Long Time 값으로 변환
         * @param dateStr
         * @return
         */
        fun getDateTimeFromString(dateStr: String?): Long {
            if (TextUtils.isEmpty(dateStr)) {
                return 0L
            } else {

                var dateFormat = SimpleDateFormat()

                if (dateStr!!.contains(".")) dateFormat.applyPattern("yyyy-MM-dd HH:mm:ss.S")
                else dateFormat.applyPattern("yyyy-MM-dd HH:mm:ss")

                var timeMills = 0L
                try {
                    timeMills = dateFormat.parse(dateStr).time
                } catch (e: ParseException) {
                    Logger.p(e)
                } catch (e: StringIndexOutOfBoundsException) {
                    Logger.p(e)
                }

                //간혹 내려주는 시간이 hh 포맷으로는 파싱안될때가 있음.
                if (timeMills == 0L) {
                    if (dateStr.contains(".")) {
                        dateFormat.applyPattern("yyyy-MM-dd HH:mm:ss.S")
                    } else {
                        dateFormat.applyPattern("yyyy-MM-dd HH:mm:ss")
                    }
                }

                try {
                    timeMills = dateFormat.parse(dateStr).time
                } catch (e: ParseException) {
                    Logger.p(e)
                } catch (e: StringIndexOutOfBoundsException) {
                    Logger.p(e)
                }

                return timeMills
            }
        }

        /**
         * Unix Long Time 형식의 시간값을 "yyyy-MM-dd HH:mm:ss" 형식 으로 변환
         */
        fun getDateStringFromTimeMills(timeMills: Long): String {
            return SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(timeMills)
        }

        /**
         * Unix Long Time 형식의 시간값을 "yyyy-MM-dd" 형식으로 변환
         */
        fun getDateStringFromDateMills(timeMills: Long): String {
            return SimpleDateFormat("yyyy-MM-dd").format(timeMills)
        }

        /**
         * Unix Long Time 형식의 시간값을 "MM.dd" 형식으로 변환
         */
        fun getDateStringOnlyDaysFromTimeMills(timeMills: Long): String {
            return SimpleDateFormat("MM.dd").format(Date(timeMills))
        }

        /**
         * Unix Long Time 형식의 시간값을 "HH" 형식으로 변환
         */
        fun getHourStringOnlyDaysFromTimeMills(timeMills: Long): String {
            return SimpleDateFormat("HH").format(Date(timeMills))
        }

        /**
         * Unix Long Time 형식의 시간값을 이용하여 현재 나이를 계산
         */
        fun getAgeFromYear(timeMills: Long): Int {

            val calendar = Calendar.getInstance()
            val currentYear = calendar.get(Calendar.YEAR)
            var age = 0
            try {
                var date = Date(timeMills)
                var cal = GregorianCalendar()
                cal.time = date

                var year = cal.get(Calendar.YEAR)
                age = currentYear - year
            } catch (e: Exception) {
            }

            return age
        }
    }
}