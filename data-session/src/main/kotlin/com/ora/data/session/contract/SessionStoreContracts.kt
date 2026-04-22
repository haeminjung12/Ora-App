package com.ora.data.session.contract

import com.ora.data.session.model.FeatureFrame
import com.ora.data.session.model.LandmarkFrame
import com.ora.data.session.model.SessionHistoryPage
import com.ora.data.session.model.SessionSummary

interface SessionSummaryRepository {
    fun upsertSummary(summary: SessionSummary)
    fun getSummary(sessionId: String): SessionSummary?
    fun listSessionHistory(query: SessionHistoryQuery = SessionHistoryQuery()): SessionHistoryPage
}

interface FeatureTraceRepository {
    fun replaceFeatureTrace(sessionId: String, frames: List<FeatureFrame>)
    fun appendFeatureFrame(frame: FeatureFrame)
    fun getFeatureTrace(sessionId: String): List<FeatureFrame>
}

interface ResearchLandmarkRepository {
    fun appendResearchLandmark(frame: LandmarkFrame, policy: ResearchStoragePolicy): ResearchLandmarkWriteResult
    fun replaceResearchLandmarks(
        sessionId: String,
        frames: List<LandmarkFrame>,
        policy: ResearchStoragePolicy,
    ): ResearchLandmarkWriteResult

    fun getResearchLandmarks(
        sessionId: String,
        policy: ResearchStoragePolicy,
    ): ResearchLandmarkReadResult

    fun clearResearchLandmarks(sessionId: String)
}

interface SessionStore :
    SessionSummaryRepository,
    FeatureTraceRepository,
    ResearchLandmarkRepository

data class SessionHistoryQuery(
    val limit: Int = 50,
    val offset: Int = 0,
    val sortOrder: SessionHistorySortOrder = SessionHistorySortOrder.STARTED_AT_DESC,
)

enum class SessionHistorySortOrder {
    STARTED_AT_DESC,
    STARTED_AT_ASC,
}

data class ResearchStoragePolicy(
    val mode: ResearchStorageMode,
    val maxFramesPerSession: Int? = null,
)

enum class ResearchStorageMode {
    DISABLED,
    ENABLED,
}

data class ResearchLandmarkWriteResult(
    val accepted: Boolean,
    val storedFrameCount: Int,
    val reason: ResearchLandmarkRejectionReason? = null,
)

data class ResearchLandmarkReadResult(
    val accepted: Boolean,
    val frames: List<LandmarkFrame>,
    val reason: ResearchLandmarkRejectionReason? = null,
)

enum class ResearchLandmarkRejectionReason {
    RESEARCH_MODE_DISABLED,
    SESSION_LIMIT_REACHED,
}
