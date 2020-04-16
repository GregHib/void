package rs.dusk.engine.map.location

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import org.koin.dsl.module
import rs.dusk.engine.model.Region
import rs.dusk.utility.func.plural
import java.io.File
import java.io.RandomAccessFile
import kotlin.system.measureTimeMillis

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 16, 2020
 */
typealias Xteas = Map<Region, Xtea>

val xteaModule = module {
    single(createdAtStart = true) { loadXteas(getProperty("xteaPath")) }
}

fun loadXteas(directory: String): Xteas {
    val file = File(directory)
    val xteas = mutableMapOf<Region, Xtea>()
    val time = measureTimeMillis {
        when {
            file.isDirectory || file.extension == "txt" -> fromFiles(xteas, file)
            file.extension == "json" -> fromJson(xteas, file)
            else -> fromData(xteas, file)
        }
    }
    println("Loaded ${xteas.size} ${"xtea".plural(xteas.size)} in ${time}ms.")
    return xteas
}

private fun fromFiles(xteas: MutableMap<Region, Xtea>, file: File) {
    file.listFiles { f -> f.extension == "txt" }?.forEach { f ->
        val region = Region(f.nameWithoutExtension.toInt())
        val lines = f.readLines()
        xteas[region] = Xtea { lines[it].toInt() }
    }
}

@Suppress("UNCHECKED_CAST")
private fun fromJson(xteas: MutableMap<Region, Xtea>, file: File) {
    val mapper = ObjectMapper(JsonFactory())
    val map = mapper.readValue(file, Array<Any>::class.java)
    map.forEach {
        val entry = it as? LinkedHashMap<String, Any> ?: return@forEach
        val id = entry["mapsquare"]?.toString()?.toInt() ?: return@forEach
        val keys = entry["key"] as? ArrayList<Int> ?: return@forEach
        xteas[Region(id)] = Xtea(keys.toTypedArray().toIntArray())
    }
}

private fun fromData(xteas: MutableMap<Region, Xtea>, file: File) {
    val raf = RandomAccessFile(file, "r")
    while (raf.filePointer < raf.length()) {
        val region = Region(raf.readShort().toInt())
        xteas[region] = Xtea { raf.readInt() }
    }
    raf.close()
}