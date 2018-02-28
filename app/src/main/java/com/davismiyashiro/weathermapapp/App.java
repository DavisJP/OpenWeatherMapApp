package com.davismiyashiro.weathermapapp;

import android.app.Application;

import com.davismiyashiro.weathermapapp.injection.ApplicationComponent;
import com.davismiyashiro.weathermapapp.injection.ApplicationModule;
import com.davismiyashiro.weathermapapp.injection.DaggerApplicationComponent;
import com.jakewharton.threetenabp.AndroidThreeTen;

import timber.log.Timber;

/**
 * Created by Davis Miyashiro on 12/12/2017.
 */

public class App extends Application {

    private ApplicationComponent component;

    @Override
    public void onCreate () {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        component = getComponent();

        AndroidThreeTen.init(this);
    }

    public ApplicationComponent getComponent () {
        if (component == null) {
            component = DaggerApplicationComponent.builder()
                    .applicationModule(new ApplicationModule(this))
                    .build();
        }
        return component;
    }
}
