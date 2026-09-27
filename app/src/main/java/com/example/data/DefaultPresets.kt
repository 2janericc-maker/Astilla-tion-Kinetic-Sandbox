package com.example.data

import com.example.physics.ColorUtils

data class PresetDefinition(
    val name: String,
    val description: String,
    val speciesCount: Int,
    val particleCount: Int,
    val friction: Float,
    val forceStrength: Float,
    val forceDistance: Float,
    val matrix: Array<FloatArray>,
    val speciesNames: List<String>,
    val speciesColors: List<String>,
    val backgroundColorHex: String = "#0A0D14",
    val boundaryMode: String = "WRAP"
) {
    fun toEntity(id: Long = 0, isBuiltIn: Boolean = true): PresetEntity {
        val matrixJson = matrix.joinToString(";") { row ->
            row.joinToString(",") { String.format(java.util.Locale.US, "%.3f", it) }
        }
        val colorsJson = speciesColors.take(speciesCount).joinToString(",")
        val namesJson = speciesNames.take(speciesCount).joinToString(",")
        return PresetEntity(
            id = id,
            name = name,
            isBuiltIn = isBuiltIn,
            speciesCount = speciesCount,
            particleCount = particleCount,
            friction = friction,
            forceStrength = forceStrength,
            forceDistance = forceDistance,
            matrixJson = matrixJson,
            speciesColorsHex = colorsJson,
            speciesNames = namesJson,
            backgroundColorHex = backgroundColorHex,
            boundaryMode = boundaryMode
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PresetDefinition
        return name == other.name
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}

object DefaultPresets {

    fun parseMatrix(json: String, speciesCount: Int): Array<FloatArray> {
        val result = Array(speciesCount) { FloatArray(speciesCount) }
        try {
            val rows = json.split(";")
            for (i in 0 until minOf(speciesCount, rows.size)) {
                val cols = rows[i].split(",")
                for (j in 0 until minOf(speciesCount, cols.size)) {
                    result[i][j] = cols[j].trim().toFloatOrNull() ?: 0f
                }
            }
        } catch (_: Exception) {}
        return result
    }

    fun parseColors(json: String, speciesCount: Int): List<String> {
        val list = json.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val defaults = ColorUtils.DEFAULT_SPECIES_COLORS.map { ColorUtils.toHex(it) }
        return List(speciesCount) { i ->
            if (i < list.size) list[i] else defaults[i % defaults.size]
        }
    }

    fun parseNames(json: String, speciesCount: Int): List<String> {
        val list = json.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val defaults = ColorUtils.DEFAULT_SPECIES_NAMES
        return List(speciesCount) { i ->
            if (i < list.size) list[i] else defaults[i % defaults.size]
        }
    }

    val BUILT_IN_PRESETS = listOf(
        PresetDefinition(
            name = "Primordial Soup",
            description = "Multi-species symbiotic cellular membranes, self-assembling organelle clusters, and division.",
            speciesCount = 4,
            particleCount = 800,
            friction = 0.90f,
            forceStrength = 1.0f,
            forceDistance = 110f,
            matrix = arrayOf(
                floatArrayOf(0.40f, -0.65f, 0.35f, 0.10f),
                floatArrayOf(0.20f, 0.50f, -0.70f, 0.25f),
                floatArrayOf(-0.50f, 0.40f, 0.30f, -0.20f),
                floatArrayOf(0.15f, -0.20f, 0.50f, 0.45f)
            ),
            speciesNames = listOf("Membrane", "Core Spores", "Organelles", "Plasmids"),
            speciesColors = listOf("#00E5FF", "#FF2A6D", "#05FFA1", "#FFE600"),
            backgroundColorHex = "#0A0D14",
            boundaryMode = "WRAP"
        ),
        PresetDefinition(
            name = "Cellular Drift",
            description = "Worm-like gliding colonies, crawling filament networks, and organic self-propelling cells.",
            speciesCount = 3,
            particleCount = 750,
            friction = 0.88f,
            forceStrength = 1.2f,
            forceDistance = 120f,
            matrix = arrayOf(
                floatArrayOf(0.60f, -0.40f, 0.20f),
                floatArrayOf(0.45f, 0.55f, -0.60f),
                floatArrayOf(-0.50f, 0.30f, 0.40f)
            ),
            speciesNames = listOf("Locomotion", "Endoskeleton", "Metabolites"),
            speciesColors = listOf("#05FFA1", "#00E5FF", "#B537F2"),
            backgroundColorHex = "#0B0D1B",
            boundaryMode = "WRAP"
        ),
        PresetDefinition(
            name = "Chaos Matrix",
            description = "High-energy interstellar turbulence, galaxy spirals, vortex rings, and chaotic mixing.",
            speciesCount = 5,
            particleCount = 1000,
            friction = 0.94f,
            forceStrength = 1.4f,
            forceDistance = 130f,
            matrix = arrayOf(
                floatArrayOf(0.10f, 0.80f, -0.90f, 0.40f, -0.30f),
                floatArrayOf(-0.70f, 0.15f, 0.85f, -0.40f, 0.50f),
                floatArrayOf(0.60f, -0.80f, 0.10f, 0.70f, -0.40f),
                floatArrayOf(-0.30f, 0.50f, -0.75f, 0.20f, 0.80f),
                floatArrayOf(0.85f, -0.40f, 0.30f, -0.60f, 0.10f)
            ),
            speciesNames = listOf("Vortex Alpha", "Stellar Flux", "Void Motes", "Solar Flare", "Dark Matter"),
            speciesColors = listOf("#00E5FF", "#FFE600", "#FF2A6D", "#FF7700", "#B537F2"),
            backgroundColorHex = "#000000",
            boundaryMode = "WRAP"
        ),
        PresetDefinition(
            name = "Atomic Crystals",
            description = "Rigid crystalline lattices, hexagonal atomic packing, and vibrating molecular structures.",
            speciesCount = 4,
            particleCount = 650,
            friction = 0.85f,
            forceStrength = 1.6f,
            forceDistance = 90f,
            matrix = arrayOf(
                floatArrayOf(0.70f, -0.50f, -0.30f, 0.10f),
                floatArrayOf(-0.50f, 0.70f, 0.20f, -0.40f),
                floatArrayOf(-0.30f, 0.20f, 0.80f, -0.60f),
                floatArrayOf(0.10f, -0.40f, -0.60f, 0.75f)
            ),
            speciesNames = listOf("Carbon Lattice", "Hydride Ions", "Cation Rings", "Valence Cloud"),
            speciesColors = listOf("#00E5FF", "#05FFA1", "#FFE600", "#FFFFFF"),
            backgroundColorHex = "#141824",
            boundaryMode = "BOUNCE"
        ),
        PresetDefinition(
            name = "Predator & Prey",
            description = "Cyclic predatory pursuit: Cyan hunts Red, Red hunts Green, Green hunts Cyan, forming endless orbital chases.",
            speciesCount = 3,
            particleCount = 850,
            friction = 0.91f,
            forceStrength = 1.3f,
            forceDistance = 115f,
            matrix = arrayOf(
                floatArrayOf(0.30f, 0.80f, -0.90f),
                floatArrayOf(-0.90f, 0.30f, 0.80f),
                floatArrayOf(0.80f, -0.90f, 0.30f)
            ),
            speciesNames = listOf("Chaser (Cyan)", "Hunter (Ruby)", "Scout (Emerald)"),
            speciesColors = listOf("#00E5FF", "#FF2A6D", "#05FFA1"),
            backgroundColorHex = "#0A0D14",
            boundaryMode = "WRAP"
        ),
        PresetDefinition(
            name = "Symbiosis",
            description = "Balanced dual-species partnership generating binary spinning nodes and synchronized dance.",
            speciesCount = 2,
            particleCount = 600,
            friction = 0.89f,
            forceStrength = 1.1f,
            forceDistance = 100f,
            matrix = arrayOf(
                floatArrayOf(0.60f, -0.35f),
                floatArrayOf(0.75f, 0.50f)
            ),
            speciesNames = listOf("Host Organism", "Symbiont"),
            speciesColors = listOf("#00E5FF", "#FFE600"),
            backgroundColorHex = "#0B0D1B",
            boundaryMode = "WRAP"
        ),
        PresetDefinition(
            name = "Omniverse Swarm",
            description = "12-species mega-ecosystem: complex emergent food webs, crystalline cluster nodes, and iridescent spiral currents.",
            speciesCount = 12,
            particleCount = 1200,
            friction = 0.92f,
            forceStrength = 1.25f,
            forceDistance = 105f,
            matrix = Array(12) { i ->
                FloatArray(12) { j ->
                    when {
                        i == j -> 0.45f
                        (i + 1) % 12 == j -> 0.75f
                        (i - 1 + 12) % 12 == j -> -0.65f
                        (i + 3) % 12 == j -> 0.35f
                        (i - 3 + 12) % 12 == j -> -0.35f
                        else -> ((i * 3 + j * 7) % 10 - 5) / 10f
                    }
                }
            },
            speciesNames = ColorUtils.DEFAULT_SPECIES_NAMES,
            speciesColors = ColorUtils.DEFAULT_SPECIES_COLORS.map { ColorUtils.toHex(it) },
            backgroundColorHex = "#000000",
            boundaryMode = "WRAP"
        )
    )
}
