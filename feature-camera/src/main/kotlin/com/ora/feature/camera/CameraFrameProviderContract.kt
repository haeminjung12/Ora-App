package com.ora.feature.camera

import androidx.lifecycle.LifecycleOwner
import com.ora.feature.camera.model.CameraFrame
import com.ora.feature.camera.model.CameraSessionConfig
import com.ora.feature.camera.model.CameraSessionEvent
import com.ora.feature.camera.model.CameraStatus
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface CameraFrameProviderContract {
    val cameraStatus: StateFlow<CameraStatus>
    val activeSession: StateFlow<CameraSessionConfig?>
    val frameStream: SharedFlow<CameraFrame>
    val sessionEvents: SharedFlow<CameraSessionEvent>

    fun prepareSession(config: CameraSessionConfig)
    fun bind(lifecycleOwner: LifecycleOwner)
    fun updatePermission(granted: Boolean)
    fun start()
    fun stop()
    fun release()
}
