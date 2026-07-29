package com.davismiyashiro.weathermapapp.presentation

import com.davismiyashiro.weathermapapp.data.dtos.Conditions
import com.davismiyashiro.weathermapapp.data.dtos.Main
import com.davismiyashiro.weathermapapp.data.dtos.Place
import com.davismiyashiro.weathermapapp.data.dtos.Weather
import com.davismiyashiro.weathermapapp.data.mappers.ForecastListItemMapper
import com.davismiyashiro.weathermapapp.data.network.FakeRepository
import com.davismiyashiro.weathermapapp.data.storage.UserPreferencesRepository
import com.davismiyashiro.weathermapapp.domain.ForecastListItem
import com.davismiyashiro.weathermapapp.presentation.ForecastListEvent.Refresh
import com.slack.circuit.test.test
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class ForecastListPresenterTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @get:Rule
    val mockitoRule: MockitoRule = MockitoJUnit.rule()

    private val repo = FakeRepository()
    private val userPrefs: UserPreferencesRepository = mock()

    private val place = Place()
    private lateinit var localForecastListItem: ImmutableList<ForecastListItem>

    @BeforeTest
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
        repo.emit(localForecastListItem)
        whenever(userPrefs.temperatureUnitFlow).thenReturn(flowOf(TEMPERATURE_DEFAULT))
        whenever(userPrefs.getTemperatureUnit()).thenReturn(TEMPERATURE_DEFAULT)

        val presenter = ForecastListPresenter(repo, userPrefs)

        presenter.test {
            // Might see Loading briefly while LaunchEffect runs
            var state = awaitItem()
            if (state is ForecastListState.Loading) {
                state = awaitItem()
            }

            assertTrue(
                state is ForecastListState.Success,
                "Expected Success state but was ${state::class.simpleName}"
            )
            assertEquals(localForecastListItem, state.forecastItems)
            assertFalse(state.isRefreshing)
            assertEquals(TEMPERATURE_DEFAULT, state.temperatureUnit)
        }

        assertEquals(1, repo.refreshCount)
    }

    @Test
    fun `refresh event updates refreshing state`() = runTest {
        repo.emit(localForecastListItem)
        repo.refreshDelay = 50
        whenever(userPrefs.temperatureUnitFlow).thenReturn(flowOf(TEMPERATURE_DEFAULT))
        whenever(userPrefs.getTemperatureUnit()).thenReturn(TEMPERATURE_DEFAULT)

        val presenter = ForecastListPresenter(repo, userPrefs)

        presenter.test {
            var state = awaitItem()
            if (state is ForecastListState.Loading) {
                state = awaitItem()
            }

            assertTrue(state is ForecastListState.Success)
            val successInitial = state
            assertFalse(successInitial.isRefreshing)

            successInitial.eventSink(Refresh)

            val refreshingState = awaitItem()
            assertTrue(refreshingState is ForecastListState.Success)
            assertTrue(
                refreshingState.isRefreshing,
                "Expected isRefreshing to be true"
            )

            val finishedState = awaitItem()
            assertTrue(finishedState is ForecastListState.Success)
            assertFalse(
                finishedState.isRefreshing,
                "Expected isRefreshing to be false"
            )
        }
    }

    @Test
    fun `initialization with error emits Error state`() = runTest {
        repo.shouldFail = true
        whenever(userPrefs.temperatureUnitFlow).thenReturn(flowOf(TEMPERATURE_DEFAULT))
        whenever(userPrefs.getTemperatureUnit()).thenReturn(TEMPERATURE_DEFAULT)

        val presenter = ForecastListPresenter(repo, userPrefs)

        presenter.test {
            var state = awaitItem()
            if (state is ForecastListState.Loading) {
                state = awaitItem()
            }

            assertTrue(
                state is ForecastListState.Error,
                "Expected Error state but was ${state::class.simpleName}"
            )
            assertEquals("Initial fetch failed", state.error.message)
        }
    }

    @Test
    fun `refresh event failure emits Error state`() = runTest {
        repo.emit(localForecastListItem)
        whenever(userPrefs.temperatureUnitFlow).thenReturn(flowOf(TEMPERATURE_DEFAULT))
        whenever(userPrefs.getTemperatureUnit()).thenReturn(TEMPERATURE_DEFAULT)

        val presenter = ForecastListPresenter(repo, userPrefs)

        presenter.test {
            var state = awaitItem()
            if (state is ForecastListState.Loading) {
                state = awaitItem()
            }

            assertTrue(state is ForecastListState.Success)
            val successInitial = state

            // Setup failure for the next refresh call
            repo.shouldFail = true
            repo.refreshDelay = 50

            successInitial.eventSink(Refresh)

            // Success(isRefreshing=true)
            val refreshingState = awaitItem()
            assertTrue((refreshingState as ForecastListState.Success).isRefreshing)

            // Transition to Error (it might be refreshing=true briefly)
            var errorState = awaitItem()
            if (errorState is ForecastListState.Success && errorState.isRefreshing) {
                errorState = awaitItem()
            }

            if (errorState is ForecastListState.Error && errorState.isRefreshing) {
                errorState = awaitItem()
            }

            assertTrue(
                errorState is ForecastListState.Error,
                "Expected Error state but was ${errorState::class.simpleName}"
            )
            assertEquals("Refresh failed", errorState.error.message)
            assertFalse(errorState.isRefreshing, "Expected isRefreshing to be false after error")
        }
    }

    @Test
    fun `initialization always triggers refresh`() = runTest {
        repo.emit(localForecastListItem)
        whenever(userPrefs.temperatureUnitFlow).thenReturn(flowOf(TEMPERATURE_DEFAULT))
        whenever(userPrefs.getTemperatureUnit()).thenReturn(TEMPERATURE_DEFAULT)

        val presenter = ForecastListPresenter(repo, userPrefs)

        presenter.test {
            var state = awaitItem()
            if (state is ForecastListState.Loading) {
                state = awaitItem()
            }

            assertTrue(
                state is ForecastListState.Success,
                "Expected Success state but was ${state::class.simpleName}"
            )
            assertEquals(localForecastListItem, state.forecastItems)
        }

        assertEquals(1, repo.refreshCount)
    }

    @Test
    fun `initialization offline triggers refresh and loads local data`() = runTest {
        whenever(userPrefs.temperatureUnitFlow).thenReturn(flowOf(TEMPERATURE_DEFAULT))
        whenever(userPrefs.getTemperatureUnit()).thenReturn(TEMPERATURE_DEFAULT)

        // Simulate repository loading local data when refresh() is called
        repo.emitOnRefresh = localForecastListItem

        val presenter = ForecastListPresenter(repo, userPrefs)

        presenter.test {
            // Should start in Loading
            val loadingState = awaitItem()
            assertTrue(loadingState is ForecastListState.Loading)

            // Once refresh() finishes and emits the local data, it should transition to Success
            val successState = awaitItem()
            assertTrue(successState is ForecastListState.Success)
            assertEquals(
                localForecastListItem,
                successState.forecastItems
            )
        }

        assertEquals(1, repo.refreshCount)
    }

}
