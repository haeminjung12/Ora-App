package com.ora.feature.camera.model

import androidx.camera.core.ImageProxy
import java.io.Closeable

data class CameraFrame(
    val sessionId: String,
    val frameIndex: Long,
    val timestampMs: Long,
    val rotationDegrees: Int,
    val image: ImageProxy,
) : Closeable {
    override fun close() {
        image.close()
    }
}
