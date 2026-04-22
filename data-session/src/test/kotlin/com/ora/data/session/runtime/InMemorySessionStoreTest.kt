package com.ora.data.session.runtime

import com.ora.data.session.contract.ResearchLandmarkRejectionReason
import com.ora.data.session.contract.ResearchStorageMode
import com.ora.data.session.contract.ResearchStoragePolicy
import com.ora.data.session.model.LandmarkFrame
import com.ora.data.session.model.NormalizedLandmark
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InMemorySessionStoreTest {

    @Test
    fun `research landmark reads are denied when policy disables research mode`() {
        val store = InMemorySessionStore()
        val enabledPolicy = ResearchStoragePolicy(mode = ResearchStorageMode.ENABLED)
        val disabledPolicy = ResearchStoragePolicy(mode = ResearchStorageMode.DISABLED)

        store.appendResearchLandmark(testLandmarkFrame(), enabledPolicy)

        val result = store.getResearchLandmarks("session-1", disabledPolicy)

        assertFalse(result.accepted)
        assertTrue(result.frames.isEmpty())
        assertEquals(ResearchLandmarkRejectionReason.RESEARCH_MODE_DISABLED, result.reason)
    }

    @Test
    fun `research landmark reads succeed when policy enables research mode`() {
        val store = InMemorySessionStore()
        val policy = ResearchStoragePolicy(mode = ResearchStorageMode.ENABLED)

        store.appendResearchLandmark(testLandmarkFrame(), policy)

        val result = store.getResearchLandmarks("session-1", policy)

        assertTrue(result.accepted)
        assertEquals(1, result.frames.size)
        assertEquals("session-1", result.frames.single().sessionId)
    }

    private fun testLandmarkFrame(): LandmarkFrame {
        return LandmarkFrame(
            sessionId = "session-1",
            timestampMs = 1500L,
            frameIndex = 7L,
            facePresent = true,
            landmarks = listOf(
                NormalizedLandmark(x = 0.1f, y = 0.2f, z = 0.3f),
            ),
            trackingConfidence = 0.91f,
        )
    }
}
