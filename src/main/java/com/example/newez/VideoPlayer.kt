package com.example.newez

import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView


@OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(
    player: ExoPlayer,
    focusRequester: FocusRequester,
    onPlayerViewCreated: (PlayerView) -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    AndroidView(
        modifier = modifier.focusRequester(focusRequester),
        factory = { context ->
            PlayerView(context).apply {
                this.player = player
                useController = true
                onPlayerViewCreated(this)

            }
        }
    )
}