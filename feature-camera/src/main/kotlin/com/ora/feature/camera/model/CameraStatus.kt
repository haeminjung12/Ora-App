package com.ora.feature.camera.model

sealed interface CameraStatus {
    data object Idle : CameraStatus
    data object RequestingPermission : CameraStatus
    data object Ready : CameraStatus
    data object Running : CameraStatus
    data object Denied : CameraStatus

    data class Error(
        val message: String,
        val cause: Throwable? = null,
    ) : CameraStatus
}
