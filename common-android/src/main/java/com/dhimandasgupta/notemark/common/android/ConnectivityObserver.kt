package com.dhimandasgupta.notemark.common.android

import kotlinx.coroutines.flow.Flow

/**
 * Source of internet connectivity changes, so callers don't depend on [android.content.Context].
 */
fun interface ConnectivityObserver {
  fun observe(): Flow<ConnectionState>
}
