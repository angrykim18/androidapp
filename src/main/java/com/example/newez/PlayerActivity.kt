package com.example.newez

import android.os.Bundle
import androidx.activity.ComponentActivity // ✅ import 변경
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer

// ✅ [핵심 수정] AppCompatActivity 대신 ComponentActivity를 상속합니다.
class PlayerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val streamUrl = intent.getStringExtra("STREAM_URL")

        setContent {
            val context = LocalContext.current
            val exoPlayer = remember {
                ExoPlayer.Builder(context).build().apply {
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

            VideoPlayer(
                player = exoPlayer,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}