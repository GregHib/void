package world.gregs.config

import net.pearx.kasechange.toSentenceCase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertThrows
import java.io.File

class ConfigReaderTest {
    @TestFactory
    fun `Readable configs`() = File(ConfigReaderTest::class.java.getResource("read/valid/")!!.file)
        .listFiles { f -> f.isFile && f.extension == "toml" }!!
        .map { file ->
            dynamicTest(file.nameWithoutExtension.toSentenceCase()) {
                val reader = ConfigPrinter()
                reader.parse(file.inputStream().buffered())
                val expected = ConfigReaderTest::class.java.getResourceAsStream("read/valid/${file.nameWithoutExtension}.txt")!!.readBytes().toString(Charsets.UTF_8)
                assertEquals(expected, reader.builder.toString())
            }
        }

    @TestFactory
    fun `Invalid configs`() = File(ConfigReaderTest::class.java.getResource("read/invalid/")!!.file)
        .listFiles { f -> f.isFile && f.extension == "toml" }!!
        .map { file ->
            dynamicTest(file.nameWithoutExtension.toSentenceCase()) {
                val reader = ConfigPrinter()
                assertThrows<IllegalArgumentException> {
                    reader.parse(file.inputStream().buffered())
                }
            }
        }

    private class ConfigPrinter : ConfigReader() {
        override val buffer: ByteArray = ByteArray(1024)
        val builder = StringBuilder()

        override fun set(section: String, key: String, value: Any) {
            println("[$section] $key = $value")
            builder.appendLine("[$section] $key = $value")
        }

    }


    @Suppress("UNCHECKED_CAST")
    private fun printJson(builder: StringBuilder, any: Any) {
        when (any) {
            is Map<*, *> -> {
                builder.append('{')
                var first = true
                for ((key, value) in (any as Map<String, Any>).toSortedMap()) {
                    if (first) {
                        first = false
                    } else {
                        builder.append(',')
                    }
                    builder.append("\"${escapeString(key)}\":")
                    printJson(builder, value!!)
                }
                builder.append('}')
            }
            is List<*> -> {
                builder.append('[')
                var first = true
                for (value in any) {
                    if (first) {
                        first = false
                    } else {
                        builder.append(',')
                    }
                    printJson(builder, value!!)
                }
                builder.append(']')
            }
            is Array<*> -> {
                builder.append('[')
                var first = true
                for (value in any) {
                    if (first) {
                        first = false
                    } else {
                        builder.append(',')
                    }
                    printJson(builder, value!!)
                }
                builder.append(']')
            }
            is String -> {
                builder.append('\"')
                builder.append(escapeString(any))
                builder.append('\"')
            }
            else -> builder.append(any)
        }
    }

    private fun escapeString(input: String): String {
        var previous = ' '
        for (char in input) {
            if (char == '"' && previous != '\\') {
                return input.replace("\"", "\\\"")
            }
            previous = char
        }
        return input
    }

}