package com.davismiyashiro.weathermapapp.presentation

import android.content.SharedPreferences
import android.os.Bundle
import android.os.Parcelable
import android.preference.PreferenceManager
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.mvrx.*
import com.davismiyashiro.weathermapapp.R
import com.davismiyashiro.weathermapapp.databinding.FragmentForecastListBinding
import com.davismiyashiro.weathermapapp.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint

const val TEMPERATURE_KEY = "TEMPERATURE_KEY"
const val RECYCLER_STATE = "RECYCLER_STATE"

@AndroidEntryPoint
class ForecastListFragment : Fragment(R.layout.fragment_forecast_list),
    MavericksView,
    SharedPreferences.OnSharedPreferenceChangeListener {

    @InternalMavericksApi
    private val forecastListViewModel: ForecastListViewModel by fragmentViewModel(keyFactory = { "test" })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    private lateinit var adapter: ForecastListAdapter

    private var savedState: Parcelable? = null

    private val binding: FragmentForecastListBinding by viewBinding()

    @InternalMavericksApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Listening to changes on Temperature Units
        PreferenceManager.getDefaultSharedPreferences(requireContext())
            .registerOnSharedPreferenceChangeListener(this)

        adapter = ForecastListAdapter(requireContext())

        binding.recyclerWeatherList.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.recyclerWeatherList.setHasFixedSize(true)
        binding.recyclerWeatherList.adapter = adapter

        if (savedInstanceState != null) {
            savedState = savedInstanceState.getParcelable(RECYCLER_STATE)
        }

        binding.contentMainSwipeRefreshLayout.setOnRefreshListener {
            forecastListViewModel.loadWeatherData(true)
            savedState = null
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(
            RECYCLER_STATE,
            binding.recyclerWeatherList.layoutManager?.onSaveInstanceState()
        )
        super.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()
        setSwipeRefresh(true)
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_main, menu)
    }

    private fun setSwipeRefresh(value: Boolean) {
        binding.contentMainSwipeRefreshLayout.isRefreshing = value
    }

    private fun showTemperatureOptions() {
        val temperatureDialog = ForecastSettingsFragmentDialog.newInstance()
        temperatureDialog.show(parentFragmentManager, "fragDialog")
    }

    private fun showErrorMsg() {
        binding.errorMessageDisplay.visibility = View.VISIBLE
        binding.recyclerWeatherList.visibility = View.GONE
    }

    private fun showForecastList(items: List<ForecastListItem>) {
        adapter.replaceData(items)
        binding.errorMessageDisplay.visibility = View.GONE
        binding.recyclerWeatherList.visibility = View.VISIBLE

        if (savedState != null) {
            binding.recyclerWeatherList.layoutManager?.onRestoreInstanceState(savedState)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        PreferenceManager.getDefaultSharedPreferences(requireContext())
            .unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (key == TEMPERATURE_KEY) {
            adapter.notifyDataSetChanged()
        }
    }

    @InternalMavericksApi
    override fun invalidate() {
        withState(forecastListViewModel) { state ->
            when (state.forecast) {
                is Loading -> {
                    setSwipeRefresh(true)
                }
                is Success -> {
                    setSwipeRefresh(false)
                    showForecastList(state.forecast.invoke())
                }
                is Fail -> {
                    setSwipeRefresh(false)
                    showErrorMsg()
                }
                is Uninitialized -> setSwipeRefresh(true)
            }
        }
    }
}