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

package com.davismiyashiro.weathermapapp.injection

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.davismiyashiro.weathermapapp.data.mappers.ForecastListItemMapper
import com.davismiyashiro.weathermapapp.data.network.AndroidNetworkConnectivity
import com.davismiyashiro.weathermapapp.data.network.OpenWeatherApi
import com.davismiyashiro.weathermapapp.data.network.OpenWeatherApiImpl
import com.davismiyashiro.weathermapapp.data.storage.ForecastLocalRepository
import com.davismiyashiro.weathermapapp.domain.LocalRepository
import com.davismiyashiro.weathermapapp.domain.NetworkConnectivity
import com.davismiyashiro.weathermapapp.domain.Storage
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.encodedPath
import io.ktor.http.takeFrom
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import co.touchlab.kermit.Logger as KermitLogger

private const val API_HOST = "api.openweathermap.org"
private const val BASE_URL = "https://$API_HOST/data/2.5/"
private const val APP_ID_PARAM = "appid"
private const val APP_ID = "3e29cf11d4eabe8eba6cf25d535eaac2"
private const val DATASTORE_NAME = "weather_settings"

@BindingContainer
@ContributesTo(AppScope::class)
object NetworkModule {

    @Provides
    fun provideContext(application: Application): Context = application

    @Provides
    @SingleInApp
    fun provideHttpClient(): HttpClient {
        return HttpClient(Android) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        KermitLogger.withTag("HttpClient").d(message)
                    }
                }
                level = LogLevel.BODY
                filter { request ->
                    request.url.host == API_HOST
                }
            }
            install(HttpCache)
            defaultRequest {
                if (url.host.isEmpty() || url.host == API_HOST) {
                    val originalPath = url.encodedPath
                    url.takeFrom(BASE_URL)
                    url.encodedPath =
                        (url.encodedPath.removeSuffix("/") + "/" + originalPath.removePrefix("/")).replace(
                            "//",
                            "/"
                        )
                    url.parameters.append(APP_ID_PARAM, APP_ID)
                }
            }
        }
    }

    @Provides
    @SingleInApp
    fun provideNetworkConnectivity(application: Application): NetworkConnectivity {
        return AndroidNetworkConnectivity(application)
    }

    @Provides
    @SingleInApp
    fun provideApiService(client: HttpClient): OpenWeatherApi {
        return OpenWeatherApiImpl(client)
    }

    @Provides
    @SingleInApp
    fun provideDataStore(application: Application): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            produceFile = { application.preferencesDataStoreFile(DATASTORE_NAME) }
        )
    }

    @Provides
    @SingleInApp
    fun provideLocalRepository(storage: Storage): LocalRepository {
        return ForecastLocalRepository(
            storage,
            ForecastListItemMapper(),
        )
    }
}
