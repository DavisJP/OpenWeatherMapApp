package com.davismiyashiro.weathermapapp.presentation

import android.content.SharedPreferences
import android.os.Bundle
import android.os.Parcelable
import android.preference.PreferenceManager
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.activityViewModel
import com.davismiyashiro.weathermapapp.R
import com.davismiyashiro.weathermapapp.databinding.FragmentForecastListBinding
import com.davismiyashiro.weathermapapp.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

const val TEMPERATURE_KEY = "TEMPERATURE_KEY"
const val RECYCLER_STATE = "RECYCLER_STATE"

@AndroidEntryPoint
class ForecastListFragment : Fragment(R.layout.fragment_forecast_list),
    MavericksView,
    ForecastListInterfaces.View,
    SharedPreferences.OnSharedPreferenceChangeListener {

    private val forecastListViewModel: ForecastListViewModel by activityViewModel()

    @Inject
    internal lateinit var presenter: ForecastListPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    private lateinit var adapter: ForecastListAdapter

    private var savedState: Parcelable? = null

    private val binding: FragmentForecastListBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Listening to changes on Temperature Units
        PreferenceManager.getDefaultSharedPreferences(requireContext()).registerOnSharedPreferenceChangeListener(this)

        adapter = ForecastListAdapter(requireContext())

        binding.recyclerWeatherList.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.recyclerWeatherList.setHasFixedSize(true)
        binding.recyclerWeatherList.adapter = adapter

        if (savedInstanceState != null) {
            savedState = savedInstanceState.getParcelable(RECYCLER_STATE)
        }

        binding.contentMainSwipeRefreshLayout.setOnRefreshListener {
            presenter.loadWeatherData(true)
            savedState = null
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(RECYCLER_STATE, binding.recyclerWeatherList.layoutManager?.onSaveInstanceState())
        super.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()

        presenter.attachView(this)

        setSwipeRefresh(true)

        presenter.loadWeatherData(false)
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

    override fun setSwipeRefresh(value: Boolean) {
        binding.contentMainSwipeRefreshLayout.isRefreshing = value
    }

    private fun showTemperatureOptions() {
        val temperatureDialog = ForecastSettingsFragmentDialog.newInstance()
        temperatureDialog.show(parentFragmentManager, "fragDialog")
    }

    override fun showErrorMsg() {
        binding.errorMessageDisplay.visibility = View.VISIBLE
        binding.recyclerWeatherList.visibility = View.GONE
    }

    override fun showForecastList(items: List<ForecastListItem>) {
        adapter.replaceData(items)
        binding.errorMessageDisplay.visibility = View.GONE
        binding.recyclerWeatherList.visibility = View.VISIBLE

        if (savedState != null) {
            binding.recyclerWeatherList.layoutManager?.onRestoreInstanceState(savedState)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.dettachView()
        PreferenceManager.getDefaultSharedPreferences(requireContext()).unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (key == TEMPERATURE_KEY) {
            adapter.notifyDataSetChanged()
        }
    }

    override fun invalidate() {
        //TODO("Not yet implemented")
    }
}