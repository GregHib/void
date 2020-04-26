package rs.dusk.engine

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * @author Greg Hibberd <greg></greg>@greghibberd.com>
 * @since April 25, 2020
 */
internal class EngineTasksTest {

    lateinit var tasks: EngineTasks

    @BeforeEach
    fun setup() {
        tasks = EngineTasks()
    }

    @Test
    fun `Add to list`() {
        // Given
        val task = object : EngineTask() {
            override fun run() {
            }
        }
        // When
        tasks.add(task)
        // Then
        assert(tasks.contains(task))
    }

    @Test
    fun `Ordered by priority`() {
        // Given
        val high = object : EngineTask(priority = 4) {
            override fun run() {
            }
        }
        val medium = object : EngineTask(priority = 2) {
            override fun run() {
            }
        }
        val low = object : EngineTask(priority = 1) {
            override fun run() {
            }
        }
        tasks.data.add(medium)
        tasks.data.add(low)
        // When
        tasks.add(high)
        tasks.sort()
        // Then
        assertEquals(high, tasks.first())
        assertEquals(low, tasks.last())
    }
}