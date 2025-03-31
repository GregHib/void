package world.gregs.voidps.engine

import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@Disabled("Flaky")
internal class GameLoopTest {

    private lateinit var loop: GameLoop

    private val stages = mutableListOf<Runnable>()

    @BeforeEach
    fun setup() {
        loop = GameLoop(stages, 1L)
    }

    @Test
    fun `Start game loop`() = runTest {
        var count = 0
        stages.add(Runnable {
            count++
        })

        val job = loop.start(this)
        delay(5)

        // Then
        assertEquals(5, count)
        job.cancelAndJoin()
    }

    @Test
    fun `Cancel job early`() = runTest {
        var count = 0
        stages.add(Runnable {
            count++
        })

        val job = loop.start(this)
        delay(2)
        job.cancel()
        delay(2)

        // Then
        assertEquals(2, count)
        job.join()
    }

    @Test
    fun `Exception in stage`() = runTest {
        var count = 0
        stages.add(Runnable {
            count++
        })
        stages.add(Runnable {
            throw IllegalStateException("Test")
        })

        val job = loop.start(this)
        delay(5)

        // Then
        assertFalse(job.isActive)
        assertEquals(1, count)
    }
}