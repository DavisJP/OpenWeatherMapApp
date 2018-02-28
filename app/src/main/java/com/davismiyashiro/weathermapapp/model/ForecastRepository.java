package com.davismiyashiro.weathermapapp.model;

import com.davismiyashiro.weathermapapp.model.data.Place;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by Davis Miyashiro.
 */

public class ForecastRepository {

    //TODO: Hardcoded for now, change later
    final public static int LONDON_ID = 2643743;

    OpenWeatherApi openWeatherApi;

    Repository localRepository;

    private Place place;
    boolean dataIsStale = false;

    @Inject
    ForecastRepository (OpenWeatherApi remoteData, Repository localRepo) {
        openWeatherApi = remoteData;
        localRepository = localRepo;
    }

    public Observable<Place> loadWeatherData () {
        if (place != null && !dataIsStale) {
            return Observable.just(place);
        } else {
            place = new Place();
        }

        Observable<Place> remoteData = getAndSaveRemoteData();

        if (dataIsStale) {
            return remoteData;
        } else {
            Observable<Place> localData = getAndCacheLocalData();
            return localData.publish(local -> Observable.merge(local, remoteData.takeUntil(local)))
                    .firstOrError()
                    .toObservable();
        }
    }

    private Observable<Place> getAndCacheLocalData () {
        return localRepository.loadData()
                .map(localPlace -> place = localPlace);
    }

    private Observable<Place> getAndSaveRemoteData () {
        return openWeatherApi.getForecastById(LONDON_ID)
                .map(placeRemote -> {
                    place = placeRemote;
                    localRepository.storeData(placeRemote);
                    return placeRemote;
                })
                .doOnComplete(() -> dataIsStale = false);
    }

    public void refreshData() {
        dataIsStale = true;
    }
}
