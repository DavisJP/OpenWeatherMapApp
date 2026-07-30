package com.davismiyashiro.weathermapapp.data.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.davismiyashiro.weathermapapp.domain.Storage
import com.davismiyashiro.weathermapapp.injection.AppScope
import com.davismiyashiro.weathermapapp.injection.SingleInApp
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

@SingleInApp
@ContributesBinding(AppScope::class)
@Inject
class DataStoreStorage(
    private val dataStore: DataStore<Preferences>
) : Storage {

    override suspend fun setString(key: String, value: String) {
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey(key)] = value
        }
    }

    override suspend fun getString(key: String): String {
        return dataStore.data.first()[stringPreferencesKey(key)] ?: ""
    }

    override fun getStringFlow(key: String): Flow<String> {
        return dataStore.data.map { preferences ->
            preferences[stringPreferencesKey(key)] ?: ""
        }
    }

    override suspend fun setInt(key: String, value: Int) {
        dataStore.edit { preferences ->
            preferences[intPreferencesKey(key)] = value
        }
    }

    override suspend fun getInt(key: String, defaultValue: Int): Int {
        return dataStore.data.first()[intPreferencesKey(key)] ?: defaultValue
    }

    override fun getIntFlow(key: String, defaultValue: Int): Flow<Int> {
        return dataStore.data.map { preferences ->
            preferences[intPreferencesKey(key)] ?: defaultValue
        }
    }
}
