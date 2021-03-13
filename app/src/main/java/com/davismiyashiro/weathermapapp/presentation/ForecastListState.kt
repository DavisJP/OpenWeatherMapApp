package com.davismiyashiro.weathermapapp.presentation

import com.airbnb.mvrx.Async
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.Uninitialized

data class ForecastListState(
    val forecast: Async<List<UIForecastListItem>> = Uninitialized
) : MavericksState {
}