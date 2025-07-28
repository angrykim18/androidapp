package com.example.newez

import android.content.Intent
import android.os.Bundle
import android.util.Log // ✅ import 추가
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

data class ApiLiveChannel(
    val id: Long,
    val channelName: String,
    val streamUrl: String
)

interface LiveChannelApiService {
    @GET("/api/live-channels")
    suspend fun getLiveChannels(): List<ApiLiveChannel>
}

class LiveChannelActivity : AppCompatActivity(), LiveChannelAdapter.OnChannelClickListener {

    private val retrofit = Retrofit.Builder()
        .baseUrl("http://192.168.0.2:8081")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService = retrofit.create(LiveChannelApiService::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_channel)

        val recyclerView = findViewById<RecyclerView>(R.id.channel_recycler_view)
        recyclerView.layoutManager = GridLayoutManager(this, 8)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val channels = apiService.getLiveChannels()
                withContext(Dispatchers.Main) {
                    recyclerView.adapter = LiveChannelAdapter(channels, this@LiveChannelActivity)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(applicationContext, "채널 목록을 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onChannelClick(channel: ApiLiveChannel) {
        // ✅ [핵심 수정] 테스트 URL 부분을 삭제하고, 원래대로 channel.streamUrl을 사용합니다.
        Log.d("ChannelClickLog", "클릭된 채널의 streamUrl: ${channel.streamUrl}")

        val intent = Intent(this, PlayerActivity::class.java).apply {
            putExtra("STREAM_URL", channel.streamUrl)
        }
        startActivity(intent)
    }
}