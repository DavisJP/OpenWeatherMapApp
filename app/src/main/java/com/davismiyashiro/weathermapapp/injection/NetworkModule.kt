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
import android.net.ConnectivityManager

import com.davismiyashiro.weathermapapp.network.OpenWeatherApi

import java.io.File

import javax.inject.Singleton

import dagger.Module
import dagger.Provides
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Created by Davis Miyashiro.
 */

@Module
class NetworkModule {

    private val BASE_URL = "https://api.openweathermap.org/data/2.5/"
    private val APP_ID_PARAM = "appid"
    private val APP_ID = "3e29cf11d4eabe8eba6cf25d535eaac2"

    @Provides
    @Singleton
    fun provideOkHttpClient(application: Application): OkHttpClient {

        val httpCacheDirectory = File(application.cacheDir, "responses")
        val cacheSize = 10 * 1024 * 1024 //10MB
        val cache = Cache(httpCacheDirectory, cacheSize.toLong())

        val urlBuilder = Interceptor { chain ->
            chain.proceed(chain.request()
                    .newBuilder()
                    .url(chain.request()
                            .url
                            .newBuilder()
                            .addQueryParameter(APP_ID_PARAM, APP_ID)
                            .build())
                    .build())
        }

        return OkHttpClient.Builder()
                .addNetworkInterceptor { chain ->
                    val originalResponse: Response = chain.proceed(chain.request())
                    addHttpClientHeader(application, originalResponse)
                }
                .addInterceptor(urlBuilder)
                .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
                .cache(cache)
                .build()
    }

    private fun addHttpClientHeader(application: Application, originalResponse: Response): Response {
        return if (isOnline(application)) {
            val maxAge = 60 // read from cache for 1 minute
            originalResponse.newBuilder()
                    .header("Cache-Control", "public, max-age=$maxAge")
                    .build()
        } else {//TODO: Check API to use right max-age and max-stale
            val maxStale = 60 * 60 * 24 * 28 // tolerate 4-weeks stale
            originalResponse.newBuilder()
                    .header("Cache-Control", "public, only-if-cached, max-stale=$maxStale")
                    .build()
        }
    }

    private fun isOnline(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.activeNetworkInfo
        return netInfo != null && netInfo.isConnectedOrConnecting
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .client(client)
                .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): OpenWeatherApi {
        return retrofit.create(OpenWeatherApi::class.java)
    }
}