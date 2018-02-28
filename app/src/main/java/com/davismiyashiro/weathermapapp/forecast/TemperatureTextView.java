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
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import com.davismiyashiro.weathermapapp.R;

import static com.davismiyashiro.weathermapapp.forecast.ForecastListAdapter.TEMPERATURE_CELSIUS;
import static com.davismiyashiro.weathermapapp.forecast.ForecastListAdapter.TEMPERATURE_FAHRENHEIT;

/**
 * Created by Davis Miyashiro.
 */

public class TemperatureTextView extends AppCompatTextView{

    public TemperatureTextView(Context context) {
        super(context);
    }

    public TemperatureTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TemperatureTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setTemperatureUnit (int temperatureUnit) {

        switch (temperatureUnit) {
            case TEMPERATURE_CELSIUS:
                setText(getContext().getText(R.string.celsius));
                break;
            case TEMPERATURE_FAHRENHEIT:
                setText(getContext().getText(R.string.fahrenheit));
                break;
            default:
                setText(""); //Default is Kelvin
        }
    }
}
