/*
 * MIT License
 *
 * Copyright (c) 2018 Davis Miyashiro
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

package com.davismiyashiro.weathermapapp.data.network

import com.davismiyashiro.weathermapapp.data.mappers.ForecastListItemMapper
import com.davismiyashiro.weathermapapp.domain.ForecastListItem
import com.davismiyashiro.weathermapapp.domain.LocalRepository
import com.davismiyashiro.weathermapapp.domain.NetworkConnectivity
import com.davismiyashiro.weathermapapp.domain.Repository
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

// Hardcoded for testing, but api is deprecated, move to LAT/LONG
private const val LONDON_ID = 2643743

/**
 * Created by Davis Miyashiro.
 */
@Singleton
class ForecastRepository @Inject constructor(
    private val openWeatherApi: OpenWeatherApi,
    private val localRepository: LocalRepository,
    private val forecastListItemMapper: ForecastListItemMapper,
    private val networkConnectivity: NetworkConnectivity,
) : Repository {

    private val json = Json { ignoreUnknownKeys = true }

    private val _weatherFlow = MutableStateFlow<ImmutableList<ForecastListItem>>(persistentListOf())
    override val weatherFlow: Flow<ImmutableList<ForecastListItem>> = _weatherFlow.asStateFlow()

    override suspend fun refresh() {
        if (!networkConnectivity.isOnline()) {
            Timber.d("Offline: loading from local storage.")
            localRepository.loadData().collect { localData ->
                _weatherFlow.emit(localData)
            }
            return
        }

        try {
            val remoteData = getAndSaveRemoteData()
            _weatherFlow.emit(remoteData)
        } catch (e: ClientRequestException) {
            Timber.e("Client error: ${e.response.status}")
            throw e
        } catch (e: ServerResponseException) {
            Timber.e("Server error: ${e.response.status}")
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Unexpected error")
            throw e // Propagate error when online
        }
    }

    private suspend fun getAndSaveRemoteData(): ImmutableList<ForecastListItem> {
        val placeRemote = openWeatherApi.getForecastById(LONDON_ID)
        localRepository.storeData(json.encodeToString(placeRemote))
        return forecastListItemMapper.mapPlaceToForecastListItem(placeRemote)
    }
}
