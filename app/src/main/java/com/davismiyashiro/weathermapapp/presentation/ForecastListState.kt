package com.davismiyashiro.weathermapapp.presentation

import com.airbnb.mvrx.Async
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.Uninitialized
import com.davismiyashiro.weathermapapp.domain.ForecastListItemEntity

data class ForecastListState(
    val forecastEntityList: Async<List<ForecastListItemEntity>> = Uninitialized
) : MavericksState