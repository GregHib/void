package rs.dusk.engine.task

import io.mockk.*
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class TaskExecutorTest {
    lateinit var executor: TaskExecutor

    @BeforeEach
    fun setup() {
        executor = TaskExecutor()
    }

    @Test
    fun `Executor is empty`() {
        assertTrue(executor.empty)
    }

    @Test
    fun `Executor scheduled isn't empty`() {
        executor.delay(0) {}
        assertFalse(executor.empty)
    }

    @Test
    fun `Scheduled task execution is empty`() {
        executor.delay(0) {}
        executor.run()
        assertTrue(executor.empty)
    }

    @Test
    fun `Cleared task execution is empty`() {
        executor.delay(0) {}
        executor.clear()
        assertTrue(executor.empty)
    }

    @Test
    fun `Task scheduled after 1 isn't empty after first`() {
        executor.delay(1) {}
        executor.run()
        assertFalse(executor.empty)
    }

    @Test
    fun `Task scheduled after 2 isn't empty after 1`() {
        executor.delay(2) {}
        executor.run()
        executor.run()
        assertFalse(executor.empty)
    }

    @Test
    fun `Task scheduled after 2 is empty after 2`() {
        executor.delay(2) {}
        executor.run()
        executor.run()
        executor.run()
        assertTrue(executor.empty)
    }

    @Test
    fun `Executable is ran`() {
        val runnable = spyk<(Long) -> Unit>({  })
        executor.delay(0, runnable)
        // When
        executor.run()
        // Then
        coVerify { runnable.invoke(0) }
    }

    @Test
    fun `Two tasks scheduled`() {
        val task1 = spyk<(Long) -> Unit>({  })
        val task2 = spyk<(Long) -> Unit>({  })
        executor.delay(0, task1)
        executor.delay(0, task2)
        // When
        executor.run()
        // Then
        assertTrue(executor.empty)
        coVerifyOrder {
            task1.invoke(0)
            task2.invoke(0)
        }
    }

    @Test
    fun `Two tasks scheduled at different times`() {
        val task1 = spyk<(Long) -> Unit>({  })
        val task2 = spyk<(Long) -> Unit>({  })
        executor.delay(2, task1)
        executor.delay(1, task2)
        // When
        executor.run()
        executor.run()
        // Then
        coVerify { task2.invoke(1) }
        coVerify(exactly = 0) { task1.invoke(any()) }
    }

    @Test
    fun `One task complete isn't empty`() {
        executor.delay(2) {}
        executor.delay(1) {}
        // When
        executor.run()
        // Then
        assertFalse(executor.empty)
    }

    @Test
    fun `Task nesting executes on the same tick`() {
        val task2 = spyk<(Long) -> Unit>({  })
        executor.delay(0) {
            executor.delay(0, task2)
        }
        // When
        executor.run()
        // Then
        assertTrue(executor.empty)
        coVerify { task2.invoke(0) }
    }

    @Test
    fun `Task doesn't have to be deleted`() {
        val task = spyk<(Long) -> Unit>({  })
        executor.repeat(task)
        // When
        executor.run()
        executor.run()
        // Then
        assertFalse(executor.empty)
        coVerify(exactly = 2) { task.invoke(any()) }
    }

    @Test
    fun `Task throwing exception doesn't break future tasks`() {
        val task1: (Long) -> Unit = mockk()
        every { task1.invoke(any()) } throws IllegalStateException()
        val task2 = spyk<(Long) -> Unit>({  })
        executor.delay(0, task1)
        executor.delay(0, task2)
        // When
        executor.run()
        // Then
        assertTrue(executor.empty)
        coVerify {
            task1.invoke(0)
            task2.invoke(0)
        }
    }

}