package com.example.newez

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newez.data.UserManager
import com.example.newez.network.ApiLiveChannel
import com.example.newez.network.ApiService
import com.example.newez.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException


class LiveChannelActivity : AppCompatActivity(), LiveChannelAdapter.OnChannelClickListener {

    private val apiService: ApiService by lazy { RetrofitClient.instance }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_channel)

        val recyclerView = findViewById<RecyclerView>(R.id.channel_recycler_view)
        recyclerView.layoutManager = GridLayoutManager(this, 8)

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

    override fun onChannelClick(channel: ApiLiveChannel) {
        lifecycleScope.launch {
            try {
                UserManager.checkSubscription(this@LiveChannelActivity)

                val intent = Intent(this@LiveChannelActivity, PlayerActivity::class.java).apply {
                    putExtra("STREAM_URL", channel.streamUrl)
                }
                startActivity(intent)

            } catch (error: Exception) {
                handleCheckError(error)
            }
        }
    }

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