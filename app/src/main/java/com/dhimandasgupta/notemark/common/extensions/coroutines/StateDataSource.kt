package com.dhimandasgupta.notemark.common.extensions.coroutines

import kotlin.time.Duration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.internal.SynchronizedObject

/**
 * A data source class that provides on-demand access to `StateFlow` instances which are created for
 * specific keys and backed by a builder function.
 *
 * @param K The type of keys used to access the `StateFlow` instances.
 * @param V The type of values emitted by the `StateFlow` instances.
 * @param scope The coroutine scope in which the created flows will operate.
 * @param initial The initial value of the `StateFlow` instances created by this data source.
 * @param replayExpiration The duration for which `StateFlow`s continue replaying values after being
 *   unsubscribed, before they are stopped. Defaults to infinite duration.
 * @param stopTimeout The duration to wait before stopping the flow after an unsubscribed event.
 *   Defaults to zero duration.
 * @param builder A function that creates a `Flow<V>` for a given key.
 */
class StateDataSource<K, V>(
  private val scope: CoroutineScope,
  private val initial: V,
  private val replayExpiration: Duration = Duration.INFINITE,
  private val stopTimeout: Duration = Duration.ZERO,
  private val builder: (K) -> Flow<V>,
) {
  private val connections = mutableMapOf<K, StateFlow<V>>()

  @OptIn(InternalCoroutinesApi::class) private val lock = object : SynchronizedObject() {}

  @OptIn(InternalCoroutinesApi::class)
  fun get(key: K): StateFlow<V> =
    synchronized(lock) {
      connections.getOrPut(key) {
        builder(key)
          .stateIn(
            scope = scope,
            initialValue = initial,
            started =
              SharingStarted.WhileSubscribed(
                replayExpirationMillis = replayExpiration.inWholeMilliseconds,
                stopTimeoutMillis = stopTimeout.inWholeMilliseconds,
              ),
          )
      }
    }

  @OptIn(InternalCoroutinesApi::class)
  fun all(): List<StateFlow<V>> =
    synchronized(lock) {
      connections.values.toList()
    }

  @OptIn(InternalCoroutinesApi::class)
  fun clear() =
    synchronized(lock) {
      connections.clear()
    }
}
