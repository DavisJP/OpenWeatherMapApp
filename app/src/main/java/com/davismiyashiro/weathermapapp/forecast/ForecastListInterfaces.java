package com.davismiyashiro.weathermapapp.forecast;

import java.util.List;
import io.reactivex.annotations.NonNull;

/**
 * Created by Davis Miyashiro.
 */

public interface ForecastListInterfaces {

    interface View {
        void showForecastList(List<ForecastListItem> item);
        void showErrorMsg ();
        void setSwipeRefresh (boolean value);
    }

    interface Presenter {
        void attachView (@NonNull ForecastListInterfaces.View mainView);
        void dettachView ();
        void loadWeatherData (boolean refreshData);
    }
}
