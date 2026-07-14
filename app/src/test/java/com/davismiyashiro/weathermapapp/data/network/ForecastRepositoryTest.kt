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

import app.cash.turbine.test
import com.davismiyashiro.weathermapapp.data.dtos.City
import com.davismiyashiro.weathermapapp.data.dtos.Conditions
import com.davismiyashiro.weathermapapp.data.dtos.Place
import com.davismiyashiro.weathermapapp.data.mappers.ForecastListItemMapper
import com.davismiyashiro.weathermapapp.domain.ForecastListItem
import com.davismiyashiro.weathermapapp.domain.LocalRepository
import com.davismiyashiro.weathermapapp.domain.Repository
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.IOException

class ForecastRepositoryTest {

    private lateinit var repository: Repository

    private val openWeatherApi: OpenWeatherApi = mock()
    private val localRepository: LocalRepository = mock()
    private lateinit var remotePlace: Place
    private lateinit var localPlace: Place
    private lateinit var localForecastListItem: ImmutableList<ForecastListItem>
    private val json = Json { ignoreUnknownKeys = true }

    @Before
    fun setUp() {
        remotePlace = Place("remote", 10.0, 1, mutableListOf(Conditions()), City())
        localPlace = Place("local", 10.0, 1, mutableListOf(Conditions()), City())
        localForecastListItem = ForecastListItemMapper().mapPlaceToForecastListItem(localPlace)
        repository = ForecastRepository(openWeatherApi, localRepository, ForecastListItemMapper())
    }

    @Test
    fun `weatherFlow remote succeeds returns remote data`() = runTest {
        whenever(openWeatherApi.getForecastById(anyInt())).thenReturn(remotePlace)

        repository.weatherFlow.test {
            assertEquals(persistentListOf<ForecastListItem>(), awaitItem()) // Initial state

            repository.refresh()

            assertEquals(localForecastListItem, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        verify(openWeatherApi, times(1)).getForecastById(anyInt())
        verify(localRepository, times(1)).storeData(json.encodeToString(remotePlace))
    }

    @Test
    fun `weatherFlow remote fails local succeeds returns local data`() = runTest {
        whenever(openWeatherApi.getForecastById(anyInt())).thenAnswer { throw IOException() }
        whenever(localRepository.loadData()).thenReturn(flowOf(localForecastListItem))

        repository.weatherFlow.test {
            assertEquals(persistentListOf<ForecastListItem>(), awaitItem()) // Initial state

            repository.refresh()

            assertEquals(localForecastListItem, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        verify(openWeatherApi, times(1)).getForecastById(anyInt())
        verify(localRepository, times(1)).loadData()
    }

    @Test(expected = IOException::class)
    fun `refresh remote fails local fails throws exception`() = runTest {
        val remoteException = IOException("Remote error")
        whenever(openWeatherApi.getForecastById(anyInt())).thenAnswer { throw remoteException }
        whenever(localRepository.loadData()).thenReturn(flow { throw IOException("Local error") })

        try {
            repository.refresh()
        } finally {
            verify(openWeatherApi, times(1)).getForecastById(anyInt())
            verify(localRepository, times(1)).loadData()
        }
    }

    @Test
    fun `refresh performs work and updates flow`() = runTest {
        whenever(openWeatherApi.getForecastById(anyInt())).thenReturn(remotePlace)

        repository.weatherFlow.test {
            assertEquals(persistentListOf<ForecastListItem>(), awaitItem()) // Initial state

            repository.refresh()

            assertEquals(localForecastListItem, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
