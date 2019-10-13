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
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.davismiyashiro.weathermapapp.R
import com.squareup.picasso.Picasso
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import java.util.*

/**
 * Created by Davis Miyashiro.
 */
const val IMG_SRC_W_URL = "http://openweathermap.org/img/w/"
const val TEMPERATURE_CELSIUS = 0
const val TEMPERATURE_FAHRENHEIT = 1

class ForecastListAdapter(private val context: Context) : RecyclerView.Adapter<ForecastListAdapter.WeatherHolder>() {

    private var forecastListItems: List<ForecastListItem>

    private val temperatureUnit: Int
        get() = PreferenceManager.getDefaultSharedPreferences(context).getInt(TEMPERATURE_KEY, 0)

    init {
        forecastListItems = ArrayList()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherHolder {
        val inflater = LayoutInflater.from(parent.context)
        val root = inflater.inflate(R.layout.recycler_weather_item, parent, false)
        return WeatherHolder(root)
    }

    override fun onBindViewHolder(holder: WeatherHolder, position: Int) {
        val item = forecastListItems[position]

        holder.weatherDesc?.text = item.main

        holder.weatherDate?.text = getReadableDate(item.dt)

        val formatter = context.getString(R.string.format_temperature)
        val formattedTemperature = String.format(formatter, convertTemperature(item.temp))
        holder.weatherHighTemp?.text = formattedTemperature

        holder.weatherUnitTemp?.setTemperatureUnit(temperatureUnit)

        Picasso.get()
                .load(IMG_SRC_W_URL + item.imgIcon)
                .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                .into(holder.weatherIcon)
    }

    override fun getItemCount(): Int {
        return forecastListItems.size
    }

    private fun convertTemperature(kelvin: Double): Double {
        return when (temperatureUnit) {
            TEMPERATURE_CELSIUS -> convertKelvinToCelsius(kelvin)
            TEMPERATURE_FAHRENHEIT -> convertKelvinToFahrenheit(kelvin)
            else -> convertKelvinToCelsius(kelvin) //Default is Celsius
        }
    }

    private fun getReadableDate(dateMilli: Long): String {

        val localDateTime = Instant.ofEpochSecond(dateMilli).atZone(ZoneId.systemDefault()).toLocalDateTime()

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

    fun replaceData(items: List<ForecastListItem>) {
        forecastListItems = items
        notifyDataSetChanged()
    }

    inner class WeatherHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        @BindView(R.id.weather_img)
        @JvmField
        var weatherIcon: ImageView? = null //list.weather.icon
        @BindView(R.id.weather_date_text)
        @JvmField
        var weatherDate: TextView? = null //list.dt (millis?) or list.dtTxt (Str)
        @BindView(R.id.weather_desc_text)
        @JvmField
        var weatherDesc: TextView? = null //list.weather.main or description
        @BindView(R.id.weather_high_temp_text)
        @JvmField
        var weatherHighTemp: TextView? = null //list.main.tempMax
        @BindView(R.id.weather_temp_unit_text)
        @JvmField
        var weatherUnitTemp: TemperatureTextView? = null

        init {
            ButterKnife.bind(this, itemView)
        }
    }
}
