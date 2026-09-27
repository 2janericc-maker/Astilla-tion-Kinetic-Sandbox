package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.window.Dialog
import com.example.physics.ColorUtils
import com.example.physics.ParticleSimulation
import com.example.physics.Species
import com.example.ui.theme.NegativeRed
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonPink
import com.example.ui.theme.PositiveGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatrixEditorSheet(
    speciesList: List<Species>,
    matrix: Array<FloatArray>,
    numSpecies: Int,
    particleCount: Int,
    onSpeciesCountChange: (Int) -> Unit,
    onParticleCountChange: (Int) -> Unit,
    onMatrixCellChange: (row: Int, col: Int, value: Float) -> Unit,
    onRandomizeMatrix: (ParticleSimulation.RandomizeMode) -> Unit,
    onSpeciesUpdated: (index: Int, name: String, color: Color) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Matrix Grid, 1: Species Customizer
    var selectedCell by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var colorPickerSpeciesIndex by remember { mutableStateOf<Int?>(null) }
    var showRandomizeMenu by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        scrimColor = Color.Black.copy(alpha = 0.6f),
        modifier = Modifier.testTag("sheet_matrix_editor")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Species & Force Matrix",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Define pairwise attraction (+) & repulsion (-)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box {
                        Button(
                            onClick = { showRandomizeMenu = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("btn_randomize_matrix")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shuffle,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Randomize", style = MaterialTheme.typography.labelMedium)
                        }

                        DropdownMenu(
                            expanded = showRandomizeMenu,
                            onDismissRequest = { showRandomizeMenu = false }
                        ) {
                            ParticleSimulation.RandomizeMode.values().forEach { mode ->
                                DropdownMenuItem(
                                    text = { Text(mode.label) },
                                    onClick = {
                                        onRandomizeMatrix(mode)
                                        showRandomizeMenu = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("btn_close_matrix_sheet")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Navigation Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp)
            ) {
                TabButton(
                    title = "Force Matrix",
                    isSelected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    modifier = Modifier.weight(1f)
                )
                TabButton(
                    title = "Species Customizer",
                    isSelected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Content Body based on tab
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                if (selectedTab == 0) {
                    MatrixGridTab(
                        speciesList = speciesList.take(numSpecies),
                        matrix = matrix,
                        numSpecies = numSpecies,
                        particleCount = particleCount,
                        onSpeciesCountChange = onSpeciesCountChange,
                        onParticleCountChange = onParticleCountChange,
                        onCellClick = { row, col -> selectedCell = Pair(row, col) }
                    )
                } else {
                    SpeciesCustomizerTab(
                        speciesList = speciesList.take(numSpecies),
                        onEditColor = { idx -> colorPickerSpeciesIndex = idx },
                        onUpdateName = { idx, name ->
                            val s = speciesList[idx]
                            onSpeciesUpdated(idx, name, s.color)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Cell Force Adjuster Dialog
    selectedCell?.let { (row, col) ->
        if (row < numSpecies && col < numSpecies) {
            val sRow = speciesList[row]
            val sCol = speciesList[col]
            var currentVal by remember(row, col) { mutableFloatStateOf(matrix[row][col]) }

            Dialog(onDismissRequest = { selectedCell = null }) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Interaction Rule",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(sRow.color)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = sRow.name,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "→",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(sCol.color)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = sCol.name,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Force Value Display
                        val forceColor = when {
                            currentVal > 0.05f -> PositiveGreen
                            currentVal < -0.05f -> NegativeRed
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                        val forceLabel = when {
                            currentVal > 0.05f -> "Attraction (+${String.format(java.util.Locale.US, "%.2f", currentVal)})"
                            currentVal < -0.05f -> "Repulsion (${String.format(java.util.Locale.US, "%.2f", currentVal)})"
                            else -> "Neutral (0.00)"
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(forceColor.copy(alpha = 0.15f))
                                .border(1.dp, forceColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = forceLabel,
                                color = forceColor,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Slider(
                            value = currentVal,
                            onValueChange = {
                                currentVal = it
                                onMatrixCellChange(row, col, it)
                            },
                            valueRange = -1.0f..1.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = forceColor,
                                activeTrackColor = forceColor,
                                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.testTag("slider_cell_force")
                        )

                        // Quick buttons: Max Repel, Neutral, Max Attract
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OutlinedButton(
                                onClick = {
                                    currentVal = -1.0f
                                    onMatrixCellChange(row, col, -1.0f)
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("-1.0", color = NegativeRed, fontSize = 12.sp)
                            }
                            OutlinedButton(
                                onClick = {
                                    currentVal = 0.0f
                                    onMatrixCellChange(row, col, 0.0f)
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("0.0", fontSize = 12.sp)
                            }
                            OutlinedButton(
                                onClick = {
                                    currentVal = 1.0f
                                    onMatrixCellChange(row, col, 1.0f)
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("+1.0", color = PositiveGreen, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { selectedCell = null },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Done")
                        }
                    }
                }
            }
        }
    }

    // Color Picker Dialog for Species
    colorPickerSpeciesIndex?.let { idx ->
        if (idx < speciesList.size) {
            val s = speciesList[idx]
            var tempColor by remember { mutableStateOf(s.color) }

            Dialog(onDismissRequest = { colorPickerSpeciesIndex = null }) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Species Color: ${s.name}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        FullSpectrumColorPicker(
                            initialColor = tempColor,
                            onColorChanged = { tempColor = it },
                            simpleSwatches = ColorUtils.SWATCH_COLORS,
                            title = "Species Hue"
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { colorPickerSpeciesIndex = null }) {
                                Text("Cancel")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    onSpeciesUpdated(idx, s.name, tempColor)
                                    colorPickerSpeciesIndex = null
                                }
                            ) {
                                Text("Apply")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TabButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary
                else Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MatrixGridTab(
    speciesList: List<Species>,
    matrix: Array<FloatArray>,
    numSpecies: Int,
    particleCount: Int,
    onSpeciesCountChange: (Int) -> Unit,
    onParticleCountChange: (Int) -> Unit,
    onCellClick: (Int, Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Controls: Species Count & Particle Count
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Species count selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Active Species",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$numSpecies Types",
                        style = MaterialTheme.typography.labelMedium,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        (2..7).forEach { count ->
                            val isSelected = count == numSpecies
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surface
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { onSpeciesCountChange(count) }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$count",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        (8..12).forEach { count ->
                            val isSelected = count == numSpecies
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surface
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { onSpeciesCountChange(count) }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$count",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Particle count slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Active Particles",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$particleCount particles",
                        style = MaterialTheme.typography.labelMedium,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Slider(
                    value = particleCount.toFloat(),
                    onValueChange = { onParticleCountChange(it.toInt()) },
                    valueRange = 100f..2000f,
                    steps = 18,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.testTag("slider_particle_count")
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Matrix Grid Table
        Text(
            text = "Interaction Matrix Grid (Tap cell to tune)",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                .padding(8.dp)
                .horizontalScroll(rememberScrollState())
        ) {
            Column {
                // Column Headers (Target Species)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Empty top-left cell
                    Box(modifier = Modifier.size(54.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "From\\To",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    for (j in 0 until numSpecies) {
                        val colSpecies = speciesList[j]
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(colSpecies.color)
                                )
                                Text(
                                    text = colSpecies.name.take(4),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                // Rows (Source Species)
                for (i in 0 until numSpecies) {
                    val rowSpecies = speciesList[i]
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Row header
                        Box(
                            modifier = Modifier
                                .width(54.dp)
                                .height(46.dp)
                                .padding(2.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(rowSpecies.color)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = rowSpecies.name.take(4),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                            }
                        }

                        // Matrix Cells
                        for (j in 0 until numSpecies) {
                            val value = matrix[i][j]
                            val cellColor = when {
                                value > 0.05f -> PositiveGreen.copy(alpha = 0.25f + value * 0.45f)
                                value < -0.05f -> NegativeRed.copy(alpha = 0.25f + (-value) * 0.45f)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                            val textColor = when {
                                value > 0.05f -> PositiveGreen
                                value < -0.05f -> NegativeRed
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }

                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .padding(2.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(cellColor)
                                    .border(
                                        1.dp,
                                        if (i == j) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { onCellClick(i, j) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = String.format(java.util.Locale.US, "%+.1f", value),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(PositiveGreen))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Attraction (+)", style = MaterialTheme.typography.labelSmall, color = PositiveGreen)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(NegativeRed))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Repulsion (-)", style = MaterialTheme.typography.labelSmall, color = NegativeRed)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(MaterialTheme.colorScheme.outline))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Diagonal: Self-interaction", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun SpeciesCustomizerTab(
    speciesList: List<Species>,
    onEditColor: (Int) -> Unit,
    onUpdateName: (Int, String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        speciesList.forEachIndexed { index, species ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Color button
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(species.color)
                            .border(2.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                            .clickable { onEditColor(index) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Edit color",
                            tint = if (species.color.red + species.color.green + species.color.blue > 1.5f) Color.Black else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Editable Name TextField
                    var nameInput by remember(species.name) { mutableStateOf(species.name) }
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = {
                            nameInput = it
                            onUpdateName(index, it)
                        },
                        label = { Text("Species ${index + 1}") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_species_name_$index")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { onEditColor(index) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Tune color",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
