package com.davismiyashiro.weathermapapp.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.davismiyashiro.weathermapapp.data.storage.UserPreferencesRepository
import com.davismiyashiro.weathermapapp.domain.ForecastListItemMapper
import com.davismiyashiro.weathermapapp.domain.Repository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

sealed interface ForecastListEvent {
    data object Refresh : ForecastListEvent
    data class UpdateTemperatureUnit(val unit: Int) : ForecastListEvent
}

@Composable
fun forecastListPresenter(
    repo: Repository,
    mapper: ForecastListItemMapper,
    userPrefs: UserPreferencesRepository
): ForecastListState {
    var isLoading by remember { mutableStateOf(true) }
    var forecastItems by remember { mutableStateOf(emptyList<ForecastListItem>()) }
    var error by remember { mutableStateOf<Throwable?>(null) }
    val temperatureUnit by userPrefs.temperatureUnitFlow.collectAsState(initial = userPrefs.getTemperatureUnit())

    val scope = rememberCoroutineScope()

    fun CoroutineScope.load() {
        launch {
            isLoading = true
            error = null
            try {
                repo.loadWeatherData()
                    .map { mapper.mapPlaceToForecastListItem(it) }
                    .catch { error = it }
                    .collect { forecastItems = it }
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        load()
    }

    val eventSink: (ForecastListEvent) -> Unit = { event ->
        when (event) {
            is ForecastListEvent.Refresh -> {
                scope.load()
            }

            is ForecastListEvent.UpdateTemperatureUnit -> {
                userPrefs.setTemperatureUnit(event.unit)
            }
        }
    }

    return ForecastListState(
        isLoading = isLoading,
        forecastItems = forecastItems,
        temperatureUnit = temperatureUnit,
        error = error,
        eventSink = eventSink
    )
}
