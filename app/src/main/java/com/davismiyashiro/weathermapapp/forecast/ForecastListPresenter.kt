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

package com.davismiyashiro.weathermapapp.forecast

import com.davismiyashiro.weathermapapp.model.RepositoryInterface
import com.davismiyashiro.weathermapapp.model.data.Place
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.annotations.NonNull
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.*
import javax.inject.Inject

/**
 * Created by Davis Miyashiro.
 */
class ForecastListPresenter @Inject
constructor(@param:NonNull @field:NonNull
            private val repo: RepositoryInterface) : ForecastListInterfaces.Presenter {

    private var view: ForecastListInterfaces.View? = null

    private val disposable: CompositeDisposable = CompositeDisposable()

    override fun attachView(@NonNull mainView: ForecastListInterfaces.View) {
        view = mainView
    }

    override fun dettachView() {
        disposable.clear()
        view = null
    }

    override fun loadWeatherData(refreshTasks: Boolean) {

        if (refreshTasks) {
            repo.refreshData()
        }

        repo.loadWeatherData()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<Place> {
                    override fun onSubscribe(d: Disposable) {
                        disposable.add(d)
                    }

                    override fun onNext(place: Place) {
                        view?.showForecastList(mapPlaceToForecastListItem(place))
                    }

                    override fun onError(e: Throwable) {
                        Timber.e(e, e.message)
                        view?.setSwipeRefresh(false)
                        view?.showErrorMsg()
                    }

                    override fun onComplete() {
                        view?.setSwipeRefresh(false)
                    }
                })
    }

    fun mapPlaceToForecastListItem(data: Place?): List<ForecastListItem> {

        val items = ArrayList<ForecastListItem>()

        data?.let {
            for (condition in it.list!!) {
                items.add(ForecastListItem(condition))
            }
        }

        return items
    }
}
