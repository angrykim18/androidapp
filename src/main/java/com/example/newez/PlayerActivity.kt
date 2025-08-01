package com.example.newez

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

class PlayerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val streamUrl = intent.getStringExtra("STREAM_URL")

        setContent {
            val context = LocalContext.current
            // ✅ [추가] 영상이 준비되었는지 상태를 기억하는 변수
            var isPlayerReady by remember { mutableStateOf(false) }

            val exoPlayer = remember {
                ExoPlayer.Builder(context).build().apply {
                    // ✅ [추가] 플레이어 상태를 감지하는 리스너 추가
                    addListener(object : Player.Listener {
                        override fun onPlaybackStateChanged(playbackState: Int) {
                            // 영상이 준비되면 isPlayerReady를 true로 변경
                            if (playbackState == Player.STATE_READY) {
                                isPlayerReady = true
                            }
                        }
                    })

                    streamUrl?.let {
                        val mediaItem = MediaItem.fromUri(it)
                        setMediaItem(mediaItem)
                        prepare()
                        playWhenReady = true
                    }
                }
            }

            DisposableEffect(Unit) {
                onDispose {
                    exoPlayer.release()
                }
            }

            // ✅ [수정] Box를 사용해 로딩 상태에 따라 다른 화면을 보여줌
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black), // 검은 화면 대신 검은 배경을 명시적으로 설정
                contentAlignment = Alignment.Center
            ) {
                // 영상이 준비되었다면 VideoPlayer를, 아니라면 로딩 아이콘을 보여줌
                if (isPlayerReady) {
                    VideoPlayer(
                        player = exoPlayer,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    CircularProgressIndicator()
                }
            }
        }
    }
}