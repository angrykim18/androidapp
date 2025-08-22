package com.example.newez

import android.content.Context
import android.content.Intent
import android.media.MediaDrm
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.*
import com.example.newez.BuildConfig

// 서버와 통신하기 위한 데이터 클래스들
data class HqActivationRequest(val deviceId: String, val adminPassword: String)
data class HqErrorResponse(val message: String)

// 본사 개통용 Retrofit API 인터페이스
interface HqActivationApiService {
    @POST("/api/admins/activate-device")
    suspend fun activateDeviceByAdmin(@Body request: HqActivationRequest): retrofit2.Response<SplashUser>
}

class HqActivationActivity : AppCompatActivity() {

    private val WIDEVINE_UUID = UUID(-0x121074568629b532L, -0x5c37d8232ae2de13L)
    private var widevineId: String? = null

    private val retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService = retrofit.create(HqActivationApiService::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hq_activation)

        val passwordInput = findViewById<EditText>(R.id.password_input)
        val activateButton = findViewById<Button>(R.id.activate_button)
        activateButton.isEnabled = false

        CoroutineScope(Dispatchers.IO).launch {
            widevineId = getWidevineId()
            withContext(Dispatchers.Main) {
                activateButton.isEnabled = widevineId != null
                if (widevineId == null) {
                    Toast.makeText(applicationContext, "기기 ID를 가져올 수 없습니다.", Toast.LENGTH_LONG).show()
                }
            }
        }

        activateButton.setOnClickListener {
            val password = passwordInput.text.toString()
            if (password.isEmpty()) {
                Toast.makeText(applicationContext, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val request = HqActivationRequest(widevineId!!, password)
                    val response = apiService.activateDeviceByAdmin(request)

                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            Toast.makeText(applicationContext, "본사 개통 성공!", Toast.LENGTH_SHORT).show()
                            moveToMain(response.body(), widevineId)
                        } else {
                            // ▼▼▼ [수정된 부분] 에러 메시지 처리를 더 안전하게 변경했습니다. ▼▼▼
                            val errorBody = response.errorBody()?.string()
                            val serverMessage = try {
                                Gson().fromJson(errorBody, HqErrorResponse::class.java)?.message
                            } catch (e: Exception) {
                                null // JSON 파싱 실패 시 null 반환
                            }

                            // 서버 메시지가 비어있으면 직접 만든 메시지를, 아니면 서버 메시지를 사용합니다.
                            val displayMessage = if (serverMessage.isNullOrEmpty()) {
                                "비밀번호가 일치하지 않거나 서버 응답 오류입니다. (코드: ${response.code()})"
                            } else {
                                serverMessage
                            }
                            Toast.makeText(applicationContext, "개통 실패: $displayMessage", Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(applicationContext, "서버 연결에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun getWidevineId(): String? {
        // ... (이하 동일)
        return try {
            val mediaDrm = MediaDrm(WIDEVINE_UUID)
            val widevineIdAsBytes = mediaDrm.getPropertyByteArray(MediaDrm.PROPERTY_DEVICE_UNIQUE_ID)
            Base64.encodeToString(widevineIdAsBytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            null
        }
    }

    private fun moveToMain(user: SplashUser?, deviceId: String?) {
        // ... (이하 동일)
        if (deviceId != null) {
            val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            prefs.edit().putString("device_id", deviceId).apply()
            Log.d("HqActivationActivity", "SharedPreferences에 device_id 저장: $deviceId")
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
}