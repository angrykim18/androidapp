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
// [수정] UserManager와 관련된 import는 더 이상 필요 없으므로 제거하거나 주석 처리할 수 있습니다.
// import com.example.newez.data.UserManager
import com.example.newez.model.VodFile
import com.example.newez.viewmodel.ContentDetailViewModel
import com.example.newez.viewmodel.EpisodeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
// [수정] HttpException 관련 import 제거
// import retrofit2.HttpException
import java.util.*
import com.example.newez.BuildConfig

class ContentDetailActivity : AppCompatActivity(), OnEpisodeClickListener {

    private val detailViewModel: ContentDetailViewModel by viewModels()
    private val episodeViewModel: EpisodeViewModel by viewModels()
    private lateinit var episodeAdapter: EpisodeAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var lastWatchedTextView: TextView
    private var deviceId: String? = null
    private val WIDEVINE_UUID = UUID(-0x121074568629b532L, -0x5c37d8232ae2de13L)

    private fun buildPosterUrl(base: String, raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        return if (raw.startsWith("http")) raw else base.trimEnd('/') + raw
    }

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
     * [수정] 클릭 시 권한 확인 없이 즉시 PlayerActivity를 실행합니다.
     */
    override fun onEpisodeClick(episode: VodFile) {
        val currentDeviceId = deviceId
        if (currentDeviceId == null) {
            Toast.makeText(this, "기기 ID를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }
        // 권한 확인 로직을 제거하고 바로 startPlayer를 호출합니다.
        startPlayer(episode.fullUrl, currentDeviceId, episode.id, 0)
    }

    private fun observeDetailViewModel() {
        val titleTextView = findViewById<TextView>(R.id.textView_detail_title)
        val descriptionTextView = findViewById<TextView>(R.id.textView_detail_description)
        val posterImageView: ImageView = findViewById(R.id.imageView_detail_poster)

        detailViewModel.contentDetail.observe(this) { content ->
            titleTextView.text = content.title
            descriptionTextView.text = content.description

            val posterUrl = buildPosterUrl(BuildConfig.BASE_URL, content.posterPath)
            Glide.with(this).load(posterUrl).into(posterImageView)

            val episodeId = content.lastWatchedEpisodeId
            val episodeNumber = content.lastWatchedEpisodeNumber
            val timestamp = content.lastWatchedTimestamp

            if (episodeId != null && episodeNumber != null && timestamp != null) {
                val minutes = timestamp / 60
                val seconds = timestamp % 60
                lastWatchedTextView.text = "마지막 시청 기록: ${episodeNumber} (${String.format("%02d:%02d", minutes, seconds)})"

                lastWatchedTextView.setOnClickListener {
                    // [수정] 이어보기 클릭 시에도 권한 확인 없이 즉시 PlayerActivity를 실행합니다.
                    val episodeToResume = episodeViewModel.episodes.value?.find { it.id == episodeId }
                    if (episodeToResume != null) {
                        val currentDeviceId = deviceId
                        if (currentDeviceId != null) {
                            startPlayer(episodeToResume.fullUrl, currentDeviceId, episodeId, timestamp)
                        } else {
                            Toast.makeText(this, "기기 ID를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
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

    // [제거] 아래 두 함수(handleCheckError, showExpirationDialog)는 PlayerActivity로 로직이 이전되었으므로
    // 더 이상 필요하지 않습니다. 삭제해도 무방합니다.
    /*
    private fun handleCheckError(error: Exception) {
        // ... (내용 생략)
    }

    private fun showExpirationDialog() {
        // ... (내용 생략)
    }
    */
}