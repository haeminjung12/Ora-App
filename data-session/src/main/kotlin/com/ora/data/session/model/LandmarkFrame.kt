package com.ora.data.session.model

data class LandmarkFrame(
    val sessionId: String,
    val timestampMs: Long,
    val frameIndex: Long,
    val facePresent: Boolean,
    val landmarks: List<NormalizedLandmark>,
    val trackingConfidence: Float,
    val headPose: HeadPoseEstimate? = null,
)

data class NormalizedLandmark(
    val x: Float,
    val y: Float,
    val z: Float,
)

data class HeadPoseEstimate(
    val pitchDegrees: Float,
    val yawDegrees: Float,
    val rollDegrees: Float,
)

