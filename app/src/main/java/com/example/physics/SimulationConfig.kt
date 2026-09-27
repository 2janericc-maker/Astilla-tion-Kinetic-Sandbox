package com.example.physics

import androidx.compose.ui.graphics.Color

enum class BoundaryMode {
    WRAP,
    BOUNCE
}

enum class TouchMode(val label: String) {
    ATTRACT("Attract"),
    REPEL("Repel"),
    SPAWN("Spawn"),
    NONE("Touch: Off")
}

enum class ParticleStyle(val label: String) {
    DISC("Disc"),
    SPLINTER("Splinter"),
    GLOW("Glow")
}

data class Species(
    val id: Int,
    val name: String,
    val color: Color,
    val forceRadiusMultiplier: Float = 1.0f
)

object ColorUtils {
    fun toHex(color: Color): String {
        val a = (color.alpha * 255).toInt().coerceIn(0, 255)
        val r = (color.red * 255).toInt().coerceIn(0, 255)
        val g = (color.green * 255).toInt().coerceIn(0, 255)
        val b = (color.blue * 255).toInt().coerceIn(0, 255)
        return String.format("#%02X%02X%02X", r, g, b)
    }

    fun fromHex(hex: String, defaultColor: Color = Color(0xFF00E5FF)): Color {
        return try {
            val cleanHex = hex.removePrefix("#")
            when (cleanHex.length) {
                6 -> {
                    val colorInt = cleanHex.toLong(16).toInt()
                    Color(0xFF000000 or colorInt.toLong())
                }
                8 -> {
                    val colorLong = cleanHex.toLong(16)
                    Color(colorLong)
                }
                else -> defaultColor
            }
        } catch (_: Exception) {
            defaultColor
        }
    }

    val DEFAULT_SPECIES_COLORS = listOf(
        Color(0xFF00E5FF), // Neon Cyan (Species 1)
        Color(0xFFFF2A6D), // Cyber Pink (Species 2)
        Color(0xFF05FFA1), // Emerald Spores (Species 3)
        Color(0xFFFFE600), // Solar Gold (Species 4)
        Color(0xFFB537F2), // Void Violet (Species 5)
        Color(0xFFFF7700), // Flare Orange (Species 6)
        Color(0xFF3D5AFE), // Cobalt Prisms (Species 7)
        Color(0xFFFF1744), // Ruby Sparks (Species 8)
        Color(0xFF00E676), // Lime Shards (Species 9)
        Color(0xFFE040FB), // Magenta Whisps (Species 10)
        Color(0xFF00B0FF), // Azure Crystals (Species 11)
        Color(0xFFFFD600)  // Gold Stardust (Species 12)
    )

    val DEFAULT_SPECIES_NAMES = listOf(
        "Cyan Specks",
        "Crimson Motes",
        "Emerald Spores",
        "Solar Splinters",
        "Void Orbs",
        "Amber Flares",
        "Cobalt Prisms",
        "Ruby Sparks",
        "Lime Shards",
        "Magenta Whisps",
        "Azure Crystals",
        "Gold Stardust"
    )

    val SWATCH_COLORS = listOf(
        Color(0xFF00E5FF), Color(0xFF05FFA1), Color(0xFFFFE600),
        Color(0xFFFF7700), Color(0xFFFF2A6D), Color(0xFFB537F2),
        Color(0xFF4D96FF), Color(0xFF6BCB77), Color(0xFFFFD93D),
        Color(0xFFFF6B6B), Color(0xFFFFFFFF), Color(0xFF94A3B8)
    )

    val BACKGROUND_SWATCHES = listOf(
        Color(0xFF000000) to "Void Black",
        Color(0xFF0A0D14) to "Cyber Dark",
        Color(0xFF0B0D1B) to "Deep Space",
        Color(0xFF141824) to "Midnight",
        Color(0xFF181829) to "Neon Nebula",
        Color(0xFF1E293B) to "Slate Gray",
        Color(0xFF0F172A) to "Abyss",
        Color(0xFFF1F5F9) to "Canvas Light"
    )
}
