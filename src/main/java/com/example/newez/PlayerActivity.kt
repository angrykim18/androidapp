package com.example.newez

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.newez.viewmodel.PlayerViewModel
import kotlinx.coroutines.delay

class PlayerActivity : ComponentActivity() {

    private val playerViewModel: PlayerViewModel by viewModels()
    private var exoPlayer: ExoPlayer? = null
    private var deviceId: String? = null
    private var vodFileId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val streamUrl = intent.getStringExtra("STREAM_URL")
        deviceId = intent.getStringExtra("DEVICE_ID")
        vodFileId = intent.getLongExtra("VOD_FILE_ID", -1L)
        val startTimeSeconds = intent.getIntExtra("START_TIME_SECONDS", 0)

        setContent {
            val context = LocalContext.current
            var isPlayerReady by remember { mutableStateOf(false) }

            LaunchedEffect(streamUrl) {
                if (streamUrl == null) return@LaunchedEffect

                exoPlayer = ExoPlayer.Builder(context).build().apply {
                    addListener(object : Player.Listener {
                        override fun onPlaybackStateChanged(playbackState: Int) {
                            if (playbackState == Player.STATE_READY) {
                                isPlayerReady = true
                            }
                        }
                    })
                    val mediaItem = MediaItem.fromUri(streamUrl)
                    setMediaItem(mediaItem)

                    if (startTimeSeconds > 0) {
                        seekTo(startTimeSeconds * 1000L)
                    }

                    prepare()
                    playWhenReady = true
                }
            }

            LaunchedEffect(isPlayerReady) {
                if (isPlayerReady) {
                    while (true) {
                        delay(15_000L)
                        saveCurrentPosition()
                    }
                }
            }

            DisposableEffect(Unit) {
                onDispose {
                    exoPlayer?.release()
                    exoPlayer = null
                }
            }

            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                if (isPlayerReady && exoPlayer != null) {
                    VideoPlayer(
                        player = exoPlayer!!,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(80.dp),
                            color = Color.White,
                            strokeWidth = 5.dp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "불러오는 중...", color = Color.White, fontSize = 40.sp)
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        saveCurrentPosition()
    }

    private fun saveCurrentPosition() {
        val currentDeviceId = deviceId
        val currentVodFileId = vodFileId
        val player = exoPlayer

        if (player != null && currentDeviceId != null && currentVodFileId != -1L) {
            val positionSeconds = (player.currentPosition / 1000).toInt()
            if (positionSeconds > 0) {
                Log.d("HistoryLog", "Saving history: VOD ID ${currentVodFileId}, Time ${positionSeconds}s")
                playerViewModel.updateWatchHistory(currentDeviceId, currentVodFileId, positionSeconds)
            }
        }
    }
}