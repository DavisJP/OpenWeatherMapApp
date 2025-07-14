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

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.davismiyashiro.weathermapapp.R
import com.davismiyashiro.weathermapapp.databinding.RecyclerWeatherItemBinding
import com.davismiyashiro.weathermapapp.domain.ForecastListItemEntity
import com.squareup.picasso.Picasso
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle

/**
 * Created by Davis Miyashiro.
 */
const val IMG_SRC_W_URL = "https://openweathermap.org/img/w/"
const val TEMPERATURE_CELSIUS = 0
const val TEMPERATURE_FAHRENHEIT = 1

class ForecastListAdapter(private val context: Context) :
    RecyclerView.Adapter<ForecastListAdapter.WeatherHolder>() {

    private var forecastListItemEntities: List<ForecastListItemEntity>

    private val temperatureUnit: Int
        get() = PreferenceManager.getDefaultSharedPreferences(context).getInt(TEMPERATURE_KEY, 0)

    init {
        forecastListItemEntities = ArrayList()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = RecyclerWeatherItemBinding.inflate(inflater, parent, false)
        return WeatherHolder(binding)
    }

    override fun onBindViewHolder(holder: WeatherHolder, position: Int) {
        val item = forecastListItemEntities[position]

        with(holder) {
            binding.weatherDescText.text = item.main

            binding.weatherDateText.text = getReadableDate(item.dt)

            val formatter = context.getString(R.string.format_temperature)
            val formattedTemperature = String.format(formatter, convertTemperature(item.temp))
            binding.weatherHighTempText.text = formattedTemperature

            binding.weatherTempUnitText.setTemperatureUnit(temperatureUnit)

            Picasso.get()
                .load(IMG_SRC_W_URL + item.imgIcon)
                .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                .into(binding.weatherImg)
        }
    }

    override fun getItemCount(): Int {
        return forecastListItemEntities.size
    }

    private fun convertTemperature(kelvin: Double): Double {
        return when (temperatureUnit) {
            TEMPERATURE_CELSIUS -> convertKelvinToCelsius(kelvin)
            TEMPERATURE_FAHRENHEIT -> convertKelvinToFahrenheit(kelvin)
            else -> convertKelvinToCelsius(kelvin) //Default is Celsius
        }
    }

    private fun getReadableDate(dateMilli: Long): String {

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

    fun replaceData(itemEntities: List<ForecastListItemEntity>) {
        forecastListItemEntities = itemEntities
        notifyDataSetChanged()
    }

    class WeatherHolder(val binding: RecyclerWeatherItemBinding) :
        RecyclerView.ViewHolder(binding.root)
}
