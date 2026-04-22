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
    private val featureTraces = ConcurrentHashMap<String, MutableList<FeatureFrame>>()
    private val researchLandmarks = ConcurrentHashMap<String, MutableList<LandmarkFrame>>()

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
            .toMutableList()
    }

    override fun appendFeatureFrame(frame: FeatureFrame) {
        val frames = featureTraces.getOrPut(frame.sessionId) { mutableListOf() }
        synchronized(frames) {
            frames.add(frame)
            frames.sortBy { it.timestampMs }
        }
    }

    override fun getFeatureTrace(sessionId: String): List<FeatureFrame> =
        featureTraces[sessionId]
            ?.toList()
            .orEmpty()

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

        val frames = researchLandmarks.getOrPut(frame.sessionId) { mutableListOf() }
        synchronized(frames) {
            val maxFrames = policy.maxFramesPerSession
            if (maxFrames != null && frames.size >= maxFrames) {
                return ResearchLandmarkWriteResult(
                    accepted = false,
                    storedFrameCount = frames.size,
                    reason = ResearchLandmarkRejectionReason.SESSION_LIMIT_REACHED,
                )
            }

            frames.add(frame)
            frames.sortBy { it.frameIndex }
            return ResearchLandmarkWriteResult(
                accepted = true,
                storedFrameCount = frames.size,
            )
        }
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

        researchLandmarks[sessionId] = normalizedFrames.toMutableList()
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
            frames = researchLandmarks[sessionId]
                ?.toList()
                .orEmpty(),
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
