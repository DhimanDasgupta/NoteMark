package com.dhimandasgupta.notemark.common.extensions.coroutines

import kotlin.time.Duration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.internal.SynchronizedObject

/**
 * A utility class that manages a collection of shared Flows, ensuring proper lifecycle management
 * and resource sharing for concurrent consumers in a coroutine scope.
 *
 * @param K The type of the key used to identify and retrieve shared Flows.
 * @param V The type of the values emitted by the shared Flows.
 * @property scope The CoroutineScope in which the shared Flows will operate.
 * @property replayExpiration The duration after which the sharing stops if no collectors are
 *   present. Defaults to [Duration.INFINITE].
 * @property stopTimeout The duration to wait before stopping the sharing after all consumers have
 *   unsubscribed. Defaults to [Duration.ZERO].
 * @property replay The number of most recent items to replay to new collectors.
 * @property builder A function that defines how to build a Flow when a requested key is not already
 *   present in the collection.
 */
class SharedDataSource<K, V>(
  private val scope: CoroutineScope,
  private val replayExpiration: Duration = Duration.INFINITE,
  private val stopTimeout: Duration = Duration.ZERO,
  private val replay: Int = 0,
  private val builder: (K) -> Flow<V>,
) {
  private val connections = mutableMapOf<K, SharedFlow<V>>()
  @OptIn(InternalCoroutinesApi::class) private val lock = object : SynchronizedObject() {}

  @OptIn(InternalCoroutinesApi::class)
  fun get(key: K): SharedFlow<V> =
    synchronized(lock) {
      connections.getOrPut(key) {
        builder(key)
          .shareIn(
            scope,
            started =
              SharingStarted.WhileSubscribed(
                replayExpirationMillis = replayExpiration.inWholeMilliseconds,
                stopTimeoutMillis = stopTimeout.inWholeMilliseconds,
              ),
            replay = replay,
          )
      }
    }

  @OptIn(InternalCoroutinesApi::class)
  fun all(): List<SharedFlow<V>> =
    synchronized(lock) {
      connections.values.toList()
    }

  @OptIn(InternalCoroutinesApi::class)
  fun clear() =
    synchronized(lock) {
      connections.clear()
    }
}
