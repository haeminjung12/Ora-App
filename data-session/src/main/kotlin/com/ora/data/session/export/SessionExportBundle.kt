package com.ora.data.session.export

data class SessionExportBundle(
    val sessionJson: String,
    val summaryCsv: String,
    val featureFramesCsv: String,
    val landmarkFramesCsv: String,
    val artifactsCsv: String,
)

object SessionExportBundleFactory {
    fun create(record: SessionExportRecord): SessionExportBundle {
        return SessionExportBundle(
            sessionJson = SessionJsonExporter.exportSession(record),
            summaryCsv = SessionCsvExporter.exportSessionSummaries(listOf(record)),
            featureFramesCsv = SessionCsvExporter.exportFeatureFrames(record),
            landmarkFramesCsv = SessionCsvExporter.exportLandmarkFrames(record),
            artifactsCsv = SessionCsvExporter.exportArtifacts(record),
        )
    }
}
