package world.gregs.voidps.engine.data

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import world.gregs.voidps.buffer.read.ArrayReader
import world.gregs.voidps.buffer.write.ArrayWriter
import world.gregs.voidps.engine.timedLoad
import java.io.File
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
    val needsUpdate: MutableSet<String> = mutableSetOf(),
    val lastUpdated: MutableMap<String, Long> = mutableMapOf(),
) {
    fun list(path: String): List<String> = map.getOrDefault(path, emptyList())

    fun getValue(key: String) = map.getValue(key)

    fun find(path: String, type: String = "toml"): String = map.getOrDefault(type, emptyList()).firstOrNull { it.endsWith(path) } ?: throw NoSuchFileException("Unable to find config file '$path' in /data/ directory.")

    fun update(extension: String) {
        lastUpdated[extension] = System.currentTimeMillis()
        save()
    }

    fun needsUpdate(extension: String) = needsUpdate.contains(extension)

    fun save() {
        val modified = Path.of(Settings["storage.data.modified"])
        val writer = ArrayWriter(10_000)
        writer.writeLong(System.currentTimeMillis())
        writer.writeByte(lastUpdated.size)
        for ((key, value) in lastUpdated.toList()) {
            writer.writeString(key)
            writer.writeLong(value)
        }
        modified.writeBytes(writer.toArray())
    }
}

fun configFiles(): ConfigFiles {
    val directory = File(Settings["storage.caching.path"])
    if (!directory.exists()) {
        directory.mkdirs()
    }
    val map = Object2ObjectOpenHashMap<String, MutableList<String>>()
    val path = Path.of(Settings["storage.data"])
    val modified = Path.of(Settings["storage.data.modified"])
    val lastUpdated = loadLastUpdate(modified)
    val needsUpdate = mutableSetOf<String>()
    timedLoad("config file paths") {
        walkPath(map, path, lastUpdated, needsUpdate)
        map.size
    }
    val cacheChanged = cacheChanged(Path.of(Settings["storage.cache.path"]), lastUpdated.getOrDefault("cache", 0L))
    return ConfigFiles(map, cacheChanged, needsUpdate, lastUpdated)
}

private fun loadLastUpdate(path: Path): MutableMap<String, Long> {
    if (path.exists()) {
        val reader = ArrayReader(path.readBytes())
        val map = mutableMapOf<String, Long>()
        map["cache"] = reader.readLong()
        val size = reader.readByte()
        for (i in 0 until size) {
            map[reader.readString()] = reader.readLong()
        }
        return map
    }
    return mutableMapOf()
}

private fun walkPath(
    map: MutableMap<String, MutableList<String>>,
    dir: Path,
    lastUpdated: MutableMap<String, Long>,
    needUpdate: MutableSet<String> = mutableSetOf(),
) {
    val name = dir.name
    if (name == "saves" || name == "players" || name == "cache") {
        return
    }
    for (path in Files.newDirectoryStream(dir)) {
        if (path.isDirectory()) {
            walkPath(map, path, lastUpdated, needUpdate)
            continue
        }
        val extension = path.name.substringAfter('.')
        map.getOrPut(extension) { ObjectArrayList() }.add(path.pathString)

        // Check file-type hasn't been marked as invalidated before checking the last modified time for invalidation
        if (!needUpdate.contains(extension) && Files.getLastModifiedTime(path).toMillis() > lastUpdated.getOrDefault(extension, 0L)) {
            needUpdate.add(extension)
        }
    }
}

private fun cacheChanged(dir: Path, lastUpdated: Long): Boolean {
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