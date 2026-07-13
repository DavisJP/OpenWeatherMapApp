package com.davismiyashiro.weathermapapp.domain

import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.Flow

interface Repository {
    fun loadWeatherData(): Flow<ImmutableList<ForecastListItem>>
}
