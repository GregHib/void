package rs.dusk.engine.task

import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class RepeatTaskTest {

    @Test
    fun `Is time to run`() {
        // Given
        val task = RepeatTask(mockk())
        // When
        val result = task.isTimeToRun(-1)
        // Then
        assertTrue(result)
    }

    @Test
    fun `Isn't time to run if cancelled`() {
        // Given
        val task = RepeatTask(mockk())
        task.cancel()
        // When
        val result = task.isTimeToRun(3)
        // Then
        assertFalse(result)
    }

    @Test
    fun `Isn't time to remove`() {
        // Given
        val task = RepeatTask(mockk())
        // When
        val result = task.isTimeToRemove(0)
        // Then
        assertFalse(result)
    }

    @Test
    fun `Is time to remove if cancelled`() {
        // Given
        val task = RepeatTask(mockk())
        task.cancel()
        // When
        val result = task.isTimeToRemove(0)
        // Then
        assertTrue(result)
    }

    @Test
    fun `Run executes task`() {
        // Given
        val block: (Long) -> Unit = mockk(relaxed = true)
        val task = RepeatTask(block)
        task.cancel()
        // When
        task.run(2)
        // Then
        verify { block.invoke(2) }
    }
}