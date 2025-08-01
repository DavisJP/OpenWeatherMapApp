package com.davismiyashiro.weathermapapp.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.davismiyashiro.weathermapapp.data.entities.City
import com.davismiyashiro.weathermapapp.data.entities.Conditions
import com.davismiyashiro.weathermapapp.data.entities.Place
import com.davismiyashiro.weathermapapp.data.entities.Weather
import com.davismiyashiro.weathermapapp.designsystem.theme.AppTheme
import com.davismiyashiro.weathermapapp.domain.ForecastListItemMapper

@Preview(showBackground = true)
@Composable
private fun PreviewLoadingScreen() {
    ForecastLoadingScreen()
}

@Preview(showBackground = true)
@Composable
private fun PreviewErrorScreen() {
    ForecastErrorScreen(isRefreshing = false) {}
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

@Composable
@Preview("Setting dialog")
fun ShowSettingsDialog() {
    AppTheme(dynamicColor = false) {
        SettingsDialog(
            onDismissRequest = { },
            showDialog = true,
            currentUnitIndexSelected = 0,
            onUnitSelected = { },
        )
    }
}
