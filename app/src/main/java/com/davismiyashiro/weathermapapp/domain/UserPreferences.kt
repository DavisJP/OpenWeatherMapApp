package com.davismiyashiro.weathermapapp.domain

import kotlinx.coroutines.flow.Flow

interface UserPreferences {
    val temperatureUnitFlow: Flow<Int>
    fun getTemperatureUnit(): Int
    fun setTemperatureUnit(unit: Int)
}
