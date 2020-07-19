package rs.dusk.engine.task

import io.mockk.mockk
import io.mockk.verifyOrder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class StartTaskTest {

    @Test
    fun `Run invokes all sub tasks`() {
        // Given
        val task1: (Long) -> Unit = mockk(relaxed = true)
        val task2: (Long) -> Unit = mockk(relaxed = true)
        val startTask = StartTask()
        startTask.subTasks.add(task1)
        startTask.subTasks.add(task2)
        // When
        startTask.run(0)
        // Then
        assertEquals(0, startTask.subTasks.size)
        verifyOrder {
            task1.invoke(0)
            task2.invoke(0)
        }
    }

}