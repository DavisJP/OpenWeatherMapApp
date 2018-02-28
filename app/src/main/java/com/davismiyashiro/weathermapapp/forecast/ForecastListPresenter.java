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

package com.davismiyashiro.weathermapapp.forecast;

import com.davismiyashiro.weathermapapp.model.data.Conditions;
import com.davismiyashiro.weathermapapp.model.data.Place;
import com.davismiyashiro.weathermapapp.model.ForecastRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by Davis Miyashiro.
 */
public class ForecastListPresenter implements ForecastListInterfaces.Presenter {

    private ForecastListInterfaces.View view;

    private CompositeDisposable disposable;

    @NonNull
    private ForecastRepository repo;

    @Inject
    public ForecastListPresenter(@NonNull final ForecastRepository repository) {
        repo = repository;
        disposable = new CompositeDisposable();
    }

    @Override
    public void attachView(@NonNull ForecastListInterfaces.View mainView) {
        view = mainView;
    }

    @Override
    public void dettachView () {
        if (disposable != null) {
            disposable.clear();
        }
        view = null;
    }

    @Override
    public void loadWeatherData (boolean refreshTasks) {

        if (refreshTasks) {
            repo.refreshData();
        }

        repo.loadWeatherData()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Place>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onNext(Place place) {
                        view.showForecastList(mapPlaceToForecastListItem(place));
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e.getMessage());
                        view.setSwipeRefresh(false);
                        view.showErrorMsg();
                    }

                    @Override
                    public void onComplete() {
                        view.setSwipeRefresh(false);
                    }
                });
    }

    List<ForecastListItem> mapPlaceToForecastListItem(Place data) {

        List<ForecastListItem> items = new ArrayList<>();

        if (data != null) {
            for (Conditions condition : data.getList()) {
                items.add(new ForecastListItem(condition));
            }
        }

        return items;
    }
}
