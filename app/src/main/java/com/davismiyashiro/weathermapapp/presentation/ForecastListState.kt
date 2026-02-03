package com.davismiyashiro.weathermapapp.presentation

sealed interface ForecastListState {
    val temperatureUnit: Int

    data class Loading(
        override val temperatureUnit: Int = TEMPERATURE_DEFAULT,
    ) : ForecastListState

    data class Success(
        val forecastItems: List<ForecastListItem>,
        override val temperatureUnit: Int = TEMPERATURE_DEFAULT,
        val isRefreshing: Boolean = false,
    ) : ForecastListState

    data class Error(
        val error: Throwable,
        override val temperatureUnit: Int = TEMPERATURE_DEFAULT,
        val isRefreshing: Boolean = false,
    ) : ForecastListState
}

sealed interface ForecastListEvent {
    data object Refresh : ForecastListEvent
    data class UpdateTemperatureUnit(val unit: Int) : ForecastListEvent
}
