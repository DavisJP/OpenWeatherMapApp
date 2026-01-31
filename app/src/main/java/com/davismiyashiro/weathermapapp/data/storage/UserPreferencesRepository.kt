package com.davismiyashiro.weathermapapp.data.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.davismiyashiro.weathermapapp.presentation.TEMPERATURE_DEFAULT
import com.davismiyashiro.weathermapapp.presentation.TEMPERATURE_KEY
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

@Singleton
class UserPreferencesRepository @Inject constructor(
    @param: ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    val temperatureUnitFlow: Flow<Int> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == TEMPERATURE_KEY) {
                trySend(getTemperatureUnit())
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)

        trySend(getTemperatureUnit())
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    fun getTemperatureUnit(): Int {
        return prefs.getInt(TEMPERATURE_KEY, TEMPERATURE_DEFAULT)
    }

    fun setTemperatureUnit(unit: Int) {
        prefs.edit { putInt(TEMPERATURE_KEY, unit) }
    }
}
