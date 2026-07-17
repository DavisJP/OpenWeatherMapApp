package com.davismiyashiro.weathermapapp.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.davismiyashiro.weathermapapp.domain.NetworkConnectivity
import javax.inject.Inject

/**
 * Android implementation of [NetworkConnectivity].
 */
class AndroidNetworkConnectivity @Inject constructor(
    private val context: Context
) : NetworkConnectivity {

    override fun isOnline(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetwork
        val networkCapabilities = activeNetwork?.let(cm::getNetworkCapabilities)
        return when {
            networkCapabilities == null -> false
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
            else -> false
        }
    }
}
