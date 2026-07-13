package com.davismiyashiro.weathermapapp.presentation

import com.davismiyashiro.weathermapapp.domain.ForecastListItem
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import kotlinx.collections.immutable.ImmutableList

sealed interface ForecastListState : CircuitUiState {
    val temperatureUnit: Int
    val eventSink: (ForecastListEvent) -> Unit

    data class Loading(
        override val temperatureUnit: Int = TEMPERATURE_DEFAULT,
        override val eventSink: (ForecastListEvent) -> Unit = {},
    ) : ForecastListState

    data class Success(
        val forecastItems: ImmutableList<ForecastListItem>,
        override val temperatureUnit: Int = TEMPERATURE_DEFAULT,
        val isRefreshing: Boolean = false,
        override val eventSink: (ForecastListEvent) -> Unit = {},
    ) : ForecastListState

    data class Error(
        val error: Throwable,
        override val temperatureUnit: Int = TEMPERATURE_DEFAULT,
        val isRefreshing: Boolean = false,
        override val eventSink: (ForecastListEvent) -> Unit = {},
    ) : ForecastListState
}

sealed interface ForecastListEvent : CircuitUiEvent {
    data object Refresh : ForecastListEvent
    data class UpdateTemperatureUnit(val unit: Int) : ForecastListEvent
}
