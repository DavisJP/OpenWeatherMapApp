package com.davismiyashiro.weathermapapp.presentation

data class ForecastListItem(
    val date: Long,
    val main: String,
    val temp: Double,
    val icon: String
)
