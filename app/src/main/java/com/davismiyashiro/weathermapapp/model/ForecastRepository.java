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
