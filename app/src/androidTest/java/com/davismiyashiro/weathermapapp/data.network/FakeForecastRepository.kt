package com.davismiyashiro.weathermapapp.data.network

import com.davismiyashiro.weathermapapp.data.dtos.Conditions
import com.davismiyashiro.weathermapapp.data.dtos.Main
import com.davismiyashiro.weathermapapp.data.dtos.Place
import com.davismiyashiro.weathermapapp.data.dtos.Weather
import com.davismiyashiro.weathermapapp.data.mappers.ForecastListItemMapper
import com.davismiyashiro.weathermapapp.domain.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeForecastRepository : Repository {
    @Volatile
    var place: Place = fakePlace()
        set(value) {
            field = value
            _weatherFlow.value = ForecastListItemMapper().mapPlaceToForecastListItem(value)
        }

    private val _weatherFlow = MutableStateFlow(ForecastListItemMapper().mapPlaceToForecastListItem(place))
    override val weatherFlow = _weatherFlow.asStateFlow()

    override suspend fun refresh() {
        _weatherFlow.value = ForecastListItemMapper().mapPlaceToForecastListItem(place)
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