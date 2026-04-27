package com.davismiyashiro.weathermapapp.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberUpdatedState
import com.davismiyashiro.weathermapapp.data.storage.UserPreferencesRepository
import com.davismiyashiro.weathermapapp.domain.ForecastListItemMapper
import com.davismiyashiro.weathermapapp.domain.Repository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

@Composable
fun forecastListPresenter(
    repo: Repository,
    mapper: ForecastListItemMapper,
    userPrefs: UserPreferencesRepository,
    events: Flow<ForecastListEvent>,
): ForecastListState {
    val temperatureUnit by userPrefs.temperatureUnitFlow.collectAsState(initial = userPrefs.getTemperatureUnit())
    val currentTemperatureUnit by rememberUpdatedState(temperatureUnit)

    val state by produceState<ForecastListState>(
        initialValue = ForecastListState.Loading(temperatureUnit),
        key1 = repo,
        key2 = mapper,
        key3 = events,
    ) {
        events
            .onStart { emit(ForecastListEvent.Refresh) }
            .collectLatest { event ->
                when (event) {
                    is ForecastListEvent.Refresh -> {
                        val previousItems = when (value) {
                            is ForecastListState.Success -> (value as ForecastListState.Success).forecastItems
                            else -> emptyList()
                        }

                        value = if (previousItems.isEmpty()) {
                            ForecastListState.Loading(currentTemperatureUnit)
                        } else {
                            ForecastListState.Success(
                                forecastItems = previousItems,
                                temperatureUnit = currentTemperatureUnit,
                                isRefreshing = true
                            )
                        }

                        repo.loadWeatherData()
                            .map { mapper.mapPlaceToForecastListItem(it) }
                            .catch { error ->
                                value = ForecastListState.Error(
                                    error = error,
                                    temperatureUnit = currentTemperatureUnit,
                                    isRefreshing = false
                                )
                            }.collect { items ->
                                value = ForecastListState.Success(
                                    forecastItems = items,
                                    temperatureUnit = currentTemperatureUnit,
                                    isRefreshing = false
                                )
                            }
                    }

                    is ForecastListEvent.UpdateTemperatureUnit -> {
                        userPrefs.setTemperatureUnit(event.unit)
                        value = when (val currentState = value) {
                            is ForecastListState.Success -> currentState.copy(temperatureUnit = event.unit)
                            is ForecastListState.Error -> currentState.copy(temperatureUnit = event.unit)
                            is ForecastListState.Loading -> currentState.copy(temperatureUnit = event.unit)
                        }
                    }
                }
            }
    }

    return state
}
