package com.davismiyashiro.weathermapapp.injection;

import com.davismiyashiro.weathermapapp.forecast.ForecastListActivity;
import com.davismiyashiro.weathermapapp.model.ForecastRepository;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by Davis Miyashiro.
 */

@Singleton
@Component (modules = {ApplicationModule.class, NetworkModule.class} )
public interface ApplicationComponent {
    void inject (ForecastListActivity activity);
    void inject (ForecastRepository repository);
}
