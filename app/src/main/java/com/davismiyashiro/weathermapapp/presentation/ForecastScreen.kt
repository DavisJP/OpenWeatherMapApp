package com.davismiyashiro.weathermapapp.presentation

import android.content.Context
import android.content.res.Resources
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
import com.davismiyashiro.weathermapapp.designsystem.theme.AppTheme
import com.davismiyashiro.weathermapapp.domain.IMG_SRC_W_URL
import com.davismiyashiro.weathermapapp.domain.TEMPERATURE_CELSIUS
import com.davismiyashiro.weathermapapp.domain.TEMPERATURE_FAHRENHEIT
import com.davismiyashiro.weathermapapp.domain.convertKelvinToCelsius
import com.davismiyashiro.weathermapapp.domain.convertKelvinToFahrenheit
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle

const val TEMPERATURE_KEY = "TEMPERATURE_KEY"
const val TEMPERATURE_DEFAULT = TEMPERATURE_CELSIUS

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForecastHomeScreen(
    forecastState: ForecastListState,
) {
    var showSettingsDialog by remember { mutableStateOf(false) }

    if (forecastState.isLoading) {
        ForecastLoadingScreen()
    } else if (forecastState.error != null) {
        ForecastErrorScreen(
            isRefreshing = forecastState.isLoading,
            onRefresh = { forecastState.eventSink(ForecastListEvent.Refresh) }
        )
    } else {
        ForecastListScreen(
            data = forecastState.forecastItems,
            temperatureUnit = forecastState.temperatureUnit,
            isRefreshing = forecastState.isLoading,
            onRefresh = { forecastState.eventSink(ForecastListEvent.Refresh) },
            showDialog = showSettingsDialog,
            onShowDialogChange = { showSettingsDialog = it },
            onDialogUnitSelected = {
                forecastState.eventSink(ForecastListEvent.UpdateTemperatureUnit(it))
                showSettingsDialog = false
            }
        )
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
    val scrollState = rememberScrollState()
    AppTheme(dynamicColor = false) {
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
                    text = context.resources.getString(R.string.please_check_your_network_status_or_try_again_later),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ForecastListScreen(
    data: List<ForecastListItem>,
    temperatureUnit: Int,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    showDialog: Boolean,
    onShowDialogChange: (Boolean) -> Unit,
    onDialogUnitSelected: (Int) -> Unit,
) {
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val scrollState = rememberLazyListState()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(context.resources.getString(R.string.open_weather_map))
                },
                scrollBehavior = scrollBehavior,
                actions = {
                    IconButton(onClick = {
                        onShowDialogChange(true)
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
            modifier = Modifier.padding(contentPadding),
            isRefreshing = isRefreshing,
            onRefresh = onRefresh
        ) {
            ForecastList(data, temperatureUnit, scrollState)
        }
    }

    SettingsDialog(
        showDialog = showDialog,
        currentUnitIndexSelected = temperatureUnit,
        onDismissRequest = { onShowDialogChange(false) },
        onUnitSelected = { selectedIndex ->
            onDialogUnitSelected(selectedIndex)
        }
    )
}

@Composable
private fun ForecastList(
    data: List<ForecastListItem>,
    temperatureUnit: Int,
    scrollState: LazyListState,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        state = scrollState,
        contentPadding = PaddingValues(16.dp)
    ) {
        items(data) {
            ForecastListItem(it, temperatureUnit)
        }
    }
}

@Composable
fun ForecastListItem(
    item: ForecastListItem,
    temperatureInt: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current
    val readableDate = remember(item.date, isPreview) { getReadableDate(item.date, isPreview) }
    val tempString =
        remember(item.temp, temperatureInt) { item.temp.toTemperatureUnit(temperatureInt, context) }
    val unitString =
        remember(temperatureInt) { temperatureInt.toTemperatureUnit(context.resources) }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
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
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(text = readableDate)
                Text(text = item.main)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Row(
                horizontalArrangement = Arrangement.End
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
    showDialog: Boolean,
    currentUnitIndexSelected: Int,
    onDismissRequest: () -> Unit,
    onUnitSelected: (Int) -> Unit,
) {
    if (showDialog) {
        val context = LocalContext.current
        var tempSelectedOptionIndex by remember(currentUnitIndexSelected) {
            mutableIntStateOf(
                currentUnitIndexSelected
            )
        }
        val temperatureScales =
            context.resources.getStringArray(R.array.pref_temperature_units)
        AlertDialog(
            containerColor = MaterialTheme.colorScheme.background,
            onDismissRequest = onDismissRequest,
            title = {
                Text(
                    text = context.getString(R.string.choose_temperature_unit),
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
                                    onClick = { tempSelectedOptionIndex = index })
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
                        onUnitSelected(tempSelectedOptionIndex)
                    },
                ) {
                    Text(stringResource(android.R.string.ok))
                }
            },
        )
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