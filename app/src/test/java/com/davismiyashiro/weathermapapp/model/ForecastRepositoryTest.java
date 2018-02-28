package com.davismiyashiro.weathermapapp.model;

import com.davismiyashiro.weathermapapp.model.data.Place;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by Davis Miyashiro.
 */
@RunWith(MockitoJUnitRunner.class)
public class ForecastRepositoryTest {

    private ForecastRepository repository;

    private TestObserver<Place> placeTestObserver;
    private TestObserver<Place> placeTestObserver2;

    @Mock
    OpenWeatherApi remoteRepository;

    @Mock
    Repository localRepository;

    @Before
    public void setUp() {
        repository = new ForecastRepository(remoteRepository, localRepository);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void loadWeatherData_twoConsecutiveCalls_returnsValueFromCache() {
        Place placeLocal = new Place();
        Place placeRemote = new Place();
        placeTestObserver = new TestObserver<>();
        placeTestObserver2 = new TestObserver<>();

        when(localRepository.loadData()).thenReturn(Observable.just(placeLocal));
        when(remoteRepository.getForecastById(anyInt())).thenReturn(Observable.just(placeRemote));

        //Making 2 consecutive calls
        repository.loadWeatherData()
                .subscribe(placeTestObserver);

        repository.loadWeatherData()
                .subscribe(placeTestObserver2);

        //Repositories must be accessed only once
        verify(remoteRepository).getForecastById(anyInt());
        verify(localRepository).loadData();

        assertFalse(repository.dataIsStale);

        placeTestObserver.assertValue(placeRemote);
        placeTestObserver2.assertValue(placeRemote);

        assertEquals(1, placeTestObserver.valueCount());
        assertEquals(placeRemote, placeTestObserver.values().get(0));
    }

    @Test
    public void loadWeatherData_onlyRemoteAvailable_returnsFromCache() {
        Place place = new Place();
        placeTestObserver = new TestObserver<>();
        placeTestObserver2 = new TestObserver<>();

        when(remoteRepository.getForecastById(anyInt())).thenReturn(Observable.just(place));
        when(localRepository.loadData()).thenReturn(Observable.never());

        //Making 2 consecutive calls
        repository.loadWeatherData()
                .subscribe(placeTestObserver);

        repository.loadWeatherData()
                .subscribe(placeTestObserver2);

        //Repositories must be accessed only once
        verify(remoteRepository).getForecastById(anyInt());
        verify(localRepository).loadData();

        assertFalse(repository.dataIsStale);

        assertEquals(1, placeTestObserver.valueCount());
        assertEquals(place, placeTestObserver.values().get(0));
    }

    @Test
    public void loadWeatherData_onlyLocalAvailable_returnsFromCache() {
        Place place = new Place();
        placeTestObserver = new TestObserver<>();
        placeTestObserver2 = new TestObserver<>();

        when(remoteRepository.getForecastById(anyInt())).thenReturn(Observable.never());
        when(localRepository.loadData()).thenReturn(Observable.just(place));

        //Making 2 consecutive calls
        repository.loadWeatherData()
                .subscribe(placeTestObserver);

        repository.loadWeatherData()
                .subscribe(placeTestObserver2);

        //Repositories must be accessed only once
        verify(remoteRepository).getForecastById(anyInt());
        verify(localRepository).loadData();

        assertFalse(repository.dataIsStale);

        assertEquals(1, placeTestObserver.valueCount());
        assertEquals(place, placeTestObserver.values().get(0));
    }

    @Test
    public void loadWeatherData_twoCallsWithRefresh_returnsFromRemoteTwice() {
        Place place = new Place();
        placeTestObserver = new TestObserver<>();
        placeTestObserver2 = new TestObserver<>();

        when(localRepository.loadData()).thenReturn(Observable.just(place));
        when(remoteRepository.getForecastById(anyInt())).thenReturn(Observable.just(new Place()));

        repository.loadWeatherData()
                .subscribe(placeTestObserver);

        repository.refreshData();

        repository.loadWeatherData()
                .subscribe(placeTestObserver2);

        verify(remoteRepository, times(2)).getForecastById(anyInt());
        verify(localRepository).loadData();

        assertFalse(repository.dataIsStale);
    }

    @Test
    public void loadWeatherData_remoteNotAvailable_returnsFromLocal() {
        Place place = new Place();
        placeTestObserver = new TestObserver<>();

        when(localRepository.loadData()).thenReturn(Observable.just(place));
        when(remoteRepository.getForecastById(anyInt())).thenReturn(Observable.never());

        repository.loadWeatherData()
                .subscribe(placeTestObserver);

        verify(remoteRepository).getForecastById(anyInt());
        verify(localRepository).loadData();

        assertFalse(repository.dataIsStale);

        assertEquals(1, placeTestObserver.valueCount());
        assertEquals(place, placeTestObserver.values().get(0));
    }

    @Test
    public void loadWeatherData_localNotAvailable_returnsFromRemote() {
        Place place = new Place();
        placeTestObserver = new TestObserver<>();

        when(localRepository.loadData()).thenReturn(Observable.never());
        when(remoteRepository.getForecastById(anyInt())).thenReturn(Observable.just(place));

        repository.loadWeatherData()
                .subscribe(placeTestObserver);

        verify(remoteRepository).getForecastById(anyInt());
        verify(localRepository).loadData();

        assertFalse(repository.dataIsStale);

        assertEquals(1, placeTestObserver.valueCount());
        assertEquals(place, placeTestObserver.values().get(0));
    }
}