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

import com.davismiyashiro.weathermapapp.data.entities.Place
import com.davismiyashiro.weathermapapp.domain.Repository
import com.davismiyashiro.weathermapapp.domain.RepositoryInterface
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by Davis Miyashiro.
 */

class ForecastRepository @Inject
constructor(
    private val openWeatherApi: OpenWeatherApi,
    private val localRepository: Repository
) : RepositoryInterface {

    //TODO: Hardcoded for now, change later
    private val LONDON_ID = 2643743

    private var localCache: Place? = null
    @Volatile
    internal var refreshFromRemote = true

    override fun loadWeatherData(): Flow<Place> = flow {
        if (localCache != null && !refreshFromRemote) {
            emit(localCache!!)
            return@flow
        }

        if (!refreshFromRemote) {
            try {
                val localData = localRepository.loadData().first()
                localCache = localData
                emit(localData)
                return@flow
            } catch (e: NoSuchElementException) {
                Timber.d("No local data available")
            }
        }

        try {
            val remoteData = getAndSaveRemoteData()
            emit(remoteData)
        } catch (e: Exception) {
            Timber.e(e, "remote error")
            throw e
        }
    }

    private suspend fun getAndSaveRemoteData(): Place {
        val placeRemote = openWeatherApi.getForecastById(LONDON_ID)
        localCache = placeRemote
        localRepository.storeData(placeRemote)
        refreshFromRemote = false
        return placeRemote
    }

    override suspend fun refreshFromRemote() {
        refreshFromRemote = true
    }

    fun refreshCache(cache: Place?) {
        localCache = cache
    }
}
