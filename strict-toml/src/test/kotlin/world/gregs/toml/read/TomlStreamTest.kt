package world.gregs.toml.read

import org.junit.jupiter.api.Test
import java.io.File

class TomlStreamTest {

    @Test
    fun `Streaming test`() {
        val file = File("C:\\Users\\Greg\\AppData\\Roaming\\JetBrains\\IntelliJIdea2024.3\\scratches\\scratch.toml")
        val streaming = TomlStream()
        val api = object : TomlStream.Api {
            override fun table(addressBuffer: Array<String>, addressSize: Int) {
                println("api.table(${addressBuffer.take(addressSize)})")
            }

            override fun inlineTable(addressBuffer: Array<String>, addressSize: Int) {
                println("api.inlineTable(${addressBuffer.take(addressSize)})")
            }

            override fun appendMap(addressBuffer: Array<String>, addressSize: Int, key: String, value: Double) {
                println("api.appendMap(${addressBuffer.take(addressSize)}, $key, $value)")
            }

            override fun appendMap(addressBuffer: Array<String>, addressSize: Int, key: String, value: Long) {
                println("api.appendMap(${addressBuffer.take(addressSize)}, $key, $value)")
            }

            override fun appendMap(addressBuffer: Array<String>, addressSize: Int, key: String, value: String) {
                println("api.appendMap(${addressBuffer.take(addressSize)}, $key, $value)")
            }

            override fun appendMap(addressBuffer: Array<String>, addressSize: Int, key: String, value: Boolean) {
                println("api.appendMap(${addressBuffer.take(addressSize)}, $key, $value)")
            }

            override fun mapEnd(addressBuffer: Array<String>, addressSize: Int) {
                println("api.mapEnd(${addressBuffer.take(addressSize)})")
            }

            override fun list(addressBuffer: Array<String>, addressSize: Int) {
                println("api.list(${addressBuffer.take(addressSize)})")
            }

            override fun appendList(addressBuffer: Array<String>, addressSize: Int, value: Double) {
                println("api.appendList(${addressBuffer.take(addressSize)}, $value)")
            }

            override fun appendList(addressBuffer: Array<String>, addressSize: Int, value: Long) {
                println("api.appendList(${addressBuffer.take(addressSize)}, $value)")
            }

            override fun appendList(addressBuffer: Array<String>, addressSize: Int, value: String) {
                println("api.appendList(${addressBuffer.take(addressSize)}, $value)")
            }

            override fun appendList(addressBuffer: Array<String>, addressSize: Int, value: Boolean) {
                println("api.appendList(${addressBuffer.take(addressSize)}, $value)")
            }

            override fun listEnd(addressBuffer: Array<String>, addressSize: Int) {
                println("api.listEnd(${addressBuffer.take(addressSize)})")
            }

        }
        val buffer = ByteArray(1024)
        val address = Array(10) { "" }
        streaming.read(file.inputStream().buffered(), api, buffer, address)
    }

    @Test
    fun `Collection test`() {
        val file = File("C:\\Users\\Greg\\AppData\\Roaming\\JetBrains\\IntelliJIdea2024.3\\scratches\\scratch.toml")
        val streaming = TomlStream()
        val api = TomlMapApi()
        val buffer = ByteArray(1024)
        val address = Array(10) { "" }
        streaming.read(file.inputStream().buffered(), api, buffer, address)
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
        val address = Array(10) { "" }
        var total = 0L
        for (i in 0 until count) {
            val stream = file.inputStream().buffered()
            val start = System.nanoTime()
            streaming.read(stream, api, buffer, address)
            val end = System.nanoTime()
            total += end - start
            api.root.clear()
        }
        println("Took ${total/ count}ns")
    }
}