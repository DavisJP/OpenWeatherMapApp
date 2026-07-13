package com.davismiyashiro.weathermapapp.data.mappers

import com.davismiyashiro.weathermapapp.data.dtos.Place
import com.davismiyashiro.weathermapapp.domain.ForecastListItem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import javax.inject.Inject

open class ForecastListItemMapper @Inject constructor() {
    fun mapPlaceToForecastListItem(data: Place?): ImmutableList<ForecastListItem> {
        return mutableListOf<ForecastListItem>().apply {
            data?.list?.forEach { condition ->
                add(
                    ForecastListItem(
                        date = condition.dt ?: 0L,
                        main = condition.weather?.firstOrNull()?.main ?: "",
                        temp = condition.main?.temp ?: 0.0,
                        icon = condition.weather?.firstOrNull()?.icon?.plus(".png") ?: "",
                    ),
                )
            }
        }.toImmutableList()
    }
}