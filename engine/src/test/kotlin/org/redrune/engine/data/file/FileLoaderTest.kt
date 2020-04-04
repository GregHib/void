package org.redrune.engine.data.file

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File

/**
 * @author Greg Hibberd <greg></greg>@greghibberd.com>
 * @since April 04, 2020
 */
internal class FileLoaderTest {

    private data class TestData(val message: String)

    @Test
    fun `Load data`() {
        // Given
        val path = "test.yml"
        val file = File(path)
        file.writeText("message: \"Test message\"")
        val loader = FileLoader()
        // When
        val result = loader.load<TestData>(path)
        // Then
        assertNotNull(result)
        assertEquals("Test message", result!!.message)
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
        val data = TestData("Test message")
        // When
        loader.save(path, data)
        // Then
        val file = File(path)
        assert(file.exists())
        println(file.readText())
        assertEquals("message: \"Test message\"", file.readText().trim())
        // Teardown
        file.delete()
    }
}