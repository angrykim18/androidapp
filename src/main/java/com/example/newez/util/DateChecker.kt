package com.example.newez.util

import android.content.Context
import android.util.Log
import com.example.newez.network.RetrofitClient // ✅ [추가] 공용 RetrofitClient를 import 합니다.
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object DateChecker {




    suspend fun check(context: Context) = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val deviceId = prefs.getString("device_id", null)

        if (deviceId.isNullOrEmpty()) {
            Log.e("DateChecker", "SharedPreferences에 device_id가 없습니다.")
            throw IllegalStateException("사용자 정보를 찾을 수 없습니다.")
        }

        try {
            val encodedDeviceId = URLEncoder.encode(deviceId, StandardCharsets.UTF_8.name())


            val userInfo = RetrofitClient.instance.getUserInfo(encodedDeviceId)

            if (isDateExpired(userInfo.subscriptionEndDate)) {

                throw DateExpiredException()
            }

        } catch (e: Exception) {
            Log.e("DateChecker", "상태 확인 중 오류 발생", e)

            throw e
        }
    }


    private fun isDateExpired(endDateString: String?): Boolean {
        if (endDateString.isNullOrEmpty()) return true
        return try {
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
            val endDate = formatter.parse(endDateString) ?: return true
            val todayCalendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }
            endDate.before(todayCalendar.time)
        } catch (e: Exception) {
            true
        }
    }


    class DateExpiredException : Exception("시청 기간이 만료되었습니다.")
}