package org.redrune.engine.data.file

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.File

/**
 * @author Greg Hibberd <greg></greg>@greghibberd.com>
 * @since April 04, 2020
 */
internal class FileLoaderTest {


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
        val loader = FileLoader()
        // When
        val result = loader.load<TestData>(path)
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
        val loader = FileLoader()
        // When
        val result = loader.load<TestData>(path)
        // Then
        assertNull(result)
    }

    @Test
    fun save() {
        // Given
        val path = "test.yml"
        val loader = FileLoader()
        val data = TestData("Test message", TestEnum.FIRST)
        // When
        loader.save(path, data)
        // Then
        val file = File(path)
        assert(file.exists())
        assertEquals(
            """
            message: Test message
            value: FIRST
        """.trimIndent(), file.readText().trim()
        )
        // Teardown
        file.delete()
    }
}