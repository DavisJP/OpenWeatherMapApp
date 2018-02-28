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
