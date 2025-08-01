package com.example.newez.ui

import android.os.Bundle
import android.view.KeyEvent
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.newez.R
import com.example.newez.adapter.EpisodeAdapter
import com.example.newez.viewmodel.ContentDetailViewModel
import com.example.newez.viewmodel.EpisodeViewModel

class ContentDetailActivity : AppCompatActivity() {

    private val detailViewModel: ContentDetailViewModel by viewModels()
    private val episodeViewModel: EpisodeViewModel by viewModels()
    private lateinit var episodeAdapter: EpisodeAdapter
    private lateinit var recyclerView: RecyclerView

    // ✅ [추가] 뷰 ID를 클래스 변수로 선언하여 리스너 내부에서도 사용
    private lateinit var nextButton: TextView
    private lateinit var prevButton: TextView
    private lateinit var lastWatchedTextView: TextView
    private lateinit var posterImageView: ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_content_detail)

        val contentId = intent.getLongExtra("CONTENT_ID", -1)

        // 뷰를 미리 찾아 변수에 할당
        recyclerView = findViewById(R.id.recyclerView_episodes)
        prevButton = findViewById(R.id.button_episode_previous)
        nextButton = findViewById(R.id.button_episode_next)
        lastWatchedTextView = findViewById(R.id.textView_last_watched)
        posterImageView = findViewById(R.id.imageView_detail_poster)

        setupEpisodeRecyclerView()
        observeDetailViewModel()
        observeEpisodeViewModel()

        // 이전/다음 버튼에 대한 클릭 리스너 설정
        prevButton.setOnClickListener {
            episodeViewModel.loadPreviousPage()
        }
        nextButton.setOnClickListener {
            episodeViewModel.loadNextPage()
        }

        if (contentId != -1L) {
            detailViewModel.loadContentDetail(contentId)
            episodeViewModel.loadInitialEpisodes(contentId)
        }
    }

    private fun setupEpisodeRecyclerView() {
        episodeAdapter = EpisodeAdapter()
        val layoutManager = GridLayoutManager(this, 5)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = episodeAdapter

        // ✅ [최종 수정] OnKeyListener를 사용한 포커스 강제 이동 로직
        recyclerView.setOnKeyListener { _, keyCode, event ->
            // 키를 누를 때만 동작하도록 설정
            if (event.action == KeyEvent.ACTION_DOWN) {
                val lm = recyclerView.layoutManager as? GridLayoutManager ?: return@setOnKeyListener false
                val focusedView = recyclerView.findFocus() ?: return@setOnKeyListener false
                val currentPosition = lm.getPosition(focusedView)
                val spanCount = lm.spanCount
                val itemCount = episodeAdapter.itemCount

                when (keyCode) {
                    // 아래로 이동 시
                    KeyEvent.KEYCODE_DPAD_DOWN -> {
                        // 현재 아이템이 마지막 줄에 있는지 확인
                        if (currentPosition >= itemCount - spanCount) {
                            nextButton.requestFocus()
                            return@setOnKeyListener true // 이벤트 처리 완료
                        }
                    }
                    // 위로 이동 시
                    KeyEvent.KEYCODE_DPAD_UP -> {
                        // 현재 아이템이 첫 줄에 있는지 확인
                        if (currentPosition < spanCount) {
                            lastWatchedTextView.requestFocus()
                            return@setOnKeyListener true // 이벤트 처리 완료
                        }
                    }
                    // 왼쪽으로 이동 시
                    KeyEvent.KEYCODE_DPAD_LEFT -> {
                        // 현재 아이템이 첫 번째 열에 있는지 확인
                        if (currentPosition % spanCount == 0) {
                            posterImageView.requestFocus()
                            return@setOnKeyListener true // 이벤트 처리 완료
                        }
                    }
                }
            }
            // 그 외의 경우는 시스템 기본 동작에 맡김
            return@setOnKeyListener false
        }
    }

    private fun observeDetailViewModel() {
        val titleTextView = findViewById<TextView>(R.id.textView_detail_title)
        val descriptionTextView = findViewById<TextView>(R.id.textView_detail_description)

        detailViewModel.contentDetail.observe(this) { content ->
            titleTextView.text = content.title
            descriptionTextView.text = content.description

            Glide.with(this)
                .load(content.posterPath)
                .into(posterImageView)
        }
    }

    private fun observeEpisodeViewModel() {
        episodeViewModel.episodes.observe(this) { episodes ->
            episodeAdapter.submitList(episodes) {
                // 페이지 변경 후 포커스가 사라지는 문제를 막기 위해 첫 아이템에 포커스 요청
                recyclerView.post {
                    val firstViewHolder = recyclerView.findViewHolderForAdapterPosition(0)
                    firstViewHolder?.itemView?.requestFocus()
                }
            }
        }
    }
}