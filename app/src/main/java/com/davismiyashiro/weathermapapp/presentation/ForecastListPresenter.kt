package com.davismiyashiro.weathermapapp.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
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

    val state by produceState<ForecastListState>(
        initialValue = ForecastListState.Loading(temperatureUnit),
        key1 = temperatureUnit
    ) {
        events
            .onStart { emit(ForecastListEvent.Refresh) }
            .collectLatest { event ->
                when (event) {
                    is ForecastListEvent.Refresh -> {
                        val previousItems = when (value) {
                            is ForecastListState.Success -> (value as ForecastListState.Success).forecastItems
                            is ForecastListState.Error -> emptyList()
                            else -> emptyList()
                        }

                        value =
                            if (previousItems.isEmpty()) {
                                ForecastListState.Loading(temperatureUnit)
                            } else {
                                ForecastListState.Success(
                                    forecastItems = previousItems,
                                    temperatureUnit = temperatureUnit,
                                    isRefreshing = true
                                )
                            }

                        repo.loadWeatherData()
                            .map { mapper.mapPlaceToForecastListItem(it) }
                            .catch { error ->
                                value = ForecastListState.Error(
                                    error = error,
                                    temperatureUnit = temperatureUnit,
                                    isRefreshing = false
                                )
                            }.collect { items ->
                                value = ForecastListState.Success(
                                    forecastItems = items,
                                    temperatureUnit = temperatureUnit,
                                    isRefreshing = false
                                )
                            }
                    }

                    is ForecastListEvent.UpdateTemperatureUnit -> {
                        userPrefs.setTemperatureUnit(event.unit)
                    }
                }
            }
    }
    return state
}
