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
