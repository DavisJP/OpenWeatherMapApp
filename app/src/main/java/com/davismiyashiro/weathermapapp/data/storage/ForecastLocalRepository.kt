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

package com.davismiyashiro.weathermapapp.data.storage

import com.davismiyashiro.weathermapapp.data.entities.Place
import com.davismiyashiro.weathermapapp.domain.LocalRepository
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val KEY_PLACE = "KEY_PLACE"

/**
 * Created by Davis Miyashiro.
 */
class ForecastLocalRepository @Inject constructor(private val storage: SharedPreferenceStorage) :
    LocalRepository {

    private val gson = Gson()

    override fun loadData(): Flow<Place> = flow {
        val value = storage.getString(KEY_PLACE)
        val place: Place? = gson.fromJson(value, Place::class.java)

        if (place != null) {
            emit(place)
        } else {
            throw NoSuchElementException("No local data found")
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun storeData(place: Place) {
        withContext(Dispatchers.IO) {
            storage.setString(KEY_PLACE, gson.toJson(place))
        }
    }
}
