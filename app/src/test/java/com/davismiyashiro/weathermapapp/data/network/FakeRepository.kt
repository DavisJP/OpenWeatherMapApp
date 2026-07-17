package com.davismiyashiro.weathermapapp.data.network

import com.davismiyashiro.weathermapapp.domain.ForecastListItem
import com.davismiyashiro.weathermapapp.domain.Repository
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeRepository : Repository {
    private val _weatherFlow = MutableStateFlow<ImmutableList<ForecastListItem>>(persistentListOf())
    override val weatherFlow: Flow<ImmutableList<ForecastListItem>> = _weatherFlow.asStateFlow()

    var refreshCount = 0
        private set

    var shouldFail = false
    var refreshDelay = 0L
    var emitOnRefresh: ImmutableList<ForecastListItem>? = null

    override suspend fun refresh() {
        refreshCount++
        if (refreshDelay > 0) delay(refreshDelay)
        if (shouldFail) {
            if (refreshCount == 1) throw Exception("Initial fetch failed")
            else throw Exception("Refresh failed")
        }
        emitOnRefresh?.let { _weatherFlow.emit(it) }
    }

    suspend fun emit(items: ImmutableList<ForecastListItem>) {
        _weatherFlow.emit(items)
    }
}