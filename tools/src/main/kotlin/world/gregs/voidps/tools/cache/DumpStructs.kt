package world.gregs.voidps.tools.cache

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.config.decoder.StructDecoder
import world.gregs.voidps.engine.data.Settings
import java.io.File

object DumpStructs {
    @JvmStatic
    fun main(args: Array<String>) {
        Settings.load()
        val cache: Cache = CacheDelegate(Settings["storage.cache.path"])
        val decoder = StructDecoder().load(cache)
        val builder = StringBuilder()
        for (i in decoder.indices) {
            val def = decoder.getOrNull(i) ?: continue
            builder.append(def.toString()).append("\n")
        }
        File("structs.txt").writeText(builder.toString())
    }
}