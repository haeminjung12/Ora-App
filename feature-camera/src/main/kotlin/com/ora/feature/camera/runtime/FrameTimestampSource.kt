package com.ora.feature.camera.runtime

import android.os.SystemClock

fun interface FrameTimestampSource {
    fun timestampMs(): Long
}

object ElapsedRealtimeTimestampSource : FrameTimestampSource {
    override fun timestampMs(): Long = SystemClock.elapsedRealtimeNanos() / 1_000_000L
}
