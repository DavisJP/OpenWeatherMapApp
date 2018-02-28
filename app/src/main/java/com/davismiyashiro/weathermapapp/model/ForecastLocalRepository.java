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
