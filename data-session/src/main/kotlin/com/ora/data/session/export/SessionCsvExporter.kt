package com.ora.data.session.export

object SessionCsvExporter {
    fun exportSessionSummaries(records: List<SessionExportRecord>): String = buildString {
        appendLine(
            "session_id,video_id,started_at_epoch_ms,ended_at_epoch_ms,total_duration_ms," +
                "playback_completion_percent,face_detected_percent,mouth_movement_event_count," +
                "head_motion_magnitude,lighting_passed,face_framing_passed," +
                "device_orientation_passed,distance_passed,ready,has_summary_json,has_feature_trace," +
                "has_landmark_trace,debug_note_count,feature_frame_count,landmark_frame_count"
        )
        records.forEach { record ->
            val summary = record.summary
            appendCsvRow(
                listOf(
                    summary.sessionId,
                    summary.videoId,
                    summary.startedAtEpochMs,
                    summary.endedAtEpochMs,
                    summary.totalDurationMs,
                    summary.playbackCompletionPercent,
                    summary.faceDetectedPercent,
                    summary.mouthMovementEventCount,
                    summary.headMotionMagnitude,
                    summary.readiness.lightingPassed,
                    summary.readiness.faceFramingPassed,
                    summary.readiness.deviceOrientationPassed,
                    summary.readiness.distancePassed,
                    summary.readiness.ready,
                    summary.debugArtifacts.summaryJsonPath != null,
                    summary.debugArtifacts.featureTracePath != null,
                    summary.debugArtifacts.landmarkTracePath != null,
                    summary.debugArtifacts.notes.size,
                    record.featureFrames.size,
                    record.landmarkFrames.size,
                )
            )
        }
    }

    fun exportFeatureFrames(record: SessionExportRecord): String = buildString {
        appendLine(
            "session_id,video_id,timestamp_ms,mouth_openness_signal,mouth_open_event," +
                "head_motion_delta,face_tracking_quality"
        )
        record.featureFrames.forEach { frame ->
            appendCsvRow(
                listOf(
                    record.summary.sessionId,
                    record.summary.videoId,
                    frame.timestampMs,
                    frame.mouthOpennessSignal,
                    frame.mouthOpenEvent,
                    frame.headMotionDelta,
                    frame.faceTrackingQuality.name,
                )
            )
        }
    }

    fun exportArtifacts(record: SessionExportRecord): String = buildString {
        appendLine("session_id,video_id,artifact_key,artifact_value")
        val summary = record.summary
        val artifacts = buildList {
            add("summary_json_path" to summary.debugArtifacts.summaryJsonPath)
            add("feature_trace_path" to summary.debugArtifacts.featureTracePath)
            add("landmark_trace_path" to summary.debugArtifacts.landmarkTracePath)
            summary.debugArtifacts.notes.forEach { (key, value) ->
                add("note:$key" to value)
            }
        }

        artifacts.forEach { (key, value) ->
            appendCsvRow(listOf(summary.sessionId, summary.videoId, key, value))
        }
    }

    fun exportLandmarkFrames(record: SessionExportRecord): String = buildString {
        appendLine(
            "session_id,video_id,timestamp_ms,frame_index,face_present,tracking_confidence," +
                "head_pose_pitch_degrees,head_pose_yaw_degrees,head_pose_roll_degrees,landmark_count"
        )
        record.landmarkFrames.forEach { frame ->
            appendCsvRow(
                listOf(
                    record.summary.sessionId,
                    record.summary.videoId,
                    frame.timestampMs,
                    frame.frameIndex,
                    frame.facePresent,
                    frame.trackingConfidence,
                    frame.headPose?.pitchDegrees,
                    frame.headPose?.yawDegrees,
                    frame.headPose?.rollDegrees,
                    frame.landmarks.size,
                )
            )
        }
    }

    private fun StringBuilder.appendCsvRow(values: List<Any?>) {
        values.forEachIndexed { index, value ->
            if (index > 0) {
                append(',')
            }
            append(escapeCsv(value))
        }
        appendLine()
    }

    private fun escapeCsv(value: Any?): String {
        if (value == null) {
            return ""
        }

        val text = when (value) {
            is Float -> if (value.isFinite()) value.toString() else "0.0"
            else -> value.toString()
        }

        val needsQuotes = text.any { it == ',' || it == '"' || it == '\n' || it == '\r' }
        if (!needsQuotes) {
            return text
        }

        return buildString(text.length + 2) {
            append('"')
            text.forEach { character ->
                if (character == '"') {
                    append("\"\"")
                } else {
                    append(character)
                }
            }
            append('"')
        }
    }
}
