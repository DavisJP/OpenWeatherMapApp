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

package com.davismiyashiro.weathermapapp.forecast;

import com.davismiyashiro.weathermapapp.model.data.Conditions;
import com.davismiyashiro.weathermapapp.model.data.Main;
import com.davismiyashiro.weathermapapp.model.data.Place;
import com.davismiyashiro.weathermapapp.model.data.Weather;
import com.davismiyashiro.weathermapapp.model.ForecastRepository;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.Observable;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by Davis Miyashiro.
 */
@RunWith(MockitoJUnitRunner.class)
public class ForecastListPresenterTest {

    @Mock
    ForecastRepository repo;

    @Mock
    ForecastListInterfaces.View view;

    private ForecastListPresenter presenter;

//    private final long DATE_MILLI = 1513296000;

    Place place = new Place();

    @Rule
    public final RxSchedulersOverrideRule mOverrideSchedulersRule = new RxSchedulersOverrideRule();

    @Before
    public void setUp () {
        presenter = new ForecastListPresenter(repo);
        presenter.attachView(view);

        Conditions conditions = new Conditions();
        conditions.setDt((long)10);

        Main main = new Main();
        main.setTemp(Double.valueOf(32));
        conditions.setMain(main);

        List<Weather> listWeather = new ArrayList<>();
        Weather weather = new Weather();
        weather.setIcon("Whatever");
        weather.setMain("Ok");
        listWeather.add(weather);
        conditions.setWeather(listWeather);

        place.setList(Arrays.asList(conditions));
    }

    @After
    public void tearDown () {
        presenter.dettachView();
    }

    @Test
    public void testLoadWeatherData_WhenApiReturnsSuccess_ShowForecastList() {

        when(repo.loadWeatherData()).thenReturn(Observable.just(place));

        presenter.loadWeatherData(true);

        verify(view).showForecastList(anyList());
        verify(view, never()).showErrorMsg();
        verify(view).setSwipeRefresh(false);
    }

    @Test
    public void testLoadWeatherData_WhenApiReturnsError_ShowErrorMsg() {
        when(repo.loadWeatherData()).thenReturn(Observable.<Place>error(new Throwable("Random error")));

        presenter.loadWeatherData(true);

        verify(view).showErrorMsg();
        verify(view, never()).showForecastList(presenter.mapPlaceToForecastListItem(place));
        verify(view).setSwipeRefresh(false);
    }

//    @Test
//    public void testDateApi () {
//
//        assertEquals("FRIDAY - 00:00", presenter.getReadableDate (DATE_MILLI));
//    }
}