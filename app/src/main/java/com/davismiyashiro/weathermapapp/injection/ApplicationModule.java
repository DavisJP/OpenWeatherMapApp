package com.davismiyashiro.weathermapapp.injection;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.davismiyashiro.weathermapapp.model.ForecastLocalRepository;
import com.davismiyashiro.weathermapapp.model.Repository;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Davis Miyashiro.
 */

@Module
public class ApplicationModule {
    private Application application;

    public ApplicationModule (Application app) {
        application = app;
    }

    @Provides
    public Application provideApplication () {
        return application;
    }

    @Provides
    @ApplicationContext
    Context provideContext() {
        return application;
    }

    @Provides
    public SharedPreferences provideDefaultSharedPref () {
        return PreferenceManager.getDefaultSharedPreferences(application);
    }

    @Provides
    @Singleton
    public Repository provideLocalRepository () {
        return new ForecastLocalRepository(provideDefaultSharedPref());
    }
}
