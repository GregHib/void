package rs.dusk.engine

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.koin.core.get
import org.koin.dsl.module
import rs.dusk.engine.script.KoinMock

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 03, 2020
 */
internal class EngineTaskTest : KoinMock() {

    private class TestTask(tasks: EngineTasks, priority: Int) : EngineTask(tasks, priority) {
        override fun run() {
        }
    }

    override val modules = listOf(engineModule, module {
        single { TestTask(get(), 1) }
    })

    @Test
    fun `Adds self to task list`() {
        // Given
        val tasks: EngineTasks = get()
        val updateTask: TestTask = get()
        // Then
        assert(tasks.contains(updateTask))
    }

    @Test
    fun `Multiple tasks are ordered by priority`() {
        // Given
        val tasks: EngineTasks = get()
        val third = TestTask(tasks, 1)
        val first = TestTask(tasks, 3)
        val second = TestTask(tasks, 2)
        // Then
        val expected = listOf(first, second, third)
        tasks.data.forEachIndexed { index, task ->
            assertEquals(expected[index], task)
        }
    }

}