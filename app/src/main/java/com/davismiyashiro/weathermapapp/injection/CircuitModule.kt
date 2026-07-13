package com.davismiyashiro.weathermapapp.injection

import com.slack.circuit.foundation.Circuit
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.ui.Ui
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.Multibinds

@Module
@InstallIn(SingletonComponent::class)
interface CircuitModule {
    @Multibinds
    fun presenterFactories(): Set<Presenter.Factory>

    @Multibinds
    fun uiFactories(): Set<Ui.Factory>

    companion object {
        @Provides
        fun provideCircuit(
            presenterFactories: Set<@JvmSuppressWildcards Presenter.Factory>,
            uiFactories: Set<@JvmSuppressWildcards Ui.Factory>,
        ): Circuit {
            return Circuit.Builder()
                .addPresenterFactories(presenterFactories)
                .addUiFactories(uiFactories)
                .build()
        }
    }
}
