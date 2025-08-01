package com.example.newez

import android.content.Intent
import android.media.MediaDrm
import android.os.Bundle
import android.util.Base64
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.UUID
import android.util.Log
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

data class SplashUser(val name: String?, val subscriptionEndDate: String?)

interface SplashApiService {
    @GET("/api/users/device/{deviceId}")
    suspend fun getUserByDeviceId(@Path("deviceId") deviceId: String): retrofit2.Response<SplashUser>
}


class SplashActivity : AppCompatActivity() {

    private val WIDEVINE_UUID = UUID(-0x121074568629b532L, -0x5c37d8232ae2de13L)

    private val retrofit = Retrofit.Builder()
        .baseUrl("http://192.168.0.2:8081")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService = retrofit.create(SplashApiService::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        CoroutineScope(Dispatchers.IO).launch {
            val deviceId = getWidevineId()
            Log.d("SplashActivityLog", "조회된 Device ID: $deviceId")

            if (deviceId == null) {
                Log.e("SplashActivityLog", "Device ID가 null이므로 개통 화면으로 이동합니다.")
                moveToActivation()
                return@launch
            }

            try {
                // ✅ [핵심 수정] deviceId를 URL에 사용하기 안전한 형태로 인코딩합니다.
                val encodedDeviceId = URLEncoder.encode(deviceId, StandardCharsets.UTF_8.toString())

                Log.d("SplashActivityLog", "서버에 사용자 정보 요청: /api/users/device/$encodedDeviceId")
                // ✅ [핵심 수정] 인코딩된 ID로 서버에 요청을 보냅니다.
                val response = apiService.getUserByDeviceId(encodedDeviceId)

                if (response.isSuccessful) {
                    Log.d("SplashActivityLog", "서버 응답 성공. 메인 화면으로 이동합니다.")
                    moveToMain(response.body())
                } else {
                    Log.w("SplashActivityLog", "서버 응답 실패 (Code: ${response.code()}). 개통 화면으로 이동합니다.")
                    moveToActivation()
                }
            } catch (e: Exception) {
                Log.e("SplashActivityLog", "서버 연결 실패 또는 예외 발생. 개통 화면으로 이동합니다.", e)
                moveToActivation()
            }
        }
    }

    private fun moveToMain(user: SplashUser?) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("USER_NAME", user?.name)
            putExtra("SUBSCRIPTION_END_DATE", user?.subscriptionEndDate)
        }
        startActivity(intent)
        // ✅ [애니메이션 추가] 화면이 넘어갈 때 페이드 효과를 적용합니다.
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        finish()
    }

    private fun moveToActivation() {
        val intent = Intent(this, ActivationActivity::class.java)
        startActivity(intent)
        // ✅ [애니메이션 추가] 화면이 넘어갈 때 페이드 효과를 적용합니다.
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        finish()
    }

    private fun getWidevineId(): String? {
        return try {
            val mediaDrm = MediaDrm(WIDEVINE_UUID)
            val widevineIdAsBytes = mediaDrm.getPropertyByteArray(MediaDrm.PROPERTY_DEVICE_UNIQUE_ID)
            Base64.encodeToString(widevineIdAsBytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            null
        }
    }
}