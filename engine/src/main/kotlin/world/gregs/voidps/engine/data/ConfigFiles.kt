package world.gregs.voidps.engine.data

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import world.gregs.voidps.engine.timedLoad
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.*

data class ConfigFiles(
    val map: Map<String, List<String>>,
    /** Size and modified time of every config file, by path */
    val stamps: Map<String, Long> = emptyMap(),
    val cacheModified: Long = 0L,
) {
    fun list(path: String): List<String> = map.getOrDefault(path, emptyList())

    fun getValue(key: String) = map.getValue(key)

    fun find(path: String, type: String = "toml"): String = map.getOrDefault(type, emptyList()).firstOrNull { it.endsWith(path) } ?: throw NoSuchFileException("Unable to find config file '$path' in /data/ directory.")
}

fun configFiles(path: String = Settings["storage.data"], cachePath: String = Settings["storage.cache.path"]): ConfigFiles {
    val map = Object2ObjectOpenHashMap<String, MutableList<String>>()
    val data = Path.of(path)
    val stamps = Object2ObjectOpenHashMap<String, Long>()
    timedLoad("config file path") {
        val dirs = data.resolve("dirs.txt").toFile().readLines()
        for (dir in dirs) {
            walkPath(map, data.resolve(dir), stamps)
        }
        map.size
    }
    val cacheModified = cacheModified(Path.of(cachePath))
    return ConfigFiles(map, stamps, cacheModified)
}

/**
 * Uses [Files.walkFileTree] as it reuses the attributes read while listing each directory
 * instead of querying every file individually which is slow on Windows.
 * Symbolic links aren't followed as resolving them disables that reuse (~4x slower).
 */
private fun walkPath(
    map: MutableMap<String, MutableList<String>>,
    dir: Path,
    stamps: MutableMap<String, Long>,
) {
    Files.walkFileTree(
        dir,
        object : SimpleFileVisitor<Path>() {
            override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                val name = file.name
                if (!name.endsWith(".toml")) {
                    return FileVisitResult.CONTINUE
                }
                val extension = name.substringAfter('.')
                map.getOrPut(extension) { ObjectArrayList() }.add(file.pathString)
                val modified = attrs.lastModifiedTime().toMillis()
                stamps[file.pathString] = attrs.size() xor (modified * -7046029254386353131L)
                return FileVisitResult.CONTINUE
            }
        },
    )
}

private fun cacheModified(dir: Path = Path.of(Settings["storage.cache.path"])): Long {
    var latest = 0L
    Files.newDirectoryStream(dir).use { stream ->
        for (path in stream) {
            if (!path.extension.startsWith("dat") && !path.extension.startsWith("idx")) {
                continue
            }
            latest = maxOf(latest, Files.getLastModifiedTime(path).toMillis())
        }
    }
    return latest
}
