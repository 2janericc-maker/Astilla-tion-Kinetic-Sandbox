package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "presets")
data class PresetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val isBuiltIn: Boolean = false,
    val speciesCount: Int,
    val particleCount: Int,
    val friction: Float,
    val forceStrength: Float,
    val forceDistance: Float,
    val matrixJson: String,
    val speciesColorsHex: String,
    val speciesNames: String,
    val backgroundColorHex: String = "#0A0D14",
    val boundaryMode: String = "WRAP",
    val createdAt: Long = System.currentTimeMillis()
)
