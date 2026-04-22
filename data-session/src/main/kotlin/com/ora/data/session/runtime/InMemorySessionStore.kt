package com.ora.data.session.runtime

import com.ora.data.session.contract.ResearchLandmarkRejectionReason
import com.ora.data.session.contract.ResearchLandmarkReadResult
import com.ora.data.session.contract.ResearchLandmarkWriteResult
import com.ora.data.session.contract.ResearchStorageMode
import com.ora.data.session.contract.ResearchStoragePolicy
import com.ora.data.session.contract.SessionHistoryQuery
import com.ora.data.session.contract.SessionHistorySortOrder
import com.ora.data.session.contract.SessionStore
import com.ora.data.session.model.FeatureFrame
import com.ora.data.session.model.LandmarkFrame
import com.ora.data.session.model.SessionHistoryEntry
import com.ora.data.session.model.SessionHistoryPage
import com.ora.data.session.model.SessionSummary
import java.util.concurrent.ConcurrentHashMap

class InMemorySessionStore : SessionStore {
    private val summaries = ConcurrentHashMap<String, SessionSummary>()
    private val featureTraces = ConcurrentHashMap<String, List<FeatureFrame>>()
    private val researchLandmarks = ConcurrentHashMap<String, List<LandmarkFrame>>()

    override fun upsertSummary(summary: SessionSummary) {
        summaries[summary.sessionId] = summary
    }

    override fun getSummary(sessionId: String): SessionSummary? = summaries[sessionId]

    override fun listSessionHistory(query: SessionHistoryQuery): SessionHistoryPage {
        val sortedItems = summaries.values
            .asSequence()
            .map { summary ->
                SessionHistoryEntry(
                    sessionId = summary.sessionId,
                    videoId = summary.videoId,
                    startedAtEpochMs = summary.startedAtEpochMs,
                    endedAtEpochMs = summary.endedAtEpochMs,
                    playbackCompletionPercent = summary.playbackCompletionPercent,
                    faceDetectedPercent = summary.faceDetectedPercent,
                    mouthMovementEventCount = summary.mouthMovementEventCount,
                    researchArtifactsStored = !researchLandmarks[summary.sessionId].isNullOrEmpty(),
                )
            }
            .sortedWith(historyComparator(query.sortOrder))
            .toList()

        val safeOffset = query.offset.coerceAtLeast(0)
        val safeLimit = query.limit.coerceAtLeast(1)
        val pageItems = sortedItems.drop(safeOffset).take(safeLimit)
        val nextOffset = (safeOffset + pageItems.size).takeIf { it < sortedItems.size }

        return SessionHistoryPage(
            items = pageItems,
            nextOffset = nextOffset,
        )
    }

    override fun replaceFeatureTrace(sessionId: String, frames: List<FeatureFrame>) {
        featureTraces[sessionId] = frames
            .filter { it.sessionId == sessionId }
            .sortedBy { it.timestampMs }
    }

    override fun appendFeatureFrame(frame: FeatureFrame) {
        featureTraces.compute(frame.sessionId) { _, existing ->
            (existing.orEmpty() + frame).sortedBy { it.timestampMs }
        }
    }

    override fun getFeatureTrace(sessionId: String): List<FeatureFrame> =
        featureTraces[sessionId].orEmpty()

    override fun appendResearchLandmark(
        frame: LandmarkFrame,
        policy: ResearchStoragePolicy,
    ): ResearchLandmarkWriteResult {
        if (policy.mode == ResearchStorageMode.DISABLED) {
            return ResearchLandmarkWriteResult(
                accepted = false,
                storedFrameCount = 0,
                reason = ResearchLandmarkRejectionReason.RESEARCH_MODE_DISABLED,
            )
        }

        var result = ResearchLandmarkWriteResult(
            accepted = true,
            storedFrameCount = 0,
        )

        researchLandmarks.compute(frame.sessionId) { _, existing ->
            val nextFrames = existing.orEmpty()
            val maxFrames = policy.maxFramesPerSession
            if (maxFrames != null && nextFrames.size >= maxFrames) {
                result = ResearchLandmarkWriteResult(
                    accepted = false,
                    storedFrameCount = nextFrames.size,
                    reason = ResearchLandmarkRejectionReason.SESSION_LIMIT_REACHED,
                )
                nextFrames
            } else {
                val updatedFrames = (nextFrames + frame).sortedBy { it.frameIndex }
                result = ResearchLandmarkWriteResult(
                    accepted = true,
                    storedFrameCount = updatedFrames.size,
                )
                updatedFrames
            }
        }

        return result
    }

    override fun replaceResearchLandmarks(
        sessionId: String,
        frames: List<LandmarkFrame>,
        policy: ResearchStoragePolicy,
    ): ResearchLandmarkWriteResult {
        if (policy.mode == ResearchStorageMode.DISABLED) {
            return ResearchLandmarkWriteResult(
                accepted = false,
                storedFrameCount = 0,
                reason = ResearchLandmarkRejectionReason.RESEARCH_MODE_DISABLED,
            )
        }

        val normalizedFrames = frames
            .filter { it.sessionId == sessionId }
            .sortedBy { it.frameIndex }

        val maxFrames = policy.maxFramesPerSession
        if (maxFrames != null && normalizedFrames.size > maxFrames) {
            return ResearchLandmarkWriteResult(
                accepted = false,
                storedFrameCount = 0,
                reason = ResearchLandmarkRejectionReason.SESSION_LIMIT_REACHED,
            )
        }

        researchLandmarks[sessionId] = normalizedFrames
        return ResearchLandmarkWriteResult(
            accepted = true,
            storedFrameCount = normalizedFrames.size,
        )
    }

    override fun getResearchLandmarks(
        sessionId: String,
        policy: ResearchStoragePolicy,
    ): ResearchLandmarkReadResult {
        if (policy.mode == ResearchStorageMode.DISABLED) {
            return ResearchLandmarkReadResult(
                accepted = false,
                frames = emptyList(),
                reason = ResearchLandmarkRejectionReason.RESEARCH_MODE_DISABLED,
            )
        }

        return ResearchLandmarkReadResult(
            accepted = true,
            frames = researchLandmarks[sessionId].orEmpty(),
        )
    }

    override fun clearResearchLandmarks(sessionId: String) {
        researchLandmarks.remove(sessionId)
    }

    private fun historyComparator(sortOrder: SessionHistorySortOrder): Comparator<SessionHistoryEntry> {
        val baseComparator = compareBy<SessionHistoryEntry> { it.startedAtEpochMs }
        return when (sortOrder) {
            SessionHistorySortOrder.STARTED_AT_DESC -> baseComparator.reversed()
            SessionHistorySortOrder.STARTED_AT_ASC -> baseComparator
        }
    }
}
