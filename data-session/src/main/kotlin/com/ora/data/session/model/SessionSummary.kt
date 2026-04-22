package com.ora.data.session.model

data class SessionSummary(
    val sessionId: String,
    val videoId: String,
    val startedAtEpochMs: Long,
    val endedAtEpochMs: Long,
    val totalDurationMs: Long,
    val playbackCompletionPercent: Float,
    val faceDetectedPercent: Float,
    val mouthMovementEventCount: Int,
    val headMotionMagnitude: Float,
    val readiness: SetupReadinessResult,
    val debugArtifacts: SessionDebugArtifacts = SessionDebugArtifacts(),
)

data class SessionDebugArtifacts(
    val summaryJsonPath: String? = null,
    val featureTracePath: String? = null,
    val landmarkTracePath: String? = null,
    val notes: Map<String, String> = emptyMap(),
)

