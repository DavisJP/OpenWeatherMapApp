package com.davismiyashiro.weathermapapp.model;

import com.davismiyashiro.weathermapapp.model.data.Place;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Davis Miyashiro.
 */

public interface OpenWeatherApi {

    //http://api.openweathermap.org/data/2.5/forecast?q=London&appid=3e29cf11d4eabe8eba6cf25d535eaac2&cnt=5

    String APP_ID = "3e29cf11d4eabe8eba6cf25d535eaac2";

    @GET("forecast")
    Observable<Place> getWeatherFromPlace (@Query("q") String place, @Query("appid") String id);

    @GET("forecast?appid="+ APP_ID)
    Observable<Place> getForecastById (@Query("id") int place);
}
