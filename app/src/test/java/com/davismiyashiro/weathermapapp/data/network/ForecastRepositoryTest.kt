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

import com.davismiyashiro.weathermapapp.data.Place
import com.davismiyashiro.weathermapapp.domain.Repository
import com.nhaarman.mockitokotlin2.mock
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.observers.TestObserver
import junit.framework.Assert.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mockito.*

/**
 * Created by Davis Miyashiro.
 */
class ForecastRepositoryTest {

    private lateinit var repository: ForecastRepository

    private lateinit var placeTestObserver: TestObserver<Place>

    private var remoteRepository = mock<OpenWeatherApi>()
    private var localRepository = mock<Repository>()

    @Before
    fun setUp() {
        placeTestObserver = TestObserver()

        repository = ForecastRepository(remoteRepository, localRepository)
    }

    @After
    fun tearDown() {
    }

    @Test
    fun loadWeatherData_twoConsecutiveCalls_returnsValueFromCache() {
        val placeLocal = Place()
        val placeRemote = Place()

        `when`(localRepository.loadData()).thenReturn(Observable.just(placeLocal))
        `when`(remoteRepository.getForecastById(anyInt())).thenReturn(Observable.just(placeRemote))

        //Making 2 consecutive calls
        repository.loadWeatherData()
                .subscribe(placeTestObserver)

        repository.loadWeatherData()
                .subscribe(placeTestObserver)

        //Repositories must be accessed only once
        verify<OpenWeatherApi>(remoteRepository).getForecastById(anyInt())
        verify<Repository>(localRepository).loadData()

        assertFalse(repository.dataIsStale)

        placeTestObserver.assertValue(placeRemote)

        placeTestObserver.assertValueCount(1)
        assertEquals(placeRemote, placeTestObserver.values()[0])
    }

    @Test
    fun loadWeatherData_onlyRemoteAvailable_returnsFromCache() {
        val place = Place()

        `when`(remoteRepository.getForecastById(anyInt())).thenReturn(Observable.just(place))
        `when`(localRepository.loadData()).thenReturn(Observable.never())

        //Making 2 consecutive calls
        repository.loadWeatherData()
                .subscribe(placeTestObserver)

        repository.loadWeatherData()
                .subscribe(placeTestObserver)

        //Repositories must be accessed only once
        verify<OpenWeatherApi>(remoteRepository).getForecastById(anyInt())
        verify<Repository>(localRepository).loadData()

        assertFalse(repository.dataIsStale)

        placeTestObserver.assertValueCount(1)
        assertEquals(place, placeTestObserver.values()[0])
    }

    @Test
    fun loadWeatherData_onlyLocalAvailable_returnsFromCache() {
        val place = Place()

        `when`(remoteRepository.getForecastById(anyInt())).thenReturn(Observable.never())
        `when`(localRepository.loadData()).thenReturn(Observable.just(place))

        //Making 2 consecutive calls
        repository.loadWeatherData()
                .subscribe(placeTestObserver)

        repository.loadWeatherData()
                .subscribe(placeTestObserver)

        //Repositories must be accessed only once
        verify<OpenWeatherApi>(remoteRepository).getForecastById(anyInt())
        verify<Repository>(localRepository).loadData()

        assertFalse(repository.dataIsStale)

        placeTestObserver.assertValueCount(1)
        assertEquals(place, placeTestObserver.values()[0])
    }

    @Test
    fun loadWeatherData_twoCallsWithRefresh_returnsFromRemoteTwice() {
        val place = Place()

        `when`(localRepository.loadData()).thenReturn(Observable.just(place))
        `when`(remoteRepository.getForecastById(anyInt())).thenReturn(Observable.just(Place()))

        repository.loadWeatherData()
                .subscribe(placeTestObserver)

        repository.refreshData()

        assertTrue(repository.dataIsStale)

        repository.loadWeatherData()
                .subscribe(placeTestObserver)

        verify<OpenWeatherApi>(remoteRepository, times(2)).getForecastById(anyInt())
    }

    @Test
    fun loadWeatherData_remoteNotAvailable_returnsFromLocal() {
        val place = Place()

        `when`(localRepository.loadData()).thenReturn(Observable.just(place))
        `when`(remoteRepository.getForecastById(anyInt())).thenReturn(Observable.never())

        repository.loadWeatherData()
                .subscribe(placeTestObserver)

        verify<OpenWeatherApi>(remoteRepository).getForecastById(anyInt())
        verify<Repository>(localRepository).loadData()

        assertFalse(repository.dataIsStale)

        placeTestObserver.assertValueCount(1)
        assertEquals(place, placeTestObserver.values()[0])
    }

    @Test
    fun loadWeatherData_localNotAvailable_returnsFromRemote() {
        val place = Place()

        `when`(localRepository.loadData()).thenReturn(Observable.never())
        `when`(remoteRepository.getForecastById(anyInt())).thenReturn(Observable.just(place))

        repository.loadWeatherData()
                .subscribe(placeTestObserver)

        verify<OpenWeatherApi>(remoteRepository).getForecastById(anyInt())
        verify<Repository>(localRepository).loadData()

        assertFalse(repository.dataIsStale)

        placeTestObserver.assertValueCount(1)
        assertEquals(place, placeTestObserver.values()[0])
    }
}