package com.davismiyashiro.weathermapapp.domain

import kotlinx.coroutines.flow.Flow

interface Storage {
    suspend fun setString(key: String, value: String)
    suspend fun getString(key: String): String
    fun getStringFlow(key: String): Flow<String>
    suspend fun setInt(key: String, value: Int)
    suspend fun getInt(key: String, defaultValue: Int): Int
    fun getIntFlow(key: String, defaultValue: Int): Flow<Int>
}
