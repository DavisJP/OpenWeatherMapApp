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
import com.davismiyashiro.weathermapapp.domain.Repository
import com.google.gson.Gson

import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

/**
 * Created by Davis Miyashiro.
 */

class ForecastLocalRepository @Inject constructor(private val storage: SharedPreferenceStorage) :
    Repository {

    private val KEY_PLACE = "KEY_PLACE"
    private val gson = Gson()

    override fun loadData(): Observable<Place> {
        val value = storage.getString(KEY_PLACE)
        var place: Place? = gson.fromJson(value, Place::class.java)

        if (place == null) {
            place = Place()
        }
        return Observable.just(place)
    }

    override fun storeData(place: Place) {
        storage.setString(KEY_PLACE, gson.toJson(place))
    }
}
