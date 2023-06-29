package world.gregs.voidps.engine.map.region

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import world.gregs.voidps.buffer.read.BufferReader
import world.gregs.voidps.engine.timedLoad
import world.gregs.yaml.Yaml
import java.io.File

private typealias Xtea = IntArray

class XteaLoader {

    fun load(xteas: Xteas, path: String, key: String? = null, value: String? = null) {
        timedLoad("xtea") {
            val file = File(path)
            val all = when {
                file.extension == "txt" -> loadDirectory(file.parentFile)
                file.isDirectory -> loadDirectory(file)
                file.extension == "json" -> loadJson(
                    file.readText(),
                    key ?: DEFAULT_KEY,
                    value ?: DEFAULT_VALUE
                )
                else -> loadBinary(file)
            }
            xteas.delegate.clear()
            xteas.delegate.putAll(all)
            all.size
        }
    }

    fun loadText(text: String): IntArray {
        val lines = text.lines()
        return IntArray(4) { i -> lines.getOrNull(i)?.toInt() ?: 0 }
    }

    @Suppress("UNCHECKED_CAST")
    fun loadJson(text: String, key: String = DEFAULT_KEY, value: String = DEFAULT_VALUE): Map<Int, Xtea> {
        val mapper = Yaml()
        val map = mapper.read(text) as List<Map<String, Any>>
        return map.associate {
            val id = it[key] as Int
            val keys = it[value] as? List<Int> ?: emptyList()
            id to keys.toIntArray()
        }
    }

    private fun loadDirectory(file: File): Map<Int, Xtea> {
        return loadTextFiles(file.listFiles { f -> f.extension == "txt" } ?: emptyArray())
    }

    private fun loadTextFiles(files: Array<File>): Map<Int, Xtea> {
        return files.associate { loadTextFile(it) }
    }

    private fun loadTextFile(file: File): Pair<Int, Xtea> {
        return file.nameWithoutExtension.toInt() to loadText(file.readText())
    }

    private fun loadBinary(file: File): Map<Int, Xtea> {
        val xteas = Int2ObjectOpenHashMap<IntArray>()
        val reader = BufferReader(file.readBytes())
        while (reader.position() < reader.length) {
            val region = reader.readShort()
            xteas[region] = IntArray(4) { reader.readInt() }
        }
        return xteas
    }

    companion object {
        private const val DEFAULT_KEY = "mapsquare"
        private const val DEFAULT_VALUE = "keys"
    }
}