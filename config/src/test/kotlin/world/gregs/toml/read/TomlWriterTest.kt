package world.gregs.toml.read

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.toml.Toml
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.StringWriter

internal class TomlWriterTest {

    private val writer = TomlWriter()

    @Test
    fun `Key value pair`() {
        val stringWriter = StringWriter()
        BufferedWriter(stringWriter).use { buffer ->
            writer.write(buffer, mapOf("key" to "value"))
        }
        assertEquals("key = \"value\"\n", stringWriter.buffer.toString())
    }

    @Test
    fun `Nested map`() {
        val stringWriter = StringWriter()
        BufferedWriter(stringWriter).use { buffer ->
            writer.write(buffer, mapOf("test" to mapOf("key" to "value")))
        }

        assertEquals("[test]\nkey = \"value\"\n\n", stringWriter.buffer.toString())
    }

    @Test
    fun `List strings`() {
        val stringWriter = StringWriter()
        BufferedWriter(stringWriter).use { buffer ->
            writer.write(buffer, mapOf("list" to listOf("a", "b", "c")))
        }

        assertEquals("list = [\"a\", \"b\", \"c\"]\n", stringWriter.buffer.toString())
    }

    @Test
    fun `Collection test`() {
    }

    @Test
    fun `Benchmark test`() {
        val out = File("./output-test.toml")
        val writing = TomlWriter()
        val map = Toml.decodeFromFile("C:\\Users\\Greg\\AppData\\Roaming\\JetBrains\\IntelliJIdea2024.3\\scratches\\scratch.toml")
        println(map)
        val count = 1
        var total = 0L
        for (i in 0 until count) {
            val writer = BufferedWriter(FileWriter(out))
            val start = System.nanoTime()
            writing.write(writer, map)
            val end = System.nanoTime()
            writer.close()
            total += end - start
        }
        println("Took ${total/ count}ns")
    }


}