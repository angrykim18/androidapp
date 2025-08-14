package com.example.newez

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newez.data.UserManager // [수정] UserManager import
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
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

    /**
     * [수정] UserManager를 사용하여 시청 기간을 확인합니다.
     */
    override fun onChannelClick(channel: ApiLiveChannel) {
        lifecycleScope.launch {
            try {
                // UserManager의 checkSubscription 함수를 호출합니다.
                UserManager.checkSubscription(this@LiveChannelActivity)

                // 성공하면 플레이어를 실행합니다.
                val intent = Intent(this@LiveChannelActivity, PlayerActivity::class.java).apply {
                    putExtra("STREAM_URL", channel.streamUrl)
                }
                startActivity(intent)

            } catch (error: Exception) {
                // 실패하면 에러를 처리합니다.
                handleCheckError(error)
            }
        }
    }

    /**
     * [추가] 에러 처리를 위한 공통 함수
     */
    private fun handleCheckError(error: Exception) {
        when (error) {
            is UserManager.SubscriptionExpiredException -> showExpirationDialog()
            is HttpException -> {
                if (error.code() == 404) {
                    Toast.makeText(applicationContext, "등록되지 않은 기기입니다.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(applicationContext, "서버 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            else -> {
                Toast.makeText(applicationContext, "상태 확인 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * [추가] 만료 안내 팝업을 보여주는 함수
     */
    private fun showExpirationDialog() {
        AlertDialog.Builder(this)
            .setTitle("시청 기간 만료")
            .setMessage("시청일자가 만료되었습니다. 고객센터로 문의하세요.")
            .setPositiveButton("확인") { dialog, _ ->
                dialog.dismiss()
                val intent = Intent(this@LiveChannelActivity, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                startActivity(intent)
                finish()
            }
            .setCancelable(false)
            .show()
    }
}
