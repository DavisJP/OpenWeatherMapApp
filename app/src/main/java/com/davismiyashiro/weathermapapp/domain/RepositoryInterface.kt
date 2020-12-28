package com.davismiyashiro.weathermapapp.domain

import com.davismiyashiro.weathermapapp.data.Place
import io.reactivex.rxjava3.core.Observable

interface RepositoryInterface {
    fun loadWeatherData(): Observable<Place>
    fun refreshData()
}