package com.davismiyashiro.weathermapapp.presentation

data class ForecastListState(
    val isLoading: Boolean = true,
    val forecastItems: List<ForecastListItem> = emptyList(),
    val error: Throwable? = null,
    val eventSink: (ForecastListEvent) -> Unit = {}
)