package world.gregs.voidps.engine.data

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import world.gregs.voidps.buffer.read.ArrayReader
import world.gregs.voidps.buffer.write.ArrayWriter
import world.gregs.voidps.engine.timedLoad
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.isDirectory
import kotlin.io.path.name
import kotlin.io.path.pathString
import kotlin.io.path.readBytes
import kotlin.io.path.writeBytes

data class ConfigFiles(
    val map: Map<String, List<String>>,
    val cacheUpdate: Boolean = false,
    val extensions: Set<String> = emptySet(),
) {
    fun list(path: String): List<String> = map.getOrDefault(path, emptyList())

    fun getValue(key: String) = map.getValue(key)

    fun find(path: String, type: String = "toml"): String = map.getOrDefault(type, emptyList()).firstOrNull { it.endsWith(path) } ?: throw NoSuchFileException("Unable to find config file '$path' in /data/ directory.")
}

fun configFiles(): ConfigFiles {
    val map = Object2ObjectOpenHashMap<String, MutableList<String>>()
    val path = Path.of(Settings["storage.data"])
    val modified = Path.of(Settings["storage.data.modified"])
    val lastUpdated = loadLastUpdate(modified)
    val extensions = mutableSetOf<String>()
    timedLoad("config file paths") {
        walkPath(map, path, lastUpdated, extensions)
        map.size
    }
    return ConfigFiles(map, cacheChanged(lastUpdated), extensions)
}

private fun updateModified() {
    val modified = Path.of(Settings["storage.data.modified"])
    val writer = ArrayWriter(8)
    writer.writeLong(System.currentTimeMillis())
    modified.writeBytes(writer.toArray())
}

private fun loadLastUpdate(path: Path): Long {
    if (path.exists()) {
        val reader = ArrayReader(path.readBytes())
        return reader.readLong()
    }
    return 0
}

private fun walkPath(
    map: MutableMap<String, MutableList<String>>,
    dir: Path,
    lastUpdated: Long,
    invalidatedExtensions: MutableSet<String>,
) {
    val name = dir.name
    if (name == "saves" || name == "players" || name == "cache") {
        return
    }
    for (path in Files.newDirectoryStream(dir)) {
        if (path.isDirectory()) {
            walkPath(map, path, lastUpdated, invalidatedExtensions)
            continue
        }
        val extension = path.name.substringAfter('.')
        map.getOrPut(extension) { ObjectArrayList() }.add(path.pathString)

        // Check file type hasn't been marked as invalidated before checking the last modified time for invalidation
        if (!invalidatedExtensions.contains(extension) && Files.getLastModifiedTime(path).toMillis() > lastUpdated) {
            invalidatedExtensions.add(extension)
        }
    }
}

private fun cacheChanged(
    lastUpdated: Long,
    dir: Path = Path.of(Settings["storage.cache.path"]),
): Boolean {
    for (path in Files.newDirectoryStream(dir)) {
        if (path.isDirectory() || (!path.extension.startsWith("dat") && !path.extension.startsWith("idx"))) {
            continue
        }
        val lastModified = Files.getLastModifiedTime(path).toMillis()
        if (lastModified > lastUpdated) {
            return true
        }
    }
    return false
}