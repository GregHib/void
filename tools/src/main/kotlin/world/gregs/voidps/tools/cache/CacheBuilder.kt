package world.gregs.voidps.tools.cache

import com.displee.cache.CacheLibrary
import world.gregs.voidps.tools.convert.DefinitionsParameterConverter
import world.gregs.voidps.tools.convert.InventoryConverter
import world.gregs.voidps.tools.map.MapPacker
import java.io.File

/**
 * Automatically builds a cache from scratch with all modifications
 */
object CacheBuilder {

    @JvmStatic
    fun main(args: Array<String>) {
        val target = File("./data/cache/")

        val cache727 = File("${System.getProperty("user.home")}/Downloads/727 cache with most xteas/")
        if (!cache727.exists()) {
            System.err.println("Unable to find '727 cache with most xteas' please download manually.")
            return
        }

        checkCacheOverride(target)

        val temp = File("./temp/cache/")
        temp.mkdir()
        val path = temp.resolve("build/")
        path.deleteRecursively()
        path.mkdirs()

        println("Finding original cache...")
        val source = OpenRS2.downloadCache(temp.resolve("cache-634/"), 283)
        source.copyRecursively(path)
        val xteas = OpenRS2.getKeys(283)

        println("Finding extra caches to take maps from...")
        // Take config data from other revisions
        val other = OpenRS2.downloadCache(temp.resolve("cache-718/"), 302)
        InventoryConverter.convert(path, other)
        DefinitionsParameterConverter.convert(path, other)

        // Take maps from other revisions
        val cache681 = OpenRS2.downloadCache(temp.resolve("cache-681/"), 280)
        val xteas681 = OpenRS2.getKeys(280)
        val cache537 = OpenRS2.downloadCache(temp.resolve("cache-537/"), 257)
        MapPacker.pack634(path, xteas, cache727, Xteas(), cache681, xteas681, cache537)

        // Further improvements
        val library = CacheLibrary(path.path)
        RemoveXteas.remove(library, xteas)
        RemoveBzip2.remove(library)
        println("Rebuilding cache.")
        library.rebuild(target)
        addEmptyIndexFiles(target, library.last()?.id ?: 0)
    }

    private fun checkCacheOverride(path: File) {
        val idx = path.resolve("main_file_cache.dat2")
        if (idx.exists()) {
            println("Cache exists at '${path}' continuing will override.")
            System.err.println("Continuing will delete the current cache. Are you sure?")
            if (!readln().startsWith("y", ignoreCase = true)) {
                println("Cancelled.")
                return
            }
        }
        if (!path.exists()) {
            path.mkdirs()
            return
        }
        for (file in path.listFiles() ?: return) {
            if (!file.isFile || file.nameWithoutExtension != "main_file_cache") {
                continue
            }
            file.delete()
        }
    }

    private fun addEmptyIndexFiles(target: File, lastIndex: Int) {
        for (i in 0..lastIndex) {
            val file = target.resolve("main_file_cache.idx$i")
            if (file.exists()) {
                continue
            }
            println("Filling in blank index $i.")
            file.createNewFile()
        }
    }
}