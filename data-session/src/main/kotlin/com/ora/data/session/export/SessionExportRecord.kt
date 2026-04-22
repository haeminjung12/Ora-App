package com.ora.data.session.export

import com.ora.data.session.model.FeatureFrame
import com.ora.data.session.model.LandmarkFrame
import com.ora.data.session.model.SessionSummary

data class SessionExportRecord(
    val summary: SessionSummary,
    val featureFrames: List<FeatureFrame> = emptyList(),
    val landmarkFrames: List<LandmarkFrame> = emptyList(),
)
