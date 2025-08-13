package com.example.newez

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newez.adapter.MainMenuAdapter
import com.example.newez.model.IpUpdateRequest
import com.example.newez.model.MainMenuItem
import com.example.newez.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupUserInfo()
        setupMainMenu()
        updateUserIpAddress()
    }

    private fun setupUserInfo() {
        val userName = intent.getStringExtra("USER_NAME")
        val endDate = intent.getStringExtra("SUBSCRIPTION_END_DATE")

        findViewById<TextView>(R.id.user_name_textview).text = userName ?: "이름 없음"
        findViewById<TextView>(R.id.expire_date_textview).text = "종료일: ${endDate ?: "미지정"}"
    }

    private fun setupMainMenu() {
        val recyclerView = findViewById<RecyclerView>(R.id.main_menu_recyclerview)
        val adultContentAllowed = intent.getBooleanExtra("ADULT_CONTENT_ALLOWED", false)


        val menuItems = listOf(
            MainMenuItem("생방송", R.drawable.icon_live),
            MainMenuItem("영화", R.drawable.icon_movie),
            MainMenuItem("VOD다시보기", R.drawable.icon_vod),
            MainMenuItem("실시간다시보기", R.drawable.icon_realtime),
            MainMenuItem("성인방송", R.drawable.icon_19),
            MainMenuItem("검색", R.drawable.icon_serch)
        )

        recyclerView.adapter = MainMenuAdapter(menuItems, adultContentAllowed)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    }

    private fun updateUserIpAddress() {
        lifecycleScope.launch {
            try {
                val publicIp = getPublicIp()
                if (publicIp == null) {
                    Log.e("MainActivity", "공인 IP 주소를 가져오는데 실패했습니다.")
                    return@launch
                }

                val deviceId = getSavedDeviceId()
                if (deviceId == null) {
                    Log.e("MainActivity", "저장된 Device ID가 없습니다. (최초 개통 필요)")
                    return@launch
                }

                sendIpToServer(deviceId, publicIp)

            } catch (e: Exception) {
                Log.e("MainActivity", "IP 업데이트 중 오류 발생", e)
            }
        }
    }

    private fun getSavedDeviceId(): String? {
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return prefs.getString("device_id", null)
    }

    private suspend fun getPublicIp(): String? = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://api.ipify.org")
            val urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.connectTimeout = 5000
            BufferedReader(InputStreamReader(urlConnection.inputStream)).use {
                it.readLine()
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "getPublicIp 실패", e)
            null
        }
    }

    private suspend fun sendIpToServer(deviceId: String, ip: String) {
        try {
            val request = IpUpdateRequest(ip = ip)
            val response = RetrofitClient.instance.updateUserIp(deviceId, request)

            if (response.isSuccessful) {
                Log.d("MainActivity", "IP 주소($ip)가 서버에 성공적으로 업데이트되었습니다. (deviceId: $deviceId)")
            } else {
                Log.e("MainActivity", "서버 IP 업데이트 실패: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "sendIpToServer 실패", e)
        }
    }
}