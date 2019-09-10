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

import android.content.SharedPreferences
import android.os.Bundle
import android.os.Parcelable
import android.preference.PreferenceManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindView
import butterknife.ButterKnife
import com.davismiyashiro.weathermapapp.App
import com.davismiyashiro.weathermapapp.R
import javax.inject.Inject

/**
 * Created by Davis Miyashiro.
 */
const val TEMPERATURE_KEY = "TEMPERATURE_KEY"
const val RECYCLER_STATE = "RECYCLER_STATE"

class ForecastListActivity : AppCompatActivity(), ForecastListInterfaces.View, SharedPreferences.OnSharedPreferenceChangeListener {

    @BindView(R.id.toolbar)
    @JvmField
    internal var toolbar: Toolbar? = null
    @BindView(R.id.recycler_weather_list)
    @JvmField
    internal var recycler: RecyclerView? = null
    @BindView(R.id.error_message_display)
    @JvmField
    internal var errorMsg: TextView? = null
    @BindView(R.id.content_main_swipe_refresh_layout)
    @JvmField
    internal var swipeLayout: SwipeRefreshLayout? = null

    @Inject
    internal lateinit var presenter: ForecastListPresenter

    private lateinit var adapter: ForecastListAdapter

    private var savedState: Parcelable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ButterKnife.bind(this)

        setSupportActionBar(toolbar)
        supportActionBar?.setTitle(R.string.open_weather_map)

        //Listening to changes on Temperature Units
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this)

        (application as App).getComponent()?.inject(this)

        adapter = ForecastListAdapter(this)

        recycler?.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recycler?.setHasFixedSize(true)
        recycler?.adapter = adapter

        if (savedInstanceState != null) {
            savedState = savedInstanceState.getParcelable(RECYCLER_STATE)
        }

        swipeLayout?.setOnRefreshListener {
            presenter.loadWeatherData(true)
            savedState = null
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(RECYCLER_STATE, recycler!!.layoutManager!!.onSaveInstanceState())
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
        swipeLayout?.isRefreshing = value
    }

    private fun showTemperatureOptions() {
        val temperatureDialog = ForecastSettingsFragmentDialog.newInstance()
        temperatureDialog.show(supportFragmentManager, "fragDialog")
    }

    override fun showErrorMsg() {
        errorMsg?.visibility = View.VISIBLE
        recycler?.visibility = View.GONE
    }

    override fun showForecastList(items: List<ForecastListItem>) {
        adapter.replaceData(items)
        errorMsg?.visibility = View.GONE
        recycler?.visibility = View.VISIBLE

        if (savedState != null) {
            recycler?.layoutManager?.onRestoreInstanceState(savedState)
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
