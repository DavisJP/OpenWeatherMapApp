package com.davismiyashiro.weathermapapp.domain

import kotlinx.coroutines.flow.Flow

interface UserPreferences {
    val temperatureUnitFlow: Flow<Int>
    suspend fun setTemperatureUnit(unit: Int)
}
