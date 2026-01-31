package com.davismiyashiro.weathermapapp.domain

import com.davismiyashiro.weathermapapp.data.entities.Place
import kotlinx.coroutines.flow.Flow

interface Repository {
    fun loadWeatherData(): Flow<Place>
}