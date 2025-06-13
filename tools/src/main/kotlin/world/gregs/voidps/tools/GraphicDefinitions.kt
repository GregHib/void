package world.gregs.voidps.tools

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.definition.decoder.GraphicDecoder
import world.gregs.voidps.engine.data.Settings

object GraphicDefinitions {
    @JvmStatic
    fun main(args: Array<String>) {
        Settings.load()
        val cache: Cache = CacheDelegate(Settings["storage.cache.path"])
        val decoder = GraphicDecoder().load(cache)
        println(decoder[212])
        for (i in decoder.indices) {
            val def = decoder.getOrNull(i) ?: continue
            if (def.modelId == 3139) {
                println(def)
            }
        }
    }
}
