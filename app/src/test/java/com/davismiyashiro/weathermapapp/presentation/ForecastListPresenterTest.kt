package com.davismiyashiro.weathermapapp.presentation

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.davismiyashiro.weathermapapp.data.entities.Conditions
import com.davismiyashiro.weathermapapp.data.entities.Main
import com.davismiyashiro.weathermapapp.data.entities.Place
import com.davismiyashiro.weathermapapp.data.entities.Weather
import com.davismiyashiro.weathermapapp.domain.ForecastListItemMapper
import com.davismiyashiro.weathermapapp.domain.Repository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.IOException

@ExperimentalCoroutinesApi
class ForecastListPresenterTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @get:Rule
    val mockitoRule: MockitoRule = MockitoJUnit.rule()

    private val repo: Repository = mock()
    private val mapper = ForecastListItemMapper()
    private var events = MutableSharedFlow<ForecastListEvent>()

    private val place = Place()

    @Before
    fun setup() {
        place.list = listOf(
            Conditions(
                dt = 10.toLong(),
                main = Main(temp = 32.0),
                weather = listOf(
                    Weather(icon = "Whatever", main = "Ok"),
                ),
            ),
        )
    }

    @Test
    fun `presenter starts loading then emits success`() = runTest {
        whenever(repo.loadWeatherData()).thenReturn(flowOf(place))
        val forecastListItemList = ForecastListItemMapper().mapPlaceToForecastListItem(place)

        moleculeFlow(RecompositionMode.Immediate) {
            forecastListPresenter(
                repo = repo,
                mapper = mapper,
                temperatureUnit = TEMPERATURE_DEFAULT,
                onTemperatureUnitSelect = {},
                events = events,
            )
        }.test {
            val loadingState = awaitItem()
            assertTrue(loadingState is ForecastListState.Loading)
            assertEquals(TEMPERATURE_DEFAULT, loadingState.temperatureUnit)

            val successState = awaitItem()
            assertTrue(successState is ForecastListState.Success)
            assertEquals(
                forecastListItemList,
                (successState as ForecastListState.Success).forecastItems
            )
            assertFalse(successState.isRefreshing)
            assertEquals(TEMPERATURE_DEFAULT, successState.temperatureUnit)
        }
    }

    @Test
    fun `presenter emits error when repository fails`() = runTest {
        val exception = IOException("Network error")
        whenever(repo.loadWeatherData()).thenReturn(flow { throw exception })

        moleculeFlow(RecompositionMode.Immediate) {
            forecastListPresenter(
                repo = repo,
                mapper = mapper,
                temperatureUnit = TEMPERATURE_DEFAULT,
                onTemperatureUnitSelect = {},
                events = events,
            )
        }.test {
            val loadingState = awaitItem()
            assertTrue(loadingState is ForecastListState.Loading)
            assertEquals(TEMPERATURE_DEFAULT, loadingState.temperatureUnit)

            val errorState = awaitItem()
            assertTrue(errorState is ForecastListState.Error)
            assertFalse((errorState as ForecastListState.Error).isRefreshing)
            assertEquals(TEMPERATURE_DEFAULT, loadingState.temperatureUnit)
            assertEquals(exception, errorState.error)
        }
    }
}
