package rs.dusk.engine.model.world.map.location

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.michaelbull.logging.InlineLogger
import org.koin.dsl.module
import rs.dusk.engine.model.world.Region
import rs.dusk.utility.func.plural
import java.io.File
import java.io.RandomAccessFile
import kotlin.system.measureTimeMillis

typealias Xtea = IntArray

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 16, 2020
 */
data class Xteas(val delegate: Map<Int, Xtea>) : Map<Int, Xtea> by delegate {

    operator fun get(region: Region): Xtea? {
        return this[region.id]
    }

}

val xteaModule = module {
    single(createdAtStart = true) {
        loadXteas(
            getProperty("xteaPath"),
            getProperty("xteaJsonKey", "mapsquare"),
            getProperty("xteaJsonValue", "key")
        )
    }
}

private val logger = InlineLogger()

fun loadXteas(directory: String, idKey: String, valueKey: String): Xteas {
    val file = File(directory)
    val xteas = mutableMapOf<Int, IntArray>()
    val time = measureTimeMillis {
        when {
            file.isDirectory || file.extension == "txt" -> fromFiles(xteas, file)
            file.extension == "json" -> fromJson(xteas, file, idKey, valueKey)
            else -> fromData(xteas, file)
        }
    }
    logger.info { "Loaded ${xteas.size} ${"xtea".plural(xteas.size)} in ${time}ms." }
    return Xteas(xteas)
}

private fun fromFiles(xteas: MutableMap<Int, IntArray>, file: File) {
    file.listFiles { f -> f.extension == "txt" }?.forEach { f ->
        val region = f.nameWithoutExtension.toInt()
        val lines = f.readLines()
        xteas[region] = IntArray(4) { lines[it].toInt() }
    }
}

@Suppress("UNCHECKED_CAST")
private fun fromJson(xteas: MutableMap<Int, IntArray>, file: File, idKey: String, valueKey: String) {
    val mapper = ObjectMapper(JsonFactory())
    val map = mapper.readValue(file, Array<Any>::class.java)
    map.forEach {
        val entry = it as? LinkedHashMap<String, Any> ?: return@forEach
        val id = entry[idKey]?.toString()?.toInt() ?: return@forEach
        val keys = entry[valueKey] as? ArrayList<Int> ?: return@forEach
        xteas[id] = keys.toTypedArray().toIntArray()
    }
}

private fun fromData(xteas: MutableMap<Int, IntArray>, file: File) {
    val raf = RandomAccessFile(file, "r")
    while (raf.filePointer < raf.length()) {
        val region = raf.readShort().toInt()
        xteas[region] = IntArray(4) { raf.readInt() }
    }
    raf.close()
}