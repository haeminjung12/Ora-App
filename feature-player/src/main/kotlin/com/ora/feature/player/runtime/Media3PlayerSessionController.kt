package com.ora.feature.player.runtime

import android.content.Context
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.ora.feature.player.PlayerSessionContract
import com.ora.feature.player.model.PlaybackState
import com.ora.feature.player.model.SessionEvent
import com.ora.feature.player.model.VideoSessionConfig
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Media3PlayerSessionController(
    context: Context,
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main.immediate,
) : PlayerSessionContract {

    override val player: ExoPlayer = ExoPlayer.Builder(context).build().apply {
        repeatMode = Player.REPEAT_MODE_OFF
        playWhenReady = false
    }

    private val scope = CoroutineScope(SupervisorJob() + mainDispatcher)
    private val _playbackState = MutableStateFlow(PlaybackState.IDLE)
    private val _playbackPositionMs = MutableStateFlow(0L)
    private val _playbackDurationMs = MutableStateFlow(0L)
    private val _completionPercent = MutableStateFlow(0f)
    private val _sessionEvents = MutableSharedFlow<SessionEvent>(extraBufferCapacity = 8)

    override val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()
    override val playbackPositionMs: StateFlow<Long> = _playbackPositionMs.asStateFlow()
    override val playbackDurationMs: StateFlow<Long> = _playbackDurationMs.asStateFlow()
    override val completionPercent: StateFlow<Float> = _completionPercent.asStateFlow()
    override val sessionEvents: SharedFlow<SessionEvent> = _sessionEvents.asSharedFlow()

    private var sessionConfig: VideoSessionConfig? = null
    private var hasStarted = false
    private var tickerJob: Job? = null
    private var terminalStopReason: SessionEvent.StopReason? = null

    private val listener = object : Player.Listener {
        override fun onPlaybackStateChanged(state: Int) {
            if (state == Player.STATE_ENDED) {
                syncProgressSnapshot(forceCompletion = true)
                emitStopEvent(SessionEvent.StopReason.COMPLETED)
            }
            syncPlaybackState()
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            syncPlaybackState()

            if (isPlaying && !hasStarted) {
                hasStarted = true
                emitStartEvent()
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            syncProgressSnapshot()
            _playbackState.value = PlaybackState.ERROR
            emitStopEvent(
                reason = SessionEvent.StopReason.ERROR,
                errorMessage = error.message,
            )
        }
    }

    init {
        player.addListener(listener)
        startTicker()
    }

    override fun attachSession(config: VideoSessionConfig) {
        replaceSessionIfNeeded(nextSession = config)

        sessionConfig = config
        hasStarted = false
        terminalStopReason = null
        resetProgress()
        _playbackState.value = PlaybackState.IDLE

        player.setMediaItem(MediaItem.fromUri(config.videoUri))
        player.prepare()
        player.playWhenReady = config.autoPlay
    }

    override fun play() {
        player.play()
    }

    override fun pause() {
        player.pause()
    }

    override fun stop() {
        syncProgressSnapshot()
        emitStopEvent(SessionEvent.StopReason.USER_STOPPED)
        player.stop()
        sessionConfig = null
        resetProgress()
        _playbackState.value = PlaybackState.IDLE
    }

    override fun release() {
        syncProgressSnapshot()
        emitStopEvent(SessionEvent.StopReason.RELEASED)
        tickerJob?.cancel()
        player.removeListener(listener)
        player.release()
        sessionConfig = null
        scope.cancel()
    }

    private fun startTicker() {
        tickerJob = scope.launch {
            while (true) {
                updateProgress()
                delay(200L)
            }
        }
    }

    private suspend fun updateProgress() = withContext(mainDispatcher) {
        syncProgressSnapshot()
    }

    private fun emitStartEvent() {
        val config = sessionConfig ?: return
        scope.launch {
            _sessionEvents.emit(
                SessionEvent.Started(
                    sessionId = config.sessionId,
                    videoId = config.videoId,
                    startedAtEpochMs = System.currentTimeMillis(),
                    autoPlay = config.autoPlay,
                )
            )
        }
    }

    private fun emitStopEvent(
        reason: SessionEvent.StopReason,
        errorMessage: String? = null,
    ) {
        val config = sessionConfig ?: return
        if (!hasStarted) return
        if (terminalStopReason != null) return

        terminalStopReason = reason
        hasStarted = false

        val positionMs = _playbackPositionMs.value
        val durationMs = _playbackDurationMs.value

        scope.launch {
            _sessionEvents.emit(
                SessionEvent.Stopped(
                    sessionId = config.sessionId,
                    videoId = config.videoId,
                    stoppedAtEpochMs = System.currentTimeMillis(),
                    reason = reason,
                    positionMs = positionMs,
                    durationMs = durationMs,
                    completionPercent = _completionPercent.value,
                    errorMessage = errorMessage,
                )
            )
        }
    }

    private fun replaceSessionIfNeeded(nextSession: VideoSessionConfig) {
        val currentSession = sessionConfig ?: return
        if (currentSession == nextSession) {
            return
        }

        syncProgressSnapshot()
        emitStopEvent(SessionEvent.StopReason.SESSION_REPLACED)
        player.stop()
    }

    private fun resetProgress() {
        _playbackPositionMs.value = 0L
        _playbackDurationMs.value = 0L
        _completionPercent.value = 0f
    }

    private fun syncPlaybackState() {
        _playbackState.value = player.playbackState.toPlaybackState(player.isPlaying)
    }

    private fun syncProgressSnapshot(forceCompletion: Boolean = false) {
        val rawDuration = player.duration.takeIf { it != C.TIME_UNSET && it > 0L } ?: 0L
        val duration = if (forceCompletion && rawDuration <= 0L) {
            _playbackDurationMs.value
        } else {
            rawDuration
        }
        val position = when {
            forceCompletion && duration > 0L -> duration
            else -> player.currentPosition.coerceAtLeast(0L).coerceAtMost(duration.takeIf { it > 0L } ?: Long.MAX_VALUE)
        }

        _playbackPositionMs.value = position
        _playbackDurationMs.value = duration
        _completionPercent.value = if (duration == 0L) {
            0f
        } else {
            (position.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
        }
    }

    private fun Int.toPlaybackState(isPlaying: Boolean): PlaybackState = when {
        isPlaying -> PlaybackState.PLAYING
        this == Player.STATE_IDLE -> PlaybackState.IDLE
        this == Player.STATE_BUFFERING -> PlaybackState.BUFFERING
        this == Player.STATE_READY && hasStarted -> PlaybackState.PAUSED
        this == Player.STATE_READY -> PlaybackState.READY
        this == Player.STATE_ENDED -> PlaybackState.ENDED
        else -> PlaybackState.ERROR
    }
}
