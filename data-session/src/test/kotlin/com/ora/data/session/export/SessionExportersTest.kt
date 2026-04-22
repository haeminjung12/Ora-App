package com.ora.data.session.export

import com.ora.data.session.model.HeadPoseEstimate
import com.ora.data.session.model.LandmarkFrame
import com.ora.data.session.model.NormalizedLandmark
import com.ora.data.session.model.SessionDebugArtifacts
import com.ora.data.session.model.SessionSummary
import com.ora.data.session.model.SetupReadinessResult
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionExportersTest {

    @Test
    fun `json export includes landmark frames`() {
        val record = SessionExportRecord(
            summary = testSummary(),
            landmarkFrames = listOf(testLandmarkFrame()),
        )

        val json = SessionJsonExporter.exportSession(record)

        assertTrue(json.contains("\"landmarkFrames\":["))
        assertTrue(json.contains("\"frameIndex\":7"))
        assertTrue(json.contains("\"trackingConfidence\":0.91"))
        assertTrue(json.contains("\"headPose\":{"))
        assertTrue(json.contains("\"landmarks\":[{"))
    }

    @Test
    fun `csv export includes landmark frame rows`() {
        val record = SessionExportRecord(
            summary = testSummary(),
            landmarkFrames = listOf(testLandmarkFrame()),
        )

        val csv = SessionCsvExporter.exportLandmarkFrames(record)

        assertTrue(csv.contains("session_id,video_id,timestamp_ms,frame_index"))
        assertTrue(csv.contains("session-1,video-1,1500,7,true,0.91,1.0,2.0,3.0,2"))
    }

    private fun testSummary(): SessionSummary {
        return SessionSummary(
            sessionId = "session-1",
            videoId = "video-1",
            startedAtEpochMs = 1000L,
            endedAtEpochMs = 2000L,
            totalDurationMs = 1000L,
            playbackCompletionPercent = 1.0f,
            faceDetectedPercent = 0.95f,
            mouthMovementEventCount = 4,
            headMotionMagnitude = 1.5f,
            readiness = SetupReadinessResult(
                lightingPassed = true,
                faceFramingPassed = true,
                deviceOrientationPassed = true,
                distancePassed = true,
                ready = true,
            ),
            debugArtifacts = SessionDebugArtifacts(),
        )
    }

    private fun testLandmarkFrame(): LandmarkFrame {
        return LandmarkFrame(
            sessionId = "session-1",
            timestampMs = 1500L,
            frameIndex = 7L,
            facePresent = true,
            landmarks = listOf(
                NormalizedLandmark(x = 0.1f, y = 0.2f, z = 0.3f),
                NormalizedLandmark(x = 0.4f, y = 0.5f, z = 0.6f),
            ),
            trackingConfidence = 0.91f,
            headPose = HeadPoseEstimate(
                pitchDegrees = 1.0f,
                yawDegrees = 2.0f,
                rollDegrees = 3.0f,
            ),
        )
    }
}
