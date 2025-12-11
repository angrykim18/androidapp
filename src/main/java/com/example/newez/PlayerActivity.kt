package com.example.newez

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.newez.data.UserManager
import com.example.newez.viewmodel.PlayerViewModel
import kotlinx.coroutines.delay
import retrofit2.HttpException

private enum class AuthState {
    CHECKING,
    AUTHORIZED,
    FAILED
}

@OptIn(UnstableApi::class)
class PlayerActivity : ComponentActivity() {

    private val playerViewModel: PlayerViewModel by viewModels()
    private var deviceId: String? = null
    private var vodFileId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val streamUrl = intent.getStringExtra("STREAM_URL")
        deviceId = intent.getStringExtra("DEVICE_ID")
        vodFileId = intent.getLongExtra("VOD_FILE_ID", -1L)
        val startTimeSeconds = intent.getIntExtra("START_TIME_SECONDS", 0)

        setContent {
            var playerView by remember { mutableStateOf<PlayerView?>(null) }
            var authState by remember { mutableStateOf(AuthState.CHECKING) }
            var authError by remember { mutableStateOf<Exception?>(null) }

            LaunchedEffect(Unit) {
                try {
                    UserManager.checkSubscription(this@PlayerActivity)
                    authState = AuthState.AUTHORIZED
                } catch (e: Exception) {
                    authError = e
                    authState = AuthState.FAILED
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .onKeyEvent { keyEvent ->
                        if (keyEvent.type == KeyEventType.KeyDown && keyEvent.key.nativeKeyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                            playerView?.let {
                                if (it.isControllerFullyVisible) {
                                    it.hideController()
                                } else {
                                    it.showController()
                                }
                            }
                            return@onKeyEvent true
                        }
                        return@onKeyEvent false
                    },
                contentAlignment = Alignment.Center
            ) {
                when (authState) {
                    AuthState.CHECKING -> LoadingScreen()
                    AuthState.AUTHORIZED -> PlayerScreen(
                        streamUrl = streamUrl,
                        startTimeSeconds = startTimeSeconds,
                        onPlayerViewCreated = { createdView ->
                            playerView = createdView
                        }
                    )
                    AuthState.FAILED -> {
                        authError?.let {
                            ErrorDialog(error = it) { finish() }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun PlayerScreen(
        streamUrl: String?,
        startTimeSeconds: Int,
        onPlayerViewCreated: (PlayerView) -> Unit
    ) {
        val context = LocalContext.current
        val focusRequester = remember { FocusRequester() }

        var exoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }
        var isPlayerReady by remember { mutableStateOf(false) }

        DisposableEffect(streamUrl) {
            if (streamUrl == null) {
                return@DisposableEffect onDispose {}
            }
            val playerInstance = ExoPlayer.Builder(context)
                .setSeekBackIncrementMs(30_000)
                .setSeekForwardIncrementMs(30_000)
                .setLoadControl(DefaultLoadControl.Builder().setBufferDurationsMs(60_000, 180_000, 5_500, 8_000).build())
                .build().apply {
                    setMediaItem(MediaItem.fromUri(streamUrl))
                    if (startTimeSeconds > 0) {
                        seekTo(startTimeSeconds * 1000L)
                    }
                    playWhenReady = true
                    prepare()
                }

            val listener = object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_READY) {
                        isPlayerReady = true
                    }
                }
            }
            playerInstance.addListener(listener)
            exoPlayer = playerInstance

            onDispose {
                saveCurrentPosition(playerInstance)
                playerInstance.removeListener(listener)
                playerInstance.release()
                exoPlayer = null
            }
        }

        if (isPlayerReady && exoPlayer != null) {
            VideoPlayer(
                player = exoPlayer!!,
                focusRequester = focusRequester,
                onPlayerViewCreated = onPlayerViewCreated,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LoadingScreen()
        }

        LaunchedEffect(exoPlayer, isPlayerReady) {
            if (exoPlayer != null && isPlayerReady) {
                while (true) {
                    delay(15_000L)
                    saveCurrentPosition(exoPlayer)
                }
            }
        }
    }

    @Composable
    private fun LoadingScreen() {
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

    @Composable
    private fun ErrorDialog(error: Exception, onDismiss: () -> Unit) {
        val title: String
        val message: String
        when (error) {
            is UserManager.SubscriptionExpiredException -> {
                title = "시청 기간 만료"
                message = "시청일자가 만료되었습니다. 고객센터로 문의하세요."
            }
            is HttpException -> {
                title = "오류"
                message = if (error.code() == 404) "등록되지 않은 기기입니다." else "서버 오류가 발생했습니다."
            }
            else -> {
                title = "오류"
                message = "상태 확인 중 오류가 발생했습니다."
            }
        }
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(text = title) },
            text = { Text(text = message) },
            confirmButton = { Button(onClick = onDismiss) { Text("확인") } }
        )
    }

    override fun onPause() {
        super.onPause()
        // Compose의 생명주기(DisposableEffect)에서 위치 저장을 처리하므로
        // Activity의 onPause에서는 별도로 호출하지 않아도 됩니다.
    }

    private fun saveCurrentPosition(player: ExoPlayer?) {
        val currentDeviceId = deviceId
        val currentVodFileId = vodFileId
        if (player != null && currentDeviceId != null && currentVodFileId != -1L) {
            val positionSeconds = (player.currentPosition / 1000).toInt()
            if (positionSeconds > 0) {
                Log.d("HistoryLog", "Saving history: VOD ID $currentVodFileId, Time ${positionSeconds}s")
                playerViewModel.updateWatchHistory(currentDeviceId, currentVodFileId, positionSeconds)
            }
        }
    }
}