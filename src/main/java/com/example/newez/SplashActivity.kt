package com.example.newez

import android.content.Context
import android.content.Intent
import android.media.MediaDrm
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.UUID
import java.util.concurrent.TimeUnit
import com.example.newez.BuildConfig
import androidx.annotation.Keep


@Keep
data class SplashUser(
    val id: Long,
    val name: String?,
    val subscriptionEndDate: String?,
    val adultContentAllowed: Boolean? = false
)

interface SplashApiService {
    @GET("/api/users/device/{deviceId}")
    suspend fun getUserByDeviceId(@Path("deviceId") deviceId: String): retrofit2.Response<SplashUser>
}

class SplashActivity : AppCompatActivity() {

    private val WIDEVINE_UUID = UUID(-0x121074568629b532L, -0x5c37d8232ae2de13L)

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService = retrofit.create(SplashApiService::class.java)

    companion object {
        private const val MAX_ATTEMPTS = 10
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_splash)

        val statusTextView = findViewById<TextView>(R.id.status_textview)

        lifecycleScope.launch(Dispatchers.IO) {
            val deviceId = getWidevineId()
            if (deviceId == null) {
                // Widevine ID를 얻지 못하면 바로 개통 선택 화면으로 이동
                moveToActivationChoice()
                return@launch
            }

            //val encodedDeviceId = URLEncoder.encode(deviceId, StandardCharsets.UTF_8.toString())

            for (attempt in 1..MAX_ATTEMPTS) {
                withContext(Dispatchers.Main) {
                    statusTextView.text = "서버에 연결 중... (시도 $attempt / $MAX_ATTEMPTS)"
                }
                Log.d("SplashActivityLog", "서버 연결 시도 ($attempt / $MAX_ATTEMPTS)")

                try {
                    val response = apiService.getUserByDeviceId(deviceId)

                    if (response.isSuccessful) {
                        moveToMain(response.body(), deviceId)
                        return@launch
                    } else {
                        // ▼▼▼ [수정된 부분] 404 에러 시 ActivationChoiceActivity를 호출합니다. ▼▼▼
                        if (response.code() == 404) {
                            moveToActivationChoice()
                            return@launch
                        }
                        Log.w("SplashActivityLog", "서버 응답 실패 (Code: ${response.code()}). 잠시 후 재시도합니다.")
                    }
                } catch (e: Exception) {
                    Log.e("SplashActivityLog", "서버 연결 실패. 잠시 후 재시도합니다.", e)
                }
            }

            withContext(Dispatchers.Main) {
                Log.e("SplashActivityLog", "최종 연결 실패. 사용자에게 메시지를 표시합니다.")
                statusTextView.text = "서버에 연결할 수 없습니다.\n네트워크 상태를 확인하고 앱을 다시 시작해주세요."
            }
        }
    }

    private fun moveToMain(user: SplashUser?, deviceId: String?) {
        if (deviceId != null) {
            val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            prefs.edit().putString("device_id", deviceId).apply()
            Log.e("SplashActivity", "SharedPreferences에 device_id 저장 성공: $deviceId")
        } else {
            Log.e("SplashActivity", "SharedPreferences 저장 실패: deviceId가 null입니다.")
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("USER_ID", user?.id)
            putExtra("USER_NAME", user?.name)
            putExtra("SUBSCRIPTION_END_DATE", user?.subscriptionEndDate)
            putExtra("ADULT_CONTENT_ALLOWED", user?.adultContentAllowed ?: false)
        }
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        finish()
    }

    // ▼▼▼ [수정된 부분] ActivationChoiceActivity로 이동하는 함수입니다. ▼▼▼
    private fun moveToActivationChoice() {
        val intent = Intent(this, ActivationChoiceActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        finish()
    }

    private fun getWidevineId(): String? {
        return try {
            val mediaDrm = MediaDrm(WIDEVINE_UUID)
            val widevineIdAsBytes = mediaDrm.getPropertyByteArray(MediaDrm.PROPERTY_DEVICE_UNIQUE_ID)
            Base64.encodeToString(widevineIdAsBytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}