package com.davismiyashiro.weathermapapp.domain

/**
 * Interface to check network connectivity.
 */
interface NetworkConnectivity {
    fun isOnline(): Boolean
}
