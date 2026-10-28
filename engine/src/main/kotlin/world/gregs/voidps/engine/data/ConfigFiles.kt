package world.gregs.voidps.engine.data

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import world.gregs.voidps.buffer.read.ArrayReader
import world.gregs.voidps.buffer.write.ArrayWriter
import world.gregs.voidps.engine.client.ui.chat.toInt
import world.gregs.voidps.engine.timedLoad
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import kotlin.io.path.*

data class ConfigFiles(
    val map: Map<String, List<String>>,
    val cacheUpdate: Boolean = false,
    val extensions: Set<String> = emptySet(),
) {
    fun list(path: String): List<String> = map.getOrDefault(path, emptyList())

    fun getValue(key: String) = map.getValue(key)

    fun find(path: String, type: String = "toml"): String = map.getOrDefault(type, emptyList()).firstOrNull { it.endsWith(path) } ?: throw NoSuchFileException("Unable to find config file '$path' in /data/ directory.")

    fun update() {
        val modified = Path.of(Settings["storage.data.modified"])
        val writer = ArrayWriter(9)
        writer.writeLong(System.currentTimeMillis())
        writer.writeByte(Settings["development.loadAllObjects", false].toInt())
        modified.writeBytes(writer.toArray())
    }
}

fun configFiles(path: String = Settings["storage.data"], temp: String = Settings["storage.data.modified"], cachePath: String = Settings["storage.cache.path"]): ConfigFiles {
    val map = Object2ObjectOpenHashMap<String, MutableList<String>>()
    val data = Path.of(path)
    val modified = Path.of(temp)
    modified.createParentDirectories()
    val (lastUpdated, storeUnusedObjs) = loadLastUpdate(modified)
    val settingsChanged = storeUnusedObjs != Settings["development.loadAllObjects", false].toInt()
    val extensions = mutableSetOf<String>()
    timedLoad("config file path") {
        val dirs = data.resolve("dirs.txt").toFile().readLines()
        for (dir in dirs) {
            walkPath(map, data.resolve(dir), lastUpdated, extensions)
        }
        map.size
    }
    return ConfigFiles(map, settingsChanged || cacheChanged(lastUpdated, Path.of(cachePath)), extensions)
}

/**
 * Reads the timestamp and the storeUnused flag written after it.
 * Files from before the flag existed have no flag, so -1 is returned which never matches and rebuilds the cache once.
 */
private fun loadLastUpdate(path: Path): Pair<Long, Int> {
    if (!path.exists()) {
        return Pair(0L, -1)
    }
    val reader = ArrayReader(path.readBytes())
    val lastUpdated = reader.readLong()
    return Pair(lastUpdated, if (reader.remaining > 0) reader.readByte() else -1)
}

private fun walkPath(
    map: MutableMap<String, MutableList<String>>,
    dir: Path,
    lastUpdated: Long,
    invalidatedExtensions: MutableSet<String>,
) {
    Files.newDirectoryStream(dir).use { stream ->
        for (path in stream) {
            val name = path.name
            if (!name.endsWith(".toml")) {
                if (Files.isDirectory(path)) {
                    walkPath(map, path, lastUpdated, invalidatedExtensions)
                }
                continue
            }
            val extension = name.substringAfter('.')
            map.getOrPut(extension) { ObjectArrayList() }.add(path.pathString)

            // Check file-type hasn't been marked as invalidated before checking the last modified time for invalidation
            if (!invalidatedExtensions.contains(extension) && Files.getLastModifiedTime(path).toMillis() > lastUpdated) {
                invalidatedExtensions.add(extension)
            }
        }
    }
}

private fun cacheChanged(
    lastUpdated: Long,
    dir: Path = Path.of(Settings["storage.cache.path"]),
): Boolean {
    Files.newDirectoryStream(dir).use { stream ->
        for (path in stream) {
            if (!path.extension.startsWith("dat") && !path.extension.startsWith("idx")) {
                continue
            }
            val lastModified = Files.getLastModifiedTime(path).toMillis()
            if (lastModified > lastUpdated) {
                return true
            }
        }
    }
    return false
}
