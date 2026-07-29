package com.davismiyashiro.weathermapapp.data.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.davismiyashiro.weathermapapp.presentation.TEMPERATURE_DEFAULT
import com.davismiyashiro.weathermapapp.presentation.TEMPERATURE_KEY
import com.davismiyashiro.weathermapapp.injection.AppScope
import com.davismiyashiro.weathermapapp.injection.SingleInApp
import com.davismiyashiro.weathermapapp.domain.UserPreferences
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

@SingleInApp
@ContributesBinding(AppScope::class)
@Inject
class UserPreferencesRepository(
    private val context: Context,
) : UserPreferences {
    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    override val temperatureUnitFlow: Flow<Int> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == TEMPERATURE_KEY) {
                trySend(getTemperatureUnit())
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)

        trySend(getTemperatureUnit())
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    override fun getTemperatureUnit(): Int {
        return prefs.getInt(TEMPERATURE_KEY, TEMPERATURE_DEFAULT)
    }

    override fun setTemperatureUnit(unit: Int) {
        prefs.edit { putInt(TEMPERATURE_KEY, unit) }
    }
}
