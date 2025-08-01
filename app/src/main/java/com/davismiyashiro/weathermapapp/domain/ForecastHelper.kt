package com.davismiyashiro.weathermapapp.domain

const val IMG_SRC_W_URL = "https://openweathermap.org/img/w/"
const val TEMPERATURE_CELSIUS = 0
const val TEMPERATURE_FAHRENHEIT = 1

/**
Kelvin to Celsius conversion formula T(°C) = T(K) - 273.15
 */
fun convertKelvinToCelsius(kelvin: Double): Double {
    return kelvin - 273.16
}

/**
T(°F) = T(K) × 9/5 - 459.67
 */
fun convertKelvinToFahrenheit(kelvin: Double): Double {
    return (kelvin - 273.16) * 9.0 / 5 + 32
}