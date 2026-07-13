package com.davismiyashiro.weathermapapp.domain

import kotlinx.serialization.Serializable

@Serializable
data class ForecastListItem(
    val date: Long,
    val main: String,
    val temp: Double,
    val icon: String,
)