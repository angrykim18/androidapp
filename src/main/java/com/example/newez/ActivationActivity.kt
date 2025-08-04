package com.example.newez

import android.content.Intent
import android.media.MediaDrm
import android.os.Bundle
import android.util.Base64
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

data class User(val name: String?, val subscriptionEndDate: String?)
data class ActivationRequest(val deviceId: String, val agencyLoginId: String, val agencyPassword: String)
data class ErrorResponse(val message: String)

interface ActivationApiService {
    @POST("/api/users/activate")
    suspend fun activateDevice(@Body request: ActivationRequest): retrofit2.Response<User>
}

class ActivationActivity : AppCompatActivity() {

    private val WIDEVINE_UUID = UUID(-0x121074568629b532L, -0x5c37d8232ae2de13L)
    private var widevineId: String? = null

    private val retrofit = Retrofit.Builder()
        .baseUrl("http://192.168.0.2:8081")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService = retrofit.create(ActivationApiService::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_activation)

        val activateButton = findViewById<Button>(R.id.activate_button)
        activateButton.isEnabled = false

        CoroutineScope(Dispatchers.IO).launch {
            widevineId = getWidevineId()
            withContext(Dispatchers.Main) {
                activateButton.isEnabled = widevineId != null
            }
        }

        val agencyIdInput = findViewById<EditText>(R.id.agency_id_input)
        val agencyPasswordInput = findViewById<EditText>(R.id.agency_password_input)

        activateButton.setOnClickListener {
            val agencyId = agencyIdInput.text.toString()
            val agencyPassword = agencyPasswordInput.text.toString()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val request = ActivationRequest(widevineId!!, agencyId, agencyPassword)
                    val response = apiService.activateDevice(request)

                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            val user = response.body()
                            Toast.makeText(applicationContext, "개통 성공!", Toast.LENGTH_SHORT).show()

                            val intent = Intent(this@ActivationActivity, MainActivity::class.java).apply {
                                putExtra("USER_NAME", user?.name)
                                putExtra("SUBSCRIPTION_END_DATE", user?.subscriptionEndDate)
                            }
                            startActivity(intent)

                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                            finish()

                        } else {
                            val errorBody = response.errorBody()?.string()
                            val errorMessage = try {
                                Gson().fromJson(errorBody, ErrorResponse::class.java).message
                            } catch (e: Exception) { "알 수 없는 에러" }
                            Toast.makeText(applicationContext, "개통 실패: $errorMessage", Toast.LENGTH_LONG).show()
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
        return try {
            val mediaDrm = MediaDrm(WIDEVINE_UUID)
            val widevineIdAsBytes = mediaDrm.getPropertyByteArray(MediaDrm.PROPERTY_DEVICE_UNIQUE_ID)
            Base64.encodeToString(widevineIdAsBytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            null
        }
    }
}