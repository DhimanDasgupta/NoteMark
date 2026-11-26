package com.dhimandasgupta.notemark.common.android

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface ConnectionState {
  @Serializable @SerialName("available") data object Available : ConnectionState

  @Serializable @SerialName("unavailable") data object Unavailable : ConnectionState
}

@RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
private fun getCurrentConnectivityState(connectivityManager: ConnectivityManager): ConnectionState {
  val connected =
    connectivityManager
      .getNetworkCapabilities(connectivityManager.activeNetwork)
      ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) ?: false

  return if (connected) ConnectionState.Available else ConnectionState.Unavailable
}

/** Network Utility to observe the availability or unavailability of Internet connection */
@RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
fun Context.observeConnectivityAsFlow() =
  callbackFlow {
      val connectivityManager =
        getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

      val callback = networkCallback { connectionState -> trySend(connectionState) }

      val networkRequest =
        NetworkRequest.Builder()
          .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
          .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
          .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
          .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
          .build()

      connectivityManager.registerNetworkCallback(networkRequest, callback)

      // Set the current state
      val currentState = getCurrentConnectivityState(connectivityManager)
      trySend(element = currentState)

      // Remove callback when not used
      awaitClose {
        // Remove listeners
        connectivityManager.unregisterNetworkCallback(callback)
      }
    }
    .distinctUntilChanged()

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
      networkCapabilities: NetworkCapabilities,
    ) {
      super.onCapabilitiesChanged(network, networkCapabilities)
      val hasInternetCapability =
        networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
      val isConnected =
        networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

      val connected = hasInternetCapability && isConnected
      callback(if (connected) ConnectionState.Available else ConnectionState.Unavailable)
    }
  }
}
