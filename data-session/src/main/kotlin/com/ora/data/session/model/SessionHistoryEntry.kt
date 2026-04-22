package com.ora.data.session.model

data class SessionHistoryEntry(
    val sessionId: String,
    val videoId: String,
    val startedAtEpochMs: Long,
    val endedAtEpochMs: Long,
    val playbackCompletionPercent: Float,
    val faceDetectedPercent: Float,
    val mouthMovementEventCount: Int,
    val researchArtifactsStored: Boolean,
)

data class SessionHistoryPage(
    val items: List<SessionHistoryEntry>,
    val nextOffset: Int?,
)

