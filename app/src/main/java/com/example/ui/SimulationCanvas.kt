package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import com.example.physics.BoundaryMode
import com.example.physics.ParticleSimulation
import com.example.physics.ParticleStyle
import com.example.physics.Species
import com.example.physics.TouchMode
import kotlin.math.sqrt

@Composable
fun SimulationCanvas(
    simulation: ParticleSimulation,
    speciesList: List<Species>,
    particleRadius: Float,
    particleStyle: ParticleStyle,
    backgroundColor: Color,
    touchMode: TouchMode,
    onTouchDown: (Float, Float) -> Unit,
    onTouchMove: (Float, Float) -> Unit,
    onTouchUp: () -> Unit,
    onSpawnRequest: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .testTag("canvas_simulation")
            .pointerInput(touchMode) {
                detectTapGestures(
                    onPress = { offset ->
                        onTouchDown(offset.x, offset.y)
                        if (touchMode == TouchMode.SPAWN) {
                            onSpawnRequest(offset.x, offset.y)
                        }
                        tryAwaitRelease()
                        onTouchUp()
                    }
                )
            }
            .pointerInput(touchMode) {
                detectDragGestures(
                    onDragStart = { offset ->
                        onTouchDown(offset.x, offset.y)
                        if (touchMode == TouchMode.SPAWN) {
                            onSpawnRequest(offset.x, offset.y)
                        }
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        onTouchMove(change.position.x, change.position.y)
                        if (touchMode == TouchMode.SPAWN) {
                            onSpawnRequest(change.position.x, change.position.y)
                        }
                    },
                    onDragEnd = { onTouchUp() },
                    onDragCancel = { onTouchUp() }
                )
            }
    ) {
        val w = size.width
        val h = size.height
        simulation.updateDimensions(w, h)

        // Draw boundary box if BOUNCE mode
        if (simulation.boundaryMode == BoundaryMode.BOUNCE) {
            drawRect(
                color = Color.White.copy(alpha = 0.2f),
                size = size,
                style = Stroke(width = 2.5f)
            )
        }

        val count = simulation.count
        val xs = simulation.x
        val ys = simulation.y
        val vxs = simulation.vx
        val vys = simulation.vy
        val sps = simulation.species
        val speciesCount = speciesList.size
        val radius = particleRadius

        when (particleStyle) {
            ParticleStyle.DISC -> {
                for (i in 0 until count) {
                    val sIdx = sps[i]
                    val color = if (sIdx < speciesCount) speciesList[sIdx].color else Color.White
                    drawCircle(
                        color = color,
                        radius = radius,
                        center = Offset(xs[i], ys[i])
                    )
                }
            }
            ParticleStyle.SPLINTER -> {
                // Render as kinetic velocity splinters
                for (i in 0 until count) {
                    val sIdx = sps[i]
                    val color = if (sIdx < speciesCount) speciesList[sIdx].color else Color.White
                    val px = xs[i]
                    val py = ys[i]
                    val vx = vxs[i]
                    val vy = vys[i]
                    val speed = sqrt(vx * vx + vy * vy)
                    val streakLen = (speed * 0.08f).coerceIn(radius * 0.8f, radius * 3.5f)
                    val dirX = if (speed > 0.001f) (vx / speed) else 1f
                    val dirY = if (speed > 0.001f) (vy / speed) else 0f

                    drawLine(
                        color = color,
                        start = Offset(px - dirX * streakLen, py - dirY * streakLen),
                        end = Offset(px + dirX * streakLen, py + dirY * streakLen),
                        strokeWidth = radius * 1.5f,
                        cap = StrokeCap.Round
                    )
                }
            }
            ParticleStyle.GLOW -> {
                for (i in 0 until count) {
                    val sIdx = sps[i]
                    val color = if (sIdx < speciesCount) speciesList[sIdx].color else Color.White
                    val center = Offset(xs[i], ys[i])
                    // Subtle outer glow halo
                    drawCircle(
                        color = color.copy(alpha = 0.25f),
                        radius = radius * 2.2f,
                        center = center
                    )
                    // Bright core
                    drawCircle(
                        color = color,
                        radius = radius,
                        center = center
                    )
                }
            }
        }

        // Draw Touch Reticle if user is touching
        if (simulation.isTouched && touchMode != TouchMode.NONE) {
            val touchOffset = Offset(simulation.touchX, simulation.touchY)
            val reticleColor = when (touchMode) {
                TouchMode.ATTRACT -> Color(0xFF00E5FF)
                TouchMode.REPEL -> Color(0xFFFF3366)
                TouchMode.SPAWN -> Color(0xFF05FFA1)
                TouchMode.NONE -> Color.Transparent
            }

            drawCircle(
                color = reticleColor.copy(alpha = 0.15f),
                radius = 120f,
                center = touchOffset
            )
            drawCircle(
                color = reticleColor.copy(alpha = 0.8f),
                radius = 28f,
                center = touchOffset,
                style = Stroke(width = 2.5f)
            )
            drawCircle(
                color = reticleColor,
                radius = 5f,
                center = touchOffset
            )
        }
    }
}
