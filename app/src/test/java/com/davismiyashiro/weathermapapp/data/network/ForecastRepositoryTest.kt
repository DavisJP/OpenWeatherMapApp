/*
 * MIT License
 *
 * Copyright (c) 2018 Davis Miyashiro
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.davismiyashiro.weathermapapp.data.network

import app.cash.turbine.test
import com.davismiyashiro.weathermapapp.data.dtos.City
import com.davismiyashiro.weathermapapp.data.dtos.Conditions
import com.davismiyashiro.weathermapapp.data.dtos.Place
import com.davismiyashiro.weathermapapp.data.mappers.ForecastListItemMapper
import com.davismiyashiro.weathermapapp.domain.ForecastListItem
import com.davismiyashiro.weathermapapp.domain.LocalRepository
import com.davismiyashiro.weathermapapp.domain.NetworkConnectivity
import com.davismiyashiro.weathermapapp.domain.Repository
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import dev.mokkery.verify.VerifyMode.Companion.not
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import java.io.IOException
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ForecastRepositoryTest {

    private lateinit var repository: Repository

    private val openWeatherApi: OpenWeatherApi = mock()
    private val localRepository: LocalRepository = mock()
    private val networkConnectivity: NetworkConnectivity = mock()
    private lateinit var remotePlace: Place
    private lateinit var localPlace: Place
    private lateinit var localForecastListItem: ImmutableList<ForecastListItem>
    private val json = Json { ignoreUnknownKeys = true }

    @BeforeTest
    fun setUp() {
        remotePlace = Place("remote", 10.0, 1, mutableListOf(Conditions()), City())
        localPlace = Place("local", 10.0, 1, mutableListOf(Conditions()), City())
        localForecastListItem = ForecastListItemMapper().mapPlaceToForecastListItem(localPlace)
        repository = ForecastRepository(
            openWeatherApi,
            localRepository,
            ForecastListItemMapper(),
            networkConnectivity
        )
    }

    @Test
    fun `refresh online remote succeeds updates weatherFlow and local storage`() = runTest {
        every { networkConnectivity.isOnline() } returns true
        everySuspend { openWeatherApi.getForecastById(any()) } returns remotePlace
        everySuspend { localRepository.storeData(any()) } returns Unit

        repository.weatherFlow.test {
            assertEquals(persistentListOf<ForecastListItem>(), awaitItem()) // Initial state

            repository.refresh()

            assertEquals(localForecastListItem, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        verifySuspend { openWeatherApi.getForecastById(any()) }
        verifySuspend { localRepository.storeData(json.encodeToString(remotePlace)) }
        verifySuspend(not) { localRepository.loadData() }
    }

    @Test
    fun `refresh offline loads from local storage`() = runTest {
        every { networkConnectivity.isOnline() } returns false
        everySuspend { localRepository.loadData() } returns flowOf(localForecastListItem)

        repository.weatherFlow.test {
            assertEquals(persistentListOf(), awaitItem()) // Initial state

            repository.refresh()

            assertEquals(localForecastListItem, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        verifySuspend(not) { openWeatherApi.getForecastById(any()) }
        verifySuspend { localRepository.loadData() }
        verifySuspend(not) { localRepository.storeData(any()) }
    }

    @Test
    fun `refresh online remote fails propagates exception`() = runTest {
        every { networkConnectivity.isOnline() } returns true
        val remoteException = IOException("Remote error")
        everySuspend { openWeatherApi.getForecastById(any()) } throws remoteException

        assertFailsWith<IOException> {
            repository.refresh()
        }
        
        verifySuspend { openWeatherApi.getForecastById(any()) }
        verifySuspend(not) { localRepository.loadData() }
        verifySuspend(not) { localRepository.storeData(any()) }
    }

    @Test
    fun `refresh online client error propagates exception`() = runTest {
        every { networkConnectivity.isOnline() } returns true
        
        val clientException = createClientRequestException(HttpStatusCode.Unauthorized)
        everySuspend { openWeatherApi.getForecastById(any()) } throws clientException

        assertFailsWith<ClientRequestException> {
            repository.refresh()
        }

        verifySuspend { openWeatherApi.getForecastById(any()) }
        verifySuspend(not) { localRepository.loadData() }
        verifySuspend(not) { localRepository.storeData(any()) }
    }

    @Test
    fun `refresh online server error propagates exception`() = runTest {
        every { networkConnectivity.isOnline() } returns true
        
        val serverException = createServerResponseException(HttpStatusCode.InternalServerError)
        everySuspend { openWeatherApi.getForecastById(any()) } throws serverException

        assertFailsWith<ServerResponseException> {
            repository.refresh()
        }

        verifySuspend { openWeatherApi.getForecastById(any()) }
        verifySuspend(not) { localRepository.loadData() }
        verifySuspend(not) { localRepository.storeData(any()) }
    }

    private suspend fun createClientRequestException(status: HttpStatusCode): ClientRequestException {
        val engine = MockEngine { respond("", status) }
        val httpClient = HttpClient(engine) {
            expectSuccess = true
        }
        return try {
            httpClient.get("https://localhost")
            throw IllegalStateException("Should have thrown ClientRequestException")
        } catch (e: ClientRequestException) {
            e
        }
    }

    private suspend fun createServerResponseException(status: HttpStatusCode): ServerResponseException {
        val engine = MockEngine { respond("", status) }
        val httpClient = HttpClient(engine) {
            expectSuccess = true
        }
        return try {
            httpClient.get("https://localhost")
            throw IllegalStateException("Should have thrown ServerResponseException")
        } catch (e: ServerResponseException) {
            e
        }
    }
}
