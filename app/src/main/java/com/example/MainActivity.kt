package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.SimulationCanvas
import com.example.ui.SimulationViewModel
import com.example.ui.components.BottomControlsDrawer
import com.example.ui.components.MatrixEditorSheet
import com.example.ui.components.PresetsSheet
import com.example.ui.components.SettingsSheet
import com.example.ui.components.SupportMeDialog
import com.example.ui.components.TopHudOverlay
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                KineticSandboxScreen()
            }
        }
    }
}

@Composable
fun KineticSandboxScreen(
    viewModel: SimulationViewModel = viewModel()
) {
    val isPlaying by viewModel.isPlaying.collectAsState()
    val timeStep by viewModel.timeStep.collectAsState()
    val friction by viewModel.friction.collectAsState()
    val forceStrength by viewModel.forceStrength.collectAsState()
    val forceDistance by viewModel.forceDistance.collectAsState()
    val boundaryMode by viewModel.boundaryMode.collectAsState()
    val touchMode by viewModel.touchMode.collectAsState()
    val particleCount by viewModel.particleCount.collectAsState()
    val numSpecies by viewModel.numSpecies.collectAsState()
    val speciesList by viewModel.speciesList.collectAsState()
    val backgroundColor by viewModel.backgroundColor.collectAsState()
    val particleRadius by viewModel.particleRadius.collectAsState()
    val particleStyle by viewModel.particleStyle.collectAsState()
    val fps by viewModel.fps.collectAsState()
    val presets by viewModel.allPresets.collectAsState()
    val matrixVersion by viewModel.matrixVersion.collectAsState()
    val renderTick by viewModel.renderTick.collectAsState()

    var showMatrixSheet by remember { mutableStateOf(false) }
    var showPresetsSheet by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showSupportDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // High-Performance Simulation Canvas taking full screen
        // renderTick and matrixVersion key the recomposition
        keyRender(renderTick, matrixVersion) {
            SimulationCanvas(
                simulation = viewModel.simulation,
                speciesList = speciesList.take(numSpecies),
                particleRadius = particleRadius,
                particleStyle = particleStyle,
                backgroundColor = backgroundColor,
                touchMode = touchMode,
                onTouchDown = { x, y -> viewModel.onTouchDown(x, y) },
                onTouchMove = { x, y -> viewModel.onTouchMove(x, y) },
                onTouchUp = { viewModel.onTouchUp() },
                onSpawnRequest = { x, y -> viewModel.onSpawnRequest(x, y) }
            )
        }

        // Floating Top Glassmorphic HUD
        TopHudOverlay(
            fps = fps,
            particleCount = particleCount,
            speciesCount = numSpecies,
            onOpenMatrix = { showMatrixSheet = true },
            onOpenPresets = { showPresetsSheet = true },
            onOpenSettings = { showSettingsSheet = true },
            onOpenSupport = { showSupportDialog = true },
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // Floating Bottom Controls & Sliders Drawer
        BottomControlsDrawer(
            isPlaying = isPlaying,
            onTogglePlay = { viewModel.togglePlay() },
            onStep = { viewModel.stepOnce() },
            onReset = { viewModel.resetParticles() },
            timeStep = timeStep,
            onTimeStepChange = { viewModel.setTimeStep(it) },
            friction = friction,
            onFrictionChange = { viewModel.setFriction(it) },
            forceStrength = forceStrength,
            onForceStrengthChange = { viewModel.setForceStrength(it) },
            forceDistance = forceDistance,
            onForceDistanceChange = { viewModel.setForceDistance(it) },
            boundaryMode = boundaryMode,
            onBoundaryModeToggle = { viewModel.toggleBoundaryMode() },
            touchMode = touchMode,
            onCycleTouchMode = { viewModel.cycleTouchMode() },
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // Modals & Bottom Sheets
        if (showMatrixSheet) {
            MatrixEditorSheet(
                speciesList = speciesList,
                matrix = viewModel.simulation.matrix,
                numSpecies = numSpecies,
                particleCount = particleCount,
                onSpeciesCountChange = { viewModel.setSpeciesCount(it) },
                onParticleCountChange = { viewModel.setParticleCount(it) },
                onMatrixCellChange = { row, col, value -> viewModel.setMatrixCell(row, col, value) },
                onRandomizeMatrix = { mode -> viewModel.randomizeMatrix(mode) },
                onSpeciesUpdated = { idx, name, color -> viewModel.updateSpecies(idx, name, color) },
                onDismiss = { showMatrixSheet = false }
            )
        }

        if (showPresetsSheet) {
            PresetsSheet(
                presets = presets,
                onLoadPreset = { preset -> viewModel.loadPreset(preset) },
                onSaveCurrentPreset = { name -> viewModel.saveCurrentPreset(name) },
                onDeletePreset = { id -> viewModel.deletePreset(id) },
                onDismiss = { showPresetsSheet = false }
            )
        }

        if (showSettingsSheet) {
            SettingsSheet(
                backgroundColor = backgroundColor,
                onBackgroundColorChange = { viewModel.setBackgroundColor(it) },
                particleRadius = particleRadius,
                onParticleRadiusChange = { viewModel.setParticleRadius(it) },
                particleStyle = particleStyle,
                onParticleStyleChange = { viewModel.setParticleStyle(it) },
                onOpenSupportMe = {
                    showSettingsSheet = false
                    showSupportDialog = true
                },
                onDismiss = { showSettingsSheet = false }
            )
        }

        if (showSupportDialog) {
            SupportMeDialog(
                onDismiss = { showSupportDialog = false }
            )
        }
    }
}

@Composable
private inline fun keyRender(tick: Long, version: Int, content: @Composable () -> Unit) {
    androidx.compose.runtime.key(tick, version) {
        content()
    }
}
