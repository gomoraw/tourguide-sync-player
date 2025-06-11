package com.example.tourguidesyncplayer.ui.composables

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import timber.log.Timber

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(
    videoId: String,
    isPlaying: Boolean,
    positionMs: Long,
    onPlayerError: (String) -> Unit
) {
    val context = LocalContext.current

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            addListener(object : Player.Listener {
                override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                    super.onPlayerError(error)
                    Timber.e(error, "ExoPlayer error occurred.")
                    onPlayerError("Failed to load video: ${error.message}")
                }
            })
        }
    }

    var lastLoadedVideoId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(videoId) {
        if (videoId.isNotBlank() && videoId != lastLoadedVideoId) {
            // res/rawから動画URIを取得
            val resourceId = context.resources.getIdentifier(videoId, "raw", context.packageName)
            if (resourceId == 0) {
                Timber.e("Video resource not found for id: $videoId")
                onPlayerError("Video content '$videoId' not found on this device.")
                return@LaunchedEffect
            }
            val videoUri = Uri.parse("android.resource://${context.packageName}/$resourceId")
            
            val mediaItem = MediaItem.fromUri(videoUri)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            if (positionMs > 0) {
                exoPlayer.seekTo(positionMs)
            }
            lastLoadedVideoId = videoId
            Timber.i("Loaded video: $videoId at $positionMs ms")
        }
    }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            exoPlayer.play()
        } else {
            exoPlayer.pause()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
            Timber.d("ExoPlayer released.")
        }
    }

    AndroidView(
        factory = {
            PlayerView(it).apply {
                player = exoPlayer
                useController = false
                controllerAutoShow = false
            }
        }
    )
}

