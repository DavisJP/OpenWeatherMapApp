package com.davismiyashiro.weathermapapp.presentation

import com.airbnb.mvrx.MavericksStateFactory
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.davismiyashiro.weathermapapp.App
import com.davismiyashiro.weathermapapp.domain.RepositoryInterface
import javax.inject.Inject

class ForecastListViewModel(initialState: ForecastListState) : MavericksViewModel<ForecastListState>(initialState) {
//class ForecastListViewModel @Inject constructor(
//    initialState: ForecastListState,
//    private val repo: RepositoryInterface
//) : MavericksViewModel<ForecastListState>(initialState) {

//    companion object : MavericksViewModelFactory<ForecastListViewModel, ForecastListState> {
//        override fun create(
//            viewModelContext: ViewModelContext,
//            state: ForecastListState
//        ): ForecastListViewModel {
//            val forecastListRepo = viewModelContext.app<App>().weathermapapp
//            return ForecastListViewModel(state, repo)
//            return super.create(viewModelContext, state)
//        }
//    }

}