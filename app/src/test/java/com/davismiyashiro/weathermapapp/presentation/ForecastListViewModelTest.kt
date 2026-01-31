package com.davismiyashiro.weathermapapp.presentation

import android.os.Build
import app.cash.turbine.test
import com.davismiyashiro.weathermapapp.data.entities.Conditions
import com.davismiyashiro.weathermapapp.data.entities.Main
import com.davismiyashiro.weathermapapp.data.entities.Place
import com.davismiyashiro.weathermapapp.data.entities.Weather
import com.davismiyashiro.weathermapapp.domain.ForecastListItemMapper
import com.davismiyashiro.weathermapapp.domain.Repository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class ForecastListViewModelTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @get:Rule
    val mockitoRule: MockitoRule = MockitoJUnit.rule()

    private val repo = mock<Repository>()
    private val mapper = ForecastListItemMapper()

    private lateinit var sut: ForecastListViewModel

    private val place = Place()

    @Before
    fun setup() {
        place.list = listOf(
            Conditions(
                dt = 10.toLong(),
                main = Main(temp = 32.0),
                weather = listOf(
                    Weather(icon = "Whatever", main = "Ok")
                )
            )
        )
    }

    @Test
    fun `given repo returns valid response, state SUCCESS receives the forecast list`() = runTest {
        whenever(repo.loadWeatherData()).thenReturn(flowOf(place))
        val forecastListItemList = ForecastListItemMapper().mapPlaceToForecastListItem(place)

        sut = ForecastListViewModel(repo, mapper)

        sut.state.test {
            val initialState = awaitItem()
            assertTrue(initialState.isLoading)
            assertTrue(initialState.forecastItems.isEmpty())
            assertNull(initialState.error)

            val finalState = awaitItem()
            assertFalse(finalState.isLoading)
            assertEquals(forecastListItemList, finalState.forecastItems)
            assertNull(finalState.error)
        }
    }

    @Test
    fun `given repo returns valid error, state FAIL is set`() = runTest {
        val exception = IOException("Random Error")
        whenever(repo.loadWeatherData()).thenReturn(flow { throw exception })

        sut = ForecastListViewModel(repo, mapper)

        sut.state.test {
            val initialState = awaitItem()
            assertTrue(initialState.isLoading)
            assertTrue(initialState.forecastItems.isEmpty())
            assertNull(initialState.error)

            val finalState = awaitItem()
            assertFalse(finalState.isLoading)
            assertTrue(finalState.forecastItems.isEmpty())
            assertEquals(exception, finalState.error)
        }
    }
}