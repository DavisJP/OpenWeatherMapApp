package com.davismiyashiro.weathermapapp.model;

import com.davismiyashiro.weathermapapp.model.data.Place;

import io.reactivex.Observable;

/**
 * Created by Davis Miyashiro.
 */

public interface Repository {

    Observable<Place> loadData ();

    void storeData (Place place);
}
