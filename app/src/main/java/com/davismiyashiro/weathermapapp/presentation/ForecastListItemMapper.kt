package com.davismiyashiro.weathermapapp.presentation

import com.davismiyashiro.weathermapapp.data.Place
import com.davismiyashiro.weathermapapp.domain.ForecastListItemEntity
import javax.inject.Inject

open class ForecastListItemMapper @Inject constructor() {
    fun mapPlaceToForecastListItem(data: Place?): List<ForecastListItemEntity> {
        return mutableListOf<ForecastListItemEntity>().apply {
            data?.let {
                it.list?.let { conditions ->
                    for (condition in conditions) {
                        add(ForecastListItemEntity(condition))
                    }
                }
            }
        }
    }
}