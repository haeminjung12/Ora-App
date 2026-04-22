package com.ora.feature.player

import androidx.media3.common.Player
import com.ora.feature.player.model.PlaybackState
import com.ora.feature.player.model.SessionEvent
import com.ora.feature.player.model.VideoSessionConfig
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface PlayerSessionContract {
    val player: Player
    val playbackState: StateFlow<PlaybackState>
    val playbackPositionMs: StateFlow<Long>
    val playbackDurationMs: StateFlow<Long>
    val completionPercent: StateFlow<Float>
    val sessionEvents: SharedFlow<SessionEvent>

    fun attachSession(config: VideoSessionConfig)
    fun play()
    fun pause()
    fun stop()
    fun release()
}
