package com.ora.feature.player.model

sealed interface SessionEvent {
    data class Started(
        val sessionId: String,
        val videoId: String,
        val startedAtEpochMs: Long,
        val autoPlay: Boolean,
    ) : SessionEvent

    data class Stopped(
        val sessionId: String,
        val videoId: String,
        val stoppedAtEpochMs: Long,
        val reason: StopReason,
        val positionMs: Long,
        val durationMs: Long,
        val completionPercent: Float,
        val endedNaturally: Boolean = reason == StopReason.COMPLETED,
        val errorMessage: String? = null,
    ) : SessionEvent

    enum class StopReason {
        USER_STOPPED,
        SESSION_REPLACED,
        COMPLETED,
        RELEASED,
        ERROR,
    }
}
