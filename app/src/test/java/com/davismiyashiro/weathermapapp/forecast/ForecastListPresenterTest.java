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