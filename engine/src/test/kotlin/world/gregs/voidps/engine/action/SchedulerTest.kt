package world.gregs.voidps.engine.action

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.withTimeout
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class SchedulerTest {

    private lateinit var scheduler: Scheduler

    @BeforeEach
    fun setup() {
        scheduler = Scheduler(TestCoroutineDispatcher())
    }

    @Test
    fun `Await sync to schedule`() {
        var synced = false
        scheduler.sync {
            synced = true
        }

        scheduler.run()

        assertTrue(synced)
    }

    @Test
    fun `Await no ticks`() = runBlocking {
        withTimeout(100) {
            scheduler.await(0)
        }
    }

    @Test
    fun `Await one tick`() = runBlocking {
        withTimeout(100) {
            launch {
                scheduler.run()
            }
            scheduler.await(1)
        }
    }

    @Test
    fun `Await 5 ticks`(): Unit = runBlocking {
        withTimeout(100) {
            launch {
                repeat(5) {
                    scheduler.run()
                }
            }
            scheduler.await(5)
        }
    }
}