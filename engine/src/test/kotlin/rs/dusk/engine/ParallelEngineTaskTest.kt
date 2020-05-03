package rs.dusk.engine

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import org.junit.jupiter.api.Test
import kotlin.system.measureTimeMillis

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 03, 2020
 */
internal class ParallelEngineTaskTest {

    private class TestTask(tasks: EngineTasks) : ParallelEngineTask(tasks)

    @Test
    fun `Tasks run in parallel`() {
        // Given
        val tasks = EngineTasks()
        val first = TestTask(tasks)
        first.defers.add(GlobalScope.async {
            delay(100)
        })
        first.defers.add(GlobalScope.async {
            delay(100)
        })
        // When
        val took = measureTimeMillis {
            first.run()
        }
        assert(took < 200)// This could be temperamental
    }
}