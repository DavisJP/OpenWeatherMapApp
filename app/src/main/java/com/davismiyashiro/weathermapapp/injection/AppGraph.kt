package com.davismiyashiro.weathermapapp.injection

import android.app.Application
import com.davismiyashiro.weathermapapp.domain.Repository
import com.slack.circuit.foundation.Circuit
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import io.ktor.client.HttpClient

@SingleInApp
@DependencyGraph(AppScope::class)
interface AppGraph {
    val application: Application
    val httpClient: HttpClient
    val circuit: Circuit
    val repository: Repository

    @DependencyGraph.Factory
    interface Factory {
        fun create(@Provides application: Application): AppGraph
    }
}
