package com.davismiyashiro.weathermapapp.injection

import com.davismiyashiro.weathermapapp.presentation.ForecastListViewModel
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.multibindings.IntoMap

@Module
@InstallIn(MavericksViewModelComponent::class)
interface ViewModelsModule {
    @Binds
    @IntoMap
    @ViewModelKey(ForecastListViewModel::class)
    @JvmSuppressWildcards
    fun bindsViewModelFactory(factory: ForecastListViewModel.Factory): AssistedViewModelFactory<*, *>
}