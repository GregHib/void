package world.gregs.toml.read

import org.junit.jupiter.api.Test
import world.gregs.toml.Toml
import java.io.File

class TomlStreamTest {

    @Test
    fun `Streaming test`() {
        val file = File("C:\\Users\\Greg\\AppData\\Roaming\\JetBrains\\IntelliJIdea2024.3\\scratches\\scratch.toml")
        val streaming = TomlStream()
        val api = object : TomlStream.API {
            override fun table(address: Array<String>, addressSize: Int) {
                println("api.table(${address.take(addressSize)})")
            }

            override fun inlineTable(address: Array<String>, addressSize: Int) {
                println("api.inlineTable(${address.take(addressSize)})")
            }

            override fun appendMap(address: Array<String>, addressSize: Int, key: String, value: Double) {
                println("api.appendMap(${address.take(addressSize)}, $key, $value)")
            }

            override fun appendMap(address: Array<String>, addressSize: Int, key: String, value: Long) {
                println("api.appendMap(${address.take(addressSize)}, $key, $value)")
            }

            override fun appendMap(address: Array<String>, addressSize: Int, key: String, value: String) {
                println("api.appendMap(${address.take(addressSize)}, $key, $value)")
            }

            override fun appendMap(address: Array<String>, addressSize: Int, key: String, value: Boolean) {
                println("api.appendMap(${address.take(addressSize)}, $key, $value)")
            }

            override fun appendMap(address: Array<String>, addressSize: Int, key: String, value: List<Any>) {
                println("api.appendMap(${address.take(addressSize)}, $key, $value)")
            }

            override fun appendMap(address: Array<String>, addressSize: Int, key: String, value: Map<String, Any>) {
                println("api.appendMap(${address.take(addressSize)}, $key, $value)")
            }

            override fun mapEnd(address: Array<String>, addressSize: Int) {
                println("api.mapEnd(${address.take(addressSize)})")
            }

            override fun list(address: Array<String>, addressSize: Int) {
                println("api.list(${address.take(addressSize)})")
            }

            override fun appendList(address: Array<String>, addressSize: Int, value: Double) {
                println("api.appendList(${address.take(addressSize)}, $value)")
            }

            override fun appendList(address: Array<String>, addressSize: Int, value: Long) {
                println("api.appendList(${address.take(addressSize)}, $value)")
            }

            override fun appendList(address: Array<String>, addressSize: Int, value: String) {
                println("api.appendList(${address.take(addressSize)}, $value)")
            }

            override fun appendList(address: Array<String>, addressSize: Int, value: Boolean) {
                println("api.appendList(${address.take(addressSize)}, $value)")
            }

            override fun appendList(address: Array<String>, addressSize: Int, value: List<Any>) {
                println("api.appendList(${address.take(addressSize)}, $value)")
            }

            override fun appendList(address: Array<String>, addressSize: Int, value: Map<String, Any>) {
                println("api.appendList(${address.take(addressSize)}, $value)")
            }

            override fun listEnd(address: Array<String>, addressSize: Int) {
                println("api.listEnd(${address.take(addressSize)})")
            }

        }
        streaming.read(file.inputStream().buffered(), api)
    }

    @Test
    fun `Benchmark test`() {
        val file = File("C:\\Users\\Greg\\AppData\\Roaming\\JetBrains\\IntelliJIdea2024.3\\scratches\\scratch.toml")
        val streaming = TomlStream()
        val api = object : TomlStream.API {
        }
        var count = 10
        var start = System.nanoTime()
        for(i in 0 until count) {
            streaming.read(file.inputStream().buffered(), api)
        }
        var end = System.nanoTime()
        println("Took ${(end-start)/count}ns")
        start = System.nanoTime()
        for(i in 0 until count) {
            Toml.decodeFromCharArray(file.readText().toCharArray())
        }
        end = System.nanoTime()
        println("Took ${(end-start)/count}ns")
    }
}