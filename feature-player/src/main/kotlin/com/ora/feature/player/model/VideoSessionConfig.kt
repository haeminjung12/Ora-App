package com.ora.feature.player.model

import android.net.Uri

data class VideoSessionConfig(
    val sessionId: String,
    val videoId: String,
    val videoUri: Uri,
    val autoPlay: Boolean = true,
)
