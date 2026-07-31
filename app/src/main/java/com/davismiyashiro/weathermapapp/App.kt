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

package com.davismiyashiro.weathermapapp

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.network.ktor3.KtorNetworkFetcherFactory
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import com.davismiyashiro.weathermapapp.injection.AppGraph
import com.jakewharton.threetenabp.AndroidThreeTen
import dev.zacsweers.metro.createGraphFactory
import okio.Path.Companion.toPath

/**
 * Created by Davis Miyashiro on 12/12/2017.
 */

open class App : Application(), SingletonImageLoader.Factory {

    lateinit var appGraph: AppGraph

    @OptIn(ExperimentalCoilApi::class)
    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context, 0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache").path.toPath())
                    .maxSizePercent(0.02) // 2% of disk space
                    .build()
            }
            .components {
                add(KtorNetworkFetcherFactory(appGraph.httpClient))
            }
            .build()
    }

    override fun onCreate() {
        super.onCreate()

        setupLogging()

        appGraph = createGraph()

        AndroidThreeTen.init(this)
    }

    private fun setupLogging() {
        if (!BuildConfig.DEBUG) {
            Logger.setMinSeverity(Severity.Error)
        }
    }

    open fun createGraph(): AppGraph {
        return createGraphFactory<AppGraph.Factory>().create(this)
    }
}
