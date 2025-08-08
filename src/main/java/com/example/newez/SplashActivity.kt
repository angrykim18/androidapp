package com.example.newez

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
import okhttp3.OkHttpClient // ✅ [추가]
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.UUID
import java.util.concurrent.TimeUnit // ✅ [추가]

data class SplashUser(
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

    // ✅ [수정] 5초 타임아웃을 설정한 네트워크 클라이언트를 생성합니다.
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    // ✅ [수정] 위에서 만든 타임아웃 클라이언트를 사용하도록 Retrofit 설정 변경
    private val retrofit = Retrofit.Builder()
        .baseUrl("http://192.168.0.2:8081")
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
                moveToActivation()
                return@launch
            }

            val encodedDeviceId = URLEncoder.encode(deviceId, StandardCharsets.UTF_8.toString())

            for (attempt in 1..MAX_ATTEMPTS) {
                withContext(Dispatchers.Main) {
                    statusTextView.text = "서버에 연결 중... (시도 $attempt / $MAX_ATTEMPTS)"
                }
                Log.d("SplashActivityLog", "서버 연결 시도 ($attempt / $MAX_ATTEMPTS)")

                try {
                    val response = apiService.getUserByDeviceId(encodedDeviceId)

                    if (response.isSuccessful) {
                        moveToMain(response.body())
                        return@launch
                    } else {
                        if (response.code() == 404) {
                            moveToActivation()
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

    private fun moveToMain(user: SplashUser?) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("USER_NAME", user?.name)
            putExtra("SUBSCRIPTION_END_DATE", user?.subscriptionEndDate)
            putExtra("ADULT_CONTENT_ALLOWED", user?.adultContentAllowed ?: false)
        }
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        finish()
    }

    private fun moveToActivation() {
        val intent = Intent(this, ActivationActivity::class.java)
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