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

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.davismiyashiro.weathermapapp.R
import com.davismiyashiro.weathermapapp.domain.Repository
import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * Created by Davis Miyashiro on 18/12/2017.
 */

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ForecastListActivityTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<ForecastListActivity>()

    @Inject
    lateinit var repository: Repository

    @Before
    fun setUp() {
        AndroidThreeTen.init(InstrumentationRegistry.getInstrumentation().targetContext)
        hiltRule.inject()
    }

    @Test
    @Throws(Exception::class)
    fun useAppContext() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        assertEquals("com.davismiyashiro.weathermapapp", appContext.packageName)
    }

    @Test
    fun checkToolbarDisplaysTitle() {
        val expectedTitle = composeTestRule.activity.getString(R.string.open_weather_map)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(expectedTitle).assertIsDisplayed()
    }

    @Test
    fun forecastItem_displaysWeatherTextAndIcon() {
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText("Rain").fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("Rain").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Rain").assertIsDisplayed()
    }

    @Test
    fun changingTemperatureUnit_updatesDisplayedUnitWithoutBreakingListItem() {
        val settingsLabel = composeTestRule.activity.getString(R.string.action_settings)
        val fahrenheitLabel = composeTestRule.activity.getString(R.string.fahrenheit_label)
        val okLabel = composeTestRule.activity.getString(android.R.string.ok)

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText("Rain").fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.celsius))
            .assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(settingsLabel).performClick()
        composeTestRule.onNodeWithText(fahrenheitLabel).performClick()
        composeTestRule.onNodeWithText(okLabel).performClick()

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.fahrenheit))
            .assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Rain").assertIsDisplayed()
    }
}
