package world.gregs.toml.read

import org.junit.jupiter.api.Test
import java.io.File

class TomlStreamTest {

    @Test
    fun `Streaming test`() {
        val file = File("C:\\Users\\Greg\\AppData\\Roaming\\JetBrains\\IntelliJIdea2024.3\\scratches\\scratch.toml")
        val streaming = TomlStream()
        val api = printApi()
        val buffer = ByteArray(1024)
        val address = Array<Any>(10) { "" }
        streaming.read(file.inputStream().buffered(), api, buffer, address)
    }

    private fun printApi() = object : TomlStream.Api {
        override fun table(addressBuffer: Array<Any>, addressSize: Int) {
            println("api.table(${addressBuffer.take(addressSize)})")
        }

        override fun inlineTable(addressBuffer: Array<Any>, addressSize: Int) {
            println("api.inlineTable(${addressBuffer.take(addressSize)})")
        }

        override fun appendMap(addressBuffer: Array<Any>, addressSize: Int, key: String, value: Double) {
            println("api.appendMap(${addressBuffer.take(addressSize)}, $key, $value)")
        }

        override fun appendMap(addressBuffer: Array<Any>, addressSize: Int, key: String, value: Long) {
            println("api.appendMap(${addressBuffer.take(addressSize)}, $key, $value)")
        }

        override fun appendMap(addressBuffer: Array<Any>, addressSize: Int, key: String, value: String) {
            println("api.appendMap(${addressBuffer.take(addressSize)}, $key, $value)")
        }

        override fun appendMap(addressBuffer: Array<Any>, addressSize: Int, key: String, value: Boolean) {
            println("api.appendMap(${addressBuffer.take(addressSize)}, $key, $value)")
        }

        override fun mapEnd(addressBuffer: Array<Any>, addressSize: Int) {
            println("api.mapEnd(${addressBuffer.take(addressSize)})")
        }

        override fun list(addressBuffer: Array<Any>, addressSize: Int) {
            println("api.list(${addressBuffer.take(addressSize)})")
        }

        override fun appendList(addressBuffer: Array<Any>, addressSize: Int, value: Double) {
            println("api.appendList(${addressBuffer.take(addressSize)}, $value)")
        }

        override fun appendList(addressBuffer: Array<Any>, addressSize: Int, value: Long) {
            println("api.appendList(${addressBuffer.take(addressSize)}, $value)")
        }

        override fun appendList(addressBuffer: Array<Any>, addressSize: Int, value: String) {
            println("api.appendList(${addressBuffer.take(addressSize)}, $value)")
        }

        override fun appendList(addressBuffer: Array<Any>, addressSize: Int, value: Boolean) {
            println("api.appendList(${addressBuffer.take(addressSize)}, $value)")
        }

        override fun listEnd(addressBuffer: Array<Any>, addressSize: Int) {
            println("api.listEnd(${addressBuffer.take(addressSize)})")
        }

        override fun arrayOfTables(addressBuffer: Array<Any>, addressSize: Int) {
            println("api.arrayOfTables(${addressBuffer.take(addressSize)})")
        }

    }

    @Test
    fun `Collection test`() {
        val file = File("C:\\Users\\Greg\\AppData\\Roaming\\JetBrains\\IntelliJIdea2024.3\\scratches\\scratch.toml")
        val streaming = TomlStream()
        val api = TomlMapApi()
        val buffer = ByteArray(1024)
        val address = Array<Any>(10) { "" }
        streaming.read(file.inputStream().buffered(), api, buffer, address)
        println(api.root)
    }

    @Test
    fun `Read array test`() {
        val text = """
            id1 = 1E2
            id2 = 1.2e+3
            id3 = 1.2E-3
        """.trimIndent()
        val streaming = TomlStream()
        val api = TomlMapApi()
//        val api = printApi()
        val buffer = ByteArray(1024)
        val address = Array<Any>(10) { "" }
        streaming.read(text.byteInputStream().buffered(), api, buffer, address)
        println(api.root)
    }

    @Test
    fun `Benchmark test`() {
        val file = File("C:\\Users\\Greg\\AppData\\Roaming\\JetBrains\\IntelliJIdea2024.3\\scratches\\scratch.toml")
        val streaming = TomlStream()
//        val api = object : TomlStream.API {
//        }
        val api = TomlMapApi()
        val count = 10
        val buffer = ByteArray(1024)
        val address = Array<Any>(10) { "" }
        var total = 0L
        for (i in 0 until count) {
            val stream = file.inputStream().buffered()
            val start = System.nanoTime()
            streaming.read(stream, api, buffer, address)
            val end = System.nanoTime()
            total += end - start
            api.root.clear()
        }
        println("Took ${total / count}ns")
    }
}