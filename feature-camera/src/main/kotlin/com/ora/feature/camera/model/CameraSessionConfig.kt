package com.ora.feature.camera.model

import android.util.Size
import androidx.camera.core.ImageAnalysis

data class CameraSessionConfig(
    val sessionId: String,
    val autoStart: Boolean = true,
    val targetResolution: Size = Size(480, 640),
    val backpressureStrategy: Int = ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST,
)
