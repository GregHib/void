package world.gregs.voidps.engine.data.file

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.File

internal class FileStorageTest {


    private enum class TestEnum {
        FIRST,
        SECOND
    }

    private data class TestData(val message: String, val value: TestEnum)

    @ValueSource(
        strings = ["""
        message: "Test message"
        value: SECOND
    """, """
        message: Test message
        value: SECOND
    """]
    )
    @ParameterizedTest
    fun `Load data`(text: String) {
        // Given
        val path = "test.yml"
        val file = File(path)
        file.writeText(text)
        val storage = FileStorage()
        // When
        val result = storage.loadOrNull<TestData>(path)
        // Then
        assertNotNull(result)
        assertEquals("Test message", result!!.message)
        assertEquals(TestEnum.SECOND, result.value)
        // Teardown
        file.delete()
    }

    @Test
    fun `Load no data`() {
        // Given
        val path = "invalid.yml"
        val storage = FileStorage()
        // When
        val result = storage.loadOrNull<TestData>(path)
        // Then
        assertNull(result)
    }
}