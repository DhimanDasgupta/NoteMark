package com.dhimandasgupta.notemark.ui.common

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withTimeout

/**
 * Executes a suspending [transform] on each element of the iterable in parallel,
 * bounding the concurrent executions by [parallelism].
 *
 * This is a "fire-and-forget" style operation where no results are returned.
 * The function suspends until all actions are complete, have timed out, or failed.
 *
 * @param parallelism The maximum number of coroutines to run in parallel.
 * @param timeoutMs The maximum time allowed for each [transform] to complete.
 * @param transform The suspending function to execute for each item.
 */
suspend fun <T> Iterable<T>.mapBounded(
    parallelism: Int,
    timeoutMs: Long = 2_000,
    transform: suspend (T) -> Unit
) {
    val semaphore = Semaphore(permits = parallelism)
    supervisorScope {
        map { item ->
            async {
                semaphore.withPermit {
                    withTimeout(timeMillis = timeoutMs) { transform(item) }
                }
            }
        }.awaitAll() // Wait for all async jobs to complete
    }
}

/**
 * Transforms each element of the iterable in parallel using [transform],
 * filters out any null results, and returns a list of the non-null results.
 *
 * Concurrency is bounded by [parallelism].
 *
 * @param parallelism The maximum number of coroutines to run in parallel.
 * @param timeoutMs The maximum time allowed for each [transform] to complete.
 * @param transform The suspending function to execute for each item, returning R?
 * @return A [List] containing only the non-null results of the [transform].
 */
suspend fun <T, R : Any> Iterable<T>.mapNotNullBounded(
    parallelism: Int,
    timeoutMs: Long = 2_000,
    transform: suspend (T) -> R?
): List<R> {
    val semaphore = Semaphore(permits = parallelism)
    return supervisorScope {
        map { item ->
            async {
                semaphore.withPermit {
                    withTimeout(timeMillis = timeoutMs) { transform(item) }
                }
            }
        }.awaitAll()  // Wait for all async jobs to complete
    }.filterNotNull() // The only change is adding this
}