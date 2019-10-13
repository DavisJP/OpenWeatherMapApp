package com.davismiyashiro.weathermapapp.network

import com.davismiyashiro.weathermapapp.network.data.Place
import io.reactivex.Observable

interface RepositoryInterface {
    fun loadWeatherData(): Observable<Place>
    fun refreshData()
}