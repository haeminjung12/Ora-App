package com.ora.data.session.export

object SessionJsonExporter {
    fun exportSession(record: SessionExportRecord): String = buildString {
        appendSession(record)
    }

    fun exportSessions(records: List<SessionExportRecord>): String = buildString {
        append("{\"sessions\":[")
        records.forEachIndexed { index, record ->
            if (index > 0) {
                append(',')
            }
            appendSession(record)
        }
        append("]}")
    }

    private fun StringBuilder.appendSession(record: SessionExportRecord) {
        val summary = record.summary
        append('{')
        appendJsonField("sessionId", summary.sessionId)
        append(',')
        appendJsonField("videoId", summary.videoId)
        append(',')
        appendJsonField("startedAtEpochMs", summary.startedAtEpochMs)
        append(',')
        appendJsonField("endedAtEpochMs", summary.endedAtEpochMs)
        append(',')
        appendJsonField("totalDurationMs", summary.totalDurationMs)
        append(',')
        appendJsonField("playbackCompletionPercent", summary.playbackCompletionPercent)
        append(',')
        appendJsonField("faceDetectedPercent", summary.faceDetectedPercent)
        append(',')
        appendJsonField("mouthMovementEventCount", summary.mouthMovementEventCount)
        append(',')
        appendJsonField("headMotionMagnitude", summary.headMotionMagnitude)
        append(',')
        append("\"setupReadiness\":")
        appendSetupReadiness(summary.readiness)
        append(',')
        append("\"debugArtifacts\":")
        appendDebugArtifacts(summary.debugArtifacts)
        append(',')
        append("\"featureFrames\":")
        appendFeatureFrames(record.featureFrames)
        append(',')
        append("\"landmarkFrames\":")
        appendLandmarkFrames(record.landmarkFrames)
        append('}')
    }

    private fun StringBuilder.appendSetupReadiness(readiness: com.ora.data.session.model.SetupReadinessResult) {
        append('{')
        appendJsonField("lightingPassed", readiness.lightingPassed)
        append(',')
        appendJsonField("faceFramingPassed", readiness.faceFramingPassed)
        append(',')
        appendJsonField("deviceOrientationPassed", readiness.deviceOrientationPassed)
        append(',')
        appendJsonField("distancePassed", readiness.distancePassed)
        append(',')
        appendJsonField("ready", readiness.ready)
        append('}')
    }

    private fun StringBuilder.appendDebugArtifacts(artifacts: com.ora.data.session.model.SessionDebugArtifacts) {
        append('{')
        appendJsonNullableField("summaryJsonPath", artifacts.summaryJsonPath)
        append(',')
        appendJsonNullableField("featureTracePath", artifacts.featureTracePath)
        append(',')
        appendJsonNullableField("landmarkTracePath", artifacts.landmarkTracePath)
        append(',')
        append("\"notes\":")
        appendJsonStringMap(artifacts.notes)
        append('}')
    }

    private fun StringBuilder.appendFeatureFrames(featureFrames: List<com.ora.data.session.model.FeatureFrame>) {
        append('[')
        featureFrames.forEachIndexed { index, frame ->
            if (index > 0) {
                append(',')
            }
            append('{')
            appendJsonField("timestampMs", frame.timestampMs)
            append(',')
            appendJsonField("mouthOpennessSignal", frame.mouthOpennessSignal)
            append(',')
            appendJsonField("mouthOpenEvent", frame.mouthOpenEvent)
            append(',')
            appendJsonField("headMotionDelta", frame.headMotionDelta)
            append(',')
            appendJsonField("faceTrackingQuality", frame.faceTrackingQuality.name)
            append('}')
        }
        append(']')
    }

    private fun StringBuilder.appendLandmarkFrames(landmarkFrames: List<com.ora.data.session.model.LandmarkFrame>) {
        append('[')
        landmarkFrames.forEachIndexed { index, frame ->
            if (index > 0) {
                append(',')
            }
            append('{')
            appendJsonField("timestampMs", frame.timestampMs)
            append(',')
            appendJsonField("frameIndex", frame.frameIndex)
            append(',')
            appendJsonField("facePresent", frame.facePresent)
            append(',')
            appendJsonField("trackingConfidence", frame.trackingConfidence)
            append(',')
            append("\"headPose\":")
            appendHeadPose(frame.headPose)
            append(',')
            append("\"landmarks\":")
            appendNormalizedLandmarks(frame.landmarks)
            append('}')
        }
        append(']')
    }

    private fun StringBuilder.appendHeadPose(headPose: com.ora.data.session.model.HeadPoseEstimate?) {
        if (headPose == null) {
            append("null")
            return
        }

        append('{')
        appendJsonField("pitchDegrees", headPose.pitchDegrees)
        append(',')
        appendJsonField("yawDegrees", headPose.yawDegrees)
        append(',')
        appendJsonField("rollDegrees", headPose.rollDegrees)
        append('}')
    }

    private fun StringBuilder.appendNormalizedLandmarks(landmarks: List<com.ora.data.session.model.NormalizedLandmark>) {
        append('[')
        landmarks.forEachIndexed { index, landmark ->
            if (index > 0) {
                append(',')
            }
            append('{')
            appendJsonField("x", landmark.x)
            append(',')
            appendJsonField("y", landmark.y)
            append(',')
            appendJsonField("z", landmark.z)
            append('}')
        }
        append(']')
    }

    private fun StringBuilder.appendJsonField(name: String, value: String) {
        appendQuoted(name)
        append(':')
        appendQuoted(value)
    }

    private fun StringBuilder.appendJsonField(name: String, value: Long) {
        appendQuoted(name)
        append(':')
        append(value)
    }

    private fun StringBuilder.appendJsonField(name: String, value: Int) {
        appendQuoted(name)
        append(':')
        append(value)
    }

    private fun StringBuilder.appendJsonField(name: String, value: Float) {
        appendQuoted(name)
        append(':')
        append(normalizeFloat(value))
    }

    private fun StringBuilder.appendJsonField(name: String, value: Boolean) {
        appendQuoted(name)
        append(':')
        append(value)
    }

    private fun StringBuilder.appendJsonNullableField(name: String, value: Long?) {
        appendQuoted(name)
        append(':')
        if (value == null) {
            append("null")
        } else {
            append(value)
        }
    }

    private fun StringBuilder.appendJsonNullableField(name: String, value: String?) {
        appendQuoted(name)
        append(':')
        if (value == null) {
            append("null")
        } else {
            appendQuoted(value)
        }
    }

    private fun StringBuilder.appendJsonStringMap(values: Map<String, String>) {
        append('{')
        values.entries.forEachIndexed { index, entry ->
            if (index > 0) {
                append(',')
            }
            appendQuoted(entry.key)
            append(':')
            appendQuoted(entry.value)
        }
        append('}')
    }

    private fun StringBuilder.appendQuoted(value: String) {
        append('"')
        value.forEach { character ->
            when (character) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\b' -> append("\\b")
                '\u000C' -> append("\\f")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> {
                    if (character.code < 0x20) {
                        append("\\u")
                        append(character.code.toString(16).padStart(4, '0'))
                    } else {
                        append(character)
                    }
                }
            }
        }
        append('"')
    }

    private fun normalizeFloat(value: Float): String {
        return if (value.isFinite()) value.toString() else "0.0"
    }
}
