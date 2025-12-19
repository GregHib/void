package world.gregs.voidps.tools.cache

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import world.gregs.voidps.buffer.read.ArrayReader
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.type.Region
import world.gregs.yaml.Yaml
import java.io.File

data class Xteas(
    val delegate: MutableMap<Int, IntArray> = Int2ObjectOpenHashMap(),
) : Map<Int, IntArray> by delegate {

    operator fun get(region: Region): IntArray? = this[region.id]

    fun load(
        path: String = Settings["storage.xteas"],
        key: String = Settings["xteaJsonKey", DEFAULT_KEY],
        value: String = Settings["xteaJsonValue", DEFAULT_VALUE],
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
        fun loadDirectory(file: File): Map<Int, IntArray> = (
            file
                .listFiles { f -> f.extension == "txt" } ?: emptyArray<File>()
            ).associate {
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
            val reader = ArrayReader(file.readBytes())
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
