package com.davismiyashiro.weathermapapp.presentation

import com.davismiyashiro.weathermapapp.data.dtos.Conditions
import com.davismiyashiro.weathermapapp.data.dtos.Main
import com.davismiyashiro.weathermapapp.data.dtos.Place
import com.davismiyashiro.weathermapapp.data.dtos.Weather
import com.davismiyashiro.weathermapapp.data.mappers.ForecastListItemMapper
import com.davismiyashiro.weathermapapp.data.storage.UserPreferencesRepository
import com.davismiyashiro.weathermapapp.domain.ForecastListItem
import com.davismiyashiro.weathermapapp.domain.Repository
import com.davismiyashiro.weathermapapp.presentation.ForecastListEvent.Refresh
import com.slack.circuit.test.test
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
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

@ExperimentalCoroutinesApi
class ForecastListPresenterTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @get:Rule
    val mockitoRule: MockitoRule = MockitoJUnit.rule()

    private val repo: Repository = mock()
    private val userPrefs: UserPreferencesRepository = mock()

    private val place = Place()
    private lateinit var localForecastListItem: ImmutableList<ForecastListItem>

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
        localForecastListItem = ForecastListItemMapper().mapPlaceToForecastListItem(place)
    }

    @Test
    fun `presenter starts loading then emits success`() = runTest {
        whenever(repo.weatherFlow).thenReturn(
            flowOf(
                persistentListOf<ForecastListItem>(),
                localForecastListItem
            )
        )
        whenever(userPrefs.temperatureUnitFlow).thenReturn(flowOf(TEMPERATURE_DEFAULT))
        whenever(userPrefs.getTemperatureUnit()).thenReturn(TEMPERATURE_DEFAULT)

        val presenter = ForecastListPresenter(repo, userPrefs)

        presenter.test {
            // Initial state from repo.weatherFlow (empty list)
            val loadingState = awaitItem()
            assertTrue(
                "Expected Loading state but was ${loadingState::class.simpleName}",
                loadingState is ForecastListState.Loading
            )
            assertEquals(TEMPERATURE_DEFAULT, loadingState.temperatureUnit)

            // Second state from repo.weatherFlow (with items)
            val successState = awaitItem()
            assertTrue(
                "Expected Success state but was ${successState::class.simpleName}",
                successState is ForecastListState.Success
            )
            assertEquals(
                localForecastListItem,
                (successState as ForecastListState.Success).forecastItems
            )
            assertFalse(successState.isRefreshing)
            assertEquals(TEMPERATURE_DEFAULT, successState.temperatureUnit)
        }
    }

    @Test
    fun `refresh event updates refreshing state`() = runTest {
        val weatherFlow = MutableSharedFlow<ImmutableList<ForecastListItem>>(replay = 1)
        weatherFlow.emit(localForecastListItem)
        whenever(repo.weatherFlow).thenReturn(weatherFlow)
        whenever(userPrefs.temperatureUnitFlow).thenReturn(flowOf(TEMPERATURE_DEFAULT))
        whenever(userPrefs.getTemperatureUnit()).thenReturn(TEMPERATURE_DEFAULT)

        // Mock refresh to suspend for a bit so we can observe the isRefreshing=true state
        whenever(repo.refresh()).thenAnswer { runBlocking { delay(50) } }

        val presenter = ForecastListPresenter(repo, userPrefs)

        presenter.test {
            // Might see Loading first if LaunchedEffect hasn't completed
            var currentState = awaitItem()
            if (currentState is ForecastListState.Loading) {
                currentState = awaitItem()
            }
            
            assertTrue(currentState is ForecastListState.Success)
            val successInitial = currentState as ForecastListState.Success
            assertFalse(successInitial.isRefreshing)

            successInitial.eventSink(Refresh)

            val refreshingState = awaitItem()
            assertTrue(refreshingState is ForecastListState.Success)
            assertTrue("Expected isRefreshing to be true", (refreshingState as ForecastListState.Success).isRefreshing)

            val finishedState = awaitItem()
            assertTrue(finishedState is ForecastListState.Success)
            assertFalse("Expected isRefreshing to be false", (finishedState as ForecastListState.Success).isRefreshing)
        }
    }
}
