package world.gregs.voidps.engine.event

import java.io.BufferedReader

object Wildcards {
    private val map = mutableMapOf<String, List<String>>()

    val size: Int
        get() = map.size

    fun load(reader: BufferedReader) {
        map["*"] = listOf("*")
        while (reader.ready()) {
            val line = reader.readLine() ?: continue
            val (key, values) = line.split("|")
            map[key] = values.split(":")
        }
    }

    fun find(key: String): List<String> = map[key] ?: emptyList()

    fun clear() {
        map.clear()
    }
}