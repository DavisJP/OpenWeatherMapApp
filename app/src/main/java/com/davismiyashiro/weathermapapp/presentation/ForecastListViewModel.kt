package com.davismiyashiro.weathermapapp.presentation

import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.davismiyashiro.weathermapapp.domain.RepositoryInterface
import com.davismiyashiro.weathermapapp.injection.AssistedViewModelFactory
import com.davismiyashiro.weathermapapp.injection.hiltMavericksViewModelFactory
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class ForecastListViewModel @AssistedInject constructor(
    @Assisted initialState: ForecastListState,
    private val repo: RepositoryInterface
) : MavericksViewModel<ForecastListState>(initialState) {

    init {
        // TODO: Copy values from repo to state
//        setState {
//
//        }
    }

    @AssistedFactory
    interface Factory : AssistedViewModelFactory<ForecastListViewModel, ForecastListState> {
        override fun create(state: ForecastListState): ForecastListViewModel
    }

    companion object :
        MavericksViewModelFactory<ForecastListViewModel, ForecastListState> by hiltMavericksViewModelFactory()
}