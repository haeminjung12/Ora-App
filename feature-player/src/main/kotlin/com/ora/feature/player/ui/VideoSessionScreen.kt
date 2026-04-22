package com.ora.feature.player.ui

import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.ui.PlayerView
import com.ora.feature.player.PlayerSessionContract
import com.ora.feature.player.model.PlaybackState
import com.ora.feature.player.model.SessionEvent
import com.ora.feature.player.model.VideoSessionConfig
import kotlinx.coroutines.flow.collect

@Composable
fun VideoSessionScreen(
    controller: PlayerSessionContract,
    config: VideoSessionConfig,
    modifier: Modifier = Modifier,
) {
    val playbackState by controller.playbackState.collectAsState()
    val playbackPositionMs by controller.playbackPositionMs.collectAsState()
    val playbackDurationMs by controller.playbackDurationMs.collectAsState()
    val completionPercent by controller.completionPercent.collectAsState()
    val context = LocalContext.current
    var latestEventLabel by remember(config.sessionId, config.videoId) {
        mutableStateOf("Waiting for session start")
    }

    DisposableEffect(controller, config) {
        controller.attachSession(config)
        onDispose { }
    }

    LaunchedEffect(controller, config.sessionId, config.videoId) {
        controller.sessionEvents.collect { event ->
            latestEventLabel = event.toUiLabel()
        }
    }

    val elapsedLabel = playbackPositionMs.toClockLabel()
    val durationLabel = playbackDurationMs.toClockLabel(placeholder = "--:--")
    val remainingMs = (playbackDurationMs - playbackPositionMs).coerceAtLeast(0L)
    val remainingLabel = remainingMs.toClockLabel(placeholder = "--:--")
    val statusSummary = playbackState.toStatusSummary()
    val progressText = "${(completionPercent * 100).toInt()}%"

    Surface(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = {
                    PlayerView(context).apply {
                        player = controller.player
                        useController = false
                        layoutParams = android.view.ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                    }
                }
            )

            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Surface(
                    color = Color(0xC920252B),
                    contentColor = Color.White,
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                        Text(
                            text = "Session ${config.sessionId}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = "Video ${config.videoId}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFD5DCE3),
                        )
                    }
                }

                Surface(
                    color = Color(0x9920252B),
                    contentColor = Color.White,
                    shape = MaterialTheme.shapes.small,
                ) {
                    Text(
                        text = if (config.autoPlay) "Autoplay enabled" else "Manual start",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color(0xD9101418))
                    .padding(horizontal = 16.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Session Playback",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = statusSummary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFD5DCE3),
                        )
                    }

                    Surface(
                        color = Color(0xFF1D6B51),
                        contentColor = Color.White,
                        shape = MaterialTheme.shapes.small,
                    ) {
                        Text(
                            text = progressText,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }

                LinearProgressIndicator(
                    progress = completionPercent,
                    modifier = Modifier.fillMaxWidth(),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TimeStat(label = "Elapsed", value = elapsedLabel)
                    TimeStat(label = "Remaining", value = remainingLabel)
                    TimeStat(label = "Duration", value = durationLabel)
                }

                Surface(
                    color = Color(0x80253039),
                    contentColor = Color.White,
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = "Latest session event",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color(0xFFB7C4D1),
                        )
                        Text(
                            text = latestEventLabel,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White,
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(
                        onClick = controller::play,
                        enabled = playbackState != PlaybackState.PLAYING,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Play")
                    }

                    OutlinedButton(
                        onClick = controller::pause,
                        enabled = playbackState == PlaybackState.PLAYING,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Pause")
                    }

                    OutlinedButton(
                        onClick = controller::stop,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Stop")
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeStat(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFF9FB0C1),
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Medium,
        )
    }
}

private fun PlaybackState.toStatusSummary(): String = when (this) {
    PlaybackState.IDLE -> "Preparing session"
    PlaybackState.BUFFERING -> "Buffering playback"
    PlaybackState.READY -> "Ready to play"
    PlaybackState.PLAYING -> "Playback in progress"
    PlaybackState.PAUSED -> "Playback paused"
    PlaybackState.ENDED -> "Session finished"
    PlaybackState.ERROR -> "Playback error"
}

private fun SessionEvent.toUiLabel(): String = when (this) {
    is SessionEvent.Started -> "Started session $sessionId for video $videoId"
    is SessionEvent.Stopped -> {
        val percent = (completionPercent * 100).toInt()
        "Stopped session $sessionId at $percent% completion"
    }
}

private fun Long.toClockLabel(placeholder: String = "00:00"): String {
    if (this <= 0L) return placeholder

    val totalSeconds = this / 1000L
    val hours = totalSeconds / 3600L
    val minutes = (totalSeconds % 3600L) / 60L
    val seconds = totalSeconds % 60L

    return if (hours > 0L) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}
