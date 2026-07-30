package com.davismiyashiro.weathermapapp.data.storage

import com.davismiyashiro.weathermapapp.domain.Storage
import com.davismiyashiro.weathermapapp.domain.UserPreferences
import com.davismiyashiro.weathermapapp.injection.AppScope
import com.davismiyashiro.weathermapapp.injection.SingleInApp
import com.davismiyashiro.weathermapapp.presentation.TEMPERATURE_DEFAULT
import com.davismiyashiro.weathermapapp.presentation.TEMPERATURE_KEY
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow

@SingleInApp
@ContributesBinding(AppScope::class)
@Inject
class UserPreferencesRepository(
    private val storage: Storage,
) : UserPreferences {

    override val temperatureUnitFlow: Flow<Int> = storage.getIntFlow(TEMPERATURE_KEY, TEMPERATURE_DEFAULT)

    override suspend fun setTemperatureUnit(unit: Int) {
        storage.setInt(TEMPERATURE_KEY, unit)
    }
}
