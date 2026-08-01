package com.kusa.imagefornet

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    private object PreferencesKeys {
        val WATERMARK_TEXT = stringPreferencesKey("watermark_text")
        val POSITION = stringPreferencesKey("position")
        val TEXT_SIZE = floatPreferencesKey("text_size")
        val OPACITY = floatPreferencesKey("opacity")
        val COLOR = intPreferencesKey("color")
    }

    val settingsFlow: Flow<WatermarkSettings> = context.dataStore.data
        .map { preferences ->
            WatermarkSettings(
                text = preferences[PreferencesKeys.WATERMARK_TEXT] ?: "Watermark",
                position = try {
                    WatermarkPosition.valueOf(
                        preferences[PreferencesKeys.POSITION] ?: WatermarkPosition.BOTTOM_RIGHT.name
                    )
                } catch (e: Exception) {
                    WatermarkPosition.BOTTOM_RIGHT
                },
                textSize = preferences[PreferencesKeys.TEXT_SIZE] ?: 100f,
                opacity = preferences[PreferencesKeys.OPACITY] ?: 128f,
                color = preferences[PreferencesKeys.COLOR] ?: -1 // White
            )
        }

    suspend fun updateWatermarkText(text: String) {
        context.dataStore.edit { it[PreferencesKeys.WATERMARK_TEXT] = text }
    }

    suspend fun updatePosition(position: WatermarkPosition) {
        context.dataStore.edit { it[PreferencesKeys.POSITION] = position.name }
    }

    suspend fun updateTextSize(size: Float) {
        context.dataStore.edit { it[PreferencesKeys.TEXT_SIZE] = size }
    }

    suspend fun updateOpacity(opacity: Float) {
        context.dataStore.edit { it[PreferencesKeys.OPACITY] = opacity }
    }

    suspend fun updateColor(color: Int) {
        context.dataStore.edit { it[PreferencesKeys.COLOR] = color }
    }
}

data class WatermarkSettings(
    val text: String,
    val position: WatermarkPosition,
    val textSize: Float,
    val opacity: Float,
    val color: Int
)
