package com.davismiyashiro.weathermapapp.presentation

import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.InternalMavericksApi
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.mocking.MockBehavior
import com.airbnb.mvrx.test.MavericksTestRule
import com.airbnb.mvrx.withState
import com.davismiyashiro.weathermapapp.data.entities.Conditions
import com.davismiyashiro.weathermapapp.data.entities.Main
import com.davismiyashiro.weathermapapp.data.entities.Place
import com.davismiyashiro.weathermapapp.data.entities.Weather
import com.davismiyashiro.weathermapapp.domain.ForecastListItemEntity
import com.davismiyashiro.weathermapapp.domain.ForecastListItemMapper
import com.davismiyashiro.weathermapapp.domain.RepositoryInterface
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.rxjava3.core.Observable
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import java.io.IOException

@RunWith(MockitoJUnitRunner::class)
class ForecastListViewModelTest {

    private val repo = mock<RepositoryInterface>()
    private val mapper = ForecastListItemMapper()

    @get:Rule
    val mvrxRule = MavericksTestRule(
        true, MockBehavior(
            stateStoreBehavior = MockBehavior.StateStoreBehavior.Synchronous
        )
    )

    @get:Rule
    var mOverrideSchedulersRule = RxSchedulersOverrideRule()

    private var place = Place()

    @InternalMavericksApi
    lateinit var sut: ForecastListViewModel

    @InternalMavericksApi
    @Before
    fun setup() {
        val conditions = Conditions(
            dt = 10.toLong(),
            main = Main(temp = 32.0)
        )

        val listWeather = ArrayList<Weather>()
        listWeather.add(Weather(icon = "Whatever", main = "Ok"))
        conditions.weather = listWeather

        place.list = listOf(conditions)
    }

    @InternalMavericksApi
    @Test
    fun `given repo returns valid response, state SUCCESS receives the forecast list`() {
        whenever(repo.loadWeatherData()).thenReturn(Observable.just(place))
        val forecastListItemList = ForecastListItemMapper().mapPlaceToForecastListItem(place)

        sut = ForecastListViewModel(ForecastListState(), mapper, repo)

        withState(sut) { state ->
            assertTrue(state.forecastEntityList is Success<List<ForecastListItemEntity>>)
            assertEquals(
                forecastListItemList[0].main,
                state.forecastEntityList.invoke()?.get(0)?.main
            )
        }
    }

    @InternalMavericksApi
    @Test
    fun `given repo returns valid error, state FAIL is set`() {
        val exception = IOException("Random Error")
        whenever(repo.loadWeatherData()).thenReturn(Observable.error(exception))

        sut = ForecastListViewModel(ForecastListState(), mapper, repo)

        withState(sut) { state ->
            assertTrue(state.forecastEntityList is Fail)
            assertEquals(exception, (state.forecastEntityList as Fail).error)
        }
    }
}