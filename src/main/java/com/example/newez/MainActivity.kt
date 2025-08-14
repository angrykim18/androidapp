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
import com.example.newez.data.UserManager
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

        observeUserInfo()
        setupMainMenu()
        updateUserIpAddress()
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            UserManager.refresh(this@MainActivity)
        }
    }

    private fun observeUserInfo() {
        val userNameTextView = findViewById<TextView>(R.id.user_name_textview)
        val endDateTextView = findViewById<TextView>(R.id.expire_date_textview)

        UserManager.userName.observe(this) { name ->
            userNameTextView.text = name ?: "이름 없음"
        }

        UserManager.subscriptionEndDate.observe(this) { endDate ->
            endDateTextView.text = "종료일: ${endDate ?: "미지정"}"
        }

        // [삭제] isAdultContentAllowed가 변경될 때 메뉴를 다시 그리는 로직을 제거했습니다.
        // 이제 메뉴는 리셋되지 않으므로 포커스가 유지됩니다.
    }

    private fun setupMainMenu() {
        val recyclerView = findViewById<RecyclerView>(R.id.main_menu_recyclerview)
        val menuItems = listOf(
            MainMenuItem("생방송", R.drawable.icon_live),
            MainMenuItem("영화", R.drawable.icon_movie),
            MainMenuItem("VOD다시보기", R.drawable.icon_vod),
            MainMenuItem("실시간다시보기", R.drawable.icon_realtime),
            MainMenuItem("성인방송", R.drawable.icon_19),
            MainMenuItem("검색", R.drawable.icon_serch)
        )
        // [수정] adultContentAllowed를 어댑터에 전달하지 않습니다.
        recyclerView.adapter = MainMenuAdapter(menuItems)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    }

    // --- 아래 함수들은 기존 코드와 동일합니다. (변경 없음) ---
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
                    Log.e("MainActivity", "저장된 Device ID가 없습니다.")
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
            BufferedReader(InputStreamReader(urlConnection.inputStream)).use { it.readLine() }
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
                Log.d("MainActivity", "IP 주소($ip)가 서버에 성공적으로 업데이트되었습니다.")
            } else {
                Log.e("MainActivity", "서버 IP 업데이트 실패: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "sendIpToServer 실패", e)
        }
    }
}
