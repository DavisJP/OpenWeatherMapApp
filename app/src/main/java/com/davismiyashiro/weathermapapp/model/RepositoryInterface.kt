package com.davismiyashiro.weathermapapp.model

import com.davismiyashiro.weathermapapp.model.data.Place
import io.reactivex.Observable

interface RepositoryInterface {
    fun loadWeatherData(): Observable<Place>
    fun refreshData()
}