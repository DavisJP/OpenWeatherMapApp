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

package com.davismiyashiro.weathermapapp.model

import com.davismiyashiro.weathermapapp.model.data.Place
import io.reactivex.Observable
import javax.inject.Inject

/**
 * Created by Davis Miyashiro.
 */

class ForecastRepository @Inject
internal constructor(private val openWeatherApi: OpenWeatherApi,
                     private val localRepository: Repository) {

    //TODO: Hardcoded for now, change later
    private val LONDON_ID = 2643743

    private var place: Place? = null
    internal var dataIsStale = false

    fun loadWeatherData(): Observable<Place> {
        if (place != null && !dataIsStale) {
            return Observable.just(place)
        } else {
            place = Place()
        }

        val remoteData = getAndSaveRemoteData()

        return if (dataIsStale) {
            remoteData
        } else {
            getAndCacheLocalData()
                    .publish { local -> Observable.merge(local, remoteData.takeUntil(local)) }
                    .firstOrError()
                    .toObservable()
        }
    }

    private fun getAndCacheLocalData(): Observable<Place> {
        return localRepository.loadData()
                .map<Place> { placeParam ->
                    place = placeParam
                    placeParam
                }
    }

    private fun getAndSaveRemoteData(): Observable<Place> {
        return openWeatherApi.getForecastById(LONDON_ID)
                .map { placeRemote ->
                    place = placeRemote
                    localRepository.storeData(placeRemote)
                    placeRemote
                }
                .doOnComplete { dataIsStale = false }
    }

    fun refreshData() {
        dataIsStale = true
    }
}
