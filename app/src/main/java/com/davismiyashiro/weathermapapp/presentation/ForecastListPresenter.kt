package com.davismiyashiro.weathermapapp.presentation

import androidx.compose.runtime.*
import com.davismiyashiro.weathermapapp.domain.ForecastListItemMapper
import com.davismiyashiro.weathermapapp.domain.Repository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

sealed interface ForecastListEvent {
    data object Refresh : ForecastListEvent
}

@Composable
fun forecastListPresenter(
    repo: Repository,
    mapper: ForecastListItemMapper
): ForecastListState {
    var isLoading by remember { mutableStateOf(true) }
    var forecastItems by remember { mutableStateOf(emptyList<ForecastListItem>()) }
    var error by remember { mutableStateOf<Throwable?>(null) }
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
        }
    }

    return ForecastListState(
        isLoading = isLoading,
        forecastItems = forecastItems,
        error = error,
        eventSink = eventSink
    )
}
