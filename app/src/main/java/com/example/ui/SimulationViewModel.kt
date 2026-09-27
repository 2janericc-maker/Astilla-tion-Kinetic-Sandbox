package com.example.ui

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.DefaultPresets
import com.example.data.PresetEntity
import com.example.data.PresetRepository
import com.example.physics.BoundaryMode
import com.example.physics.ColorUtils
import com.example.physics.ParticleSimulation
import com.example.physics.ParticleStyle
import com.example.physics.Species
import com.example.physics.TouchMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.max

class SimulationViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PresetRepository
    val simulation = ParticleSimulation(1000f, 1000f, maxParticles = 2000)

    val allPresets: StateFlow<List<PresetEntity>>

    // UI States
    private val _isPlaying = MutableStateFlow(true)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _timeStep = MutableStateFlow(1.0f)
    val timeStep: StateFlow<Float> = _timeStep.asStateFlow()

    private val _friction = MutableStateFlow(simulation.friction)
    val friction: StateFlow<Float> = _friction.asStateFlow()

    private val _forceStrength = MutableStateFlow(simulation.forceStrength)
    val forceStrength: StateFlow<Float> = _forceStrength.asStateFlow()

    private val _forceDistance = MutableStateFlow(simulation.forceDistance)
    val forceDistance: StateFlow<Float> = _forceDistance.asStateFlow()

    private val _boundaryMode = MutableStateFlow(simulation.boundaryMode)
    val boundaryMode: StateFlow<BoundaryMode> = _boundaryMode.asStateFlow()

    private val _touchMode = MutableStateFlow(TouchMode.ATTRACT)
    val touchMode: StateFlow<TouchMode> = _touchMode.asStateFlow()

    private val _particleCount = MutableStateFlow(simulation.count)
    val particleCount: StateFlow<Int> = _particleCount.asStateFlow()

    private val _numSpecies = MutableStateFlow(simulation.numSpecies)
    val numSpecies: StateFlow<Int> = _numSpecies.asStateFlow()

    private val _speciesList = MutableStateFlow(createDefaultSpeciesList())
    val speciesList: StateFlow<List<Species>> = _speciesList.asStateFlow()

    private val _backgroundColor = MutableStateFlow(Color(0xFF0A0D14))
    val backgroundColor: StateFlow<Color> = _backgroundColor.asStateFlow()

    private val _particleRadius = MutableStateFlow(4.0f)
    val particleRadius: StateFlow<Float> = _particleRadius.asStateFlow()

    private val _particleStyle = MutableStateFlow(ParticleStyle.DISC)
    val particleStyle: StateFlow<ParticleStyle> = _particleStyle.asStateFlow()

    private val _fps = MutableStateFlow(60)
    val fps: StateFlow<Int> = _fps.asStateFlow()

    // Matrix state holder for Compose UI reactivity
    private val _matrixVersion = MutableStateFlow(0)
    val matrixVersion: StateFlow<Int> = _matrixVersion.asStateFlow()

    // Animation frame tick for Canvas redraw
    private val _renderTick = MutableStateFlow(0L)
    val renderTick: StateFlow<Long> = _renderTick.asStateFlow()

    init {
        val database = AppDatabase.getInstance(application)
        repository = PresetRepository(database.presetDao())
        allPresets = repository.allPresets.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        viewModelScope.launch {
            repository.ensureBuiltInPresetsSeeded()
            // Load first preset by default
            loadPresetByName("Primordial Soup")
        }

        // Start Physics Simulation Loop
        startPhysicsLoop()
    }

    private fun createDefaultSpeciesList(): List<Species> {
        return List(ParticleSimulation.MAX_SPECIES) { i ->
            Species(
                id = i,
                name = ColorUtils.DEFAULT_SPECIES_NAMES[i],
                color = ColorUtils.DEFAULT_SPECIES_COLORS[i]
            )
        }
    }

    private fun startPhysicsLoop() {
        viewModelScope.launch(Dispatchers.Default) {
            var lastTime = System.nanoTime()
            var frameCount = 0
            var lastFpsTime = System.currentTimeMillis()

            while (isActive) {
                val now = System.nanoTime()
                val deltaMillis = (now - lastTime) / 1_000_000L
                lastTime = now

                if (_isPlaying.value) {
                    val stepMultiplier = _timeStep.value
                    simulation.tick(stepMultiplier)
                    _renderTick.value = System.nanoTime()
                }

                frameCount++
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastFpsTime >= 1000) {
                    _fps.value = frameCount
                    frameCount = 0
                    lastFpsTime = currentTime
                }

                // Target ~60 FPS (16ms)
                val elapsed = (System.nanoTime() - now) / 1_000_000L
                val sleepTime = max(2L, 16L - elapsed)
                delay(sleepTime)
            }
        }
    }

    fun togglePlay() {
        _isPlaying.value = !_isPlaying.value
    }

    fun stepOnce() {
        simulation.tick(_timeStep.value)
        _renderTick.value = System.nanoTime()
    }

    fun resetParticles() {
        simulation.spawnRandomParticles(_particleCount.value, _numSpecies.value)
        _renderTick.value = System.nanoTime()
    }

    fun setTimeStep(value: Float) {
        _timeStep.value = value
    }

    fun setFriction(value: Float) {
        _friction.value = value
        simulation.friction = value
    }

    fun setForceStrength(value: Float) {
        _forceStrength.value = value
        simulation.forceStrength = value
    }

    fun setForceDistance(value: Float) {
        _forceDistance.value = value
        simulation.forceDistance = value
    }

    fun toggleBoundaryMode() {
        val next = if (_boundaryMode.value == BoundaryMode.WRAP) BoundaryMode.BOUNCE else BoundaryMode.WRAP
        _boundaryMode.value = next
        simulation.boundaryMode = next
    }

    fun cycleTouchMode() {
        val modes = TouchMode.values()
        val nextIdx = (modes.indexOf(_touchMode.value) + 1) % modes.size
        val next = modes[nextIdx]
        _touchMode.value = next
        simulation.touchMode = next
    }

    fun setSpeciesCount(count: Int) {
        val clamped = count.coerceIn(2, ParticleSimulation.MAX_SPECIES)
        _numSpecies.value = clamped
        simulation.setSpeciesCount(clamped)
        _matrixVersion.value++
    }

    fun setParticleCount(count: Int) {
        val clamped = count.coerceIn(100, 2000)
        _particleCount.value = clamped
        simulation.setParticleCount(clamped)
    }

    fun setMatrixCell(row: Int, col: Int, value: Float) {
        simulation.setMatrixValue(row, col, value)
        _matrixVersion.value++
    }

    fun randomizeMatrix(mode: ParticleSimulation.RandomizeMode) {
        simulation.randomizeMatrix(mode)
        _matrixVersion.value++
    }

    fun updateSpecies(index: Int, name: String, color: Color) {
        if (index in 0 until ParticleSimulation.MAX_SPECIES) {
            val updated = _speciesList.value.toMutableList()
            updated[index] = updated[index].copy(name = name, color = color)
            _speciesList.value = updated
        }
    }

    fun setBackgroundColor(color: Color) {
        _backgroundColor.value = color
    }

    fun setParticleRadius(radius: Float) {
        _particleRadius.value = radius
    }

    fun setParticleStyle(style: ParticleStyle) {
        _particleStyle.value = style
    }

    // Touch events
    fun onTouchDown(x: Float, y: Float) {
        simulation.isTouched = true
        simulation.touchX = x
        simulation.touchY = y
    }

    fun onTouchMove(x: Float, y: Float) {
        simulation.touchX = x
        simulation.touchY = y
    }

    fun onTouchUp() {
        simulation.isTouched = false
    }

    fun onSpawnRequest(x: Float, y: Float) {
        val randomSpecies = (0 until _numSpecies.value).random()
        simulation.spawnAtTouch(x, y, randomSpecies)
        _particleCount.value = simulation.count
    }

    // Presets
    fun loadPreset(preset: PresetEntity) {
        val parsedMatrix = DefaultPresets.parseMatrix(preset.matrixJson, preset.speciesCount)
        for (i in 0 until preset.speciesCount) {
            for (j in 0 until preset.speciesCount) {
                simulation.setMatrixValue(i, j, parsedMatrix[i][j])
            }
        }

        setSpeciesCount(preset.speciesCount)
        setParticleCount(preset.particleCount)
        setFriction(preset.friction)
        setForceStrength(preset.forceStrength)
        setForceDistance(preset.forceDistance)

        val names = DefaultPresets.parseNames(preset.speciesNames, preset.speciesCount)
        val colors = DefaultPresets.parseColors(preset.speciesColorsHex, preset.speciesCount)

        val updatedSpecies = _speciesList.value.toMutableList()
        for (i in 0 until ParticleSimulation.MAX_SPECIES) {
            if (i < preset.speciesCount) {
                updatedSpecies[i] = Species(
                    id = i,
                    name = names[i],
                    color = ColorUtils.fromHex(colors[i])
                )
            }
        }
        _speciesList.value = updatedSpecies

        _backgroundColor.value = ColorUtils.fromHex(preset.backgroundColorHex, Color(0xFF0A0D14))

        val bound = if (preset.boundaryMode == "BOUNCE") BoundaryMode.BOUNCE else BoundaryMode.WRAP
        _boundaryMode.value = bound
        simulation.boundaryMode = bound

        simulation.spawnRandomParticles(preset.particleCount, preset.speciesCount)
        _matrixVersion.value++
    }

    fun loadPresetByName(name: String) {
        val found = DefaultPresets.BUILT_IN_PRESETS.find { it.name == name }
        if (found != null) {
            loadPreset(found.toEntity())
        }
    }

    fun saveCurrentPreset(name: String) {
        viewModelScope.launch {
            val currentSpecies = _speciesList.value
            val nSpecies = _numSpecies.value

            val matrixJson = (0 until nSpecies).joinToString(";") { i ->
                (0 until nSpecies).joinToString(",") { j ->
                    String.format(java.util.Locale.US, "%.3f", simulation.matrix[i][j])
                }
            }

            val speciesColorsHex = currentSpecies.take(nSpecies).joinToString(",") { ColorUtils.toHex(it.color) }
            val speciesNames = currentSpecies.take(nSpecies).joinToString(",") { it.name }

            val entity = PresetEntity(
                name = name,
                isBuiltIn = false,
                speciesCount = nSpecies,
                particleCount = _particleCount.value,
                friction = _friction.value,
                forceStrength = _forceStrength.value,
                forceDistance = _forceDistance.value,
                matrixJson = matrixJson,
                speciesColorsHex = speciesColorsHex,
                speciesNames = speciesNames,
                backgroundColorHex = ColorUtils.toHex(_backgroundColor.value),
                boundaryMode = _boundaryMode.value.name
            )

            repository.saveUserPreset(entity)
        }
    }

    fun deletePreset(id: Long) {
        viewModelScope.launch {
            repository.deletePreset(id)
        }
    }
}
