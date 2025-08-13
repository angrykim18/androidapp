package com.example.newez

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import com.example.newez.util.DateChecker


data class ApiLiveChannel(
    val id: Long,
    val channelName: String,
    val streamUrl: String
)
data class UserStatus(
    val subscriptionEndDate: String?
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
                    Toast.makeText(applicationContext, "채널 목록을 불러오는데 실패했습니다.", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    override fun onChannelClick(channel: ApiLiveChannel) {
        lifecycleScope.launch {
            try {
                // 1. 검사기를 실행합니다. 실패하면 catch 블록으로 바로 이동합니다.
                DateChecker.check(this@LiveChannelActivity)

                // 2. 성공(시청 가능)하면 플레이어를 실행합니다.
                val intent = Intent(this@LiveChannelActivity, PlayerActivity::class.java).apply {
                    putExtra("STREAM_URL", channel.streamUrl)
                }
                startActivity(intent)

            } catch (error: Exception) {
                // 3. DateChecker에서 예외가 발생하면 여기서 처리합니다.
                when (error) {
                    is DateChecker.DateExpiredException -> {
                        // 원인이 '만료'인 경우 -> 팝업 표시
                        showExpirationDialog()
                    }
                    is HttpException -> {
                        // 원인이 '서버 에러'인 경우
                        if (error.code() == 404) {
                            Toast.makeText(applicationContext, "등록되지 않은 기기입니다.", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(applicationContext, "서버 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
                    else -> {
                        // 그 외 모든 에러 (네트워크 등)
                        Toast.makeText(applicationContext, "상태 확인 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    /**
     * 만료 안내 팝업을 보여주는 함수입니다.
     */
    private fun showExpirationDialog() {
        AlertDialog.Builder(this)
            .setTitle("시청 기간 만료")
            .setMessage("시청일자가 만료되었습니다. 고객센터로 문의하세요.")
            .setPositiveButton("확인") { dialog, _ ->
                dialog.dismiss()
                // MainActivity.class.java는 실제 프로젝트의 메인 화면 이름으로 수정해야 합니다.
                val intent = Intent(this@LiveChannelActivity, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                startActivity(intent)
                finish()
            }
            .setCancelable(false)
            .show()
    }
}
