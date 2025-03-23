package world.gregs.voidps.engine.data

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import world.gregs.voidps.engine.timedLoad
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.name
import kotlin.io.path.pathString

fun configFiles(): Map<String, List<String>> {
    val map = Object2ObjectOpenHashMap<String, MutableList<String>>()
    timedLoad("config file paths") {
        walkPath(map, Path.of(Settings["storage.data"]))
    }
    return map
}

private fun walkPath(map: MutableMap<String, MutableList<String>>, dir: Path): Int {
    // Exclusions
    val name = dir.name
    if (name == "saves" || name == "players" || name == "cache") {
        return 0
    }
    var count = 0
    for (path in Files.newDirectoryStream(dir)) {
        if (path.isDirectory()) {
            count += walkPath(map, path)
        } else {
            val extension = path.name.substringAfter('.')
            count++
            map.getOrPut(extension) { ObjectArrayList() }.add(path.pathString)
        }
    }
    return count
}