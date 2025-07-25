package com.davismiyashiro.weathermapapp.presentation

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import coil3.compose.AsyncImage
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.InternalMavericksApi
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.Uninitialized
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import com.davismiyashiro.weathermapapp.R
import com.davismiyashiro.weathermapapp.data.entities.City
import com.davismiyashiro.weathermapapp.data.entities.Conditions
import com.davismiyashiro.weathermapapp.data.entities.Place
import com.davismiyashiro.weathermapapp.data.entities.Weather
import com.davismiyashiro.weathermapapp.databinding.FragmentForecastListBinding
import com.davismiyashiro.weathermapapp.designsystem.theme.AppTheme
import com.davismiyashiro.weathermapapp.domain.ForecastListItemEntity
import com.davismiyashiro.weathermapapp.domain.ForecastListItemMapper
import com.davismiyashiro.weathermapapp.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle

const val TEMPERATURE_KEY = "TEMPERATURE_KEY"
const val RECYCLER_STATE = "RECYCLER_STATE"
const val TEMPERATURE_DEFAULT = 0

@AndroidEntryPoint
class ForecastListFragment : Fragment(R.layout.fragment_forecast_list),
    MavericksView,
    SharedPreferences.OnSharedPreferenceChangeListener {

    @InternalMavericksApi
    private val forecastListViewModel: ForecastListViewModel by fragmentViewModel(keyFactory = { "test" })

    private var savedState: Parcelable? = null

    private val binding by viewBinding(FragmentForecastListBinding::bind)

    private var temperatureUnitState by mutableIntStateOf(TEMPERATURE_DEFAULT)

    private val temperatureUnitPref: Int
        get() = PreferenceManager.getDefaultSharedPreferences(requireContext())
            .getInt(TEMPERATURE_KEY, TEMPERATURE_DEFAULT)

    @InternalMavericksApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Listening to changes on Temperature Units
        temperatureUnitState = temperatureUnitPref

        PreferenceManager.getDefaultSharedPreferences(requireContext())
            .registerOnSharedPreferenceChangeListener(this)

//        ViewCompat.setOnApplyWindowInsetsListener(binding.recyclerWeatherList) { view, windowInsets ->
//            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
//
//            view.updatePadding(
//                left = insets.left,
//                right = insets.right,
//                bottom = insets.bottom
//            )

//            binding.recyclerWeatherList.clipToPadding = false
//            WindowInsetsCompat.CONSUMED
//        }

        if (savedInstanceState != null) {
            savedState = savedInstanceState.getParcelable(RECYCLER_STATE)
        }

//        binding.contentMainSwipeRefreshLayout.setOnRefreshListener {
        forecastListViewModel.loadWeatherData(true)
//            savedState = null
//        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
//        outState.putParcelable(
//            RECYCLER_STATE,
//            binding.recyclerWeatherList.layoutManager?.onSaveInstanceState()
//        )
        super.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()
        setSwipeRefresh(true)
    }

    private fun setSwipeRefresh(value: Boolean) {
//        binding.contentMainSwipeRefreshLayout.isRefreshing = value
    }

    private fun showTemperatureOptions() {
        val temperatureDialog = ForecastSettingsFragmentDialog.newInstance()
        temperatureDialog.show(parentFragmentManager, "fragDialog")
    }

    private fun showErrorMsg() {
        binding.composeView.setContent {
            AppTheme(dynamicColor = false) {
                Scaffold { padding ->
                    Text(
                        modifier = Modifier.padding(padding),
                        text =
                            resources.getString(R.string.please_check_your_network_status_or_try_again_later),
                    )
                }
            }
        }
    }

    private fun showForecastList(itemEntities: List<ForecastListItemEntity>) {
        binding.composeView.setContent {
            AppTheme(dynamicColor = false) {

                val currentTempUnit = temperatureUnitState
                ForecastListScreen(itemEntities, currentTempUnit)
            }
        }
    }

    @Composable
    @OptIn(ExperimentalMaterial3Api::class)
    private fun ForecastListScreen(
        data: List<ForecastListItemEntity>,
        temperatureUnit: Int,
        modifier: Modifier = Modifier
    ) {
        Scaffold(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            topBar = {
                TopAppBar(
                    title = {
                        Text(resources.getString(R.string.open_weather_map))
                    },
                    actions = {
                        IconButton(onClick = {
                            showTemperatureOptions()
                        }) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = stringResource(R.string.action_settings)
                            )
                        }
                    }
                )
            },
        ) { contentPadding ->
            ForecastList(data, temperatureUnit, contentPadding, modifier)
        }
    }

    @Composable
    private fun ForecastList(
        data: List<ForecastListItemEntity>,
        temperatureUnit: Int,
        contentPadding: PaddingValues,
        modifier: Modifier,
    ) {
        LazyColumn(
            modifier = modifier
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .fillMaxSize()
                .padding(contentPadding),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(data) {
                ForecastListItem(it, temperatureUnit)
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ForecastListItem(
        item: ForecastListItemEntity,
        temperatureUnit: Int,
        modifier: Modifier = Modifier
    ) {
        val context = LocalContext.current
        Row(
            modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                modifier = modifier
                    .size(48.dp)
                    .padding(4.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                model = IMG_SRC_W_URL + item.imgIcon,
                contentDescription = item.main,
//                placeholder = painterResource(android.R.drawable.progress_indeterminate_horizontal),
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = modifier.padding(horizontal = 16.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = getReadableDate(item.dt))
                    Text(text = item.main)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Row(
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(text = convertTemperature(item.temp, temperatureUnit, context))
                    Text(text = convertTemperatureUnit(temperatureUnit, context.resources))
                }
            }
        }
    }

    @Preview("Preview Weather List", showBackground = true)
    @Composable
    fun PreviewForecastList() {
        val place = Place(
            "123",
            10.0,
            1,
            mutableListOf(
                Conditions(
                    dt = 21,
                    weather = mutableListOf(
                        Weather(
                            main = "Weather",
                            description = "Clear",
                            icon = "icon",
                        )
                    )
                )
            ),
            City()
        )
        val item = ForecastListItemMapper().mapPlaceToForecastListItem(place)
        AppTheme(dynamicColor = false) {
            ForecastListItem(item[0], 0)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        PreferenceManager.getDefaultSharedPreferences(requireContext())
            .unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        if (key == TEMPERATURE_KEY) {
            temperatureUnitState = sharedPreferences.getInt(TEMPERATURE_KEY, TEMPERATURE_DEFAULT)
        }
    }

    @InternalMavericksApi
    override fun invalidate() {
        withState(forecastListViewModel) { state ->
            when (state.forecastEntityList) {
                is Loading -> {
                    setSwipeRefresh(true)
                }

                is Success -> {
                    setSwipeRefresh(false)
                    showForecastList(state.forecastEntityList.invoke())
                }

                is Fail -> {
                    setSwipeRefresh(false)
                    showErrorMsg()
                }

                is Uninitialized -> setSwipeRefresh(true)
            }
        }
    }

    private fun convertTemperature(
        temperature: Double,
        temperatureUnit: Int,
        context: Context
    ): String {
        val convertedTemp = when (temperatureUnit) {
            TEMPERATURE_CELSIUS -> convertKelvinToCelsius(temperature)
            TEMPERATURE_FAHRENHEIT -> convertKelvinToFahrenheit(temperature)
            else -> convertKelvinToCelsius(temperature) //Default is Celsius
        }
        val formatter = context.getString(R.string.format_temperature)
        return String.format(formatter, convertedTemp)
    }

    fun convertTemperatureUnit(temperatureUnit: Int, resources: Resources): String {
        return when (temperatureUnit) {
            TEMPERATURE_CELSIUS -> resources.getString(R.string.celsius)
            TEMPERATURE_FAHRENHEIT -> resources.getString(R.string.fahrenheit)
            else -> "" //Default is Kelvin
        }
    }

    @Composable
    private fun getReadableDate(dateMilli: Long): String {
        if (LocalInspectionMode.current) {
            return "Mon - 10:30 AM (Preview)" // Simple placeholder
        }
        val localDateTime =
            Instant.ofEpochSecond(dateMilli).atZone(ZoneId.systemDefault()).toLocalDateTime()

        val formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
        val dayOfWeek = localDateTime.dayOfWeek

        return dayOfWeek.name + " - " + localDateTime.format(formatter)
    }

    //T(°C) = T(K) - 273.15
    private fun convertKelvinToCelsius(kelvin: Double): Double {
        return kelvin - 273.16
    }

    //T(°F) = T(K) × 9/5 - 459.67
    private fun convertKelvinToFahrenheit(kelvin: Double): Double {
        return (kelvin - 273.16) * 9.0 / 5 + 32
    }
}