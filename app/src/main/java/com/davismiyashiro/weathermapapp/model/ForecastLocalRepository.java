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

import android.content.SharedPreferences;

import com.davismiyashiro.weathermapapp.model.data.Place;
import com.google.gson.Gson;

import io.reactivex.Observable;

/**
 * Created by Davis Miyashiro.
 */

public class ForecastLocalRepository implements Repository {

    public static String KEY_PLACE = "KEY_PLACE";

    SharedPreferences sharedPreferences;

    public ForecastLocalRepository (SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    @Override
    public Observable<Place> loadData() {
        String value = sharedPreferences.getString(KEY_PLACE, "");
        Place place = new Gson().fromJson(value, Place.class);
        return Observable.just(place);
    }

    @Override
    public void storeData (Place place) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_PLACE, new Gson().toJson(place));
        editor.apply();
    }
}
