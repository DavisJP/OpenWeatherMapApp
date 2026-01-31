package com.davismiyashiro.weathermapapp.presentation

import androidx.compose.runtime.*
import com.davismiyashiro.weathermapapp.domain.ForecastListItemMapper
import com.davismiyashiro.weathermapapp.domain.RepositoryInterface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

sealed interface ForecastListEvent {
    data object Refresh : ForecastListEvent
}

@Composable
fun forecastListPresenter(
    repo: RepositoryInterface,
    mapper: ForecastListItemMapper
): ForecastListState {
    var isLoading by remember { mutableStateOf(true) }
    var forecastItems by remember { mutableStateOf(emptyList<ForecastListItem>()) }
    var error by remember { mutableStateOf<Throwable?>(null) }
    val scope = rememberCoroutineScope()

    fun CoroutineScope.load(isRefresh: Boolean) {
        launch {
            isLoading = true
            try {
                if (isRefresh) {
                    repo.refreshFromRemote()
                }
                repo.loadWeatherData()
                    .map { mapper.mapPlaceToForecastListItem(it) }
                    .catch { error = it }
                    .collect {
                        forecastItems = it
                    }
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        load(true)
    }

    val eventSink: (ForecastListEvent) -> Unit = { event ->
        when (event) {
            is ForecastListEvent.Refresh -> {
                scope.load(true)
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
