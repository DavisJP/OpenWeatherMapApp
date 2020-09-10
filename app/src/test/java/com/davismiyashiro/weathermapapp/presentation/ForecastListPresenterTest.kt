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

package com.davismiyashiro.weathermapapp.presentation

import com.davismiyashiro.weathermapapp.network.RepositoryInterface
import com.davismiyashiro.weathermapapp.network.data.Conditions
import com.davismiyashiro.weathermapapp.network.data.Main
import com.davismiyashiro.weathermapapp.network.data.Place
import com.davismiyashiro.weathermapapp.network.data.Weather
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import io.reactivex.Observable
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyList
import org.mockito.Mockito.`when`
import java.util.*

/**
 * Created by Davis Miyashiro.
 */
class ForecastListPresenterTest {

    private val repo = mock<RepositoryInterface>()

    private val view = mock<ForecastListInterfaces.View>()

    lateinit var presenter: ForecastListPresenter

    //    private final long DATE_MILLI = 1513296000;

    private var place = Place()

    @get:Rule
    var mOverrideSchedulersRule = RxSchedulersOverrideRule()

    @Before
    fun setUp() {
        presenter = ForecastListPresenter(repo)
        presenter.attachView(view)

        val conditions = Conditions()
        conditions.dt = 10.toLong()

        val main = Main()
        main.temp = java.lang.Double.valueOf(32.0)
        conditions.main = main

        val listWeather = ArrayList<Weather>()
        val weather = Weather()
        weather.icon = "Whatever"
        weather.main = "Ok"
        listWeather.add(weather)
        conditions.weather = listWeather

        place.list = listOf(conditions)
    }

    @After
    fun tearDown() {
        presenter.dettachView()
    }

    @Test
    fun testLoadWeatherData_WhenApiReturnsSuccess_ShowForecastList() {

        `when`(repo.loadWeatherData()).thenReturn(Observable.just(place))

        presenter.loadWeatherData(true)

        verify(view).showForecastList(anyList())
        verify(view, never()).showErrorMsg()
        verify(view).setSwipeRefresh(false)
    }

    @Test
    fun testLoadWeatherData_WhenApiReturnsError_ShowErrorMsg() {
        `when`(repo.loadWeatherData()).thenReturn(Observable.error(Throwable("Random error")))

        presenter.loadWeatherData(false)

        verify(view, never()).showForecastList(presenter.mapPlaceToForecastListItem(place))
        verify(view).setSwipeRefresh(false)
        verify(view).showErrorMsg()
    }
}