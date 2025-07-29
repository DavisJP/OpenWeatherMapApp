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

import com.davismiyashiro.weathermapapp.data.entities.City
import com.davismiyashiro.weathermapapp.data.entities.Conditions
import com.davismiyashiro.weathermapapp.data.entities.Place
import com.davismiyashiro.weathermapapp.domain.Repository
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.observers.TestObserver
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

/**
 * Created by Davis Miyashiro.
 */
class ForecastRepositoryTest {

    private lateinit var repository: ForecastRepository

    private lateinit var placeTestObserver: TestObserver<Place>

    private var remoteRepository = mock<OpenWeatherApi>()
    private var localRepository = mock<Repository>()
    private lateinit var place: Place

    @Before
    fun setUp() {
        place = Place("123", 10.0, 1, mutableListOf(Conditions()), City())
        placeTestObserver = TestObserver()

        repository = ForecastRepository(remoteRepository, localRepository)
    }

    @After
    fun tearDown() {
    }

    @Test
    fun loadWeatherData_twoConsecutiveCalls_returnsValueFromCache() {
        `when`(localRepository.loadData()).thenReturn(Observable.never())
        `when`(remoteRepository.getForecastById(anyInt())).thenReturn(Observable.just(place))

        //Making 2 consecutive calls
        repository.loadWeatherData()
            .subscribe(placeTestObserver)

        repository.loadWeatherData()
            .subscribe(placeTestObserver)

        //Repositories must be accessed only once
        verify(remoteRepository).getForecastById(anyInt())
        verify(localRepository, never()).loadData()

        assertFalse(repository.refreshFromRemote)

        placeTestObserver.assertValue(place)
            .assertValueCount(1)
            .assertValue(place)
    }

    @Test
    fun loadWeatherData_onlyRemoteAvailable_returnsFromCache() {
        `when`(remoteRepository.getForecastById(anyInt())).thenReturn(Observable.just(place))
        `when`(localRepository.loadData()).thenReturn(Observable.never())

        //Making 2 consecutive calls
        repository.loadWeatherData()
            .subscribe(placeTestObserver)

        repository.loadWeatherData()
            .subscribe(placeTestObserver)

        //Repositories must be accessed only once
        verify(remoteRepository).getForecastById(anyInt())

        assertFalse(repository.refreshFromRemote)

        placeTestObserver.assertValueCount(1)
        assertEquals(place, placeTestObserver.values()[0])
    }

    /**
     * Save to cache and localRepo, and set dataStale = false
     **/
    private fun firstRemoteCallSuccessful() {
        whenever(remoteRepository.getForecastById(anyInt())).thenReturn(Observable.just(place))

        repository.loadWeatherData()
            .subscribe(placeTestObserver)

        verify(remoteRepository).getForecastById(anyInt())
    }

    @Test
    fun loadWeatherData_1stRemoteFetchThenClearCache_returnsFromLocalRepo() {
        firstRemoteCallSuccessful()

        whenever(remoteRepository.getForecastById(anyInt())).thenReturn(Observable.never())
        whenever(localRepository.loadData()).thenReturn(Observable.just(place))

        repository.refreshCache(null)

        repository.loadWeatherData()
            .subscribe(placeTestObserver)

        verify(remoteRepository, times(2))
            .getForecastById(anyInt())
        verify(localRepository).loadData()
        assertFalse(repository.refreshFromRemote)

        placeTestObserver.assertValueCount(1)
        assertEquals(place, placeTestObserver.values()[0])
    }

    @Test
    fun loadWeatherData_twoCallsWithRefresh_returnsFromRemoteTwice() {
        `when`(localRepository.loadData()).thenReturn(Observable.just(place))
        `when`(remoteRepository.getForecastById(anyInt())).thenReturn(Observable.just(Place()))

        repository.loadWeatherData()
            .subscribe(placeTestObserver)

        repository.refreshFromRemote()

        assertTrue(repository.refreshFromRemote)

        repository.loadWeatherData()
            .subscribe(placeTestObserver)

        verify(remoteRepository, times(2)).getForecastById(anyInt())
    }

    @Test
    fun loadWeatherData_localNotAvailable_returnsFromRemote() {
        firstRemoteCallSuccessful()

        `when`(localRepository.loadData()).thenReturn(Observable.never())
        `when`(remoteRepository.getForecastById(anyInt())).thenReturn(Observable.just(place))

        repository.refreshCache(null)

        repository.loadWeatherData()
            .subscribe(placeTestObserver)

        verify(remoteRepository, times(2)).getForecastById(anyInt())
        verify(localRepository).loadData()

        assertFalse(repository.refreshFromRemote)

        placeTestObserver.assertValueCount(1)
        assertEquals(place, placeTestObserver.values()[0])
    }
}