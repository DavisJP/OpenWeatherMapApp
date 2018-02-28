package com.davismiyashiro.weathermapapp.forecast;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.davismiyashiro.weathermapapp.forecast.ForecastListActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;

/**
 * Created by Davis Miyashiro on 18/12/2017.
 */

@RunWith(AndroidJUnit4.class)
public class ForecastListActivityTest {

    @Rule
    public final ActivityTestRule<ForecastListActivity> main = new ActivityTestRule<>(ForecastListActivity.class);

    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.davismiyashiro.weathermapapp", appContext.getPackageName());
    }

    @Test
    public void checkToolbarDisplaysTitle() {
        onView(withText("OpenWeatherMap")).check(matches(isDisplayed()));
    }
}
