package com.davismiyashiro.weathermapapp.forecast;

import com.davismiyashiro.weathermapapp.model.data.Conditions;

/**
 * Created by Davis Miyashiro.
 */

public class ForecastListItem {

    private String main;
    private long dt;
    private Double temp;
    private String imgIcon;

    public ForecastListItem (Conditions condition) {
        main = condition.getWeather().get(0).getMain();
        dt = condition.getDt();
        temp = condition.getMain().getTemp();
        imgIcon = condition.getWeather().get(0).getIcon() + ".png";
    }

    public String getMain() {
        return main;
    }

    public long getDt() {
        return dt;
    }

    public Double getTemp() {
        return temp;
    }

    public String getImgIcon() {
        return imgIcon;
    }
}
