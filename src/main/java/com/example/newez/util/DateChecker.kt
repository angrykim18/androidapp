package com.example.newez.util

import android.content.Context
import android.util.Log
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object DateChecker {

    // --- 내부 데이터 및 API 정의 (변경 없음) ---
    private data class UserStatus(val subscriptionEndDate: String?)
    private interface CheckerApiService {
        @GET("/api/users/device/{deviceId}")
        suspend fun getUserStatus(@Path("deviceId") deviceId: String): UserStatus
    }
    private val apiService: CheckerApiService by lazy {
        Retrofit.Builder()
            .baseUrl("http://192.168.0.2:8081")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CheckerApiService::class.java)
    }

    /**
     * [수정] 사용자의 시청 가능 여부를 확인합니다.
     * 성공하면 아무것도 하지 않고, 실패 시 예외(Exception)를 던집니다.
     */
    suspend fun check(context: Context) = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val deviceId = prefs.getString("device_id", null)

        if (deviceId.isNullOrEmpty()) {
            Log.e("DateChecker", "SharedPreferences에 device_id가 없습니다.")
            throw IllegalStateException("사용자 정보를 찾을 수 없습니다.")
        }

        try {
            val encodedDeviceId = URLEncoder.encode(deviceId, StandardCharsets.UTF_8.name())
            val userStatus = apiService.getUserStatus(encodedDeviceId)

            if (isDateExpired(userStatus.subscriptionEndDate)) {
                // 만료되었으면, 직접 '만료 예외'를 던집니다.
                throw DateExpiredException()
            }
            // 만료되지 않았으면 함수가 조용히 종료됩니다. (성공)
        } catch (e: Exception) {
            Log.e("DateChecker", "상태 확인 중 오류 발생", e)
            // 받은 예외를 그대로 다시 던져서 호출한 쪽에서 처리하게 합니다.
            throw e
        }
    }

    // --- 내부 헬퍼 함수 (변경 없음) ---
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

    // 만료 예외 클래스 (변경 없음)
    class DateExpiredException : Exception("시청 기간이 만료되었습니다.")
}
