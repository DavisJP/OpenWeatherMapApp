package com.davismiyashiro.weathermapapp.data.storage

import android.content.Context
import android.preference.PreferenceManager
import javax.inject.Inject

class SharedPreferenceStorage @Inject constructor(context: Context) : Storage{

    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    override fun setString(key: String, value: String) {
        with(sharedPreferences.edit()) {
            putString(key, value)
            apply()
        }
    }

    override fun getString(key: String): String {
        return sharedPreferences.getString(key, "")!!
    }
}