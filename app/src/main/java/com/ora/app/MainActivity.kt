package com.ora.app

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ora.feature.camera.model.CameraSessionConfig
import com.ora.feature.camera.model.CameraSessionEvent
import com.ora.feature.camera.model.CameraStatus
import com.ora.feature.camera.runtime.CameraXFrameProvider
import com.ora.feature.player.model.SessionEvent
import com.ora.feature.player.model.VideoSessionConfig
import com.ora.feature.player.runtime.Media3PlayerSessionController
import com.ora.feature.player.ui.VideoSessionScreen
import kotlinx.coroutines.flow.collectLatest
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OraTrackATheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PlayerFoundationRoute()
                }
            }
        }
    }
}

@Composable
private fun PlayerFoundationRoute() {
    var selectedVideoUri by rememberSaveable { mutableStateOf<String?>(null) }
    val activeVideoUri = selectedVideoUri?.let(Uri::parse)

    if (activeVideoUri == null) {
        FoundationHome(
            onVideoSelected = { uri ->
                selectedVideoUri = uri.toString()
            }
        )
        return
    }

    val sessionConfig = remember(activeVideoUri) {
        VideoSessionConfig(
            sessionId = "session-${UUID.randomUUID()}",
            videoId = activeVideoUri.lastPathSegment ?: "local-video",
            videoUri = activeVideoUri,
            autoPlay = true,
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ActiveRuntimeSession(
            sessionConfig = sessionConfig,
            onExitSession = { selectedVideoUri = null },
        )
    }
}

@Composable
private fun ActiveRuntimeSession(
    sessionConfig: VideoSessionConfig,
    onExitSession: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val playerController = remember(context, sessionConfig.sessionId) {
        Media3PlayerSessionController(context)
    }
    val cameraProvider = remember(context, sessionConfig.sessionId) {
        CameraXFrameProvider(context)
    }
    var cameraPermissionGranted by rememberSaveable(sessionConfig.sessionId) { mutableStateOf(false) }
    var capturedFrameCount by remember(sessionConfig.sessionId) { mutableStateOf(0L) }
    var latestCameraEvent by remember(sessionConfig.sessionId) { mutableStateOf("Waiting for camera session") }
    val cameraStatus by cameraProvider.cameraStatus.collectAsState()
    val requestCameraPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            cameraPermissionGranted = granted
        },
    )

    DisposableEffect(playerController, cameraProvider, lifecycleOwner, sessionConfig.sessionId) {
        cameraProvider.bind(lifecycleOwner)
        cameraProvider.prepareSession(
            CameraSessionConfig(
                sessionId = sessionConfig.sessionId,
                autoStart = true,
            )
        )
        onDispose {
            cameraProvider.release()
            playerController.release()
        }
    }

    LaunchedEffect(sessionConfig.sessionId) {
        requestCameraPermission.launch(android.Manifest.permission.CAMERA)
    }

    LaunchedEffect(cameraProvider, cameraPermissionGranted) {
        cameraProvider.updatePermission(cameraPermissionGranted)
    }

    LaunchedEffect(cameraProvider, sessionConfig.sessionId) {
        cameraProvider.frameStream.collectLatest { frame ->
            capturedFrameCount = frame.frameIndex + 1L
            frame.close()
        }
    }

    LaunchedEffect(cameraProvider, sessionConfig.sessionId) {
        cameraProvider.sessionEvents.collectLatest { event ->
            latestCameraEvent = event.toUiLabel()
        }
    }

    LaunchedEffect(playerController, sessionConfig.sessionId) {
        playerController.sessionEvents.collectLatest { event ->
            if (event is SessionEvent.Stopped) {
                cameraProvider.stop()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        VideoSessionScreen(
            controller = playerController,
            config = sessionConfig,
        )
        SessionRuntimeHud(
            cameraStatus = cameraStatus,
            cameraPermissionGranted = cameraPermissionGranted,
            capturedFrameCount = capturedFrameCount,
            latestCameraEvent = latestCameraEvent,
            onChooseAnotherVideo = onExitSession,
            modifier = Modifier
                .padding(WindowInsets.safeDrawing.asPaddingValues())
                .padding(16.dp),
        )
    }
}

@Composable
private fun FoundationHome(
    onVideoSelected: (Uri) -> Unit,
) {
    val openVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri != null) {
                onVideoSelected(uri)
            }
        },
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.colorScheme.background,
                    )
                )
            )
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Ora Track A Video Session",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "The foundation app now hosts the real player module. Pick a local video file to start a full-screen session using the `:feature-player` runtime.",
            style = MaterialTheme.typography.bodyLarge,
        )
        Button(
            onClick = { openVideoLauncher.launch(arrayOf("video/*")) },
        ) {
            Text("Select Local Video")
        }
        ModuleCard(
            title = ":feature-player",
            body = "Active integration target. Hosts Media3 playback, session lifecycle, progress, and session events.",
        )
        ModuleCard(
            title = ":feature-camera",
            body = "Next runtime companion. Front-camera ingestion and timestamps can be layered beside the player session once runtime orchestration is added.",
        )
        ModuleCard(
            title = ":data-session",
            body = "Session summaries, feature traces, research landmark storage, and export contracts.",
        )
        ModuleCard(
            title = ":core-contracts",
            body = "Frozen project-wide constants and baseline metadata owned by the foundation branch.",
        )
    }
}

@Composable
private fun ModuleCard(
    title: String,
    body: String,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun SessionRuntimeHud(
    cameraStatus: CameraStatus,
    cameraPermissionGranted: Boolean,
    capturedFrameCount: Long,
    latestCameraEvent: String,
    onChooseAnotherVideo: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = Color(0xA6141820),
        contentColor = Color.White,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Runtime Spine",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Camera permission: ${if (cameraPermissionGranted) "granted" else "missing"}",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = "Camera status: ${cameraStatus.toUiLabel()}",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = "Frames captured: $capturedFrameCount",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = latestCameraEvent,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFD5DCE3),
            )
            Button(onClick = onChooseAnotherVideo) {
                Text("Choose Another Video")
            }
        }
    }
}

private fun CameraStatus.toUiLabel(): String = when (this) {
    CameraStatus.Idle -> "Idle"
    CameraStatus.RequestingPermission -> "Requesting permission"
    CameraStatus.Ready -> "Ready"
    CameraStatus.Running -> "Running"
    CameraStatus.Denied -> "Denied"
    is CameraStatus.Error -> "Error: $message"
}

private fun CameraSessionEvent.toUiLabel(): String = when (this) {
    is CameraSessionEvent.Started -> "Camera started for $sessionId"
    is CameraSessionEvent.Stopped -> "Camera stopped for $sessionId after $framesProduced frames"
}
