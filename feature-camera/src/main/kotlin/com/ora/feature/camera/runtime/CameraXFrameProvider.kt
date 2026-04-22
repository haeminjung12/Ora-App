package com.ora.feature.camera.runtime

import android.content.Context
import android.view.Surface
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.concurrent.futures.await
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.ora.feature.camera.CameraFrameProviderContract
import com.ora.feature.camera.model.CameraFrame
import com.ora.feature.camera.model.CameraSessionConfig
import com.ora.feature.camera.model.CameraSessionEvent
import com.ora.feature.camera.model.CameraStatus
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CameraXFrameProvider(
    private val context: Context,
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main.immediate,
    private val timestampSource: FrameTimestampSource = ElapsedRealtimeTimestampSource,
) : CameraFrameProviderContract {

    private val scope = CoroutineScope(SupervisorJob() + mainDispatcher)
    private val analysisExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    private val _cameraStatus = MutableStateFlow<CameraStatus>(CameraStatus.Idle)
    private val _activeSession = MutableStateFlow<CameraSessionConfig?>(null)
    private val _frameStream = MutableSharedFlow<CameraFrame>(extraBufferCapacity = 1)
    private val _sessionEvents = MutableSharedFlow<CameraSessionEvent>(extraBufferCapacity = 8)

    override val cameraStatus: StateFlow<CameraStatus> = _cameraStatus.asStateFlow()
    override val activeSession: StateFlow<CameraSessionConfig?> = _activeSession.asStateFlow()
    override val frameStream: SharedFlow<CameraFrame> = _frameStream.asSharedFlow()
    override val sessionEvents: SharedFlow<CameraSessionEvent> = _sessionEvents.asSharedFlow()

    private var lifecycleOwner: LifecycleOwner? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var imageAnalysis: ImageAnalysis? = null
    private var permissionGranted = false
    private var running = false
    private var frameIndex = 0L
    private var sessionStarted = false
    private var lifecycleObserver: LifecycleEventObserver? = null

    override fun prepareSession(config: CameraSessionConfig) {
        if (running) {
            stop()
        }
        _activeSession.value = config
        frameIndex = 0L
        running = false
        sessionStarted = false
        _cameraStatus.value = if (permissionGranted) {
            CameraStatus.Ready
        } else {
            CameraStatus.RequestingPermission
        }
        maybeAutoStart()
    }

    override fun bind(lifecycleOwner: LifecycleOwner) {
        lifecycleObserver?.let { observer ->
            this.lifecycleOwner?.lifecycle?.removeObserver(observer)
        }
        this.lifecycleOwner = lifecycleOwner
        lifecycleObserver = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> maybeAutoStart()
                Lifecycle.Event.ON_STOP -> handleLifecycleStop()
                Lifecycle.Event.ON_DESTROY -> {
                    handleLifecycleStop()
                    clearLifecycleBinding()
                }
                else -> Unit
            }
        }.also { observer ->
            lifecycleOwner.lifecycle.addObserver(observer)
        }
        maybeAutoStart()
    }

    override fun updatePermission(granted: Boolean) {
        permissionGranted = granted
        if (!granted && running) {
            stop()
            return
        }
        _cameraStatus.value = when {
            granted && running -> CameraStatus.Running
            granted -> CameraStatus.Ready
            else -> CameraStatus.Denied
        }
        maybeAutoStart()
    }

    override fun start() {
        val session = _activeSession.value ?: return
        val owner = lifecycleOwner ?: return
        if (!permissionGranted) {
            _cameraStatus.value = CameraStatus.RequestingPermission
            return
        }
        if (running) return

        scope.launch {
            runCatching {
                withContext(mainDispatcher) {
                    val provider = ProcessCameraProvider.getInstance(context).await()
                    provider.unbindAll()
                    provider.bindToLifecycle(owner, FRONT_CAMERA_SELECTOR, buildImageAnalysis(session))
                    cameraProvider = provider
                    running = true
                    _cameraStatus.value = CameraStatus.Running
                    emitStartEvent(session)
                }
            }.onFailure { error ->
                running = false
                _cameraStatus.value = CameraStatus.Error(
                    message = "Unable to start front camera analysis.",
                    cause = error,
                )
            }
        }
    }

    override fun stop() {
        val session = _activeSession.value
        if (!running) {
            cameraProvider?.unbindAll()
            imageAnalysis?.clearAnalyzer()
            imageAnalysis = null
            _cameraStatus.value = if (permissionGranted) CameraStatus.Ready else CameraStatus.Denied
            return
        }

        running = false
        imageAnalysis?.clearAnalyzer()
        imageAnalysis = null
        cameraProvider?.unbindAll()
        _cameraStatus.value = if (permissionGranted) CameraStatus.Ready else CameraStatus.Denied
        if (session != null) {
            emitStopEvent(session)
        }
    }

    override fun release() {
        stop()
        clearLifecycleBinding()
        analysisExecutor.shutdown()
        scope.cancel()
    }

    private fun buildImageAnalysis(session: CameraSessionConfig): ImageAnalysis {
        imageAnalysis?.clearAnalyzer()
        return ImageAnalysis.Builder()
            .setBackpressureStrategy(session.backpressureStrategy)
            .setTargetResolution(session.targetResolution)
            .setTargetRotation(Surface.ROTATION_0)
            .build()
            .also { analysis ->
                analysis.setAnalyzer(analysisExecutor) { imageProxy ->
                    val frame = CameraFrame(
                        sessionId = session.sessionId,
                        frameIndex = frameIndex++,
                        timestampMs = timestampSource.timestampMs(),
                        rotationDegrees = imageProxy.imageInfo.rotationDegrees,
                        image = imageProxy,
                    )
                    if (!_frameStream.tryEmit(frame)) {
                        frame.close()
                    }
                }
                imageAnalysis = analysis
            }
    }

    private fun emitStartEvent(session: CameraSessionConfig) {
        if (sessionStarted) return
        sessionStarted = true
        scope.launch {
            _sessionEvents.emit(
                CameraSessionEvent.Started(
                    sessionId = session.sessionId,
                    startedAtEpochMs = System.currentTimeMillis(),
                )
            )
        }
    }

    private fun emitStopEvent(session: CameraSessionConfig) {
        if (!sessionStarted) return
        sessionStarted = false
        scope.launch {
            _sessionEvents.emit(
                CameraSessionEvent.Stopped(
                    sessionId = session.sessionId,
                    stoppedAtEpochMs = System.currentTimeMillis(),
                    framesProduced = frameIndex,
                )
            )
        }
    }

    private fun maybeAutoStart() {
        val session = _activeSession.value ?: return
        if (!session.autoStart) return
        if (!permissionGranted) return
        val owner = lifecycleOwner ?: return
        if (!owner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) return
        if (running) return
        start()
    }

    private fun handleLifecycleStop() {
        if (!running) {
            if (permissionGranted) {
                _cameraStatus.value = CameraStatus.Ready
            }
            return
        }
        stop()
    }

    private fun clearLifecycleBinding() {
        lifecycleObserver?.let { observer ->
            lifecycleOwner?.lifecycle?.removeObserver(observer)
        }
        lifecycleObserver = null
        lifecycleOwner = null
    }

    private companion object {
        val FRONT_CAMERA_SELECTOR: CameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
            .build()
    }
}
