package com.ora.feature.camera.model

sealed interface CameraSessionEvent {
    data class Started(
        val sessionId: String,
        val startedAtEpochMs: Long,
    ) : CameraSessionEvent

    data class Stopped(
        val sessionId: String,
        val stoppedAtEpochMs: Long,
        val framesProduced: Long,
    ) : CameraSessionEvent
}
