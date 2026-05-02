package com.davismiyashiro.weathermapapp.data.network

import com.davismiyashiro.weathermapapp.data.entities.Conditions
import com.davismiyashiro.weathermapapp.data.entities.Main
import com.davismiyashiro.weathermapapp.data.entities.Place
import com.davismiyashiro.weathermapapp.data.entities.Weather
import com.davismiyashiro.weathermapapp.domain.Repository
import kotlinx.coroutines.flow.flow

class FakeForecastRepository : Repository {
    @Volatile
    var place: Place = fakePlace()

    override fun loadWeatherData() = flow {
        emit(place)
    }
}

fun fakePlace(): Place {
    return Place(
        cnt = 1,
        list = listOf(
            Conditions(
                dt = 1_700_000_000L,
                main = Main(temp = 300.0),
                weather = listOf(
                    Weather(
                        main = "Rain",
                        icon = "10d",
                    ),
                ),
            ),
        ),
    )
}