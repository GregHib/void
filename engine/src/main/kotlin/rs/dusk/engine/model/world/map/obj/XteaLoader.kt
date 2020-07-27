package rs.dusk.engine.model.world.map.obj

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import rs.dusk.engine.TimedLoader
import java.io.File
import java.io.RandomAccessFile

private typealias Xtea = IntArray

class XteaLoader : TimedLoader<Xteas>("xtea") {

    override fun load(args: Array<out Any?>): Xteas {
        val path = args[0] as String
        val file = File(path)
        val xteas = when {
            file.extension == "txt" -> loadDirectory(file.parentFile)
            file.isDirectory -> loadDirectory(file)
            file.extension == "json" -> loadJson(
                file.readText(),
                args.getOrNull(1) as? String ?: DEFAULT_KEY,
                args.getOrNull(2) as? String ?: DEFAULT_VALUE
            )
            else -> loadBinary(RandomAccessFile(file, "r"))
        }
        count = xteas.size
        return Xteas(xteas)
    }

    fun loadText(text: String): IntArray {
        val lines = text.lines()
        return IntArray(4) { i -> lines.getOrNull(i)?.toInt() ?: 0 }
    }

    @Suppress("UNCHECKED_CAST")
    fun loadJson(text: String, key: String, value: String): Map<Int, Xtea> {
        val mapper = ObjectMapper(JsonFactory())
        val map: Array<Map<String, Any>> = mapper.readValue(text)
        return map.map {
            val id = it[key] as Int
            val keys = it[value] as ArrayList<Int>
            id to keys.toIntArray()
        }.toMap()
    }

    private fun loadDirectory(file: File): Map<Int, Xtea> {
        return loadTextFiles(file.listFiles { f -> f.extension == "txt" } ?: emptyArray())
    }

    private fun loadTextFiles(files: Array<File>): Map<Int, Xtea> {
        return files.map { loadTextFile(it) }.toMap()
    }

    private fun loadTextFile(file: File): Pair<Int, Xtea> {
        return file.nameWithoutExtension.toInt() to loadText(file.readText())
    }

    private fun loadBinary(file: RandomAccessFile): Map<Int, Xtea> {
        val xteas = mutableMapOf<Int, IntArray>()
        file.use { raf ->
            while (raf.filePointer < raf.length()) {
                val region = raf.readShort().toInt()
                xteas[region] = IntArray(4) { raf.readInt() }
            }
        }
        return xteas
    }

    companion object {
        private const val DEFAULT_KEY = "mapsquare"
        private const val DEFAULT_VALUE = "keys"
    }
}