package com.example.newez.ui

import android.content.Intent
import android.media.MediaDrm
import android.os.Bundle
import android.util.Base64
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.newez.PlayerActivity
import com.example.newez.R
import com.example.newez.adapter.EpisodeAdapter
import com.example.newez.adapter.OnEpisodeClickListener
import com.example.newez.model.VodFile
import com.example.newez.viewmodel.ContentDetailViewModel
import com.example.newez.viewmodel.EpisodeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class ContentDetailActivity : AppCompatActivity(), OnEpisodeClickListener {

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

        prevButton.setOnClickListener {
            episodeViewModel.loadPreviousPage()
        }
        nextButton.setOnClickListener {
            episodeViewModel.loadNextPage()
        }
    }

    // ✅ [수정] 화면이 다시 보일 때마다 데이터를 새로고침하도록 onResume 사용
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
        val layoutManager = GridLayoutManager(this, 5)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = episodeAdapter
    }

    override fun onEpisodeClick(episode: VodFile) {
        val currentDeviceId = deviceId ?: return
        startPlayer(episode.fullUrl, currentDeviceId, episode.id, 0)
    }

    private fun observeDetailViewModel() {
        val titleTextView = findViewById<TextView>(R.id.textView_detail_title)
        val descriptionTextView = findViewById<TextView>(R.id.textView_detail_description)
        val posterImageView: ImageView = findViewById(R.id.imageView_detail_poster)

        detailViewModel.contentDetail.observe(this) { content ->
            titleTextView.text = content.title
            descriptionTextView.text = content.description

            Glide.with(this)
                .load(content.posterPath)
                .into(posterImageView)

            // ✅ [수정] 경합 상태를 해결한 최종 UI 업데이트 로직
            val episodeId = content.lastWatchedEpisodeId
            val episodeNumber = content.lastWatchedEpisodeNumber // 백엔드에서 받은 회차 이름을 직접 사용
            val timestamp = content.lastWatchedTimestamp

            if (episodeId != null && episodeNumber != null && timestamp != null) {
                val minutes = timestamp / 60
                val seconds = timestamp % 60
                lastWatchedTextView.text = "마지막 시청 기록: ${episodeNumber} (${String.format("%02d:%02d", minutes, seconds)})"

                // 클릭 리스너를 이 안에서 설정하여, 정보가 있을 때만 활성화
                lastWatchedTextView.setOnClickListener {
                    val episodeToResume = episodeViewModel.episodes.value?.find { it.id == episodeId }
                    if (episodeToResume != null) {
                        val currentDeviceId = deviceId ?: return@setOnClickListener
                        startPlayer(episodeToResume.fullUrl, currentDeviceId, episodeId, timestamp)
                    }
                }
            } else {
                // 정보가 없으면 "없음"으로 표시하고 클릭 리스너를 제거
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
}