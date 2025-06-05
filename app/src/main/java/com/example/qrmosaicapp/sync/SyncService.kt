package com.example.qrmosaicapp.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log

class SyncService(private val context: Context) {

    fun syncIfNeeded() {
        if (isInternetAvailable()) {
            Log.d("SyncService", "Internet available. Syncing...")
            // TODO: Add actual server sync logic here
        } else {
            Log.d("SyncService", "No internet. Sync deferred.")
        }
    }

    private fun isInternetAvailable(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
