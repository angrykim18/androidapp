package com.example.newez.ui

import android.content.Intent
import android.media.MediaDrm
import android.os.Bundle
import android.util.Base64
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.newez.MainActivity
import com.example.newez.PlayerActivity
import com.example.newez.R
import com.example.newez.adapter.EpisodeAdapter
import com.example.newez.adapter.OnEpisodeClickListener
import com.example.newez.data.UserManager // [수정] UserManager import
import com.example.newez.model.VodFile
import com.example.newez.viewmodel.ContentDetailViewModel
import com.example.newez.viewmodel.EpisodeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.util.*

class ContentDetailActivity : AppCompatActivity(), OnEpisodeClickListener {

    // --- 기존 코드와 동일 ---
    private val detailViewModel: ContentDetailViewModel by viewModels()
    private val episodeViewModel: EpisodeViewModel by viewModels()
    private lateinit var episodeAdapter: EpisodeAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var lastWatchedTextView: TextView
    private var deviceId: String? = null
    private val WIDEVINE_UUID = UUID(-0x121074568629b532L, -0x5c37d8232ae2de13L)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_content_detail)
        recyclerView = findViewById(R.id.recyclerView_episodes)
        val prevButton: TextView = findViewById(R.id.button_episode_previous)
        val nextButton: TextView = findViewById(R.id.button_episode_next)
        lastWatchedTextView = findViewById(R.id.textView_last_watched)
        setupRecyclerView()
        observeDetailViewModel()
        observeEpisodeViewModel()
        prevButton.setOnClickListener { episodeViewModel.loadPreviousPage() }
        nextButton.setOnClickListener { episodeViewModel.loadNextPage() }
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun loadData() {
        val contentId = intent.getLongExtra("CONTENT_ID", -1)
        if (contentId != -1L) {
            lifecycleScope.launch(Dispatchers.IO) {
                val fetchedDeviceId = getWidevineId()
                deviceId = fetchedDeviceId
                withContext(Dispatchers.Main) {
                    if (fetchedDeviceId != null) {
                        detailViewModel.loadContentDetail(contentId, fetchedDeviceId)
                        episodeViewModel.loadInitialEpisodes(contentId, fetchedDeviceId)
                    }
                }
            }
        }
    }

    private fun setupRecyclerView() {
        episodeAdapter = EpisodeAdapter(this)
        recyclerView.layoutManager = GridLayoutManager(this, 5)
        recyclerView.adapter = episodeAdapter
    }

    /**
     * [수정] 에피소드 클릭 시, UserManager를 통해 권한을 확인합니다.
     */
    override fun onEpisodeClick(episode: VodFile) {
        lifecycleScope.launch {
            try {
                UserManager.checkSubscription(this@ContentDetailActivity)
                val currentDeviceId = deviceId ?: return@launch
                startPlayer(episode.fullUrl, currentDeviceId, episode.id, 0)
            } catch (error: Exception) {
                handleCheckError(error)
            }
        }
    }

    private fun observeDetailViewModel() {
        val titleTextView = findViewById<TextView>(R.id.textView_detail_title)
        val descriptionTextView = findViewById<TextView>(R.id.textView_detail_description)
        val posterImageView: ImageView = findViewById(R.id.imageView_detail_poster)

        detailViewModel.contentDetail.observe(this) { content ->
            titleTextView.text = content.title
            descriptionTextView.text = content.description
            Glide.with(this).load(content.posterPath).into(posterImageView)

            val episodeId = content.lastWatchedEpisodeId
            val episodeNumber = content.lastWatchedEpisodeNumber
            val timestamp = content.lastWatchedTimestamp

            if (episodeId != null && episodeNumber != null && timestamp != null) {
                val minutes = timestamp / 60
                val seconds = timestamp % 60
                lastWatchedTextView.text = "마지막 시청 기록: ${episodeNumber} (${String.format("%02d:%02d", minutes, seconds)})"

                /**
                 * [수정] 이어보기 클릭 시에도 UserManager를 통해 권한을 확인합니다.
                 */
                lastWatchedTextView.setOnClickListener {
                    lifecycleScope.launch {
                        try {
                            UserManager.checkSubscription(this@ContentDetailActivity)
                            val episodeToResume = episodeViewModel.episodes.value?.find { it.id == episodeId }
                            if (episodeToResume != null) {
                                val currentDeviceId = deviceId ?: return@launch
                                startPlayer(episodeToResume.fullUrl, currentDeviceId, episodeId, timestamp)
                            }
                        } catch (error: Exception) {
                            handleCheckError(error)
                        }
                    }
                }
            } else {
                lastWatchedTextView.text = "마지막 시청 기록: 없음"
                lastWatchedTextView.setOnClickListener(null)
            }
        }
    }

    private fun startPlayer(streamUrl: String, deviceId: String, vodFileId: Long, startTimeSeconds: Int) {
        val intent = Intent(this, PlayerActivity::class.java).apply {
            putExtra("STREAM_URL", streamUrl)
            putExtra("DEVICE_ID", deviceId)
            putExtra("VOD_FILE_ID", vodFileId)
            putExtra("START_TIME_SECONDS", startTimeSeconds)
        }
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    private fun observeEpisodeViewModel() {
        episodeViewModel.episodes.observe(this) { episodes ->
            episodeAdapter.submitList(episodes)
        }
    }

    private fun getWidevineId(): String? {
        return try {
            val mediaDrm = MediaDrm(WIDEVINE_UUID)
            val widevineIdAsBytes = mediaDrm.getPropertyByteArray(MediaDrm.PROPERTY_DEVICE_UNIQUE_ID)
            Base64.encodeToString(widevineIdAsBytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            null
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
                val intent = Intent(this@ContentDetailActivity, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                startActivity(intent)
                finish()
            }
            .setCancelable(false)
            .show()
    }
}
