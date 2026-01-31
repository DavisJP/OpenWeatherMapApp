package com.davismiyashiro.weathermapapp.presentation

data class ForecastListState(
    val isLoading: Boolean = true,
    val forecastItems: List<ForecastListItem> = emptyList(),
    val temperatureUnit: Int = TEMPERATURE_DEFAULT,
    val error: Throwable? = null,
    val eventSink: (ForecastListEvent) -> Unit = {}
)