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

package com.davismiyashiro.weathermapapp.forecast;

import android.content.Context;
import android.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.davismiyashiro.weathermapapp.R;
import com.squareup.picasso.Picasso;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.FormatStyle;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.davismiyashiro.weathermapapp.forecast.ForecastListActivity.TEMPERATURE_KEY;

/**
 * Created by Davis Miyashiro.
 */

public class ForecastListAdapter extends RecyclerView.Adapter<ForecastListAdapter.WeatherHolder>{

    public static final String IMG_SRC_W_URL = "http://openweathermap.org/img/w/";

    final public static int TEMPERATURE_CELSIUS = 0;
    final public static int TEMPERATURE_FAHRENHEIT = 1;

    private Context context;

    List<ForecastListItem> forecastListItems;

    public ForecastListAdapter(Context context) {
        forecastListItems = new ArrayList<>();
        this.context = context;
    }

    @Override
    public WeatherHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View root = inflater.inflate(R.layout.recycler_weather_item, parent, false);
        return new WeatherHolder(root);
    }

    @Override
    public void onBindViewHolder(WeatherHolder holder, int position) {
        ForecastListItem item = forecastListItems.get(position);

        holder.weatherDesc.setText(item.getMain());

        holder.weatherDate.setText(getReadableDate(item.getDt()));

        String formatter = context.getString(R.string.format_temperature);
        String formattedTemperature = String.format(formatter, convertTemperature(item.getTemp()));
        holder.weatherHighTemp.setText(formattedTemperature);

        holder.weatherUnitTemp.setTemperatureUnit(getTemperatureUnit());

        Picasso.get()
                .load(IMG_SRC_W_URL + item.getImgIcon())
                .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                .into(holder.weatherIcon);
    }

    @Override
    public int getItemCount() {
        return forecastListItems.size();
    }

    public int getTemperatureUnit() {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(TEMPERATURE_KEY, 0);
    }

    public double convertTemperature (double kelvin) {

        switch (getTemperatureUnit()) {
            case TEMPERATURE_CELSIUS:
                return convertKelvinToCelsius(kelvin);
            case TEMPERATURE_FAHRENHEIT:
                return convertKelvinToFahrenheit(kelvin);
            default:
                return convertKelvinToCelsius(kelvin); //Default is Celsius
        }
    }

    public String getReadableDate(long dateMilli) {

        LocalDateTime localDateTime = Instant.ofEpochSecond(dateMilli).atZone(ZoneId.systemDefault()).toLocalDateTime();

        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT);
        DayOfWeek dayOfWeek = localDateTime.getDayOfWeek();

        return dayOfWeek.name() + " - " + localDateTime.format(formatter);
    }

    //T(°C) = T(K) - 273.15
    private double convertKelvinToCelsius(double kelvin) {
        return kelvin - 273.16;
    }

    //T(°F) = T(K) × 9/5 - 459.67
    private double convertKelvinToFahrenheit(double kelvin) {
        return ((kelvin - 273.16) * 9d/5) + 32;
    }

    public void replaceData (List<ForecastListItem> items) {
        forecastListItems = items;
        notifyDataSetChanged();
    }

    public class WeatherHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.weather_img) ImageView weatherIcon; //list.weather.icon
        @BindView(R.id.weather_date_text) TextView weatherDate; //list.dt (millis?) or list.dtTxt (Str)
        @BindView(R.id.weather_desc_text) TextView weatherDesc; //list.weather.main or description
        @BindView(R.id.weather_high_temp_text) TextView weatherHighTemp; //list.main.tempMax
        @BindView(R.id.weather_temp_unit_text) TemperatureTextView weatherUnitTemp;

        public WeatherHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }
}
