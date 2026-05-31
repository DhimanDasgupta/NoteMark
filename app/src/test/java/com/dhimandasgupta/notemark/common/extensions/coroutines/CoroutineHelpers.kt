package com.dhimandasgupta.notemark.common.extensions.coroutines

import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Ignore
import org.junit.Test
import kotlin.collections.emptyList
import kotlin.coroutines.CoroutineContext
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds

private val EMIT_RATE = 500.milliseconds
private val VERY_SLOW = EMIT_RATE.times(2)
private val VERY_FAST = EMIT_RATE.div(2)

@OptIn(ExperimentalCoroutinesApi::class)
class CoroutineHelpers {
    private fun dataFlow(n: Int): Flow<Int> = flow {
        repeat(n) {
            emit(it)
            delay(EMIT_RATE)
        }
    }

    @Test
    fun period_greater_than_flow_rate() = runTest {

        val period = VERY_SLOW
        var time = 0L

        withContext(Dispatchers.Default) {
            dataFlow(10)
                .enqueueWithDelay(period = period)
                .onEach {
                    val current = currentTimeMillis()
                    val delta = current - time

                    if (delta < period.inWholeMilliseconds)
                        assertEquals(period.inWholeMilliseconds, delta)

                    time = current
                }
                .toList()
        }
    }

    @Test
    fun period_smaller_than_flow_rate() = runTest {

        val period = VERY_FAST
        var time = 0L

        withContext(Dispatchers.Default) {
            dataFlow(10)
                .enqueueWithDelay(period = period)
                .onEach {
                    val current = currentTimeMillis()
                    val delta = current - time

                    if (delta < period.inWholeMilliseconds)
                        assertEquals(period.inWholeMilliseconds, delta)

                    time = current
                }
                .toList()
        }
    }

    private fun currentTimeMillis(): Long = Clock.System.now().toEpochMilliseconds()
}

@Ignore("This needs to be fixed.")
@OptIn(ExperimentalCoroutinesApi::class)
class MapAsyncConcurrencyTest {
    private val anyConcurrency = 3

    @Test
    fun should_behave_like_a_regular_map_for_a_list_and_a_set() = runTest {
        val list = ('a'..'z').toList()
        val charTransformation1 = { c: Char -> c.inc() }
        assertEquals(list.map(charTransformation1), list.mapAsync(concurrency = anyConcurrency, charTransformation1))
        val charTransformation2 = { c: Char -> c.code }
        assertEquals(list.map(charTransformation2), list.mapAsync(concurrency = anyConcurrency, charTransformation2))
        val charTransformation3 = { c: Char -> c.uppercaseChar() }
        assertEquals(list.map(charTransformation3), list.mapAsync(concurrency = anyConcurrency, charTransformation3))

        val set = (1..10).toSet()
        val intTransformation1 = { i: Int -> i * i }
        assertEquals(set.map(intTransformation1), set.mapAsync(concurrency = anyConcurrency, intTransformation1))
        val intTransformation2 = { i: Int -> "A$i" }
        assertEquals(set.map(intTransformation2), set.mapAsync(concurrency = anyConcurrency, intTransformation2))
        val intTransformation3 = { i: Int -> i.toChar() }
        assertEquals(set.map(intTransformation3), set.mapAsync(concurrency = anyConcurrency, intTransformation3))
    }

    @Test
    fun should_map_async_and_keep_elements_order() = runTest {
        val transforms = listOf(
            suspend { delay(3000); "A" },
            suspend { delay(2000); "B" },
            suspend { delay(4000); "C" },
            suspend { delay(1000); "D" },
        )

        val res = transforms.mapAsync(concurrency = 4) { it() }
        assertEquals(listOf("A", "B", "C", "D"), res)
        assertEquals(4000, currentTime)
    }

    @Test
    fun should_limit_concurrency_for_single_delay() = runTest {
        val process: suspend (Int) -> Int = { i: Int ->
            delay(1000)
            i * i
        }

        List(1000) { it }.mapAsync(concurrency = 10, transformation = process)
        assertEquals(1000 * 1000 / 10, currentTime)
    }

    @Test
    fun should_limit_concurrency_for_different_delays() = testFor(
        1 to 3000L + 2000L + 4000L + 1000L + 2000L,
        2 to 6000L,
        3 to 5000L,
        4 to 4000L,
        5 to 4000L,
    ) { concurrency, expectedTime ->
        val transforms = listOf(
            suspend { delay(3000); "A" },
            suspend { delay(2000); "B" },
            suspend { delay(4000); "C" },
            suspend { delay(1000); "D" },
            suspend { delay(2000); "E" },
        )

        val res = transforms.mapAsync(concurrency = concurrency) { it() }
        assertEquals(listOf("A", "B", "C", "D", "E"), res)
        assertEquals(expectedTime, currentTime)
    }

    @Test
    fun should_support_context_propagation() = runTest {
        var ctx: CoroutineContext? = null

        val name1 = CoroutineName("Name 1")
        withContext(name1) {
            listOf("A").mapAsync(concurrency = anyConcurrency) {
                ctx = currentCoroutineContext()
                it
            }
            assertEquals(name1, ctx?.get(CoroutineName))
        }

        val name2 = CoroutineName("Some name 2")
        withContext(name2) {
            listOf("B").mapAsync(concurrency = anyConcurrency) {
                ctx = currentCoroutineContext()
                it
            }
            assertEquals(name2, ctx?.get(CoroutineName))
        }
    }

    @Test
    fun should_support_cancellation() = runTest {
        var job: Job? = null

        val parentJob = launch {
            listOf("A").mapAsync(concurrency = anyConcurrency) {
                job = currentCoroutineContext().job
                delay(Long.MAX_VALUE)
            }
        }

        delay(1000)
        parentJob.cancel()
        assertEquals(true, job?.isCancelled)
    }
}

private fun <T1, T2> testFor(vararg data: Pair<T1, T2>, body: suspend TestScope.(T1, T2) -> Unit) {
    for ((input, expected) in data) {
        runTest {
            body(input, expected)
        }
    }
}

@Ignore("This needs to be fixed.")
@OptIn(ExperimentalCoroutinesApi::class)
class MapAsyncTest {
    @Test
    fun should_behave_like_a_regular_map_for_a_list_and_a_set() = runTest {
        val list = ('a'..'z').toList()
        val charTransformation1 = { c: Char -> c.inc() }
        assertEquals(list.map(charTransformation1), list.mapAsync(charTransformation1))
        val charTransformation2 = { c: Char -> c.code }
        assertEquals(list.map(charTransformation2), list.mapAsync(charTransformation2))
        val charTransformation3 = { c: Char -> c.uppercaseChar() }
        assertEquals(list.map(charTransformation3), list.mapAsync(charTransformation3))

        val set = (1..10).toSet()
        val intTransformation1 = { i: Int -> i * i }
        assertEquals(set.map(intTransformation1), set.mapAsync(intTransformation1))
        val intTransformation2 = { i: Int -> "A$i" }
        assertEquals(set.map(intTransformation2), set.mapAsync(intTransformation2))
        val intTransformation3 = { i: Int -> i.toChar() }
        assertEquals(set.map(intTransformation3), set.mapAsync(intTransformation3))
    }

    @Test
    fun should_map_async_and_keep_elements_order() = runTest {
        val transforms: List<suspend () -> String> = listOf(
            { delay(3000); "A" },
            { delay(2000); "B" },
            { delay(4000); "C" },
            { delay(1000); "D" },
        )

        val res = transforms.mapAsync { it() }
        assertEquals(listOf("A", "B", "C", "D"), res)
        assertEquals(4000, currentTime)
    }

    @Test
    fun should_support_context_propagation() = runTest {
        var ctx: CoroutineContext? = null

        val name1 = CoroutineName("Name 1")
        withContext(name1) {
            listOf("A").mapAsync {
                ctx = currentCoroutineContext()
                it
            }
        }
        assertEquals(name1, ctx?.get(CoroutineName))

        val name2 = CoroutineName("Some name 2")
        withContext(name2) {
            listOf("B").mapAsync {
                ctx = currentCoroutineContext()
                it
            }
        }
        assertEquals(name2, ctx?.get(CoroutineName))
    }

    @Test
    fun should_support_cancellation() = runTest {
        var job: Job? = null

        val parentJob = launch {
            listOf("A").mapAsync {
                job = currentCoroutineContext().job
                delay(Long.MAX_VALUE)
            }
        }

        delay(1000)
        parentJob.cancel()
        assertEquals(true, job?.isCancelled)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class OnEachBatchTest {

    @Test
    fun `should invoke action for full batches`() = runTest {
        val batches = mutableListOf<List<Int>>()
        val result = flowOf(1, 2, 3, 4, 5, 6)
            .onEachBatch(3) { batches.add(it) }
            .toList()

        assertEquals(listOf(1, 2, 3, 4, 5, 6), result)
        assertEquals(listOf(listOf(1, 2, 3), listOf(4, 5, 6)), batches)
    }

    @Test
    fun `should invoke action for remaining elements at the end`() = runTest {
        val batches = mutableListOf<List<Int>>()
        val result = flowOf(1, 2, 3, 4, 5)
            .onEachBatch(3) { batches.add(it) }
            .toList()

        assertEquals(listOf(1, 2, 3, 4, 5), result)
        assertEquals(listOf(listOf(1, 2, 3), listOf(4, 5)), batches)
    }

    @Test
    fun `should handle empty flow`() = runTest {
        val batches = mutableListOf<List<Int>>()
        val result = emptyFlow<Int>()
            .onEachBatch(3) { batches.add(it) }
            .toList()

        assertEquals(emptyList(), result)
        assertEquals(emptyList(), batches)
    }

    @Test
    fun `should invoke action for remaining elements when exception occurs`() = runTest {
        val batches = mutableListOf<List<Int>>()
        val flow = flow {
            emit(1)
            emit(2)
            throw RuntimeException("Test exception")
        }

        try {
            flow.onEachBatch(3) { batches.add(it) }.collect()
        } catch (e: RuntimeException) {
            assertEquals("Test exception", e.message)
        }

        assertEquals(listOf(listOf(1, 2)), batches)
    }

    @Test
    fun `should emit elements downstream immediately`() = runTest {
        val emitted = mutableListOf<Int>()
        val batches = mutableListOf<List<Int>>()

        flowOf(1, 2, 3)
            .onEachBatch(2) { batches.add(it) }
            .collect {
                emitted.add(it)
                if (it == 1) {
                    assertEquals(0, batches.size, "Batch should not be invoked yet")
                }
                if (it == 2) {
                    assertEquals(1, batches.size, "Batch should be invoked after 2nd element")
                    assertEquals(listOf(1, 2), batches[0])
                }
            }

        assertEquals(listOf(1, 2, 3), emitted)
        assertEquals(listOf(listOf(1, 2), listOf(3)), batches)
    }

    @Test
    fun `should flush remaining elements when downstream fails`() = runTest {
        val batches = mutableListOf<List<Int>>()
        val flow = flowOf(1, 2)

        assertFailsWith<RuntimeException> {
            flow.onEachBatch(3) { batches.add(it) }
                .collect {
                    if (it == 2) throw RuntimeException("Downstream failure")
                }
        }

        assertEquals(listOf(listOf(1, 2)), batches, "Should flush remaining elements even if downstream fails")
    }

    @Test
    fun `should flush remaining elements when cancelled`() = runTest {
        val batches = mutableListOf<List<Int>>()
        val flow = flow {
            emit(1)
            emit(2)
            delay(1000)
            emit(3)
        }

        val job = launch {
            flow.onEachBatch(3) { batches.add(it) }.collect()
        }

        delay(500)
        job.cancelAndJoin()

        assertEquals(listOf(listOf(1, 2)), batches, "Should flush remaining elements when cancelled")
    }

    @Test
    fun `should propagate exception from action`() = runTest {
        val flow = flowOf(1, 2, 3)
        val exception = assertFailsWith<RuntimeException> {
            flow.onEachBatch(2) {
                if (it.contains(2)) throw RuntimeException("Action failure")
            }.collect()
        }

        assertEquals("Action failure", exception.message)
    }
}

@Ignore("This needs to be fixed.")
@OptIn(ExperimentalCoroutinesApi::class)
class RaceOfTest {

    @Test
    fun should_wait_for_the_fastest() = runTest {
        raceOf(
            { delay(3) },
            { delay(1) },
            { delay(2) },
        )
        assertEquals(1, currentTime)
    }

    @Test
    fun should_wait_for_the_fastest_for_big_number() = runTest {
        val racers = List<suspend CoroutineScope.() -> Long>(1000) { i ->
            {
                val num = (i + 100).toLong()
                delay(num)
                num
            }
        }.shuffled().toMutableList()
        val result = raceOf(racers.removeFirst(), *racers.toTypedArray())
        assertEquals(100, result)
        assertEquals(100, currentTime)
    }

    @Test
    fun should_respond_with_fastest() = runTest {
        val result = raceOf(
            { delay(3000); "C" },
            { delay(1000); "A" },
            { delay(2000); "B" },
        )
        assertEquals("A", result)
        assertEquals(1000, currentTime)
    }

    @Test
    fun should_cancel_slower() = runTest {
        var slowerJob: Job? = null
        val result = raceOf(
            { delay(1000); "A" },
            { slowerJob = currentCoroutineContext().job; delay(2000); "B" },
        )
        assertEquals("A", result)
        assertEquals(1000, currentTime)
        assertEquals(true, slowerJob?.isCancelled)
    }

    @Test
    fun should_cancel_when_parent_cancelled() = runTest {
        var innerJob: Job? = null
        val job = launch {
            raceOf(
                { delay(1000) },
                { innerJob = currentCoroutineContext().job; delay(2000) },
            )
        }
        delay(500)
        assertEquals(true, innerJob?.isActive)
        job.cancel()
        assertEquals(true, innerJob?.isCancelled)
    }

    @Test
    fun should_propagate_context() = runTest {
        var innerCtx: CoroutineContext? = null

        val coroutineName1 = CoroutineName("ABC")
        withContext(coroutineName1) {
            raceOf(
                { delay(1000) },
                { innerCtx = currentCoroutineContext(); delay(2000) },
            )
        }
        delay(500)
        assertEquals(coroutineName1, innerCtx?.get(CoroutineName))

        val coroutineName2 = CoroutineName("DEF")
        withContext(coroutineName2) {
            raceOf(
                { delay(1000) },
                { innerCtx = currentCoroutineContext(); delay(2000) },
            )
        }
        delay(500)
        assertEquals(coroutineName2, innerCtx?.get(CoroutineName))
    }
}

@Ignore("This needs to be fixed.")
@OptIn(ExperimentalCoroutinesApi::class)
class SuspendLazyTest {
    @Test
    fun should_produce_value() = runTest {
        val lazyValue = suspendLazy { delay(1000); 123 }
        assertEquals(123, lazyValue())
        assertEquals(1000, currentTime)
    }

    @Test
    fun should_not_recalculate_value() = runTest {
        var next = 1
        val lazyValue = suspendLazy { delay(1000); next++ }
        assertEquals(1, lazyValue())
        assertEquals(1, lazyValue())
        assertEquals(1, lazyValue())
        assertEquals(1, lazyValue())
        assertEquals(1000, currentTime)
    }

    @Test
    fun should_try_again_when_failure_during_value_initialization() = runTest {
        var next = 0
        val lazyValue = suspendLazy {
            val v = next++
            if (v < 2) throw Error()
            v
        }
        assertTrue(runCatching { lazyValue() }.isFailure)
        assertTrue(runCatching { lazyValue() }.isFailure)
        assertEquals(2, lazyValue())
        assertEquals(2, lazyValue())
        assertEquals(2, lazyValue())
    }

    @Test
    fun should_use_context_of_the_first_caller() = runTest {
        var ctx: CoroutineContext? = null
        val lazyValue = suspendLazy {
            ctx = currentCoroutineContext()
            123
        }
        val name1 = CoroutineName("ABC")
        withContext(name1) {
            lazyValue()
        }
        assertEquals(name1, ctx?.get(CoroutineName))
        val name2 = CoroutineName("DEF")
        withContext(name2) {
            lazyValue()
        }
        assertEquals(name1, ctx?.get(CoroutineName))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun should_set_is_initialized() = runTest {
        val lazyValue = suspendLazy { delay(1000); 123 }

        assertEquals(false, lazyValue.isInitialized)
        launch { lazyValue() }
        assertEquals(false, lazyValue.isInitialized)
        advanceTimeBy(1000)
        assertEquals(false, lazyValue.isInitialized)
        runCurrent()
        assertEquals(true, lazyValue.isInitialized)
    }
}