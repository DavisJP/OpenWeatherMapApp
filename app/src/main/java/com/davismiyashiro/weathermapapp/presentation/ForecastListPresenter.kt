/*
 * MIT License
 *
 * Copyright (c) 2021 Davis Miyashiro
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
import com.davismiyashiro.weathermapapp.presentation.ForecastListEvent.Refresh
import com.davismiyashiro.weathermapapp.presentation.ForecastListEvent.UpdateTemperatureUnit
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.presenter.Presenter
import dagger.hilt.components.SingletonComponent
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@CircuitInject(ForecastListScreen::class, SingletonComponent::class)
class ForecastListPresenter @Inject constructor(
    private val repo: Repository,
    private val mapper: ForecastListItemMapper,
    private val userPrefs: UserPreferencesRepository,
) : Presenter<ForecastListState> {

    @Composable
    override fun present(): ForecastListState {
        val temperatureUnit by userPrefs.temperatureUnitFlow.collectAsState(initial = userPrefs.getTemperatureUnit())
        var isRefreshing by remember { mutableStateOf(false) }
        var error by remember { mutableStateOf<Throwable?>(null) }
        var forecastItems by remember { mutableStateOf<ImmutableList<ForecastListItem>>(persistentListOf()) }
        var isLoading by remember { mutableStateOf(true) }

        val scope = rememberCoroutineScope()

        fun loadData() {
            scope.launch {
                if (forecastItems.isEmpty()) {
                    isLoading = true
                } else {
                    isRefreshing = true
                }
                error = null
                repo.loadWeatherData()
                    .map { mapper.mapPlaceToForecastListItem(it) }
                    .catch { e ->
                        error = e
                        isLoading = false
                        isRefreshing = false
                    }
                    .collect { items ->
                        forecastItems = items
                        isLoading = false
                        isRefreshing = false
                    }
            }
        }

        LaunchedEffect(Unit) {
            loadData()
        }

        val eventSink: (ForecastListEvent) -> Unit = { event ->
            when (event) {
                Refresh -> loadData()
                is UpdateTemperatureUnit -> userPrefs.setTemperatureUnit(event.unit)
            }
        }

        return when {
            isLoading -> ForecastListState.Loading(temperatureUnit, eventSink)
            error != null -> ForecastListState.Error(error!!, temperatureUnit, isRefreshing, eventSink)
            else -> ForecastListState.Success(forecastItems, temperatureUnit, isRefreshing, eventSink)
        }
    }
}
