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
import com.davismiyashiro.weathermapapp.data.entities.City
import com.davismiyashiro.weathermapapp.data.entities.Conditions
import com.davismiyashiro.weathermapapp.data.entities.Place
import com.davismiyashiro.weathermapapp.domain.Repository
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.NoSuchElementException

class ForecastRepositoryTest {

    private lateinit var repository: ForecastRepository

    private val remoteRepository: OpenWeatherApi = mock()
    private val localRepository: Repository = mock()
    private lateinit var place: Place

    @Before
    fun setUp() {
        place = Place("123", 10.0, 1, mutableListOf(Conditions()), City())
        repository = ForecastRepository(remoteRepository, localRepository)
    }

    @Test
    fun `loadWeatherData consecutive calls returns value from cache`() = runTest {
        whenever(remoteRepository.getForecastById(anyInt())).thenReturn(place)

        // First call fetches from remote and populates cache
        repository.loadWeatherData().test {
            assertEquals(place, awaitItem())
            awaitComplete()
        }

        // Second call should hit the cache
        repository.loadWeatherData().test {
            assertEquals(place, awaitItem())
            awaitComplete()
        }

        // Verify remote was only called once, and local was never called
        verify(remoteRepository, times(1)).getForecastById(anyInt())
        verify(localRepository, never()).loadData()
        assertFalse(repository.refreshFromRemote)
    }

    @Test
    fun `loadWeatherData only remote available returns from cache`() = runTest {
        whenever(remoteRepository.getForecastById(anyInt())).thenReturn(place)
        whenever(localRepository.loadData()).thenReturn(flow { throw NoSuchElementException() })

        // Ensure we try local first, which will fail, then fetch from remote, populating cache
        repository.refreshFromRemote = false
        repository.loadWeatherData().test {
            assertEquals(place, awaitItem())
            awaitComplete()
        }
        verify(localRepository, times(1)).loadData() // Verify we tried local

        // Second call should hit the cache
        repository.loadWeatherData().test {
            assertEquals(place, awaitItem())
            awaitComplete()
        }

        // Verify remote was only called once in total
        verify(remoteRepository, times(1)).getForecastById(anyInt())
        assertFalse(repository.refreshFromRemote)
    }

    @Test
    fun `loadWeatherData 1st remote fetch then clear cache returns from local repo`() = runTest {
        whenever(remoteRepository.getForecastById(anyInt())).thenReturn(place)

        // First call is a refresh, fetches from remote
        repository.loadWeatherData().test {
            assertEquals(place, awaitItem())
            awaitComplete()
        }

        // Clear cache and set up local repository to return data
        whenever(localRepository.loadData()).thenReturn(flowOf(place))
        repository.refreshCache(null)

        // Second call should now fetch from local
        repository.loadWeatherData().test {
            assertEquals(place, awaitItem())
            awaitComplete()
        }

        // Verify remote was only hit on the first call, and local was hit on the second
        verify(remoteRepository, times(1)).getForecastById(anyInt())
        verify(localRepository).loadData()
        assertFalse(repository.refreshFromRemote)
    }

    @Test
    fun `loadWeatherData two calls with refresh returns from remote twice`() = runTest {
        val remotePlace1 = Place("remote_1")
        whenever(remoteRepository.getForecastById(anyInt())).thenReturn(remotePlace1)

        // First call is a refresh (by default), should hit remote
        repository.loadWeatherData().test {
            assertEquals(remotePlace1, awaitItem())
            awaitComplete()
        }

        // Force a refresh
        repository.refreshFromRemote()
        assertTrue(repository.refreshFromRemote)

        val remotePlace2 = Place("remote_2")
        whenever(remoteRepository.getForecastById(anyInt())).thenReturn(remotePlace2)

        // Second call should also hit remote
        repository.loadWeatherData().test {
            assertEquals(remotePlace2, awaitItem())
            awaitComplete()
        }

        // Verify remote was called twice
        verify(remoteRepository, times(2)).getForecastById(anyInt())
    }

    @Test
    fun `loadWeatherData local not available returns from remote`() = runTest {
        // Start in a non-refreshing state with an empty cache
        repository.refreshFromRemote = false
        repository.refreshCache(null)

        // Setup local to fail and remote to succeed
        whenever(localRepository.loadData()).thenReturn(flow { throw NoSuchElementException("No local data") })
        whenever(remoteRepository.getForecastById(anyInt())).thenReturn(place)

        // Load data, should try local, fail, and then fetch from remote
        repository.loadWeatherData().test {
            assertEquals(place, awaitItem())
            awaitComplete()
        }

        // Verify the sequence of events
        verify(localRepository, times(1)).loadData()
        verify(remoteRepository, times(1)).getForecastById(anyInt())
        assertFalse(repository.refreshFromRemote)
    }
}