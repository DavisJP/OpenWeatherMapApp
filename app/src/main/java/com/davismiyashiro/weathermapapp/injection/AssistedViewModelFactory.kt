package com.davismiyashiro.weathermapapp.injection

import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksViewModel

interface AssistedViewModelFactory<VM : MavericksViewModel<S>, S : MavericksState> {
    fun create(state: S): VM
}