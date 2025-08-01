package com.davismiyashiro.weathermapapp.data.storage

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import javax.inject.Inject

class SharedPreferenceStorage @Inject constructor(context: Context) : Storage {

    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    override fun setString(key: String, value: String) {
        sharedPreferences.edit {
            putString(key, value)
        }
    }

    override fun getString(key: String): String {
        return sharedPreferences.getString(key, "")!!
    }
}