package world.gregs.voidps.engine.map.region

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import world.gregs.voidps.buffer.read.BufferReader
import world.gregs.voidps.engine.getProperty
import world.gregs.voidps.engine.getPropertyOrNull
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.type.Region
import world.gregs.yaml.Yaml
import java.io.File

data class Xteas(
    val delegate: MutableMap<Int, IntArray> = Int2ObjectOpenHashMap()
) : Map<Int, IntArray> by delegate {

    operator fun get(region: Region): IntArray? {
        return this[region.id]
    }

    fun load(
        path: String = getProperty("xteaPath"),
        key: String = getPropertyOrNull("xteaJsonKey") ?: DEFAULT_KEY,
        value: String = getPropertyOrNull("xteaJsonValue") ?: DEFAULT_VALUE
    ): Xteas {
        timedLoad("xtea") {
            val file = File(path)
            val all = when {
                file.extension == "txt" -> loadDirectory(file.parentFile)
                file.isDirectory -> loadDirectory(file)
                file.extension == "json" -> loadJson(file.readText(), key, value)
                else -> loadBinary(file)
            }
            delegate.clear()
            delegate.putAll(all)
            delegate.size
        }
        return this
    }

    companion object {
        fun loadDirectory(file: File): Map<Int, IntArray> = (file
            .listFiles { f -> f.extension == "txt" } ?: emptyArray<File>()).associate {
            val lines = it.readLines()
            val keys = IntArray(4) { i -> lines.getOrNull(i)?.toInt() ?: 0 }
            it.nameWithoutExtension.toInt() to keys
        }

        @Suppress("UNCHECKED_CAST")
        fun loadJson(text: String, key: String = DEFAULT_KEY, value: String = DEFAULT_VALUE): Map<Int, IntArray> {
            val mapper = Yaml()
            val map = mapper.read(text) as List<Map<String, Any>>
            return map.associate {
                val id = it[key] as Int
                val keys = it[value] as? List<Int> ?: emptyList()
                id to keys.toIntArray()
            }
        }

        fun loadBinary(file: File): Map<Int, IntArray> {
            val xteas = Int2ObjectOpenHashMap<IntArray>()
            val reader = BufferReader(file.readBytes())
            while (reader.remaining > 0) {
                val region = reader.readShort()
                xteas[region] = IntArray(4) { reader.readInt() }
            }
            return xteas
        }

        const val DEFAULT_KEY = "mapsquare"
        const val DEFAULT_VALUE = "keys"
    }
}