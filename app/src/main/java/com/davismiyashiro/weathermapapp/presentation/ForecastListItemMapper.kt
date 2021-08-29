package com.davismiyashiro.weathermapapp.presentation

import com.davismiyashiro.weathermapapp.data.Place
import javax.inject.Inject

open class ForecastListItemMapper @Inject constructor() {
    fun mapPlaceToForecastListItem(data: Place?): List<ForecastListItem> {
        return mutableListOf<ForecastListItem>().apply {
            data?.let {
                it.list?.let { conditions ->
                    for (condition in conditions) {
                        add(ForecastListItem(condition))
                    }
                }
            }
        }
    }
}