package com.ora.data.session.model

data class FeatureFrame(
    val sessionId: String,
    val timestampMs: Long,
    val mouthOpennessSignal: Float,
    val mouthOpenEvent: Boolean,
    val headMotionDelta: Float,
    val faceTrackingQuality: TrackingQuality,
)

enum class TrackingQuality {
    GOOD,
    DEGRADED,
    LOST,
}

