package com.example.newez

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newez.network.ApiLiveChannel
import com.example.newez.network.ApiService
import com.example.newez.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// [수정] 권한 확인 관련 import 모두 제거
// import com.example.newez.data.UserManager
// import retrofit2.HttpException
// import androidx.appcompat.app.AlertDialog

class LiveChannelActivity : AppCompatActivity(), LiveChannelAdapter.OnChannelClickListener {

    private val apiService: ApiService by lazy { RetrofitClient.instance }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_channel)

        val recyclerView = findViewById<RecyclerView>(R.id.channel_recycler_view)
        recyclerView.layoutManager = GridLayoutManager(this, 7)

        lifecycleScope.launch(Dispatchers.Main) {
            val deviceId = getDeviceId(this@LiveChannelActivity)
            if (deviceId == null) {
                Toast.makeText(applicationContext, "기기 ID를 찾을 수 없습니다. 앱을 재시작해주세요.", Toast.LENGTH_LONG).show()
                finish()
                return@launch
            }

            try {
                val channels = withContext(Dispatchers.IO) {
                    apiService.getLiveChannels(deviceId)
                }

                if (channels.isEmpty()) {
                    Toast.makeText(applicationContext, "시청 가능한 채널이 없습니다. 그룹 설정을 확인하세요.", Toast.LENGTH_LONG).show()
                } else {
                    recyclerView.adapter = LiveChannelAdapter(channels, this@LiveChannelActivity)
                }

            } catch (e: Exception) {
                Toast.makeText(applicationContext, "채널 목록을 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getDeviceId(context: Context): String? {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return prefs.getString("device_id", null)
    }

    /**
     * [수정] 클릭 시 권한 확인 없이 즉시 PlayerActivity를 실행합니다.
     * 모든 로딩과 확인 로직은 PlayerActivity가 담당합니다.
     */
    override fun onChannelClick(channel: ApiLiveChannel) {
        val intent = Intent(this@LiveChannelActivity, PlayerActivity::class.java).apply {
            putExtra("STREAM_URL", channel.streamUrl)
            putExtra("DEVICE_ID", getDeviceId(this@LiveChannelActivity))
            // VOD가 아니므로 VOD 관련 ID는 보내지 않습니다.
        }
        startActivity(intent)
    }

    // [제거] handleCheckError, showExpirationDialog 등 모든 에러 처리 함수 제거
}