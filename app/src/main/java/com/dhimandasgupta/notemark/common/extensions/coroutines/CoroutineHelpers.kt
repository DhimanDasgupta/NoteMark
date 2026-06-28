package com.dhimandasgupta.notemark.common.extensions.coroutines

import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.job
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withTimeout

/**
 * Executes a suspending [transform] on each element of the iterable in parallel, bounding the
 * concurrent executions by [parallelism].
 *
 * This is a "fire-and-forget" style operation where no results are returned. The function suspends
 * until all actions are complete, have timed out, or failed.
 *
 * @param parallelism The maximum number of coroutines to run in parallel.
 * @param timeoutMs The maximum time allowed for each [transform] to complete.
 * @param transform The suspending function to execute for each item.
 */
suspend fun <T> Iterable<T>.mapBounded(
  parallelism: Int,
  timeoutMs: Long = 2_000,
  transform: suspend (T) -> Unit,
) {
  val semaphore = Semaphore(permits = parallelism)
  supervisorScope {
    map { item ->
        async {
          semaphore.withPermit {
            withTimeout(timeMillis = timeoutMs) { transform(item) }
          }
        }
      }
      .awaitAll() // Wait for all async jobs to complete
  }
}

/**
 * Transforms each element of the iterable in parallel using [transform], filters out any null
 * results, and returns a list of the non-null results.
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
  transform: suspend (T) -> R?,
): List<R> {
  val semaphore = Semaphore(permits = parallelism)
  return supervisorScope {
      map { item ->
          async {
            semaphore.withPermit {
              withTimeout(timeMillis = timeoutMs) { transform(item) }
            }
          }
        }
        .awaitAll() // Wait for all async jobs to complete
    }
    .filterNotNull() // The only change is adding this
}

/**
 * Executes the provided transformation function asynchronously on each element of the given
 * iterable using coroutines and returns a list of the results in the same order.
 *
 * @param T The type of the elements in the source iterable.
 * @param R The type of the elements in the resulting list after transformation.
 * @param transformation A suspendable function that takes an element of type T and returns a
 *   transformed result of type R.
 * @return A list of transformed elements, maintaining the order of the original iterable.
 */
suspend fun <T, R> Iterable<T>.mapAsync(transformation: suspend (T) -> R): List<R> =
  coroutineScope {
    this@mapAsync.map { async { transformation(it) } }.awaitAll()
  }

/**
 * Applies a transformation function to each element in the iterable concurrently, respecting a
 * specified maximum level of concurrency.
 *
 * @param T The type of the elements in the input iterable.
 * @param R The type of the elements in the resulting list after applying the transformation.
 * @param concurrency The maximum number of transformations that can execute concurrently. Must be
 *   greater than 0.
 * @param transformation A suspendable function used to transform each element of the iterable.
 * @return A list containing the results of applying the transformation function to each element of
 *   the input iterable. The order of the resulting list corresponds to the order of the elements in
 *   the input iterable.
 */
suspend fun <T, R> Iterable<T>.mapAsync(
  concurrency: Int,
  transformation: suspend (T) -> R,
): List<R> = coroutineScope {
  val semaphore = Semaphore(concurrency)
  this@mapAsync.map { async { semaphore.withPermit { transformation(it) } } }.awaitAll()
}

/**
 * Executes multiple suspending functions concurrently, returning the result of the first one to
 * complete successfully while canceling the remaining ones.
 *
 * @param racer The first suspending function to execute.
 * @param racers Additional suspending functions to execute concurrently.
 * @return The result of the first completed suspending function.
 */
suspend fun <T> raceOf(
  racer: suspend CoroutineScope.() -> T,
  vararg racers: suspend CoroutineScope.() -> T,
): T = coroutineScope {
  select {
    (listOf(racer) + racers).forEach { racer ->
      async { racer() }
        .onAwait {
          coroutineContext.job.cancelChildren()
          it
        }
    }
  }
}

/**
 * Repeatedly attempts to execute the given operation, retrying based on a specified predicate.
 *
 * This function will invoke the `operation` and, if it throws an exception, it will evaluate the
 * `predicate` with the thrown exception and the current retry attempt count. If the predicate
 * returns `true`, the operation will be retried; otherwise, the exception will be propagated.
 *
 * @param T The return type of the operation.
 * @param predicate A function that takes a throwable and the current retry count, and returns
 *   `true` if a retry should occur, or `false` to propagate the exception.
 * @param operation The operation to be executed and retried if an exception occurs and the
 *   predicate allows.
 * @return The result of the successfully completed operation.
 * @throws Throwable The exception thrown by the operation if retries are exhausted or if the
 *   predicate returns `false`.
 */
inline fun <T> retryWhen(
  predicate: (Throwable, retries: Int) -> Boolean,
  operation: () -> T,
): T {
  var retries = 0
  var fromDownstream: Throwable? = null
  while (true) {
    try {
      return operation()
    } catch (e: Throwable) {
      if (fromDownstream != null) {
        e.addSuppressed(fromDownstream)
      }
      fromDownstream = e
      if (e is CancellationException || !predicate(e, retries++)) {
        throw e
      }
    }
  }
}

/**
 * Lazily initializes and provides access to a value computed by a suspendable initializer function.
 * The computation is synchronized to ensure a single execution even when accessed concurrently.
 *
 * @param initializer A suspend function that computes the value when invoked.
 * @return An implementation of [SuspendLazy], which provides lazy, suspendable access to the value.
 */
fun <T> suspendLazy(initializer: suspend () -> T): SuspendLazy<T> {
  var innerInitializer: (suspend () -> T)? = initializer
  val mutex = Mutex()
  var holder: Any? = Any()

  return object : SuspendLazy<T> {
    override val isInitialized: Boolean
      get() = innerInitializer == null

    @Suppress("UNCHECKED_CAST")
    override suspend operator fun invoke(): T =
      if (innerInitializer == null) holder as T
      else
        mutex.withLock {
          innerInitializer?.let {
            holder = it()
            innerInitializer = null
          }
          holder as T
        }
  }
}

interface SuspendLazy<T> {
  val isInitialized: Boolean

  suspend operator fun invoke(): T
}
