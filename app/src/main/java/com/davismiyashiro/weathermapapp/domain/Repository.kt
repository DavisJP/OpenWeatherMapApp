package com.davismiyashiro.weathermapapp.domain

import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.Flow

interface Repository {
    val weatherFlow: Flow<ImmutableList<ForecastListItem>>
    suspend fun refresh()
}
