package com.dhimandasgupta.notemark.common.android

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

sealed interface ConnectionState {
    data object Available : ConnectionState
    data object Unavailable : ConnectionState
}

/**
 * Network utility to get current state of internet connection
 */
val Context.currentConnectivityState: ConnectionState
    get() {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return getCurrentConnectivityState(connectivityManager)
    }

private fun getCurrentConnectivityState(
    connectivityManager: ConnectivityManager
): ConnectionState {
    val connected = connectivityManager.getNetworkCapabilities(
        connectivityManager.activeNetwork
    )?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) ?: false

    return if (connected) ConnectionState.Available else ConnectionState.Unavailable
}

/**
 * Network Utility to observe availability or unavailability of Internet connection
 */
fun Context.observeConnectivityAsFlow() = callbackFlow {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val callback = networkCallback { connectionState -> trySend(connectionState) }

    val networkRequest = NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
        .build()

    connectivityManager.registerNetworkCallback(networkRequest, callback)

    // Set current state
    val currentState = getCurrentConnectivityState(connectivityManager)
    trySend(element = currentState)

    // Remove callback when not used
    awaitClose {
        // Remove listeners
        connectivityManager.unregisterNetworkCallback(callback)
    }
}.distinctUntilChanged()

private fun networkCallback(
    callback: (ConnectionState) -> Unit
): ConnectivityManager.NetworkCallback {
    return object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            callback(ConnectionState.Available)
        }

        override fun onLost(network: Network) {
            callback(ConnectionState.Unavailable)
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            super.onCapabilitiesChanged(network, networkCapabilities)
            val hasInternetCapability = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            val isConnected = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)

            val connected = hasInternetCapability && isConnected
            callback(if (connected) ConnectionState.Available else ConnectionState.Unavailable)
        }
    }
}

// Utility Composable to use ConnectionState from Compose UI...
@Composable
fun connectivityState(): State<ConnectionState> {
    val context = LocalContext.current.applicationContext

    // Creates a State<ConnectionState> with current connectivity state as initial value
    return produceState(initialValue = context.currentConnectivityState) {
        // In a coroutine, can make suspend calls
        context.observeConnectivityAsFlow().collect { connectivityState ->
            this.value = connectivityState
        }
    }
}
