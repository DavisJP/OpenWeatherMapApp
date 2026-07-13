package com.davismiyashiro.weathermapapp.presentation

import android.content.Context
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.davismiyashiro.weathermapapp.R
import com.davismiyashiro.weathermapapp.domain.IMG_SRC_W_URL
import com.davismiyashiro.weathermapapp.domain.TEMPERATURE_CELSIUS
import com.davismiyashiro.weathermapapp.domain.TEMPERATURE_FAHRENHEIT
import com.davismiyashiro.weathermapapp.domain.convertKelvinToCelsius
import com.davismiyashiro.weathermapapp.domain.convertKelvinToFahrenheit
import com.davismiyashiro.weathermapapp.presentation.ForecastListEvent.Refresh
import com.davismiyashiro.weathermapapp.presentation.ForecastListEvent.UpdateTemperatureUnit
import com.slack.circuit.codegen.annotations.CircuitInject
import dagger.hilt.components.SingletonComponent
import kotlinx.collections.immutable.ImmutableList
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle

const val TEMPERATURE_KEY = "TEMPERATURE_KEY"
const val TEMPERATURE_DEFAULT = TEMPERATURE_CELSIUS

@CircuitInject(ForecastListScreen::class, SingletonComponent::class)
@Composable
fun ForecastListUi(state: ForecastListState, modifier: Modifier = Modifier) {
    ForecastHomeScreen(
        forecastState = state,
        onRefresh = { state.eventSink(Refresh) },
        onTemperatureUnitSelect = { state.eventSink(UpdateTemperatureUnit(it)) },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForecastHomeScreen(
    forecastState: ForecastListState,
    onRefresh: () -> Unit,
    onTemperatureUnitSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showSettingsDialog by rememberSaveable { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.open_weather_map))
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
                actions = {
                    IconButton(onClick = {
                        showSettingsDialog = true
                    }) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = stringResource(R.string.action_settings),
                        )
                    }
                },
            )
        },
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize()
        ) {
            when (forecastState) {
                is ForecastListState.Loading -> {
                    ForecastLoadingScreen()
                }

                is ForecastListState.Success -> {
                    ForecastListContent(
                        data = forecastState.forecastItems,
                        temperatureUnit = forecastState.temperatureUnit,
                        isRefreshing = forecastState.isRefreshing,
                        onRefresh = onRefresh,
                    )
                }

                is ForecastListState.Error -> {
                    ForecastErrorScreen(
                        isRefreshing = forecastState.isRefreshing,
                        onRefresh = onRefresh,
                    )
                }
            }
        }
    }

    if (showSettingsDialog && forecastState is ForecastListState.Success) {
        SettingsDialog(
            currentUnitIndexSelected = forecastState.temperatureUnit,
            onDismissRequest = { showSettingsDialog = false },
            onUnitSelect = {
                onTemperatureUnitSelect(it)
                showSettingsDialog = false
            },
        )
    }
}

@Composable
fun ForecastLoadingScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForecastErrorScreen(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.please_check_your_network_status_or_try_again_later),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ForecastListContent(
    data: ImmutableList<ForecastListItem>,
    temperatureUnit: Int,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
) {
    val scrollState = rememberLazyListState()

    PullToRefreshBox(
        modifier = Modifier.fillMaxSize(),
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            state = scrollState,
            contentPadding = PaddingValues(16.dp),
        ) {
            items(
                items = data,
                key = { it.date },
            ) {
                ForecastListItem(it, temperatureUnit)
            }
        }
    }
}

@Composable
fun ForecastListItem(item: ForecastListItem, temperatureInt: Int, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current
    val readableDate = remember(item.date, isPreview) { getReadableDate(item.date, isPreview) }
    val tempString =
        remember(item.temp, temperatureInt) { item.temp.toTemperatureUnit(temperatureInt, context) }
    val unitString =
        remember(temperatureInt) { temperatureInt.toTemperatureUnit(context) }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            modifier = Modifier
                .size(48.dp)
                .padding(4.dp)
                .background(MaterialTheme.colorScheme.primaryContainer),
            model = IMG_SRC_W_URL + item.icon,
            contentDescription = item.main,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(text = readableDate)
                Text(text = item.main)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Row(
                horizontalArrangement = Arrangement.End,
            ) {
                Text(text = tempString)
                Text(text = unitString)
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SettingsDialog(
    currentUnitIndexSelected: Int,
    onDismissRequest: () -> Unit,
    onUnitSelect: (Int) -> Unit,
) {
    val context = LocalContext.current
    var tempSelectedOptionIndex by remember(currentUnitIndexSelected) {
        mutableIntStateOf(
            currentUnitIndexSelected,
        )
    }
    val temperatureScales =
        context.resources.getStringArray(R.array.pref_temperature_units)
    AlertDialog(
        containerColor = MaterialTheme.colorScheme.background,
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = stringResource(R.string.choose_temperature_unit),
            )
        },
        text = {
            Column {
                temperatureScales.forEachIndexed { index, scale ->
                    Row(
                        modifier = Modifier
                            .padding(PaddingValues(bottom = 8.dp))
                            .selectable(
                                index == tempSelectedOptionIndex,
                                onClick = { tempSelectedOptionIndex = index },
                            ),
                    ) {
                        RadioButton(
                            selected = index == tempSelectedOptionIndex,
                            onClick = null,
                        )
                        Spacer(Modifier.width(16.dp))
                        Text(scale)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onUnitSelect(tempSelectedOptionIndex)
                },
            ) {
                Text(stringResource(android.R.string.ok))
            }
        },
    )
}

fun Double.toTemperatureUnit(temperatureUnit: Int, context: Context): String {
    val convertedTemp = when (temperatureUnit) {
        TEMPERATURE_CELSIUS -> convertKelvinToCelsius(this)
        TEMPERATURE_FAHRENHEIT -> convertKelvinToFahrenheit(this)
        else -> convertKelvinToCelsius(this) // Default is Celsius
    }
    val formatter = context.getString(R.string.format_temperature)
    return String.format(formatter, convertedTemp)
}

fun Int.toTemperatureUnit(context: Context): String {
    return when (this) {
        TEMPERATURE_CELSIUS -> context.getString(R.string.celsius)
        TEMPERATURE_FAHRENHEIT -> context.getString(R.string.fahrenheit)
        else -> "" // Default is Kelvin
    }
}

fun getReadableDate(dateMilli: Long, isPreview: Boolean): String {
    if (isPreview) {
        return "Mon - 10:30 AM (Preview)"
    }
    val localDateTime =
        Instant.ofEpochSecond(dateMilli).atZone(ZoneId.systemDefault()).toLocalDateTime()

    val formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
    val dayOfWeek = localDateTime.dayOfWeek

    return dayOfWeek.name + " - " + localDateTime.format(formatter)
}
