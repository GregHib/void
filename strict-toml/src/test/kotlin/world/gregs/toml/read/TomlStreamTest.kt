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
            override fun table(address: Array<String>) {
                println("api.table(${address.dropLastWhile { it == "" }})")
            }

            override fun inlineTable(address: Array<String>) {
                println("api.inlineTable(${address.dropLastWhile { it == "" }})")
            }

            override fun appendMap(address: Array<String>, key: String, value: Double) {
                println("api.appendMap(${address.dropLastWhile { it == "" }}, $key, $value)")
            }

            override fun appendMap(address: Array<String>, key: String, value: Long) {
                println("api.appendMap(${address.dropLastWhile { it == "" }}, $key, $value)")
            }

            override fun appendMap(address: Array<String>, key: String, value: String) {
                println("api.appendMap(${address.dropLastWhile { it == "" }}, $key, $value)")
            }

            override fun appendMap(address: Array<String>, key: String, value: Boolean) {
                println("api.appendMap(${address.dropLastWhile { it == "" }}, $key, $value)")
            }

            override fun appendMap(address: Array<String>, key: String, value: List<Any>) {
                println("api.appendMap(${address.dropLastWhile { it == "" }}, $key, $value)")
            }

            override fun appendMap(address: Array<String>, key: String, value: Map<String, Any>) {
                println("api.appendMap(${address.dropLastWhile { it == "" }}, $key, $value)")
            }

            override fun mapEnd(address: Array<String>, key: String) {
                println("api.mapEnd(${address.dropLastWhile { it == "" }}, $key)")
            }

            override fun list(address: Array<String>) {
                println("api.list(${address.dropLastWhile { it == "" }})")
            }

            override fun appendList(address: Array<String>, value: Double) {
                println("api.appendList(${address.dropLastWhile { it == "" }}, $value)")
            }

            override fun appendList(address: Array<String>, value: Long) {
                println("api.appendList(${address.dropLastWhile { it == "" }}, $value)")
            }

            override fun appendList(address: Array<String>, value: String) {
                println("api.appendList(${address.dropLastWhile { it == "" }}, $value)")
            }

            override fun appendList(address: Array<String>, value: Boolean) {
                println("api.appendList(${address.dropLastWhile { it == "" }}, $value)")
            }

            override fun appendList(address: Array<String>, value: List<Any>) {
                println("api.appendList(${address.dropLastWhile { it == "" }}, $value)")
            }

            override fun appendList(address: Array<String>, value: Map<String, Any>) {
                println("api.appendList(${address.dropLastWhile { it == "" }}, $value)")
            }

            override fun listEnd(address: Array<String>) {
                println("api.listEnd(${address.dropLastWhile { it == "" }})")
            }

        }
        streaming.read(file.inputStream().buffered(), api)
    }

    @Test
    fun `Benchmark test`() {
        val file = File("C:\\Users\\Greg\\AppData\\Roaming\\JetBrains\\IntelliJIdea2024.3\\scratches\\scratch.toml")
        val streaming = TomlStream()
        val api = object : TomlStream.API {
            override fun table(address: Array<String>) {
            }

            override fun inlineTable(address: Array<String>) {
            }

            override fun appendMap(address: Array<String>, key: String, value: Double) {
            }

            override fun appendMap(address: Array<String>, key: String, value: Long) {
            }

            override fun appendMap(address: Array<String>, key: String, value: String) {
            }

            override fun appendMap(address: Array<String>, key: String, value: Boolean) {
            }

            override fun appendMap(address: Array<String>, key: String, value: List<Any>) {
            }

            override fun appendMap(address: Array<String>, key: String, value: Map<String, Any>) {
            }

            override fun mapEnd(address: Array<String>, key: String) {
            }

            override fun list(address: Array<String>) {
            }

            override fun appendList(address: Array<String>, value: Double) {
            }

            override fun appendList(address: Array<String>, value: Long) {
            }

            override fun appendList(address: Array<String>, value: String) {
            }

            override fun appendList(address: Array<String>, value: Boolean) {
            }

            override fun appendList(address: Array<String>, value: List<Any>) {
            }

            override fun appendList(address: Array<String>, value: Map<String, Any>) {
            }

            override fun listEnd(address: Array<String>) {
            }

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