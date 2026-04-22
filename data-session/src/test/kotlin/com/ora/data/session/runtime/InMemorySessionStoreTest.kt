package com.ora.data.session.runtime

import com.ora.data.session.contract.ResearchLandmarkRejectionReason
import com.ora.data.session.contract.ResearchStorageMode
import com.ora.data.session.contract.ResearchStoragePolicy
import com.ora.data.session.model.FeatureFrame
import com.ora.data.session.model.LandmarkFrame
import com.ora.data.session.model.NormalizedLandmark
import com.ora.data.session.model.TrackingQuality
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

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

    @Test
    fun `feature trace reads stay ordered during concurrent appends`() {
        val store = InMemorySessionStore()
        val iterations = 250
        val executor = Executors.newFixedThreadPool(2)
        val startGate = CountDownLatch(1)
        val failure = AtomicReference<Throwable?>()

        val writer = executor.submit {
            startGate.await()
            for (index in 0 until iterations) {
                store.appendFeatureFrame(
                    testFeatureFrame(
                        timestampMs = (iterations - index).toLong(),
                    )
                )
            }
        }

        val reader = executor.submit {
            startGate.await()
            repeat(iterations) {
                val snapshot = store.getFeatureTrace("session-1")
                try {
                    assertOrderedBy(snapshot.map { it.timestampMs })
                } catch (t: Throwable) {
                    failure.compareAndSet(null, t)
                }
            }
        }

        startGate.countDown()
        writer.get(5, TimeUnit.SECONDS)
        reader.get(5, TimeUnit.SECONDS)
        executor.shutdownNow()

        failure.get()?.let { throw it }
        val finalSnapshot = store.getFeatureTrace("session-1")
        assertEquals(iterations, finalSnapshot.size)
        assertOrderedBy(finalSnapshot.map { it.timestampMs })
    }

    @Test
    fun `research landmark reads stay ordered during concurrent appends`() {
        val store = InMemorySessionStore()
        val policy = ResearchStoragePolicy(mode = ResearchStorageMode.ENABLED)
        val iterations = 250
        val executor = Executors.newFixedThreadPool(2)
        val startGate = CountDownLatch(1)
        val failure = AtomicReference<Throwable?>()

        val writer = executor.submit {
            startGate.await()
            for (index in 0 until iterations) {
                store.appendResearchLandmark(
                    frame = testLandmarkFrame(frameIndex = (iterations - index).toLong()),
                    policy = policy,
                )
            }
        }

        val reader = executor.submit {
            startGate.await()
            repeat(iterations) {
                val snapshot = store.getResearchLandmarks("session-1", policy).frames
                try {
                    assertOrderedBy(snapshot.map { it.frameIndex })
                } catch (t: Throwable) {
                    failure.compareAndSet(null, t)
                }
            }
        }

        startGate.countDown()
        writer.get(5, TimeUnit.SECONDS)
        reader.get(5, TimeUnit.SECONDS)
        executor.shutdownNow()

        failure.get()?.let { throw it }
        val finalSnapshot = store.getResearchLandmarks("session-1", policy).frames
        assertEquals(iterations, finalSnapshot.size)
        assertOrderedBy(finalSnapshot.map { it.frameIndex })
    }

    private fun testFeatureFrame(timestampMs: Long): FeatureFrame {
        return FeatureFrame(
            sessionId = "session-1",
            timestampMs = timestampMs,
            mouthOpennessSignal = 0.42f,
            mouthOpenEvent = true,
            headMotionDelta = 0.15f,
            faceTrackingQuality = TrackingQuality.GOOD,
        )
    }

    private fun testLandmarkFrame(frameIndex: Long = 7L): LandmarkFrame {
        return LandmarkFrame(
            sessionId = "session-1",
            timestampMs = 1500L,
            frameIndex = frameIndex,
            facePresent = true,
            landmarks = listOf(
                NormalizedLandmark(x = 0.1f, y = 0.2f, z = 0.3f),
            ),
            trackingConfidence = 0.91f,
        )
    }

    private fun assertOrderedBy(values: List<Long>) {
        val sorted = values.sorted()
        assertEquals(sorted, values)
    }
}
