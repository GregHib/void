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
                val expected = ConfigReaderTest::class.java.getResourceAsStream("read/valid/${file.nameWithoutExtension}.txt")!!
                    .readBytes()
                    .toString(Charsets.UTF_8)
                    .replace("\r\n", "\n")
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
            builder.appendLine("[$section] $key = $value")
        }

    }
}