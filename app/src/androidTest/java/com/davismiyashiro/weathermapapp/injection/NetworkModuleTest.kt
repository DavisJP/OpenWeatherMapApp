package com.davismiyashiro.weathermapapp.injection

import com.davismiyashiro.weathermapapp.data.network.FakeForecastRepository
import com.davismiyashiro.weathermapapp.domain.Repository
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [RepositoryModule::class]
)
class NetworkModuleTest {
    @Provides
    @Singleton
    fun provideRepository(): Repository = FakeForecastRepository()
}