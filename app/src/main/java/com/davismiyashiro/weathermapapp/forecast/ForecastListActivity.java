package com.davismiyashiro.weathermapapp.forecast;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.davismiyashiro.weathermapapp.App;
import com.davismiyashiro.weathermapapp.R;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Davis Miyashiro.
 */

public class ForecastListActivity extends AppCompatActivity implements
        ForecastListInterfaces.View,
        SharedPreferences.OnSharedPreferenceChangeListener {

    final public static String TEMPERATURE_KEY = "TEMPERATURE_KEY";
    final public static String RECYCLER_STATE = "RECYCLER_STATE";

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.recycler_weather_list) RecyclerView recycler;
    @BindView(R.id.error_message_display) TextView errorMsg;
    @BindView(R.id.content_main_swipe_refresh_layout) SwipeRefreshLayout swipeLayout;

    @Inject
    ForecastListPresenter presenter;

    ForecastListAdapter adapter;

    private Parcelable savedState;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.open_weather_map);

        //Listening to changes on Temperature Units
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);

        ((App) getApplication()).getComponent().inject(this);

        adapter = new ForecastListAdapter(this);

        recycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recycler.setHasFixedSize(true);
        recycler.setAdapter(adapter);

        if (savedInstanceState != null) {
            savedState = savedInstanceState.getParcelable(RECYCLER_STATE);
        }

        swipeLayout.setOnRefreshListener(() -> {
            presenter.loadWeatherData(true);
            savedState = null;
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(RECYCLER_STATE, recycler.getLayoutManager().onSaveInstanceState());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();

        presenter.attachView(this);

        setSwipeRefresh(true);

        presenter.loadWeatherData(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int menuSelected = item.getItemId();
        switch (menuSelected) {
            case R.id.action_settings:
                showTemperatureOptions();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setSwipeRefresh (boolean value) {
        swipeLayout.setRefreshing(value);
    }

    private void showTemperatureOptions() {
        DialogFragment temperatureDialog = ForecastSettingsFragmentDialog.newInstance();
        temperatureDialog.show(getSupportFragmentManager(), "fragDialog");
    }

    @Override
    public void showErrorMsg () {
        errorMsg.setVisibility(View.VISIBLE);
        recycler.setVisibility(View.GONE);
    }

    @Override
    public void showForecastList(List<ForecastListItem> items) {
        adapter.replaceData(items);
        errorMsg.setVisibility(View.GONE);
        recycler.setVisibility(View.VISIBLE);

        if (savedState != null) {
            recycler.getLayoutManager().onRestoreInstanceState(savedState);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.dettachView();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(TEMPERATURE_KEY)){
            adapter.notifyDataSetChanged();
        }
    }
}
