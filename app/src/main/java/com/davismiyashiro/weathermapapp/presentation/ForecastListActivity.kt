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

package com.davismiyashiro.weathermapapp.presentation

import android.content.SharedPreferences
import android.os.Bundle
import android.os.Parcelable
import android.preference.PreferenceManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.davismiyashiro.weathermapapp.R
import com.davismiyashiro.weathermapapp.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Created by Davis Miyashiro.
 */
const val TEMPERATURE_KEY = "TEMPERATURE_KEY"
const val RECYCLER_STATE = "RECYCLER_STATE"

@AndroidEntryPoint
class ForecastListActivity : AppCompatActivity(), ForecastListInterfaces.View, SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject
    internal lateinit var presenter: ForecastListPresenter

    private lateinit var adapter: ForecastListAdapter

    private var savedState: Parcelable? = null

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        setSupportActionBar(binding.toolbar)
        supportActionBar?.setTitle(R.string.open_weather_map)

        //Listening to changes on Temperature Units
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this)

        adapter = ForecastListAdapter(this)

        binding.content.recyclerWeatherList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.content.recyclerWeatherList.setHasFixedSize(true)
        binding.content.recyclerWeatherList.adapter = adapter

        if (savedInstanceState != null) {
            savedState = savedInstanceState.getParcelable(RECYCLER_STATE)
        }

        binding.content.contentMainSwipeRefreshLayout.setOnRefreshListener {
            presenter.loadWeatherData(true)
            savedState = null
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(RECYCLER_STATE, binding.content.recyclerWeatherList.layoutManager?.onSaveInstanceState())
        super.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()

        presenter.attachView(this)

        setSwipeRefresh(true)

        presenter.loadWeatherData(false)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> {
                showTemperatureOptions()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun setSwipeRefresh(value: Boolean) {
        binding.content.contentMainSwipeRefreshLayout.isRefreshing = value
    }

    private fun showTemperatureOptions() {
        val temperatureDialog = ForecastSettingsFragmentDialog.newInstance()
        temperatureDialog.show(supportFragmentManager, "fragDialog")
    }

    override fun showErrorMsg() {
        binding.content.errorMessageDisplay.visibility = View.VISIBLE
        binding.content.recyclerWeatherList.visibility = View.GONE
    }

    override fun showForecastList(items: List<ForecastListItem>) {
        adapter.replaceData(items)
        binding.content.errorMessageDisplay.visibility = View.GONE
        binding.content.recyclerWeatherList.visibility = View.VISIBLE

        if (savedState != null) {
            binding.content.recyclerWeatherList.layoutManager?.onRestoreInstanceState(savedState)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.dettachView()
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (key == TEMPERATURE_KEY) {
            adapter.notifyDataSetChanged()
        }
    }
}
