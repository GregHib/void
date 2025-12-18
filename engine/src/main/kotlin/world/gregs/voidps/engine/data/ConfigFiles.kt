package world.gregs.voidps.engine.data

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import world.gregs.voidps.engine.timedLoad
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import kotlin.io.path.getLastModifiedTime
import kotlin.io.path.isDirectory
import kotlin.io.path.name
import kotlin.io.path.pathString

typealias ConfigFiles = Map<String, List<String>>

fun ConfigFiles.list(path: String): List<String> = getOrDefault(path, emptyList())

fun ConfigFiles.find(path: String, type: String = "toml"): String = getOrDefault(type, emptyList()).firstOrNull { it.endsWith(path) } ?: throw NoSuchFileException("Unable to find config file '$path' in /data/ directory.")

class CacheInvalidation {
    val invalidated = mutableSetOf<String>()
    // TODO test for invalid and if invalid then recursively get the endings of all files within
}

fun configFiles(lastUpdated: Long = 0): ConfigFiles {
    val map = Object2ObjectOpenHashMap<String, MutableList<String>>()
    timedLoad("config file paths") {
        walkPath(map, Path.of(Settings["storage.data"]), lastUpdated)
        map.size
    }
    return map
}

private fun walkPath(map: MutableMap<String, MutableList<String>>, dir: Path, lastUpdated: Long): Boolean {
    // Exclusions
    val name = dir.name
    if (name == "saves" || name == "players" || name == "cache") {
        return false
    }
    var invalid = false
    for (path in Files.newDirectoryStream(dir)) {
        if (path.isDirectory()) {
            invalid = invalid or walkPath(map, path, lastUpdated)
            if (!invalid && path.getLastModifiedTime().toMillis() > lastUpdated) {
                invalid = true
            }
        } else {
            val extension = path.name.substringAfter('.')
            map.getOrPut(extension) { ObjectArrayList() }.add(path.pathString)
        }
    }
    return invalid
}
