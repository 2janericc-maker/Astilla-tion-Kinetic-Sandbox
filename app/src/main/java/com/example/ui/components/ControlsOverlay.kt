package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.physics.BoundaryMode
import com.example.physics.TouchMode
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonPink

@Composable
fun TopHudOverlay(
    fps: Int,
    particleCount: Int,
    speciesCount: Int,
    onOpenMatrix: () -> Unit,
    onOpenPresets: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenSupport: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .testTag("top_hud_overlay"),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Title and real-time stats
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Astilla-tion",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = NeonCyan
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Sandbox",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // FPS badge
                    Text(
                        text = "$fps FPS",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = if (fps >= 50) NeonEmerald else Color(0xFFFFD600)
                    )
                    Text(
                        text = " • $particleCount specks • $speciesCount types",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }

            // Quick Menu Actions
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onOpenMatrix,
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("btn_hud_matrix")
                ) {
                    Icon(
                        imageVector = Icons.Default.GridView,
                        contentDescription = "Force Matrix",
                        tint = NeonCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onOpenPresets,
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("btn_hud_presets")
                ) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = "Presets",
                        tint = Color(0xFFFFD600),
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("btn_hud_settings")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onOpenSupport,
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("btn_hud_support")
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Support Astilla",
                        tint = NeonPink,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun BottomControlsDrawer(
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    onStep: () -> Unit,
    onReset: () -> Unit,
    timeStep: Float,
    onTimeStepChange: (Float) -> Unit,
    friction: Float,
    onFrictionChange: (Float) -> Unit,
    forceStrength: Float,
    onForceStrengthChange: (Float) -> Unit,
    forceDistance: Float,
    onForceDistanceChange: (Float) -> Unit,
    boundaryMode: BoundaryMode,
    onBoundaryModeToggle: () -> Unit,
    touchMode: TouchMode,
    onCycleTouchMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .testTag("bottom_controls_drawer"),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            // Main Action Row (Always Visible)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Play / Pause Button
                IconButton(
                    onClick = onTogglePlay,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (isPlaying) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer)
                        .testTag("btn_toggle_play")
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = if (isPlaying) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Step Forward Button
                IconButton(
                    onClick = onStep,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .testTag("btn_step_forward")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Single step",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Reset / Respawn Button
                IconButton(
                    onClick = onReset,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .testTag("btn_reset_simulation")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Respawn particles",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Touch Mode Toggle Chip
                val touchColor = when (touchMode) {
                    TouchMode.ATTRACT -> NeonCyan
                    TouchMode.REPEL -> NeonPink
                    TouchMode.SPAWN -> NeonEmerald
                    TouchMode.NONE -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(touchColor.copy(alpha = 0.16f))
                        .border(1.dp, touchColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .clickable(onClick = onCycleTouchMode)
                        .padding(horizontal = 10.dp, vertical = 7.dp)
                        .testTag("btn_cycle_touch_mode"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.TouchApp,
                            contentDescription = null,
                            tint = touchColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = touchMode.label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = touchColor
                        )
                    }
                }

                // Expand/Collapse Drawer button
                IconButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("btn_expand_sliders")
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                        contentDescription = if (isExpanded) "Hide sliders" else "Show sliders",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Collapsible Sliders Drawer
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    // Time Step Speed Slider
                    PhysicsSliderRow(
                        label = "Simulation Speed",
                        valueText = "${String.format(java.util.Locale.US, "%.1f", timeStep)}x",
                        value = timeStep,
                        onValueChange = onTimeStepChange,
                        range = 0.2f..3.0f,
                        testTag = "slider_timestep"
                    )

                    // Friction / Damping Slider
                    PhysicsSliderRow(
                        label = "Friction / Damping",
                        valueText = String.format(java.util.Locale.US, "%.2f", friction),
                        value = friction,
                        onValueChange = onFrictionChange,
                        range = 0.60f..0.98f,
                        testTag = "slider_friction"
                    )

                    // Force Strength Slider
                    PhysicsSliderRow(
                        label = "Force Intensity",
                        valueText = "${String.format(java.util.Locale.US, "%.1f", forceStrength)}x",
                        value = forceStrength,
                        onValueChange = onForceStrengthChange,
                        range = 0.2f..3.0f,
                        testTag = "slider_force_strength"
                    )

                    // Force Interaction Radius Slider
                    PhysicsSliderRow(
                        label = "Force Distance",
                        valueText = "${forceDistance.toInt()} px",
                        value = forceDistance,
                        onValueChange = onForceDistanceChange,
                        range = 40f..220f,
                        testTag = "slider_force_distance"
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Boundary Mode Toggle (WRAP vs BOUNCE)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Boundary Behavior",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (boundaryMode == BoundaryMode.WRAP) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable { if (boundaryMode != BoundaryMode.WRAP) onBoundaryModeToggle() }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                    .testTag("btn_boundary_wrap")
                            ) {
                                Text(
                                    text = "Toroidal Wrap",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (boundaryMode == BoundaryMode.WRAP) FontWeight.Bold else FontWeight.Normal,
                                    color = if (boundaryMode == BoundaryMode.WRAP) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (boundaryMode == BoundaryMode.BOUNCE) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable { if (boundaryMode != BoundaryMode.BOUNCE) onBoundaryModeToggle() }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                    .testTag("btn_boundary_bounce")
                            ) {
                                Text(
                                    text = "Bouncy Walls",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (boundaryMode == BoundaryMode.BOUNCE) FontWeight.Bold else FontWeight.Normal,
                                    color = if (boundaryMode == BoundaryMode.BOUNCE) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PhysicsSliderRow(
    label: String,
    valueText: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    range: ClosedFloatingPointRange<Float>,
    testTag: String
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = valueText,
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier
                .height(28.dp)
                .testTag(testTag)
        )
    }
}
