package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class PresetRepository(private val presetDao: PresetDao) {

    val allPresets: Flow<List<PresetEntity>> = presetDao.getAllPresets()

    suspend fun ensureBuiltInPresetsSeeded() {
        withContext(Dispatchers.IO) {
            val count = presetDao.getBuiltInCount()
            if (count == 0) {
                DefaultPresets.BUILT_IN_PRESETS.forEach { presetDef ->
                    presetDao.insertPreset(presetDef.toEntity(isBuiltIn = true))
                }
            }
        }
    }

    suspend fun saveUserPreset(preset: PresetEntity): Long {
        return withContext(Dispatchers.IO) {
            presetDao.insertPreset(preset.copy(isBuiltIn = false, createdAt = System.currentTimeMillis()))
        }
    }

    suspend fun deletePreset(id: Long) {
        withContext(Dispatchers.IO) {
            presetDao.deletePresetById(id)
        }
    }

    suspend fun getPreset(id: Long): PresetEntity? {
        return withContext(Dispatchers.IO) {
            presetDao.getPresetById(id)
        }
    }
}
