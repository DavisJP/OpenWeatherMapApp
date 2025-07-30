package com.davismiyashiro.weathermapapp.presentation

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
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
import com.davismiyashiro.weathermapapp.databinding.FragmentForecastListBinding
import com.davismiyashiro.weathermapapp.designsystem.theme.AppTheme
import com.davismiyashiro.weathermapapp.domain.ForecastListItemEntity
import com.davismiyashiro.weathermapapp.domain.IMG_SRC_W_URL
import com.davismiyashiro.weathermapapp.domain.TEMPERATURE_CELSIUS
import com.davismiyashiro.weathermapapp.domain.TEMPERATURE_FAHRENHEIT
import com.davismiyashiro.weathermapapp.domain.convertKelvinToCelsius
import com.davismiyashiro.weathermapapp.domain.convertKelvinToFahrenheit
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

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            view.updatePadding(
                top = insets.top,
                left = insets.left,
                right = insets.right,
            )

            WindowInsetsCompat.CONSUMED
        }

        if (savedInstanceState != null) {
            savedState = savedInstanceState.getParcelable(RECYCLER_STATE)
        }
        forecastListViewModel.loadWeatherData(false)
    }

    private fun showTemperatureOptions() {
        val temperatureDialog = ForecastSettingsFragmentDialog.newInstance()
        temperatureDialog.show(parentFragmentManager, "fragDialog")
    }

    @OptIn(ExperimentalMaterial3Api::class)
    private fun showErrorMsg(
        onRefresh: () -> Unit,
    ) {
        binding.composeView.setContent {
            ForecastErrorScreen(
                isRefreshing = false,
                onRefresh = onRefresh
            )
        }
    }

    private fun showForecastList(
        itemEntities: List<ForecastListItemEntity>,
        onRefresh: () -> Unit,
        onMenuAction: () -> Unit,
    ) {
        binding.composeView.setContent {
            AppTheme(dynamicColor = false) {

                val currentTempUnit = temperatureUnitState
                ForecastListScreen(
                    itemEntities,
                    currentTempUnit,
                    isRefreshing = false,
                    onRefresh = onRefresh,
                    context = requireContext(),
                    onMenuAction = onMenuAction,
                )
            }
        }
    }

    fun showLoadingScreen() {
        binding.composeView.setContent {
            ForecastLoadingScreen()
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
                is Loading, Uninitialized -> {
                    showLoadingScreen()
                }

                is Success -> {
                    showForecastList(
                        state.forecastEntityList(),
                        onRefresh = { forecastListViewModel.loadWeatherData(true) },
                        onMenuAction = { showTemperatureOptions() }
                    )
                }

                is Fail -> {
                    showErrorMsg(
                        onRefresh = { forecastListViewModel.loadWeatherData(true) }
                    )
                }
            }
        }
    }
}

@Composable
fun ForecastLoadingScreen() {
    AppTheme(dynamicColor = false) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForecastErrorScreen(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
) {
    val context = LocalContext.current
    val pullToRefreshState = rememberPullToRefreshState()
    val scrollState = rememberScrollState()
    AppTheme(dynamicColor = false) {
        Scaffold(
            modifier = Modifier.pullToRefresh(
                state = pullToRefreshState,
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
            )
        ) { padding ->
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = onRefresh
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(scrollState),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        modifier = Modifier.padding(padding),
                        text =
                            context.resources.getString(R.string.please_check_your_network_status_or_try_again_later),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ForecastListScreen(
    data: List<ForecastListItemEntity>,
    temperatureUnit: Int,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    context: Context,
    onMenuAction: () -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val scrollState = rememberLazyListState()
    val pullToRefreshState = rememberPullToRefreshState()
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .pullToRefresh(
                state = pullToRefreshState,
                onRefresh = onRefresh,
                isRefreshing = isRefreshing
            ),
        topBar = {
            TopAppBar(
                title = {
                    Text(context.resources.getString(R.string.open_weather_map))
                },
                scrollBehavior = scrollBehavior,
                actions = {
                    IconButton(onClick = {
                        onMenuAction()
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
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh
        ) {
            ForecastList(data, temperatureUnit, contentPadding, scrollState, modifier)
        }
    }
}

@Composable
private fun ForecastList(
    data: List<ForecastListItemEntity>,
    temperatureUnit: Int,
    contentPadding: PaddingValues,
    scrollState: LazyListState,
    modifier: Modifier,
) {
    LazyColumn(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .fillMaxSize()
            .padding(contentPadding),
        state = scrollState,
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
    temperatureInt: Int,
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
                Text(text = item.temp.toTemperatureUnit(temperatureInt, context))
                Text(text = temperatureInt.toTemperatureUnit(context.resources))
            }
        }
    }
}

fun Double.toTemperatureUnit(temperatureUnit: Int, context: Context): String {
    val convertedTemp = when (temperatureUnit) {
        TEMPERATURE_CELSIUS -> convertKelvinToCelsius(this)
        TEMPERATURE_FAHRENHEIT -> convertKelvinToFahrenheit(this)
        else -> convertKelvinToCelsius(this) //Default is Celsius
    }
    val formatter = context.getString(R.string.format_temperature)
    return String.format(formatter, convertedTemp)
}

fun Int.toTemperatureUnit(
    resources: Resources
): String {
    return when (this) {
        TEMPERATURE_CELSIUS -> resources.getString(R.string.celsius)
        TEMPERATURE_FAHRENHEIT -> resources.getString(R.string.fahrenheit)
        else -> "" //Default is Kelvin
    }
}

@Composable
fun getReadableDate(dateMilli: Long): String {
    if (LocalInspectionMode.current) {
        return "Mon - 10:30 AM (Preview)" // Simple placeholder
    }
    val localDateTime =
        Instant.ofEpochSecond(dateMilli).atZone(ZoneId.systemDefault()).toLocalDateTime()

    val formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
    val dayOfWeek = localDateTime.dayOfWeek

    return dayOfWeek.name + " - " + localDateTime.format(formatter)
}